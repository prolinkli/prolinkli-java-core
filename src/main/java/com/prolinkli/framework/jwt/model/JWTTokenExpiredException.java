package com.prolinkli.framework.jwt.model;

public class JWTTokenExpiredException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public JWTTokenExpiredException() {
        super("JWT token has expired");
    }

    public JWTTokenExpiredException(String message) {
        super(message);
    }

    public JWTTokenExpiredException(String message, Throwable cause) {
        super(message, cause);
    }

    public JWTTokenExpiredException(Throwable cause) {
        super(cause);
    }

  
}
