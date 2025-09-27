package com.yern.service.storage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public interface StorageProvider {
    public boolean fileExists(String path);
    public void createFolder(String path);
    public List<String> listFolders(String folderPath);
    public void deleteFolder(String path);
    public List<String> listFiles(String path);
    public void uploadFile(Path localPath, String targetPath) throws IOException;
    public void downloadFile(Path localPath, String targetPath);
    public void updateFile(String path);
    public void deleteFile(String path); 
    public void copyFile(String currentPath, String targetPath);
    public void moveFile(String currentPath, String targetPath);
}
