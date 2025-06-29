package com.prolinkli.core.app.components.user.service;

import java.util.List;
import java.util.Map;

import com.prolinkli.core.app.Constants;
import com.prolinkli.core.app.Constants.AuthenticationKeys;
import com.prolinkli.core.app.Constants.LkUserAuthenticationMethods;
import com.prolinkli.core.app.components.user.model.AuthorizedUser;
import com.prolinkli.core.app.components.user.model.User;
import com.prolinkli.core.app.components.user.model.UserAuthenticationForm;
import com.prolinkli.core.app.db.model.generated.UserDb;
import com.prolinkli.core.app.db.model.generated.UserDbExample;
import com.prolinkli.framework.auth.model.AuthProvider;
import com.prolinkli.framework.auth.providers.GoogleOAuth2Provider;
import com.prolinkli.framework.auth.util.AuthValidationUtil;
import com.prolinkli.framework.auth.util.OAuthUsernameUtil;
import com.prolinkli.framework.db.dao.Dao;
import com.prolinkli.framework.db.dao.DaoFactory;
import com.prolinkli.framework.exception.exceptions.model.ResourceAlreadyExists;

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
  private GoogleOAuth2Provider googleOAuth2Provider;

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

    try {

      // Attempt to create an OAuth user if the method is OAuth2.
      authProvider.createUser(user, dao);
      return userAuthService.login(user);

    } catch (ResourceAlreadyExists e) {

      // If the user already exists, we throw a ResourceAlreadyExists exception.
      throw new ResourceAlreadyExists("User already exists with username: " + user.getUsername());

    } catch (Exception e) {

      LOGGER.error("Failed to create user: {}:{}", user.getId(), user.getUsername(), e);
      // Rollback the user creation if any error occurs.
      rollback(user);
      throw new RuntimeException("Failed to create user: " + user.getUsername(), e);

    }

  }

  private AuthProvider getAuthProvider(String providerName) {
    return authProviders.stream()
        .filter(provider -> provider.getProviderName().toLowerCase().equals(providerName.toLowerCase()))
        .findFirst()
        .orElseThrow(() -> new IllegalStateException("Authentication provider not found: " + providerName));
  }

  private void rollback(User user) {

    if (user == null) {
      LOGGER.warn("No user to rollback");
      return;
    }

    if (user.getId() != null) {
      LOGGER.warn("Rolling back user creation for user: {}:{}", user.getUsername(), user.getId());
      dao.delete(user.getId());
    } else if (user.getUsername() != null) {
      LOGGER.warn("Rolling back user creation for user: {}", user.getUsername());
      UserDbExample example = new UserDbExample();
      example.createCriteria().andUsernameEqualTo(user.getUsername());
      dao.delete(example);
    } else {
      LOGGER.warn("No user to rollback");
    }

  }

}
