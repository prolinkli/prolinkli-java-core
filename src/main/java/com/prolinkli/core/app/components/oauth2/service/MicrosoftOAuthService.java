package com.prolinkli.core.app.components.oauth2.service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.prolinkli.core.app.Constants.OAuth2Providers;
import com.prolinkli.core.app.Constants.OAuth2ProvidersLks;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Microsoft OAuth2 service implementation for handling Microsoft OAuth2 authentication flows.
 * 
 * @documentation-pr-rule.mdc
 * 
 * This service implements Microsoft-specific OAuth2 operations including:
 * - Authorization URL generation with Microsoft OAuth2 endpoints
 * - Authorization code to access token exchange using Microsoft Graph API
 * - User profile retrieval from Microsoft Graph API
 * - Proper scope configuration for user profile and email access
 * 
 * Uses Microsoft's OAuth2 endpoints and Microsoft Graph API for secure and reliable
 * OAuth2 integration. Handles the complete OAuth2 authorization code flow as specified
 * by Microsoft's OAuth2 documentation and RFC 6749.
 * 
 * Required environment configuration:
 * - MICROSOFT_OAUTH_CLIENT_ID: Microsoft OAuth2 client identifier
 * - MICROSOFT_OAUTH_CLIENT_SECRET: Microsoft OAuth2 client secret
 * 
 * Scopes requested:
 * - User.Read: Access to user's basic profile information
 * - email: Access to user's email address
 * - openid: OpenID Connect compatibility
 * - profile: Access to basic profile information
 * 
 * @author ProLinkLi Development Team
 * @since 1.0
 */
@Service
public class MicrosoftOAuthService extends AbstractOAuthService {

  private static final String MICROSOFT_AUTH_URL = "https://login.microsoftonline.com/common/oauth2/v2.0/authorize";
  private static final String MICROSOFT_TOKEN_URL = "https://login.microsoftonline.com/common/oauth2/v2.0/token";
  private static final String MICROSOFT_GRAPH_API_URL = "https://graph.microsoft.com/v1.0/me";
  

  
  /**
   * Default endpoint to redirect to after successful token exchange.
   * TODO: Replace with proper application redirect URL configuration.
   */
  private static final String LOGIN_ENDPOINT = "/v1/api/buildinfo";

  private final HttpClient httpClient = HttpClient.newHttpClient();
  private final ObjectMapper objectMapper = new ObjectMapper();

  /**
   * Returns the Microsoft OAuth2 provider identifier.
   * 
   * @return "microsoft" as defined in OAuth2ProvidersLks.MICROSOFT
   * 
   * @documentation-pr-rule.mdc
   * This identifier is used for URL routing and service registration.
   * Must match the constant defined in OAuth2ProvidersLks for consistency.
   */
  @Override
  public String getProviderName() {
    return OAuth2ProvidersLks.MICROSOFT;
  }

  /**
   * Generates Microsoft OAuth2 authorization URL for user consent.
   * 
   * @return Complete Microsoft OAuth2 authorization URL with client credentials and scopes
   * 
   * @documentation-pr-rule.mdc
   * Creates a Microsoft OAuth2 authorization URL that includes:
   * - Client ID from SecretsManager configuration
   * - Redirect URI for callback handling
   * - Requested scopes: User.Read, email, openid, profile
   * - Proper OAuth2 response_type=code parameter
   * 
   * Users are redirected to this URL to grant consent for the requested permissions.
   */
  @Override
  public String getRedirectUrl() {
    final String clientId = secretsManager.getMicrosoftClientId();
    List<String> scopes = List.of("User.Read", "email", "openid", "profile");
    
    String scopeString = scopes.stream()
        .map(scope -> "https://graph.microsoft.com/" + scope)
        .collect(Collectors.joining(" "));
    
    return String.format("%s?client_id=%s&response_type=code&redirect_uri=%s&scope=%s&response_mode=query",
        MICROSOFT_AUTH_URL,
        clientId,
        secretsManager.getMicrosoftRedirectUri(),
        scopeString);
  }

