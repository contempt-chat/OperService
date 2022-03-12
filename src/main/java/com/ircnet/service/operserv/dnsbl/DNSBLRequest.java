package com.ircnet.service.operserv.dnsbl;

import com.ircnet.service.operserv.irc.IRCUser;

public class DNSBLRequest {
  private String ipAddress;
  private IRCUser user;

  public DNSBLRequest() {
  }

  public String getIpAddress() {
    return ipAddress;
  }

  public void setIpAddress(String ipAddress) {
    this.ipAddress = ipAddress;
  }

  public IRCUser getUser() {
    return user;
  }

  public void setUser(IRCUser user) {
    this.user = user;
  }
}
