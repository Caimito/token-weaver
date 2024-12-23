package net.caimito.tokenweaver;

public class AccountNotVerifiedException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  public AccountNotVerifiedException(String email) {
    super(String.format("Account with email '%s' is not verified", email));
  }

}
