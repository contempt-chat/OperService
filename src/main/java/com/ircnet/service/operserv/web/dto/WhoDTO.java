package com.ircnet.service.operserv.web.dto;

import lombok.Data;

@Data
public class WhoDTO {
  private String username;
  private String hostname;
  private String sid;
  private String account;
  private boolean excludeUsersWithSASL;
  private boolean excludeIdent;

  @Override
  public String toString() {
    return "WhoDTO{" +
        "username='" + username + '\'' +
        ", hostname='" + hostname + '\'' +
        ", sid='" + sid + '\'' +
        ", account='" + account + '\'' +
        ", excludeUsersWithSASL=" + excludeUsersWithSASL +
        ", excludeIdent=" + excludeIdent +
        '}';
  }
}
