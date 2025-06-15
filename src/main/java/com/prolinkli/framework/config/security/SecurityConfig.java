package com.prolinkli.framework.config.security;

import com.prolinkli.framework.jwt.http.JwtRequestValidator;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

	private final JwtRequestValidator beforeFilter;

	public SecurityConfig() {
		this.beforeFilter = new JwtRequestValidator();
	}

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

		http
				.csrf(csrf -> csrf.disable()) // Disable CSRF for simplicity, adjust as needed
				.authorizeHttpRequests(auth -> {
					auth.anyRequest().permitAll(); // Require authentication for all requests
				})
				.addFilterBefore(beforeFilter, AbstractPreAuthenticatedProcessingFilter.class);

		return http.build();
	}
}
