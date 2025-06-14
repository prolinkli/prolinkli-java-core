package com.prolinkli.framework.auth.model;

import lombok.Data;

@Data
public class InternalAuthCredentials {

	private String username;
	private String password;

	// More could be expanded on such as verification codes, etc.

}
