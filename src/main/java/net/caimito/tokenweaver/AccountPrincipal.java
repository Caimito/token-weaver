package net.caimito.tokenweaver;

import java.time.LocalDateTime;
import java.util.Locale;
import java.util.UUID;

import org.apache.commons.lang3.builder.ToStringBuilder;
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

  private Locale locale;
  private PersonName personName;

  private T additionalInformation;

  @SuppressWarnings("unused")
  private AccountPrincipal() {
  }

  private AccountPrincipal(Builder<T> builder) {
    this.email = builder.email;
    this.locale = builder.locale;
    this.personName = builder.personName;
    this.additionalInformation = builder.additionalInformation;
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

  public PersonName getPersonName() {
    return personName;
  }

  public Locale getLocale() {
    return locale;
  }

  public static class Builder<T> {
    private final String email;
    private final Locale locale;
    private PersonName personName;
    private T additionalInformation;

    public Builder(String email, Locale locale) {
      this.email = email;
      this.locale = locale;
    }

    public Builder<T> withPersonName(PersonName personName) {
      this.personName = personName;
      return this;
    }

    public Builder<T> withAdditionalInformation(T additionalInformation) {
      this.additionalInformation = additionalInformation;
      return this;
    }

    public AccountPrincipal<T> build() {
      AccountPrincipal<T> principal = new AccountPrincipal<>(this);
      return principal;
    }
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this);
  }

}