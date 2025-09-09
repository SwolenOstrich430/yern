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
import org.aspectj.lang.annotation.Before;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.security.NoSuchProviderException;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = AuthenticationServiceTests.class)
public class AuthenticationServiceTests {

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private UserAuthenticationRepository userAuthenticationRepository;

    @MockitoBean
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private Oauth2ServiceFactory oauth2ServiceFactory;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserMapper userMapper;

    private AuthenticationService authenticationService;

    private UserPostDto validUserDto;
    private UserPostDto invalidUserDto;
    private User user;
    private UserAuthentication userAuthentication;

    @BeforeEach()
    public void setUp() {
        authenticationService = new AuthenticationService(
            userService,
            userAuthenticationRepository,
            passwordEncoder,
            oauth2ServiceFactory,
            jwtService,
            userMapper
        );

        validUserDto = new UserPostDto();
        validUserDto.setUsername("valid");

        invalidUserDto = new UserPostDto();
        invalidUserDto.setUsername("invalid");

        user = new User();
        userAuthentication = new UserAuthentication();
    }

    @Test
    public void registerUser_createsUser_ifUserPostDtoIsValid() throws DuplicateException {
        Mockito.doNothing().when(userService).validateUserForRegistration(validUserDto);

        when(userService.createUser(any(User.class))).thenReturn(user);
        when(userAuthenticationRepository.save(any(UserAuthentication.class))).thenReturn(userAuthentication);
        when(userMapper.dtoToModel(validUserDto)).thenReturn(user);

        authenticationService.registerUser(validUserDto);

        verify(userService, times(1)).createUser(user);
        verify(userAuthenticationRepository, times(1)).save(any(UserAuthentication.class));
    }

    @Test
    public void registerUser_throwsDuplicateException_ifUserPostDtoIsInvalid() throws DuplicateException {
        doThrow(DuplicateException.class).when(userService).validateUserForRegistration(invalidUserDto);

        assertThrows(DuplicateException.class, () -> {
            authenticationService.registerUser(invalidUserDto);
        });
    }

    @Test
    public void loginUser_returnsALoginResponse_withAValidToken() {
        String email = validUserDto.getUsername();
        String token = UUID.randomUUID().toString();
        when(jwtService.generateToken(email)).thenReturn(token);

        LoginResponse loginResponse = authenticationService.loginUser(email);

        assertInstanceOf(LoginResponse.class, loginResponse);
        assertEquals(loginResponse.email(), email);
        assertEquals(loginResponse.token(), token);

        verify(jwtService, times(1)).generateToken(email);
    }

    @Test
    public void processGrantCode_returnsTheResultOfLoginUser_ifTheTokenIsValid() throws NoSuchProviderException {
        String token = UUID.randomUUID().toString();
        String code = UUID.randomUUID().toString();
        Oauth2Service mockService = Mockito.mock(Oauth2Service.class);

        AuthenticationService spy = spy(authenticationService);

        when(oauth2ServiceFactory.getService(anyString())).thenReturn(mockService);
        when(mockService.processGrantCode(code)).thenReturn(user);

        LoginResponse loginResponse = new LoginResponse(user.getEmail(), token);
        doReturn(loginResponse).when(spy).loginUser(user.getEmail());

        LoginResponse resp = spy.loginUser(code);
        assertInstanceOf(LoginResponse.class, resp);
        assertEquals(loginResponse.email(), user.getEmail());
        assertEquals(loginResponse.token(), token);
    }

    @Test
    public void processGrantCode_throwsNoSuchProviderException_ifProviderIsInvalid() throws NoSuchProviderException {
        assertThrows(NoSuchProviderException.class, () -> {
            String badProvider = UUID.randomUUID().toString();
            when(oauth2ServiceFactory.getService(badProvider)).thenThrow(NoSuchProviderException.class);
            authenticationService.processGrantCode(badProvider, "");
        });
    }
}
