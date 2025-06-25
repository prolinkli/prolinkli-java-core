package com.prolinkli.framework.jwt.service;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.prolinkli.core.app.db.model.generated.JwtTokenDb;
import com.prolinkli.core.app.db.model.generated.JwtTokenDbExample;
import com.prolinkli.framework.db.dao.Dao;
import com.prolinkli.framework.db.dao.DaoFactory;
import com.prolinkli.framework.jwt.model.AuthToken;
import com.prolinkli.framework.jwt.model.AuthToken.AuthTokenBuilder;

import org.springframework.stereotype.Service;

@Service
public class JwtGetService {

  private final Dao<JwtTokenDb, Long> dao;

  public JwtGetService(DaoFactory daoFactory) {
    this.dao = daoFactory.getDao(JwtTokenDb.class, Long.class);
  }

  public Set<AuthToken> getJwtTokenByRefreshToken(String refreshToken) {
    if (refreshToken == null || refreshToken.isEmpty()) {
      throw new IllegalArgumentException("Refresh token cannot be null or empty");
    }

    JwtTokenDbExample example = new JwtTokenDbExample();
    example.createCriteria().andRefreshTokenEqualTo(refreshToken);

    return dao.select(example).stream().map((i) -> {
      return new AuthTokenBuilder()
          .accessToken(i.getAccessToken())
          .refreshToken(i.getRefreshToken())
          .id(i.getUserId())
          .build();
    }).collect(Collectors.toSet());
  }

  public Set<AuthToken> getJwtToken(Long userId) {

    if (userId == null) {
      throw new IllegalArgumentException("User ID cannot be null");
    }

    JwtTokenDbExample example = new JwtTokenDbExample();
    example.createCriteria().andUserIdEqualTo(userId).andExpiresAtGreaterThan(Date.from(Instant.now()));

    List<JwtTokenDb> jwtTokenDb = dao.select(example);
    if (jwtTokenDb == null) {
      return null; // or throw an exception if preferred
    }

    return jwtTokenDb.stream().map(jwt -> new AuthToken.AuthTokenBuilder()
        .id(jwt.getUserId())
        .accessToken(jwt.getAccessToken())
        .refreshToken(jwt.getRefreshToken())
        .build()).collect(Collectors.toSet());
  }

}
