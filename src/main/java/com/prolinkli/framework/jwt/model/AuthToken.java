package com.prolinkli.framework.jwt.model;

import lombok.Data;

@Data
public class AuthToken {

  private Long id;

  private String accessToken;
  private String refreshToken;

  private AuthToken() {
    // Default constructor for serialization/deserialization
  }

  public static class AuthTokenBuilder {
    private Long id;
    private String accessToken;
    private String refreshToken;

    public AuthTokenBuilder accessToken(String accessToken) {
      this.accessToken = accessToken;
      return this;
    }

    public AuthTokenBuilder refreshToken(String refreshToken) {
      this.refreshToken = refreshToken;
      return this;
    }

    public AuthTokenBuilder id(Long id) {
      this.id = id;
      return this;
    }

    public AuthToken build() {
      if (this.accessToken == null || this.accessToken.isEmpty() || this.refreshToken == null
          || this.refreshToken.isEmpty()) {
        throw new IllegalArgumentException("Access token and refresh token must not be null or empty");
      }
      AuthToken token = new AuthToken();
      token.setId(this.id);
      token.setAccessToken(this.accessToken);
      token.setRefreshToken(this.refreshToken);
      return token;
    }
  }

}
