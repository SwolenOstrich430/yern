package com.yern.common.file;

import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.Path;

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
}
