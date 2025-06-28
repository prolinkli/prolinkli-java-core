package com.prolinkli.framework.auth.service;

import com.prolinkli.core.app.components.user.model.UserPassword;
import com.prolinkli.core.app.components.user.service.UserGetService;
import com.prolinkli.core.app.db.model.generated.UserPasswordDb;
import com.prolinkli.framework.db.dao.Dao;
import com.prolinkli.framework.db.dao.DaoFactory;
import com.prolinkli.framework.hash.Hasher;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class InternalAuthService {

  private final Dao<UserPasswordDb, Long> dao;

  @Autowired
  private UserGetService userGetService;

  @Autowired
  public InternalAuthService(DaoFactory daoFactory) {
    this.dao = daoFactory.getDao(UserPasswordDb.class, Long.class);
  }

  /**
   * Inserts credentials for a user authentication form.
   * This method is used to store credentials in the database or any other storage
   * system.
   *
   * This is effectively a "create" method for the credentials, which means it allows
   * the system to authenticate the user in the future.
   *
   * Can be used to insert credentials, or reset credentials for a user.
   *
   * @param userId      the ID of the user for whom the credentials are being inserted
   * @param credentials basic credentials depending on the provider
   */
  public void insertCredentialsForUser(Long userId, UserPassword credentials) {

    if (userId == null || credentials == null) {
      throw new IllegalArgumentException("User ID and credentials cannot be null");
    }

    // Validate that the user exists
    userGetService.getUserById(userId);

    //REPLACE BY PROVIDER
    UserPasswordDb userPasswordDb = new UserPasswordDb();
    userPasswordDb.setUserId(userId);
    userPasswordDb.setPasswordHash(Hasher.hashString(credentials.getPassword()));

    dao.insert(userPasswordDb);

  }

  
}
