package com.yern.service.security.oauth.providers;

import java.net.URI;

import com.yern.dto.user.UserPostDto;
import com.yern.model.user.User;

public interface Oauth2Service {
    User processGrantCode(String code);
    String getAccessToken(String code);
    UserPostDto getUserFromAccessToken(String accessToken);
    URI getOauthInitiateUri(String email);
}