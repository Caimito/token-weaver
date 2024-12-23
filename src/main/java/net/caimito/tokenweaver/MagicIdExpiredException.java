package net.caimito.tokenweaver;

public class MagicIdExpiredException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  public MagicIdExpiredException(String message) {
    super(message);
  }

}
