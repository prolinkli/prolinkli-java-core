package com.prolinkli.core.app.components.oauth2.service;

import java.util.Map;

import org.springframework.web.servlet.view.RedirectView;

import jakarta.servlet.http.HttpServletRequest;

public abstract class AbstractOAuthService {

  public abstract String getProviderName(); 

  public abstract String getRedirectUrl(); 

  public abstract String handleCallback(Map<String, Object> params, HttpServletRequest request);
  
}
