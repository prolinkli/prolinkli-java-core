package com.prolinkli.core.app.components.user.service;

import com.prolinkli.core.app.components.user.model.User;
import com.prolinkli.core.app.components.user.model.UserAuthenticationForm;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserAuthService {

	public User login(UserAuthenticationForm userAuthForm) {
		return new User(); // TODO: Implement login logic
	}

}
