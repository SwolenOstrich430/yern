package com.yern.common.file;

import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import org.apache.commons.io.FilenameUtils;

public class FileUtil {

    public static void validateFileExists(
        Path localPath
    ) throws FileNotFoundException {
        if (!Files.exists(localPath)) {
            throw new FileNotFoundException(
                "File: " + localPath + " does not exist."
            );
        }
    }

    public static String getUniqueFileName(Path localPath) {
        String fileExtension = FilenameUtils.getExtension(
            localPath.getFileName().toString()
        );
        
        return UUID.randomUUID().toString() + "." + fileExtension;
    }
}
