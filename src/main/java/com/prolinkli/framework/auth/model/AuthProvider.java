package com.prolinkli.framework.auth.model;

public interface AuthProvider<T> {

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
	Boolean authenticate(T credentials);

}
