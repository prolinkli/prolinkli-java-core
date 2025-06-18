package com.prolinkli.framework.config.secrets;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration properties for secrets and sensitive values.
 * Uses @ConfigurationProperties to bind environment variables to Java objects.
 */
@Configuration
@ConfigurationProperties(prefix = "app.secrets")
public class SecretProperties {

    private Database database = new Database();
    private Jwt jwt = new Jwt();
    private Security security = new Security();

    // Getters and setters
    public Database getDatabase() { return database; }
    public void setDatabase(Database database) { this.database = database; }

    public Jwt getJwt() { return jwt; }
    public void setJwt(Jwt jwt) { this.jwt = jwt; }

    public Security getSecurity() { return security; }
    public void setSecurity(Security security) { this.security = security; }

    public static class Database {
        private String host = "${DB_HOST:localhost}";
        private String port = "${DB_PORT:5432}";
        private String name = "${DB_NAME:postgres}";
        private String username = "${DB_USERNAME:postgres}";
        private String password = "${DB_PASSWORD:}";

        // Getters and setters
        public String getHost() { return host; }
        public void setHost(String host) { this.host = host; }

        public String getPort() { return port; }
        public void setPort(String port) { this.port = port; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }

        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }

        public String getUrl() {
            return String.format("jdbc:postgresql://%s:%s/%s", host, port, name);
        }
    }

    public static class Jwt {
        private String secret = "${JWT_SECRET:}";
        private int expirationHours = 24;
        private String issuer = "${JWT_ISSUER:prolinkli-core}";

        // Getters and setters
        public String getSecret() { return secret; }
        public void setSecret(String secret) { this.secret = secret; }

        public int getExpirationHours() { return expirationHours; }
        public void setExpirationHours(int expirationHours) { this.expirationHours = expirationHours; }

        public String getIssuer() { return issuer; }
        public void setIssuer(String issuer) { this.issuer = issuer; }
    }

    public static class Security {
        private String encryptionKey = "${ENCRYPTION_KEY:}";
        private String apiSecretKey = "${API_SECRET_KEY:}";

        // Getters and setters
        public String getEncryptionKey() { return encryptionKey; }
        public void setEncryptionKey(String encryptionKey) { this.encryptionKey = encryptionKey; }

        public String getApiSecretKey() { return apiSecretKey; }
        public void setApiSecretKey(String apiSecretKey) { this.apiSecretKey = apiSecretKey; }
    }
} 