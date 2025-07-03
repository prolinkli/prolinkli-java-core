package com.prolinkli.core.app.components.oauth2.service;

import java.util.Map;

import com.prolinkli.framework.config.secrets.SecretsManager;

import org.springframework.beans.factory.annotation.Autowired;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Abstract base class for OAuth2 service implementations.
 * 
 * @documentation-pr-rule.mdc
 * 
 * This abstract class provides the foundation for OAuth2 provider integrations.
 * Each OAuth2 provider (Google, Facebook, Microsoft, etc.) must extend this class
 * and implement the required abstract methods for provider-specific behavior.
 * 
 * Common functionality provided:
 * - Access to SecretsManager for OAuth2 credentials
 * - Standardized method signatures for OAuth2 operations
 * - Integration with Spring dependency injection
 * 
 * Implementing classes must provide:
 * - Provider name identification
 * - Authorization URL generation
 * - Callback handling and token exchange
 * 
 * @author ProLinkLi Development Team
 * @since 1.0
 */
public abstract class AbstractOAuthService {

  /**
   * Secrets manager for accessing OAuth2 client credentials.
   * Provides secure access to client IDs, client secrets, and redirect URIs
   * configured in environment variables.
   */
  @Autowired
  protected SecretsManager secretsManager;

  /**
   * Returns the unique identifier for this OAuth2 provider.
   * 
   * @return String identifier for the OAuth2 provider (e.g., "google", "facebook")
   * 
   * @documentation-pr-rule.mdc
   * This identifier is used for:
   * - URL routing in OAuth2Controller
   * - Service lookup and registration
   * - Provider-specific configuration
   * 
   * Must match the provider constants defined in OAuth2ProvidersLks.
   */
  public abstract String getProviderName();

  /**
   * Generates the OAuth2 authorization URL for user redirection.
   * 
   * @return Complete authorization URL with client credentials, scopes, and redirect URI
   * 
   * @documentation-pr-rule.mdc
   * This method constructs the provider-specific authorization URL that users
   * are redirected to for OAuth2 consent. The URL includes:
   * - Client ID from SecretsManager
   * - Appropriate scopes for the provider
   * - Redirect URI for callback handling
   * - Any provider-specific parameters (state, response_type, etc.)
   */
  public abstract String getRedirectUrl();

  /**
   * Handles OAuth2 callback and exchanges authorization code for tokens.
   * 
   * @param params Map containing callback parameters (code, state, etc.)
   * @param request HttpServletRequest for storing attributes and session data
   * @return String URL or endpoint to redirect to after successful token exchange
   * 
   * @documentation-pr-rule.mdc
   * This method processes the OAuth2 callback by:
   * 1. Validating required parameters (authorization code)
   * 2. Exchanging authorization code for access/ID tokens
   * 3. Storing tokens or user info in request attributes
   * 4. Returning the next step in the authentication flow
   * 
   * Implementation should handle provider-specific token exchange APIs
   * and error scenarios appropriately.
   */
  public abstract String handleCallback(Map<String, Object> params, HttpServletRequest request);

}
