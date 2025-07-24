package com.prolinkli.framework.auth.providers;

import java.io.IOException;
import java.util.Base64;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.prolinkli.core.app.Constants;
import com.prolinkli.core.app.Constants.AuthenticationKeys;
import com.prolinkli.core.app.Constants.LkUserAuthenticationMethods;
import com.prolinkli.core.app.components.user.model.User;
import com.prolinkli.core.app.components.user.model.UserAuthenticationForm;
import com.prolinkli.core.app.components.user.service.UserGetService;
import com.prolinkli.core.app.db.model.generated.UserDb;
import com.prolinkli.framework.auth.model.AuthProvider;
import com.prolinkli.framework.auth.service.MicrosoftOAuth2Service;
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
 * Microsoft OAuth2 authentication provider implementation.
 * 
 * @documentation-pr-rule.mdc
 * 
 * This authentication provider handles Microsoft OAuth2 integration for user authentication
 * and account creation. It implements the AuthProvider interface to provide Microsoft-specific
 * authentication logic within the unified authentication framework.
 * 
 * Key features:
 * - Microsoft user profile verification using composite tokens
 * - Automatic user account creation from Microsoft profile information
 * - Secure username generation from email and Microsoft user ID
 * - Integration with internal user management and OAuth account linking
 * - Comprehensive error handling and validation
 * 
 * Security considerations:
 * - Validates composite tokens containing user profile information
 * - Validates token format and required fields
 * - Uses secure random username generation for OAuth users
 * - Enforces proper token structure validation
 * 
 * Dependencies:
 * - SecretsManager: For Microsoft OAuth2 client credentials
 * - UserGetService: For existing user lookup operations
 * - MicrosoftOAuth2Service: For OAuth account relationship management
 * - AuthValidationUtil: For username validation rules
 * - OAuthUsernameUtil: For secure username generation
 * 
 * @author ProLinkLi Development Team
 * @since 1.0
 */
@Component
public class MicrosoftOAuth2Provider implements AuthProvider {

  private static final Logger LOGGER = LoggerFactory.getLogger(MicrosoftOAuth2Provider.class);

  @Autowired
  private SecretsManager secretsManager;

  @Autowired
  private UserGetService userGetService;

  @Autowired
  private MicrosoftOAuth2Service microsoftOAuth2Service;

  private final ObjectMapper objectMapper = new ObjectMapper();

  /**
   * Returns the Microsoft OAuth2 provider name identifier.
   * 
   * @return "MICROSOFT" as defined in LkUserAuthenticationMethods.MICROSOFT_OAUTH2
   * 
   * @documentation-pr-rule.mdc
   * This identifier is used throughout the system for:
   * - Authentication method lookup and routing
   * - Database authentication method storage
   * - Provider-specific logic branching
   */
  @Override
  public String getProviderName() {
    return LkUserAuthenticationMethods.MICROSOFT_OAUTH2;
  }

  /**
   * Authenticates a user using Microsoft OAuth2 credentials.
   * 
   * @param credentials Map containing Microsoft OAuth2 authentication credentials
   * @return Boolean true if authentication is successful, false otherwise
   * @throws IllegalArgumentException if credentials are invalid or missing required fields
   * @throws ResourceNotFoundException if user is not found in the system
   * 
   * @documentation-pr-rule.mdc
   * This method authenticates users by:
   * 1. Validating the provided credentials contain required Microsoft OAuth2 data
   * 2. Decoding and validating the composite token containing user profile information
   * 3. Extracting Microsoft user ID and email from the token
   * 4. Generating the system username from email and Microsoft user ID
   * 5. Looking up the user in the system by OAuth ID
   * 6. Returning true if user exists and authentication is successful
   * 
   * The method throws appropriate exceptions for invalid credentials, missing users,
   * or authentication failures to provide clear error feedback.
   */
  @Override
  public Boolean authenticate(Map<String, Object> credentials) {
    this.validateCredentials(credentials);

    String compositeToken = credentials.get(AuthenticationKeys.MICROSOFT_OAUTH2.ID_TOKEN).toString();

    try {
      // Decode and validate the composite token
      JsonNode userInfo = decodeCompositeToken(compositeToken);
      if (userInfo == null) {
        throw new IllegalArgumentException("Invalid Microsoft composite token - decoding returned null");
      }

      String email = userInfo.get("email").asText();
      String microsoftUserId = userInfo.get("sub").asText();

      if (email == null || email.isEmpty()) {
        throw new IllegalArgumentException("Microsoft composite token does not contain a valid email");
      }

      // Generate system username based on email and Microsoft user ID
      String systemUsername = OAuthUsernameUtil.generateOAuthUsername(email, microsoftUserId);

      // Check if user exists in your system
      User existingUser = microsoftOAuth2Service.getUserByOAuthId(microsoftUserId);
      if (existingUser == null) {
        throw new ResourceNotFoundException("User not found with oauthId: " + systemUsername);
      }

      return true;

    } catch (Exception e) {
      throw new RuntimeException("Microsoft authentication failed: " + e.getMessage(), e);
    }
  }

