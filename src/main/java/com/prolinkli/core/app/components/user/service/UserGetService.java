package com.prolinkli.core.app.components.user.service;

import java.math.BigInteger;

import com.prolinkli.core.app.Constants;
import com.prolinkli.core.app.components.user.model.User;
import com.prolinkli.core.app.components.user.model.UserPassword;
import com.prolinkli.core.app.components.user.provider.UserPasswordProvider;
import com.prolinkli.core.app.components.user.provider.UserProvider;
import com.prolinkli.core.app.components.user.userdetails.model.UserDetails;
import com.prolinkli.core.app.components.user.userdetails.service.UserDetailsGetService;
import com.prolinkli.core.app.db.model.generated.UserDb;
import com.prolinkli.core.app.db.model.generated.UserDbExample;
import com.prolinkli.core.app.db.model.generated.UserPasswordDb;
import com.prolinkli.framework.db.dao.Dao;
import com.prolinkli.framework.db.dao.DaoFactory;
import com.prolinkli.framework.exception.exceptions.model.ResourceNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserGetService {

  private final Dao<UserDb, Long> dao;
  private final Dao<UserPasswordDb, Long> userPasswordDao;

  private final UserDetailsGetService userDetailsGetService;

  private final UserProvider userProvider = new UserProvider();
  private final UserPasswordProvider userPasswordProvider = new UserPasswordProvider();

  @Autowired
  public UserGetService(
      DaoFactory daoFactory,
      UserDetailsGetService userDetailsGetService
    ) {
    this.dao = daoFactory.getDao(UserDb.class, Long.class);
    this.userPasswordDao = daoFactory.getDao(UserPasswordDb.class, Long.class);
    this.userDetailsGetService = userDetailsGetService;
  }

  public User getUserById(Integer userId) {
    return getUserById(userId.longValue());
  }

  public User getUserById(Long userId) {

    if (userId == null) {
      throw new IllegalArgumentException("User ID cannot be null");
    }
    if (Constants.User.STARTING_ID.compareTo(BigInteger.valueOf(userId)) > 0) {
      throw new IllegalArgumentException("User ID must be a positive integer");
    }

    UserDb userDb = dao.select(userId);
    if (userDb == null) {
      throw new ResourceNotFoundException("User not found with ID: " + userId);
    }

    var user =  userProvider.map(userDb);
    populateUser(user);

    return user;
  }

  public User getUserByUsername(String username) {

    if (username == null || username.isEmpty()) {
      throw new IllegalArgumentException("Username cannot be null or empty");
    }

    UserDbExample example = new UserDbExample();
    example.createCriteria().andUsernameEqualTo(username);
    UserDb userDb = dao.select(example).stream().findFirst().orElse(null);
    if (userDb == null) {
      throw new ResourceNotFoundException("User not found with username: " + username);
    }

    return userProvider.map(userDb);
  }

  public UserPassword getUserWithPasswordByUsername(String username) {

    if (username == null || username.isEmpty()) {
      throw new IllegalArgumentException("Username cannot be null or empty");
    }

    User user = getUserByUsername(username);
    if (user == null) {
      throw new ResourceNotFoundException("User not found with username: " + username);
    }

    UserPasswordDb userPasswordDb = userPasswordDao.select(user.getId());
    if (userPasswordDb == null) {
      throw new ResourceNotFoundException("User password not found for username: " + username);
    }

    UserPassword userPassword = new UserPassword();
    userPassword.setUser(user);
    userPassword.setPassword(userPasswordDb.getPasswordHash());

    return userPassword;
  }

  public UserPassword getUserWithPasswordById(Integer userId) {

    if (userId == null) {
      throw new IllegalArgumentException("User ID cannot be null");
    }

    if (Constants.User.STARTING_ID.compareTo(BigInteger.valueOf(userId)) > 0) {
      throw new IllegalArgumentException("User ID must be a positive integer");
    }

    User user = getUserById(userId);
    if (user == null) {
      throw new ResourceNotFoundException("User not found with ID: " + userId);
    }

    UserPasswordDb userPasswordDb = userPasswordDao.select(userId.longValue());
    if (userPasswordDb == null) {
      throw new ResourceNotFoundException("User password not found for ID: " + userId);
    }

    UserPassword userPassword = new UserPassword();
    userPassword.setUser(user);
    userPassword.setPassword(userPasswordDb.getPasswordHash());

    return userPassword;
  }

  private void populateUser(User user) {

    if (user == null) {
      return;
    }

    UserDetails userDetails = userDetailsGetService.getUserDetails(user.getId());
    if (userDetails != null) {
      user.setUserDetails(userDetails);
    }

  }

}
