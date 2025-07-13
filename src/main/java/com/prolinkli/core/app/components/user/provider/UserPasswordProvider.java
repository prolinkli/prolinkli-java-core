package com.prolinkli.core.app.components.user.provider;

import com.prolinkli.core.app.components.user.model.UserPassword;
import com.prolinkli.core.app.db.model.generated.UserPasswordDb;
import com.prolinkli.framework.abstractprovider.AbstractProvider;

public class UserPasswordProvider extends AbstractProvider<UserPasswordDb, UserPassword> {
    @Override
    public void defineMap(AbstractProvider<UserPasswordDb, UserPassword>.ClassProviderBuilder mapper) {
        // Map passwordHash <-> password, userId is handled separately
        mapper.field("passwordHash", "password");
    }
} 