package com.yern.service.security.authentication;


import com.yern.model.user.User;
import com.yern.model.user.UserAuthentication;
import com.yern.repository.user.UserAuthenticationRepository;
import com.yern.repository.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

public class UserDetailsServiceImplTests {
    private UserDetailsServiceImpl userDetailsService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserAuthenticationRepository userAuthenticationRepository;

    private User user;
    
    @Mock
    private UserAuthentication userAuthentication;
    private final String email = UUID.randomUUID().toString();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        userDetailsService = new UserDetailsServiceImpl(
            userRepository,
            userAuthenticationRepository
        );

        user = new User();
        user.setEmail(email);
        user.setId(1L);

        userAuthentication = new UserAuthentication();
        userAuthentication.setUsername("email");
        userAuthentication.setPassword("password");
    }

    @Test
    public void loadUserByUsername_throwsUsernameNotFoundException_whenUserWithEmailDoesNotExist() {
        when(userRepository.getUserByEmail(email)).thenReturn(null);

        assertThrows(UsernameNotFoundException.class, () -> {
            userDetailsService.loadUserByUsername(email);
        });
    }

    @Test
    public void loadUserByUsername_returnsSpringSecurityUser_whenUserWithEmailExists() {
        Optional<User> potentialUser = Optional.of(user);
        when(userRepository.findByEmail(email)).thenReturn(potentialUser);

        when(userAuthenticationRepository.getByUserId(user.getId())).thenReturn(userAuthentication);

        UserDetails userDetails = userDetailsService.loadUserByUsername(email);
        assertEquals(userDetails.getUsername(), email);
        assertEquals(userDetails.getPassword(), userAuthentication.getPassword());
    }

}
