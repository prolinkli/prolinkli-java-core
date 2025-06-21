package com.prolinkli.framework.config.security;

import com.prolinkli.framework.jwt.http.JwtRequestValidator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true) // Enable @PreAuthorize and @PostAuthorize
public class SecurityConfig {

	@Autowired
	private JwtRequestValidator jwtRequestValidator;

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

		http
				.csrf(csrf -> csrf.disable()) // Disable CSRF for simplicity, adjust as needed
				.authorizeHttpRequests(auth -> {
					// Allow all requests by default - let method-level security handle authorization
					auth.anyRequest().permitAll();
				})
				.addFilterBefore(jwtRequestValidator, AbstractPreAuthenticatedProcessingFilter.class);

		return http.build();
	}
}
