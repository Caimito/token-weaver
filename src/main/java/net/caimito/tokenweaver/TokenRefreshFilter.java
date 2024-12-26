package net.caimito.tokenweaver;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class TokenRefreshFilter extends OncePerRequestFilter {
  private static final Logger LOGGER = LoggerFactory.getLogger(TokenRefreshFilter.class);

  @Autowired
  private JWTProvider jwtProvider;

  @Override
  protected void doFilterInternal(HttpServletRequest request,
      HttpServletResponse response,
      FilterChain filterChain) throws ServletException, IOException {
    Cookie[] cookies = request.getCookies();
    if (cookies != null) {
      for (Cookie cookie : cookies) {
        if (jwtProvider.getAccessTokenName().equals(cookie.getName())) {
          String token = cookie.getValue();
          if (jwtProvider.validateToken(token)) {
            String username = jwtProvider.getUsernameFromToken(token);
            AccessToken newToken = jwtProvider.generateAccessToken(username);
            // TODO: Probably smarter to refresh once per day instead of with every request
            ResponseCookie refreshedCookie = ResponseCookie
                .from(newToken.getTokenName(), newToken.getTokenValue())
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(newToken.getExpiresInSeconds())
                .build();
            response.setHeader(HttpHeaders.SET_COOKIE, refreshedCookie.toString());
            LOGGER.debug("Refreshed token for {}", username);
          } else {
            LOGGER.warn("Invalid token {}", token);
          }
        }
      }
    }

    filterChain.doFilter(request, response);
  }
}