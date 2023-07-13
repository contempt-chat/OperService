package com.ircnet.service.operserv.irc;

import com.ircnet.service.operserv.IpAddressFamily;

/**
 * Represents an user that is connected to IRC.
 * Instances will be created by parsing UNICK.
 */
public class IRCUser {
  /**
   * The server the user is connected to.
   */
  private IRCServer server;

  /**
   * UID.
   */
  private String uid;

  /**
   * Nick.
   */
  private String nick;

  /**
   * Username / ident.
   */
  private String user;

  /**
   * Hostname.
   */
  private String host;

  /**
   * IP address.
   */
  private String ipAddress;

  /**
   * IP address family (IPv4 or IPv6).
   */
  private IpAddressFamily ipAddressFamily;

  /**
   * User modes.
   */
  private String userModes;

  /**
   * SASL account name. If the user is not logged in, the value is "*" or null.
   */
  private String account;

  /**
   * Real name.
   */
  private String realName;

  public IRCUser() {
  }

  public IRCServer getServer() {
    return server;
  }

  public void setServer(IRCServer server) {
    this.server = server;
  }

  public String getUid() {
    return uid;
  }

  public void setUid(String uid) {
    this.uid = uid;
  }

  public String getNick() {
    return nick;
  }

  public void setNick(String nick) {
    this.nick = nick;
  }

  public String getUser() {
    return user;
  }

  public void setUser(String user) {
    this.user = user;
  }

  public String getHost() {
    return host;
  }

  public void setHost(String host) {
    this.host = host;
  }

  public String getIpAddress() {
    return ipAddress;
  }

  public void setIpAddress(String ipAddress) {
    this.ipAddress = ipAddress;
  }

  public IpAddressFamily getIpAddressFamily() {
    return ipAddressFamily;
  }

  public void setIpAddressFamily(IpAddressFamily ipAddressFamily) {
    this.ipAddressFamily = ipAddressFamily;
  }

  public String getUserModes() {
    return userModes;
  }

  public void setUserModes(String userModes) {
    this.userModes = userModes;
  }

  public String getAccount() {
    return account;
  }

  public void setAccount(String account) {
    this.account = account;
  }

  public String getRealName() {
    return realName;
  }

  public void setRealName(String realName) {
    this.realName = realName;
  }
}
