package com.prolinkli.framework.jwt.service;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;

import com.prolinkli.core.app.Constants.Jwt;
import com.prolinkli.core.app.components.user.model.User;
import com.prolinkli.core.app.components.user.service.UserGetService;
import com.prolinkli.framework.config.secrets.SecretsManager;
import com.prolinkli.framework.jwt.model.AuthToken;
import com.prolinkli.framework.jwt.model.AuthTokenType;
import com.prolinkli.framework.jwt.util.JwtUtil;

import jakarta.servlet.http.HttpServletResponse;

@Service
public class JwtVerifyService {

  final private static Logger LOGGER = LoggerFactory.getLogger(JwtVerifyService.class);

  private final SecretsManager secretsManager;

  @Autowired
  private JwtGetService jwtGetService;

  @Autowired
  private UserGetService userGetService;

  @Autowired
  public JwtVerifyService(SecretsManager secretsManager) {
    this.secretsManager = secretsManager;
  }

  public boolean verifyToken(String token, HttpServletResponse response) {
    return verifyToken(token, AuthTokenType.ACCESS, response);
  }

  public boolean verifyToken(String token, AuthTokenType type, HttpServletResponse response) {
    try {
      // Implement your JWT verification logic here
      if (token == null || token.isEmpty()) {
        LOGGER.debug("JWT token is null or empty");
        return false;
      }
      LOGGER.debug("Verifying JWT token: {}", token);

      // Basic validation - token should have some structure
      if (!token.contains(".")) {
        return false; // Not a proper JWT structure
      }

      if (!isJwtTokenValid(token)) {
        LOGGER.debug("JWT token is invalid or expired: {}", token);
        return false;
      }

      // Placeholder: assume token is valid if it's not empty and has JWT structure
      // In real implementation, you'd parse and verify the JWT
      return isJwtTokenActive(token, type);

    } catch (Exception e) {
      // Log the exception in a real implementation
      //
      return false;
    }
  }

  /**
   * Extract user ID from JWT token
   * This is a placeholder method that you should implement based on your JWT
   * structure
   */
  public Long extractUserId(String token) {

    if (token == null || token.isEmpty() || !isJwtTokenValid(token)) {
      LOGGER.debug("JWT token is null or empty");
      return null;
    }

    Jws<Claims> claims = getClaims(token);

    if (claims == null || claims.getPayload() == null) {
      LOGGER.debug("JWT token claims are null or empty");
      return null;
    }
    Claims body = claims.getPayload();

    Object userId = body.get(Jwt.USER_ID_CLAIMS_KEY);
    if (userId == null) {
      LOGGER.debug("JWT token does not contain a valid user ID");
      return null;
    }

    if (Jwt.USER_ID_CLAIMS_CLASS.isInstance(userId)) {
      return ((Integer) Jwt.USER_ID_CLAIMS_CLASS.cast(userId)).longValue();
    } else {
      LOGGER.debug("Invalid user ID type in JWT token. Expected: {}, Got: {}",
          Jwt.USER_ID_CLAIMS_CLASS.getSimpleName(), userId.getClass().getSimpleName());
      return null;
    }
  }

  /**
   * //TODO: this is involving user roles/permissions
   * 
   * Extract user roles/authorities from JWT token
   * This is a placeholder method that you should implement based on your JWT
   * structure
   */
  public List<String> extractAuthorities(String token) {
    // TODO: Implement JWT parsing to extract roles/authorities from claims
    // This is a placeholder implementation
    return Arrays.asList("USER");
  }

  /**
   * Extract user information from JWT token
   * This method verifies that the user is authenticated, and is in the database.
   */
  private boolean isJwtTokenActive(String token, AuthTokenType type) {
    if (token == null || token.isEmpty() || !isJwtTokenValid(token)) {
      LOGGER.debug("JWT token is null or empty or invalid");
      return false;
    }

    Long userId = extractUserId(token);
    if (userId == null || userId <= 0) {
      LOGGER.debug("JWT token does not contain a valid user ID");
      return false;
    }

    Set<AuthToken> authtokens = jwtGetService.getJwtToken(userId);
    if (authtokens == null || authtokens.isEmpty()) {
      LOGGER.debug("No JWT tokens found for user ID: {}", userId);
      return false; // No JWT tokens found for the user
    }
    if (authtokens.stream().noneMatch(tokenDb -> {
      if (type == AuthTokenType.REFRESH) {
        return tokenDb.getRefreshToken() != null && tokenDb.getRefreshToken().equals(token);
      } else if (type == AuthTokenType.ACCESS) {
        return tokenDb.getAccessToken() != null && tokenDb.getAccessToken().equals(token);
      }
      return false; // If type is not recognized, return false
    })) {
      LOGGER.debug("JWT token does not match any stored tokens for user ID: {}", userId);
      return false; // JWT token does not match any stored tokens
    }

    // finally check if the user exists in the database
    User user = userGetService.getUserById(userId);
    if (user == null) {
      LOGGER.debug("User with ID {} does not exist in the database", userId);
      return false; // User does not exist in the database
    }

    return true; // User is authenticated and exists in the database

  }

  /**
   * Check if the JWT token is valid
   * This method can be used to validate the JWT token without extracting user
   * information.
   *
   * This is just a check to make sure the JWT token is well-formed and not empty.
   * (and not expired)
   *
   * @param token The JWT token to validate
   * @return true if the token is valid, false otherwise
   */
  private boolean isJwtTokenValid(String token) {
    // Check if the JWT token is valid
    if (token == null || token.isEmpty()) {
      return false;
    }
    try {
      Jws<Claims> claims = getClaims(token);

      if (claims == null) {
        return false; // Token parsing failed
      }
      if (claims.getPayload() == null) {
        return false; // No claims found in the token
      }

      Claims body = claims.getPayload();
      if (body.getExpiration() == null) {
        return false; // No expiration date found in the token
      }
      if (JwtUtil.didExpire(body.getExpiration())) {
        LOGGER.debug("JWT token has expired: {}", token);
        return false; // Token has expired
      }

      return true;

    } catch (Exception e) {
      LOGGER.error("Error verifying JWT token: {}", e.getMessage());
      return false;
    }
  }

  private Jws<Claims> getClaims(String token) {
    try {
      return Jwts.parser()
          .verifyWith(JwtUtil.getSecretKey(secretsManager.getJwtSecret()))
          .build()
          .parseSignedClaims(token);

    } catch (Exception e) {
      LOGGER.error("Error parsing JWT token: {}", e.getMessage());
      return null;
    }
  }
}
