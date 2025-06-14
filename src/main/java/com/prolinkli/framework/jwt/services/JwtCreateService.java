package com.prolinkli.framework.jwt.services;

import java.security.Key;
import java.util.Date;
import java.util.Map;

import javax.crypto.SecretKey;

import com.prolinkli.core.app.components.user.model.AuthorizedUser;
import com.prolinkli.core.app.components.user.model.User;
import com.prolinkli.framework.jwt.model.AuthToken;
import com.prolinkli.framework.jwt.model.AuthToken.AuthTokenBuilder;
import com.prolinkli.framework.util.map.MapUtil;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtCreateService {

	@Value("${jwt.secret}")
	private String jwtSecret;

	@Value("${jwt.expiration:3600}")
	private long jwtExpiration;

	@Value("${jwt.refreshExpiration:7200}")
	private long jwtRefreshExpiration;

	@Value("${jwt.issuer:Prolinkli}")
	private String jwtIssuer;

	// need to have a method to create JWT token and store it in the database for
	// user authentication

	public AuthToken createJwtToken(Map<String, Object> claims) {
		// TODO: Implement JWT token creation logic here
		// also insert the token into the database if needed
		String accessToken = createToken(claims, jwtExpiration);
		String refreshToken = createToken(claims, jwtRefreshExpiration);

		return new AuthTokenBuilder().accessToken(accessToken)
				.refreshToken(refreshToken).build();
	}

	public AuthorizedUser createJwtTokenForUser(User user, Map<String, Object> claims) {
		// Here you would typically generate a JWT token using a library like
		// jjwt or similar, signing it with a secret key and including the user details
		// in the claims.

		Map<String, Object> userClaims = Map.of(
				"username", user.getUsername(),
				"userId", user.getId()
		// NOTE: pull in email, or other useful fields as needed
		);
		// Merge user claims with any additional claims
		Map<String, Object> finalClaims = MapUtil.merge(userClaims, claims);

		AuthToken jwtTokens = createJwtToken(finalClaims);
		AuthorizedUser authorizedUser = new AuthorizedUser(user, jwtTokens);

		return authorizedUser;
	}

	private String createToken(Map<String, Object> claims, Long expiration) {
		return Jwts.builder()
				.claims()
				.issuer(jwtIssuer) // Set the issuer
				.add(claims)
				.expiration(getExpirationDate(expiration)) // Set expiration
				.and()
				// Set expiration, signing key, etc. as needed
				.signWith(getSignInKey())
				.compact();
	}

	/**
	 * Get signing key for HMAC
	 */
	private Key getSignInKey() {
		byte[] keyBytes = jwtSecret.getBytes();
		return Keys.hmacShaKeyFor(keyBytes);
	}

	private Date getExpirationDate(long expiration) {
		return new Date(System.currentTimeMillis() + expiration * 1000);
	}

}
