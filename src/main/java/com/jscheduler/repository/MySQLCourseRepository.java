package com.jscheduler.repository;

import com.jscheduler.config.DatabaseConfig;
import com.jscheduler.model.Assignment;
import com.jscheduler.model.AssignmentStatus;
import com.jscheduler.model.Course;
import com.jscheduler.model.Semester;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * MySQL implementation of the CourseRepository interface.
 * Manages course and assignment data in MySQL database with proper transaction handling.
 */
public class MySQLCourseRepository implements CourseRepository {

    private final DatabaseConfig dbConfig;

    public MySQLCourseRepository() {
        this.dbConfig = DatabaseConfig.getInstance();
        initializeSchema();
    }

    /**
     * Creates database tables if they don't exist.
     */
    private void initializeSchema() {
        String createCoursesTable = """
            CREATE TABLE IF NOT EXISTS courses (
                id VARCHAR(36) PRIMARY KEY,
                name VARCHAR(255) NOT NULL,
                code VARCHAR(50) NOT NULL,
                description TEXT,
                professor VARCHAR(255),
                semester_season VARCHAR(20),
                semester_year INT,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                INDEX idx_semester (semester_season, semester_year),
                INDEX idx_code (code),
                INDEX idx_name (name)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
            """;

        String createAssignmentsTable = """
            CREATE TABLE IF NOT EXISTS assignments (
                id VARCHAR(36) PRIMARY KEY,
                course_id VARCHAR(36) NOT NULL,
                title VARCHAR(255) NOT NULL,
                description TEXT,
                due_date DATETIME NOT NULL,
                submission_deadline DATETIME,
                status VARCHAR(20) NOT NULL,
                notes TEXT,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE,
                INDEX idx_course_id (course_id),
                INDEX idx_due_date (due_date),
                INDEX idx_status (status),
                INDEX idx_course_status (course_id, status)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
            """;

        try (Connection conn = dbConfig.getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.execute(createCoursesTable);
            stmt.execute(createAssignmentsTable);
            conn.commit();
            System.out.println("Database schema initialized successfully");

        } catch (SQLException e) {
            System.err.println("Failed to initialize schema: " + e.getMessage());
            throw new RuntimeException("Database initialization failed", e);
        }
    }

    @Override
    public List<Course> getAllCourses() {
        List<Course> courses = new ArrayList<>();
        String sql = "SELECT * FROM courses ORDER BY semester_year DESC, semester_season";

        try (Connection conn = dbConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Course course = mapResultSetToCourse(rs);
                loadAssignmentsForCourse(course, conn);
                courses.add(course);
            }
            conn.commit();

        } catch (SQLException e) {
            System.err.println("Failed to get all courses: " + e.getMessage());
            e.printStackTrace();
        }

