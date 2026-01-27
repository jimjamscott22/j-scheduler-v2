package com.jscheduler.service;

import com.jscheduler.model.Assignment;
import com.jscheduler.model.AssignmentStatus;
import com.jscheduler.model.Course;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SearchService {

    private final CourseService courseService;
    private final AssignmentService assignmentService;

    public SearchService(CourseService courseService, AssignmentService assignmentService) {
        this.courseService = courseService;
        this.assignmentService = assignmentService;
    }

    public record SearchResult(String type, String id, String title, String subtitle, Course course) {
    }

    public List<SearchResult> search(String query) {
        if (query == null || query.trim().isEmpty()) {
            return new ArrayList<>();
        }

        List<SearchResult> results = new ArrayList<>();

        // Search courses
        courseService.searchCourses(query).forEach(course ->
            results.add(new SearchResult(
                "Course",
                course.getId(),
                course.getDisplayName(),
                "Professor: " + course.getProfessor(),
                course
            ))
        );

        // Search assignments
        assignmentService.searchAssignments(query).forEach(assignment -> {
            Course course = courseService.getCourseById(assignment.getCourseId()).orElse(null);
            String courseName = course != null ? course.getDisplayName() : "Unknown Course";
            results.add(new SearchResult(
                "Assignment",
                assignment.getId(),
                assignment.getTitle(),
                courseName + " | Due: " + (assignment.getDueDate() != null ?
                    assignment.getDueDate().toLocalDate().toString() : "No date"),
                course
            ));
        });

        return results;
    }

    public List<Assignment> filterAssignments(FilterCriteria criteria) {
        Stream<Assignment> stream = assignmentService.getAllAssignments().stream();

        if (criteria.courseId != null) {
            stream = stream.filter(a -> criteria.courseId.equals(a.getCourseId()));
        }

        if (criteria.status != null) {
            stream = stream.filter(a -> a.getStatus() == criteria.status);
        }

        if (criteria.startDate != null) {
            stream = stream.filter(a -> a.getDueDate() != null &&
                    !a.getDueDate().isBefore(criteria.startDate));
        }

        if (criteria.endDate != null) {
            stream = stream.filter(a -> a.getDueDate() != null &&
                    !a.getDueDate().isAfter(criteria.endDate));
        }

        if (criteria.overdueOnly) {
            stream = stream.filter(Assignment::isOverdue);
        }

        return stream
                .sorted(Comparator.comparing(Assignment::getDueDate,
                        Comparator.nullsLast(Comparator.naturalOrder())))
                .collect(Collectors.toList());
    }

    public static class FilterCriteria {
        private String courseId;
        private AssignmentStatus status;
        private LocalDateTime startDate;
        private LocalDateTime endDate;
        private boolean overdueOnly;

        public FilterCriteria() {
        }

        public FilterCriteria setCourseId(String courseId) {
            this.courseId = courseId;
            return this;
        }

        public FilterCriteria setStatus(AssignmentStatus status) {
            this.status = status;
            return this;
        }

        public FilterCriteria setDateRange(LocalDateTime start, LocalDateTime end) {
            this.startDate = start;
            this.endDate = end;
            return this;
        }

        public FilterCriteria setOverdueOnly(boolean overdueOnly) {
            this.overdueOnly = overdueOnly;
            return this;
        }
    }
}
