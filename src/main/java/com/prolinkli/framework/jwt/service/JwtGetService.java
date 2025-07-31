package com.prolinkli.framework.jwt.service;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.prolinkli.core.app.db.model.generated.JwtTokenDb;
import com.prolinkli.core.app.db.model.generated.JwtTokenDbExample;
import com.prolinkli.framework.db.dao.Dao;
import com.prolinkli.framework.db.dao.DaoFactory;
import com.prolinkli.framework.jwt.model.AuthToken;
import com.prolinkli.framework.jwt.model.TokenSecret;
import com.prolinkli.framework.jwt.provider.AuthTokenProvider;
import com.prolinkli.framework.jwt.provider.TokenSecretProvider;

import org.springframework.stereotype.Service;

@Service
public class JwtGetService {

  private final Dao<JwtTokenDb, Long> dao;

  private final TokenSecretProvider tokenSecretProvider = new TokenSecretProvider();

  public JwtGetService(DaoFactory daoFactory) {
    this.dao = daoFactory.getDao(JwtTokenDb.class, Long.class);
  }

  public Set<TokenSecret> getSecretTokenByUserId(Long userId) {
    JwtTokenDbExample example = new JwtTokenDbExample();
    example.createCriteria().andUserIdEqualTo(userId);
    List<JwtTokenDb> jwtTokens = dao.select(example);
    return jwtTokens.stream()
        .map(tokenSecretProvider::map)
        .collect(Collectors.toSet());
  }

  public Optional<TokenSecret> getSecretTokenById(Long id) {

    JwtTokenDb jwtToken = dao.select(id);

    if (jwtToken == null) {
      return Optional.empty();
    }

    return Optional.of(tokenSecretProvider.map(jwtToken));
  }

}
