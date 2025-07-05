package com.prolinkli.core.app;

import java.math.BigInteger;

/**
 * Constants
 */
public final class Constants {

  public static final class Application {
    public static final String APP_NAME = "ProLinkLi";
  }

  public static final class HttpStatuses {
    public static final class PageExpired {
      public static final Integer CODE = 419;
      public static final String REASON = "Page Expired";
    }
  }

  public static final class User {
    public static final BigInteger STARTING_ID = BigInteger.valueOf(100000);
    public static final Integer MIN_USERNAME_LENGTH = 4;
    public static final Integer MAX_USERNAME_LENGTH = 32;
  }

  /**
   * Authentication method constants for user authentication lookup.
   * 
   * @documentation-pr-rule.mdc
   * 
   *                            These constants define the supported
   *                            authentication methods in the system:
   *                            - PASSWORD: Traditional username/password
   *                            authentication
   *                            - GOOGLE_OAUTH2: Google OAuth2 authentication
   *                            - FACEBOOK_OAUTH2: Facebook OAuth2 authentication
   *                            (planned)
   *                            - MICROSOFT_OAUTH2: Microsoft OAuth2
   *                            authentication (planned)
   * 
   *                            Values correspond to entries in the
   *                            lk_user_authentication_methods database table
   *                            and are used throughout the authentication system
   *                            for method identification.
   */
  public static final class LkUserAuthenticationMethods {
    public static final String PASSWORD = "INTERNAL";
    public static final String GOOGLE_OAUTH2 = "GOOGLE";
    public static final String FACEBOOK_OAUTH2 = "FACEBOOK";
    public static final String MICROSOFT_OAUTH2 = "MICROSOFT";
  }

  /**
   * OAuth2 provider identifier constants for URL routing and service lookup.
   * 
   * @documentation-pr-rule.mdc
   * 
   *                            These lowercase identifiers are used in:
   *                            - OAuth2Controller URL path variables
   *                            (/auth/oauth2/{provider})
   *                            - Service registration and lookup in
   *                            OAuth2Controller
   *                            - Frontend OAuth2 provider selection
   * 
   *                            Must match the provider names returned by
   *                            AbstractOAuthService.getProviderName()
   *                            for proper service resolution and routing.
   */
  public static final class OAuth2ProvidersLks {
    public static final String GOOGLE = "google";
    public static final String FACEBOOK = "facebook";
    public static final String MICROSOFT = "microsoft";
  }

  /**
   * OAuth2 provider-specific parameter key constants.
   * 
   * @documentation-pr-rule.mdc
   * 
   *                            Contains nested classes for each OAuth2 provider
   *                            with their specific
   *                            parameter keys used in callback handling and token
   *                            exchange.
   * 
   *                            These constants ensure consistent parameter
   *                            handling across OAuth2 flows
   *                            and prevent typos in parameter key references.
   */
  public static final class OAuth2Providers {
    /**
     * Google OAuth2 specific parameter keys.
     * 
     * @documentation-pr-rule.mdc
     * 
     *                            - CODE_KEY: Authorization code parameter from
     *                            Google callback
     *                            - STATE_KEY: CSRF protection state parameter from
     *                            Google callback
     * 
     *                            Used in GoogleOAuthService for processing OAuth2
     *                            callback parameters
     *                            according to Google's OAuth2 specification.
     */
    public static final class Google {
      public static final String CODE_KEY = "code";
      public static final String STATE_KEY = "state";
    }
  }

  public static final class Cookies {
    public static final class Authentication {
      public static final String ACCESS_TOKEN = "accessToken";
      public static final String REFRESH_TOKEN = "refreshToken";
      public static final String EXPIRES = "expires";
      public static final String USER_ID = "userId";
    }
  }

  public static final class Jwt {
    public static final String USER_ID_CLAIMS_KEY = "userId";
    public static final Class<?> USER_ID_CLAIMS_CLASS = Integer.class;
    public static final String USERNAME_CLAIMS_KEY = "username";
    public static final Class<?> USERNAME_CLAIMS_CLASS = String.class;
  }

  /**
   * Authentication parameter key constants for different authentication methods.
   * 
   * @documentation-pr-rule.mdc
   * 
   *                            Contains nested classes for each authentication
   *                            method with their specific
   *                            parameter keys used in credential validation and
   *                            processing.
   * 
   *                            These constants ensure type safety and prevent
   *                            parameter key mismatches
   *                            across authentication flows.
   */
  public static final class AuthenticationKeys {
    /**
     * Traditional password authentication parameter keys.
     */
    public static final class PASSWORD {
      public static final String USERNAME = "username";
      public static final String PASSWORD = "password";
    }

    /**
     * Google OAuth2 authentication parameter keys.
     * 
     * @documentation-pr-rule.mdc
     * 
     *                            Parameter keys used in Google OAuth2
     *                            authentication flow:
     *                            - CODE: Authorization code from OAuth2 callback
     *                            - STATE: CSRF protection state parameter
     *                            - SUBJECT: Google user ID (sub claim) from ID
     *                            token
     *                            - ID_TOKEN: Google ID token containing user
     *                            profile
     *                            - ACCESS_TOKEN: Google access token for API calls
     * 
     *                            Used by GoogleOAuth2Provider for credential
     *                            validation and
     *                            user authentication processing.
     */
    public static final class GOOGLE_OAUTH2 {
      public static final String CODE = "code";
      public static final String STATE = "state";
      public static final String SUBJECT = "sub";
      public static final String ID_TOKEN = "id_token";
      public static final String ACCESS_TOKEN = "access_token";
    }
  }

}
