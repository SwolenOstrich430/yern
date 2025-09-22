package com.yern.service.security.oauth.providers;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.yern.dto.user.UserPostDto;
import com.yern.model.user.User;
import com.yern.repository.user.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigInteger;
import java.net.URI;
import java.security.SecureRandom;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import com.yern.service.cache.CacheService;

// TODO: move the instance variables to config object or think about creating a builder/factory
@Service("oauth2_google")
public final class GoogleOauth2Service implements Oauth2Service {
    public static String IDENTIFIER = "google";

    @Autowired
    private UserRepository userRepository;

    @Autowired 
    private CacheService cacheService;

    private String clientId;
    private String clientSecret;
    private String initiateUri;
    private String redirectUri;
    private Set<String> scopes;
    private String grantType;
    private String accessType;
    private String tokenUri;
    private String userUri;
    private String responseType;
    private String baseUrl;

    // TODO: move this into a config class
    public GoogleOauth2Service(
        UserRepository userRepository,
        @Value("${spring.security.oauth2.client.registration.google.client-id}") String clientId,
        @Value("${spring.security.oauth2.client.registration.google.client-secret}") String clientSecret,
        @Value("${api.external.oauth2.google.config.endpoints.initiate-uri}") String initiateUri,
        @Value("${api.endpoints.auth.oauth2.redirect-uri}") String redirectUri,
        @Value("${spring.security.oauth2.client.registration.google.scope}") Set<String> scopes,
        @Value("${spring.security.oauth2.client.registration.google.authorization-grant-type}") String grantType,
        @Value("${api.external.oauth2.google.config.access-type}") String accessType,
        @Value("${api.external.oauth2.google.config.endpoints.token-uri}") String tokenUri,
        @Value("${api.external.oauth2.google.config.endpoints.user-uri}") String userUri,
        @Value("${api.external.oauth2.google.config.response-type}") String responseType,
        @Value("${api.base-url}") String baseUrl
    ) {
        this.userRepository = userRepository;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.initiateUri = initiateUri;
        this.redirectUri = redirectUri;
        this.scopes = scopes;
        this.grantType = grantType;
        this.accessType = accessType;
        this.tokenUri = tokenUri;
        this.userUri = userUri;
        this.responseType = responseType;
        this.baseUrl = baseUrl;
    }

    public URI getOauthInitiateUri(String email) {
        MultiValueMap<String, String> params = getOauthInitiateParams(email);
        UriComponents uriComponents = UriComponentsBuilder.fromUriString(initiateUri).queryParams(params).build();
        return uriComponents.toUri();
    }

    public MultiValueMap<String, String> getOauthInitiateParams(String email) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();

        params.add("scope", getScopesQueryParam());
        params.add("access_type", accessType);
        params.add("client_id", clientId);
        params.add("redirect_uri", baseUrl + redirectUri);
        params.add("include_granted_scopes", "false");
        params.add("response_type", responseType);
        params.add("state", getSetState(email));

        return params;
    }

    public User processGrantCode(String code) {
        String accessToken = getAccessToken(code);
        // TODO: add validation for accessToken and externalUser
        UserPostDto externalUser = getUserFromAccessToken(accessToken);
        Optional<User> user = userRepository.findByEmail(externalUser.getEmail());

        if(user.isEmpty()) {
            // Per Google's docs, users should be redirected
            // to go through registration of not yet logged in (others disagree?)
            // may change this going forward
            throw new ResourceAccessException(
                "User is not registered yet. Please go through normal registration first."
            );
        }

        return user.get();
    }

    // TODO: add test cases
    public String getAccessToken(String code) {
        RestTemplate restTemplate = new RestTemplate();
        MultiValueMap<String, String> params = getAccessTokenParams(code);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(params, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(tokenUri, requestEntity, String.class, 1);
        JsonObject jsonObject = new Gson().fromJson(response.getBody(), JsonObject.class);

        assert jsonObject != null;
        return jsonObject.get("access_token").toString().replace("\"", "");
    }

    public MultiValueMap<String, String> getAccessTokenParams(String code) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("code", code);
        params.add("redirect_uri", baseUrl + redirectUri);
        params.add("client_id", clientId);
        params.add("client_secret", clientSecret);
        params.add("grant_type", grantType);

        return params;
    }

    // TODO: add test cases
    @Override
    public UserPostDto getUserFromAccessToken(String accessToken) {
        RestTemplate restTemplate = new RestTemplate();

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("access_token", accessToken);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(params, headers);

        String response = restTemplate.postForObject(userUri, requestEntity, String.class);
        JsonObject jsonObject = new Gson().fromJson(response, JsonObject.class);

        UserPostDto user = new UserPostDto();
        user.setEmail(jsonObject.get("email").toString().replace("\"", ""));
        user.setFirstName(jsonObject.get("name").toString().replace("\"", ""));
        user.setLastName(jsonObject.get("given_name").toString().replace("\"", ""));
        user.setAuthenticationProviderIdentifier(IDENTIFIER);
        user.setPassword(UUID.randomUUID().toString());

        return user;
    }

    public String getScopesQueryParam() {
        return scopes.stream()
                .map(String::trim)
                .collect(Collectors.joining(" "));
    }

    public String getSetState(String email) {
        String state = getStateValue();
        String cacheKey = getStateCacheKey(email);

       cacheService.set(cacheKey, state);
       return state;
    }

    private String getStateCacheKey(String email) {
        return "oauth2_google_" + email.hashCode();
    }

    private String getStateValue() {
        return new BigInteger(130, new SecureRandom()).toString(32);
    }
}
