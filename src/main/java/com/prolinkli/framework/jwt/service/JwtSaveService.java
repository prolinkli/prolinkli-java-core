package com.prolinkli.framework.jwt.service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.prolinkli.core.app.components.user.model.AuthorizedUser;
import com.prolinkli.core.app.db.model.generated.JwtTokenDb;
import com.prolinkli.core.app.db.model.generated.JwtTokenDbExample;
import com.prolinkli.framework.db.dao.Dao;
import com.prolinkli.framework.db.dao.DaoFactory;
import com.prolinkli.framework.exception.exceptions.model.AuthenticationFailedException;
import com.prolinkli.framework.jwt.model.AuthToken;
import com.prolinkli.framework.jwt.model.AuthTokenType;
import com.prolinkli.framework.jwt.model.TokenSecret;

import org.apache.ibatis.exceptions.PersistenceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.servlet.http.HttpServletResponse;

@Service
public class JwtSaveService {

  private static final Logger LOGGER = LoggerFactory.getLogger(JwtSaveService.class);

  @Autowired
  private JwtVerifyService jwtVerifyService;

  @Autowired
  private JwtCreateService jwtCreateService;

  private final Dao<JwtTokenDb, String> dao;

  @Autowired
  public JwtSaveService(DaoFactory daoFactory) {
    this.dao = daoFactory.getDao(JwtTokenDb.class, String.class);
  }

  /**
   * Regenerates the JWT token and saves it in the database.
   */
  public AuthorizedUser regenerateTokens(AuthorizedUser authorizedUser, HttpServletResponse response) {

    if (authorizedUser == null || authorizedUser.getId() == null) {
      throw new IllegalArgumentException("Authorized user or user ID cannot be null");
    }

    AuthToken authToken = authorizedUser.getAuthToken();

    // Verify the existing token
    if (!jwtVerifyService.verifyToken(authToken.getRefreshToken(), AuthTokenType.REFRESH, response)) {
      // TODO: Handle the case where the token is invalid
      throw new AuthenticationFailedException("Refresh token is invalid or expired");
    }

    // Get the secret token for the user
    var secretToken = jwtVerifyService.extractTokenSecret(authToken.getAccessToken());
    if (secretToken == null) {
      throw new AuthenticationFailedException("Access token is invalid or expired");
    }

    disposeTokens(
        TokenSecret
            .builder()
            .tokenSecret(secretToken)
            .userId(authorizedUser.getId())
            .build());

    // Create a new token
    AuthorizedUser newUser = jwtCreateService.createJwtTokenForUser(authorizedUser, Map.of());

    return newUser;
  }

  @Transactional(readOnly = false)
  public void disposeTokensTransactional(TokenSecret... tokens) {
    if (tokens == null || tokens.length == 0) {
      throw new IllegalArgumentException("Tokens cannot be null or empty");
    }

    try {
      disposeTokens(tokens);
    } catch (PersistenceException e) {
      // Log the error and rethrow it to trigger a rollback
      LOGGER.error("Error disposing tokens: {}", e.getMessage(), e);
      throw e; // This will trigger a rollback due to @Transactional
    }

  }

  @Transactional(readOnly = false)
  public void disposeTokensTransactional(List<TokenSecret> tokens) {

    if (tokens == null || tokens.isEmpty()) {
      throw new IllegalArgumentException("Tokens cannot be null or empty");
    }

    try {
      disposeTokens(tokens.toArray(new TokenSecret[0]));
    } catch (PersistenceException e) {
      // Log the error and rethrow it to trigger a rollback
      LOGGER.error("Error disposing tokens: {}", e.getMessage(), e);
      throw e; // This will trigger a rollback due to @Transactional
    }

  }

  /**
   * Disposes of the provided JWT tokens by removing them from the database.
   */

  @Transactional(rollbackFor = PersistenceException.class)
  private void disposeTokens(TokenSecret... tokens) {
    if (tokens == null || tokens.length == 0) {
      throw new IllegalArgumentException("Tokens cannot be null or empty");
    }

    JwtTokenDbExample example = new JwtTokenDbExample();
    example.createCriteria().andTokenSecretIn(
        Arrays.stream(tokens).map(TokenSecret::getTokenSecret).collect(Collectors.toList()));

    dao.delete(example);

  }

}
