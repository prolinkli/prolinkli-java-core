package com.prolinkli.framework.config.secrets;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.vault.core.VaultKeyValueOperations;
import org.springframework.vault.core.VaultTemplate;
import org.springframework.vault.support.VaultResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Vault-based secrets manager that provides access to sensitive configuration values.
 * Integrates with HashiCorp Vault for secure secret storage.
 * 
 * Supports different secret domains based on active profiles:
 * - dev: Uses secrets from vault path secret/spring/dev/*
 * - local-dev: Uses secrets from vault path secret/spring/local-dev/*
 * 
 * Secret paths are organized by category for easy management and extension.
 */
@Component
public class SecretsManager {

    private static final Logger logger = LoggerFactory.getLogger(SecretsManager.class);

    private final VaultTemplate vaultTemplate;
    
    @Value("${spring.vault.secret.domain:dev}")
    private String secretDomain;

    // Vault secret sub-paths
    private static final String JWT_PATH = "jwt";
    private static final String DB_PATH = "db";
    private static final String SECURITY_PATH = "security";
    private static final String OAUTH_PATH = "oauth";
    private static final String APP_PATH = "app";

    @Autowired
    public SecretsManager(VaultTemplate vaultTemplate) {
        this.vaultTemplate = vaultTemplate;
    }

    /**
     * Gets a secret value by key with intelligent path detection
     * @param key The secret key
     * @return The secret value, or null if not found
     */
    public String getSecret(String key) {
        String subPath = determineSecretPath(key);
        return getSecret(subPath, key);
    }

    /**
     * Gets a secret value from a specific Vault sub-path
     * @param subPath The Vault secret sub-path (e.g., "jwt", "db", "oauth")
     * @param key The secret key within the path
     * @return The secret value, or null if not found
     */
    public String getSecret(String subPath, String key) {
        try {
            // Construct the full path: secrets/{domain}/{category}
            String fullPath = "secrets/" + secretDomain + "/" + subPath;
            
            // Use "kv" mount point, not "secrets"
            VaultKeyValueOperations ops = vaultTemplate.opsForKeyValue("kv", 
                                                                      VaultKeyValueOperations.KeyValueBackend.KV_2);
            VaultResponse response = ops.get(fullPath);
            
            if (response != null && response.getData() != null) {
                Map<String, Object> data = response.getData();
                Object value = data.get(key);
                if (value != null) {
                    logger.debug("Retrieved secret '{}' from Vault path 'kv/{}'", key, fullPath);
                    return value.toString();
                }
            }
        } catch (Exception e) {
            logger.warn("Failed to retrieve secret from Vault for path: kv/secrets/{}/{}, key: {}, error: {}", 
                       secretDomain, subPath, key, e.getMessage());
        }
        
        logger.debug("Secret '{}' not found in Vault path 'kv/secrets/{}/{}'", key, secretDomain, subPath);
        return null;
    }

    /**
     * Determines the appropriate secret sub-path based on key patterns
     * @param key The secret key
     * @return The appropriate secret sub-path
     */
    private String determineSecretPath(String key) {
        if (key.startsWith("DB_")) {
            return DB_PATH;
        } else if (key.startsWith("JWT_")) {
            return JWT_PATH;
        } else if (key.contains("ENCRYPTION") || key.contains("API_SECRET")) {
            return SECURITY_PATH;
        } else if (key.contains("OAUTH") || key.contains("GOOGLE_")) {
            return OAUTH_PATH;
        } else if (key.equals("ENVIRONMENT")) {
            return APP_PATH;
        } else if (key.contains("EMAIL")) {
            return "email";
        } else if (key.contains("REDIS")) {
            return "redis";
        }
        return APP_PATH; // Default fallback
    }

    /**
     * Gets a secret value with a default fallback
     * @param key The secret key
     * @param defaultValue The default value if key is not found
     * @return The secret value or default value
     */
    public String getSecretOrDefault(String key, String defaultValue) {
        String value = getSecret(key);
        return value != null ? value : defaultValue;
    }

    /**
     * Gets a secret value from a specific path with a default fallback
     * @param subPath The Vault secret sub-path
     * @param key The secret key
     * @param defaultValue The default value if key is not found
     * @return The secret value or default value
     */
    public String getSecretWithDefault(String subPath, String key, String defaultValue) {
        String value = getSecret(subPath, key);
        return value != null ? value : defaultValue;
    }

