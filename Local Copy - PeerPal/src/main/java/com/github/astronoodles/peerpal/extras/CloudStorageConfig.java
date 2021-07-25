package com.github.astronoodles.peerpal.extras;

import com.azure.storage.file.share.*;
import com.azure.storage.file.share.models.ShareStorageException;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class CloudStorageConfig {
    // Find a way to secure this later (via KeyVault)
    private static final String CONNECT_STRING = "<connect-string-here>";
    private static final String STORAGE_NAME = "peerpal";

    public CloudStorageConfig() {
        ShareClient mainClient = new ShareClientBuilder()
                .connectionString(CONNECT_STRING).shareName("peerpal").buildClient();

        if (mainClient.exists()) {
            int maxStorageCap = mainClient.getProperties().getQuota() * 1100; // convert quota in TiB to GB
            double adjustableLimit = 0.95; // adjustable limit (how far to go until storage is maxed out)
            if (mainClient.getStatistics().getShareUsageInGB() > maxStorageCap * adjustableLimit) {
                mainClient.delete();
            }
        } else {
            mainClient.create();
        }
    }

//    // FOR DEBUG ONLY
//     public static void main(String[] args) {
//         //new CloudStorageConfig().isLocalStorageEmpty();
//     }

    public boolean saveLocalStorage() {
        Path storageDir = Paths.get("./src/main/java/com/github/astronoodles/peerpal", "storage");
        try(DirectoryStream<Path> dirStream = Files.newDirectoryStream(storageDir)) {
            for(Path storageItem : dirStream) {
                if(Files.isDirectory(storageItem)) {
                    ShareDirectoryClient dirClient = new ShareFileClientBuilder().connectionString(CONNECT_STRING)
                            .shareName(STORAGE_NAME).resourcePath(storageItem.getFileName().toString())
                            .buildDirectoryClient();
                    dirClient.create();

                    for(File studentFile : storageItem.toFile().listFiles()) {
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
            return true;
        } catch(IOException | ShareStorageException e) {
            e.printStackTrace(); // either I/O error happened or could not save local storage
            return false;
        }
    }

    public boolean downloadCloudStorage() {
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
                         Path folderItem = Paths.get(rootPath.toString(), cloudItemName, subCloudItem.getName());
                         ShareFileClient subFileClient = dirClient.getFileClient(subCloudItem.getName());
                         subFileClient.downloadToFile(folderItem.toString());
                     });
                 } else {
                     Path fileItem = Paths.get(rootPath.toString(), cloudItemName);
                     ShareFileClient fileClient = rootClient.getFileClient(cloudItemName);
                     fileClient.downloadToFile(fileItem.toString());
                 }
             });
         } catch(ShareStorageException e) {
             e.printStackTrace();
             return false;
             // TODO Check Internet problems
         }
         return true;
    }

    public boolean isCloudStorageEmpty() {
        ShareDirectoryClient dirClient = new ShareFileClientBuilder().connectionString(CONNECT_STRING)
                .shareName(STORAGE_NAME).resourcePath("").buildDirectoryClient();
        return dirClient.listFilesAndDirectories().stream().count() == 0;
    }

    public boolean isLocalStorageEmpty() {
        System.out.println(System.getProperty("user.dir"));
        Path storageDir = Paths.get("./src/main/java/com/github/astronoodles/peerpal", "storage");

        try(DirectoryStream<Path> dirStream = Files.newDirectoryStream(storageDir)) {
            for(Path dirItem : dirStream) {
                if(Files.isDirectory(dirItem)) {
                    return dirItem.toFile().listFiles().length == 0;
                    // here's hoping that when deletion happens, all files inside the student directories are deleted as well
                    // if not, we need to use a counter here instead of a short circuit
                }
            }
        } catch(IOException e) {
            e.printStackTrace();
        }
        return false;
    }



}
