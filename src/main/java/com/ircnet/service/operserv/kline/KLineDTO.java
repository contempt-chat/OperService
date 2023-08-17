package com.ircnet.service.operserv.kline;

import lombok.Data;

/**
 * This class is used to synchronize K-Lines with the webservice.
 */
@Data
public class KLineDTO {
  private Long id;
  private String username;
  private String hostname;
  private String reason;
  private Long duration;
  private String sid;
  private String requestedBy;

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
