package com.yern.controller.storage;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.MultipartAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.yern.dto.storage.UploadFileResponse;
import com.yern.model.LocalDateTimeDeserializer;
import com.yern.model.storage.FileImpl;
import com.yern.model.storage.StorageProviderType;
import com.yern.restservice.RestServiceApplication;
import com.yern.service.storage.file.FileService;

@SpringBootTest(classes = {
    RestServiceApplication.class
})
@EnableWebMvc
@ImportAutoConfiguration(MultipartAutoConfiguration.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("File Controller Tests")
public class FileControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FileService fileService;

    @Autowired
    private WebApplicationContext context;

    @Value("${api.files-endpoint}")
    private String filesEndpoint;

    private FileImpl fileImpl;
    private MockMultipartFile rawFile;

    private Gson gson = new GsonBuilder()
        .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeDeserializer())
        .create();

    @BeforeEach
    public void setup() {
        this.mockMvc = MockMvcBuilders
                    .webAppContextSetup(context)
                    .build();
        
        this.fileImpl = new FileImpl();
        this.fileImpl.setId(1L);
        this.fileImpl.setRawPath(UUID.randomUUID().toString());
        this.fileImpl.setStorageProvider(StorageProviderType.GCS);

        this.rawFile = new MockMultipartFile(
            "file", 
            "hello.txt", 
            MediaType.TEXT_PLAIN_VALUE, 
            "Hello, World!".getBytes()
        );
    }

    @Test 
    public void uploadFile_returnsAnUploadFileResponse() throws Exception {
        when(
            fileService.uploadFile(
                any(MultipartFile.class), any(String.class)
            )
        ).thenReturn(fileImpl);
        
        MvcResult result = this.mockMvc.perform(
            multipart(filesEndpoint + "/section/upload")
            .file(rawFile)
            .param("file", rawFile.getName())
        )
        // .andExpect(status().isOk())
        .andReturn();
        
        String responseBody = result.getResponse().getContentAsString();
        UploadFileResponse resp = gson.fromJson(
            responseBody, UploadFileResponse.class
        );

        assertInstanceOf(
            UploadFileResponse.class,
            resp
        );
        assertEquals(resp.getFileId(), fileImpl.getId());
        assertEquals(resp.getRawPath(), fileImpl.getRawPath());
        assertEquals(resp.getStorageProvider(), fileImpl.getStorageProvider());
    }

    
}
