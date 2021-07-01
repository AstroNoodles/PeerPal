package com.github.astronoodles.peerpal;

import com.github.astronoodles.peerpal.base.StudentAssignment;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class CloudAssignmentParser {

    private final Map<String, Path> studentWritings;
    private final Map<String, List<StudentAssignment>> studentAssignments;

    public CloudAssignmentParser(String assignmentFileName) {
        studentWritings = obtainStudentWritings(assignmentFileName);
        studentAssignments = obtainStudentAssignments();
    }

    public Map<String, Path> getStudentTasks() {return studentWritings;}

    /**
     * A getter method to retrieve the list of student assignments for each student in the class.
     * @return A hash map that maps a student's name to the list of assignments they have completed.
     */
    public Map<String, List<StudentAssignment>> getStudentAssignments() {return studentAssignments; }

    /**
     * This method reverse maps a student's assignment in the app to their name in their class.
     * @param assignmentFileName The assignment's name
     * @return A hashmap which maps the owner of the assignment to the Path which describes the
     * location of the assignment
     */
    private Map<String, Path> obtainStudentWritings(String assignmentFileName) {
        Map<String, Path> studentWritings = new LinkedHashMap<>(20);
        Path storagePath = Paths.get("./src/main/java/com/github/astronoodles/peerpal",
                "storage");

        try(DirectoryStream<Path> dstream = Files.newDirectoryStream(storagePath, Files::isDirectory)) {
            for(Path entry : dstream) {
                Path assignment = Paths.get(entry.toString(), assignmentFileName);
                if(Files.exists(assignment)) studentWritings.put(entry.getFileName().toString(), assignment);
            }
        } catch(IOException e) {
            e.printStackTrace();
        }
        return studentWritings;
    }

    private Map<String, List<StudentAssignment>> obtainStudentAssignments() {
        Map<String, List<StudentAssignment>> studentAssignments = new LinkedHashMap<>(20);
        Path storagePath = Paths.get("./src/main/java/com/github/astronoodles/peerpal",
                "storage");

        try(DirectoryStream<Path> dstream = Files.newDirectoryStream(storagePath, Files::isDirectory)) {
            for(Path entry : dstream) {
                String studentName = entry.getFileName().toString();
                List<StudentAssignment> assignments = AssignmentScreen.obtainAssignments(studentName);
                studentAssignments.put(studentName, assignments);
                // NOTE: This might be a HUGE Bottleneck
            }
        } catch(IOException e) {
            e.printStackTrace();
        }
        return studentAssignments;
    }
}
