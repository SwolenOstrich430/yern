package com.yern.controller.storage;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Paths;
import java.util.List;

import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.yern.dto.security.authentication.UserDetailsImpl;
import com.yern.dto.storage.GetFileResponse;
import com.yern.dto.storage.GrantFileAccessRequest;
import com.yern.dto.storage.GrantFileAccessResponse;
import com.yern.dto.storage.UploadFileResponse;
import com.yern.model.pattern.Section;
import com.yern.model.storage.FileImpl;
import com.yern.model.storage.UploadFileException;
import com.yern.service.storage.file.FileService;

import jakarta.servlet.http.HttpServletResponse;

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

    // TODO: currently this will only work for pdf files specifically
    @GetMapping("/{fileId}/content/stream")
    public void streamFileContent(
        @AuthenticationPrincipal UserDetailsImpl currentUser,
        @PathVariable Long fileId,
        HttpServletResponse response
    ) throws IOException {
        fileService.streamFileContent(
            currentUser.getUserId(),
            fileId,
            response
        );
    }

    @GetMapping("/list")
    public ResponseEntity<Page<GetFileResponse>> listFiles(
        @AuthenticationPrincipal UserDetailsImpl currentUser,
        Pageable pageable
    ) {
        return ResponseEntity.ok().body(
            fileService.findByUserId(currentUser.getUserId(), pageable)
        );
    }
}
