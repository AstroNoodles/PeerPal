package com.github.astronoodles.peerpal.base;

import javafx.beans.property.SimpleFloatProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.paint.Color;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class StudentAssignment extends Assignment {

    private double grade;
    private AssignmentStatus status;
    private LinkedList<Feedback> feedback;
    private String assignPath;

    public StudentAssignment(Assignment otherAssign, String assignPath, float grade, LinkedList<Feedback> feedback) {
        super(otherAssign.getAssignmentName(), otherAssign.getInstructorName(), otherAssign.getFileExtension(),
                otherAssign.getDescription(), otherAssign.getStartDate(), otherAssign.getEndDate());
        this.grade = grade;
        this.status = AssignmentStatus.MISSING;
        this.feedback = feedback;
        this.assignPath = assignPath;
    }

    public StudentAssignment(Assignment otherAssign, String assignPath) {
        super(otherAssign.getAssignmentName(), otherAssign.getInstructorName(), otherAssign.getFileExtension(),
                otherAssign.getDescription(), otherAssign.getStartDate(), otherAssign.getEndDate());
        this.grade = 0;
        this.status = AssignmentStatus.MISSING;
        this.feedback = new LinkedList<>();
        this.assignPath = assignPath;
    }

    public StudentAssignment(Assignment otherAssign, String assignPath, float grade, LinkedList<Feedback> feedback,
                             AssignmentStatus status) {
        super(otherAssign.getAssignmentName(), otherAssign.getInstructorName(), otherAssign.getFileExtension(),
                otherAssign.getDescription(), otherAssign.getStartDate(), otherAssign.getEndDate());
        this.grade = grade;
        this.status = status;
        this.feedback = feedback;
        this.assignPath = assignPath;
    }

    public StudentAssignment(Assignment baseAssign) {
        super(baseAssign);
        this.grade = 0;
        this.status = AssignmentStatus.MISSING;
        this.feedback = new LinkedList<>();
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
        return Objects.equals(getAssignmentName(), other.getAssignmentName()) &&
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
        return Objects.hash(getAssignmentName(), getDescription(), getInstructorName(),
                getFileExtension(), getStartDate(), getEndDate(), getGrade(), getAssignmentFeedback(),
                getStatus(), assignPath);
    }

    @Override
    public String toString() {
        return String.format("%s.%s (%f) [%s]", getAssignmentName(), getFileExtension(), getGrade(), getStatus());
    }

    // Getters and Setters Below
    public double getGrade() {
        return grade;
    }

    public void setGrade(double grade) {
        this.grade = grade;
    }

    public AssignmentStatus getStatus() {
        return status;
    }

    public void setStatus(AssignmentStatus status) {
        this.status = status;
    }

    public String getAssignmentPath() {
        return assignPath;
    }

    public void setAssignmentPath(String assignPath) {
        this.assignPath = assignPath;
    }

    public void setAssignmentFeedback(LinkedList<Feedback> feedback) {this.feedback = feedback;}

    public LinkedList<Feedback> getAssignmentFeedback() {return feedback; }


    // INNER CLASSES

    public static class Feedback implements Serializable {
        private final int startPos;
        private final int endPos;
        private String feedbackText;

        public Feedback(int startPos, int endPos, String feedbackText) {
            this.feedbackText = feedbackText;
            this.startPos = startPos;
            this.endPos = endPos;
        }

        public int getStartPos() {return startPos; }
        public int getEndPos() {return endPos; }

        public void setFeedbackText(String feedbackText) {
            this.feedbackText = feedbackText;
        }
        public String getFeedbackText() {return feedbackText;}

    }

    public enum AssignmentStatus {
        MISSING("Missing", "#e53935"),
        UPLOADED("Uploaded", "#1e88e5"),
        GRADED("Graded", "#388e3c"),
        LATE("Late", "#b71c1c");

        private final String status;
        private final String statusColorString;

        AssignmentStatus(String statusText, String statusColorString) {
            this.status = statusText;
            this.statusColorString = statusColorString;
        }

        public String getStatusText() {return status;}
        public Color getStatusColor() {return Color.web(statusColorString);}
    }
}
