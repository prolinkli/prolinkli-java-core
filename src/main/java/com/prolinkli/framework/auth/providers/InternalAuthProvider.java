package com.prolinkli.framework.auth.providers;

import com.prolinkli.core.app.Constants.LkUserAuthenticationMethods;
import com.prolinkli.core.app.components.user.model.UserAuthenticationForm;
import com.prolinkli.framework.auth.model.AuthProvider;

import org.springframework.stereotype.Component;

@Component
public class InternalAuthProvider implements AuthProvider {

	public InternalAuthProvider() {
		// Constructor can be used for initialization if needed
	}

	@Override
	public String getProviderName() {
		return LkUserAuthenticationMethods.PASSWORD;
	}

	@Override
	public Boolean authenticate(UserAuthenticationForm credentials) {
		// TODO: Implement authentication logic
		return true;
	}

}
