package com.yern.controller.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.yern.dto.authentication.LoginRequest;
import com.yern.dto.authentication.LoginResponse;
import com.yern.exceptions.AuthenticationExceptionImpl;
import com.yern.model.LocalDateTimeDeserializer;
import com.yern.service.security.authentication.AuthenticationService;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;


@SpringBootTest(classes = {
    AuthenticationController.class
})
@AutoConfigureMockMvc
@EnableWebMvc
@DisplayName("Authentication Controller Tests")
public class AuthenticationControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthenticationManager authenticationManager;

    @MockitoBean
    private AuthenticationService authenticationService;

    @Mock
    private HttpServletResponse mockResponse;

    @Value("${api.endpoints.auth.login-uri}")
    private String loginUri;

    private final LoginRequest validLoginRequest = new LoginRequest("testuser", "password");
    private final LoginRequest invalidLoginRequest = new LoginRequest("invalidtestuser", "invalidpassword");
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final String startOuathUri = "/api/auth/oauth2/google/start";

    private final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeDeserializer())
            .create();

    @InjectMocks
    AuthenticationController underTest;

    @BeforeEach
    void setUp() {
        this.underTest = new AuthenticationController(authenticationManager, authenticationService);
        mockMvc = MockMvcBuilders.standaloneSetup(underTest).build();
    }

    @Test
    public void login_shouldReturnALoginResponse_whenValidCredentialsAreProvided() throws Exception {
        String loginRequest = objectMapper.writeValueAsString(validLoginRequest);
        LoginResponse response = new LoginResponse(validLoginRequest.email(), validLoginRequest.password());

        when(
            authenticationService.loginUser(validLoginRequest.email())
        ).thenReturn(response);

        MvcResult result = this.mockMvc.perform(post(loginUri)
            .contentType(MediaType.APPLICATION_JSON)
            .content(loginRequest)
            .with(csrf())
            .with(SecurityMockMvcRequestPostProcessors.user("testuser")))
            .andExpect(status().isOk())
            .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        LoginResponse loginResponse = gson.fromJson(responseBody, LoginResponse.class);

        assertFalse(responseBody.isEmpty());
        assertFalse(loginResponse.token().isEmpty());
        assertEquals(loginResponse.email(), validLoginRequest.email());
    }

    @Test
    public void login_shouldThrowServletException_whenInvalidCredentialsAreProvided() throws Exception {
        String loginRequest = objectMapper.writeValueAsString(invalidLoginRequest);

        when(
            authenticationManager.authenticate(
                any(UsernamePasswordAuthenticationToken.class)
            )
        )
        .thenThrow(
            new AuthenticationExceptionImpl("message")
        );

        assertThrows(ServletException.class, () ->
            this.mockMvc.perform(post(loginUri)
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginRequest)
                .with(csrf())
                .with(SecurityMockMvcRequestPostProcessors.user("testuser")))
                .andExpect(status().isUnauthorized())
        );
    }

    @Test
    public void startOuathFlow_shouldRedirectToInitiateUri_whenAValidProviderIsGiven() throws Exception {
        URI redirectUri = new URI("https://redirect.zom");
        String provider = "google";
        String email = "pconnelly@gmail.com";

        when(
            authenticationService.getOauthInitiateUri(
                provider, email
            )
        ).thenReturn(redirectUri);

        this.mockMvc.perform(
            get(startOuathUri)
            .param("email", email)
        )
        .andExpect(status().isFound())
        .andExpect(redirectedUrl(redirectUri.toString()));
    }

    @Test
    public void grantCode_shouldPassRequestParams_toAuthServiceProcessGrantCode() throws Exception {
        String code = UUID.randomUUID().toString();
        String scope = UUID.randomUUID().toString();
        String authUser = UUID.randomUUID().toString();
        String prompt = UUID.randomUUID().toString();
        String provider = "google";

        LoginResponse mockLoginResponse = new LoginResponse(validLoginRequest.email(), validLoginRequest.password());

        when(
            authenticationService.processGrantCode(provider, code)
        )
        .thenReturn(mockLoginResponse);

        MvcResult result = this.mockMvc.perform(
            get("/api/auth/oauth2/google/response-oidc")
            .param("code", code)
            .param("scope", scope)
            .param("authuser", authUser)
            .param("prompt", prompt)
        )
        .andExpect(status().isOk())
        .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        LoginResponse loginResponse = gson.fromJson(responseBody, LoginResponse.class);

        assertFalse(responseBody.isEmpty());
        assertFalse(loginResponse.token().isEmpty());
        assertEquals(loginResponse.email(), validLoginRequest.email());
    }
}
