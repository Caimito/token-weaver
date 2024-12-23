package net.caimito.tokenweaver;

public class AccountNotFoundByMagicIdException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  public AccountNotFoundByMagicIdException(String magicId) {
    super(String.format("Account principal not found by magic id %s", magicId));
  }

}
