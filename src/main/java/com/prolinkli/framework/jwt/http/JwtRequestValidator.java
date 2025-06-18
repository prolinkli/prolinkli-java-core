package com.prolinkli.framework.jwt.http;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.prolinkli.framework.jwt.service.JwtVerifyService;
import com.prolinkli.core.app.Constants;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtRequestValidator extends OncePerRequestFilter {

	@Autowired
	private JwtVerifyService jwtVerifyService;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {

		String token = extractJwtFromCookies(request);

		if (token != null && jwtVerifyService.verifyToken(token, response)) {
			// Extract user information from JWT
			String userId = jwtVerifyService.extractUserId(token);
			List<String> authorities = jwtVerifyService.extractAuthorities(token);

			// Convert authorities to Spring Security format
			List<GrantedAuthority> grantedAuthorities = authorities.stream()
					.map(auth -> new SimpleGrantedAuthority("ROLE_" + auth))
					.collect(Collectors.toList());

			// Create authentication token with user details
			Authentication authentication = new UsernamePasswordAuthenticationToken(
					userId, // principal - user ID from JWT
					null, // credentials
					grantedAuthorities // authorities from JWT
			);

			// Set authentication in security context
			SecurityContextHolder.getContext().setAuthentication(authentication);
		}

		filterChain.doFilter(request, response);
	}

	private String extractJwtFromCookies(HttpServletRequest request) {
		Cookie[] cookies = request.getCookies();
		if (cookies != null) {
			for (Cookie cookie : cookies) {
				// Look for access token cookie using the constant
				if (Constants.Cookies.Authentication.ACCESS_TOKEN.equals(cookie.getName())) {
					return cookie.getValue();
				}
			}
		}
		return null;
	}
}
