package com.csims.exception;

import org.springframework.http.HttpStatus;

/**
 * Consolidated Application Exceptions.
 * Contains all custom exceptions as nested static classes.
 */
public class AppExceptions {

    public static class ApiException extends RuntimeException {
        private final HttpStatus status;

        public ApiException(String message) {
            super(message);
            this.status = HttpStatus.BAD_REQUEST;
        }

        public ApiException(String message, HttpStatus status) {
            super(message);
            this.status = status;
        }

        public HttpStatus getStatus() {
            return status;
        }
    }

    public static class NotFoundException extends ApiException {
        public NotFoundException(String message) {
            super(message, HttpStatus.NOT_FOUND);
        }
    }

    public static class BadRequestException extends ApiException {
        public BadRequestException(String message) {
            super(message, HttpStatus.BAD_REQUEST);
        }
    }

    public static class UnauthorizedException extends ApiException {
        public UnauthorizedException(String message) {
            super(message, HttpStatus.UNAUTHORIZED);
        }
    }

    public static class ForbiddenException extends ApiException {
        public ForbiddenException(String message) {
            super(message, HttpStatus.FORBIDDEN);
        }
    }
}
