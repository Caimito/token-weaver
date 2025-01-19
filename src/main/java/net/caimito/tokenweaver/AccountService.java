package net.caimito.tokenweaver;

import java.time.LocalDateTime;
import java.util.Locale;
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

  @Autowired
  private JWTProvider jwtProvider;

  /**
   * Creates a new account with the given email address. If the email address is
   * already known, an exception is thrown. The account is locked until the user
   * verifies their email address.
   * 
   * @param email
   * @param additionalInformationClass
   * @return the new account
   */
  public <T> AccountPrincipal<T> createAccount(String email, Locale locale, T additionalInformation) {
    findAccountByEmail(email).ifPresent(ap -> {
      throw new AccountAlreadyExistsException(String.format("Account with email '%s' already exists", email));
    });

    AccountPrincipal<T> accountPrincipal = new AccountPrincipal.Builder<T>(
        email, locale)
        .withAdditionalInformation(additionalInformation)
        .build();

    return accountPrincipalRepository.save(accountPrincipal);
  }

  /**
   * Finds an account by email address.
   * 
   * @param email
   * @return the account, if found
   */
  @SuppressWarnings("unchecked")
  public <T> Optional<AccountPrincipal<T>> findAccountByEmail(String email) {
    return accountPrincipalRepository.findByEmail(email)
        .map(ap -> (AccountPrincipal<T>) ap);
  }

  /**
   * Finds an account by ID.
   * 
   * @param id
   * @return the account, if found
   */
  @SuppressWarnings("unchecked")
  public <T> Optional<AccountPrincipal<T>> findAccountById(String id) {
    return accountPrincipalRepository.findById(id).map(ap -> (AccountPrincipal<T>) ap);
  }

  /**
   * Verifies the email address of the account with the given magicId. If the
   * magic ID is not found, an exception is thrown. If the magic ID is expired,
   * an exception is thrown. If the magic ID is found and not expired, the email
   * address is verified and an access token is returned.
   * 
   * @param magicId
   * @param now     - current date and time
   */
  public AccessToken verifyEmail(String magicId, LocalDateTime now)
      throws MagicIdExpiredException, AccountNotFoundByMagicIdException {
    Optional<AccountPrincipal<?>> ap = accountPrincipalRepository.findByMagicId(magicId);
    if (ap.isPresent()) {
      AccountPrincipal<?> accountPrincipal = ap.get();
      if (accountPrincipal.getMagicIdCreated().isBefore(now.minusDays(magicIdTimeoutDays))) {
        throw new MagicIdExpiredException(String.format("Magic ID expired %s", accountPrincipal.getMagicIdCreated()));
      } else {
        accountPrincipal.setEmailVerified(now);
        AccountPrincipal<?> savedAccountPrincipal = accountPrincipalRepository.save(accountPrincipal);

        return generateAccessToken(savedAccountPrincipal);
      }
    } else {
      throw new AccountNotFoundByMagicIdException(magicId);
    }
  }

  /**
   * Sends a magic link to the account principal.
   * 
   * @param accountPrincipal
   * @param magicLinkSender
   */
  public void sendMagicLink(AccountPrincipal<?> accountPrincipal, MagicLinkSender magicLinkSender) {
    accountPrincipal.generateMagicId();
    accountPrincipalRepository.save(accountPrincipal);
    magicLinkSender.deliver(accountPrincipal.getEmail(), accountPrincipal.getMagicId());
  }

  public AccessToken generateAccessToken(AccountPrincipal<?> accountPrincipal) {
    if (!accountPrincipal.isEmailVerified()) {
      throw new AccountNotVerifiedException(accountPrincipal.getEmail());
    }

    return jwtProvider.generateAccessToken(accountPrincipal.getId());
  }

  public void updateAccount(AccountPrincipal<?> accountPrincipal) {
    accountPrincipalRepository.save(accountPrincipal);
  }

}
