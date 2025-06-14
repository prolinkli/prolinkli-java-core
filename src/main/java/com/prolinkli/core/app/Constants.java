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

	public static final class AuthenticationKeys {
		public static final class PASSWORD {
			public static final String USERNAME = "username";
			public static final String PASSWORD = "password";
		}
	}

}
