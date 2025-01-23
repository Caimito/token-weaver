package net.caimito.tokenweaver;

import java.util.Locale;

public class PersonName {
  private Locale locale; // Locale for regional context
  private String firstName; // Given name
  private String middleName; // Middle name
  private String familyName; // Family or surname
  private String secondFamilyName; // Second family name (if applicable)
  private String patronymic; // Patronymic (e.g., Russia/Iceland)
  private String matronymic; // Matronymic (optional in some cultures)
  private String oriki; // Praise name (Yoruba culture)
  private String binOrBinti; // Bin/Binti for Arabic names
  private String additionalNames; // Other unconventional naming elements

  @SuppressWarnings("unused")
  private PersonName() {
  }

  private PersonName(Builder builder) {
    this.locale = builder.locale;
    this.firstName = builder.firstName;
    this.middleName = builder.middleName;
    this.familyName = builder.familyName;
    this.secondFamilyName = builder.secondFamilyName;
    this.patronymic = builder.patronymic;
    this.matronymic = builder.matronymic;
    this.oriki = builder.oriki;
    this.binOrBinti = builder.binOrBinti;
    this.additionalNames = builder.additionalNames;
  }

  public String getFirstName() {
    return firstName;
  }

  public String getMiddleName() {
    return middleName;
  }

  public String getFamilyName() {
    return familyName;
  }

  public String getSecondFamilyName() {
    return secondFamilyName;
  }

  public String getPatronymic() {
    return patronymic;
  }

  public String getMatronymic() {
    return matronymic;
  }

  public String getOriki() {
    return oriki;
  }

  public String getBinOrBinti() {
    return binOrBinti;
  }

  public String getAdditionalNames() {
    return additionalNames;
  }

  public Locale getLocale() {
    return locale;
  }

  public String getFormattedName() {
    // Combine fields into a culturally appropriate full name
    StringBuilder nameBuilder = new StringBuilder();
    if (locale != null) {
      String country = locale.getCountry();
      switch (country) {
        case "ES": // Spain
        case "PT": // Portugal
          nameBuilder.append(firstName).append(" ");
          if (middleName != null)
            nameBuilder.append(middleName).append(" ");
          nameBuilder.append(familyName).append(" ");
          if (secondFamilyName != null)
            nameBuilder.append(secondFamilyName);
          break;
        case "RU": // Russia
          nameBuilder.append(firstName).append(" ");
          if (patronymic != null)
            nameBuilder.append(patronymic).append(" ");
          nameBuilder.append(familyName);
          break;
        case "JP": // Japan
        case "CN": // China
        case "KR": // Korea
          nameBuilder.append(familyName).append(" ").append(firstName);
          break;
        default: // Default: Western
          nameBuilder.append(firstName).append(" ");
          if (middleName != null)
            nameBuilder.append(middleName).append(" ");
          if (familyName != null)
            nameBuilder.append(familyName);
          break;
      }
    }
    return nameBuilder.toString().trim();
  }

  @Override
  public String toString() {
    return getFormattedName();
  }

  public static class Builder {
    private final Locale locale;
    private final String firstName;
    private String middleName;
    private String familyName;
    private String secondFamilyName;
    private String patronymic;
    private String matronymic;
    private String oriki;
    private String binOrBinti;
    private String additionalNames;

    public Builder(Locale locale, String firstName) {
      this.locale = locale;
      this.firstName = firstName;
    }

    public Builder withMiddleName(String middleName) {
      this.middleName = middleName;
      return this;
    }

    public Builder withFamilyName(String familyName) {
      this.familyName = familyName;
      return this;
    }

    public Builder withSecondFamilyName(String secondFamilyName) {
      this.secondFamilyName = secondFamilyName;
      return this;
    }

    public Builder withPatronymic(String patronymic) {
      this.patronymic = patronymic;
      return this;
    }

    public Builder withMatronymic(String matronymic) {
      this.matronymic = matronymic;
      return this;
    }

    public Builder withOriki(String oriki) {
      this.oriki = oriki;
      return this;
    }

    public Builder withBinOrBinti(String binOrBinti) {
      this.binOrBinti = binOrBinti;
      return this;
    }

    public Builder withAdditionalNames(String additionalNames) {
      this.additionalNames = additionalNames;
      return this;
    }

    public PersonName build() {
      return new PersonName(this);
    }
  }
}
