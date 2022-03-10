package com.ircnet.service.operserv.DNSBL;

/**
 * Represents a DNSBL provider.
 */
public class DNSBLProvider {
  /**
   * Display name of the DNSBL.
   */
  private String name;

  /**
   * Domain name of the DNSBL.
   * Example: dnsbl.dronebl.org
   */
  private String domainName;

  /**
   * K-Line reason.
   * {ip} will be replaced by the IP address.
   */
  private String klineReason;

  public DNSBLProvider(String name, String domainName, String klineReason) {
    this.name = name;
    this.domainName = domainName;
    this.klineReason = klineReason;
  }

  public String getName() {
    return name;
  }

  public String getDomainName() {
    return domainName;
  }

  public String getKLineReason() {
    return klineReason;
  }
}
