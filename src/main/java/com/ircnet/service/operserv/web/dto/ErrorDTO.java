package com.ircnet.service.operserv.web.dto;

import lombok.Data;

@Data
public class ErrorDTO {
  private String message;

  public ErrorDTO(String message) {
    this.message = message;
  }
}
