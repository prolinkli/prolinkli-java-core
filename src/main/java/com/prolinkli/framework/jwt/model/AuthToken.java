package com.prolinkli.framework.jwt.model;

import lombok.Data;

@Data
public class AuthToken {

	private String accessToken;
	private String refreshToken;

	private AuthToken() {
		// Default constructor for serialization/deserialization
	}

	public static class AuthTokenBuilder {
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

		public AuthToken build() {
			AuthToken token = new AuthToken();
			token.setAccessToken(this.accessToken);
			token.setRefreshToken(this.refreshToken);
			return token;
		}
	}

}
