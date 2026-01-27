package com.jscheduler.service;

import com.jscheduler.model.Assignment;
import javafx.application.Platform;
import javafx.scene.control.Alert;

import java.awt.*;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class NotificationService {

    private final AssignmentService assignmentService;
    private final ScheduledExecutorService scheduler;
    private boolean isRunning;

    public NotificationService(AssignmentService assignmentService) {
        this.assignmentService = assignmentService;
        this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "NotificationService");
            t.setDaemon(true);
            return t;
        });
        this.isRunning = false;
    }

    public void start() {
        if (isRunning) return;
        isRunning = true;

        // Check for upcoming assignments every hour
        scheduler.scheduleAtFixedRate(this::checkUpcomingDeadlines, 0, 1, TimeUnit.HOURS);
    }

    public void stop() {
        isRunning = false;
        scheduler.shutdown();
    }

    private void checkUpcomingDeadlines() {
        // Get assignments due within 3 days
        List<Assignment> upcoming = assignmentService.getUpcomingAssignments(3);

        for (Assignment assignment : upcoming) {
            long hoursUntilDue = ChronoUnit.HOURS.between(LocalDateTime.now(), assignment.getDueDate());

            if (hoursUntilDue <= 24) {
                showNotification("Assignment Due Soon!",
                        assignment.getTitle() + " is due in " + hoursUntilDue + " hours");
            } else if (hoursUntilDue <= 72) {
                long daysUntilDue = ChronoUnit.DAYS.between(LocalDateTime.now(), assignment.getDueDate());
                showNotification("Upcoming Deadline",
                        assignment.getTitle() + " is due in " + daysUntilDue + " days");
            }
        }

        // Check for overdue assignments
        List<Assignment> overdue = assignmentService.getOverdueAssignments();
        if (!overdue.isEmpty()) {
            showNotification("Overdue Assignments",
                    "You have " + overdue.size() + " overdue assignment(s)");
        }
    }

    private void showNotification(String title, String message) {
        // Try system tray notification first
        if (SystemTray.isSupported()) {
            try {
                showSystemTrayNotification(title, message);
                return;
            } catch (Exception e) {
                // Fall back to JavaFX alert
            }
        }

        // Fallback to JavaFX alert
        Platform.runLater(() -> showAlertNotification(title, message));
    }

    private void showSystemTrayNotification(String title, String message) throws AWTException {
        SystemTray tray = SystemTray.getSystemTray();
        Image image = Toolkit.getDefaultToolkit().createImage(getClass().getResource("/images/icon.png"));

        TrayIcon trayIcon = new TrayIcon(image, "J-Scheduler");
        trayIcon.setImageAutoSize(true);
        tray.add(trayIcon);

        trayIcon.displayMessage(title, message, TrayIcon.MessageType.INFO);

        // Remove tray icon after a delay
        new Thread(() -> {
            try {
                Thread.sleep(5000);
                tray.remove(trayIcon);
            } catch (InterruptedException ignored) {
            }
        }).start();
    }

    private void showAlertNotification(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.show();
    }

    public void notifyNow(String title, String message) {
        showNotification(title, message);
    }
}
