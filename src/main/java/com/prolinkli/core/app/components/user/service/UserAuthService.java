package com.prolinkli.core.app.components.user.service;

import java.util.HashMap;
import java.util.Map;

import com.prolinkli.core.app.Constants.AuthenticationKeys;
import com.prolinkli.core.app.Constants.LkUserAuthenticationMethods;
import com.prolinkli.core.app.components.user.model.AuthorizedUser;
import com.prolinkli.core.app.components.user.model.User;
import com.prolinkli.core.app.components.user.model.UserAuthenticationForm;
import com.prolinkli.framework.auth.AuthProviderRegistry;
import com.prolinkli.framework.exception.exceptions.model.AuthenticationFailedException;
import com.prolinkli.framework.jwt.service.JwtCreateService;
import com.prolinkli.framework.jwt.service.JwtSaveService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authorization.AuthorizationDeniedException;
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
    getCredentials(userAuthForm);
    // this method does all subsequent authentication checks (including null checks)
    if (authForm.authenticate(userAuthForm.getParameters())) {
      User user = authForm.getUserFromCredentials(userAuthForm);
      // Consider checking if user is null and handle accordingly, but this shouldn't
      // happen if the authentication method is correct
      try {
        return jwtCreateService.createJwtTokenForUser(user, userAuthForm.getParameters());
      } catch (Exception e) {
        // Handle JWT creation failure, log it, or rethrow as needed
        throw new RuntimeException("Failed to create JWT token for user: " + user.getUsername(), e);
      }
    }

    throw new AuthenticationFailedException("Authentication failed for an unknown reason");

  }

  public AuthorizedUser logout(AuthorizedUser user) {

    if (user == null) {
      throw new AuthorizationDeniedException("User not authenticated");
    }

    if (user.getAuthToken() == null) {
      throw new AuthorizationDeniedException("User authentication token is missing");
    }

    // Invalidate the JWT token or perform any necessary cleanup
    jwtSaveService.disposeTokensTransactional(user.getAuthToken());
    return user;
  }

  public AuthorizedUser refresh(AuthorizedUser user, HttpServletResponse response) {
    if (user == null) {
      throw new AuthorizationDeniedException("User not authenticated");
    }

    return jwtSaveService.regenerateTokens(user, response);
  }

  private void getCredentials(UserAuthenticationForm userAuthForm) {

    if (userAuthForm == null || userAuthForm.getAuthenticationMethodLk() == null) {
      throw new IllegalArgumentException("User authentication form and authentication method cannot be null");
    }

    if (userAuthForm.getSpecialToken() == null) {
      throw new IllegalArgumentException("Special token cannot be null");
    }

    if (LkUserAuthenticationMethods.PASSWORD.equalsIgnoreCase(userAuthForm.getAuthenticationMethodLk())) {

      if (userAuthForm.getUsername() == null) {
        throw new IllegalArgumentException("Username cannot be null");
      }

      userAuthForm.addParameter(AuthenticationKeys.PASSWORD.USERNAME, userAuthForm.getUsername());
      userAuthForm.addParameter(AuthenticationKeys.PASSWORD.PASSWORD, userAuthForm.getSpecialToken());
    }

    if (LkUserAuthenticationMethods.GOOGLE_OAUTH2.equalsIgnoreCase(userAuthForm.getAuthenticationMethodLk())) {
      userAuthForm.addParameter(AuthenticationKeys.GOOGLE_OAUTH2.ID_TOKEN, userAuthForm.getSpecialToken());
    }
  }
}
