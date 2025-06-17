package com.prolinkli.core.app.components.user.model;

import java.math.BigInteger;

import lombok.Data;

@Data
public class User {

	private Long id;
	private String username;

	// TODO: Add more fields as needed, such as email, password, etc. (UserDetails
	// object)

	public User() {
		// Default constructor for serialization/deserialization
	}

	public User(Long id, String username) {
		this.id = id;
		this.username = username;
	}

}
