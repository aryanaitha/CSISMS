package com.csims.exception;

import org.springframework.http.HttpStatus;

public class ApiException extends AppExceptions.ApiException {
    public ApiException(String message) {
        super(message);
    }

    public ApiException(String message, HttpStatus status) {
        super(message, status);
    }
}
