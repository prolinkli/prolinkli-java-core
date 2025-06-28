package com.prolinkli.core.app.components.user.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.prolinkli.core.app.Constants.AuthenticationKeys;
import com.prolinkli.core.app.Constants.Cookies;
import com.prolinkli.core.app.Constants.LkUserAuthenticationMethods;
import com.prolinkli.core.app.components.user.model.AuthorizedUser;
import com.prolinkli.core.app.components.user.model.User;
import com.prolinkli.core.app.components.user.model.UserAuthenticationForm;
import com.prolinkli.core.app.components.user.service.UserAuthService;
import com.prolinkli.framework.auth.model.CurrentUser;
import com.prolinkli.framework.cookies.service.CookieSaveService;
import com.prolinkli.framework.cookies.util.JwtCookieUtil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/user")
class UserController {

  @Autowired
  private UserAuthService userAuthService;

  @Autowired
  private CookieSaveService cookieSaveService;

  /**
   * Login endpoint for user authentication.
   * 
   * @param item     User authentication form containing credentials.
   * @param response HttpServletResponse to set cookies.
   * @return AuthorizedUser object containing user details and auth token.
   */
  @PostMapping("/login")
  public AuthorizedUser login(@RequestBody UserAuthenticationForm item, HttpServletResponse response) {
    AuthorizedUser user = userAuthService.login(item);

    // Create cookies with proper path settings
    cookieSaveService.saveCookies(
        JwtCookieUtil.createAuthCookies(user.getAuthToken()),
        response);

    return user;
  }

  /**
   * Google OAuth2 login endpoint.
   * Frontend sends the Google ID token received from Google OAuth2 flow.
   * 
   * @param request  Map containing the Google ID token
   * @param response HttpServletResponse to set cookies.
   * @return AuthorizedUser object containing user details and auth token.
   */
  @PostMapping("/login/google")
  public AuthorizedUser loginWithGoogle(@RequestBody Map<String, String> request, HttpServletResponse response) {
    String idToken = request.get("idToken");
    if (idToken == null || idToken.trim().isEmpty()) {
      throw new IllegalArgumentException("Google ID token is required");
    }

    // Create authentication form for the service
    UserAuthenticationForm authForm = new UserAuthenticationForm();
    authForm.setAuthenticationMethodLk(LkUserAuthenticationMethods.GOOGLE_OAUTH2);
    authForm.setSpecialToken(idToken);

    // Authenticate using the existing service
    AuthorizedUser user = userAuthService.login(authForm);

    // Create cookies with proper path settings
    cookieSaveService.saveCookies(
        JwtCookieUtil.createAuthCookies(user.getAuthToken()),
        response);

    return user;
  }

  @GetMapping("/refresh")
  public AuthorizedUser refresh(@CurrentUser AuthorizedUser user, HttpServletResponse response) {
    if (user == null) {
      throw new IllegalStateException("User not authenticated");
    }

    // try to refresh the user
    AuthorizedUser newUser = userAuthService.refresh(user, response);

    // save cookies
    cookieSaveService.saveCookies(
        JwtCookieUtil.createAuthCookies(newUser.getAuthToken()),
        response);

    return newUser;
  }

  @PreAuthorize("isAuthenticated()")
  @GetMapping("/me")
  public User getCurrentUser(@CurrentUser User user) {
    if (user == null) {
      throw new IllegalStateException("User not authenticated");
    }
    return AuthorizedUser.strip(user);
  }

}
