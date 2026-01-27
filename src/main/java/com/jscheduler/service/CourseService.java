package com.jscheduler.service;

import com.jscheduler.model.Course;
import com.jscheduler.model.Semester;
import com.jscheduler.repository.DataRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class CourseService {

    private final DataRepository repository;

    public CourseService(DataRepository repository) {
        this.repository = repository;
    }

    public List<Course> getAllCourses() {
        return repository.getAllCourses();
    }

    public Optional<Course> getCourseById(String id) {
        return repository.getCourseById(id);
    }

    public List<Course> getCoursesBySemester(Semester semester) {
        return repository.getAllCourses().stream()
                .filter(c -> c.getSemester() != null && c.getSemester().equals(semester))
                .collect(Collectors.toList());
    }

    public Course createCourse(String name, String code, String professor, Semester semester) {
        Course course = new Course(name, code, professor, semester);
        repository.addCourse(course);
        return course;
    }

    public void updateCourse(Course course) {
        repository.updateCourse(course);
    }

    public void deleteCourse(String courseId) {
        repository.deleteCourse(courseId);
    }

    public List<Course> searchCourses(String query) {
        String lowerQuery = query.toLowerCase();
        return repository.getAllCourses().stream()
                .filter(c ->
                    (c.getName() != null && c.getName().toLowerCase().contains(lowerQuery)) ||
                    (c.getCode() != null && c.getCode().toLowerCase().contains(lowerQuery)) ||
                    (c.getProfessor() != null && c.getProfessor().toLowerCase().contains(lowerQuery)))
                .collect(Collectors.toList());
    }
}
