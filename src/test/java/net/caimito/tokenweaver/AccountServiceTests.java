package net.caimito.tokenweaver;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.MongoDBContainer;

@SpringBootTest
public class AccountServiceTests {

  @ServiceConnection
  static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:7.0");

  @Autowired
  private AccountService accountService;

  @Autowired
  private AccountPrincipalRepository accountPrincipalRepository;

  @BeforeEach
  void setUp() {
    accountPrincipalRepository.deleteAll();
  }

  @Test
  void createTimeLockedAccount() {
    AccountPrincipal<?> accountPrincipal = accountService.createAccount("joe@example.com", Locale.US,
        new AdditionalInformation());

    assertThat(accountPrincipalRepository.count()).isEqualTo(1);
    assertThat(accountPrincipal)
        .isNotNull()
        .satisfies(ap -> {
          assertThat(ap.getEmail()).isEqualTo("joe@example.com");
          assertThat(ap.getEmailVerified()).isNull();
          assertThat(ap.isEmailVerified()).isFalse();
          assertThat(ap.getMagicId()).isNotNull();
          assertThat(ap.getMagicIdCreated()).isBefore(LocalDateTime.now());
        });
  }

  @Test
  void emailAlreadyExists() {
    accountService.createAccount("joe@example.com", Locale.US,
        new AdditionalInformation());

    assertThatThrownBy(() -> accountService.createAccount("joe@example.com", Locale.US,
        new AdditionalInformation()))
        .isInstanceOf(AccountAlreadyExistsException.class);
  }

  @Test
  void unknownAccount() {
    Optional<AccountPrincipal<AdditionalInformation>> foundAccountPrincipal = accountService
        .findAccountByEmail("unknown@example.com");
    assertThat(foundAccountPrincipal)
        .isEmpty();
  }

  @Test
  void findAccount() {
    accountService.createAccount("joe@example.com", Locale.US,
        new AdditionalInformation());

    Optional<AccountPrincipal<AdditionalInformation>> foundAccountPrincipal = accountService
        .findAccountByEmail("joe@example.com");
    assertThat(foundAccountPrincipal)
        .isPresent()
        .get()
        .satisfies(ap -> {
          assertThat(ap.getEmail()).isEqualTo("joe@example.com");
          assertThat(ap.getMagicId()).isNotNull();
          assertThat(ap.getMagicIdCreated()).isBefore(LocalDateTime.now());
          assertThat(ap.getAdditionalInformation())
              .isNotNull()
              .isInstanceOf(AdditionalInformation.class);
        });
  }

  @Test
  void findAccountById() {
    AccountPrincipal<AdditionalInformation> accountSaved = accountService.createAccount("joe@example.com",
        Locale.US, new AdditionalInformation());

    Optional<AccountPrincipal<AdditionalInformation>> foundAccountPrincipal = accountService
        .findAccountById(accountSaved.getId());
    assertThat(foundAccountPrincipal)
        .isPresent()
        .get()
        .satisfies(ap -> {
          assertThat(ap.getEmail()).isEqualTo("joe@example.com");
          assertThat(ap.getMagicId()).isNotNull();
          assertThat(ap.getMagicIdCreated()).isBefore(LocalDateTime.now());
          assertThat(ap.getAdditionalInformation())
              .isNotNull()
              .isInstanceOf(AdditionalInformation.class);
        });
  }

  @Test
  void expiredMagicIdVerifyEmail() {
    LocalDateTime now = LocalDateTime.now().plusDays(10);
    AccountPrincipal<?> accountPrincipal = accountService.createAccount("joe@example.com", Locale.US,
        new AdditionalInformation());

    assertThatThrownBy(() -> accountService.verifyEmail(accountPrincipal.getMagicId(), now))
        .isInstanceOf(MagicIdExpiredException.class);
  }

