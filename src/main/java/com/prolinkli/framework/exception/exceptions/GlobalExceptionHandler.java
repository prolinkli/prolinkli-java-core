package com.prolinkli.framework.exception.exceptions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

import com.prolinkli.core.app.Constants;
import com.prolinkli.framework.exception.exceptions.model.AuthenticationFailedException;
import com.prolinkli.framework.exception.exceptions.model.InvalidCredentialsException;
import com.prolinkli.framework.exception.exceptions.model.ResourceAlreadyExists;
import com.prolinkli.framework.exception.exceptions.model.ResourceNotFoundException;
import com.prolinkli.framework.exception.response.model.ErrorResponse;
import com.prolinkli.framework.jwt.model.JWTTokenExpiredException;

import jakarta.servlet.http.HttpServletRequest;
import java.io.PrintWriter;
import java.io.StringWriter;

@RestControllerAdvice
public class GlobalExceptionHandler {
  private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

  @Value("${app.debug:false}")
  private boolean debug;

  @ExceptionHandler(ResourceNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex,
      HttpServletRequest req) {
    log.info("Resource not found: {}", ex.getMessage());
    ErrorResponse body = new ErrorResponse(
        HttpStatus.NOT_FOUND.value(),
        HttpStatus.NOT_FOUND.getReasonPhrase(),
        ex.getMessage(),
        req.getRequestURI());
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
  }

  @ExceptionHandler(ResourceAlreadyExists.class)
  public ResponseEntity<ErrorResponse> handleResourceAlreadyExists(ResourceAlreadyExists ex,
      HttpServletRequest req) {
    log.info("Resource already exists: {}", ex.getMessage());
    ErrorResponse body = new ErrorResponse(
        HttpStatus.CONFLICT.value(),
        HttpStatus.CONFLICT.getReasonPhrase(),
        ex.getMessage(),
        req.getRequestURI());
    return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
  }

  @ExceptionHandler({ IllegalArgumentException.class, IllegalStateException.class })
  public ResponseEntity<ErrorResponse> handleBadRequest(RuntimeException ex,
      HttpServletRequest req) {
    log.info("Illegal argument or state: {}", ex.getMessage(), ex);
    log.info("Bad request: {}", ex.getMessage());
    ErrorResponse body = new ErrorResponse(
        HttpStatus.BAD_REQUEST.value(),
        HttpStatus.BAD_REQUEST.getReasonPhrase(),
        ex.getMessage(),
        req.getRequestURI());
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
  }

  @ExceptionHandler({ AuthenticationFailedException.class, InvalidCredentialsException.class })
  public ResponseEntity<ErrorResponse> handleAuthenticationFailed(RuntimeException ex,
      HttpServletRequest req) {
    log.info("Authentication failed: {}", ex.getMessage());
    ErrorResponse body = new ErrorResponse(
        HttpStatus.UNAUTHORIZED.value(),
        HttpStatus.UNAUTHORIZED.getReasonPhrase(),
        ex.getMessage(),
        req.getRequestURI());
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body);
  }

  @ExceptionHandler({ AuthorizationDeniedException.class })
  public ResponseEntity<ErrorResponse> handleAuthorizationDenied(AuthorizationDeniedException ex,
      HttpServletRequest req) {
    log.info("Authorization denied: {}", ex.getMessage());
    ErrorResponse body = new ErrorResponse(
        HttpStatus.FORBIDDEN.value(),
        HttpStatus.FORBIDDEN.getReasonPhrase(),
        ex.getMessage(),
        req.getRequestURI());
    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(body);
  }

  @ExceptionHandler({ JWTTokenExpiredException.class })
  public ResponseEntity<ErrorResponse> handleJWTTokenExpired(JWTTokenExpiredException ex,
      HttpServletRequest req) {
    log.info("JWT token expired: {}", ex.getMessage());
    ErrorResponse body = new ErrorResponse(
        Constants.HttpStatuses.PageExpired.CODE,
        Constants.HttpStatuses.PageExpired.REASON,
        ex.getMessage(),
        req.getRequestURI());
    return ResponseEntity.status(Constants.HttpStatuses.PageExpired.CODE).body(body);
  }

  @ExceptionHandler(NoHandlerFoundException.class)
  public ResponseEntity<ErrorResponse> handleNoRoute(NoHandlerFoundException ex,
      HttpServletRequest req) {
    log.info("No endpoint {} {}", ex.getHttpMethod(), ex.getRequestURL());
    ErrorResponse body = new ErrorResponse(
        HttpStatus.NOT_FOUND.value(),
        HttpStatus.NOT_FOUND.getReasonPhrase(),
        String.format("No endpoint %s %s", ex.getHttpMethod(), ex.getRequestURL()),
        req.getRequestURI());
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleAll(Exception ex, HttpServletRequest req) {
    log.error("Unexpected error", ex);
    String clientMsg = debug
        ? ex.getMessage()
        : "An unexpected error occurred. Please try again later.";

    ErrorResponse body = new ErrorResponse(
        HttpStatus.INTERNAL_SERVER_ERROR.value(),
        HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
        clientMsg,
        req.getRequestURI());
    if (debug) {
      StringWriter sw = new StringWriter();
      ex.printStackTrace(new PrintWriter(sw));
      body.setDebugMessage(sw.toString());
    }
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
  }
}
