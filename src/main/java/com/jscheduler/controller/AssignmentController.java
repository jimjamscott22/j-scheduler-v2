package com.jscheduler.controller;

import com.jscheduler.model.Assignment;
import com.jscheduler.model.AssignmentStatus;
import com.jscheduler.model.Course;
import com.jscheduler.service.AssignmentService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class AssignmentController {

    @FXML private TextField titleField;
    @FXML private TextArea descriptionArea;
    @FXML private DatePicker dueDatePicker;
    @FXML private Spinner<Integer> dueHourSpinner;
    @FXML private Spinner<Integer> dueMinuteSpinner;
    @FXML private ComboBox<AssignmentStatus> statusComboBox;
    @FXML private TextArea notesArea;
    @FXML private Label courseLabel;

    private AssignmentService assignmentService;
    private Course course;
    private Assignment editingAssignment;

    @FXML
    public void initialize() {
        // Setup status combo box
        statusComboBox.setItems(FXCollections.observableArrayList(AssignmentStatus.values()));
        statusComboBox.setValue(AssignmentStatus.NOT_STARTED);

        // Setup time spinners
        SpinnerValueFactory<Integer> hourFactory =
            new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 23, 23);
        SpinnerValueFactory<Integer> minuteFactory =
            new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 59, 59);

        dueHourSpinner.setValueFactory(hourFactory);
        dueMinuteSpinner.setValueFactory(minuteFactory);

        // Set default due date to one week from now
        dueDatePicker.setValue(LocalDate.now().plusWeeks(1));
    }

    public void setServices(AssignmentService assignmentService, Course course) {
        this.assignmentService = assignmentService;
        this.course = course;
        if (course != null && courseLabel != null) {
            courseLabel.setText(course.getDisplayName());
        }
    }

    public void setAssignment(Assignment assignment) {
        this.editingAssignment = assignment;
        if (assignment != null) {
            titleField.setText(assignment.getTitle());
            descriptionArea.setText(assignment.getDescription());
            notesArea.setText(assignment.getNotes());
            statusComboBox.setValue(assignment.getStatus());

            if (assignment.getDueDate() != null) {
                dueDatePicker.setValue(assignment.getDueDate().toLocalDate());
                dueHourSpinner.getValueFactory().setValue(assignment.getDueDate().getHour());
                dueMinuteSpinner.getValueFactory().setValue(assignment.getDueDate().getMinute());
            }
        }
    }

    public Assignment createAssignment() {
        String title = titleField.getText().trim();
        String description = descriptionArea.getText().trim();
        String notes = notesArea.getText().trim();
        AssignmentStatus status = statusComboBox.getValue();
        LocalDate dueDate = dueDatePicker.getValue();
        int hour = dueHourSpinner.getValue();
        int minute = dueMinuteSpinner.getValue();

        if (title.isEmpty() || dueDate == null) {
            return null;
        }

        LocalDateTime dueDateTime = LocalDateTime.of(dueDate, LocalTime.of(hour, minute));

        if (editingAssignment != null) {
            editingAssignment.setTitle(title);
            editingAssignment.setDescription(description);
            editingAssignment.setNotes(notes);
            editingAssignment.setStatus(status);
            editingAssignment.setDueDate(dueDateTime);
            editingAssignment.setSubmissionDeadline(dueDateTime);
            assignmentService.updateAssignment(editingAssignment);
            return editingAssignment;
        } else {
            Assignment assignment = assignmentService.createAssignment(course.getId(), title, dueDateTime);
            assignment.setDescription(description);
            assignment.setNotes(notes);
            assignment.setStatus(status);
            assignmentService.updateAssignment(assignment);
            return assignment;
        }
    }

    public boolean validate() {
        return !titleField.getText().trim().isEmpty() &&
               dueDatePicker.getValue() != null;
    }

    @FXML
    private void handleMarkSubmitted() {
        statusComboBox.setValue(AssignmentStatus.SUBMITTED);
    }

    @FXML
    private void handleMarkInProgress() {
        statusComboBox.setValue(AssignmentStatus.IN_PROGRESS);
    }
}
