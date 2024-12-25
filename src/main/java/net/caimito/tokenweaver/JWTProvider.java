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
  @Value("${token-weaver.token.expire-days:5}")
  private int expirationDays;

  public long JWT_EXPIRATION = 86400 * expirationDays;

  @Value("${token-weaver.token.name:access_token}")
  public String ACCESS_TOKEN_NAME;

  private final Algorithm algorithm;

  public JWTProvider(@Value("${token-weaver.token.secret}") String secret) {
    this.algorithm = Algorithm.HMAC256(secret);
  }

  public AccessToken generateAccessToken(String username) {
    String jwt = JWT.create()
        .withSubject(username)
        .withIssuedAt(new Date())
        .withExpiresAt(Date.from(Instant.now().plusSeconds(JWT_EXPIRATION)))
        .sign(algorithm);

    return new AccessToken(jwt, ACCESS_TOKEN_NAME, JWT_EXPIRATION);
  }

  public AccessToken generateRefreshToken(String username) {
    String jwt = JWT.create()
        .withSubject(username)
        .withClaim("type", "refresh")
        .withIssuedAt(new Date())
        .withExpiresAt(Date.from(Instant.now().plusSeconds(JWT_EXPIRATION)))
        .sign(algorithm);

    return new AccessToken(jwt, ACCESS_TOKEN_NAME, JWT_EXPIRATION);
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