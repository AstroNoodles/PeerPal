package com.github.astronoodles.peerpal.base;

import com.github.astronoodles.peerpal.AssignmentTeacherScreen;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class AssignmentIO {

    /**
     * Backs up all assignments that the teacher has created to the assignments.dat
     * It saves these assignments in a LIST format so all reads from this folder must be done
     * with the consideration that the assignments are saved in a list.
     */
    public static void backUpAssignments(List<Assignment> generalAssignments) {
        try {
            Path assignmentsLoc =
                    Paths.get("./src/main/java/com/github/astronoodles/peerpal",
                            "storage", "assignments.dat");

            if (!Files.exists(assignmentsLoc)) Files.createFile(assignmentsLoc);

            try (ObjectOutputStream oos = new ObjectOutputStream(Files.newOutputStream(assignmentsLoc,
                    StandardOpenOption.WRITE))) {
                oos.writeObject(generalAssignments);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Backs up all student assignments into their appropriate storage folder given by the student's name.
     * The student assignments will be saved in a LIST format so ensure that you read the assignments by
     * reading a list!
     *
     * @param assignments The list of student assignments to be saved for a particular student
     * @param studentName The student's name (the name of the student's folder)
     */
    public void backUpAssignments(List<StudentAssignment> assignments, String studentName) {
        try {
            Path assignmentsLoc =
                    Paths.get("./src/main/java/com/github/astronoodles/peerpal",
                            "storage", "assignments.dat");

            if (assignments.stream().anyMatch((assign) ->
                    assign.getStatus() == StudentAssignment.AssignmentStatus.UPLOADED ||
                            assign.getStatus() == StudentAssignment.AssignmentStatus.LATE)) {
                assignmentsLoc = Paths.get("./src/main/java/com/github/astronoodles/peerpal",
                        "storage", studentName, "studentAssignments.dat");
            }

            if (!Files.exists(assignmentsLoc)) Files.createFile(assignmentsLoc);

            final Path finalAssignmentsLoc = assignmentsLoc;
            assignments.forEach(assign -> assign.setAssignmentPath(finalAssignmentsLoc.toString()));

            try (ObjectOutputStream oos = new ObjectOutputStream(Files.newOutputStream(assignmentsLoc,
                    StandardOpenOption.WRITE))) {
                oos.writeObject(assignments);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * This method retrieves all of the student assignments from the respective student folder as indicated by
     * the studentName property specified in the method.
     * Make sure to read the assignments from a LIST rather than individually as they are inputted as a LIST
     * in the above method.
     *
     * @param studentName The student name and name of the folder to retrieve the student names from.
     * @return A list of all of the student assignments that the student specified
     */
    @SuppressWarnings("unchecked")
    public static List<StudentAssignment> obtainAssignments(String studentName) {
        // ENSURE that the general assignments file are read first AND then read the student assignments!
        List<StudentAssignment> assignments = new ArrayList<>(10);

        // Read both the student assignments and the general assignments path to see current assignments
        // and new teacher assignments

        // Only read a path if it exists (the studentAssignments will not exist in the beginning)
        Path[] allAssignmentPaths = new Path[]{Paths.get("./src/main/java/com/github/astronoodles/peerpal",
                "storage", studentName, "studentAssignments.dat"),
                Paths.get("./src/main/java/com/github/astronoodles/peerpal",
                        "storage", "assignments.dat")};
        List<String> retrievedAssignNames = new ArrayList<>(10);

        for (Path assignmentPath : allAssignmentPaths) {
            if (Files.exists(assignmentPath)) {
                try (ObjectInputStream ois =
                             new ObjectInputStream(Files.newInputStream(assignmentPath, StandardOpenOption.READ))) {
                    if (assignmentPath.getFileName().toString().equals("studentAssignments.dat")) {
                        // I know what I am doing with these casts
                        List<StudentAssignment> savedStudentAssignments =
                                (List<StudentAssignment>) ois.readObject();
                        assignments.addAll(savedStudentAssignments.parallelStream().filter(assign -> {
                                    LocalDate expireDate = assign.getEndDate().plus(AssignmentTeacherScreen.EXPIRY_PERIOD);
                                    return expireDate.isEqual(LocalDate.now()) || expireDate.isAfter(LocalDate.now());
                                }).collect(Collectors.toList()));
                        assignments.forEach(assign -> retrievedAssignNames.add(assign.getAssignmentName()));
                    } else { // assignments.dat
                        List<Assignment> savedAssignments =
                                (List<Assignment>) ois.readObject();
                        assignments.addAll(savedAssignments.parallelStream().map(StudentAssignment::new)
                                .filter(assign2 -> {
                                    LocalDate assignExpireDate = assign2.getEndDate().plus(AssignmentTeacherScreen.EXPIRY_PERIOD);
                                    return (assignExpireDate.isEqual(LocalDate.now()) || assignExpireDate.isAfter(LocalDate.now()))
                                            && !retrievedAssignNames.contains(assign2.getAssignmentName());
                                })
                                .collect(Collectors.toList()));
                    }
                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
        return assignments;
    }
}
