package com.yern.dto.security.authentication;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.GrantedAuthority;
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

    public Long getUserId() {
        return userId;
    }
}