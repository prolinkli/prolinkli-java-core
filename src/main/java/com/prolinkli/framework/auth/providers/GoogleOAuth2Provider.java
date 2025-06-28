package com.prolinkli.framework.auth.providers;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.Map;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.prolinkli.core.app.Constants.AuthenticationKeys;
import com.prolinkli.core.app.Constants.LkUserAuthenticationMethods;
import com.prolinkli.core.app.components.user.model.User;
import com.prolinkli.core.app.components.user.service.UserGetService;
import com.prolinkli.framework.auth.model.AuthProvider;
import com.prolinkli.framework.config.secrets.SecretsManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class GoogleOAuth2Provider implements AuthProvider {

    @Autowired
    private SecretsManager secretsManager;

    @Autowired
    private UserGetService userGetService;

    private GoogleIdTokenVerifier verifier;

    @Override
    public String getProviderName() {
        return LkUserAuthenticationMethods.GOOGLE_OAUTH2;
    }

    @Override
    public Boolean authenticate(Map<String, Object> credentials) {
        this.validateCredentials(credentials);

        String idTokenString = credentials.get(AuthenticationKeys.GOOGLE_OAUTH2.ID_TOKEN).toString();

        try {
            GoogleIdToken idToken = verifyGoogleIdToken(idTokenString);
            if (idToken == null) {
                throw new IllegalArgumentException("Invalid Google ID token");
            }

            GoogleIdToken.Payload payload = idToken.getPayload();
            String email = payload.getEmail();
            String googleUserId = payload.getSubject();

            // Check if user exists in your system
            User existingUser = userGetService.getUserByUsername(email);
            if (existingUser == null) {

                throw new IllegalArgumentException("User not found. Please register first.");
            }

            return true;

        } catch (GeneralSecurityException | IOException e) {
            throw new IllegalArgumentException("Failed to verify Google ID token: " + e.getMessage());
        }
    }

    @Override
    public void validateCredentials(Map<String, Object> credentials) {
        if (credentials == null || credentials.isEmpty()) {
            throw new IllegalArgumentException("Credentials cannot be null or empty");
        }

        final String idTokenKey = AuthenticationKeys.GOOGLE_OAUTH2.ID_TOKEN;

        if (!credentials.containsKey(idTokenKey)) {
            throw new IllegalArgumentException("Credentials must contain Google ID token");
        }

        String idToken = (String) credentials.get(idTokenKey);
        if (idToken == null || idToken.isEmpty()) {
            throw new IllegalArgumentException("Google ID token cannot be null or empty");
        }
    }

    private GoogleIdToken verifyGoogleIdToken(String idTokenString) 
            throws GeneralSecurityException, IOException {
        
        if (verifier == null) {
            verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new GsonFactory())
                    .setAudience(Collections.singletonList(secretsManager.getGoogleClientId()))
                    .build();
        }

        return verifier.verify(idTokenString);
    }

    /**
     * Helper method to extract user information from Google ID token
     * This can be used by other services that need Google user info
     */
    public GoogleIdToken.Payload getGoogleUserInfo(String idTokenString) 
            throws GeneralSecurityException, IOException {
        GoogleIdToken idToken = verifyGoogleIdToken(idTokenString);
        return idToken != null ? idToken.getPayload() : null;
    }
} 
