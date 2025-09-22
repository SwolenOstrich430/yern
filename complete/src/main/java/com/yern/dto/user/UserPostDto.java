package com.yern.dto.user;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class UserPostDto {
    private String firstName;
    private String lastName;
    private String email;
    private String username;
    private String password;
    private String authenticationProviderIdentifier;
}