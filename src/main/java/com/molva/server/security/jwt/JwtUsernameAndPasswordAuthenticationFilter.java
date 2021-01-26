package com.molva.server.security.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.molva.server.data.model.ApplicationUser;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.crypto.SecretKey;
import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class JwtUsernameAndPasswordAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

  private final AuthenticationManager authenticationManager;
  private final JwtConfig jwtConfig;
  private final JwtProvider jwtProvider;

  public JwtUsernameAndPasswordAuthenticationFilter(AuthenticationManager authenticationManager,
                                                    JwtConfig jwtConfig,
                                                    JwtProvider jwtProvider) {
    this.jwtConfig = jwtConfig;
    this.jwtProvider = jwtProvider;
    this.authenticationManager = authenticationManager;
  }

  @Override
  public Authentication attemptAuthentication(HttpServletRequest request,
                                              HttpServletResponse response) throws AuthenticationException {
    try {
      UsernameAndPasswordAuthenticationRequest authenticationRequest =
          new ObjectMapper().readValue(request.getInputStream(), UsernameAndPasswordAuthenticationRequest.class);
      Authentication authentication = jwtProvider.getUsernamePasswordAuthentication(
          authenticationRequest.getUsername(),
          authenticationRequest.getPassword());
      return authenticationManager.authenticate(authentication);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }


  @Override
  protected void successfulAuthentication(HttpServletRequest request,
                                          HttpServletResponse response,
                                          FilterChain chain,
                                          Authentication authResult) throws IOException {
    String token = jwtProvider.createToken(authResult.getName(), authResult.getAuthorities());
    ObjectMapper mapper = new ObjectMapper();
    ApplicationUser applicationUser = (ApplicationUser) authResult.getPrincipal();
    response.getWriter().write(mapper.writeValueAsString(applicationUser));
    response.addHeader(jwtConfig.getAuthorizationHeader(), jwtConfig.getTokenPrefix() + token);
  }
}
