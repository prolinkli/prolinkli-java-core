package com.prolinkli.core.app.components.user.controller;

import com.prolinkli.core.app.Constants.Cookies;
import com.prolinkli.core.app.components.user.model.AuthorizedUser;
import com.prolinkli.core.app.components.user.model.User;
import com.prolinkli.core.app.components.user.model.UserAuthenticationForm;
import com.prolinkli.core.app.components.user.service.UserAuthService;
import com.prolinkli.framework.auth.model.CurrentUser;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/user")
class UserController {

  @Autowired
  UserAuthService userAuthService;

  @PostMapping("/login")
  public AuthorizedUser login(@RequestBody UserAuthenticationForm item, HttpServletResponse response) {
    AuthorizedUser user = userAuthService.login(item);

    // Create cookies with proper path settings
    Cookie accessTokenCookie = new Cookie(Cookies.Authentication.ACCESS_TOKEN, user.getAuthToken().getAccessToken());
    accessTokenCookie.setPath("/"); // Make available to all paths
    accessTokenCookie.setHttpOnly(true); // Security: prevent XSS access

    Cookie refreshTokenCookie = new Cookie(Cookies.Authentication.REFRESH_TOKEN, user.getAuthToken().getRefreshToken());
    refreshTokenCookie.setPath("/");
    refreshTokenCookie.setHttpOnly(true);

    Cookie userIdCookie = new Cookie(Cookies.Authentication.USER_ID, user.getId().toString());
    userIdCookie.setPath("/");

    response.addCookie(accessTokenCookie);
    response.addCookie(refreshTokenCookie);
    response.addCookie(userIdCookie);

    return user;
  }

  @GetMapping("/")
  public AuthorizedUser refresh(@CurrentUser AuthorizedUser user) {
    if (user == null) {
      throw new IllegalStateException("User not authenticated");
    }
    return user;
  }

  @PreAuthorize("isAuthenticated()")
  @GetMapping("/me")
  public User getCurrentUser(@CurrentUser User user) {
    if (user == null) {
      throw new IllegalStateException("User not authenticated");
    }
    return user;
  }

}
