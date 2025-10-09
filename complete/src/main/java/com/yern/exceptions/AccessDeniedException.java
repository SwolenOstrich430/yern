package com.yern.exceptions;

import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.http.HttpStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class AccessDeniedException extends org.springframework.security.access.AccessDeniedException{
    public AccessDeniedException(String msg) {
        super(msg);
    }

}
