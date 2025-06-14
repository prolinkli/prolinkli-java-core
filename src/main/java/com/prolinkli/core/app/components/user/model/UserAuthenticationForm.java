package com.prolinkli.core.app.components.user.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class UserAuthenticationForm extends User {

	private String authenticationMethodLk;
	// could be "password", "oauth", "sso", etc.
	private String specialToken;

}
