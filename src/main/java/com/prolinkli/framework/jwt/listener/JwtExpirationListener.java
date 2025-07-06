package com.prolinkli.framework.jwt.listener;

import com.prolinkli.framework.jwt.event.JwtExpirationEvent;
import com.prolinkli.framework.jwt.model.JWTTokenExpiredException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
public class JwtExpirationListener implements ApplicationListener<JwtExpirationEvent> {

  private final static Logger LOGGER = LoggerFactory.getLogger(JwtExpirationListener.class);

  @Override
  public void onApplicationEvent(JwtExpirationEvent event) {

    // Handle the JWT expiration event
    // This could involve logging, notifying users, or performing cleanup actions
    // Additional logic can be added here as needed
    LOGGER.warn("JWT Token expired: {}", event.getAuthToken().getAccessToken());

  }

}
