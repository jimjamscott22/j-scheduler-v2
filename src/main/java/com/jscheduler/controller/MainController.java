package com.jscheduler.controller;

import com.jscheduler.model.Assignment;
import com.jscheduler.model.Course;
import com.jscheduler.repository.DataRepository;
import com.jscheduler.service.AssignmentService;
import com.jscheduler.service.CourseService;
import com.jscheduler.service.NotificationService;
import com.jscheduler.service.SearchService;
import com.jscheduler.util.DateUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public class MainController {

    @FXML private BorderPane rootPane;
    @FXML private ListView<Course> courseListView;
    @FXML private TableView<Assignment> assignmentTableView;
    @FXML private TableColumn<Assignment, String> titleColumn;
    @FXML private TableColumn<Assignment, String> dueDateColumn;
    @FXML private TableColumn<Assignment, String> statusColumn;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> filterComboBox;
    @FXML private Label statusLabel;
    @FXML private VBox dashboardPane;

    private DataRepository repository;
    private CourseService courseService;
    private AssignmentService assignmentService;
    private SearchService searchService;
    private NotificationService notificationService;

    private ObservableList<Course> courseList;
    private ObservableList<Assignment> assignmentList;

    @FXML
    public void initialize() {
        // Initialize repository and services
        repository = new DataRepository();
        courseService = new CourseService(repository);
        assignmentService = new AssignmentService(repository);
        searchService = new SearchService(courseService, assignmentService);
        notificationService = new NotificationService(assignmentService);

        // Initialize observable lists
        courseList = FXCollections.observableArrayList();
        assignmentList = FXCollections.observableArrayList();

        // Setup UI components
        setupCourseListView();
        setupAssignmentTableView();
        setupFilterComboBox();
        setupSearchField();

        // Load initial data
        refreshCourseList();
        refreshDashboard();

        // Start notification service
        notificationService.start();

        updateStatusLabel("Ready");
    }

    private void setupCourseListView() {
        courseListView.setItems(courseList);
        courseListView.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Course course, boolean empty) {
                super.updateItem(course, empty);
                setText(empty || course == null ? null : course.getDisplayName());
            }
        });

        courseListView.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldVal, newVal) -> {
                if (newVal != null) {
                    loadAssignmentsForCourse(newVal);
                }
            }
        );
    }

    private void setupAssignmentTableView() {
        assignmentTableView.setItems(assignmentList);

        titleColumn.setCellValueFactory(cellData ->
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getTitle()));

        dueDateColumn.setCellValueFactory(cellData ->
            new javafx.beans.property.SimpleStringProperty(
                DateUtil.formatDateTime(cellData.getValue().getDueDate())));

        statusColumn.setCellValueFactory(cellData ->
            new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getStatus().getDisplayName()));

        assignmentTableView.setRowFactory(tv -> {
            TableRow<Assignment> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    editAssignment(row.getItem());
                }
            });
            return row;
        });
    }

    private void setupFilterComboBox() {
        filterComboBox.setItems(FXCollections.observableArrayList(
            "All", "Not Started", "In Progress", "Submitted", "Late", "Overdue"
        ));
        filterComboBox.setValue("All");
        filterComboBox.setOnAction(e -> applyFilter());
    }

    private void setupSearchField() {
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !newVal.isEmpty()) {
                performSearch(newVal);
            } else {
                refreshAssignmentList();
            }
        });
    }

    private void refreshCourseList() {
        courseList.clear();
        courseList.addAll(courseService.getAllCourses());
    }

    private void refreshAssignmentList() {
        Course selectedCourse = courseListView.getSelectionModel().getSelectedItem();
        if (selectedCourse != null) {
            loadAssignmentsForCourse(selectedCourse);
        } else {
            assignmentList.clear();
            assignmentList.addAll(assignmentService.getAllAssignments());
        }
    }

    private void loadAssignmentsForCourse(Course course) {
        assignmentList.clear();
        assignmentList.addAll(assignmentService.getAssignmentsByCourse(course.getId()));
    }

    private void refreshDashboard() {
        // Update dashboard statistics
        int totalCourses = courseService.getAllCourses().size();
        int totalAssignments = assignmentService.getAllAssignments().size();
        int overdueCount = assignmentService.getOverdueAssignments().size();
        int upcomingCount = assignmentService.getUpcomingAssignments(7).size();

        updateStatusLabel(String.format("Courses: %d | Assignments: %d | Upcoming: %d | Overdue: %d",
            totalCourses, totalAssignments, upcomingCount, overdueCount));
    }

    @FXML
    private void handleAddCourse() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/course-dialog.fxml"));
            Parent dialogContent = loader.load();
            CourseController controller = loader.getController();
            controller.setServices(courseService);

            Dialog<Course> dialog = new Dialog<>();
            dialog.setTitle("Add Course");
            dialog.getDialogPane().setContent(dialogContent);
            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

            dialog.setResultConverter(buttonType -> {
                if (buttonType == ButtonType.OK) {
                    return controller.createCourse();
                }
                return null;
            });

            Optional<Course> result = dialog.showAndWait();
            result.ifPresent(course -> {
                refreshCourseList();
                refreshDashboard();
                updateStatusLabel("Course added: " + course.getDisplayName());
            });
        } catch (IOException e) {
            showError("Error", "Failed to open course dialog: " + e.getMessage());
        }
    }

    @FXML
    private void handleEditCourse() {
        Course selected = courseListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("No Selection", "Please select a course to edit.");
            return;
        }
        // TODO: Implement edit course dialog
    }

    @FXML
    private void handleDeleteCourse() {
        Course selected = courseListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("No Selection", "Please select a course to delete.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Delete");
        confirm.setHeaderText("Delete Course");
        confirm.setContentText("Are you sure you want to delete '" + selected.getDisplayName() +
            "' and all its assignments?");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                courseService.deleteCourse(selected.getId());
                refreshCourseList();
                assignmentList.clear();
                refreshDashboard();
                updateStatusLabel("Course deleted: " + selected.getDisplayName());
            }
        });
    }

    @FXML
    private void handleAddAssignment() {
        Course selectedCourse = courseListView.getSelectionModel().getSelectedItem();
        if (selectedCourse == null) {
            showError("No Course Selected", "Please select a course first.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/assignment-dialog.fxml"));
            Parent dialogContent = loader.load();
            AssignmentController controller = loader.getController();
            controller.setServices(assignmentService, selectedCourse);

            Dialog<Assignment> dialog = new Dialog<>();
            dialog.setTitle("Add Assignment");
            dialog.getDialogPane().setContent(dialogContent);
            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

            dialog.setResultConverter(buttonType -> {
                if (buttonType == ButtonType.OK) {
                    return controller.createAssignment();
                }
                return null;
            });

            Optional<Assignment> result = dialog.showAndWait();
            result.ifPresent(assignment -> {
                loadAssignmentsForCourse(selectedCourse);
                refreshDashboard();
                updateStatusLabel("Assignment added: " + assignment.getTitle());
            });
        } catch (IOException e) {
            showError("Error", "Failed to open assignment dialog: " + e.getMessage());
        }
    }

    private void editAssignment(Assignment assignment) {
        // TODO: Implement edit assignment dialog
    }

    @FXML
    private void handleDeleteAssignment() {
        Assignment selected = assignmentTableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("No Selection", "Please select an assignment to delete.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Delete");
        confirm.setHeaderText("Delete Assignment");
        confirm.setContentText("Are you sure you want to delete '" + selected.getTitle() + "'?");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                assignmentService.deleteAssignment(selected.getCourseId(), selected.getId());
                refreshAssignmentList();
                refreshDashboard();
                updateStatusLabel("Assignment deleted: " + selected.getTitle());
            }
        });
    }

    private void performSearch(String query) {
        List<SearchService.SearchResult> results = searchService.search(query);
        assignmentList.clear();

        for (SearchService.SearchResult result : results) {
            if ("Assignment".equals(result.type())) {
                assignmentService.getAssignmentById(result.id())
                    .ifPresent(assignmentList::add);
            }
        }
    }

    private void applyFilter() {
        String filter = filterComboBox.getValue();
        Course selectedCourse = courseListView.getSelectionModel().getSelectedItem();

        List<Assignment> filtered;
        if ("All".equals(filter)) {
            filtered = selectedCourse != null ?
                assignmentService.getAssignmentsByCourse(selectedCourse.getId()) :
                assignmentService.getAllAssignments();
        } else if ("Overdue".equals(filter)) {
            filtered = assignmentService.getOverdueAssignments();
        } else {
            var status = switch (filter) {
                case "Not Started" -> com.jscheduler.model.AssignmentStatus.NOT_STARTED;
                case "In Progress" -> com.jscheduler.model.AssignmentStatus.IN_PROGRESS;
                case "Submitted" -> com.jscheduler.model.AssignmentStatus.SUBMITTED;
                case "Late" -> com.jscheduler.model.AssignmentStatus.LATE;
                default -> null;
            };
            filtered = status != null ?
                assignmentService.getAssignmentsByStatus(status) :
                assignmentService.getAllAssignments();
        }

        assignmentList.clear();
        assignmentList.addAll(filtered);
    }

    @FXML
    private void handleShowCalendar() {
        // TODO: Implement calendar view
        showError("Coming Soon", "Calendar view is not yet implemented.");
    }

    @FXML
    private void handleRefresh() {
        refreshCourseList();
        refreshAssignmentList();
        refreshDashboard();
        updateStatusLabel("Data refreshed");
    }

    @FXML
    private void handleExit() {
        notificationService.stop();
        javafx.application.Platform.exit();
    }

    @FXML
    private void handleAbout() {
        Alert about = new Alert(Alert.AlertType.INFORMATION);
        about.setTitle("About J-Scheduler");
        about.setHeaderText("J-Scheduler v2.0.0");
        about.setContentText(
            "A desktop application for tracking college assignments.\n\n" +
            "Built with JavaFX and Java 21.\n\n" +
            "Keep track of your courses and never miss a deadline!"
        );
        about.showAndWait();
    }

    private void updateStatusLabel(String message) {
        if (statusLabel != null) {
            statusLabel.setText(message);
        }
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
