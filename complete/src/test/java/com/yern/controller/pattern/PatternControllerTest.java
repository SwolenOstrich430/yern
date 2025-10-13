package com.yern.controller.pattern;

import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.yern.dto.pattern.SectionCreateResponse;
import com.yern.dto.pattern.PatternCreateRequest;
import com.yern.dto.pattern.PatternCreateResponse;
import com.yern.dto.pattern.SectionCreateRequest;
import com.yern.model.LocalDateTimeDeserializer;
import com.yern.restservice.RestServiceApplication;
import com.yern.service.pattern.PatternService;
import com.yern.service.user.UserService;
import com.yern.model.user.User; 
import com.yern.exceptions.AccessDeniedException;


@SpringBootTest(classes = {
    RestServiceApplication.class
})
@EnableWebMvc
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("Pattern Controller Tests")
public class PatternControllerTest {
    @MockitoBean
    private PatternService service;

    @MockitoBean
    private UserService userService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @Value("${api.patterns-endpoint}")
    private String patternEndpoint; 

    private Gson gson = new GsonBuilder()
        .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeDeserializer())
        .create();

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final String name = UUID.randomUUID().toString();
    private final String description = UUID.randomUUID().toString();
    private final Long patternId = 1L;
    private final Long fileId = 2L;
    private final Long userId = 3L;
    private final User user = mock(User.class);
    private String username = "testUser";

    @BeforeEach 
    public void setup() {
        this.mockMvc = MockMvcBuilders
                        .webAppContextSetup(context)
                        .build();
    }

    @Test 
    @WithMockUser(username = "testUser", roles = {"USER"})
    public void createPattern_throwsAccessDeniedException_ifUserDoesntExist() throws Exception {
        PatternCreateRequest req = new PatternCreateRequest();
        req.setName(name);
        req.setDescription(description);
        String reqAsString = objectMapper.writeValueAsString(req);

        when(userService.getUserByEmail(username)).thenReturn(null);

        this.mockMvc.perform(
            post(patternEndpoint + "/create")
            .contentType(MediaType.APPLICATION_JSON) // Specify content type as JSON
            .content(reqAsString)
        )
        .andExpect(status().isBadRequest())
        .andExpect(res -> assertTrue(res.getResolvedException() instanceof AccessDeniedException));
    }

    @Test 
    @WithMockUser(username = "testUser", roles = {"USER"})
    public void createPattern_returnsAPatterCreateResponse() throws Exception {
        PatternCreateRequest req = new PatternCreateRequest();
        req.setName(name);
        req.setDescription(description);
        String reqAsString = objectMapper.writeValueAsString(req);

        PatternCreateResponse resp = new PatternCreateResponse();
        resp.setId(patternId);
        resp.setName(name);
        resp.setDescription(description);

        when(userService.getUserByEmail(username)).thenReturn(user);
        when(user.getId()).thenReturn(userId);
        when(
            service.createPattern(
                eq(userId), 
                any(PatternCreateRequest.class)
            )
        ).thenReturn(resp);

        MvcResult result = this.mockMvc.perform(
            post(patternEndpoint + "/create")
            .contentType(MediaType.APPLICATION_JSON) // Specify content type as JSON
            .content(reqAsString)
        )
        .andExpect(status().isOk())
        .andReturn();
        
        String responseBody = result.getResponse().getContentAsString();
        PatternCreateResponse parsedResp = gson.fromJson(
            responseBody, PatternCreateResponse.class
        );

        assertEquals(parsedResp.getId(), patternId);
        assertEquals(parsedResp.getName(), name);
        assertEquals(parsedResp.getDescription(), description);
    }

    @Test 
    @WithMockUser(username = "testUser", roles = {"USER"})
    public void createSection_throwsAccessDenied_ifUserDoesntExist() throws Exception {
        SectionCreateRequest req = new SectionCreateRequest();
        req.setName(name);
        req.setFileId(fileId);
        req.setPatternId(patternId);
        String reqAsString = objectMapper.writeValueAsString(req);

        
        when(userService.getUserByEmail(username)).thenReturn(null);

        this.mockMvc.perform(
            post(patternEndpoint + "/sections/create")
            .contentType(MediaType.APPLICATION_JSON) // Specify content type as JSON
            .content(reqAsString)
        )
        .andExpect(status().isBadRequest())
        .andExpect(res -> assertTrue(res.getResolvedException() instanceof AccessDeniedException));
    }

    @Test 
    @WithMockUser(username = "testUser", roles = {"USER"})
    public void createSection_createsASection() throws Exception {
        LocalDateTime start = LocalDateTime.now();
        
        SectionCreateRequest req = new SectionCreateRequest();
        req.setName(name);
        req.setFileId(fileId);
        req.setPatternId(patternId);
        String reqAsString = objectMapper.writeValueAsString(req);

        SectionCreateResponse resp = new SectionCreateResponse();
        resp.setFileId(fileId);
        resp.setName(name);
        resp.setPatternId(patternId);
        resp.setId(fileId);
        resp.setCreatedAt(LocalDateTime.now());
        resp.setUpdatedAt(LocalDateTime.now());

        when(userService.getUserByEmail(username)).thenReturn(user);
        when(user.getId()).thenReturn(userId);
        when(
            service.addSection(
                eq(userId),
                any(SectionCreateRequest.class)
            )
        ).thenReturn(resp);

        MvcResult result = this.mockMvc.perform(
            post(patternEndpoint + "/sections/create")
            .contentType(MediaType.APPLICATION_JSON) // Specify content type as JSON
            .content(reqAsString)
        )
        .andExpect(status().isOk())
        .andReturn();
        
        String responseBody = result.getResponse().getContentAsString();
        SectionCreateResponse parsedResp = gson.fromJson(
            responseBody, SectionCreateResponse.class
        );

        assertNotNull(parsedResp);
        assertTrue(parsedResp.getId() > 0);
        assertEquals(parsedResp.getName(), name);
        assertEquals(parsedResp.getFileId(), fileId);
        assertEquals(parsedResp.getPatternId(), patternId);
        assertTrue(parsedResp.getCreatedAt().isAfter(start));
        assertTrue(parsedResp.getUpdatedAt().isAfter(start));

        verify(
            service, 
            Mockito.times(1)
        ).addSection(eq(userId), any(SectionCreateRequest.class));
    }
}