  /**
   * Handles Microsoft OAuth2 callback and exchanges authorization code for access token.
   * 
   * @param params Map containing OAuth2 callback parameters including authorization code
   * @param request HttpServletRequest for storing user info and state in attributes
   * @return String endpoint URL to redirect to after successful token exchange
   * @throws IllegalArgumentException if authorization code is missing from callback
   * @throws RuntimeException if token exchange fails due to network or API errors
   * 
   * @documentation-pr-rule.mdc
   * This method processes Microsoft's OAuth2 callback by:
   * 1. Validating the presence of authorization code parameter
   * 2. Exchanging the code for Microsoft access token using Microsoft's token endpoint
   * 3. Retrieving user profile information from Microsoft Graph API
   * 4. Storing the user info in request attributes for downstream processing
   * 5. Preserving state parameter if present for CSRF protection
   * 6. Returning the next endpoint in the authentication flow
   * 
   * The retrieved user profile contains email, display name, and other profile information
   * that can be used for authentication and user account creation/linking.
   */
  @Override
  public String handleCallback(Map<String, Object> params, HttpServletRequest request) {
    if (!params.containsKey(OAuth2Providers.Microsoft.CODE_KEY)) {
      throw new IllegalArgumentException("Missing code parameter in callback");
    }

    String authorizationCode = (String) params.get(OAuth2Providers.Microsoft.CODE_KEY);

    try {
      // Exchange authorization code for access token
      String accessToken = exchangeCodeForAccessToken(authorizationCode);
      
      // Get user profile from Microsoft Graph API
      JsonNode userProfile = getUserProfile(accessToken);
      
      // Create a composite token containing user info for downstream processing
      String userInfoToken = createUserInfoToken(userProfile);
      
      // Store the user info token in request attributes for the next step
      request.setAttribute("idToken", userInfoToken);

      if (params.containsKey(OAuth2Providers.Microsoft.STATE_KEY)) {
        request.setAttribute(
            OAuth2Providers.Microsoft.STATE_KEY,
            params.getOrDefault(OAuth2Providers.Microsoft.STATE_KEY, null));
      }

      // TODO: Change into proper redirect URL without hardcoding
      return LOGIN_ENDPOINT;

    } catch (IOException | InterruptedException e) {
      throw new RuntimeException("Failed to exchange authorization code for user profile", e);
    }
  }

  /**
   * Exchanges Microsoft authorization code for access token using Microsoft's token endpoint.
   * 
   * @param authorizationCode Authorization code received from Microsoft OAuth2 callback
   * @return String access token for accessing Microsoft Graph API
   * @throws IOException if network communication with Microsoft's token endpoint fails
   * @throws InterruptedException if the HTTP request is interrupted
   * @throws RuntimeException if token response is invalid or missing access token
   * 
   * @documentation-pr-rule.mdc
   * This method performs the OAuth2 authorization code grant flow by:
   * 1. Creating a token request with Microsoft's OAuth2 token endpoint
   * 2. Including client credentials from SecretsManager
   * 3. Specifying the redirect URI used in the authorization request
   * 4. Executing the request against Microsoft's token endpoint
   * 5. Validating the response contains a valid access token
   * 
   * The returned access token is used to access Microsoft Graph API for user profile
   * information retrieval.
   */
  private String exchangeCodeForAccessToken(String authorizationCode) throws IOException, InterruptedException {
    String clientId = secretsManager.getMicrosoftClientId();
    String clientSecret = secretsManager.getMicrosoftClientSecret();
    
    String requestBody = String.format(
        "client_id=%s&client_secret=%s&code=%s&grant_type=authorization_code&redirect_uri=%s",
        clientId, clientSecret, authorizationCode, secretsManager.getMicrosoftRedirectUri());
    
    HttpRequest tokenRequest = HttpRequest.newBuilder()
        .uri(URI.create(MICROSOFT_TOKEN_URL))
        .header("Content-Type", "application/x-www-form-urlencoded")
        .POST(HttpRequest.BodyPublishers.ofString(requestBody))
        .build();
    
    HttpResponse<String> response = httpClient.send(tokenRequest, HttpResponse.BodyHandlers.ofString());
    
    if (response.statusCode() != 200) {
      throw new RuntimeException("Failed to exchange authorization code. Status: " + response.statusCode() + ", Body: " + response.body());
    }
    
    JsonNode tokenResponse = objectMapper.readTree(response.body());
    String accessToken = tokenResponse.get("access_token").asText();
    
    if (accessToken == null || accessToken.isEmpty()) {
      throw new RuntimeException("Microsoft token response did not contain an access token");
    }
    
    return accessToken;
  }

