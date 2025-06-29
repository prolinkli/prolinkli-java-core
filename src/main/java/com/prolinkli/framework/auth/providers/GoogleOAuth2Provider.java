package com.prolinkli.framework.auth.providers;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.Map;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.prolinkli.core.app.Constants;
import com.prolinkli.core.app.Constants.AuthenticationKeys;
import com.prolinkli.core.app.Constants.LkUserAuthenticationMethods;
import com.prolinkli.core.app.components.user.model.User;
import com.prolinkli.core.app.components.user.model.UserAuthenticationForm;
import com.prolinkli.core.app.components.user.service.UserGetService;
import com.prolinkli.core.app.db.model.generated.UserDb;
import com.prolinkli.framework.auth.model.AuthProvider;
import com.prolinkli.framework.auth.service.GoogleOAuth2Service;
import com.prolinkli.framework.auth.util.AuthValidationUtil;
import com.prolinkli.framework.auth.util.OAuthUsernameUtil;
import com.prolinkli.framework.config.secrets.SecretsManager;
import com.prolinkli.framework.db.dao.Dao;
import com.prolinkli.framework.exception.exceptions.model.ResourceNotFoundException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class GoogleOAuth2Provider implements AuthProvider {

  private static final Logger LOGGER = LoggerFactory.getLogger(GoogleOAuth2Provider.class);

  @Autowired
  private SecretsManager secretsManager;

  @Autowired
  private UserGetService userGetService;

  @Autowired
  private GoogleOAuth2Service googleOAuth2Service;

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
        throw new ResourceNotFoundException("User not found with username: " + systemUsername);
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
  public void createUser(UserAuthenticationForm user, Dao<UserDb, Long> dao) {
    try {
      // Extract user information from Google ID token
      String idToken = user.getSpecialToken();
      var googleUserInfo = getGoogleUserInfo(idToken);

      if (googleUserInfo == null) {
        throw new IllegalArgumentException("Failed to extract user information from Google ID token");
      }

      String email = googleUserInfo.getEmail();
      String googleUserId = googleUserInfo.getSubject();

      if (email == null || email.isEmpty() || googleUserId == null || googleUserId.isEmpty()) {
        throw new IllegalArgumentException("Google ID token does not contain required user information");
      }

      // Generate system username
      String systemUsername = OAuthUsernameUtil.generateOAuthUsername(email, googleUserId);
      user.setUsername(systemUsername);

      // Check if user already exists
      User foundUser = null;
      try {
        foundUser = userGetService.getUserByUsername(systemUsername);
      } catch (IllegalArgumentException e) {
        // User does not exist, proceed with creation.
      }

      if (foundUser != null) {
        throw new IllegalArgumentException("User already exists with username: " + systemUsername);
      }

      AuthValidationUtil.validateUserName(systemUsername);

      // Insert the user into the database
      UserDb userDb = new UserDb();
      userDb.setUsername(systemUsername);
      userDb.setAuthenticationMethod(user.getAuthenticationMethodLk().toUpperCase());

      LOGGER.debug("Inserting OAuth user into database: {}", systemUsername);
      dao.insert(userDb);

      // Verify that the insert was successful and ID was generated
      if (userDb.getId() == null) {
        throw new RuntimeException("Database insert failed - no ID was generated for user: " + systemUsername);
      }

      user.setId(userDb.getId());

      // insert credentials for the OAuth user
      insertCredentialsForUser(user, Map.of(
          Constants.AuthenticationKeys.GOOGLE_OAUTH2.ID_TOKEN, idToken,
          Constants.AuthenticationKeys.GOOGLE_OAUTH2.SUBJECT, googleUserId));

      // For OAuth users, we don't need to store credentials in the traditional way
      // The OAuth provider handles authentication
      LOGGER.info("Successfully created OAuth user: {}:{} with email: {}", user.getId(), systemUsername, email);

      return;

    } catch (Exception e) {
      // Rollback the user creation if anything fails
      throw new RuntimeException("Failed to create OAuth user: " + e.getMessage(), e);
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
    this.validateCredentials(credentials);

    String idToken = credentials.get(AuthenticationKeys.GOOGLE_OAUTH2.ID_TOKEN).toString();
    try {
      GoogleIdToken idTokenObj = verifyGoogleIdToken(idToken);
      if (idTokenObj == null) {
        throw new IllegalArgumentException("Invalid Google ID token - verification returned null");
      }

      GoogleIdToken.Payload payload = idTokenObj.getPayload();
      String googleUserId = payload.getSubject();

      // Insert the OAuth relationship into the database
      googleOAuth2Service.insertUserOAuthRelationship(
          Map.of(
              "oAuthId", googleUserId,
              "userId", user.getId()));

    } catch (GeneralSecurityException | IOException e) {
      throw new IllegalArgumentException("Failed to verify Google ID token: " + e.getMessage(), e);
    }

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

  @Override
  public User getUserFromCredentials(UserAuthenticationForm userAuthForm) {
    try {

      var googleUserInfo = getGoogleUserInfo(userAuthForm.getSpecialToken());
      if (googleUserInfo == null) {
        throw new IllegalArgumentException("Failed to extract user information from Google ID token");
      }

      return googleOAuth2Service.getUserByOAuthId(googleUserInfo.getSubject());

    } catch (GeneralSecurityException | IOException e) {
      throw new IllegalArgumentException("Failed to get user from Google ID token: " + e.getMessage(), e);
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
