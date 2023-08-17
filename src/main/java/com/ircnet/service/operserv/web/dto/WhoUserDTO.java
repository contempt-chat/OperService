package com.ircnet.service.operserv.web.dto;

import lombok.Data;

@Data
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
}
