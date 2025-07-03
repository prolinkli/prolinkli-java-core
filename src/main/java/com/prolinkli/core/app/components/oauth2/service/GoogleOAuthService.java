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
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Google OAuth2 service implementation for handling Google OAuth2 authentication flows.
 * 
 * @documentation-pr-rule.mdc
 * 
 * This service implements Google-specific OAuth2 operations including:
 * - Authorization URL generation with Google OAuth2 endpoints
 * - Authorization code to ID token exchange using Google APIs
 * - Proper scope configuration for user profile and email access
 * 
 * Uses Google's official client libraries for secure and reliable OAuth2 integration.
 * Handles the complete OAuth2 authorization code flow as specified by Google's
 * OAuth2 documentation and RFC 6749.
 * 
 * Required environment configuration:
 * - GOOGLE_OAUTH_CLIENT_ID: Google OAuth2 client identifier
 * - GOOGLE_OAUTH_CLIENT_SECRET: Google OAuth2 client secret
 * 
 * Scopes requested:
 * - email: Access to user's email address
 * - profile: Access to basic profile information
 * - openid: OpenID Connect compatibility
 * 
 * @author ProLinkLi Development Team
 * @since 1.0
 */
@Service
public class GoogleOAuthService extends AbstractOAuthService {

  /**
   * Google OAuth2 callback URL for authorization code reception.
   * TODO: Make this configurable via environment variables instead of hardcoding.
   */
  private static final String REDIRECT_URL = "http://localhost:8080/v1/api/auth/oauth2/google/callback";
  
  /**
   * Default endpoint to redirect to after successful token exchange.
   * TODO: Replace with proper application redirect URL configuration.
   */
  private static final String LOGIN_ENDPOINT = "/v1/api/buildinfo";

  /**
   * Returns the Google OAuth2 provider identifier.
   * 
   * @return "google" as defined in OAuth2ProvidersLks.GOOGLE
   * 
   * @documentation-pr-rule.mdc
   * This identifier is used for URL routing and service registration.
   * Must match the constant defined in OAuth2ProvidersLks for consistency.
   */
  @Override
  public String getProviderName() {
    return OAuth2ProvidersLks.GOOGLE;
  }

  /**
   * Generates Google OAuth2 authorization URL for user consent.
   * 
   * @return Complete Google OAuth2 authorization URL with client credentials and scopes
   * 
   * @documentation-pr-rule.mdc
   * Creates a Google OAuth2 authorization URL that includes:
   * - Client ID from SecretsManager configuration
   * - Redirect URI for callback handling
   * - Requested scopes: email, profile, openid
   * - Proper OAuth2 response_type=code parameter
   * 
   * Users are redirected to this URL to grant consent for the requested permissions.
   */
  @Override
  public String getRedirectUrl() {
    final String clientId = secretsManager.getGoogleClientId();
    String redirectUrl = new GoogleAuthorizationCodeRequestUrl(
        clientId,
        REDIRECT_URL,
        List.of("email", "profile", "openid")).build();

    return redirectUrl;
  }

  /**
   * Handles Google OAuth2 callback and exchanges authorization code for ID token.
   * 
   * @param params Map containing OAuth2 callback parameters including authorization code
   * @param request HttpServletRequest for storing ID token and state in attributes
   * @return String endpoint URL to redirect to after successful token exchange
   * @throws IllegalArgumentException if authorization code is missing from callback
   * @throws RuntimeException if token exchange fails due to network or API errors
   * 
   * @documentation-pr-rule.mdc
   * This method processes Google's OAuth2 callback by:
   * 1. Validating the presence of authorization code parameter
   * 2. Exchanging the code for Google ID token using Google's token endpoint
   * 3. Storing the ID token in request attributes for downstream processing
   * 4. Preserving state parameter if present for CSRF protection
   * 5. Returning the next endpoint in the authentication flow
   * 
   * The exchanged ID token contains user profile information and is used
   * for authentication and user account creation/linking.
   */
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
   * Exchanges Google authorization code for ID token using Google's token endpoint.
   * 
   * @param authorizationCode Authorization code received from Google OAuth2 callback
   * @return String ID token containing user profile and authentication information
   * @throws IOException if network communication with Google's token endpoint fails
   * @throws RuntimeException if token response is invalid or missing ID token
   * 
   * @documentation-pr-rule.mdc
   * This method performs the OAuth2 authorization code grant flow by:
   * 1. Creating a token request with Google's API client library
   * 2. Including client credentials from SecretsManager
   * 3. Specifying the redirect URI used in the authorization request
   * 4. Executing the request against Google's token endpoint
   * 5. Validating the response contains a valid ID token
   * 
   * The returned ID token is a JWT that contains user profile information
   * and can be verified using Google's public keys for authentication.
   * 
   * Uses Google's NetHttpTransport and GsonFactory for reliable HTTP communication
   * and JSON processing according to Google's best practices.
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
