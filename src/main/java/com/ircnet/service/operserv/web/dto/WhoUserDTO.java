package com.ircnet.service.operserv.web.dto;

public class WhoUserDTO {
  /**
   * SID of the server the user is connected to.
   */
  private String sid;

  /**
   * Name of the server the user is connected to.
   */
  private String serverName;

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
  private String username;

  /**
   * Hostname.
   */
  private String host;

  /**
   * IP address.
   */
  private String ipAddress;

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

  public String getSid() {
    return sid;
  }

  public void setSid(String sid) {
    this.sid = sid;
  }

  public String getServerName() {
    return serverName;
  }

  public void setServerName(String serverName) {
    this.serverName = serverName;
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

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
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
