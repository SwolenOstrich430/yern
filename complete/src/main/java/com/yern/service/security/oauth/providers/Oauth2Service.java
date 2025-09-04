package com.yern.service.security.oauth.providers;

import com.yern.dto.authentication.LoginResponse;
import com.yern.dto.user.UserPostDto;
import com.yern.model.user.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.security.NoSuchProviderException;
import java.util.Map;

public interface Oauth2Service {
    User processGrantCode(String code);
    String getAccessToken(String code);
    UserPostDto getUserFromAccessToken(String accessToken);
    String getOauthInitiateUri(String email);
}