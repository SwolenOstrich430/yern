package com.yern.mapper;

import com.yern.dto.authentication.UserAuthenticationPostDto;
import com.yern.dto.user.UserGetDto;
import com.yern.dto.user.UserPostDto;
import com.yern.model.user.User;
import com.yern.model.user.UserAuthentication;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserMapper() {}
    
    public User dtoToModel(UserPostDto userPostDto) {
        return new User(
            userPostDto.getFirstName(),
            userPostDto.getLastName(),
            userPostDto.getEmail()
        );
    }

    public UserGetDto modelToDto(User user) {
        return new UserGetDto(
            user.getId(),
            user.getEmail(),
            user.getFirstName(),
            user.getLastName()
        );
    }
}


