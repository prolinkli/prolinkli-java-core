package com.prolinkli.framework.auth.util;

import com.prolinkli.core.app.Constants;

/**
 * Utility class for authentication-related validation operations.
 * 
 * @documentation-pr-rule.mdc
 * 
 * This utility provides centralized validation logic for authentication
 * components, ensuring consistent validation rules across the system.
 * 
 * Current validation capabilities:
 * - Username format and length validation
 * - Compliance with system-defined constraints
 * - Consistent error messaging for validation failures
 * 
 * Validation rules are based on Constants.User configuration:
 * - Minimum username length: Constants.User.MIN_USERNAME_LENGTH
 * - Maximum username length: Constants.User.MAX_USERNAME_LENGTH
 * 
 * All validation methods throw IllegalArgumentException with descriptive
 * messages when validation fails, allowing calling code to handle
 * validation errors appropriately.
 * 
 * @author ProLinkLi Development Team
 * @since 1.0
 */
public class AuthValidationUtil {

  /**
   * Validates username according to system requirements.
   * 
   * @param username Username string to validate
   * @throws IllegalArgumentException if username is invalid
   * 
   * @documentation-pr-rule.mdc
   * 
   * Validation checks performed:
   * 1. Null check - username cannot be null
   * 2. Empty check - username cannot be empty string
   * 3. Minimum length - must meet Constants.User.MIN_USERNAME_LENGTH
   * 4. Maximum length - must not exceed Constants.User.MAX_USERNAME_LENGTH
   * 
   * This method is used throughout the authentication system to ensure
   * all usernames meet system requirements before database storage or
   * authentication processing.
   * 
   * Validation rules:
   * - Current minimum length: 4 characters
   * - Current maximum length: 32 characters
   * - Must not be null or empty
   * 
   * Used by:
   * - User registration processes
   * - OAuth2 username generation validation
   * - User profile update operations
   */
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
