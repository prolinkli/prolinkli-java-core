package com.prolinkli.framework.jwt.event;

import com.prolinkli.framework.jwt.model.AuthToken;

import org.springframework.context.ApplicationEvent;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class JwtExpirationEvent extends ApplicationEvent {

  private AuthToken authToken;

  public JwtExpirationEvent(
      Object source,
      AuthToken authToken) {
    super(source);
    this.authToken = authToken;
  }

}
