package net.caimito.tokenweaver;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Locale;

import org.junit.jupiter.api.Test;

public class PersonNameTest {

  @Test
  void onlyFirstName() {
    PersonName personName = new PersonName.Builder(Locale.US, "Joe").build();
    assertThat(personName).isNotNull();
    assertThat(personName.getFirstName()).isEqualTo("Joe");

    assertThat(personName.getFormattedName()).isEqualTo("Joe");
  }

}
