package net.caimito.tokenweaver;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "accountPrincipals")
public class AccountPrincipal {

  @Id
  private String id;

  @Indexed
  private String email;

  @Indexed
  private String magicId;

  private LocalDateTime magicIdCreated;
  private LocalDateTime emailVerified;

  @SuppressWarnings("unused")
  private AccountPrincipal() {
  }

  public AccountPrincipal(String email) {
    this.email = email;
    this.magicId = UUID.randomUUID().toString();
    this.magicIdCreated = LocalDateTime.now();
  }

  public String getId() {
    return id;
  }

  public String getEmail() {
    return email;
  }

  public String getMagicId() {
    return magicId;
  }

  public LocalDateTime getMagicIdCreated() {
    return magicIdCreated;
  }

  public LocalDateTime getEmailVerified() {
    return emailVerified;
  }

  public void setEmailVerified(LocalDateTime emailVerified) {
    this.emailVerified = emailVerified;
  }

  public boolean isEmailVerified() {
    return emailVerified != null;
  }

}