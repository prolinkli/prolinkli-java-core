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

  public static List<Cookie> createClearAuthCookies() {
    // Create cookies with empty values and maxAge=0 to clear them
    Cookie accessTokenCookie = new Cookie(Cookies.Authentication.ACCESS_TOKEN, "");
    accessTokenCookie.setPath("/");
    accessTokenCookie.setHttpOnly(true);
    accessTokenCookie.setMaxAge(0); // This clears the cookie

    Cookie refreshTokenCookie = new Cookie(Cookies.Authentication.REFRESH_TOKEN, "");
    refreshTokenCookie.setPath("/");
    refreshTokenCookie.setHttpOnly(true);
    refreshTokenCookie.setMaxAge(0);

    Cookie userIdCookie = new Cookie(Cookies.Authentication.USER_ID, "");
    userIdCookie.setPath("/");
    userIdCookie.setHttpOnly(true);
    userIdCookie.setMaxAge(0);

    return List.of(accessTokenCookie, refreshTokenCookie, userIdCookie);
  }

}
