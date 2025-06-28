package com.prolinkli.framework.auth.model;

import java.util.Map;

import com.prolinkli.core.app.components.user.model.User;
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


  /**
   * Inserts credentials for a user authentication form.
   * This method is used to store credentials in the database or any other storage
   * system.
   *
   * This is effectively a "create" method for the credentials, which means it allows
   * the system to authenticate the user in the future.
   *
   * Can be used to insert credentials, or reset credentials for a user.
   *
   * @param {@link com.prolinkli.core.app.components.user.model.User}  the user for whom the credentials are being inserted
   * @param credentials basic credentials depending on the provider 
   */
  void insertCredentialsForUser(User user, Map<String, Object> credentials);

}
