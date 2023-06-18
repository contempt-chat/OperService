package com.ircnet.service.operserv.web.dto;

public class WhoDTO {
  private String username;
  private String hostname;
  private String sid;

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

  public String getSid() {
    return sid;
  }

  public void setSid(String sid) {
    this.sid = sid;
  }

  @Override
  public String toString() {
    return "WhoDTO{" +
        "username='" + username + '\'' +
        ", hostname='" + hostname + '\'' +
        ", sid='" + sid + '\'' +
        '}';
  }
}
