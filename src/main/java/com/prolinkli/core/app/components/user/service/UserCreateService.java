package com.prolinkli.core.app.components.user.service;

import java.util.List;

import com.prolinkli.core.app.components.user.model.User;
import com.prolinkli.core.app.components.user.model.UserAuthenticationForm;
import com.prolinkli.framework.auth.model.AuthProvider;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserCreateService {

  private final List<AuthProvider> authProviders;

  @Autowired
  private UserGetService userGetService;

  @Autowired
  public UserCreateService(List<AuthProvider> authProviders) {
    this.authProviders = authProviders;
  }

  /**
   * Creates a new user with the provided credentials.
   * This method is used to create a new user in the system.
   *
   * @param credentials the credentials for the new user
   */
  public void createUser(User user, UserAuthenticationForm form) {

    if (user == null || form == null) {
      throw new IllegalArgumentException("User and form cannot be null");
    }

    // throw an exception if the user already exists
    if(userGetService.getUserByUsername(user.getUsername()) != null) {
      throw new IllegalArgumentException("User with username " + user.getUsername() + " already exists");
    };

    // Validate the user credentials before inserting.
    AuthProvider authProvider = getAuthProvider(form.getAuthenticationMethodLk());

  }

  private AuthProvider getAuthProvider(String providerName) {
    return authProviders.stream()
        .filter(provider -> provider.getProviderName().equals(providerName))
        .findFirst()
        .orElseThrow(() -> new IllegalStateException("Authentication provider not found: " + providerName));
  }

}
