package com.yern.controller.user;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

import com.yern.dto.security.authentication.UserDetailsImpl;

public class WithMockCustomUserSecurityContextFactory implements WithSecurityContextFactory<UserDetailsImplMock> {
    
    @Override
    public SecurityContext createSecurityContext(UserDetailsImplMock customUser) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
		UserDetailsImpl principal = new UserDetailsImpl(
            customUser.username(),
            customUser.password(),
            customUser.userId()
        );

		context.setAuthentication(
            UsernamePasswordAuthenticationToken.authenticated(
                principal, 
                principal.getPassword(),
                principal.getAuthorities()
            )
        );

        return context;
    }
}