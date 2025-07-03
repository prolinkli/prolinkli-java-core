package com.prolinkli.framework.auth.providers;

import java.util.Map;

import com.prolinkli.core.app.Constants.AuthenticationKeys;
import com.prolinkli.core.app.Constants.LkUserAuthenticationMethods;
import com.prolinkli.core.app.components.user.model.User;
import com.prolinkli.core.app.components.user.model.UserAuthenticationForm;
import com.prolinkli.core.app.components.user.model.UserPassword;
import com.prolinkli.core.app.components.user.service.UserGetService;
import com.prolinkli.core.app.db.model.generated.UserDb;
import com.prolinkli.framework.auth.model.AuthProvider;
import com.prolinkli.framework.auth.service.InternalAuthService;
import com.prolinkli.framework.auth.util.AuthValidationUtil;
import com.prolinkli.framework.db.dao.Dao;
import com.prolinkli.framework.exception.exceptions.model.InvalidCredentialsException;
import com.prolinkli.framework.exception.exceptions.model.ResourceAlreadyExists;
import com.prolinkli.framework.exception.exceptions.model.ResourceNotFoundException;
import com.prolinkli.framework.hash.Hasher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class InternalAuthProvider implements AuthProvider {

  private static final Logger LOGGER = LoggerFactory.getLogger(InternalAuthProvider.class);

  @Autowired
  private UserGetService userGetService;

  @Autowired
  private InternalAuthService internalAuthService;

  @Override
  public String getProviderName() {
    return LkUserAuthenticationMethods.PASSWORD;
  }

  @Override
  public Boolean authenticate(Map<String, Object> credentials) {
    this.validateCredentials(credentials);

    UserPassword user = userGetService
        .getUserWithPasswordByUsername(credentials.get(AuthenticationKeys.PASSWORD.USERNAME).toString());
    if (user == null) {
      throw new ResourceNotFoundException("User not found for the provided username");
    }
    String password = credentials.get(AuthenticationKeys.PASSWORD.PASSWORD).toString();

    if (!Hasher.verifyString(password, user.getPassword())) {
      throw new InvalidCredentialsException("Invalid password for the provided username");
    }

    return true;
  }

  @Override
  public void createUser(UserAuthenticationForm user, Dao<UserDb, Long> dao) {
    User foundUser = null;
    try {
      foundUser = userGetService.getUserByUsername(user.getUsername());
    } catch (IllegalArgumentException e) {
      // User does not exist, proceed with creation.
    }

    if (foundUser != null) {
      throw new ResourceAlreadyExists("User already exists with username: " + user.getUsername());
    }

    AuthValidationUtil.validateUserName(user.getUsername());

    Map<String, Object> credentials = Map.of(
        AuthenticationKeys.PASSWORD.USERNAME, user.getUsername(),
        AuthenticationKeys.PASSWORD.PASSWORD, user.getSpecialToken());

    // Insert the user into the database.
    UserDb userDb = new UserDb();
    userDb.setUsername(user.getUsername());
    userDb.setAuthenticationMethod(user.getAuthenticationMethodLk().toUpperCase());

    LOGGER.debug("Inserting password user into database: {}", user.getUsername());
    dao.insert(userDb);

    // Verify that the insert was successful and ID was generated
    if (userDb.getId() == null) {
      throw new RuntimeException("Database insert failed - no ID was generated for user: " + user.getUsername());
    }

    user.setId(userDb.getId());

    validateCredentials(credentials);
    // Insert the credentials for the user.
    insertCredentialsForUser(user, credentials);
  }

  @Override
  public void validateCredentials(Map<String, Object> credentials) {
    if (credentials == null || credentials.isEmpty()) {
      throw new IllegalArgumentException("Credentials cannot be null or empty");
    }

    final String usernameKey = AuthenticationKeys.PASSWORD.USERNAME;
    final String passwordKey = AuthenticationKeys.PASSWORD.PASSWORD;

    if (!credentials.containsKey(usernameKey)
        || !credentials.containsKey(passwordKey)) {
      throw new IllegalArgumentException("Credentials must contain username and password");
    }

    String username = (String) credentials.get(usernameKey);
    String password = (String) credentials.get(passwordKey);

    if (username == null || username.isEmpty()) {
      throw new IllegalArgumentException("Username cannot be null or empty");
    }

    if (password == null || password.isEmpty()) {
      throw new IllegalArgumentException("Password cannot be null or empty");
    }
  }

  public void insertCredentialsForUser(User user, Map<String, Object> credentials) {
    this.validateCredentials(credentials);

    String password = credentials.get(AuthenticationKeys.PASSWORD.PASSWORD).toString();

    // Here you would typically save the user and password to the database
    // For example:
    UserPassword userPassword = new UserPassword();
    userPassword.setUser(user);
    userPassword.setPassword(password);

    // Save the userPassword object to the database (not shown here)
    internalAuthService.insertCredentialsForUser(user.getId(), userPassword);
  }

  @Override
  public User getUserFromCredentials(UserAuthenticationForm userAuthForm) {

    var credentials = userAuthForm.getParameters();
    this.validateCredentials(credentials);

    String username = credentials.get(AuthenticationKeys.PASSWORD.USERNAME).toString();

    User user = userGetService.getUserByUsername(username);
    if (user == null) {
      throw new IllegalArgumentException("User not found for the provided username");
    }

    return user;
  }

}