        return courses;
    }

    @Override
    public Optional<Course> getCourseById(String id) {
        String sql = "SELECT * FROM courses WHERE id = ?";

        try (Connection conn = dbConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Course course = mapResultSetToCourse(rs);
                    loadAssignmentsForCourse(course, conn);
                    conn.commit();
                    return Optional.of(course);
                }
            }
            conn.commit();

        } catch (SQLException e) {
            System.err.println("Failed to get course by id: " + e.getMessage());
            e.printStackTrace();
        }

        return Optional.empty();
    }

    @Override
    public void addCourse(Course course) {
        String sql = """
            INSERT INTO courses (id, name, code, description, professor,
                                semester_season, semester_year)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """;

        try (Connection conn = dbConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, course.getId());
            stmt.setString(2, course.getName());
            stmt.setString(3, course.getCode());
            stmt.setString(4, course.getDescription());
            stmt.setString(5, course.getProfessor());

            if (course.getSemester() != null) {
                stmt.setString(6, course.getSemester().getSeason().name());
                stmt.setInt(7, course.getSemester().getYear());
            } else {
                stmt.setNull(6, Types.VARCHAR);
                stmt.setNull(7, Types.INTEGER);
            }

            stmt.executeUpdate();

            // Insert assignments if present
            if (course.getAssignments() != null) {
                for (Assignment assignment : course.getAssignments()) {
                    insertAssignment(assignment, conn);
                }
            }

            conn.commit();

        } catch (SQLException e) {
            System.err.println("Failed to add course: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to add course", e);
        }
    }

    @Override
    public void updateCourse(Course course) {
        String sql = """
            UPDATE courses
            SET name = ?, code = ?, description = ?, professor = ?,
                semester_season = ?, semester_year = ?
            WHERE id = ?
            """;

        Connection conn = null;
        try {
            conn = dbConfig.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setString(1, course.getName());
                stmt.setString(2, course.getCode());
                stmt.setString(3, course.getDescription());
                stmt.setString(4, course.getProfessor());

                if (course.getSemester() != null) {
                    stmt.setString(5, course.getSemester().getSeason().name());
                    stmt.setInt(6, course.getSemester().getYear());
                } else {
                    stmt.setNull(5, Types.VARCHAR);
                    stmt.setNull(6, Types.INTEGER);
                }

                stmt.setString(7, course.getId());
                stmt.executeUpdate();
            }

            // Sync assignments - delete all and re-insert
            deleteAllAssignmentsForCourse(course.getId(), conn);
            if (course.getAssignments() != null) {
                for (Assignment assignment : course.getAssignments()) {
                    insertAssignment(assignment, conn);
                }
            }

            conn.commit();

        } catch (SQLException e) {
            System.err.println("Failed to update course: " + e.getMessage());
            e.printStackTrace();
            rollback(conn);
            throw new RuntimeException("Failed to update course", e);
        } finally {
            closeConnection(conn);
        }
    }

    @Override
    public void deleteCourse(String courseId) {
        String sql = "DELETE FROM courses WHERE id = ?";

        try (Connection conn = dbConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, courseId);
            stmt.executeUpdate();
            conn.commit();
            // Assignments auto-deleted via CASCADE

        } catch (SQLException e) {
            System.err.println("Failed to delete course: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to delete course", e);
        }
    }

    @Override
    public void load() {
        // No-op for MySQL (connection pool handles initialization)
    }

    @Override
    public void save() {
        // No-op for MySQL (data persisted immediately via transactions)
    }

    // ===== Helper Methods =====

    private Course mapResultSetToCourse(ResultSet rs) throws SQLException {
        Course course = new Course();
        course.setId(rs.getString("id"));
        course.setName(rs.getString("name"));
        course.setCode(rs.getString("code"));
        course.setDescription(rs.getString("description"));
        course.setProfessor(rs.getString("professor"));

        String season = rs.getString("semester_season");
        int year = rs.getInt("semester_year");
        if (season != null && !rs.wasNull()) {
            course.setSemester(new Semester(
                Semester.Season.valueOf(season), year));
        }

        return course;
    }

    private void loadAssignmentsForCourse(Course course, Connection conn)
            throws SQLException {
        String sql = "SELECT * FROM assignments WHERE course_id = ? ORDER BY due_date";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, course.getId());

            try (ResultSet rs = stmt.executeQuery()) {
                List<Assignment> assignments = new ArrayList<>();
                while (rs.next()) {
                    assignments.add(mapResultSetToAssignment(rs));
                }
                course.setAssignments(assignments);
            }
        }
    }

    private Assignment mapResultSetToAssignment(ResultSet rs) throws SQLException {
        Assignment assignment = new Assignment();
        assignment.setId(rs.getString("id"));
        assignment.setCourseId(rs.getString("course_id"));
        assignment.setTitle(rs.getString("title"));
        assignment.setDescription(rs.getString("description"));

        Timestamp dueDate = rs.getTimestamp("due_date");
        if (dueDate != null) {
            assignment.setDueDate(dueDate.toLocalDateTime());
        }

        Timestamp submissionDeadline = rs.getTimestamp("submission_deadline");
        if (submissionDeadline != null) {
            assignment.setSubmissionDeadline(submissionDeadline.toLocalDateTime());
        }

        String status = rs.getString("status");
        if (status != null) {
            assignment.setStatus(AssignmentStatus.valueOf(status));
        }

        assignment.setNotes(rs.getString("notes"));

        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            assignment.setCreatedAt(createdAt.toLocalDateTime());
        }

        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) {
            assignment.setUpdatedAt(updatedAt.toLocalDateTime());
        }

        return assignment;
    }

    private void insertAssignment(Assignment assignment, Connection conn)
            throws SQLException {
        String sql = """
            INSERT INTO assignments
            (id, course_id, title, description, due_date, submission_deadline,
             status, notes, created_at, updated_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, assignment.getId());
            stmt.setString(2, assignment.getCourseId());
            stmt.setString(3, assignment.getTitle());
            stmt.setString(4, assignment.getDescription());
            stmt.setTimestamp(5, Timestamp.valueOf(assignment.getDueDate()));

            if (assignment.getSubmissionDeadline() != null) {
                stmt.setTimestamp(6, Timestamp.valueOf(
                    assignment.getSubmissionDeadline()));
            } else {
                stmt.setNull(6, Types.TIMESTAMP);
            }

            stmt.setString(7, assignment.getStatus().name());
            stmt.setString(8, assignment.getNotes());

            if (assignment.getCreatedAt() != null) {
                stmt.setTimestamp(9, Timestamp.valueOf(assignment.getCreatedAt()));
            } else {
                stmt.setTimestamp(9, Timestamp.valueOf(LocalDateTime.now()));
            }

            if (assignment.getUpdatedAt() != null) {
                stmt.setTimestamp(10, Timestamp.valueOf(assignment.getUpdatedAt()));
            } else {
                stmt.setTimestamp(10, Timestamp.valueOf(LocalDateTime.now()));
            }

            stmt.executeUpdate();
        }
    }

    private void deleteAllAssignmentsForCourse(String courseId, Connection conn)
            throws SQLException {
        String sql = "DELETE FROM assignments WHERE course_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, courseId);
            stmt.executeUpdate();
        }
    }

    private void rollback(Connection conn) {
        try {
            if (conn != null) {
                conn.rollback();
            }
        } catch (SQLException e) {
            System.err.println("Failed to rollback transaction: " + e.getMessage());
        }
    }

    private void closeConnection(Connection conn) {
        try {
            if (conn != null) {
                conn.close();
            }
        } catch (SQLException e) {
            System.err.println("Failed to close connection: " + e.getMessage());
        }
    }
}
