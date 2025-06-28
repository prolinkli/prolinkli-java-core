package com.prolinkli.core.app.components.user.controller;

import com.prolinkli.core.app.components.user.model.AuthorizedUser;
import com.prolinkli.core.app.components.user.model.User;
import com.prolinkli.core.app.components.user.model.UserAuthenticationForm;
import com.prolinkli.core.app.components.user.service.UserAuthService;
import com.prolinkli.core.app.components.user.service.UserCreateService;
import com.prolinkli.framework.auth.model.CurrentUser;
import com.prolinkli.framework.cookies.service.CookieSaveService;
import com.prolinkli.framework.cookies.util.JwtCookieUtil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/user")
class UserController {

  @Autowired
  private UserAuthService userAuthService;

  @Autowired
  private UserCreateService userCreateService;

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
   * Register endpoint for user creation.
   * 
   * @param item     User authentication form containing credentials.
   * @param response HttpServletResponse to set cookies.
   * @return AuthorizedUser object containing user details and auth token.
   */
  @PostMapping("/register")
  public AuthorizedUser register(@RequestBody UserAuthenticationForm form,
      HttpServletResponse response) {

    AuthorizedUser user = userCreateService.createUser(form);

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
