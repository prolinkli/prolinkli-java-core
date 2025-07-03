package com.prolinkli.framework.exception.exceptions.model;

public class AuthenticationFailedException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  public AuthenticationFailedException() {
    super("Authentication failed");
  }

  public AuthenticationFailedException(String message) {
    super(message);
  }

  public AuthenticationFailedException(String message, Throwable cause) {
    super(message, cause);
  }

  public AuthenticationFailedException(Throwable cause) {
    super(cause);
  }

}
