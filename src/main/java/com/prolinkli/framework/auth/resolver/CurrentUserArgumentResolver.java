package com.prolinkli.framework.auth.resolver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import com.prolinkli.core.app.components.user.service.UserGetService;
import com.prolinkli.core.app.components.user.model.User;
import com.prolinkli.framework.auth.model.CurrentUser;

@Component
public class CurrentUserArgumentResolver implements HandlerMethodArgumentResolver {

	private static final Logger LOGGER = LoggerFactory.getLogger(CurrentUserArgumentResolver.class);

	@Autowired
	private UserGetService userGetService;

	@Override
	public Object resolveArgument(MethodParameter arg0, ModelAndViewContainer arg1, NativeWebRequest arg2,
			WebDataBinderFactory arg3) throws Exception {

		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

		if (authentication == null || !authentication.isAuthenticated()) {
			LOGGER.debug("Current user authentication not found");
			return null;
		}

		if (authentication.getPrincipal() instanceof Long) {
			LOGGER.debug("Current user ID found in authentication: {}", authentication.getPrincipal());
			Long userId = (Long) authentication.getPrincipal();
			User user = userGetService.getUserById(userId);
			if (user != null) {
				LOGGER.debug("Current user found: {}", user);
				return user;
			} else {
				LOGGER.warn("User not found for ID: {}", userId);
			}
		}

		return null;
	}

	@Override
	public boolean supportsParameter(MethodParameter parameter) {
		// TODO Auto-generated method stub
		return parameter.hasParameterAnnotation(CurrentUser.class)
				&& parameter.getParameterType().equals(User.class);
	}

}
