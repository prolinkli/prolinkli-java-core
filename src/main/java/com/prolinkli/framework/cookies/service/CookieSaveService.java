package com.prolinkli.framework.cookies.service;

import java.util.List;

import org.springframework.stereotype.Service;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

@Service
public class CookieSaveService {

  public void saveCookies(List<Cookie> cookies, HttpServletResponse response) {
    if (cookies == null || cookies.size() == 0) {
      throw new IllegalArgumentException("No cookies to save");
    }

    for (Cookie cookie : cookies) {
      if (cookie == null || cookie.getName() == null || cookie.getValue() == null) {
        throw new IllegalArgumentException("Invalid cookie: " + cookie);
      }
      response.addCookie(cookie);
    }
  }

  public void saveCookie(Cookie cookie, HttpServletResponse response) {
    if (cookie == null || cookie.getName() == null || cookie.getValue() == null) {
      throw new IllegalArgumentException("Invalid cookie: " + cookie);
    }
    response.addCookie(cookie);
  }

}
