package com.ircnet.service.operserv.kline;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.Date;

/**
 * Represents a K-Line that has been added on this service.
 * K-Lines will be enforced when a matching user connects to IRC.
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class KLine {
  /**
   * Internal ID.
   */
  private String id;

  /**
   * ID from web portal.
   */
  private Long webPortalId;

  /**
   * Username or ident.
   */
  private String username;

  /**
   * IP address or hostname. Wildcards are supported.
   *
   * Examples:
   *  1.2.3.4
   *  1.2.3.0/24
   *  1.2.3.*
   *  1.2.3.?
   *  ircnet.com
   *  *.ircnet.com
   */
  private String hostname;

  /**
   * True if hostname is an IP address or a range.
   */
  private boolean ipAddressOrRange;

  /**
   * Reason.
   */
  private String reason;

  /**
   * SASL account of the creator.
   */
  private String createdBy;

  /**
   * Expiration date.
   */
  private Date expirationDate;

  /**
   * SID if the K-Line should not be global.
   * Examples: 000A, 000*, 000?
   */
  private String sid;

  /**
   * K-Line type to distinguish if the K-Line comes from web or from Tor services etc.
   */
  private KLineType type;

  /**
   * K-Line matches only users that are not authenticated via SASL.
   */
  private boolean saslException;

  /**
   * K-Line matches only users that have no ident.
   */
  private boolean identException;

  @Override
  public String toString() {
    return "KLine{" +
            "id='" + id + '\'' +
            ", webPortalId=" + webPortalId +
            ", username='" + username + '\'' +
            ", hostname='" + hostname + '\'' +
            ", reason='" + reason + '\'' +
            ", createdBy='" + createdBy + '\'' +
            ", expirationDate=" + expirationDate +
            ", type=" + type +
            '}';
  }

  /**
   * Creates a hostmask for a K-Line.
   *
   * @return A hostmask like user@host
   */
  public String toHostmask() {
    return String.format("%s@%s", getUsername(), getHostname());
  }

  /**
   * Creates (T)K-Line flags.
   *
   * @return (T)K-Line flags
   */
  public String createFlags() {
    StringBuilder flags = new StringBuilder();

    if(saslException) {
      flags.append('S');
    }
    if(identException) {
      flags.append('I');
    }

    return flags.toString();
  }

  @Override
  public int hashCode() {
    if(webPortalId != null) {
      return webPortalId.intValue();
    }
    else {
      return super.hashCode();
    }
  }
}
