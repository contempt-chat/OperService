package com.ircnet.service.operserv.dnsbl;

import java.util.List;

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

  /**
   * Creates a K-Line reason.
   *
   * @param aRecords The found A records
   * @return A K-Line reason
   */
  public String getKLineReason(List<String> aRecords) {
    return klineReason;
  }

  public boolean isRelevantARecord(String aRecord) {
    return true;
  }
}
