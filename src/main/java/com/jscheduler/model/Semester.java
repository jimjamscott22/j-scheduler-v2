package com.jscheduler.model;

import java.util.Objects;

public class Semester {

    public enum Season {
        SPRING("Spring"),
        SUMMER("Summer"),
        FALL("Fall"),
        WINTER("Winter");

        private final String displayName;

        Season(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    private Season season;
    private int year;

    public Semester() {
    }

    public Semester(Season season, int year) {
        this.season = season;
        this.year = year;
    }

    public Season getSeason() {
        return season;
    }

    public void setSeason(Season season) {
        this.season = season;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    @Override
    public String toString() {
        return season.getDisplayName() + " " + year;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Semester semester = (Semester) o;
        return year == semester.year && season == semester.season;
    }

    @Override
    public int hashCode() {
        return Objects.hash(season, year);
    }
}
