package com.prolinkli.framework.jwt.util;

import java.security.Key;
import java.util.Date;

import javax.crypto.SecretKey;

import com.prolinkli.core.app.Constants;
import com.prolinkli.framework.jwt.model.AuthToken;
import com.prolinkli.framework.jwt.model.AuthToken.AuthTokenBuilder;

import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

public class JwtUtil {

  public static Key getHmacShaKey(String key) {
    return Keys.hmacShaKeyFor(key.getBytes());
  }

  public static SecretKey getSecretKey(String key) {
    return Keys.hmacShaKeyFor(key.getBytes());
  }

  public static Date getExpirationDate(long expiration) {
    return new Date(System.currentTimeMillis() + expiration * 1000);
  }

  public static boolean didExpire(Date expirationDate) {
    return expirationDate.before(new Date(System.currentTimeMillis()));
  }

  public static AuthToken extractJwtFromCookies(HttpServletRequest request) {
    Cookie[] cookies = request.getCookies();
    AuthTokenBuilder builder = new AuthTokenBuilder();
    if (cookies != null) {
      for (Cookie cookie : cookies) {
        // Look for access token cookie using the constant
        if (Constants.Cookies.Authentication.ACCESS_TOKEN.equals(cookie.getName())) {
          builder.accessToken(cookie.getValue());
        }
        if (Constants.Cookies.Authentication.REFRESH_TOKEN.equals(cookie.getName())) {
          builder.refreshToken(cookie.getValue());
        }
        if (Constants.Cookies.Authentication.USER_ID.equals(cookie.getName())) {
          try {
            builder.id(Long.parseLong(cookie.getValue()));
          } catch (NumberFormatException e) {
            // Log or handle the error if needed
          }
        }
      }
    }
    return builder.build();
  }

}
