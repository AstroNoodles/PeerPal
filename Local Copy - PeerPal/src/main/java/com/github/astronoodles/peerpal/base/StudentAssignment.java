package com.github.astronoodles.peerpal.base;

import javafx.beans.property.SimpleFloatProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.paint.Color;

import java.util.List;
import java.util.Objects;

public class StudentAssignment extends Assignment {

    private final SimpleFloatProperty grade;
    private final SimpleObjectProperty<AssignmentStatus> status;
    private final SimpleStringProperty feedback;
    private String assignPath;

    public StudentAssignment(Assignment otherAssign, String assignPath, float grade, String feedback) {
        super(otherAssign.getFullName(), otherAssign.getInstructorName(), otherAssign.getFileExtension(),
                otherAssign.getDescription(), otherAssign.getStartDate(), otherAssign.getEndDate());
        this.grade = new SimpleFloatProperty(grade);
        this.status = new SimpleObjectProperty<>(AssignmentStatus.MISSING);
        this.feedback = new SimpleStringProperty(feedback);
        this.assignPath = assignPath;
    }

    public StudentAssignment(Assignment otherAssign, String assignPath) {
        super(otherAssign.getFullName(), otherAssign.getInstructorName(), otherAssign.getFileExtension(),
                otherAssign.getDescription(), otherAssign.getStartDate(), otherAssign.getEndDate());
        this.grade = new SimpleFloatProperty(0);
        this.status = new SimpleObjectProperty<>(AssignmentStatus.MISSING);
        this.feedback = new SimpleStringProperty("");
        this.assignPath = assignPath;
    }

    public StudentAssignment(Assignment otherAssign, String assignPath, float grade, String feedback,
                             AssignmentStatus status) {
        super(otherAssign.getFullName(), otherAssign.getInstructorName(), otherAssign.getFileExtension(),
                otherAssign.getDescription(), otherAssign.getStartDate(), otherAssign.getEndDate());
        this.grade = new SimpleFloatProperty(grade);
        this.status = new SimpleObjectProperty<>(status);
        this.feedback = new SimpleStringProperty(feedback);
        this.assignPath = assignPath;
    }

    public StudentAssignment(SerializableStudentAssignment serAssign) {
        super(serAssign);
        this.grade = new SimpleFloatProperty(serAssign.serGrade);
        this.status = new SimpleObjectProperty<>(serAssign.serStatus);
        this.feedback = new SimpleStringProperty(serAssign.assignFeedback);
        this.assignPath = serAssign.serAssignPath;
    }

    public StudentAssignment(SerializableAssignment serAssign) {
        super(serAssign);
        this.grade = new SimpleFloatProperty(0);
        this.status = new SimpleObjectProperty<>(AssignmentStatus.MISSING);
        this.feedback = new SimpleStringProperty("");
    }

    public StudentAssignment(Assignment baseAssign) {
        super(baseAssign);
        this.grade = new SimpleFloatProperty(0);
        this.status = new SimpleObjectProperty<>(AssignmentStatus.MISSING);
        this.feedback = new SimpleStringProperty("");
    }

    @Override
    public boolean equals(Object other) {
        if(other == this) return true;
        if(!(other instanceof StudentAssignment)) return false;
        StudentAssignment studentAssign = (StudentAssignment) other;

        return super.equals(other) && Objects.equals(getGrade(), studentAssign.getGrade())
                && Objects.equals(getStatus(), studentAssign.getStatus())
                && Objects.equals(getAssignmentFeedback(), studentAssign.getAssignmentFeedback())
                && Objects.equals(assignPath, studentAssign.assignPath);
    }

    public boolean assignmentEquals(Assignment other) {
        return Objects.equals(getFullName(), other.getFullName()) &&
                Objects.equals(getDescription(), other.getDescription()) &&
                Objects.equals(getInstructorName(), other.getInstructorName()) &&
                Objects.equals(getFileExtension(), other.getFileExtension()) &&
                Objects.equals(getStartDate(), other.getStartDate()) &&
                Objects.equals(getEndDate(), other.getEndDate());
    }

    public static boolean assignmentContains(List<StudentAssignment> assignmentList, Assignment generalAssign) {
        for(StudentAssignment assignment : assignmentList){
            if(assignment.assignmentEquals(generalAssign)) {
                return true;
            }
        }
        return false;
    }


    @Override
    public int hashCode() {
        return Objects.hash(getFullName(), getDescription(), getInstructorName(),
                getFileExtension(), getStartDate(), getEndDate(), getGrade(), getAssignmentFeedback(),
                getStatus(), assignPath);
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

    public SimpleStringProperty feedbackProperty() {return feedback;}

    public void setAssignmentFeedback(String feedback) {this.feedback.set(feedback);}

    public String getAssignmentFeedback() {return feedback.get(); }

    // INNER CLASS
    public static class SerializableStudentAssignment extends SerializableAssignment {
        protected final float serGrade;
        protected final String serAssignPath;
        protected final AssignmentStatus serStatus;
        protected final String assignFeedback;
        private static final long serialVersionUID = 23456789L;

        public SerializableStudentAssignment(StudentAssignment studentAssign) {
            super(studentAssign);
            serGrade = studentAssign.getGrade();
            serStatus = studentAssign.getStatus();
            assignFeedback = studentAssign.getAssignmentFeedback();
            serAssignPath = studentAssign.getAssignmentPath();
        }
    }

    // INNER CLASS
    public enum AssignmentStatus {
        MISSING("Missing", Color.web("#e53935")),
        UPLOADED("Uploaded", Color.web("#1e88e5")),
        GRADED("Graded", Color.web("#388e3c")),
        LATE("Late", Color.web("#b71c1c"));

        private final Color statusColor;

        AssignmentStatus(String statusText, Color statusColor) {
            this.statusColor = statusColor;
        }

        public Color getStatusColor() {return statusColor;}
    }
}
