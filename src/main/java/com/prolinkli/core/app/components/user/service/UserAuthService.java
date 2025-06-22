package com.prolinkli.core.app.components.user.service;

import java.util.HashMap;
import java.util.Map;

import com.prolinkli.core.app.Constants.AuthenticationKeys;
import com.prolinkli.core.app.Constants.LkUserAuthenticationMethods;
import com.prolinkli.core.app.components.user.model.AuthorizedUser;
import com.prolinkli.core.app.components.user.model.User;
import com.prolinkli.core.app.components.user.model.UserAuthenticationForm;
import com.prolinkli.framework.auth.AuthProviderRegistry;
import com.prolinkli.framework.jwt.service.JwtCreateService;
import com.prolinkli.framework.jwt.service.JwtSaveService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletResponse;

@Service
public class UserAuthService {

  private final AuthProviderRegistry authProviderRegistry;
  private final UserGetService userGetService;
  private final JwtCreateService jwtCreateService;
  private final JwtSaveService jwtSaveService;

  @Autowired
  UserAuthService(
      AuthProviderRegistry authProviderRegistry,
      UserGetService userGetService,
      JwtCreateService jwtCreateService,
      JwtSaveService jwtSaveService) {
    this.authProviderRegistry = authProviderRegistry;
    this.userGetService = userGetService;
    this.jwtCreateService = jwtCreateService;
    this.jwtSaveService = jwtSaveService;
  }

  public AuthorizedUser login(UserAuthenticationForm userAuthForm) {

    if (userAuthForm == null || userAuthForm.getAuthenticationMethodLk() == null) {
      throw new IllegalArgumentException("User authentication form and authentication method cannot be null");
    }

    var authForm = this.authProviderRegistry.getProvider(userAuthForm.getAuthenticationMethodLk());
    // this method does all subsequent authentication checks (including null checks)
    if (authForm.authenticate(getCredentials(userAuthForm))) {
      User user = userGetService.getUserByUsername(userAuthForm.getUsername());
      // Consider checking if user is null and handle accordingly, but this shouldn't
      // happen if the authentication method is correct
      try {
        return jwtCreateService.createJwtTokenForUser(user, getCredentials(userAuthForm));
      } catch (Exception e) {
        // Handle JWT creation failure, log it, or rethrow as needed
        throw new RuntimeException("Failed to create JWT token for user: " + user.getUsername(), e);
      }
    }

    // TODO: throw new exception when implemented
    return null;
  }

  public AuthorizedUser refresh(AuthorizedUser user, HttpServletResponse response) {
    if (user == null) {
      throw new IllegalStateException("User not authenticated");
    }

    return jwtSaveService.regenerateTokens(user, response);
  }

  private Map<String, Object> getCredentials(UserAuthenticationForm userAuthForm) {
    if (userAuthForm == null || userAuthForm.getAuthenticationMethodLk() == null) {
      throw new IllegalArgumentException("User authentication form and authentication method cannot be null");
    }

    Map<String, Object> credentials = new HashMap<String, Object>();

    if (LkUserAuthenticationMethods.PASSWORD.equals(userAuthForm.getAuthenticationMethodLk())) {
      credentials.put(AuthenticationKeys.PASSWORD.USERNAME, userAuthForm.getUsername());
      credentials.put(AuthenticationKeys.PASSWORD.PASSWORD, userAuthForm.getSpecialToken());
      return credentials;
    }

    throw new IllegalArgumentException(
        "Unsupported authentication method: " + userAuthForm.getAuthenticationMethodLk());
  }
}
