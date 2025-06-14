package com.prolinkli.framework.auth.model;

import java.util.Map;

import com.prolinkli.core.app.components.user.model.UserAuthenticationForm;

public interface AuthProvider {

	/**
	 * Returns the name of the authentication provider.
	 *
	 * @return the name of the authentication provider
	 */
	String getProviderName();

	/**
	 * Authenticates a user with the given credentials.
	 *
	 * NOTE: this will be subject to change as future authentication providers
	 * require bigger methods.
	 *
	 * 
	 * @param credentials the credentials to authenticate
	 * @return an authentication token if successful, null otherwise
	 */
	Boolean authenticate(Map<String, Object> credentials);

	/**
	 * Validates the credentials provided for authentication.
	 *
	 * @param credentials the credentials to validate
	 * @throws IllegalArgumentException if the credentials are invalid
	 */
	void validateCredentials(Map<String, Object> credentials);

}
