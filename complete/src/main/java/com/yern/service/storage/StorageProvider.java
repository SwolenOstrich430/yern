package com.yern.service.storage;

import java.io.File;
import java.util.List;

public interface StorageProvider {
    public boolean fileExists(String path);
    public void createFolder(String path);
    public List<String> listFolders(String folderPath);
    public void deleteFolder(String path);
    public void uploadFile(String localPath, String targetPath);
    public File downloadFile(String localPath, String targetPath);
    public void updateFile(String path);
    public void deleteFile(String path); 
    public void copyFile(String currentPath, String targetPath);
    public void moveFile(String curreString, String targetPath);
}
