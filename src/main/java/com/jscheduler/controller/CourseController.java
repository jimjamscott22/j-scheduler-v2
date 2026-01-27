package com.jscheduler.controller;

import com.jscheduler.model.Course;
import com.jscheduler.model.Semester;
import com.jscheduler.service.CourseService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.time.Year;

public class CourseController {

    @FXML private TextField nameField;
    @FXML private TextField codeField;
    @FXML private TextField professorField;
    @FXML private TextArea descriptionArea;
    @FXML private ComboBox<Semester.Season> seasonComboBox;
    @FXML private ComboBox<Integer> yearComboBox;

    private CourseService courseService;
    private Course editingCourse;

    @FXML
    public void initialize() {
        // Setup season combo box
        seasonComboBox.setItems(FXCollections.observableArrayList(Semester.Season.values()));
        seasonComboBox.setValue(Semester.Season.FALL);

        // Setup year combo box (current year and next 2 years)
        int currentYear = Year.now().getValue();
        yearComboBox.setItems(FXCollections.observableArrayList(
            currentYear - 1, currentYear, currentYear + 1, currentYear + 2
        ));
        yearComboBox.setValue(currentYear);
    }

    public void setServices(CourseService courseService) {
        this.courseService = courseService;
    }

    public void setCourse(Course course) {
        this.editingCourse = course;
        if (course != null) {
            nameField.setText(course.getName());
            codeField.setText(course.getCode());
            professorField.setText(course.getProfessor());
            descriptionArea.setText(course.getDescription());

            if (course.getSemester() != null) {
                seasonComboBox.setValue(course.getSemester().getSeason());
                yearComboBox.setValue(course.getSemester().getYear());
            }
        }
    }

    public Course createCourse() {
        String name = nameField.getText().trim();
        String code = codeField.getText().trim();
        String professor = professorField.getText().trim();
        String description = descriptionArea.getText().trim();
        Semester.Season season = seasonComboBox.getValue();
        Integer year = yearComboBox.getValue();

        if (name.isEmpty() || code.isEmpty()) {
            return null;
        }

        Semester semester = new Semester(season, year);

        if (editingCourse != null) {
            editingCourse.setName(name);
            editingCourse.setCode(code);
            editingCourse.setProfessor(professor);
            editingCourse.setDescription(description);
            editingCourse.setSemester(semester);
            courseService.updateCourse(editingCourse);
            return editingCourse;
        } else {
            Course course = courseService.createCourse(name, code, professor, semester);
            course.setDescription(description);
            courseService.updateCourse(course);
            return course;
        }
    }

    public boolean validate() {
        return !nameField.getText().trim().isEmpty() &&
               !codeField.getText().trim().isEmpty();
    }
}
