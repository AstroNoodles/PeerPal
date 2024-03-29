package com.github.astronoodles.peerpal.extras;

import com.azure.storage.file.share.*;
import com.azure.storage.file.share.models.ShareStorageException;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;

public class CloudStorageConfig {
    // Find a way to secure this later (via KeyVault)
    private static final String CONNECT_STRING = "DefaultaaaaoooppEndpointsProtocol=https;AccountName=peerpalblobstorage;AccountKey=MjpvbphVLs0Q7NvNoEjFXK5wGIM7nNvIK" +
            "flOveXPsRFcsWbvb8ekf3WJaTVwefFs/I5/gN7DOpmO8oSK0HwP1XQ++==;EndpointSuffix=core.windows.net";
    private static final String STORAGE_NAME = "peerpal";
    private static boolean hasInternetConnection = true;

    public CloudStorageConfig() {
        if (hasInternetConnection) {
            ShareClient mainClient = new ShareClientBuilder()
                    .connectionString(CONNECT_STRING).shareName("peerpal").buildClient();
            try {
                if (mainClient.existsWithResponse(Duration.ofSeconds(35), null).getValue()) {
                    int maxStorageCap = mainClient.getProperties().getQuota() * 1100; // convert quota in TiB to GB
                    double adjustableLimit = 0.95; // adjustable limit (how far to go until storage is maxed out)
                    if (mainClient.getStatistics().getShareUsageInGB() > maxStorageCap * adjustableLimit) {
                        mainClient.delete();
                    }
                } else {
                    mainClient.create();
                }
            } catch (IllegalStateException internetException) {
                // an internet problem happened
                internetException.printStackTrace();
                Alert internetAlert = new Alert(Alert.AlertType.INFORMATION, "Unfortunately, " +
                        "your internet connection cannot connect to the cloud storage service.\n" +
                        "As a result, your progress will only get stored locally to this computer and will not be shared to" +
                        " the teacher and other classmates.", ButtonType.OK);
                internetAlert.setTitle("Unable To Connect");
                internetAlert.setHeaderText("Internet Connection Error");
                internetAlert.showAndWait();
                hasInternetConnection = false;
            }
        }
    }

//    // FOR DEBUG ONLY
//     public static void main(String[] args) {
//         //new CloudStorageConfig().isLocalStorageEmpty();
//     }

    public boolean saveLocalStorage() {
        if (hasInternetConnection) {
            Path storageDir = Paths.get("./src/main/java/com/github/astronoodles/peerpal", "storage");
            try (DirectoryStream<Path> dirStream = Files.newDirectoryStream(storageDir)) {
                for (Path storageItem : dirStream) {
                    if (Files.isDirectory(storageItem)) {
                        ShareDirectoryClient dirClient = new ShareFileClientBuilder().connectionString(CONNECT_STRING)
                                .shareName(STORAGE_NAME).resourcePath(storageItem.getFileName().toString())
                                .buildDirectoryClient();

                        if (!dirClient.exists()) dirClient.create();

                        for (File studentFile : storageItem.toFile().listFiles()) {
                            ShareFileClient subFileClient = dirClient.getFileClient(studentFile.getName());
                            subFileClient.create(studentFile.length());
                            subFileClient.uploadFromFile(studentFile.getPath());
                        }

                    } else {
                        ShareDirectoryClient rootDirClient = new ShareFileClientBuilder().connectionString(CONNECT_STRING)
                                .shareName(STORAGE_NAME).resourcePath("").buildDirectoryClient();
                        ShareFileClient rootFileClient = rootDirClient.getFileClient(storageItem.getFileName().toString());
                        rootFileClient.create(storageItem.toFile().length());
                        rootFileClient.uploadFromFile(storageItem.toString());
                    }
                }
                System.out.println("---UPLOADING TO CLOUD STORAGE SUCCESS!!---");
                return true;
            } catch (IOException | ShareStorageException e) {
                e.printStackTrace(); // either I/O error happened or could not save local storage
            }
        }
        return false;
    }

