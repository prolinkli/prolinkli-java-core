package com.prolinkli.core.app.components.user.service;

import com.prolinkli.core.app.Constants.LkUserAuthenticationMethods;
import com.prolinkli.core.app.components.user.model.User;
import com.prolinkli.core.app.components.user.model.UserAuthenticationForm;
import com.prolinkli.framework.auth.AuthProviderRegistry;
import com.prolinkli.framework.auth.model.InternalAuthCredentials;

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
		authForm.authenticate(userAuthForm);

		return new User(); // TODO: Implement login logic
	}
}
