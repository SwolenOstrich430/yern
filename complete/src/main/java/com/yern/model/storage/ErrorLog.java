package com.yern.model.storage;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter 
@NoArgsConstructor
public class ErrorLog extends Throwable {
    private String exceptionType;
    private String message;
    private StackTraceElement[] stackTrace;

    public static ErrorLog from(Throwable exc) {
        ErrorLog error = new ErrorLog();
        error.setExceptionType(exc.getCause().getClass().getName());
        error.setMessage(exc.getMessage());
        error.setStackTrace(exc.getStackTrace());
        
        return error;
    }
}
