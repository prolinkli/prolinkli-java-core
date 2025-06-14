package com.prolinkli.core.app.components.user.service;

import java.util.HashMap;
import java.util.Map;

import com.prolinkli.core.app.Constants.AuthenticationKeys;
import com.prolinkli.core.app.Constants.LkUserAuthenticationMethods;
import com.prolinkli.core.app.components.user.model.User;
import com.prolinkli.core.app.components.user.model.UserAuthenticationForm;
import com.prolinkli.framework.auth.AuthProviderRegistry;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserAuthService {

	private AuthProviderRegistry authProviderRegistry;

	@Autowired
	UserAuthService(AuthProviderRegistry authProviderRegistry) {
		this.authProviderRegistry = authProviderRegistry;
	}

	public User login(UserAuthenticationForm userAuthForm) {

		if (userAuthForm == null || userAuthForm.getAuthenticationMethodLk() == null) {
			throw new IllegalArgumentException("User authentication form and authentication method cannot be null");
		}

		var authForm = this.authProviderRegistry.getProvider(userAuthForm.getAuthenticationMethodLk());
		authForm.authenticate(getCredentials(userAuthForm));

		return new User(); // TODO: Implement login logic
	}

	private Map<String, Object> getCredentials(UserAuthenticationForm userAuthForm) {
		if (userAuthForm == null || userAuthForm.getAuthenticationMethodLk() == null) {
			throw new IllegalArgumentException("User authentication form and authentication method cannot be null");
		}

		Map<String, Object> credentials = new HashMap<String, Object>();

		if (LkUserAuthenticationMethods.PASSWORD.equals(userAuthForm.getAuthenticationMethodLk())) {
			credentials.put(AuthenticationKeys.PASSWORD.USERNAME, userAuthForm.getUsername());
			credentials.put(AuthenticationKeys.PASSWORD.PASSWORD, userAuthForm.getSpecialToken());
			return credentials;
		}

		throw new IllegalArgumentException(
				"Unsupported authentication method: " + userAuthForm.getAuthenticationMethodLk());
	}
}
