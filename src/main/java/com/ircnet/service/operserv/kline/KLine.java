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
   * ID from webservice.
   */
  private Long id;

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

  @Override
  public String toString() {
    return "KLine{" +
            "id=" + id +
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

  @Override
  public int hashCode() {
    if(id != null) {
      return id.intValue();
    }
    else {
      return super.hashCode();
    }
  }
}
