package net.caimito.tokenweaver;

public interface MagicLinkSender {

  /**
   * This needs to be implemented by the user of the library. It should send an
   * email to the given email address with a link that contains the magicId.
   * 
   * @param email
   * @param magicId
   */
  public void deliver(String email, String magicId);

}
