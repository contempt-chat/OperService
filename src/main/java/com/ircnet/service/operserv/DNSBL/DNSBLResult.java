package com.ircnet.service.operserv.DNSBL;

public class DNSBLResult {
  private DNSBLStatus status;

  public DNSBLResult(DNSBLStatus status) {
    this.status = status;
  }

  public DNSBLStatus getStatus() {
    return status;
  }
}
