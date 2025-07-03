package com.prolinkli.core.app.components.oauth2.controller;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.prolinkli.core.app.components.oauth2.service.AbstractOAuthService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import jakarta.servlet.http.HttpServletRequest;

/**
 * OAuth2 authentication controller for handling OAuth2 flows.
 * 
 * @documentation-pr-rule.mdc
 * 
 * This controller provides endpoints for OAuth2 authentication flows including:
 * - OAuth2 login redirects to provider authorization URLs
 * - OAuth2 callback handling for authorization code exchange
 * - Multi-provider support through AbstractOAuthService implementations
 * 
 * Supports multiple OAuth2 providers (Google, Facebook, Microsoft) through
 * a pluggable service architecture. Each provider implements AbstractOAuthService
 * and is automatically registered via dependency injection.
 * 
 * Endpoints:
 * - GET /auth/oauth2/{providerId} - Initiates OAuth2 flow
 * - GET /auth/oauth2/{providerId}/callback - Handles OAuth2 callback
 * 
 * @author ProLinkLi Development Team
 * @since 1.0
 */
@Controller
@RequestMapping("/auth/oauth2")
public class OAuth2Controller {

  /**
   * Map of OAuth2 services keyed by provider name for fast lookup.
   * Populated via constructor injection of all AbstractOAuthService implementations.
   */
  private final Map<String, AbstractOAuthService> oauthServices;

  /**
   * Constructs OAuth2Controller with all available OAuth2 services.
   * 
   * @param oauthServices List of all OAuth2 service implementations injected by Spring
   *                     Each service must implement AbstractOAuthService and provide
   *                     a unique provider name via getProviderName()
   */
  @Autowired
  public OAuth2Controller(List<AbstractOAuthService> oauthServices) {
    this.oauthServices = oauthServices.stream()
        .collect(Collectors.toMap(
            AbstractOAuthService::getProviderName,
            service -> service));
  }

  /**
   * Initiates OAuth2 authentication flow by redirecting to provider's authorization URL.
   * 
   * @param id OAuth2 provider identifier (e.g., "google", "facebook", "microsoft")
   * @param response HttpServletRequest for the current request context
   * @return RedirectView to the OAuth2 provider's authorization URL
   * @throws IllegalArgumentException if the specified OAuth2 provider is not found
   * 
   * @documentation-pr-rule.mdc
   * Example: GET /auth/oauth2/google redirects to Google's OAuth2 authorization page
   * with appropriate client credentials and scopes configured for the provider.
   */
  @RequestMapping("/{id}")
  public RedirectView oauthLogin(@PathVariable("id") String id, HttpServletRequest response) {

    AbstractOAuthService svc = getOAuth2Service(id);

    return new RedirectView(
        svc.getRedirectUrl());

  }

  /**
   * Handles OAuth2 callback after user authorization at the provider.
   * 
   * @param id OAuth2 provider identifier that matches the original login request
   * @param code Authorization code returned by OAuth2 provider (required)
   * @param state State parameter for CSRF protection (optional)
   * @param request HttpServletRequest containing callback parameters
   * @return ModelAndView with postRedirect template for client-side processing
   * @throws IllegalArgumentException if code parameter is missing or provider not found
   * 
   * @documentation-pr-rule.mdc
   * This method:
   * 1. Validates the authorization code parameter
   * 2. Delegates to the appropriate OAuth2 service for token exchange
   * 3. Stores ID token and provider info in request attributes
   * 4. Returns a template that handles client-side authentication completion
   * 
   * The postRedirect template contains JavaScript that processes the OAuth2 tokens
   * and completes the authentication flow on the client side.
   */
  @RequestMapping("/{id}/callback")
  public ModelAndView oauthCallback(
      @PathVariable("id") String id,
      @RequestParam(value = "code", required = false) String code,
      @RequestParam(value = "state", required = false) String state,
      HttpServletRequest request) {

    AbstractOAuthService svc = getOAuth2Service(id);

    if (code == null || code.isEmpty()) {
      throw new IllegalArgumentException("Missing code parameter in callback");
    }

    Map<String, Object> params = Map.of(
        "code", code,
        "state", state != null ? state : "");

    String forward = svc.handleCallback(
        params,
        request);

    // Create a POST redirect using an auto-submitting form
    ModelAndView modelAndView = new ModelAndView("postRedirect");
    modelAndView.addObject("redirectUrl", forward);
    modelAndView.addObject("idToken", request.getAttribute("idToken"));
    modelAndView.addObject("provider", svc.getProviderName());

    return modelAndView;
  }

  /**
   * Retrieves the OAuth2 service implementation for the specified provider.
   * 
   * @param id OAuth2 provider identifier
   * @return AbstractOAuthService implementation for the provider
   * @throws IllegalArgumentException if no service is registered for the provider
   * 
   * @documentation-pr-rule.mdc
   * This method provides centralized provider lookup with clear error messaging
   * for unsupported or misconfigured OAuth2 providers.
   */
  private AbstractOAuthService getOAuth2Service(String id) {

    if (id == null || id.trim().isEmpty()) {
      throw new IllegalArgumentException("OAuth2 provider ID cannot be null or empty");
    }

    AbstractOAuthService svc = oauthServices.getOrDefault(id.toLowerCase(), null);
    if (svc == null) {
      throw new IllegalArgumentException("OAuth2 provider not found: " + id);
    }
    return svc;
  }

}
