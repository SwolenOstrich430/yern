package com.yern.service.security.authentication;

import com.yern.dto.security.authentication.UserDetailsImpl;
import com.yern.model.user.User;
import com.yern.model.user.UserAuthentication;
import com.yern.repository.user.UserAuthenticationRepository;
import com.yern.repository.user.UserRepository;

import java.util.Collections;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private final UserRepository userRepository;

    @Autowired
    private final UserAuthenticationRepository userAuthRepository;

    public UserDetailsServiceImpl(
        UserRepository userRepository,
        UserAuthenticationRepository userAuthRepository
    ) {
        this.userRepository = userRepository;
        this.userAuthRepository = userAuthRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Optional<User> user = userRepository.findByEmail(email);

        user.orElseThrow(() -> new UsernameNotFoundException(
            String.format("User does not exist, email: %s", email)
        ));

        User foundUser = user.get();

        UserAuthentication userAuth = userAuthRepository.getByUserId(foundUser.getId());

        return new UserDetailsImpl(
            foundUser.getEmail(),
            userAuth.getPassword(),
            Collections.emptyList(),
            foundUser.getId()
        ); 
    }
}