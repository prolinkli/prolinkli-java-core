package com.prolinkli.framework.auth.service;

import java.util.Map;

import com.prolinkli.core.app.Constants.LkUserAuthenticationMethods;
import com.prolinkli.core.app.components.user.model.User;
import com.prolinkli.core.app.components.user.service.UserGetService;
import com.prolinkli.core.app.db.mapper.generated.UserOAuthAccountDbMapper;
import com.prolinkli.core.app.db.model.generated.UserOAuthAccountDb;
import com.prolinkli.core.app.db.model.generated.UserOAuthAccountDbExample;
import com.prolinkli.core.app.db.model.generated.UserOAuthAccountDbKey;
import com.prolinkli.framework.db.dao.Dao;
import com.prolinkli.framework.db.dao.DaoFactory;
import com.prolinkli.framework.exception.exceptions.model.ResourceNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Microsoft OAuth2 service for managing Microsoft OAuth account relationships.
 * 
 * @documentation-pr-rule.mdc
 * 
 * This service provides functionality for managing the relationship between
 * Microsoft OAuth2 accounts and internal user accounts. It handles:
 * - Looking up users by Microsoft OAuth ID
 * - Creating OAuth account relationships for new users
 * - Managing the database storage of OAuth account links
 * 
 * The service integrates with the existing user management system to provide
 * seamless OAuth2 authentication while maintaining data consistency and
 * proper relationship management.
 * 
 * @author ProLinkLi Development Team
 * @since 1.0
 */
@Service
public class MicrosoftOAuth2Service {

  @Autowired
  private UserGetService userGetService;

  private final Dao<UserOAuthAccountDb, UserOAuthAccountDbKey> dao;

  @Autowired
  public MicrosoftOAuth2Service(DaoFactory daoFactory) {
    this.dao = daoFactory.getDao(UserOAuthAccountDb.class, UserOAuthAccountDbKey.class);
  }

  /**
   * Retrieves a user by their Microsoft OAuth ID.
   * 
   * @param oAuthId Microsoft OAuth user ID
   * @return User object if found
   * @throws IllegalArgumentException if OAuth ID is null or empty
   * @throws ResourceNotFoundException if no user is found with the given OAuth ID
   * 
   * @documentation-pr-rule.mdc
   * This method performs a database lookup to find the internal user account
   * associated with a Microsoft OAuth ID. It queries the user_oauth_accounts
   * table to find the relationship and then retrieves the corresponding user
   * information.
   * 
   * The method includes proper validation and error handling to ensure
   * reliable user lookup operations.
   */
  public User getUserByOAuthId(String oAuthId) {

    if (oAuthId == null || oAuthId.isEmpty()) {
      throw new IllegalArgumentException("OAuth ID cannot be null or empty");
    }

    UserOAuthAccountDbExample example = new UserOAuthAccountDbExample();
    example.createCriteria()
        .andOauthProviderEqualTo(LkUserAuthenticationMethods.MICROSOFT_OAUTH2)
        .andOauthUserIdEqualTo(oAuthId);

    UserOAuthAccountDb userOAuthAccountDb = dao.select(example).stream()
        .findFirst()
        .orElseThrow(() -> new ResourceNotFoundException("User not found with OAuth ID: " + oAuthId));

    return userGetService.getUserById(userOAuthAccountDb.getUserId());

  }

  /**
   * Creates a relationship between a user and their Microsoft OAuth account.
   * 
   * @param params Map containing oAuthId and userId parameters
   * @throws IllegalArgumentException if parameters are invalid or missing
   * @throws RuntimeException if database operation fails
   * 
   * @documentation-pr-rule.mdc
   * This method creates a database record linking a Microsoft OAuth ID to
   * an internal user account. This relationship is used for future
   * authentication lookups and enables users to sign in using their
   * Microsoft account.
   * 
   * The method validates all required parameters and ensures the relationship
   * is properly stored in the database for reliable authentication flows.
   */
  public void insertUserOAuthRelationship(Map<String, Object> params) {

    if (params == null || params.isEmpty()) {
      throw new IllegalArgumentException("Parameters cannot be null or empty");
    }

    String oAuthId = (String) params.get("oAuthId");
    Long userId = (Long) params.get("userId");

    if (oAuthId == null || oAuthId.isEmpty()) {
      throw new IllegalArgumentException("OAuth ID cannot be null or empty");
    }
    if (userId == null) {
      throw new IllegalArgumentException("User ID cannot be null");
    }

    UserOAuthAccountDb userOAuthAccountDb = new UserOAuthAccountDb();
    userOAuthAccountDb.setOauthProvider(LkUserAuthenticationMethods.MICROSOFT_OAUTH2);
    userOAuthAccountDb.setOauthUserId(oAuthId);
    userOAuthAccountDb.setUserId(userId);

    dao.insert(userOAuthAccountDb);

  }
} 