package com.prolinkli.core.app.components.user.controller;

import com.prolinkli.core.app.Constants.Cookies;
import com.prolinkli.core.app.components.user.model.AuthorizedUser;
import com.prolinkli.core.app.components.user.model.User;
import com.prolinkli.core.app.components.user.model.UserAuthenticationForm;
import com.prolinkli.core.app.components.user.service.UserAuthService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/user")
class UserController {

	@Autowired
	UserAuthService userAuthService;

	@PostMapping("/login")
	public AuthorizedUser login(@RequestBody UserAuthenticationForm item, HttpServletResponse response) {
		AuthorizedUser user = userAuthService.login(item);
		response.addCookie(new Cookie(Cookies.Authentication.ACCESS_TOKEN, user.getAuthToken().getAccessToken()));
		response.addCookie(new Cookie(Cookies.Authentication.REFRESH_TOKEN, user.getAuthToken().getRefreshToken()));
		response.addCookie(new Cookie(Cookies.Authentication.USER_ID, user.getId().toString()));

		return user;
	}

}