  @Test
  void verifyEmail() {
    LocalDateTime now = LocalDateTime.now().plusHours(2);
    AccountPrincipal<?> accountPrincipal = accountService.createAccount("joe@example.com", Locale.US,
        new AdditionalInformation());

    accountService.verifyEmail(accountPrincipal.getMagicId(), now);

    Optional<AccountPrincipal<AdditionalInformation>> foundAccountPrincipal = accountService
        .findAccountByEmail("joe@example.com");
    assertThat(foundAccountPrincipal)
        .isPresent()
        .get()
        .satisfies(ap -> {
          assertThat(ap.isEmailVerified()).isTrue();
          assertThat(ap.getEmailVerified()).isNotNull();
        });
  }

  @Test
  void verifyEmailAndReceiveAccessToken() {
    LocalDateTime now = LocalDateTime.now().plusHours(2);
    AccountPrincipal<?> accountPrincipal = accountService.createAccount("joe@example.com", Locale.US,
        new AdditionalInformation());

    assertThat(accountService.verifyEmail(accountPrincipal.getMagicId(), now))
        .isNotNull();
  }

  @Test
  void deliverMagicId() {
    AccountPrincipal<?> accountPrincipal = accountService.createAccount("joe@example.com", Locale.US,
        new AdditionalInformation());

    accountService.sendMagicLink(accountPrincipal, new MagicLinkSender() {
      @Override
      public void deliver(String email, String magicId) {
        assertThat(email).isEqualTo("joe@example.com");
        assertThat(magicId).isEqualTo(accountPrincipal.getMagicId());
      }
    });
  }

  @Test
  void lockedAccountCannotCreateJWT() {
    AccountPrincipal<?> accountPrincipal = accountService.createAccount("joe@example.com", Locale.US,
        new AdditionalInformation());

    assertThatThrownBy(() -> accountService.generateAccessToken(accountPrincipal))
        .isInstanceOf(AccountNotVerifiedException.class);
  }

  @Test
  void createJWT() {
    AccountPrincipal<?> accountPrincipal = accountService.createAccount("joe@example.com", Locale.US,
        new AdditionalInformation());
    accountService.verifyEmail(accountPrincipal.getMagicId(), LocalDateTime.now());

    AccessToken token = accountService.generateAccessToken(accountService.findAccountByEmail("joe@example.com").get());
    assertThat(token)
        .isNotNull();
  }

  @Test
  void storeReadAdditionalInformation() {
    AccountPrincipal<AdditionalInformation> accountPrincipal = accountService.createAccount("joe@example.com",
        Locale.US,
        new AdditionalInformation());

    assertThat(accountPrincipal.getAdditionalInformation()).isNotNull();

    accountPrincipal.getAdditionalInformation().setSomeValue("value");
    accountPrincipalRepository.save(accountPrincipal);

    Optional<AccountPrincipal<AdditionalInformation>> foundAccountPrincipal = accountService
        .findAccountByEmail("joe@example.com");
    assertThat(foundAccountPrincipal)
        .isPresent()
        .get()
        .satisfies(ap -> {
          assertThat(ap.getAdditionalInformation().getSomeValue()).isEqualTo("value");
        });
  }

  @Test
  void updateAccount() {
    AccountPrincipal<AdditionalInformation> accountPrincipal = accountService.createAccount("joe@example.com",
        Locale.US,
        new AdditionalInformation());

    accountPrincipal.getAdditionalInformation().setSomeValue("value");
    accountPrincipalRepository.save(accountPrincipal);

    Optional<AccountPrincipal<AdditionalInformation>> foundAccountPrincipal = accountService
        .findAccountByEmail("joe@example.com");
    assertThat(foundAccountPrincipal)
        .isPresent()
        .get()
        .satisfies(ap -> {
          assertThat(ap.getAdditionalInformation().getSomeValue()).isEqualTo("value");
        });

    accountPrincipal.getAdditionalInformation().setSomeValue("new value");
    accountService.updateAccount(accountPrincipal);

    foundAccountPrincipal = accountService.findAccountByEmail("joe@example.com");
    assertThat(foundAccountPrincipal)
        .isPresent()
        .get()
        .satisfies(ap -> {
          assertThat(ap.getAdditionalInformation().getSomeValue()).isEqualTo("new value");
        });
  }

}
