package com.yern.dto.user;

import lombok.Getter;

@Getter
public class UserGetDto {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;

    public UserGetDto() {}

    public UserGetDto(Long id, String firstName, String lastName, String email) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
    }
}
