package com.yern.controller.pattern;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.yern.model.LocalDateTimeDeserializer;
import com.yern.restservice.RestServiceApplication;
import com.yern.service.pattern.SectionService;

@SpringBootTest(classes = {
    RestServiceApplication.class
})
@EnableWebMvc
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("Section Controller Tests")
public class SectionControllerTest {
    @MockitoBean
    private SectionService service;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    private Gson gson = new GsonBuilder()
        .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeDeserializer())
        .create();

    @BeforeEach 
    public void setup() {
        this.mockMvc = MockMvcBuilders
                        .webAppContextSetup(context)
                        .build();
    }

    @Test 
    public void createSection() {
        
    }
}
