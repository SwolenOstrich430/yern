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

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;


@Service("oauth2_google")
public final class GoogleOauth2Service implements Oauth2Service {
    public static String IDENTIFIER = "google";

    @Autowired
    private UserRepository userRepository;

    // TODO: move this into a config class
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


    public String getOauthInitiateUri(String email) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();

        params.add("scope", getScopesQueryParam());
        params.add("access_type", accessType);
        params.add("client_id", clientId);
        params.add("redirect_uri", "http://localhost:8080" + redirectUri);
        params.add("include_granted_scopes", "false");
        params.add("response_type", responseType);
        params.add("state", getSetState(email));

        UriComponents uriComponents = UriComponentsBuilder.fromUriString(initiateUri).queryParams(params).build();
        return uriComponents.toUri().toString();
    }

    public User processGrantCode(String code) {
        String accessToken = getAccessToken(code);

        UserPostDto externalUser = getUserFromAccessToken(accessToken);
        Optional<User> user = userRepository.findByEmail(externalUser.getEmail());

        if(user.isEmpty()) {
            throw new ResourceAccessException(
                "User is not registered yet. Please go through normal registration first"
            );
        }

        return user.get();
    }

    public String getAccessToken(String code) {
        RestTemplate restTemplate = new RestTemplate();

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("code", code);
        params.add("redirect_uri", "http://localhost:8080" + redirectUri);
        params.add("client_id", clientId);
        params.add("client_secret", clientSecret);
        params.add("grant_type", grantType);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(params, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(tokenUri, requestEntity, String.class, 1);
        JsonObject jsonObject = new Gson().fromJson(response.getBody(), JsonObject.class);

        return jsonObject.get("access_token").toString().replace("\"", "");
    }

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

    private String getScopesQueryParam() {
        return scopes.stream()
                .map(String::trim)
                .collect(Collectors.joining(" "));
    }

    private String urlEncode(String value) throws UnsupportedEncodingException {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    // This should probably be a util method
    private String getSetState(String email) {
        String state = getStateValue();
        String cacheKey = getStateCacheKey(email);

        return state;
//        return cacheService.getSet(cacheKey, state);
    }

    private String getStateCacheKey(String email) {
        return "oauth2_google_" + email.hashCode();
    }

    private String getStateValue() {
        return new BigInteger(130, new SecureRandom()).toString(32);
    }
}
