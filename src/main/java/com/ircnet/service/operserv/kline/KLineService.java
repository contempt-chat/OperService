package com.ircnet.service.operserv.kline;

import com.ircnet.library.common.User;
import com.ircnet.library.common.connection.IRCConnection;
import com.ircnet.service.operserv.irc.IRCUser;

import java.util.List;

/**
 * Service for managing K-Lines.
 */
public interface KLineService {
  /**
   * Creates a new K-Line and sends it to the webservice.
   *
   * @param username Username or ident
   * @param hostname An IP address/range or hostname
   * @param isIpAddressOrRange true if the hostname is an IP address or range
   * @param reason Reason
   * @param from If SQUERY was used to create the K-Line, the user is stored here, otherwise null
   * @param fromAccount The SASL account of the user who creates this K-Line
   * @param duration Duration in seconds when this K-Line expires, or null
   * @param sid A SID pattern if the K-Line is not global, otherwise null
   * @param dryRun true if this is just a dry run, only simulates K-Line and show who would get banned
   * @param isLocal true if the K-Line should not be sent to the webservice
   */
  void create(String username, String hostname, boolean isIpAddressOrRange, String reason, User from, String fromAccount, Long duration, String sid, boolean dryRun, boolean isLocal);

  /**
   * Finds the first K-Line that matches the given user.
   *
   * @param user An user that is connected to IRC
   * @return A K-Line or null if no K-Line matches
   */
  KLine findMatchingKLine(IRCUser user);

  /**
   * Returns all K-Lines stored by this service that have not expired.
   *
   * @return A list of K-Lines
   */
  List<KLine> findAllNotExpired();

  /**
   * Finds all K-Lines of a given type.
   *
   * @param types A list of types
   * @return A list of K-Lines
   */
  List<KLine> findAllWithTypes(KLineType... types);

  /**
   * Checks if at least one not expired K-Line of the given type exists.
   *
   * @param type A type
   * @return true if a K-Line from the given type exists
   */
  boolean hasAnyWithType(KLineType type);

  /**
   * Returns all expired K-Lines.
   *
   * @return A list of expired K-Lines
   */
  List<KLine> findExpired();

  /**
   * Sends a K-Line to an IRC server.
   *
   * @param kline A K-Line
   * @param from The creator of the K-Line, used to notice him that the K-Line gets enforced
   * @param skipAuthenticatedUsers true to not send a K-Line for authenticated users
   * @param dryRun true if this is just a dry run, only simulates K-Line and show who would get banned
   */
  void enforceKLine(KLine kline, User from, boolean skipAuthenticatedUsers, boolean dryRun);

  /**
   * Sends a K-Line to an IRC server.
   *
   * @param kline A K-Line
   * @param sid If the K-Line is not global, the SID of the server(s) where the K-Line should be enforced
   */
  void enforceKLine(KLine kline, String sid);

  /**
   * Loads K-Lines from the webservice.
   *
   * @param from If this method was executed by SQUERY, the user is stored here
   */
  void loadFromAPI(User from);

  /**
   * Removes a K-Line.
   *
   * @param from If this method was executed by SQUERY, the user is stored here
   * @param hostmask The hostmask of the K-Line that should be removed
   * @param sid If the K-Line is not global, the SID of the server(s) where the K-Line should be removed.
   *            This parameter cannot be used to convert a global K-Line into a local K-Line.
   */
  void remove(User from, String hostmask, String sid);

  /**
   * Removes all K-Lines of a given type.
   * This method is used to clear K-Lines for Tor exit nodes when a new list has been downloaded.
   *
   * @param type A K-Line type
   */
  void removeAllWithType(KLineType type);
}
