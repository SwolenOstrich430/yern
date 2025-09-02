package com.yern.service.security.oauth.providers;

import com.yern.dto.authentication.LoginResponse;
import com.yern.dto.user.UserPostDto;

public interface Ouath2Service {
    LoginResponse processGrantCode(String code);
    String getAccessToken(String code);
    UserPostDto getUserFromAccessToken(String accessToken);
}
