package com.ircnet.service.operserv.irc;

import com.ircnet.library.service.user.IRCUser;

import java.util.Collection;

/**
 * Services for managing users that are connected to IRC.
 */
public interface UserService {
  /**
   * Adds a user.
   *
   * @param user A user
   */
  void add(IRCUser user);

  /**
   * Finds a user by UID.
   *
   * @return A user or null if the user could not be found
   */
  IRCUser findByUID(String uid);

  /**
   * Finds a user by nick.
   *
   * @return A user or null if the user could not be found
   */
  IRCUser findByNick(String nick);

  /**
   * Finds an IRC user by uid or by nick.
   *
   * @return A user or null if the user could not be found
   */
  IRCUser findByUIDorNick(String arg);

  /**
   * Renames a user.
   *
   * @param user A user
   * @param newNick The new nick
   */
  void rename(IRCUser user, String newNick);

  /**
   * Removes a user.
   *
   * @param user A user
   */
  void remove(IRCUser user);

  /**
   * Returns all users.
   *
   * @return All users
   */
  Collection<IRCUser> getAllUsers();
}
