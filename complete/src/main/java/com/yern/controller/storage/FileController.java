package com.yern.controller.storage;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.yern.dto.authentication.UserDetailsImpl;
import com.yern.dto.storage.GrantFileAccessRequest;
import com.yern.dto.storage.GrantFileAccessResponse;
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

    // TODO: think about limits on file size and general file validation 
    @PostMapping("/section/upload")
    public ResponseEntity<UploadFileResponse> uploadFile(
        @RequestParam("file") MultipartFile file,
        @AuthenticationPrincipal UserDetailsImpl currentUser
    ) throws UploadFileException, IOException {
        ResponseEntity<UploadFileResponse> resp;

        try {
            FileImpl fileRecord = fileService.uploadAndSaveFile(
                currentUser.getUserId(),
                file,
                Section.class.getSimpleName()
            );

            resp = ResponseEntity.ok(
                UploadFileResponse.from(fileRecord)
            );
        } catch(UploadFileException exc) {
            resp = ResponseEntity.status(
                HttpStatus.BAD_REQUEST
            ).body(UploadFileResponse.from(exc));
        } 

        return resp;
    }

    @PostMapping("/access/grant")
    public ResponseEntity<GrantFileAccessResponse> grantAccess(
        @AuthenticationPrincipal UserDetailsImpl currentUser,
        @RequestBody GrantFileAccessRequest accessRequest
    ) throws Exception {
        GrantFileAccessResponse resp = fileService.grantAccess(
            currentUser.getUserId(),
            accessRequest
        );

        return ResponseEntity.ok().body(resp);
    }
}
