package com.ircnet.service.operserv.kline;

import lombok.Data;

/**
 * This class is used to synchronize K-Lines with the web portal.
 */
@Data
public class KLineDTO {
  private Long webPortalId;
  private String username;
  private String hostname;
  private String reason;
  private Long duration;
  private String sid;
  private String requestedBy;
  private boolean saslException;
  private boolean identException;

  @Override
  public String toString() {
    return "KLineDTO{" +
            "webPortalId=" + webPortalId +
            ", username='" + username + '\'' +
            ", hostname='" + hostname + '\'' +
            ", reason='" + reason + '\'' +
            ", duration=" + duration +
            ", sid='" + sid + '\'' +
            ", requestedBy='" + requestedBy + '\'' +
            ", saslException=" + saslException +
            ", identException=" + identException +
            '}';
  }

  public String toHostmask() {
    if(username != null) {
      return username + "@" + hostname;
    }
    else {
      return "*@" + hostname;
    }
  }
}
