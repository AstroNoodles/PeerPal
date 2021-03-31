package com.github.astronoodles.peerpal.base;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.HashMap;

public class Assignment {

    protected final SimpleStringProperty fullName;
    protected final SimpleStringProperty assignDesc;
    protected final SimpleStringProperty instructorName;
    protected final SimpleStringProperty assignExtension;
    protected final SimpleObjectProperty<LocalDate> startDate;
    protected final SimpleObjectProperty<LocalDate> endDate;

    protected HashMap<String, Boolean> spellSettings;
    protected static int assignmentCount = 0;

    public Assignment(String name, String instructorName, String assignExtension,
                      LocalDate startDate, LocalDate endDate) {
        this.fullName = new SimpleStringProperty(name);
        this.instructorName = new SimpleStringProperty(instructorName);
        this.assignDesc = new SimpleStringProperty("");
        this.assignExtension = new SimpleStringProperty(assignExtension);
        this.startDate = new SimpleObjectProperty<>(startDate);
        this.endDate = new SimpleObjectProperty<>(endDate);
        assignmentCount++;
    }

    public Assignment(String name, String instructorName, String assignDesc, String assignExtension,
                      LocalDate startDate, LocalDate endDate) {
        this.fullName = new SimpleStringProperty(name);
        this.instructorName = new SimpleStringProperty(instructorName);
        this.assignDesc = new SimpleStringProperty(assignDesc);
        this.assignExtension = new SimpleStringProperty(assignExtension);
        this.startDate = new SimpleObjectProperty<>(startDate);
        this.endDate = new SimpleObjectProperty<>(endDate);
        assignmentCount++;
    }

    public Assignment(SerializableAssignment serAssign) {
        this.fullName = new SimpleStringProperty(serAssign.serFullName);
        this.instructorName = new SimpleStringProperty(serAssign.serInstructorName);
        this.assignDesc = new SimpleStringProperty(serAssign.serDesc);
        this.assignExtension = new SimpleStringProperty(serAssign.serExtension);
        this.startDate = new SimpleObjectProperty<>(serAssign.serStartDate);
        this.endDate = new SimpleObjectProperty<>(serAssign.serEndDate);
        this.spellSettings = serAssign.serSettings;
        assignmentCount++;

    }

    // Getters and Setters Below

    public final String getFullName() {return fullName.get();}
    public final String getInstructorName() {return instructorName.get();}
    public final String getDescription() {return assignDesc.get();}
    public final String getFileExtension() {return assignExtension.get(); }
    public LocalDate getStartDate() { return startDate.get(); }
    public LocalDate getEndDate() { return endDate.get(); }

    public final SimpleStringProperty fullNameProperty() {return fullName;}
    public final SimpleStringProperty instructorNameProperty() {return instructorName;}
    public final SimpleStringProperty descriptionProperty() {return assignDesc;}
    public final SimpleStringProperty extensionProperty() {return assignExtension; }
    public final SimpleObjectProperty<LocalDate> startDateProperty() {return startDate;}
    public final SimpleObjectProperty<LocalDate> endDateProperty() {return endDate;}

    public static int getAssignmentCount() {return assignmentCount;}

    public HashMap<String, Boolean> getSpellSettings() {
        return spellSettings;
    }

    @Override
    public String toString() {
        return String.format("%s.%s (%s)", getFullName(), getFileExtension(), getInstructorName());
    }

    public static class SerializableAssignment implements Serializable {
        protected final String serFullName;
        protected final String serInstructorName;
        protected final String serDesc;
        protected final String serExtension;
        protected final LocalDate serStartDate;
        protected final LocalDate serEndDate;
        protected final HashMap<String, Boolean> serSettings;
        private static final long serialVersionUID = 12345678912345L;

        public SerializableAssignment(Assignment assign) {
            serFullName = assign.getFullName();
            serInstructorName = assign.getInstructorName();
            serDesc = assign.getDescription();
            serExtension = assign.getFileExtension();
            serStartDate = assign.getStartDate();
            serEndDate = assign.getEndDate();
            serSettings = assign.getSpellSettings();
        }

    }

}
