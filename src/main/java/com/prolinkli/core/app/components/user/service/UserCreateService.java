package com.prolinkli.core.app.components.user.service;

import java.util.List;

import com.prolinkli.core.app.components.user.model.AuthorizedUser;
import com.prolinkli.core.app.components.user.model.UserAuthenticationForm;
import com.prolinkli.core.app.db.model.generated.UserDb;
import com.prolinkli.framework.auth.model.AuthProvider;
import com.prolinkli.framework.db.dao.Dao;
import com.prolinkli.framework.db.dao.DaoFactory;
import com.prolinkli.framework.exception.exceptions.model.ResourceAlreadyExists;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserCreateService {

  private static final Logger LOGGER = LoggerFactory.getLogger(UserCreateService.class);

  private final List<AuthProvider> authProviders;

  private final Dao<UserDb, Long> dao;

  @Autowired
  private UserAuthService userAuthService;

  @Autowired
  public UserCreateService(List<AuthProvider> authProviders, DaoFactory daoFactory) {
    this.authProviders = authProviders;
    this.dao = daoFactory.getDao(UserDb.class, Long.class);
  }

  /**
   * Creates a new user with the provided credentials.
   * This method is transactional - if any operation fails, all database changes
   * will be automatically rolled back.
   *
   * @param user the user authentication form for the new user
   * @return AuthorizedUser with JWT tokens
   * @throws ResourceAlreadyExists if user already exists
   * @throws RuntimeException      if user creation fails for any other reason
   */
  @Transactional(rollbackFor = Exception.class)
  public AuthorizedUser createUser(UserAuthenticationForm user) {

    if (user == null) {
      throw new IllegalArgumentException("User and form cannot be null");
    }

    AuthProvider authProvider = getAuthProvider(user.getAuthenticationMethodLk());

    try {

      // Create user account (inserts into users table and auth-specific tables)
      authProvider.createUser(user, dao);

      // Login and create JWT tokens (inserts into jwt_tokens table)
      return userAuthService.login(user);

    } catch (ResourceAlreadyExists e) {
      // If the user already exists, we throw a ResourceAlreadyExists exception.
      // Transaction will rollback automatically, but since this is a business logic
      // exception,
      // we want to propagate it as-is
      throw e;

    } catch (Exception e) {
      LOGGER.error("Failed to create user: {}:{}", user.getId(), user.getUsername(), e);
      // The @Transactional annotation will automatically rollback all database
      // operations
      // No manual cleanup needed - Spring will handle it
      throw new RuntimeException("Failed to create user: " + user.getUsername(), e);
    }
  }

  private AuthProvider getAuthProvider(String providerName) {
    return authProviders.stream()
        .filter(provider -> provider.getProviderName().toLowerCase().equals(providerName.toLowerCase()))
        .findFirst()
        .orElseThrow(() -> new IllegalStateException("Authentication provider not found: " + providerName));
  }

}
