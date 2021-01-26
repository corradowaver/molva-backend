package com.molva.server.security.jwt;

import com.molva.server.data.model.ApplicationUser;
import com.molva.server.data.service.ApplicationUserService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Date;

@Component
public class JwtProvider {

  JwtConfig jwtConfig;
  SecretKey secretKey;
  ApplicationUserService applicationUserService;

  public JwtProvider(JwtConfig jwtConfig, SecretKey secretKey, ApplicationUserService applicationUserService) {
    this.jwtConfig = jwtConfig;
    this.secretKey = secretKey;
    this.applicationUserService = applicationUserService;
  }

  public String createToken(String username, Collection<? extends GrantedAuthority> authorities) {
    return Jwts.builder()
        .setSubject(username)
        .claim("authorities", authorities)
        .setIssuedAt(new Date())
        .setExpiration(java.sql.Date.valueOf(LocalDate.now().plusDays(jwtConfig.getTokenExpirationAfterDays())))
        .signWith(secretKey)
        .compact();
  }

  public String getUsername(String token) {
    return Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token).getBody().getSubject();
  }

  public String resolveToken(HttpServletRequest request) {
    String header = request.getHeader(jwtConfig.getAuthorizationHeader());
    if (header != null && header.startsWith(jwtConfig.getTokenPrefix())) {
      return header.substring(jwtConfig.getTokenPrefix().length());
    }
    return null;
  }

  public String resolveToken(String header) {
    if (header != null && header.startsWith(jwtConfig.getTokenPrefix())) {
      return header.substring(jwtConfig.getTokenPrefix().length());
    }
    return null;
  }

  public Authentication getTokenAuthentication(String token) {
    UserDetails userDetails = applicationUserService.loadUserByUsername(getUsername(token));
    return new UsernamePasswordAuthenticationToken(
        userDetails,
        null,
        userDetails.getAuthorities()
    );
  }

  public Authentication getUsernamePasswordAuthentication(String username, String password) {
    return new UsernamePasswordAuthenticationToken(
        username,
        password
    );
  }
}
