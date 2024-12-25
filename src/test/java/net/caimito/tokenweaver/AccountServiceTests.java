package net.caimito.tokenweaver;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDateTime;
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
    AccountPrincipal<?> accountPrincipal = accountService.createAccount("joe@example.com", AdditionalInformation.class);

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
    accountService.createAccount("joe@example.com", AdditionalInformation.class);

    assertThatThrownBy(() -> accountService.createAccount("joe@example.com", AdditionalInformation.class))
        .isInstanceOf(AccountAlreadyExistsException.class);
  }

  @Test
  void unknownAccount() {
    Optional<AccountPrincipal<AdditionalInformation>> foundAccountPrincipal = accountService
        .findAccount("unknown@example.com");
    assertThat(foundAccountPrincipal)
        .isEmpty();
  }

  @Test
  void findAccount() {
    accountService.createAccount("joe@example.com", AdditionalInformation.class);

    Optional<AccountPrincipal<AdditionalInformation>> foundAccountPrincipal = accountService
        .findAccount("joe@example.com");
    assertThat(foundAccountPrincipal)
        .isPresent()
        .get()
        .satisfies(ap -> {
          assertThat(ap.getEmail()).isEqualTo("joe@example.com");
          assertThat(ap.getMagicId()).isNotNull();
          assertThat(ap.getMagicIdCreated()).isBefore(LocalDateTime.now());
        });
  }

  @Test
  void expiredMagicIdVerifyEmail() {
    LocalDateTime now = LocalDateTime.now().plusDays(10);
    AccountPrincipal<?> accountPrincipal = accountService.createAccount("joe@example.com", AdditionalInformation.class);

    assertThatThrownBy(() -> accountService.verifyEmail(accountPrincipal.getMagicId(), now))
        .isInstanceOf(MagicIdExpiredException.class);
  }

  @Test
  void verifyEmail() {
    LocalDateTime now = LocalDateTime.now().plusHours(2);
    AccountPrincipal<?> accountPrincipal = accountService.createAccount("joe@example.com", AdditionalInformation.class);

    accountService.verifyEmail(accountPrincipal.getMagicId(), now);

    Optional<AccountPrincipal<AdditionalInformation>> foundAccountPrincipal = accountService
        .findAccount("joe@example.com");
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
    AccountPrincipal<?> accountPrincipal = accountService.createAccount("joe@example.com", AdditionalInformation.class);

    assertThat(accountService.verifyEmail(accountPrincipal.getMagicId(), now))
        .isNotNull();
  }

  @Test
  void deliverMagicId() {
    AccountPrincipal<?> accountPrincipal = accountService.createAccount("joe@example.com", AdditionalInformation.class);

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
    AccountPrincipal<?> accountPrincipal = accountService.createAccount("joe@example.com", AdditionalInformation.class);

    assertThatThrownBy(() -> accountService.generateAccessToken(accountPrincipal))
        .isInstanceOf(AccountNotVerifiedException.class);
  }

  @Test
  void createJWT() {
    AccountPrincipal<?> accountPrincipal = accountService.createAccount("joe@example.com", AdditionalInformation.class);
    accountService.verifyEmail(accountPrincipal.getMagicId(), LocalDateTime.now());

    AccessToken token = accountService.generateAccessToken(accountService.findAccount("joe@example.com").get());
    assertThat(token)
        .isNotNull();
  }

  @Test
  void storeReadAdditionalInformation() {
    AccountPrincipal<AdditionalInformation> accountPrincipal = accountService.createAccount("joe@example.com",
        AdditionalInformation.class);

    assertThat(accountPrincipal.getAdditionalInformation()).isNotNull();

    accountPrincipal.getAdditionalInformation().setSomeValue("value");
    accountPrincipalRepository.save(accountPrincipal);

    Optional<AccountPrincipal<AdditionalInformation>> foundAccountPrincipal = accountService
        .findAccount("joe@example.com");
    assertThat(foundAccountPrincipal)
        .isPresent()
        .get()
        .satisfies(ap -> {
          assertThat(ap.getAdditionalInformation().getSomeValue()).isEqualTo("value");
        });
  }

}
