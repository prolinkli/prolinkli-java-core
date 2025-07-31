package com.prolinkli.framework.jwt.provider;

import com.prolinkli.core.app.db.model.generated.JwtTokenDb;
import com.prolinkli.framework.abstractprovider.AbstractProvider;
import com.prolinkli.framework.jwt.model.TokenSecret;

public class TokenSecretProvider extends AbstractProvider<JwtTokenDb, TokenSecret> {

    @Override
    public void defineMap(AbstractProvider<JwtTokenDb, TokenSecret>.ClassProviderBuilder mapper) {
      mapper.field("userId", "userId")
            .field("tokenSecret", "tokenSecret");
    }
  
}