    public boolean downloadCloudStorage() {
        if(hasInternetConnection) {
            ShareDirectoryClient rootClient = new ShareFileClientBuilder().connectionString(CONNECT_STRING)
                    .shareName(STORAGE_NAME).resourcePath("").buildDirectoryClient();
            Path rootPath = Paths.get("./src/main/java/com/github/astronoodles/peerpal", "storage");
            try {
                rootClient.listFilesAndDirectories().forEach(cloudFile -> {
                    String cloudItemName = cloudFile.getName();
                    if (cloudFile.isDirectory()) {
                        ShareDirectoryClient dirClient = new ShareFileClientBuilder().connectionString(CONNECT_STRING)
                                .shareName(STORAGE_NAME).resourcePath(cloudFile.getName()).buildDirectoryClient();
                        dirClient.listFilesAndDirectories().forEach(subCloudItem -> {
                            try {
                                Path dirItem = Paths.get(rootPath.toString(), cloudItemName);
                                Path folderItem = Paths.get(rootPath.toString(), cloudItemName, subCloudItem.getName());
                                ShareFileClient subFileClient = dirClient.getFileClient(subCloudItem.getName());

                                if(!Files.exists(dirItem)) Files.createDirectory(dirItem);
                                if(!Files.exists(folderItem)) Files.createFile(folderItem);

                                subFileClient.download(new FileOutputStream(folderItem.toFile()));
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        });
                    } else {
                        try {
                            Path fileItem = Paths.get(rootPath.toString(), cloudItemName);
                            ShareFileClient fileClient = rootClient.getFileClient(cloudItemName);
                            fileClient.download(new FileOutputStream(fileItem.toFile()));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
            } catch (ShareStorageException e) {
                e.printStackTrace();
                // TODO Check Internet problems
            }
            System.out.println("Downloading from CLOUD STORAGE success!");
            return true;
        }
        return false;
    }

    // will download only the assignments.dat file from the cloud storage to find updated teacher assignments
    public void downloadTeacherAssignments() {
        if(hasInternetConnection) {
            try {
                ShareDirectoryClient rootClient = new ShareFileClientBuilder().connectionString(CONNECT_STRING)
                        .shareName(STORAGE_NAME).resourcePath("").buildDirectoryClient();
                File assignmentFile = new File("./src/main/java/com/github/astronoodles/peerpal/storage/assignments.dat");
                ShareFileClient assignmentFileClient = rootClient.getFileClient("assignments.dat");

                if (assignmentFileClient != null) {
                    assignmentFileClient.download(new FileOutputStream(assignmentFile));
                    System.out.println("---Successfully DOWNLOADED assignments.dat---");
                }
            } catch(IOException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean isCloudStorageAvailable() {
        if(hasInternetConnection) {
            ShareDirectoryClient dirClient = new ShareFileClientBuilder().connectionString(CONNECT_STRING)
                    .shareName(STORAGE_NAME).resourcePath("").buildDirectoryClient();
            return dirClient.listFilesAndDirectories().stream().findAny().isPresent();
        } else return true;
    }

    public boolean countLocalStorage(int numFiles) {
        System.out.println(System.getProperty("user.dir"));
        Path storageDir = Paths.get("./src/main/java/com/github/astronoodles/peerpal", "storage");
        int counter = 0;

        try (DirectoryStream<Path> dirStream = Files.newDirectoryStream(storageDir)) {
            for (Path dirItem : dirStream) {
                if (Files.isDirectory(dirItem)) {
                    return dirItem.toFile().listFiles().length == numFiles;
                    // here's hoping that when deletion happens, all files inside the student directories are deleted as well
                    // if not, we need to use a counter here instead of a short circuit
                } else {
                    counter++;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return counter == numFiles;
    }


}
