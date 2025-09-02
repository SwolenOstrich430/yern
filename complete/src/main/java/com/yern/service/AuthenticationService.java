package com.yern.service;

import com.yern.dto.user.UserPostDto;
import com.yern.exceptions.DuplicateException;
import com.yern.mapper.UserMapper;
import com.yern.model.user.User;
import com.yern.model.user.UserAuthentication;
import com.yern.repository.UserAuthenticationRepository;
import com.yern.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class AuthenticationService {
    @Autowired
    private UserService userService;

    @Autowired
    private UserAuthenticationRepository userAuthenticationRepository;

    @Autowired
    private final PasswordEncoder passwordEncoder;

    public AuthenticationService(
        UserService userService,
        UserAuthenticationRepository userAuthenticationRepository,
        PasswordEncoder passwordEncoder
    ) {
        this.userService = userService;
        this.userAuthenticationRepository = userAuthenticationRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public void registerUser(UserPostDto userDto) throws DuplicateException {
        userService.validateUserForRegistration(userDto);

        User user = UserMapper.dtoToModel(userDto);
        User createdUser = userService.createUser(user);

        userAuthenticationRepository.save(
            new UserAuthentication(
                createdUser,
                Objects.requireNonNullElse(userDto.getUsername(), userDto.getEmail()),
                passwordEncoder.encode(userDto.getPassword())
            )
        );
    }
}
