package com.yern.service.security.authentication;

import com.yern.dto.authentication.LoginResponse;
import com.yern.dto.user.UserPostDto;
import com.yern.exceptions.DuplicateException;
import com.yern.mapper.UserMapper;
import com.yern.model.user.User;
import com.yern.model.user.UserAuthentication;
import com.yern.repository.user.UserAuthenticationRepository;
import com.yern.service.security.oauth.providers.Oauth2Service;
import com.yern.service.security.oauth.providers.Oauth2ServiceFactory;
import com.yern.service.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.NoSuchProviderException;
import java.util.Objects;

@Service
public class AuthenticationService {
    @Autowired
    private UserService userService;

    @Autowired
    private UserAuthenticationRepository userAuthenticationRepository;

    @Autowired
    private final PasswordEncoder passwordEncoder;

    @Autowired
    private final Oauth2ServiceFactory oauth2ServiceFactory;

    @Autowired
    private final JwtService jwtService;

    public AuthenticationService(
            UserService userService,
            UserAuthenticationRepository userAuthenticationRepository,
            PasswordEncoder passwordEncoder, Oauth2ServiceFactory oauth2ServiceFactory,
            JwtService jwtService
    ) {
        this.userService = userService;
        this.userAuthenticationRepository = userAuthenticationRepository;
        this.passwordEncoder = passwordEncoder;
        this.oauth2ServiceFactory = oauth2ServiceFactory;
        this.jwtService = jwtService;
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

    public LoginResponse loginUser(String email) {
        String token = jwtService.generateToken(email);
        return new LoginResponse(token, email);
    }

    public LoginResponse processGrantCode(String provider, String code) throws NoSuchProviderException {
        Oauth2Service oauth2Service = oauth2ServiceFactory.getService(provider);
        User foundUser = oauth2Service.processGrantCode(code);

        return loginUser(foundUser.getEmail());
    }

    public String getOauthInitiateUri(String provider, String email) throws NoSuchProviderException {
        Oauth2Service oauth2Service = oauth2ServiceFactory.getService(provider);
        return oauth2Service.getOauthInitiateUri(email);
    }
}
