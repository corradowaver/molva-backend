package com.molva.server.security.jwt;

import com.google.common.base.Strings;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.crypto.SecretKey;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;

public class JwtTokenVerifier extends OncePerRequestFilter {

  private final SecretKey secretKey;
  private final JwtProvider jwtProvider;

  public JwtTokenVerifier(SecretKey secretKey,
                          JwtProvider jwtProvider) {
    this.secretKey = secretKey;
    this.jwtProvider = jwtProvider;
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request,
                                  HttpServletResponse response,
                                  FilterChain filterChain) throws ServletException, IOException {
    String token = jwtProvider.resolveToken(request);
    if (token == null) {
      filterChain.doFilter(request, response);
      return;
    }
    validateToken(token);
    Authentication authentication = jwtProvider.getTokenAuthentication(token);
    SecurityContextHolder.getContext().setAuthentication(authentication);
    filterChain.doFilter(request, response);
  }

  public void validateToken(String token) {
    try {
      Jws<Claims> claims = Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token);
      if (!claims.getBody().getExpiration().before(new Date())) {
        throw new IllegalStateException("Expired token");
      }
    } catch (JwtException | IllegalArgumentException e) {
      throw new IllegalStateException("Invalid token");
    }
  }
}
