package com.yern.service.security.authentication;

import com.yern.exceptions.NotFoundException;
import com.yern.model.user.User;
import com.yern.model.user.UserAuthentication;
import com.yern.repository.user.UserAuthenticationRepository;
import com.yern.repository.user.UserRepository;
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
        User user = userRepository.getUserByEmail(email);

        if(user == null) {
            throw new UsernameNotFoundException(
                String.format("User does not exist, email: %s", email)
            );
        }

        UserAuthentication userAuth = userAuthRepository.getByUserId(user.getId());

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password(userAuth.getPassword())
                .build();
    }
}