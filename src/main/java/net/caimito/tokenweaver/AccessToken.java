package net.caimito.tokenweaver;

public class AccessToken {

  private String tokenValue;
  private String tokenName;
  private long expiresInSeconds;

  public AccessToken(String tokenValue, String tokenName, long expiresInSeconds) {
    this.tokenValue = tokenValue;
    this.tokenName = tokenName;
    this.expiresInSeconds = expiresInSeconds;
  }

  public String getTokenValue() {
    return tokenValue;
  }

  public String getTokenName() {
    return tokenName;
  }

  public long getExpiresInSeconds() {
    return expiresInSeconds;
  }

}
