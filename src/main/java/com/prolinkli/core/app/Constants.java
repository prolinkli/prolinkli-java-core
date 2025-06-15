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
	}

	public static final class Cookies {
		public static final class Authentication {
			public static final String ACCESS_TOKEN = "accessToken";
			public static final String REFRESH_TOKEN = "refreshToken";
			public static final String EXPIRES = "expires";
			public static final String USER_ID = "userId";
		}
	}

	public static final class AuthenticationKeys {
		public static final class PASSWORD {
			public static final String USERNAME = "username";
			public static final String PASSWORD = "password";
		}
	}

}
