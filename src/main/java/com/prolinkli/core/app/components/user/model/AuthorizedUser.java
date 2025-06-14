package com.prolinkli.core.app.components.user.model;

import com.prolinkli.framework.jwt.model.AuthToken;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class AuthorizedUser extends User {

	private AuthToken authToken;

	public AuthorizedUser() {
	}

	public AuthorizedUser(AuthToken authToken) {
		this.authToken = authToken;
	}

	public AuthorizedUser(User user) {
		super(user.getId(), user.getUsername());
		this.authToken = null; // Initialize with null, can be set later
	}

	public AuthorizedUser(User user, AuthToken authToken) {
		super(user.getId(), user.getUsername());
		this.authToken = authToken;
	}

}
