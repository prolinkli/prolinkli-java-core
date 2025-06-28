package com.prolinkli.framework.auth.util;

public class OAuthUsernameUtil {

  public static String generateOAuthUsername(String email, String oauthId) {
    if (email == null || email.isEmpty() || oauthId == null || oauthId.isEmpty()) {
      throw new IllegalArgumentException("Email and OAuth ID cannot be null or empty");
    }

    // Normalize email to lowercase and remove any special characters
    String normalizedEmail = email.split("@")[0]; // Use the part before '@' for username
    normalizedEmail = normalizedEmail.toLowerCase().replaceAll("[^a-z0-9]", "");

    String truncatedOauthId = oauthId.replaceAll("[^a-z0-9]", "").toLowerCase().substring(0,
        Math.min(oauthId.length(), 10));

    StringBuilder emailBuilder = new StringBuilder(normalizedEmail);
    StringBuilder oauthIdBuilder = new StringBuilder(truncatedOauthId);

    int lastTruncated = 0b0000;
    // Truncate to a maximum length of the email necessary
    while ((emailBuilder.length() + oauthIdBuilder.length()) > 30) {

      if ((lastTruncated & 0b0001) == 0) {
        emailBuilder.delete(emailBuilder.length() - 1, emailBuilder.length());
      } else {
        oauthIdBuilder.delete(oauthIdBuilder.length() - 1, oauthIdBuilder.length());
      }

      lastTruncated += 1;

    }

    // Generate a unique username by combining normalized email with OAuth ID
    return emailBuilder.toString() + oauthIdBuilder.toString();
  }

}
