package com.prolinkli.core.app.components.user.provider;

import com.prolinkli.core.app.components.user.model.User;
import com.prolinkli.core.app.db.model.generated.UserDb;
import com.prolinkli.framework.abstractprovider.AbstractProvider;

public class UserProvider extends AbstractProvider<UserDb, User> {

  @Override
  public void defineMap(AbstractProvider<UserDb, User>.ClassProviderBuilder mapper) {
    mapper.field("id", "id")
          .field("username", "username");
  }


  
}
