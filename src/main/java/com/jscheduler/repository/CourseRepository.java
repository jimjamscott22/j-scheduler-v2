package com.jscheduler.repository;

import com.jscheduler.model.Course;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Course data access.
 * Abstracts storage implementation details from services.
 * Implementations can use JSON, MySQL, or other storage backends.
 */
public interface CourseRepository {

    /**
     * Retrieves all courses from storage.
     * @return List of all courses
     */
    List<Course> getAllCourses();

    /**
     * Retrieves a course by its unique identifier.
     * @param id The course ID
     * @return Optional containing the course if found
     */
    Optional<Course> getCourseById(String id);

    /**
     * Adds a new course to storage.
     * @param course The course to add
     */
    void addCourse(Course course);

    /**
     * Updates an existing course in storage.
     * @param course The course with updated information
     */
    void updateCourse(Course course);

    /**
     * Deletes a course from storage.
     * @param courseId The ID of the course to delete
     */
    void deleteCourse(String courseId);

    /**
     * Loads data from storage (for initialization).
     * No-op for database implementations.
     */
    void load();

    /**
     * Saves data to storage.
     * No-op for database implementations (data persisted immediately).
     */
    void save();
}
