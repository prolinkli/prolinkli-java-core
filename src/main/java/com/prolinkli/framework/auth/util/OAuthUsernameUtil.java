package com.prolinkli.framework.auth.util;

/**
 * Utility class for generating OAuth2-specific usernames.
 * 
 * @documentation-pr-rule.mdc
 * 
 * This utility provides secure username generation for OAuth2 users by combining
 * email and OAuth ID information. The generated usernames are designed to be:
 * - Unique across the system
 * - Compliant with username length constraints
 * - Traceable to the original OAuth account
 * - Free of special characters that could cause issues
 * 
 * Username generation algorithm:
 * 1. Extracts local part of email (before @)
 * 2. Normalizes to lowercase and removes special characters
 * 3. Truncates OAuth ID to maximum 10 characters
 * 4. Combines email and OAuth ID components
 * 5. Ensures total length does not exceed 30 characters
 * 6. Uses alternating truncation to maintain balance
 * 
 * Security considerations:
 * - Removes all non-alphanumeric characters to prevent injection
 * - Maintains reasonable entropy for uniqueness
 * - Predictable generation for consistent username assignment
 * 
 * @author ProLinkLi Development Team
 * @since 1.0
 */
public class OAuthUsernameUtil {

  private final static Integer MAX_OAUTH_ID_LENGTH = 10;
  private final static Integer MAX_USERNAME_LENGTH = 30;

  /**
   * Generates a unique username from OAuth user email and OAuth ID.
   * 
   * @param email User's email address from OAuth provider
   * @param oauthId Unique OAuth user identifier from provider
   * @return String username that is unique, normalized, and length-compliant
   * @throws IllegalArgumentException if email or OAuth ID is null or empty
   * 
   * @documentation-pr-rule.mdc
   * 
   * Generation process:
   * 1. Validates input parameters are not null or empty
   * 2. Extracts email local part (text before @)
   * 3. Normalizes email to lowercase, removes special characters
   * 4. Truncates OAuth ID to max 10 characters, removes special characters
   * 5. Combines email and OAuth ID components
   * 6. Truncates alternately if combined length exceeds 30 characters
   * 7. Returns final username string
   * 
   * Examples:
   * - email="john.doe@example.com", oauthId="123456789" → "johndoe123456789"
   * - email="user@test.com", oauthId="verylongid" → "userverylongi" (truncated)
   * 
   * Character filtering removes: !"#$%&'()*+,-./:;<=>?@[\]^_`{|}~
   * Ensures usernames contain only: a-z, 0-9
   */
  public static String generateOAuthUsername(String email, String oauthId) {
    if (email == null || email.isEmpty() || oauthId == null || oauthId.isEmpty()) {
      throw new IllegalArgumentException("Email and OAuth ID cannot be null or empty");
    }

    // Normalize email to lowercase and remove any special characters
    String normalizedEmail = email.split("@")[0]; // Use the part before '@' for username
    normalizedEmail = normalizedEmail.toLowerCase().replaceAll("[^a-z0-9]", "");

    String truncatedOauthId = oauthId.replaceAll("[^a-z0-9]", "").toLowerCase();
    if (truncatedOauthId.isBlank()) {
      throw new IllegalArgumentException("OAuth ID must not be empty or contain only special characters");
    }

    truncatedOauthId = truncatedOauthId.substring(0,
        Math.min(oauthId.length(), MAX_OAUTH_ID_LENGTH));

    StringBuilder emailBuilder = new StringBuilder(normalizedEmail);
    StringBuilder oauthIdBuilder = new StringBuilder(truncatedOauthId);

    boolean wasEmailTruncated = false;
    // Truncate to a maximum length of the email necessary
    while ((emailBuilder.length() + oauthIdBuilder.length()) > MAX_USERNAME_LENGTH) {

      if (!wasEmailTruncated) {
        emailBuilder.delete(emailBuilder.length() - 1, emailBuilder.length());
      } else {
        oauthIdBuilder.delete(oauthIdBuilder.length() - 1, oauthIdBuilder.length());
      }

      wasEmailTruncated = !wasEmailTruncated; // Alternate truncation

    }

    // Generate a unique username by combining normalized email with OAuth ID
    return emailBuilder.toString() + oauthIdBuilder.toString();
  }

}
