package com.yern.dto.user;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserPostDto {
    private String firstName;
    private String lastName;
    private String email;
    private String username;
    private String password;
     private String authenticationProviderIdentifier;

    public UserPostDto() {}
}