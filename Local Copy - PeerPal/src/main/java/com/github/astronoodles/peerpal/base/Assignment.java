package com.github.astronoodles.peerpal.base;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Objects;

public class Assignment implements Serializable {

    protected final String assignmentName;
    protected final String assignDesc;
    protected final String instructorName;
    protected final String assignExtension;
    protected final LocalDate startDate;
    protected final LocalDate endDate;
    protected final AssignmentType assignType;

    protected HashMap<String, Boolean> spellSettings;
    protected static int assignmentCount = 0;

    public Assignment(String name, String instructorName, String assignExtension,
                      LocalDate startDate, LocalDate endDate) {
        this.assignmentName = name;
        this.instructorName = instructorName;
        this.assignDesc = "";
        this.assignExtension = assignExtension;
        this.startDate = startDate;
        this.endDate = endDate;
        this.assignType = AssignmentType.NORMAL;
        assignmentCount++;
    }

    public Assignment(AssignmentType type, String name, String instructorName, String assignDesc, String assignExtension,
                      LocalDate startDate, LocalDate endDate) {
        this.assignmentName = name;
        this.instructorName = instructorName;
        this.assignDesc = assignDesc;
        this.assignExtension = assignExtension;
        this.startDate = startDate;
        this.endDate = endDate;
        this.assignType = type;
        assignmentCount++;
    }

    public Assignment(String name, String instructorName, String assignDesc, String assignExtension,
                      LocalDate startDate, LocalDate endDate) {
        this.assignType = AssignmentType.NORMAL;
        this.assignmentName = name;
        this.instructorName = instructorName;
        this.assignDesc = assignDesc;
        this.assignExtension = assignExtension;
        this.startDate = startDate;
        this.endDate = endDate;
        assignmentCount++;
    }

    public Assignment(Assignment other) {
        this.assignmentName = other.getAssignmentName();
        this.instructorName = other.getInstructorName();
        this.assignDesc = other.getDescription();
        this.assignExtension = other.getFileExtension();
        this.startDate = other.getStartDate();
        this.endDate = other.getEndDate();
        this.assignType = other.getAssignmentType();
        this.spellSettings = other.spellSettings;
        assignmentCount++;
    }

    // Getters and Setters Below

    public final String getAssignmentName() {
        return assignmentName;
    }

    public final String getInstructorName() {
        return instructorName;
    }

    public final String getDescription() {
        return assignDesc;
    }

    public final AssignmentType getAssignmentType() {
        return assignType;
    }

    public final String getFileExtension() {
        return assignExtension;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public static int getAssignmentCount() {
        return assignmentCount;
    }

    public HashMap<String, Boolean> getSpellSettings() {
        return spellSettings;
    }

    @Override
    public boolean equals(Object other) {
        if(other == this) return true;
        if(!(other instanceof Assignment)) return false;

        Assignment otherAssign = (Assignment) other;

        return Objects.equals(getAssignmentName(), otherAssign.getAssignmentName()) &&
                Objects.equals(getDescription(), otherAssign.getDescription()) &&
                Objects.equals(getInstructorName(), otherAssign.getInstructorName()) &&
                Objects.equals(getFileExtension(), otherAssign.getFileExtension()) &&
                Objects.equals(getStartDate(), otherAssign.getStartDate()) &&
                Objects.equals(getEndDate(), otherAssign.getEndDate());
    }

    public boolean copyOf(Assignment other) {
        return Objects.equals(getAssignmentName(), other.getAssignmentName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getAssignmentName(), getDescription(), getInstructorName(),
                getFileExtension(), getStartDate(), getEndDate());
    }

    @Override
    public String toString() {
        return String.format("%s.%s (%s)", getAssignmentName(), getFileExtension(), getInstructorName());
    }

    public enum AssignmentType {
        NORMAL, INFORMATIONAL, UPLOAD
    }


}