  /**
   * Retrieves user profile information from Microsoft Graph API.
   * 
   * @param accessToken Access token for Microsoft Graph API
   * @return JsonNode containing user profile information
   * @throws IOException if network communication with Microsoft Graph API fails
   * @throws InterruptedException if the HTTP request is interrupted
   * @throws RuntimeException if profile retrieval fails or response is invalid
   * 
   * @documentation-pr-rule.mdc
   * This method retrieves user profile information by:
   * 1. Creating an HTTP request to Microsoft Graph API /me endpoint
   * 2. Including the access token in the Authorization header
   * 3. Executing the request against Microsoft Graph API
   * 4. Parsing the JSON response to extract user profile information
   * 5. Validating the response contains required user information
   * 
   * The returned profile contains email, display name, user ID, and other profile
   * information that can be used for user authentication and account creation.
   */
  private JsonNode getUserProfile(String accessToken) throws IOException, InterruptedException {
    HttpRequest profileRequest = HttpRequest.newBuilder()
        .uri(URI.create(MICROSOFT_GRAPH_API_URL))
        .header("Authorization", "Bearer " + accessToken)
        .header("Content-Type", "application/json")
        .GET()
        .build();
    
    HttpResponse<String> response = httpClient.send(profileRequest, HttpResponse.BodyHandlers.ofString());
    
    if (response.statusCode() != 200) {
      throw new RuntimeException("Failed to retrieve user profile. Status: " + response.statusCode() + ", Body: " + response.body());
    }
    
    JsonNode userProfile = objectMapper.readTree(response.body());
    
    // Validate that required fields are present
    if (!userProfile.has("id") || !userProfile.has("userPrincipalName")) {
      throw new RuntimeException("Microsoft user profile is missing required fields (id or userPrincipalName)");
    }
    
    return userProfile;
  }

  /**
   * Creates a composite token containing user profile information for downstream processing.
   * 
   * @param userProfile JsonNode containing Microsoft user profile information
   * @return String composite token with user information
   * @throws RuntimeException if user profile is missing required information
   * 
   * @documentation-pr-rule.mdc
   * This method creates a composite token that contains essential user information
   * extracted from the Microsoft user profile. The token is structured to be
   * compatible with the existing authentication flow and contains:
   * - User ID (Microsoft object ID)
   * - Email address (userPrincipalName)
   * - Display name
   * - Other profile information as needed
   * 
   * This composite token is used by the authentication provider to create or
   * authenticate users in the system.
   */
  private String createUserInfoToken(JsonNode userProfile) {
    try {
      // Extract essential user information
      String userId = userProfile.get("id").asText();
      String email = userProfile.get("userPrincipalName").asText();
      String displayName = userProfile.has("displayName") ? userProfile.get("displayName").asText() : "";
      
      // Create a simple JSON structure with user info
      String userInfoJson = String.format(
          "{\"sub\":\"%s\",\"email\":\"%s\",\"name\":\"%s\",\"provider\":\"microsoft\"}",
          userId, email, displayName);
      
      // For simplicity, we'll base64 encode this as a "token"
      // In a production system, you might want to use proper JWT encoding
      return java.util.Base64.getEncoder().encodeToString(userInfoJson.getBytes());
      
    } catch (Exception e) {
      throw new RuntimeException("Failed to create user info token from Microsoft profile", e);
    }
  }
} 