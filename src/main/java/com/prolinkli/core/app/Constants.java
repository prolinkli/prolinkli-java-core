package com.prolinkli.core.app;

import java.math.BigInteger;

/**
 * Constants
 */
public final class Constants {

	public static final class Application {
		public static final String APP_NAME = "ProLinkLi";
	}

	public static final class User {
		public static final BigInteger STARTING_ID = BigInteger.valueOf(100000);
	}

	public static final class LkUserAuthenticationMethods {
		public static final String PASSWORD = "INTERNAL";
		public static final String GOOGLE_OAUTH2 = "GOOGLE_OAUTH2";
	}

  public static final class OAuth2ProvidersLks {
    public static final String GOOGLE = "google";
    public static final String FACEBOOK = "facebook";
    public static final String MICROSOFT = "microsoft";
  }

  public static final class OAuth2Providers {
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

	public static final class AuthenticationKeys {
		public static final class PASSWORD {
			public static final String USERNAME = "username";
			public static final String PASSWORD = "password";
		}
		
		public static final class GOOGLE_OAUTH2 {
			public static final String CODE = "code";
			public static final String STATE = "state";
			public static final String ID_TOKEN = "id_token";
			public static final String ACCESS_TOKEN = "access_token";
		}
	}

}
