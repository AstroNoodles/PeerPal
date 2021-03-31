package com.github.astronoodles.peerpal.base;

import javafx.beans.property.SimpleFloatProperty;
import javafx.beans.property.SimpleObjectProperty;

public class StudentAssignment extends Assignment {

    private final SimpleFloatProperty grade;
    private final SimpleObjectProperty<AssignmentStatus> status;
    private String assignPath;
    private boolean markDirty;

    public StudentAssignment(Assignment otherAssign, String assignPath, float grade) {
        super(otherAssign.getFullName(), otherAssign.getInstructorName(), otherAssign.getFileExtension(),
                otherAssign.getDescription(), otherAssign.getStartDate(), otherAssign.getEndDate());
        this.grade = new SimpleFloatProperty(grade);
        this.status = new SimpleObjectProperty<>(AssignmentStatus.MISSING);
        this.assignPath = assignPath;
    }

    public StudentAssignment(Assignment otherAssign, String assignPath) {
        super(otherAssign.getFullName(), otherAssign.getInstructorName(), otherAssign.getFileExtension(),
                otherAssign.getDescription(), otherAssign.getStartDate(), otherAssign.getEndDate());
        this.grade = new SimpleFloatProperty(0);
        this.status = new SimpleObjectProperty<>(AssignmentStatus.MISSING);
        this.assignPath = assignPath;
    }

    public StudentAssignment(Assignment otherAssign, String assignPath, float grade, AssignmentStatus status) {
        super(otherAssign.getFullName(), otherAssign.getInstructorName(), otherAssign.getFileExtension(),
                otherAssign.getDescription(), otherAssign.getStartDate(), otherAssign.getEndDate());
        this.grade = new SimpleFloatProperty(grade);
        this.status = new SimpleObjectProperty<>(status);
        this.assignPath = assignPath;
    }

    public StudentAssignment(SerializableStudentAssignment serAssign) {
        super(serAssign);
        this.grade = new SimpleFloatProperty(serAssign.serGrade);
        this.status = new SimpleObjectProperty<>(serAssign.serStatus);
        this.assignPath = serAssign.serAssignPath;
    }

    public StudentAssignment(SerializableAssignment serAssign) {
        super(serAssign);
        this.grade = new SimpleFloatProperty(0);
        this.status = new SimpleObjectProperty<>(AssignmentStatus.MISSING);
    }

    @Override
    public String toString() {
        return String.format("%s.%s (%f) [%s]", getFullName(), getFileExtension(), getGrade(), getStatus());
    }

    // Getters and Setters Below
    public float getGrade() {
        return grade.get();
    }

    public void setGrade(float grade) {
        this.grade.set(grade);
    }

    public AssignmentStatus getStatus() {
        return status.get();
    }

    public void setStatus(AssignmentStatus status) {
        this.status.set(status);
    }

    public String getAssignmentPath() {
        return assignPath;
    }

    public void setAssignmentPath(String assignPath) {
        this.assignPath = assignPath;
    }

    public SimpleFloatProperty gradeProperty() {
        return grade;
    }

    public SimpleObjectProperty<AssignmentStatus> statusProperty() {
        return status;
    }

    public void markDirty() {
        this.markDirty = true;
    }

    public boolean isDirty() {
        return this.markDirty;
    }

    // INNER CLASS
    public static class SerializableStudentAssignment extends SerializableAssignment {
        protected final float serGrade;
        protected final String serAssignPath;
        protected final AssignmentStatus serStatus;
        private static final long serialVersionUID = 23456789L;

        public SerializableStudentAssignment(StudentAssignment studentAssign) {
            super(studentAssign);
            serGrade = studentAssign.getGrade();
            serStatus = studentAssign.getStatus();
            serAssignPath = studentAssign.getAssignmentPath();
        }
    }

    // INNER CLASS
    public enum AssignmentStatus {
        MISSING("Missing"), UPLOADED("Uploaded"), GRADED("Graded"), LATE("Late");

        AssignmentStatus(String statusText) {
        }
    }
}
