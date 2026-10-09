package com.csims.util;

import com.csims.service.ServiceException;

/**
 * Thrown when input validation fails in the service layer.
 */
public class ValidationException extends ServiceException {

    private final String field;

    public ValidationException(String message) {
        super(message);
        this.field = null;
    }

    public ValidationException(String field, String message) {
        super(message);
        this.field = field;
    }

    public String getField() {
        return field;
    }
}
