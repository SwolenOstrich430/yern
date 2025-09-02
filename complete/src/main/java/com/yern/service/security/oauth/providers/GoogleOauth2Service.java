package com.yern.service.security.oauth.providers;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.yern.dto.authentication.LoginResponse;
import com.yern.dto.user.UserPostDto;
import com.yern.model.user.User;
import com.yern.repository.user.UserRepository;
import com.yern.service.security.authentication.AuthenticationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
public class GoogleOauth2Service implements Ouath2Service {
    public static String IDENTIFIER = "google";

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuthenticationService authenticationService;

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.google.client-secret}")
    private String clientSecret;

    @Value("${api.endpoints.auth.oauth2.start}")
    private String redirectUri;

    @Value("${spring.security.oauth2.client.registration.google.scope}")
    private Set<String> scopes;

    @Value("${spring.security.oauth2.client.registration.google.authorization-grant-type}")
    private String grantType;

    @Value("${api.external.google.oauth2.endpoints.token-uri}")
    private String tokenUri;

    @Value("${api.external.google.oauth2.endpoints.user-uri}")
    private String userUri;

    public LoginResponse processGrantCode(String code) {
        String accessToken = getAccessToken(code);

        UserPostDto externalUser = getUserFromAccessToken(accessToken);
        Optional<User> user = userRepository.findByEmail(externalUser.getEmail());

        if(user.isEmpty()) {
            authenticationService.registerUser(externalUser);
        }

        return authenticationService.loginExternalUser(externalUser);
    }

    public String getAccessToken(String code) {
        RestTemplate restTemplate = new RestTemplate();

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("code", code);
        params.add("redirect_uri", redirectUri);
        params.add("client_id", clientId);
        params.add("client_secret", clientSecret);
        params.add("grant_type", grantType);

        for (String scope : scopes) {
            params.add("scope", scope);
        }

        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(params);

        String response = restTemplate.postForObject(tokenUri, requestEntity, String.class);
        JsonObject jsonObject = new Gson().fromJson(response, JsonObject.class);

        return jsonObject.get("access_token").toString().replace("\"", "");
    }

    @Override
    public UserPostDto getUserFromAccessToken(String accessToken) {
        RestTemplate restTemplate = new RestTemplate();

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("access_token", accessToken);

        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(params);

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
}
