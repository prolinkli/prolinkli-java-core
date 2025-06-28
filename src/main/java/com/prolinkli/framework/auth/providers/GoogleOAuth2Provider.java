package com.prolinkli.framework.auth.providers;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.Map;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.prolinkli.core.app.Constants.AuthenticationKeys;
import com.prolinkli.core.app.Constants.LkUserAuthenticationMethods;
import com.prolinkli.core.app.components.user.model.User;
import com.prolinkli.core.app.components.user.service.UserGetService;
import com.prolinkli.framework.auth.model.AuthProvider;
import com.prolinkli.framework.auth.util.OAuthUsernameUtil;
import com.prolinkli.framework.config.secrets.SecretsManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class GoogleOAuth2Provider implements AuthProvider {

  @Autowired
  private SecretsManager secretsManager;

  @Autowired
  private UserGetService userGetService;

  private GoogleIdTokenVerifier verifier;

  @Override
  public String getProviderName() {
    return LkUserAuthenticationMethods.GOOGLE_OAUTH2;
  }

  @Override
  public Boolean authenticate(Map<String, Object> credentials) {
    this.validateCredentials(credentials);

    String idTokenString = credentials.get(AuthenticationKeys.GOOGLE_OAUTH2.ID_TOKEN).toString();

    try {
      GoogleIdToken idToken = verifyGoogleIdToken(idTokenString);
      if (idToken == null) {
        throw new IllegalArgumentException("Invalid Google ID token - verification returned null");
      }

      GoogleIdToken.Payload payload = idToken.getPayload();
      String email = payload.getEmail();
      String googleUserId = payload.getSubject();

      // generate system username based on email and Google user ID
      String systemUsername = OAuthUsernameUtil.generateOAuthUsername(email, googleUserId);

      if (email == null || email.isEmpty()) {
        throw new IllegalArgumentException("Google ID token payload does not contain a valid email");
      }

      // Check if user exists in your system
      User existingUser = userGetService.getUserByUsername(systemUsername);
      if (existingUser == null) {
        throw new IllegalArgumentException("User not found. Please register first.");
      }

      return true;

    } catch (GeneralSecurityException | IOException e) {
      throw new IllegalArgumentException("Failed to verify Google ID token: " + e.getMessage(), e);
    } catch (IllegalArgumentException e) {
      // Re-throw IllegalArgumentException with more context
      throw new IllegalArgumentException("Google authentication failed: " + e.getMessage(), e);
    }
  }

  @Override
  public void validateCredentials(Map<String, Object> credentials) {
    if (credentials == null || credentials.isEmpty()) {
      throw new IllegalArgumentException("Credentials cannot be null or empty");
    }

    final String idTokenKey = AuthenticationKeys.GOOGLE_OAUTH2.ID_TOKEN;

    if (!credentials.containsKey(idTokenKey)) {
      throw new IllegalArgumentException("Credentials must contain Google ID token");
    }

    String idToken = (String) credentials.get(idTokenKey);
    if (idToken == null || idToken.isEmpty()) {
      throw new IllegalArgumentException("Google ID token cannot be null or empty");
    }
  }

  @Override
  public void insertCredentialsForUser(User user, Map<String, Object> credentials) {
    // This method is not applicable for Google OAuth2 as credentials are not stored
    // in the same way as traditional username/password systems.
    throw new UnsupportedOperationException("Google OAuth2 does not support inserting credentials directly. Not yet.");
  }

  private GoogleIdToken verifyGoogleIdToken(String idTokenString)
      throws GeneralSecurityException, IOException {

    if (idTokenString == null || idTokenString.isEmpty()) {
      throw new IllegalArgumentException("ID token string cannot be null or empty");
    }

    // Basic JWT format validation (should have 3 parts separated by dots)
    if (!isValidTokenFormat(idTokenString)) {
      throw new IllegalArgumentException("Invalid ID token format");
    }

    if (verifier == null) {
      verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new GsonFactory())
          .setAudience(Collections.singletonList(secretsManager.getGoogleClientId()))
          .build();
    }

    try {
      return verifier.verify(idTokenString);
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException("Failed to verify Google ID token: " + e.getMessage()
          + ". Please ensure the token is valid and not expired.");
    }
  }

  /**
   * Helper method to extract user information from Google ID token
   * This can be used by other services that need Google user info
   */
  public GoogleIdToken.Payload getGoogleUserInfo(String idTokenString)
      throws GeneralSecurityException, IOException {

    GoogleIdToken idToken = verifyGoogleIdToken(idTokenString);
    return idToken != null ? idToken.getPayload() : null;
  }

  private boolean isValidTokenFormat(String token) {
    if (token == null || token.isEmpty()) {
      return false;
    }
    String[] parts = token.split("\\.");
    return parts.length == 3 && parts[0] != null && !parts[0].trim().isEmpty()
        && parts[1] != null && !parts[1].trim().isEmpty() && parts[2] != null && !parts[2].trim().isEmpty();
  }
}
