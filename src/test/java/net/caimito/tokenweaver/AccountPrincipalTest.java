package net.caimito.tokenweaver;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
  void additionalInformation() {
    AccountPrincipal<AdditionalInformation> accountPrincipal = new AccountPrincipal<>("joe@example.com");

    accountPrincipal.setAdditionalInformation(new AdditionalInformation("value"));
    AdditionalInformation info = accountPrincipal.getAdditionalInformation();
    assertEquals("value", info.getSomeValue());
  }

}
