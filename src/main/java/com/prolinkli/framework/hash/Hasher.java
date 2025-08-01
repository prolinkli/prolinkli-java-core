package com.prolinkli.framework.hash;

import java.security.SecureRandom;

import at.favre.lib.crypto.bcrypt.BCrypt;

public class Hasher {

  private static final int MAX_HASH_LENGTH = 16; // Maximum length for a BCrypt hash (including null terminator)

  public static String hashString(String input) throws IllegalStateException {
    if (input == null || input.isEmpty()) {
      throw new IllegalStateException("Input cannot be null or empty");
    }
    char[] inputBytes = input.toCharArray();
    return BCrypt.withDefaults().hashToString(12, inputBytes);
  }

  public static String generateRandomHash() {
    // Generate a random string of 32 characters
    StringBuilder sb = new StringBuilder(MAX_HASH_LENGTH);
    SecureRandom secureRandom = new SecureRandom();
    for (int i = 0; i < MAX_HASH_LENGTH; i++) {
      int randomChar = (int) (secureRandom.nextInt() * 26) + 'a'; // Random lowercase letter
      sb.append((char) randomChar);
    }
    
    return hashString(sb.toString());
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
