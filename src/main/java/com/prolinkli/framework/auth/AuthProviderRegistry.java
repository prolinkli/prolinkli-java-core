package com.prolinkli.framework.auth;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.prolinkli.framework.auth.model.AuthProvider;
import com.prolinkli.framework.auth.providers.InternalAuthProvider;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

@Component
@ComponentScan(basePackages = "com.prolinkli.framework.auth.providers")
public class AuthProviderRegistry {

	private final List<AuthProvider> providers;
	private final Map<String, AuthProvider> providersByType = new HashMap<>();

	@Autowired
	public AuthProviderRegistry(List<AuthProvider> allProviders) {
		this.providers = allProviders;
	}

	@PostConstruct
	public void init() {
		for (AuthProvider provider : providers) {
			String providerName = provider.getProviderName();
			if (providersByType.containsKey(providerName)) {
				throw new IllegalArgumentException("Duplicate provider name: " + providerName);
			}
			providersByType.put(providerName, provider);
		}
	}

	public AuthProvider getProvider(String providerName) {
		if (providerName == null || providerName.isEmpty()) {
			throw new IllegalArgumentException("Provider name cannot be null or empty");
		}

		if (!providersByType.containsKey(providerName)) {
			throw new IllegalArgumentException("No provider found for name: " + providerName);
		}

		AuthProvider provider = providersByType.get(providerName);
		if (provider == null) {
			throw new IllegalArgumentException("No provider found for name: " + providerName);
		}

		return provider;
	}

}
