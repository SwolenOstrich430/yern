package com.yern.controller.user;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import org.springframework.security.test.context.support.WithSecurityContext;

@Retention(RetentionPolicy.RUNTIME)
@WithSecurityContext(factory = WithMockCustomUserSecurityContextFactory.class)
public @interface UserDetailsImplMock {
    String username() default "testuser";
    String password() default "password";
    long userId() default 1L;
    String role() default "USER";
    // Add other properties relevant to your custom user
}