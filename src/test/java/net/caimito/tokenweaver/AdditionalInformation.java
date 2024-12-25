package net.caimito.tokenweaver;

public class AdditionalInformation {

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
