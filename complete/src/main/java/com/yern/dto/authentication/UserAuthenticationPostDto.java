package com.yern.dto.authentication;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserAuthenticationPostDto {
    private String username;
    private String password;
    private String identifier;

    public UserAuthenticationPostDto() {}

    public UserAuthenticationPostDto(String username, String password, String identifier) {
        this.username = username;
        this.password = password;
        this.identifier = identifier;
    }
}
