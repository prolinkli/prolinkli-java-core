package com.prolinkli.core.app.components.user.controller;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.prolinkli.core.app.Constants.AuthenticationKeys;
import com.prolinkli.core.app.Constants.Cookies;
import com.prolinkli.core.app.Constants.LkUserAuthenticationMethods;
import com.prolinkli.core.app.components.user.model.AuthorizedUser;
import com.prolinkli.core.app.components.user.model.User;
import com.prolinkli.core.app.components.user.model.UserAuthenticationForm;
import com.prolinkli.core.app.components.user.service.UserAuthService;
import com.prolinkli.core.app.components.user.service.UserCreateService;
import com.prolinkli.framework.auth.model.CurrentUser;
import com.prolinkli.framework.cookies.service.CookieSaveService;
import com.prolinkli.framework.cookies.util.JwtCookieUtil;
import com.prolinkli.framework.exception.response.model.ResponseObject;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/user")
class UserController {

  @Autowired
  private UserAuthService userAuthService;

  @Autowired
  private UserCreateService userCreateService;

  @Autowired
  private CookieSaveService cookieSaveService;

  /**
   * Login endpoint for user authentication.
   * 
   * @param item     User authentication form containing credentials.
   * @param response HttpServletResponse to set cookies.
   * @return AuthorizedUser object containing user details and auth token.
   */
  @PostMapping("/login")
  public AuthorizedUser login(@RequestBody UserAuthenticationForm item, HttpServletResponse response) {
    AuthorizedUser user = userAuthService.login(item);

    // Create cookies with proper path settings
    cookieSaveService.saveCookies(
        JwtCookieUtil.createAuthCookies(user.getAuthToken()),
        response);

    return user;
  }

  /**
   * Google OAuth2 login endpoint for authenticating users with Google ID tokens.
   * 
   * @param request  Map containing the Google ID token under "idToken" key
   * @param response HttpServletResponse to set authentication cookies
   * @return AuthorizedUser object containing user details and auth token
   * @throws IllegalArgumentException if Google ID token is missing or empty
   * 
   * @documentation-pr-rule.mdc
   * 
   *                            This endpoint handles Google OAuth2 authentication
   *                            by:
   *                            1. Extracting Google ID token from request body
   *                            2. Validating ID token presence and format
   *                            3. Creating UserAuthenticationForm with
   *                            GOOGLE_OAUTH2 method
   *                            4. Delegating to UserAuthService for
   *                            authentication processing
   *                            5. Setting secure authentication cookies in
   *                            response
   *                            6. Returning authenticated user information
   * 
   *                            Expected request format:
   *                            ```json
   *                            {
   *                            "idToken": "eyJhbGciOiJSUzI1NiIs..."
   *                            }
   *                            ```
   * 
   *                            The ID token should be obtained from Google's
   *                            OAuth2 flow and contain
   *                            verified user profile information. This endpoint
   *                            only authenticates
   *                            existing users - new user registration is handled
   *                            by the /register endpoint.
   * 
   *                            Security features:
   *                            - ID token verification against Google's public
   *                            keys
   *                            - Secure HTTP-only authentication cookies
   *                            - CSRF protection via cookie settings
   *                            - Automatic token expiration handling
   * 
   *                            Response includes:
   *                            - User profile information
   *                            - JWT access and refresh tokens (in cookies)
   *                            - Authentication status and permissions
   */
  //TODO: add {authProvider} to the path 
  @PostMapping("/login/google")
  public AuthorizedUser loginWithGoogle(@RequestBody Map<String, String> request, HttpServletResponse response) {
    String idToken = request.get("idToken");
    if (idToken == null || idToken.trim().isEmpty()) {
      throw new IllegalArgumentException("Google ID token is required");
    }

    // Create authentication form for the service
    UserAuthenticationForm authForm = new UserAuthenticationForm();
    authForm.setAuthenticationMethodLk(LkUserAuthenticationMethods.GOOGLE_OAUTH2);
    authForm.setSpecialToken(idToken);

    // Authenticate using the existing service
    AuthorizedUser user = userAuthService.login(authForm);

    // Create cookies with proper path settings
    cookieSaveService.saveCookies(
        JwtCookieUtil.createAuthCookies(user.getAuthToken()),
        response);

    return user;
  }

  /**
   * Register endpoint for user creation.
   * 
   * @param item     User authentication form containing credentials.
   * @param response HttpServletResponse to set cookies.
   * @return AuthorizedUser object containing user details and auth token.
   */
  @PostMapping("/register")
  public AuthorizedUser register(@RequestBody UserAuthenticationForm form,
      HttpServletResponse response) {

    AuthorizedUser user = userCreateService.createUser(form);

    // Create cookies with proper path settings
    cookieSaveService.saveCookies(
        JwtCookieUtil.createAuthCookies(user.getAuthToken()),
        response);

    return user;
  }

  @GetMapping("/refresh")
  public AuthorizedUser refresh(@CurrentUser AuthorizedUser user, HttpServletResponse response) {
    if (user == null) {
      throw new IllegalStateException("User not authenticated");
    }

    // try to refresh the user
    AuthorizedUser newUser = userAuthService.refresh(user, response);

    // save cookies
    cookieSaveService.saveCookies(
        JwtCookieUtil.createAuthCookies(newUser.getAuthToken()),
        response);

    return newUser;
  }

  @PreAuthorize("isAuthenticated()")
  @GetMapping("/me")
  public User getCurrentUser(@CurrentUser User user) {
    if (user == null) {
      throw new IllegalStateException("User not authenticated");
    }
    return AuthorizedUser.strip(user);
  }

  /**
   * Logout endpoint that clears authentication cookies.
   * 
   * @param response HttpServletResponse to clear cookies.
   * @return Success message.
   */
  @RequestMapping("/logout")
  public ResponseObject<Void> logout(@CurrentUser AuthorizedUser user, HttpServletResponse response) {

    if (user == null) {
      // really return nothing if the user is not authenticated
      return null;
    }

    userAuthService.logout(user);

    // Clear authentication cookies
    cookieSaveService.saveCookies(
        JwtCookieUtil.createClearAuthCookies(),
        response);

    return new ResponseObject<>(LocalDateTime.now(), "SUCCESS", "Logged out successfully", null);
  }

}
