package com.prolinkli.framework.hash;

import at.favre.lib.crypto.bcrypt.BCrypt;

public class Hasher {

  public static String hashString(String input) throws IllegalStateException {
    if (input == null || input.isEmpty()) {
      throw new IllegalStateException("Input cannot be null or empty");
    }
    char[] inputBytes = input.toCharArray();
    return BCrypt.withDefaults().hashToString(12, inputBytes);
  }

  public static boolean verifyString(String input, String hashed) throws IllegalStateException {

    if (input == null || input.isEmpty() || hashed == null || hashed.isEmpty()) {
      throw new IllegalStateException("Input and hashed password cannot be null or empty");
    }
    char[] inputBytes = input.toCharArray();
    BCrypt.Result result = BCrypt.verifyer().verify(inputBytes, hashed);

    return result.verified;
  }

}
