package com.ircnet.service.operserv.irc;

import com.ircnet.service.operserv.IpAddressFamily;
import lombok.Data;

/**
 * Represents an user that is connected to IRC.
 * Instances will be created by parsing UNICK.
 */
@Data
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
}
