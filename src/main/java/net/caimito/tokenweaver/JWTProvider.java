package net.caimito.tokenweaver;

import java.time.Instant;
import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;

@Component
public class JWTProvider {
  @Value("${token-weaver.token.expire-days}")
  private int expirationDays;

  public final long JWT_EXPIRATION = 86400 * 1000 * expirationDays;

  @Value("${token-weaver.token.name}")
  public final String ACCESS_TOKEN_NAME = "access_token";

  private final Algorithm algorithm;

  public JWTProvider(@Value("${token-weaver.token.secret}") String secret) {
    this.algorithm = Algorithm.HMAC256(secret);
  }

  public String generateAccessToken(String username) {
    return JWT.create()
        .withSubject(username)
        .withIssuedAt(new Date())
        .withExpiresAt(Date.from(Instant.now().plusSeconds(JWT_EXPIRATION)))
        .sign(algorithm);
  }

  public String generateRefreshToken(String username) {
    return JWT.create()
        .withSubject(username)
        .withClaim("type", "refresh")
        .withIssuedAt(new Date())
        .withExpiresAt(Date.from(Instant.now().plusSeconds(JWT_EXPIRATION)))
        .sign(algorithm);
  }

  public boolean validateToken(String token) {
    try {
      JWT.require(algorithm).build().verify(token);
      return true;
    } catch (Exception e) {
      return false;
    }
  }

  public String getUsernameFromToken(String token) {
    DecodedJWT decoded = JWT.require(algorithm).build().verify(token);
    return decoded.getSubject();
  }

  public long getRemainingValidity(String token) {
    DecodedJWT decoded = JWT.require(algorithm).build().verify(token);
    Date exp = decoded.getExpiresAt();
    long diff = exp.getTime() - System.currentTimeMillis();
    return diff / 1000; // Convert milliseconds to seconds
  }

}