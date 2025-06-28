package com.prolinkli.core.app.components.oauth2.controller;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.prolinkli.core.app.components.oauth2.service.AbstractOAuthService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import jakarta.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/auth/oauth2")
public class OAuth2Controller {

  private final Map<String, AbstractOAuthService> oauthServices;

  @Autowired
  public OAuth2Controller(List<AbstractOAuthService> oauthServices) {
    this.oauthServices = oauthServices.stream()
        .collect(Collectors.toMap(
            AbstractOAuthService::getProviderName,
            service -> service));
  }

  @RequestMapping("/{id}")
  public RedirectView oauthLogin(@PathVariable("id") String id, HttpServletRequest response) {

    AbstractOAuthService svc = getOAuth2Service(id);

    return new RedirectView(
        svc.getRedirectUrl());

  }

  @RequestMapping("/{id}/callback")
  public ModelAndView oauthCallback(
      @PathVariable("id") String id,
      @RequestParam(value = "code", required = false) String code,
      @RequestParam(value = "state", required = false) String state,
      HttpServletRequest request) {

    AbstractOAuthService svc = getOAuth2Service(id);

    if (code == null || code.isEmpty()) {
      throw new IllegalArgumentException("Missing code parameter in callback");
    }

    Map<String, Object> params = Map.of(
        "code", code,
        "state", state != null ? state : "");

    String forward = svc.handleCallback(
        params,
        request);

    // Create a POST redirect using an auto-submitting form
    ModelAndView modelAndView = new ModelAndView("postRedirect");
    modelAndView.addObject("redirectUrl", forward);
    modelAndView.addObject("idToken", request.getAttribute("idToken"));
    modelAndView.addObject("provider", svc.getProviderName());

    return modelAndView;
  }

  private AbstractOAuthService getOAuth2Service(String id) {
    AbstractOAuthService svc = oauthServices.getOrDefault(id, null);
    if (svc == null) {
      throw new IllegalArgumentException("OAuth2 provider not found: " + id);
    }
    return svc;
  }

}