    /**
     * Gets a required secret value, throws exception if not found
     * @param key The secret key
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
     * Gets a required secret value from a specific path, throws exception if not found
     * @param subPath The Vault secret sub-path
     * @param key The secret key
     * @return The secret value
     * @throws IllegalStateException if the secret is not found
     */
    public String getRequiredSecret(String subPath, String key) {
        String value = getSecret(subPath, key);
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalStateException("Required secret not found: " + key + " in path: " + subPath);
        }
        return value;
    }

    // ===========================================
    // EXTENSIBLE SECRET GETTERS
    // Add new secret categories here easily
    // ===========================================

    /**
     * Database configuration getters
     */
    public String getDbHost() {
        return getSecretWithDefault(DB_PATH, "DB_HOST", "localhost");
    }

    public String getDbPort() {
        return getSecretWithDefault(DB_PATH, "DB_PORT", "5432");
    }

    public String getDbName() {
        return getSecretWithDefault(DB_PATH, "DB_NAME", "postgres");
    }

    public String getDbUsername() {
        return getRequiredSecret(DB_PATH, "DB_USERNAME");
    }

    public String getDbPassword() {
        return getRequiredSecret(DB_PATH, "DB_PASSWORD");
    }

    /**
     * JWT configuration getters
     */
    public String getJwtSecret() {
        return getRequiredSecret(JWT_PATH, "JWT_SECRET");
    }

    public int getJwtExpirationHours() {
        String hours = getSecretWithDefault(JWT_PATH, "JWT_EXPIRATION_HOURS", "24");
        return Integer.parseInt(hours);
    }

    public String getJwtIssuer() {
        return getSecretWithDefault(JWT_PATH, "JWT_ISSUER", "prolinkli-core");
    }

    /**
     * Security configuration getters
     */
    public String getEncryptionKey() {
        return getRequiredSecret(SECURITY_PATH, "ENCRYPTION_KEY");
    }

    public String getApiSecretKey() {
        return getRequiredSecret(SECURITY_PATH, "API_SECRET_KEY");
    }

    /**
     * OAuth2 configuration getters
     */
    public String getGoogleClientId() {
        return getRequiredSecret("oauth/google", "GOOGLE_OAUTH_CLIENT_ID");
    }

    public String getGoogleClientSecret() {
        return getRequiredSecret("oauth/google", "GOOGLE_OAUTH_CLIENT_SECRET");
    }

    public String getGoogleRedirectUri() {
        return getRequiredSecret("oauth/google", "GOOGLE_OAUTH_REDIRECT_URI");
    }

    /**
     * Environment configuration
     */
    public String getEnvironment() {
        return getSecretWithDefault(APP_PATH, "ENVIRONMENT", "development");
    }

    public boolean isDevelopment() {
        return "development".equalsIgnoreCase(getEnvironment());
    }

    public boolean isProduction() {
        return "production".equalsIgnoreCase(getEnvironment());
    }

    // ===========================================
    // EXTENSION METHODS
    // Add new secret categories here easily
    // ===========================================

    /**
     * Email service configuration getters
     * Example of how to extend with new secrets
     */
    public String getEmailApiKey() {
        return getSecret("email", "EMAIL_API_KEY");
    }

    public String getEmailSenderAddress() {
        return getSecretWithDefault("email", "EMAIL_SENDER_ADDRESS", "noreply@prolinkli.com");
    }

    /**
     * Redis configuration getters
     */
    public String getRedisHost() {
        return getSecretWithDefault("redis", "REDIS_HOST", "localhost");
    }

    public String getRedisPort() {
        return getSecretWithDefault("redis", "REDIS_PORT", "6379");
    }

    public String getRedisPassword() {
        return getSecret("redis", "REDIS_PASSWORD");
    }

    /**
     * External API configuration getters
     */
    public String getExternalApiKey(String serviceName) {
        return getSecret("external-apis", serviceName.toUpperCase() + "_API_KEY");
    }

    public String getExternalApiUrl(String serviceName) {
        return getSecret("external-apis", serviceName.toUpperCase() + "_API_URL");
    }
} 
