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
import com.prolinkli.core.app.components.user.model.AuthorizedUser;
import com.prolinkli.core.app.components.user.model.User;
import com.prolinkli.framework.auth.model.CurrentUser;
import com.prolinkli.framework.jwt.model.AuthToken;
import com.prolinkli.framework.jwt.model.AuthToken.AuthTokenBuilder;

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
      if (user == null) {
        LOGGER.warn("User not found for ID: {}", userId);
        return null;
      }

      AuthToken authToken = (AuthToken) authentication.getCredentials();
      if (authToken == null) {
        LOGGER.warn("Auth token not found in authentication for user ID: {}", userId);
      }

      AuthorizedUser authorizedUser = new AuthorizedUser(user, authToken);

      return authorizedUser;
    }

    return null;
  }

  @Override
  public boolean supportsParameter(MethodParameter parameter) {
    // TODO Auto-generated method stub
    return parameter.hasParameterAnnotation(CurrentUser.class)
        && (parameter.getParameterType().equals(User.class)
            || parameter.getParameterType().equals(AuthorizedUser.class));
  }

}
