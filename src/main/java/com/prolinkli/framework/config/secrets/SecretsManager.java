package com.prolinkli.framework.config.secrets;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * Centralized secrets manager that provides access to sensitive configuration values.
 * Prioritizes system environment variables over .env file values for security in production.
 */
@Component
public class SecretsManager {

    private final Dotenv dotenv;
    private final Environment environment;

    public SecretsManager(Dotenv dotenv, Environment environment) {
        this.dotenv = dotenv;
        this.environment = environment;
    }

    /**
     * Gets a secret value by key, prioritizing system environment variables
     * @param key The environment variable key
     * @return The secret value, or null if not found
     */
    public String getSecret(String key) {
        // First check system environment variables (for production)
        String systemValue = System.getenv(key);
        if (systemValue != null && !systemValue.trim().isEmpty()) {
            return systemValue;
        }

        // Then check .env file (for development)
        String dotenvValue = dotenv.get(key);
        if (dotenvValue != null && !dotenvValue.trim().isEmpty()) {
            return dotenvValue;
        }

        // Finally check Spring properties as fallback
        return environment.getProperty(key);
    }

    /**
     * Gets a secret value with a default fallback
     * @param key The environment variable key
     * @param defaultValue The default value if key is not found
     * @return The secret value or default value
     */
    public String getSecret(String key, String defaultValue) {
        String value = getSecret(key);
        return value != null ? value : defaultValue;
    }

    /**
     * Gets a required secret value, throws exception if not found
     * @param key The environment variable key
     * @return The secret value
     * @throws IllegalStateException if the secret is not found
     */
    public String getRequiredSecret(String key) {
        String value = getSecret(key);
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalStateException("Required secret not found: " + key);
        }
        return value;
    }

    /**
     * Database configuration getters
     */
    public String getDbHost() {
        return getSecret("DB_HOST", "localhost");
    }

    public String getDbPort() {
        return getSecret("DB_PORT", "5432");
    }

    public String getDbName() {
        return getSecret("DB_NAME", "postgres");
    }

    public String getDbUsername() {
        return getRequiredSecret("DB_USERNAME");
    }

    public String getDbPassword() {
        return getRequiredSecret("DB_PASSWORD");
    }

    /**
     * JWT configuration getters
     */
    public String getJwtSecret() {
        return getRequiredSecret("JWT_SECRET");
    }

    public int getJwtExpirationHours() {
        String hours = getSecret("JWT_EXPIRATION_HOURS", "24");
        return Integer.parseInt(hours);
    }

    public String getJwtIssuer() {
        return getSecret("JWT_ISSUER", "prolinkli-core");
    }

    /**
     * Security configuration getters
     */
    public String getEncryptionKey() {
        return getRequiredSecret("ENCRYPTION_KEY");
    }

    public String getApiSecretKey() {
        return getRequiredSecret("API_SECRET_KEY");
    }

    /**
     * Environment configuration
     */
    public String getEnvironment() {
        return getSecret("ENVIRONMENT", "development");
    }

    public boolean isDevelopment() {
        return "development".equalsIgnoreCase(getEnvironment());
    }

    public boolean isProduction() {
        return "production".equalsIgnoreCase(getEnvironment());
    }
} 