package net.caimito.tokenweaver;

import java.time.LocalDateTime;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class AccountService {
  private static final Logger LOGGER = LoggerFactory.getLogger(AccountService.class);

  @Value("${tokenweaver.magicIdTimeoutDays:1}")
  private int magicIdTimeoutDays;

  @Autowired
  private AccountPrincipalRepository accountPrincipalRepository;

  /**
   * Creates a new account with the given email address. If the email address is
   * already known, an exception is thrown.
   * 
   * @param email
   * @return the new account
   */
  public AccountPrincipal createAccount(String email) {
    findAccount(email).ifPresent(ap -> {
      throw new AccountAlreadyExistsException(String.format("Account with email '%s' already exists", email));
    });

    AccountPrincipal accountPrincipal = new AccountPrincipal(email);
    return accountPrincipalRepository.save(accountPrincipal);
  }

  public Optional<AccountPrincipal> findAccount(String email) {
    return accountPrincipalRepository.findByEmail(email);
  }

  private Optional<AccountPrincipal> findByMagicId(String magicId, LocalDateTime now) {
    Optional<AccountPrincipal> ap = accountPrincipalRepository.findByMagicId(magicId);
    if (ap.isPresent()) {
      AccountPrincipal accountPrincipal = ap.get();
      if (accountPrincipal.getMagicIdCreated().isBefore(now.minusDays(magicIdTimeoutDays))) {
        throw new MagicIdExpiredException(String.format("Magic ID expired %s", accountPrincipal.getMagicIdCreated()));
      } else {
        return ap;
      }
    } else {
      return Optional.empty();
    }
  }

  public void verifyEmail(String magicId, LocalDateTime now) {
    Optional<AccountPrincipal> ap = findByMagicId(magicId, now);
    if (ap.isPresent()) {
      AccountPrincipal accountPrincipal = ap.get();
      accountPrincipal.setEmailVerified(now);
      accountPrincipalRepository.save(accountPrincipal);
    } else {
      LOGGER.warn("Account principal not found by magic id {}", magicId);
    }
  }

}
