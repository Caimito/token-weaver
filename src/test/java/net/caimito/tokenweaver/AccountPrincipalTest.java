package net.caimito.tokenweaver;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Locale;

import org.junit.jupiter.api.Test;

public class AccountPrincipalTest {

  class AdditionalInformation {
    private String someValue;

    public AdditionalInformation() {
      // Default constructor for deserialization
    }

    public AdditionalInformation(String someValue) {
      this.someValue = someValue;
    }

    public String getSomeValue() {
      return someValue;
    }
  }

  @Test
  void build() {
    PersonName personName = new PersonName.Builder(new Locale("es", "ES"), "María")
        .withMiddleName("Ana")
        .withFamilyName("García")
        .withSecondFamilyName("López")
        .build();

    AdditionalInformation additionalInformation = new AdditionalInformation("Extra info");

    AccountPrincipal<AdditionalInformation> account = new AccountPrincipal.Builder<AdditionalInformation>(
        "test@example.com",
        new Locale("es", "ES"))
        .withPersonName(personName)
        .withAdditionalInformation(additionalInformation)
        .build();

    assertThat(account.getEmail()).isEqualTo("test@example.com");
    assertThat(account.getLocale()).isEqualTo(new Locale("es", "ES"));
    assertThat(account.getPersonName().toString()).isEqualTo("María Ana García López");
    assertThat(account.getAdditionalInformation().getSomeValue()).isEqualTo("Extra info");
  }

  @Test
  void emptyPersonName() {
    AccountPrincipal<AdditionalInformation> account = new AccountPrincipal.Builder<AdditionalInformation>(
        "test@example.com",
        new Locale("es", "ES"))
        .build();

    assertThat(account.getPersonName()).isNotNull();
  }

  @Test
  void changePersonalName() {
    AccountPrincipal<AdditionalInformation> account = new AccountPrincipal.Builder<AdditionalInformation>(
        "test@example.com",
        new Locale("es", "ES"))
        .build();

    PersonName personName = new PersonName.Builder(new Locale("es", "ES"), "María")
        .withMiddleName("Ana")
        .withFamilyName("García")
        .withSecondFamilyName("López")
        .build();

    account.setPersonName(personName);

    assertThat(account.getPersonName().getFormattedName()).isEqualTo("María Ana García López");
  }

}
