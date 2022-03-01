package com.ircnet.service.operserv.kline;

/**
 * This class is used to synchronize K-Lines with the webservice.
 */
public class KLineDTO {
  private Long id;
  private String username;
  private String hostname;
  private String reason;
  private Long duration;
  private String sid;
  private String requestedBy;

  public KLineDTO() {
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

  public String getReason() {
    return reason;
  }

  public void setReason(String reason) {
    this.reason = reason;
  }

  public Long getDuration() {
    return duration;
  }

  public void setDuration(Long duration) {
    this.duration = duration;
  }

  public String getSid() {
    return sid;
  }

  public void setSid(String sid) {
    this.sid = sid;
  }

  public String getRequestedBy() {
    return requestedBy;
  }

  public void setRequestedBy(String requestedBy) {
    this.requestedBy = requestedBy;
  }

  @Override
  public String toString() {
    return "KLineDTO{" +
        "id=" + id +
        ", username='" + username + '\'' +
        ", hostname='" + hostname + '\'' +
        ", reason='" + reason + '\'' +
        ", duration=" + duration +
        ", sid='" + sid + '\'' +
        ", requestedBy='" + requestedBy + '\'' +
        '}';
  }
}
