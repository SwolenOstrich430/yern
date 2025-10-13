package com.yern.service.storage.file.access;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yern.repository.storage.FileAccessControlRespository;

@Service
public class FileAccessControlService {
    private FileAccessControlRespository accessRepository;

    public FileAccessControlService(
        @Autowired FileAccessControlRespository accessRepository
    ) {
        this.accessRepository = accessRepository;
    }

    
}
