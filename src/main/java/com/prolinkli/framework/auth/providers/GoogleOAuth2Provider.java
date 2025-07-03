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

/**
 * Google OAuth2 authentication provider implementation.
 * 
 * @documentation-pr-rule.mdc
 * 
 * This authentication provider handles Google OAuth2 integration for user authentication
 * and account creation. It implements the AuthProvider interface to provide Google-specific
 * authentication logic within the unified authentication framework.
 * 
 * Key features:
 * - Google ID token verification using Google's official client libraries
 * - Automatic user account creation from Google profile information
 * - Secure username generation from email and Google user ID
 * - Integration with internal user management and OAuth account linking
 * - Comprehensive error handling and validation
 * 
 * Security considerations:
 * - Verifies ID tokens against Google's public keys
 * - Validates token audience to prevent token substitution attacks
 * - Enforces proper JWT token format validation
 * - Uses secure random username generation for OAuth users
 * 
 * Dependencies:
 * - SecretsManager: For Google OAuth2 client credentials
 * - UserGetService: For existing user lookup operations
 * - GoogleOAuth2Service: For OAuth account relationship management
 * - AuthValidationUtil: For username validation rules
 * - OAuthUsernameUtil: For secure username generation
 * 
 * @author ProLinkLi Development Team
 * @since 1.0
 */
@Component
public class GoogleOAuth2Provider implements AuthProvider {

  private static final Logger LOGGER = LoggerFactory.getLogger(GoogleOAuth2Provider.class);

  @Autowired
  private SecretsManager secretsManager;

  @Autowired
  private UserGetService userGetService;

  @Autowired
  private GoogleOAuth2Service googleOAuth2Service;

  /**
   * Google ID token verifier instance for token validation.
   * Lazily initialized on first use with proper audience configuration.
   */
  private GoogleIdTokenVerifier verifier;

  /**
   * Returns the Google OAuth2 provider name identifier.
   * 
   * @return "GOOGLE" as defined in LkUserAuthenticationMethods.GOOGLE_OAUTH2
   * 
   * @documentation-pr-rule.mdc
   * This identifier is used throughout the system for:
   * - Authentication method lookup and routing
   * - Database authentication method storage
   * - Provider-specific logic branching
   */
  @Override
  public String getProviderName() {
    return LkUserAuthenticationMethods.GOOGLE_OAUTH2;
  }

  /**
   * Authenticates a user using Google OAuth2 ID token.
   * 
   * @param credentials Map containing Google ID token under AuthenticationKeys.GOOGLE_OAUTH2.ID_TOKEN
   * @return Boolean true if authentication succeeds and user exists in system
   * @throws IllegalArgumentException if credentials are invalid or ID token verification fails
   * @throws ResourceNotFoundException if user does not exist in the system
   * @throws RuntimeException if Google authentication fails due to security exceptions
   * 
   * @documentation-pr-rule.mdc
   * Authentication process:
   * 1. Validates credentials contain required ID token
   * 2. Verifies ID token signature and audience using Google's public keys
   * 3. Extracts user email and Google user ID from token payload
   * 4. Generates system username using OAuthUsernameUtil
   * 5. Looks up existing user by Google OAuth ID
   * 6. Returns true if user exists, throws ResourceNotFoundException if not
   * 
   * This method only authenticates existing users. New user creation is handled
   * separately by the createUser method.
   */
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
      User existingUser = googleOAuth2Service.getUserByOAuthId(googleUserId);
      if (existingUser == null) {
        throw new ResourceNotFoundException("User not found with oauthId: " + systemUsername);
      }

      return true;

    } catch (GeneralSecurityException | IOException e) {
      throw new IllegalArgumentException("Failed to verify Google ID token: " + e.getMessage(), e);
    } catch (IllegalArgumentException e) {
      // Re-throw IllegalArgumentException with more context
      throw new RuntimeException("Google authentication failed: " + e.getMessage(), e);
    }
  }

  /**
   * Creates a new user account from Google OAuth2 authentication.
   * 
   * @param user UserAuthenticationForm containing Google ID token in specialToken field
   * @param dao Database access object for user creation operations
   * @throws IllegalArgumentException if ID token is invalid or user already exists
   * @throws RuntimeException if user creation fails due to database or validation errors
   * 
   * @documentation-pr-rule.mdc
   * User creation process:
   * 1. Extracts user information from Google ID token payload
   * 2. Validates required fields (email, Google user ID) are present
   * 3. Generates unique system username using email and OAuth ID
   * 4. Checks for existing users to prevent duplicates
   * 5. Validates username against system requirements
   * 6. Creates user record in database with OAuth authentication method
   * 7. Links Google OAuth account to newly created user
   * 8. Logs successful creation with user details
   * 
   * Username generation ensures uniqueness and compliance with system constraints
   * while maintaining traceability to the original Google account.
   */
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

  /**
   * Validates OAuth2 credentials contain required Google ID token.
   * 
   * @param credentials Map of authentication credentials
   * @throws IllegalArgumentException if credentials are null, empty, or missing ID token
   * 
   * @documentation-pr-rule.mdc
   * Validation checks:
   * - Credentials map is not null or empty
   * - Contains AuthenticationKeys.GOOGLE_OAUTH2.ID_TOKEN key
   * - ID token value is not null or empty string
   * 
   * This method provides early validation to ensure downstream processing
   * receives properly formatted credentials.
   */
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
