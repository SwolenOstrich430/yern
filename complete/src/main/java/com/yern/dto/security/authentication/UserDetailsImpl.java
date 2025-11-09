package com.yern.dto.security.authentication;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import io.jsonwebtoken.lang.Collections;

import org.springframework.security.core.GrantedAuthority;

import java.util.ArrayList;
import java.util.Collection;

public class UserDetailsImpl extends User {
    private final Long userId;

    public UserDetailsImpl(
        String username, 
        String password, 
        Collection<? extends GrantedAuthority> authorities, 
        Long userId
    ) {
        super(username, password, authorities);
        this.userId = userId;
    }

    public UserDetailsImpl(
        String username, 
        String password, 
        long userId
    ) {
        super(username, password, new ArrayList<>());
        this.userId = Long.valueOf(userId);
    }

    public Long getUserId() {
        return userId;
    }
}