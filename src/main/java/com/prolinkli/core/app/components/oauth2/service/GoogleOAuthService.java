package com.prolinkli.core.app.components.oauth2.service;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeRequestUrl;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeTokenRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.prolinkli.core.app.Constants.OAuth2Providers;
import com.prolinkli.core.app.Constants.OAuth2ProvidersLks;
import com.prolinkli.framework.config.secrets.SecretsManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletRequest;

@Service
public class GoogleOAuthService extends AbstractOAuthService {

  private static final String REDIRECT_URL = "http://localhost:8080/v1/api/auth/oauth2/google/callback";
  private static final String LOGIN_ENDPOINT = "/v1/api/buildinfo";

  @Autowired
  private SecretsManager secretsManager;

  @Override
  public String getProviderName() {
    return OAuth2ProvidersLks.GOOGLE;
  }

  @Override
  public String getRedirectUrl() {
    final String clientId = secretsManager.getGoogleClientId();
    String redirectUrl = new GoogleAuthorizationCodeRequestUrl(
        clientId,
        REDIRECT_URL,
        List.of("email", "profile", "openid")).build();

    return redirectUrl;
  }

  @Override
  public String handleCallback(Map<String, Object> params, HttpServletRequest request) {

    if (!params.containsKey(OAuth2Providers.Google.CODE_KEY)) {
      throw new IllegalArgumentException("Missing code parameter in callback");
    }

    String authorizationCode = (String) params.get(OAuth2Providers.Google.CODE_KEY);

    try {
      // Exchange authorization code for ID token
      String idToken = exchangeCodeForIdToken(authorizationCode);

      // Store the ID token in request attributes for the next step
      request.setAttribute("idToken", idToken);

      if (params.containsKey(OAuth2Providers.Google.STATE_KEY)) {
        request.setAttribute(
            OAuth2Providers.Google.STATE_KEY,
            params.getOrDefault(OAuth2Providers.Google.STATE_KEY, null));
      }

      // TODO: Change into proper redirect URL without hardcoding
      return LOGIN_ENDPOINT;

    } catch (IOException e) {
      throw new RuntimeException("Failed to exchange authorization code for ID token", e);
    }
  }

  /**
   * Exchanges the authorization code for an ID token using Google's token
   * endpoint
   */
  private String exchangeCodeForIdToken(String authorizationCode) throws IOException {
    try {
      GoogleTokenResponse tokenResponse = new GoogleAuthorizationCodeTokenRequest(
          new NetHttpTransport(),
          new GsonFactory(),
          secretsManager.getGoogleClientId(),
          secretsManager.getGoogleClientSecret(),
          authorizationCode,
          // TODO: Change into proper redirect URL without hardcoding
          REDIRECT_URL)
          .execute();

      String idToken = tokenResponse.getIdToken();
      if (idToken == null || idToken.isEmpty()) {
        throw new RuntimeException("Google token response did not contain an ID token");
      }

      return idToken;

    } catch (IOException e) {
      throw new RuntimeException("Failed to exchange authorization code for ID token: " + e.getMessage(), e);
    }
  }

}
