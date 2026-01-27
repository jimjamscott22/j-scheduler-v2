package com.jscheduler.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class Course {

    private String id;
    private String name;
    private String code;
    private String description;
    private String professor;
    private Semester semester;
    private List<Assignment> assignments;

    public Course() {
        this.id = UUID.randomUUID().toString();
        this.assignments = new ArrayList<>();
    }

    public Course(String name, String code, String professor, Semester semester) {
        this();
        this.name = name;
        this.code = code;
        this.professor = professor;
        this.semester = semester;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getProfessor() {
        return professor;
    }

    public void setProfessor(String professor) {
        this.professor = professor;
    }

    public Semester getSemester() {
        return semester;
    }

    public void setSemester(Semester semester) {
        this.semester = semester;
    }

    public List<Assignment> getAssignments() {
        return assignments;
    }

    public void setAssignments(List<Assignment> assignments) {
        this.assignments = assignments;
    }

    public void addAssignment(Assignment assignment) {
        if (this.assignments == null) {
            this.assignments = new ArrayList<>();
        }
        assignment.setCourseId(this.id);
        this.assignments.add(assignment);
    }

    public void removeAssignment(Assignment assignment) {
        if (this.assignments != null) {
            this.assignments.remove(assignment);
        }
    }

    public String getDisplayName() {
        return code != null ? code + " - " + name : name;
    }

    @Override
    public String toString() {
        return getDisplayName();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Course course = (Course) o;
        return Objects.equals(id, course.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
