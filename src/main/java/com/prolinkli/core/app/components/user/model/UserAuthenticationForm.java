package com.prolinkli.core.app.components.user.model;

import java.util.HashMap;
import java.util.Map;

import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Data
@EqualsAndHashCode(callSuper = false)
public class UserAuthenticationForm extends User {

	private String authenticationMethodLk;
	// could be "password", "oauth", "sso", etc.
	private String specialToken;

  @Getter(value=AccessLevel.PRIVATE)
  private final Map<String, Object> parameters = new HashMap<>();

  public void addParameter(String key, Object value) {
    if (key == null || value == null) {
      throw new IllegalArgumentException("Key and value cannot be null");
    }

    if (parameters.containsKey(key)) {
      parameters.replace(key, value);
    } else {
      parameters.put(key, value);
    }

  }

  @SuppressWarnings("unchecked")
  public <T> T getParameter(String key) {
    return (T)parameters.getOrDefault(key, null);
  }


}
