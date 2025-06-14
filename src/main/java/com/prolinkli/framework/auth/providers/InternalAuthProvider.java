package com.prolinkli.framework.auth.providers;

import com.prolinkli.core.app.Constants.LkUserAuthenticationMethods;
import com.prolinkli.framework.auth.model.AuthProvider;
import com.prolinkli.framework.auth.model.InternalAuthCredentials;

import org.springframework.stereotype.Component;

@Component
public class InternalAuthProvider implements AuthProvider<InternalAuthCredentials> {

	@Override
	public String getProviderName() {
		return LkUserAuthenticationMethods.PASSWORD;
	}

	@Override
	public Boolean authenticate(InternalAuthCredentials credentials) {
		// TODO: Implement authentication logic
		return true;
	}

}
