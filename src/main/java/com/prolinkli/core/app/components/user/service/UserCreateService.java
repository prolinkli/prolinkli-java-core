package com.prolinkli.core.app.components.user.service;

import java.util.List;
import java.util.Map;

import com.prolinkli.core.app.Constants;
import com.prolinkli.core.app.Constants.AuthenticationKeys;
import com.prolinkli.core.app.components.user.model.AuthorizedUser;
import com.prolinkli.core.app.components.user.model.User;
import com.prolinkli.core.app.components.user.model.UserAuthenticationForm;
import com.prolinkli.core.app.db.model.generated.UserDb;
import com.prolinkli.core.app.db.model.generated.UserDbExample;
import com.prolinkli.framework.auth.model.AuthProvider;
import com.prolinkli.framework.db.dao.Dao;
import com.prolinkli.framework.db.dao.DaoFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserCreateService {

  private static final Logger LOGGER = LoggerFactory.getLogger(UserCreateService.class);

  private final List<AuthProvider> authProviders;

  private final Dao<UserDb, Long> dao;

  @Autowired
  private UserGetService userGetService;

  @Autowired
  private UserAuthService userAuthService;

  @Autowired
  public UserCreateService(List<AuthProvider> authProviders, DaoFactory daoFactory) {
    this.authProviders = authProviders;
    this.dao = daoFactory.getDao(UserDb.class, Long.class);
  }

  /**
   * Creates a new user with the provided credentials.
   * This method is used to create a new user in the system.
   *
   * @param credentials the credentials for the new user
   */
  public AuthorizedUser createUser(UserAuthenticationForm user) {

    if (user == null) {
      throw new IllegalArgumentException("User and form cannot be null");
    }

    AuthProvider authProvider = getAuthProvider(user.getAuthenticationMethodLk());

    User foundUser = null;
    try {
      foundUser = userGetService.getUserByUsername(user.getUsername());
    } catch (IllegalArgumentException e) {
      // User does not exist, proceed with creation.
    }

    if (foundUser != null) {
      throw new IllegalArgumentException("User already exists with username: " + user.getUsername());
    }

    validateUserName(user.getUsername());

    Map<String, Object> credentials = Map.of(
        AuthenticationKeys.PASSWORD.USERNAME, user.getUsername(),
        AuthenticationKeys.PASSWORD.PASSWORD, user.getSpecialToken());

    // Insert the user into the database.
    UserDb userDb = new UserDb();
    userDb.setUsername(user.getUsername());
    userDb.setAuthenticationMethod(user.getAuthenticationMethodLk().toUpperCase());

    dao.insert(userDb);
    // TODO: implement mapper
    user.setId(userDb.getId());

    try {
      // Validate the user credentials before inserting.
      authProvider.validateCredentials(credentials);
      // Insert the credentials for the user.
      authProvider.insertCredentialsForUser(user, credentials);
    } catch (Exception e) {
      // Rollback the user creation if credentials insertion fails.
      rollback(user);
      LOGGER.error("Failed to insert credentials for user: {}:{}", user.getId(), user.getUsername());
      throw new RuntimeException("Failed to insert credentials for user: " + user.getUsername(), e);
    }

    // Return the authorized user.
    return userAuthService.login(user);

  }

  private void validateUserName(String username) {
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

  private AuthProvider getAuthProvider(String providerName) {
    return authProviders.stream()
        .filter(provider -> provider.getProviderName().toLowerCase().equals(providerName.toLowerCase()))
        .findFirst()
        .orElseThrow(() -> new IllegalStateException("Authentication provider not found: " + providerName));
  }

  private void rollback(User user) {
    if (user != null && user.getId() != null) {
      LOGGER.warn("Rolling back user creation for user: {}:{}", user.getUsername(), user.getId());
      dao.delete(user.getId());
    } else if (user != null && user.getUsername() != null) {
      LOGGER.warn("Rolling back user creation for user: {}", user.getUsername());
      UserDbExample example = new UserDbExample();
      example.createCriteria().andUsernameEqualTo(user.getUsername());
      dao.delete(example);
    } else {
      LOGGER.warn("No user to rollback");
    }
  }

}
