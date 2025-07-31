package com.prolinkli.framework.jwt.service;

import java.util.Map;

import com.prolinkli.core.app.Constants.Jwt;
import com.prolinkli.core.app.components.user.model.AuthorizedUser;
import com.prolinkli.core.app.components.user.model.User;
import com.prolinkli.core.app.db.model.generated.JwtTokenDb;
import com.prolinkli.framework.db.dao.Dao;
import com.prolinkli.framework.db.dao.DaoFactory;
import com.prolinkli.framework.hash.Hasher;
import com.prolinkli.framework.jwt.model.AuthToken;
import com.prolinkli.framework.jwt.model.AuthToken.AuthTokenBuilder;
import com.prolinkli.framework.jwt.provider.AuthTokenProvider;
import com.prolinkli.framework.jwt.util.JwtUtil;
import com.prolinkli.framework.util.map.MapUtil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.prolinkli.framework.config.secrets.SecretsManager;

import io.jsonwebtoken.Jwts;

@Service
public class JwtCreateService {

  private final SecretsManager secretsManager;

  @Value("${jwt.expiration:3600}")
  private long jwtExpiration;

  @Value("${jwt.refreshExpiration:7200}")
  private long jwtRefreshExpiration;

  private Dao<JwtTokenDb, Long> dao;

  private final AuthTokenProvider authTokenProvider = new AuthTokenProvider();

  @Autowired
  public JwtCreateService(DaoFactory daoFactory, SecretsManager secretsManager) {
    this.dao = daoFactory.getDao(JwtTokenDb.class, Long.class);
    this.secretsManager = secretsManager;
  }

  // need to have a method to create JWT token and store it in the database for
  // user authentication

  public AuthToken createJwtToken(Map<String, Object> claims) {
    String accessToken = createToken(claims, jwtExpiration);
    String refreshToken = createToken(claims, jwtRefreshExpiration);

    return new AuthTokenBuilder().accessToken(accessToken)
        .refreshToken(refreshToken).build();
  }

  public AuthorizedUser createJwtTokenForUser(User user, Map<String, Object> claims) {
    // Here you would typically generate a JWT token using a library like
    // jjwt or similar, signing it with a secret key and including the user details
    // in the claims.

    // This token secret is used to *identify* the token and should be securely stored
    // It is cross-referenced in the database to validate the token and make sure 
    // that it can be revoked if needed.
    String tokenSecret = Hasher.generateRandomHash(); // Generate a random token secret

    // TODO: add here as needed
    Map<String, Object> userClaims = Map.of(
        Jwt.USER_ID_CLAIMS_KEY, user.getId(),
        Jwt.USERNAME_CLAIMS_KEY, user.getUsername(),
        Jwt.SECRET_CLAIMS_KEY, tokenSecret // Include user secret if needed
    );

    // Merge user claims with any additional claims
    Map<String, Object> finalClaims = MapUtil.merge(userClaims, claims);

    AuthToken jwtTokens = createJwtToken(finalClaims);
    jwtTokens.setId(user.getId());

    JwtTokenDb jwtTokenDb = authTokenProvider.map(jwtTokens);
    jwtTokenDb.setExpiresAt(JwtUtil.getExpirationDate(jwtExpiration));
    jwtTokenDb.setTokenSecret(tokenSecret);

    // Save the JWT token in the database
    dao.insert(jwtTokenDb);

    AuthorizedUser authorizedUser = new AuthorizedUser(user, jwtTokens);

    return authorizedUser;
  }

  private String createToken(Map<String, Object> claims, Long expiration) {
    return Jwts.builder()
        .claims()
        .issuer(secretsManager.getJwtIssuer()) // Set the issuer
        .add(claims)
        .expiration(JwtUtil.getExpirationDate(expiration)) // Set expiration
        .and()
        // Set expiration, signing key, etc. as needed
        .signWith(JwtUtil.getHmacShaKey(secretsManager.getJwtSecret()))
        .compact();
  }

}
