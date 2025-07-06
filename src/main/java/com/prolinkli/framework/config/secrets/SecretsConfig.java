package com.prolinkli.framework.config.secrets;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.vault.authentication.TokenAuthentication;
import org.springframework.vault.client.VaultEndpoint;
import org.springframework.vault.core.VaultTemplate;
import org.springframework.vault.authentication.ClientAuthentication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Configuration class for Vault-based secrets management.
 * Sets up VaultTemplate and SecretsManager for secure secret storage.
 */
@Configuration
@Profile("!test") // Don't load in test profile
public class SecretsConfig {

    private static final Logger logger = LoggerFactory.getLogger(SecretsConfig.class);

    private static final String DEFAULT_VAULT_ADDRESS = "https://mg2vdmvxdh4s8i6w9hsyuxvg.prolinkli.com";
    private static final String DEFAULT_TOKEN_FILE_PATH = System.getProperty("user.home") + "/.vault-token";

    @Bean
    public VaultEndpoint vaultEndpoint() {
        try {
            String vaultAddress = getVaultAddress();
            logger.info("Configuring Vault endpoint: {}", vaultAddress);
            return VaultEndpoint.from(URI.create(vaultAddress));
        } catch (Exception e) {
            logger.error("Failed to configure Vault endpoint", e);
            throw new RuntimeException("Failed to configure Vault endpoint", e);
        }
    }

    @Bean
    public ClientAuthentication vaultClientAuthentication() {
        try {
            String token = getVaultToken();
            logger.debug("Using Vault token authentication");
            return new TokenAuthentication(token);
        } catch (Exception e) {
            logger.error("Failed to configure Vault authentication", e);
            throw new RuntimeException("Failed to configure Vault authentication", e);
        }
    }

    @Bean
    public VaultTemplate vaultTemplate(VaultEndpoint vaultEndpoint, ClientAuthentication clientAuthentication) {
        return new VaultTemplate(vaultEndpoint, clientAuthentication);
    }

    @Bean
    public SecretsManager secretsManager(VaultTemplate vaultTemplate, Environment environment) {
        return new SecretsManager(vaultTemplate);
    }

    /**
     * Gets the Vault address from environment variables or uses default
     * @return The Vault server address
     */
    private String getVaultAddress() {
        String vaultAddr = System.getenv("VAULT_ADDR");
        if (vaultAddr != null && !vaultAddr.trim().isEmpty()) {
            return vaultAddr;
        }
        return DEFAULT_VAULT_ADDRESS;
    }

    /**
     * Gets the Vault token from environment variable or token file
     * @return The Vault authentication token
     * @throws RuntimeException if token is not found
     */
    private String getVaultToken() {
        // First try environment variable
        String token = System.getenv("VAULT_TOKEN");
        if (token != null && !token.trim().isEmpty()) {
            return token;
        }

        // Then try token file
        String tokenFilePath = System.getenv("VAULT_TOKEN_FILE");
        if (tokenFilePath == null || tokenFilePath.trim().isEmpty()) {
            tokenFilePath = DEFAULT_TOKEN_FILE_PATH;
        }

        try {
            Path path = Paths.get(tokenFilePath);
            if (Files.exists(path)) {
                token = Files.readString(path).trim();
                if (!token.isEmpty()) {
                    return token;
                }
            }
        } catch (IOException e) {
            logger.warn("Failed to read Vault token from file: {}", tokenFilePath, e);
        }

        throw new RuntimeException("No Vault token found. Please set VAULT_TOKEN environment variable or ensure token file exists at: " + tokenFilePath);
    }
} 
