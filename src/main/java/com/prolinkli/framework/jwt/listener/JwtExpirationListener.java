package com.prolinkli.framework.jwt.listener;

import com.prolinkli.framework.jwt.event.JwtExpirationEvent;
import com.prolinkli.framework.jwt.model.JWTTokenExpiredException;

import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
public class JwtExpirationListener implements ApplicationListener<JwtExpirationEvent> {

  @Override
  public void onApplicationEvent(JwtExpirationEvent event) {

    // Handle the JWT expiration event
    // This could involve logging, notifying users, or performing cleanup actions
    System.out.println("JWT Token expired for: " + event.getAuthToken().getAccessToken());
    // Additional logic can be added here as needed
    //
  }

}