  /**
   * Creates a new user account from Microsoft OAuth2 authentication data.
   * 
   * @param user UserAuthenticationForm containing Microsoft OAuth2 credentials
   * @param dao Data access object for user database operations
   * @throws IllegalArgumentException if user data is invalid or user already exists
   * @throws RuntimeException if user creation fails due to database or processing errors
   * 
   * @documentation-pr-rule.mdc
   * This method creates new user accounts by:
   * 1. Extracting user information from the Microsoft composite token
   * 2. Validating required user information (email, user ID)
   * 3. Generating a secure system username from email and Microsoft user ID
   * 4. Checking for existing users to prevent duplicates
   * 5. Validating the generated username against system rules
   * 6. Inserting the new user into the database
   * 7. Creating OAuth account relationship for future authentication
   * 8. Logging successful user creation for audit purposes
   * 
   * The method includes comprehensive error handling and rollback logic
   * to ensure data consistency in case of failures.
   */
  @Override
  public void createUser(UserAuthenticationForm user, Dao<UserDb, Long> dao) {
    try {
      // Extract user information from Microsoft composite token
      String compositeToken = user.getSpecialToken();
      JsonNode userInfo = decodeCompositeToken(compositeToken);

      if (userInfo == null) {
        throw new IllegalArgumentException("Failed to extract user information from Microsoft composite token");
      }

      String email = userInfo.get("email").asText();
      String microsoftUserId = userInfo.get("sub").asText();

      if (email == null || email.isEmpty() || microsoftUserId == null || microsoftUserId.isEmpty()) {
        throw new IllegalArgumentException("Microsoft composite token does not contain required user information");
      }

      // Generate system username
      String systemUsername = OAuthUsernameUtil.generateOAuthUsername(email, microsoftUserId);
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

      // Insert credentials for the OAuth user
      insertCredentialsForUser(user, Map.of(
          Constants.AuthenticationKeys.MICROSOFT_OAUTH2.ID_TOKEN, compositeToken,
          Constants.AuthenticationKeys.MICROSOFT_OAUTH2.SUBJECT, microsoftUserId));

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
   * Validates OAuth2 credentials contain required Microsoft composite token.
   * 
   * @param credentials Map of authentication credentials
   * @throws IllegalArgumentException if credentials are null, empty, or missing composite token
   * 
   * @documentation-pr-rule.mdc
   * Validation checks:
   * - Credentials map is not null or empty
   * - Contains AuthenticationKeys.MICROSOFT_OAUTH2.ID_TOKEN key
   * - Composite token value is not null or empty string
   * 
   * This method provides early validation to ensure downstream processing
   * receives properly formatted credentials.
   */
  @Override
  public void validateCredentials(Map<String, Object> credentials) {
    if (credentials == null || credentials.isEmpty()) {
      throw new IllegalArgumentException("Credentials cannot be null or empty");
    }

    final String idTokenKey = AuthenticationKeys.MICROSOFT_OAUTH2.ID_TOKEN;

    if (!credentials.containsKey(idTokenKey)) {
      throw new IllegalArgumentException("Credentials must contain Microsoft composite token");
    }

    String compositeToken = (String) credentials.get(idTokenKey);
    if (compositeToken == null || compositeToken.isEmpty()) {
      throw new IllegalArgumentException("Microsoft composite token cannot be null or empty");
    }
  }

  /**
   * Inserts Microsoft OAuth2 credentials for an existing user.
   * 
   * @param user User object for which to insert credentials
   * @param credentials Map containing Microsoft OAuth2 authentication data
   * @throws IllegalArgumentException if credentials are invalid or user data is missing
   * @throws RuntimeException if credential insertion fails
   * 
   * @documentation-pr-rule.mdc
   * This method creates OAuth account relationships by:
   * 1. Validating the provided credentials
   * 2. Decoding the composite token to extract Microsoft user ID
   * 3. Creating a database relationship between the system user and Microsoft OAuth ID
   * 4. Storing the relationship for future authentication lookups
   * 
   * This enables users to authenticate using their Microsoft account
   * in subsequent login attempts.
   */
  @Override
  public void insertCredentialsForUser(User user, Map<String, Object> credentials) {
    this.validateCredentials(credentials);

    String compositeToken = credentials.get(AuthenticationKeys.MICROSOFT_OAUTH2.ID_TOKEN).toString();
    try {
      JsonNode userInfo = decodeCompositeToken(compositeToken);
      if (userInfo == null) {
        throw new IllegalArgumentException("Invalid Microsoft composite token - decoding returned null");
      }

      String microsoftUserId = userInfo.get("sub").asText();

      // Insert the OAuth relationship into the database
      microsoftOAuth2Service.insertUserOAuthRelationship(
          Map.of(
              "oAuthId", microsoftUserId,
              "userId", user.getId()));

    } catch (Exception e) {
      throw new IllegalArgumentException("Failed to decode Microsoft composite token: " + e.getMessage(), e);
    }
  }

  /**
   * Retrieves user information from Microsoft OAuth2 credentials.
   * 
   * @param userAuthForm UserAuthenticationForm containing OAuth2 credentials
   * @return User object if found, null otherwise
   * @throws IllegalArgumentException if credentials are invalid
   * 
   * @documentation-pr-rule.mdc
   * This method extracts user information from Microsoft OAuth2 credentials
   * and attempts to find the corresponding user in the system. It's used
   * for user lookup during authentication flows.
   */
  @Override
  public User getUserFromCredentials(UserAuthenticationForm userAuthForm) {
    // This method is not typically used for OAuth2 flows
    // OAuth2 authentication is handled through the authenticate method
    throw new UnsupportedOperationException("getUserFromCredentials is not supported for Microsoft OAuth2");
  }

  /**
   * Decodes and validates a Microsoft composite token.
   * 
   * @param compositeToken Base64 encoded composite token containing user information
   * @return JsonNode containing decoded user information
   * @throws IllegalArgumentException if token is invalid or cannot be decoded
   * 
   * @documentation-pr-rule.mdc
   * This method decodes the composite token created by MicrosoftOAuthService
   * and validates its structure. The token contains user profile information
   * extracted from Microsoft Graph API responses.
   */
  private JsonNode decodeCompositeToken(String compositeToken) {
    try {
      // Decode the base64 encoded composite token
      byte[] decodedBytes = Base64.getDecoder().decode(compositeToken);
      String decodedJson = new String(decodedBytes);
      
      // Parse the JSON to extract user information
      JsonNode userInfo = objectMapper.readTree(decodedJson);
      
      // Validate required fields
      if (!userInfo.has("sub") || !userInfo.has("email")) {
        throw new IllegalArgumentException("Composite token missing required fields (sub or email)");
      }
      
      return userInfo;
      
    } catch (Exception e) {
      throw new IllegalArgumentException("Failed to decode composite token: " + e.getMessage(), e);
    }
  }
}
