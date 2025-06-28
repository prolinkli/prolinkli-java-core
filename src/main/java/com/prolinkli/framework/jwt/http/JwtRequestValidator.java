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

import com.prolinkli.framework.jwt.model.AuthToken;
import com.prolinkli.framework.jwt.service.JwtVerifyService;
import com.prolinkli.framework.jwt.util.JwtUtil;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtRequestValidator extends OncePerRequestFilter {

  @Autowired
  private JwtVerifyService jwtVerifyService;

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    AuthToken authToken = JwtUtil.extractJwtFromCookies(request);
    if (authToken == null) {
      // If no token is found, continue the filter chain without authentication
      filterChain.doFilter(request, response);
      return;
    }
    String token = authToken.getAccessToken();

    if (token != null && jwtVerifyService.verifyToken(token, response)) {
      // Extract user information from JWT
      Long userId = jwtVerifyService.extractUserId(token);
      List<String> authorities = jwtVerifyService.extractAuthorities(token);

      // Convert authorities to Spring Security format
      List<GrantedAuthority> grantedAuthorities = authorities.stream()
          .map(auth -> new SimpleGrantedAuthority("ROLE_" + auth))
          .collect(Collectors.toList());

      // Create authentication token with user details
      Authentication authentication = new UsernamePasswordAuthenticationToken(
          userId, // principal - user ID from JWT
          authToken, // credentials - not used here
          grantedAuthorities // authorities from JWT
      );

      // Set authentication in security context
      SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    filterChain.doFilter(request, response);
  }

}
