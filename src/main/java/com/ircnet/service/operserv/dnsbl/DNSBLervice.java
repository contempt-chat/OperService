package com.ircnet.service.operserv.dnsbl;

import com.ircnet.library.service.user.IRCUser;

public interface DNSBLervice {
  /**
   * Checks asynchronously if a user's IP address is listed in any of the configured DNSBLs.
   * If yes, a K-Line will be added.
   *
   * @param user A user
   */
  void check(IRCUser user);
}
