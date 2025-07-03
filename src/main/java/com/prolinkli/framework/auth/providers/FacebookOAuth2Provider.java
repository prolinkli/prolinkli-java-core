package com.prolinkli.framework.auth.providers;

import java.util.Map;

import com.prolinkli.core.app.Constants.OAuth2ProvidersLks;
import com.prolinkli.core.app.components.user.model.User;
import com.prolinkli.core.app.components.user.model.UserAuthenticationForm;
import com.prolinkli.core.app.db.model.generated.UserDb;
import com.prolinkli.framework.auth.model.AuthProvider;
import com.prolinkli.framework.db.dao.Dao;

import org.springframework.stereotype.Component;

@Component
public class FacebookOAuth2Provider implements AuthProvider {

  @Override
  public String getProviderName() {
    return OAuth2ProvidersLks.FACEBOOK;
  }

  @Override
  public Boolean authenticate(Map<String, Object> credentials) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'authenticate'");
  }

  @Override
  public void validateCredentials(Map<String, Object> credentials) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'validateCredentials'");
  }

  @Override
  public void insertCredentialsForUser(User user, Map<String, Object> credentials) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'insertCredentialsForUser'");
  }

  @Override
  public User getUserFromCredentials(UserAuthenticationForm userAuthForm) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'getUserFromCredentials'");
  }

  @Override
  public void createUser(UserAuthenticationForm userAuthForm, Dao<UserDb, Long> dao) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'createUser'");
  }


}
