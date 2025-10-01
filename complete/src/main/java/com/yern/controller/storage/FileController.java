package com.yern.controller.storage;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.yern.dto.storage.UploadFileResponse;
import com.yern.model.pattern.Section;
import com.yern.model.storage.FileImpl;
import com.yern.model.storage.UploadFileException;
import com.yern.service.storage.file.FileService;

@RestController
@RequestMapping("${api.files-endpoint}")
public class FileController {

    private FileService fileService; 

    public FileController(
        @Autowired FileService fileService
    ) {
        this.fileService = fileService;
    }

    // TODO: think about limits on file size 
    @PostMapping("/section/upload")
    public ResponseEntity<UploadFileResponse> uploadFile(
        @RequestParam("file") MultipartFile file
    ) throws UploadFileException {
        Path formattedPath = Paths.get(
            file.getOriginalFilename()
        ).getFileName();

        FileImpl resp = fileService.uploadFile(
            formattedPath, 
            Section.class.toString()
        );

        return ResponseEntity.ok(
            UploadFileResponse.from(resp)
        );
    }
}
