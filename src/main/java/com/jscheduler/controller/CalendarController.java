package com.jscheduler.controller;

import com.jscheduler.model.Assignment;
import com.jscheduler.service.AssignmentService;
import com.jscheduler.util.DateUtil;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;

public class CalendarController {

    @FXML private Label monthYearLabel;
    @FXML private GridPane calendarGrid;
    @FXML private VBox upcomingList;

    private AssignmentService assignmentService;
    private YearMonth currentMonth;

    @FXML
    public void initialize() {
        currentMonth = YearMonth.now();
    }

    public void setServices(AssignmentService assignmentService) {
        this.assignmentService = assignmentService;
        refreshCalendar();
        refreshUpcomingList();
    }

    @FXML
    private void handlePreviousMonth() {
        currentMonth = currentMonth.minusMonths(1);
        refreshCalendar();
    }

    @FXML
    private void handleNextMonth() {
        currentMonth = currentMonth.plusMonths(1);
        refreshCalendar();
    }

    @FXML
    private void handleToday() {
        currentMonth = YearMonth.now();
        refreshCalendar();
    }

    private void refreshCalendar() {
        if (monthYearLabel != null) {
            monthYearLabel.setText(currentMonth.getMonth().getDisplayName(TextStyle.FULL, Locale.getDefault())
                + " " + currentMonth.getYear());
        }

        if (calendarGrid == null) return;

        calendarGrid.getChildren().clear();

        // Add day headers
        DayOfWeek[] days = DayOfWeek.values();
        for (int i = 0; i < 7; i++) {
            Label dayLabel = new Label(days[i].getDisplayName(TextStyle.SHORT, Locale.getDefault()));
            dayLabel.getStyleClass().add("calendar-header");
            GridPane.setMargin(dayLabel, new Insets(5));
            calendarGrid.add(dayLabel, i, 0);
        }

        // Get first day of month and calculate offset
        LocalDate firstOfMonth = currentMonth.atDay(1);
        int dayOfWeekOffset = firstOfMonth.getDayOfWeek().getValue() - 1; // Monday = 0

        // Get assignments for this month
        LocalDate monthStart = currentMonth.atDay(1);
        LocalDate monthEnd = currentMonth.atEndOfMonth();
        List<Assignment> monthAssignments = assignmentService.getAssignmentsBetweenDates(
            DateUtil.startOfDay(monthStart),
            DateUtil.endOfDay(monthEnd)
        );

        // Fill in the days
        int daysInMonth = currentMonth.lengthOfMonth();
        int row = 1;
        int col = dayOfWeekOffset;

        for (int day = 1; day <= daysInMonth; day++) {
            LocalDate date = currentMonth.atDay(day);
            VBox dayCell = createDayCell(date, monthAssignments);

            calendarGrid.add(dayCell, col, row);

            col++;
            if (col > 6) {
                col = 0;
                row++;
            }
        }
    }

    private VBox createDayCell(LocalDate date, List<Assignment> monthAssignments) {
        VBox cell = new VBox(2);
        cell.getStyleClass().add("calendar-cell");
        cell.setPadding(new Insets(5));
        cell.setMinSize(80, 60);

        // Day number
        Label dayNumber = new Label(String.valueOf(date.getDayOfMonth()));
        dayNumber.getStyleClass().add("calendar-day-number");

        if (date.equals(LocalDate.now())) {
            cell.getStyleClass().add("calendar-today");
        }

        cell.getChildren().add(dayNumber);

        // Add assignment indicators
        long assignmentCount = monthAssignments.stream()
            .filter(a -> a.getDueDate() != null && a.getDueDate().toLocalDate().equals(date))
            .count();

        if (assignmentCount > 0) {
            Label indicator = new Label(assignmentCount + " due");
            indicator.getStyleClass().add("calendar-assignment-indicator");

            // Build tooltip with assignment names
            StringBuilder tooltipText = new StringBuilder();
            monthAssignments.stream()
                .filter(a -> a.getDueDate() != null && a.getDueDate().toLocalDate().equals(date))
                .forEach(a -> {
                    if (tooltipText.length() > 0) tooltipText.append("\n");
                    tooltipText.append("- ").append(a.getTitle());
                });

            Tooltip tooltip = new Tooltip(tooltipText.toString());
            Tooltip.install(indicator, tooltip);

            cell.getChildren().add(indicator);
        }

        return cell;
    }

    private void refreshUpcomingList() {
        if (upcomingList == null || assignmentService == null) return;

        upcomingList.getChildren().clear();

        List<Assignment> upcoming = assignmentService.getUpcomingAssignments(14);

        if (upcoming.isEmpty()) {
            Label emptyLabel = new Label("No upcoming assignments in the next 2 weeks");
            emptyLabel.getStyleClass().add("empty-message");
            upcomingList.getChildren().add(emptyLabel);
            return;
        }

        for (Assignment assignment : upcoming) {
            VBox item = new VBox(2);
            item.getStyleClass().add("upcoming-item");
            item.setPadding(new Insets(5));

            Label titleLabel = new Label(assignment.getTitle());
            titleLabel.getStyleClass().add("upcoming-title");

            Label dateLabel = new Label(DateUtil.getRelativeTime(assignment.getDueDate()));
            dateLabel.getStyleClass().add("upcoming-date");

            item.getChildren().addAll(titleLabel, dateLabel);
            upcomingList.getChildren().add(item);
        }
    }
}
