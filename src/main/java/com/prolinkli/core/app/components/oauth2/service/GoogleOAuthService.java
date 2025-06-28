package com.prolinkli.core.app.components.oauth2.service;

import java.util.List;
import java.util.Map;

import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeRequestUrl;
import com.prolinkli.core.app.Constants.LkUserAuthenticationMethods;
import com.prolinkli.core.app.Constants.OAuth2Providers;
import com.prolinkli.core.app.Constants.OAuth2ProvidersLks;
import com.prolinkli.framework.config.secrets.SecretsManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.view.RedirectView;

import io.jsonwebtoken.lang.Arrays;
import jakarta.servlet.http.HttpServletRequest;

@Service
public class GoogleOAuthService extends AbstractOAuthService {

  @Autowired
  private SecretsManager secretsManager;

  @Override
  public String getProviderName() {
    // TODO Auto-generated method stub
    return OAuth2ProvidersLks.GOOGLE;
  }

  @Override
  public String getRedirectUrl() {
    final String clientId = secretsManager.getGoogleClientId();
    String redirectUrl = new GoogleAuthorizationCodeRequestUrl(
        clientId,
        "http://localhost:8080/v1/api/auth/oauth2/google/callback",
        List.of("email", "profile", "openid")).build();

    return redirectUrl;
  }

  @Override
  public String handleCallback(Map<String, Object> params, HttpServletRequest request) {

    if (!params.containsKey(OAuth2Providers.Google.CODE_KEY)) {
      throw new IllegalArgumentException("Missing code parameter in callback");
    }

    request.setAttribute(
        OAuth2Providers.Google.CODE_KEY,
        params.get(OAuth2Providers.Google.CODE_KEY));

    if (params.containsKey(OAuth2Providers.Google.STATE_KEY)) {
      request.setAttribute(
          OAuth2Providers.Google.STATE_KEY,
          params.getOrDefault(OAuth2Providers.Google.STATE_KEY, null));
    }

    return "http://localhost:8080/v1/api/user/login/google";

  }

}
