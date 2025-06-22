package com.prolinkli.framework.cookies.util;

import java.util.List;

import com.prolinkli.core.app.Constants.Cookies;
import com.prolinkli.framework.jwt.model.AuthToken;

import jakarta.servlet.http.Cookie;

public class JwtCookieUtil {

  public static List<Cookie> createAuthCookies(AuthToken authToken) {
    if (authToken == null) {
      throw new IllegalArgumentException("AuthToken cannot be null");
    }

    Cookie accessTokenCookie = new Cookie(Cookies.Authentication.ACCESS_TOKEN, authToken.getAccessToken());
    accessTokenCookie.setPath("/"); // Make available to all paths
    accessTokenCookie.setHttpOnly(true); // Security: prevent XSS access

    Cookie refreshTokenCookie = new Cookie(Cookies.Authentication.REFRESH_TOKEN, authToken.getRefreshToken());
    refreshTokenCookie.setPath("/");
    refreshTokenCookie.setHttpOnly(true);

    Cookie userIdCookie = new Cookie(Cookies.Authentication.USER_ID, authToken.getId().toString());
    userIdCookie.setPath("/");

    return List.of(accessTokenCookie, refreshTokenCookie, userIdCookie);
  }

}
