package com.prolinkli.framework.cookies.util;

import java.util.List;
import java.util.stream.Collectors;

import com.prolinkli.core.app.Constants.Cookies;
import com.prolinkli.framework.config.secrets.SecretsManager;
import com.prolinkli.framework.jwt.model.AuthToken;

import jakarta.servlet.http.Cookie;

public class JwtCookieUtil {

  public static List<Cookie> createAuthCookies(AuthToken authToken) {
    if (authToken == null) {
      throw new IllegalArgumentException("AuthToken cannot be null");
    }

    Cookie accessTokenCookie = new Cookie(Cookies.Authentication.ACCESS_TOKEN, authToken.getAccessToken());
    Cookie refreshTokenCookie = new Cookie(Cookies.Authentication.REFRESH_TOKEN, authToken.getRefreshToken());
    Cookie userIdCookie = new Cookie(Cookies.Authentication.USER_ID, authToken.getId().toString());

    List<Cookie> cookies = List.of(accessTokenCookie, refreshTokenCookie, userIdCookie);
    cookies.forEach(cookie -> {
      cookie.setPath("/");
      cookie.setHttpOnly(true); // Set HttpOnly to prevent client-side access
      cookie.setSecure(true); // Set Secure if using HTTPS
    });

    return cookies;
  }

  public static List<Cookie> setMaxAgeForAuthCookies(List<Cookie> cookies, SecretsManager secretsManager) {
    if (cookies == null || cookies.isEmpty()) {
      throw new IllegalArgumentException("Cookies list cannot be null or empty");
    }

    //TODO: make this more configurable for each field
    cookies.forEach(cookie -> cookie.setMaxAge(secretsManager.getJwtExpirationHours()));

    return cookies;
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
