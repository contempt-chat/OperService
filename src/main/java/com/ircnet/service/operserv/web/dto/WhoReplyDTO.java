package com.ircnet.service.operserv.web.dto;

import lombok.Data;

import java.util.List;

@Data
public class WhoReplyDTO {
  private List<WhoUserDTO> users;
  private int total;
}
