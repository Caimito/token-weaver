package net.caimito.tokenweaver;

import java.time.LocalDateTime;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "accountPrincipals")
public class AccountPrincipal<T> {
  private static final Logger LOGGER = LoggerFactory.getLogger(AccountPrincipal.class);

  @Id
  private String id;

  @Indexed
  private String email;

  @Indexed
  private String magicId;

  private LocalDateTime magicIdCreated;
  private LocalDateTime emailVerified;

  private T additionalInformation;

  @SuppressWarnings("unused")
  private AccountPrincipal() {
  }

  public AccountPrincipal(String email) {
    this.email = email;
    generateMagicId();
  }

  public String getId() {
    return id;
  }

  public String getEmail() {
    return email;
  }

  public void generateMagicId() {
    LOGGER.debug("Generating magic ID for {}", email);
    this.magicId = UUID.randomUUID().toString();
    this.magicIdCreated = LocalDateTime.now();
  }

  public String getMagicId() {
    return magicId;
  }

  public LocalDateTime getMagicIdCreated() {
    return magicIdCreated;
  }

  public void backdateMagicId(LocalDateTime now) {
    this.magicIdCreated = now;
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

  public T getAdditionalInformation() {
    return additionalInformation;
  }

  public void setAdditionalInformation(T additionalInformation) {
    this.additionalInformation = additionalInformation;
  }

}