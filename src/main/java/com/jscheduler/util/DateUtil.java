package com.jscheduler.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public final class DateUtil {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MMM dd, yyyy");
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    private DateUtil() {
    }

    public static String formatDate(LocalDate date) {
        return date != null ? date.format(DATE_FORMATTER) : "";
    }

    public static String formatDateTime(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(DATETIME_FORMATTER) : "";
    }

    public static String formatTime(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(TIME_FORMATTER) : "";
    }

    public static String getRelativeTime(LocalDateTime dateTime) {
        if (dateTime == null) return "";

        LocalDateTime now = LocalDateTime.now();
        long days = ChronoUnit.DAYS.between(now, dateTime);
        long hours = ChronoUnit.HOURS.between(now, dateTime);

        if (days < 0) {
            return Math.abs(days) + " day(s) ago";
        } else if (days == 0) {
            if (hours < 0) {
                return Math.abs(hours) + " hour(s) ago";
            } else if (hours == 0) {
                return "Due now";
            } else {
                return "In " + hours + " hour(s)";
            }
        } else if (days == 1) {
            return "Tomorrow";
        } else if (days <= 7) {
            return "In " + days + " days";
        } else {
            return formatDate(dateTime.toLocalDate());
        }
    }

    public static boolean isToday(LocalDateTime dateTime) {
        return dateTime != null && dateTime.toLocalDate().equals(LocalDate.now());
    }

    public static boolean isThisWeek(LocalDateTime dateTime) {
        if (dateTime == null) return false;
        LocalDate date = dateTime.toLocalDate();
        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.minusDays(today.getDayOfWeek().getValue() - 1);
        LocalDate weekEnd = weekStart.plusDays(6);
        return !date.isBefore(weekStart) && !date.isAfter(weekEnd);
    }

    public static LocalDateTime startOfDay(LocalDate date) {
        return date.atStartOfDay();
    }

    public static LocalDateTime endOfDay(LocalDate date) {
        return date.atTime(23, 59, 59);
    }
}
