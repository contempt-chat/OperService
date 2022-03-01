package com.ircnet.service.operserv.irc;
// Checked (FIXME)
/**
 * Services for managing users that are connected to IRC.
 */
public interface UserService {
  /**
   * Adds an user.
   *
   * @param user
   */
  void add(IRCUser user);

  /**
   * Finds an user by UID.
   *
   * @return An user or null if the user could not be found
   */
  IRCUser findByUID(String uid);

  /**
   * Finds an user by nick.
   *
   * @return An user or null if the user could not be found
   */
  IRCUser findByNick(String nick);

  /**
   * Finds an IRC user by uid or by nick.
   *
   * @return An user or null if the user could not be found
   */
  IRCUser findByUIDorNick(String arg);

  /**
   * Renames an user.
   *
   * @param user User object
   * @param newNick The new nick
   */
  void rename(IRCUser user, String newNick);

  /**
   * Removes an user.
   *
   * @param user User object
   */
  void remove(IRCUser user);
}
