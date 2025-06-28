package com.prolinkli.framework.auth.providers;

import java.util.Map;

import com.prolinkli.core.app.Constants.AuthenticationKeys;
import com.prolinkli.core.app.Constants.LkUserAuthenticationMethods;
import com.prolinkli.core.app.components.user.model.User;
import com.prolinkli.core.app.components.user.model.UserPassword;
import com.prolinkli.core.app.components.user.service.UserGetService;
import com.prolinkli.framework.auth.model.AuthProvider;
import com.prolinkli.framework.auth.service.InternalAuthService;
import com.prolinkli.framework.hash.Hasher;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class InternalAuthProvider implements AuthProvider {

  @Autowired
  private UserGetService userGetService;

  @Autowired
  private InternalAuthService internalAuthService;

  @Override
  public String getProviderName() {
    return LkUserAuthenticationMethods.PASSWORD;
  }

  @Override
  public Boolean authenticate(Map<String, Object> credentials) {
    this.validateCredentials(credentials);

    UserPassword user = userGetService
        .getUserWithPasswordByUsername(credentials.get(AuthenticationKeys.PASSWORD.USERNAME).toString());
    if (user == null) {
      throw new IllegalArgumentException("User not found for the provided username");
    }
    String password = credentials.get(AuthenticationKeys.PASSWORD.PASSWORD).toString();

    if (!Hasher.verifyString(password, user.getPassword())) {
      throw new IllegalArgumentException("Invalid password for the provided username");
    }

    return true;
  }

  @Override
  public void validateCredentials(Map<String, Object> credentials) {
    if (credentials == null || credentials.isEmpty()) {
      throw new IllegalArgumentException("Credentials cannot be null or empty");
    }

    final String usernameKey = AuthenticationKeys.PASSWORD.USERNAME;
    final String passwordKey = AuthenticationKeys.PASSWORD.PASSWORD;

    if (!credentials.containsKey(usernameKey)
        || !credentials.containsKey(passwordKey)) {
      throw new IllegalArgumentException("Credentials must contain username and password");
    }

    String username = (String) credentials.get(usernameKey);
    String password = (String) credentials.get(passwordKey);

    if (username == null || username.isEmpty()) {
      throw new IllegalArgumentException("Username cannot be null or empty");
    }

    if (password == null || password.isEmpty()) {
      throw new IllegalArgumentException("Password cannot be null or empty");
    }
  }

  public void insertCredentialsForUser(User user, Map<String, Object> credentials) {
    this.validateCredentials(credentials);

    String password = credentials.get(AuthenticationKeys.PASSWORD.PASSWORD).toString();

    // Here you would typically save the user and password to the database
    // For example:
    UserPassword userPassword = new UserPassword();
    userPassword.setUser(user);
    userPassword.setPassword(password);

    // Save the userPassword object to the database (not shown here)
    internalAuthService.insertCredentialsForUser(user.getId(), userPassword);

  }

}
