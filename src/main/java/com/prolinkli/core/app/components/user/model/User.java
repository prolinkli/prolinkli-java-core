package com.prolinkli.core.app.components.user.model;

import java.math.BigInteger;

import com.prolinkli.core.app.components.user.userdetails.model.UserDetails;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class User {

	private Long id;
	private String username;

  private UserDetails userDetails;

}
