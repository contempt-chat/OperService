package com.ircnet.service.operserv.web.dto;

import java.util.List;

public class WhoReplyDTO {
  private List<WhoUserDTO> users;
  private int total;

  public WhoReplyDTO() {
  }

  public List<WhoUserDTO> getUsers() {
    return users;
  }

  public void setUsers(List<WhoUserDTO> users) {
    this.users = users;
  }

  public int getTotal() {
    return total;
  }

  public void setTotal(int total) {
    this.total = total;
  }
}
