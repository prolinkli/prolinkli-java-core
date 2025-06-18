package com.prolinkli.framework.config.secrets;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

/**
 * Configuration class for loading secrets and environment variables.
 * Loads from .env file in development and system environment variables in production.
 */
@Configuration
public class SecretsConfig {

    /**
     * Creates a Dotenv bean that loads environment variables from .env file
     * Falls back to system environment variables if .env file doesn't exist
     */
    @Bean
    public Dotenv dotenv() {
        return Dotenv.configure()
                .directory("./")  // Look for .env in project root
                .ignoreIfMalformed()
                .ignoreIfMissing()  // Don't fail if .env doesn't exist (for production)
                .load();
    }

    /**
     * Creates a SecretsManager bean for accessing secrets
     */
    @Bean
    public SecretsManager secretsManager(Dotenv dotenv, Environment environment) {
        return new SecretsManager(dotenv, environment);
    }
} 