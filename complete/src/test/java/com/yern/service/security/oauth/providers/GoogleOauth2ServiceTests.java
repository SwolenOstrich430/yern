package com.yern.service.security.oauth.providers;


import com.yern.dto.user.UserPostDto;
import com.yern.model.user.User;
import com.yern.repository.user.UserRepository;
import com.yern.restservice.RestServiceApplication;
import com.yern.service.cache.CacheService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.ResourceAccessException;

import java.net.URI;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = RestServiceApplication.class)
public class GoogleOauth2ServiceTests {
    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private CacheService cacheService;

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String clientId;
    @Value("${spring.security.oauth2.client.registration.google.client-secret}")
    private String clientSecret;
    @Value("${api.external.oauth2.google.config.endpoints.initiate-uri}")
    private String initiateUri;
    @Value("${api.endpoints.auth.oauth2.redirect-uri}")
    private String redirectUri;
    @Value("${spring.security.oauth2.client.registration.google.scope}")
    private Set<String> scopes;
    @Value("${spring.security.oauth2.client.registration.google.authorization-grant-type}")
    private String grantType;
    @Value("${api.external.oauth2.google.config.access-type}")
    private String accessType;
    @Value("${api.external.oauth2.google.config.endpoints.token-uri}")
    private String tokenUri;
    @Value("${api.external.oauth2.google.config.endpoints.user-uri}")
    private String userUri;
    @Value("${api.external.oauth2.google.config.response-type}")
    private String responseType;
    @Value("${api.base-url}")
    private String baseUrl;
    private String email;
    private String state;

    private GoogleOauth2Service googleOauth2Service;

    @BeforeEach()
    public void setup() {
        googleOauth2Service = new GoogleOauth2Service(
            userRepository,
            cacheService,
            clientId,
            clientSecret,
            initiateUri,
            redirectUri,
            scopes,
            grantType,
            accessType,
            tokenUri,
            userUri,
            responseType,
            baseUrl
        );

        email = UUID.randomUUID().toString();
        state = UUID.randomUUID().toString();
    }

    @Test
    public void getOauthInitiateUri_returnsAURI_representingTheRequestURItToBeginOauthFlow() {
        URI initiateRequest = googleOauth2Service.getOauthInitiateUri(email);

        assertInstanceOf(URI.class, initiateRequest);
        assertTrue(initiateRequest.toString().startsWith(initiateUri + "?"));
    }

    @Test
    public void getOauthInitiateUriParams_returnsAMapOfQueryParams() {
        GoogleOauth2Service spy = spy(googleOauth2Service);
        String scope = "scope";

        doReturn(state).when(spy).getSetState(email);
        doReturn(scope).when(spy).getScopesQueryParam();
        MultiValueMap<String, String> params = spy.getOauthInitiateParams(email);

        assertTrue(params.containsKey("state"));
        assertEquals(state, params.get("state").get(0));

        assertTrue(params.containsKey("access_type"));
        assertEquals(params.get("access_type").get(0), accessType);

        assertTrue(params.containsKey("client_id"));
        assertEquals(params.get("client_id").get(0), clientId);

        assertTrue(params.containsKey("redirect_uri"));
        assertEquals(params.get("redirect_uri").get(0), baseUrl + redirectUri);

        assertTrue(params.containsKey("include_granted_scopes"));
        assertEquals(params.get("include_granted_scopes").get(0), "false");

        assertTrue(params.containsKey("response_type"));
        assertEquals(params.get("response_type").get(0), responseType);

        assertTrue(params.containsKey("state"));
        assertEquals(params.get("state").get(0), state);
    }

    @Test
    public void processGrantCode_throwsResourceAccessException_whenNoUserCanBeExtracted() {
        GoogleOauth2Service spy = spy(googleOauth2Service);

        String code = UUID.randomUUID().toString();
        String token = UUID.randomUUID().toString();
        UserPostDto externalUser = mock(UserPostDto.class);

        doReturn(token).when(spy).getAccessToken(code);
        doReturn(externalUser).when(spy).getUserFromAccessToken(token);
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        assertThrows(ResourceAccessException.class, () -> spy.processGrantCode(code));
    }

    @Test
    public void processGrantCode_returnsAValidUser_whenUserCanBeExtracted() {
        GoogleOauth2Service spy = spy(googleOauth2Service);

        String code = UUID.randomUUID().toString();
        String token = UUID.randomUUID().toString();
        UserPostDto externalUser = new UserPostDto();
        externalUser.setEmail(email);
        User user = mock(User.class);

        doReturn(token).when(spy).getAccessToken(code);
        doReturn(externalUser).when(spy).getUserFromAccessToken(token);
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        User foundUser = spy.processGrantCode(code);
        assertEquals(user, foundUser);
    }

    @Test
    public void getAccessTokenParams_returnsAMapOfQueryParams() {
        MultiValueMap<String, String> params = googleOauth2Service.getAccessTokenParams(email);

        assertTrue(params.containsKey("code"));
        assertEquals(params.get("code").get(0), email);

        assertTrue(params.containsKey("client_id"));
        assertEquals(params.get("client_id").get(0), clientId);

        assertTrue(params.containsKey("redirect_uri"));
        assertEquals(params.get("redirect_uri").get(0), baseUrl + redirectUri);

        assertTrue(params.containsKey("client_secret"));
        assertEquals(params.get("client_secret").get(0), clientSecret);

        assertTrue(params.containsKey("grant_type"));
        assertEquals(params.get("grant_type").get(0), grantType);
    }
}
