package com.prolinkli.core.app.components.user.controller;

import com.prolinkli.core.app.components.user.model.User;
import com.prolinkli.core.app.components.user.model.UserAuthenticationForm;
import com.prolinkli.core.app.components.user.service.UserAuthService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
class UserController {

	@Autowired
	UserAuthService userAuthService;

	@PostMapping("/login")
	public User login(@RequestBody UserAuthenticationForm item) {
		return userAuthService.login(item);
	}

}
