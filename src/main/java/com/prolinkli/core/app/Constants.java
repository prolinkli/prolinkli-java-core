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
    public static final Integer MIN_USERNAME_LENGTH = 4;
    public static final Integer MAX_USERNAME_LENGTH = 32;
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
  }

}
