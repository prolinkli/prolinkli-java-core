package com.prolinkli.core.app.components.user.userdetails.service;

import com.prolinkli.core.app.components.user.userdetails.model.UserDetails;
import com.prolinkli.core.app.components.user.userdetails.provider.UserDetailsProvider;
import com.prolinkli.core.app.db.model.generated.UserDetailDb;
import com.prolinkli.core.app.db.model.generated.UserDetailDbExample;
import com.prolinkli.framework.db.dao.Dao;
import com.prolinkli.framework.db.dao.DaoFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserDetailsGetService {

  private final Dao<UserDetailDb, Long> userDetailsDao;
  private final UserDetailsProvider userDetailsProvider = new UserDetailsProvider();

  @Autowired
  public UserDetailsGetService(DaoFactory daoFactory) {
    this.userDetailsDao = daoFactory.getDao(UserDetailDb.class, Long.class);
  }

  public UserDetails getUserDetails(Long userId) {
    UserDetailDbExample userDetailDbExample = new UserDetailDbExample();
    userDetailDbExample.createCriteria().andUserIdEqualTo(userId);
    UserDetailDb db = userDetailsDao.select(userDetailDbExample).stream()
        .findFirst()
        .orElse(null);
    if (db == null) {
      return null;
    }

    return userDetailsProvider.map(db);
  }
  
}
