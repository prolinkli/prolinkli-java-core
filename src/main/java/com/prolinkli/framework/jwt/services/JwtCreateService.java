package com.prolinkli.framework.jwt.services;

import java.util.Map;

import com.prolinkli.core.app.components.user.model.AuthorizedUser;
import com.prolinkli.core.app.components.user.model.User;
import com.prolinkli.framework.jwt.model.AuthToken;
import com.prolinkli.framework.jwt.model.AuthToken.AuthTokenBuilder;
import com.prolinkli.framework.util.map.MapUtil;

import org.springframework.stereotype.Service;

@Service
public class JwtCreateService {

	// need to have a method to create JWT token and store it in the database for
	// user authentication

	public AuthToken createJwtToken(Map<String, Object> claims) {
		// TODO: Implement JWT token creation logic here
		// also insert the token into the database if needed
		return new AuthTokenBuilder().accessToken("ACCESS_TOKEN_PLACEHOLDER")
				.refreshToken("REFRESH_TOKEN_PLACEHOLDER").build();
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

}
