package com.prolinkli.core.app.components.oauth2.service;

import java.util.Map;

import com.prolinkli.framework.config.secrets.SecretsManager;

import org.springframework.beans.factory.annotation.Autowired;

import jakarta.servlet.http.HttpServletRequest;

public abstract class AbstractOAuthService {

  @Autowired
  protected SecretsManager secretsManager;

  public abstract String getProviderName();

  public abstract String getRedirectUrl();

  public abstract String handleCallback(Map<String, Object> params, HttpServletRequest request);

}
