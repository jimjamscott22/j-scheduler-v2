package com.jscheduler.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.jscheduler.model.Course;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DataRepository implements CourseRepository {

    private static final String DATA_DIR = "data";
    private static final String DATA_FILE = "scheduler-data.json";

    private final ObjectMapper objectMapper;
    private final Path dataFilePath;
    private List<Course> courses;

    public DataRepository() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        this.dataFilePath = Paths.get(DATA_DIR, DATA_FILE);
        this.courses = new ArrayList<>();

        initializeDataDirectory();
        load();
    }

    private void initializeDataDirectory() {
        try {
            Files.createDirectories(Paths.get(DATA_DIR));
        } catch (IOException e) {
            System.err.println("Failed to create data directory: " + e.getMessage());
        }
    }

    public void load() {
        File file = dataFilePath.toFile();
        if (file.exists()) {
            try {
                DataWrapper wrapper = objectMapper.readValue(file, DataWrapper.class);
                this.courses = wrapper.getCourses() != null ? wrapper.getCourses() : new ArrayList<>();
            } catch (IOException e) {
                System.err.println("Failed to load data: " + e.getMessage());
                this.courses = new ArrayList<>();
            }
        }
    }

    public void save() {
        try {
            DataWrapper wrapper = new DataWrapper(courses);
            objectMapper.writeValue(dataFilePath.toFile(), wrapper);
        } catch (IOException e) {
            System.err.println("Failed to save data: " + e.getMessage());
        }
    }

    // Course operations
    public List<Course> getAllCourses() {
        return new ArrayList<>(courses);
    }

    public Optional<Course> getCourseById(String id) {
        return courses.stream()
                .filter(c -> c.getId().equals(id))
                .findFirst();
    }

    public void addCourse(Course course) {
        courses.add(course);
        save();
    }

    public void updateCourse(Course course) {
        for (int i = 0; i < courses.size(); i++) {
            if (courses.get(i).getId().equals(course.getId())) {
                courses.set(i, course);
                save();
                return;
            }
        }
    }

    public void deleteCourse(String courseId) {
        courses.removeIf(c -> c.getId().equals(courseId));
        save();
    }

    // Inner class for JSON wrapper
    private static class DataWrapper {
        private List<Course> courses;

        public DataWrapper() {
            this.courses = new ArrayList<>();
        }

        public DataWrapper(List<Course> courses) {
            this.courses = courses;
        }

        public List<Course> getCourses() {
            return courses;
        }

        public void setCourses(List<Course> courses) {
            this.courses = courses;
        }
    }
}
