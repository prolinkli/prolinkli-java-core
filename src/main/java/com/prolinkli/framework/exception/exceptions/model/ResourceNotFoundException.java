package com.prolinkli.framework.exception.exceptions.model;


import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class ResourceNotFoundException extends RuntimeException {

    /** Default 404 with a generic message */
    public ResourceNotFoundException() {
        super("Resource not found");
    }

    /** 404 with a custom message */
    public ResourceNotFoundException(String message) {
        super(message);
    }

    /** 404 wrapping an underlying cause */
    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
