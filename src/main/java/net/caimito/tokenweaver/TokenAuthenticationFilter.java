package net.caimito.tokenweaver;

import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class TokenAuthenticationFilter extends OncePerRequestFilter {
  private static final Logger LOGGER = LoggerFactory.getLogger(TokenAuthenticationFilter.class);

  @Autowired
  private JWTProvider jwtProvider;

  @Autowired
  private AccountPrincipalRepository accountPrincipalRepository;

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
            accountPrincipalRepository.findById(username).ifPresent(accountPrincipal -> {
              UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                  accountPrincipal.getId(),
                  null,
                  List.of(new SimpleGrantedAuthority("ROLE_USER")));
              // TODO make role configurable

              SecurityContextHolder.getContext().setAuthentication(authToken);
              LOGGER.debug("Authenticated {} with authorities {}", authToken.getPrincipal(),
                  authToken.getAuthorities());
            });
          }
        }
      }
    }

    filterChain.doFilter(request, response);
  }
}