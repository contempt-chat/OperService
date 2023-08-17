package com.ircnet.service.operserv.kline;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Date;

/**
 * Represents a K-Line that has been added on this service.
 * K-Lines will be enforced when a matching user connects to IRC.
 */
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

  public KLine() {
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getHostname() {
    return hostname;
  }

  public void setHostname(String hostname) {
    this.hostname = hostname;
  }

  public boolean isIpAddressOrRange() {
    return ipAddressOrRange;
  }

  public void setIpAddressOrRange(boolean ipAddressOrRange) {
    this.ipAddressOrRange = ipAddressOrRange;
  }

  public String getReason() {
    return reason;
  }

  public void setReason(String reason) {
    this.reason = reason;
  }

  public String getCreatedBy() {
    return createdBy;
  }

  public void setCreatedBy(String createdBy) {
    this.createdBy = createdBy;
  }

  public Date getExpirationDate() {
    return expirationDate;
  }

  public void setExpirationDate(Date expirationDate) {
    this.expirationDate = expirationDate;
  }

  public String getSid() {
    return sid;
  }

  public void setSid(String sid) {
    this.sid = sid;
  }

  public KLineType getType() {
    return type;
  }

  public void setType(KLineType type) {
    this.type = type;
  }

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
