package com.prolinkli.framework.jwt.provider;

import com.prolinkli.core.app.db.model.generated.JwtTokenDb;
import com.prolinkli.framework.abstractprovider.AbstractProvider;
import com.prolinkli.framework.jwt.model.AuthToken;

public class AuthTokenProvider extends AbstractProvider<AuthToken, JwtTokenDb> {

    @Override
    public void defineMap(AbstractProvider<AuthToken, JwtTokenDb>.ClassProviderBuilder mapper) {
        mapper.field("id", "userId");
    }

  
}
