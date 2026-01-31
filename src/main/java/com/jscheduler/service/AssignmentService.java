package com.jscheduler.service;

import com.jscheduler.model.Assignment;
import com.jscheduler.model.AssignmentStatus;
import com.jscheduler.model.Course;
import com.jscheduler.repository.CourseRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class AssignmentService {

    private final CourseRepository repository;

    public AssignmentService(CourseRepository repository) {
        this.repository = repository;
    }

    public List<Assignment> getAllAssignments() {
        return repository.getAllCourses().stream()
                .flatMap(c -> c.getAssignments().stream())
                .collect(Collectors.toList());
    }

    public List<Assignment> getAssignmentsByCourse(String courseId) {
        return repository.getCourseById(courseId)
                .map(Course::getAssignments)
                .orElse(new ArrayList<>());
    }

    public Optional<Assignment> getAssignmentById(String assignmentId) {
        return getAllAssignments().stream()
                .filter(a -> a.getId().equals(assignmentId))
                .findFirst();
    }

    public Assignment createAssignment(String courseId, String title, LocalDateTime dueDate) {
        Optional<Course> courseOpt = repository.getCourseById(courseId);
        if (courseOpt.isPresent()) {
            Course course = courseOpt.get();
            Assignment assignment = new Assignment(title, dueDate);
            course.addAssignment(assignment);
            repository.updateCourse(course);
            return assignment;
        }
        throw new IllegalArgumentException("Course not found: " + courseId);
    }

    public void updateAssignment(Assignment assignment) {
        repository.getAllCourses().stream()
                .filter(c -> c.getId().equals(assignment.getCourseId()))
                .findFirst()
                .ifPresent(course -> {
                    List<Assignment> assignments = course.getAssignments();
                    for (int i = 0; i < assignments.size(); i++) {
                        if (assignments.get(i).getId().equals(assignment.getId())) {
                            assignments.set(i, assignment);
                            repository.updateCourse(course);
                            return;
                        }
                    }
                });
    }

    public void deleteAssignment(String courseId, String assignmentId) {
        repository.getCourseById(courseId).ifPresent(course -> {
            course.getAssignments().removeIf(a -> a.getId().equals(assignmentId));
            repository.updateCourse(course);
        });
    }

    public List<Assignment> getAssignmentsByStatus(AssignmentStatus status) {
        return getAllAssignments().stream()
                .filter(a -> a.getStatus() == status)
                .collect(Collectors.toList());
    }

    public List<Assignment> getUpcomingAssignments(int daysAhead) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime limit = now.plusDays(daysAhead);

        return getAllAssignments().stream()
                .filter(a -> a.getDueDate() != null)
                .filter(a -> a.getDueDate().isAfter(now) && a.getDueDate().isBefore(limit))
                .filter(a -> a.getStatus() != AssignmentStatus.SUBMITTED)
                .sorted(Comparator.comparing(Assignment::getDueDate))
                .collect(Collectors.toList());
    }

    public List<Assignment> getOverdueAssignments() {
        return getAllAssignments().stream()
                .filter(Assignment::isOverdue)
                .sorted(Comparator.comparing(Assignment::getDueDate))
                .collect(Collectors.toList());
    }

    public List<Assignment> searchAssignments(String query) {
        String lowerQuery = query.toLowerCase();
        return getAllAssignments().stream()
                .filter(a ->
                    (a.getTitle() != null && a.getTitle().toLowerCase().contains(lowerQuery)) ||
                    (a.getDescription() != null && a.getDescription().toLowerCase().contains(lowerQuery)) ||
                    (a.getNotes() != null && a.getNotes().toLowerCase().contains(lowerQuery)))
                .collect(Collectors.toList());
    }

    public List<Assignment> getAssignmentsBetweenDates(LocalDateTime start, LocalDateTime end) {
        return getAllAssignments().stream()
                .filter(a -> a.getDueDate() != null)
                .filter(a -> !a.getDueDate().isBefore(start) && !a.getDueDate().isAfter(end))
                .sorted(Comparator.comparing(Assignment::getDueDate))
                .collect(Collectors.toList());
    }
}
