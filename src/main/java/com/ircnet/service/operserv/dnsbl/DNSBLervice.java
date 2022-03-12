package com.ircnet.service.operserv.dnsbl;

import com.ircnet.service.operserv.irc.IRCUser;

public interface DNSBLervice {
  /**
   * Checks asynchronously if an user's IP address is listed in any of the configured DNSBLs.
   * If yes, a K-Line will be added.
   *
   * @param user An user
   */
  void check(IRCUser user);
}
