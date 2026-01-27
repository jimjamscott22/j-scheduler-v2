package com.jscheduler.model;

public enum AssignmentStatus {
    NOT_STARTED("Not Started"),
    IN_PROGRESS("In Progress"),
    SUBMITTED("Submitted"),
    LATE("Late");

    private final String displayName;

    AssignmentStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
