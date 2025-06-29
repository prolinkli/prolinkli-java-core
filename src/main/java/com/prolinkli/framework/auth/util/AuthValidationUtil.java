package com.prolinkli.framework.auth.util;

import com.prolinkli.core.app.Constants;

public class AuthValidationUtil {

  public static void validateUserName(String username) {
    if (username == null || username.isEmpty()) {
      throw new IllegalArgumentException("Username cannot be null or empty");
    }
    if (username.length() < Constants.User.MIN_USERNAME_LENGTH) {
      throw new IllegalArgumentException(
          "Username must be at least " + Constants.User.MIN_USERNAME_LENGTH + " characters long");
    }
    if (username.length() > Constants.User.MAX_USERNAME_LENGTH) {
      throw new IllegalArgumentException(
          "Username must be at most " + Constants.User.MAX_USERNAME_LENGTH + " characters long");
    }
  }

}
