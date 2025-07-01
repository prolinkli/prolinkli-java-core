package com.prolinkli.framework.auth.service;

import java.util.Map;

import com.prolinkli.core.app.Constants.LkUserAuthenticationMethods;
import com.prolinkli.core.app.components.user.model.User;
import com.prolinkli.core.app.components.user.service.UserGetService;
import com.prolinkli.core.app.db.model.generated.UserOAuthAccountDb;
import com.prolinkli.core.app.db.model.generated.UserOAuthAccountDbExample;
import com.prolinkli.core.app.db.model.generated.UserOAuthAccountDbKey;
import com.prolinkli.framework.db.dao.Dao;
import com.prolinkli.framework.db.dao.DaoFactory;
import com.prolinkli.framework.exception.exceptions.model.ResourceNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class GoogleOAuth2Service {

  @Autowired
  private UserGetService userGetService;

  private final Dao<UserOAuthAccountDb, UserOAuthAccountDbKey> dao;

  @Autowired
  public GoogleOAuth2Service(DaoFactory daoFactory) {
    this.dao = daoFactory.getDao(UserOAuthAccountDb.class, UserOAuthAccountDbKey.class);
  }

  public User getUserByOAuthId(String oAuthId) {

    if (oAuthId == null || oAuthId.isEmpty()) {
      throw new IllegalArgumentException("OAuth ID cannot be null or empty");
    }

    UserOAuthAccountDbExample example = new UserOAuthAccountDbExample();
    example.createCriteria()
        .andOauthProviderEqualTo(LkUserAuthenticationMethods.GOOGLE_OAUTH2)
        .andOauthUserIdEqualTo(oAuthId);

    UserOAuthAccountDb userOAuthAccountDb = dao.select(example).stream()
        .findFirst()
        .orElseThrow(() -> new ResourceNotFoundException("User not found with OAuth ID: " + oAuthId));

    return userGetService.getUserById(userOAuthAccountDb.getUserId());

  }

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
    userOAuthAccountDb.setOauthProvider(LkUserAuthenticationMethods.GOOGLE_OAUTH2);
    userOAuthAccountDb.setOauthUserId(oAuthId);
    userOAuthAccountDb.setUserId(userId);

    dao.insert(userOAuthAccountDb);

  }
}
