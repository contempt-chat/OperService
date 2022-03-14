package com.ircnet.service.operserv.dnsbl;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class SpamhausProvider extends DNSBLProvider {
  public SpamhausProvider(String name, String domainName, String klineReason) {
    super(name, domainName, klineReason);
  }

  @Override
  public boolean isRelevantARecord(String aRecord) {
    return StringUtils.equalsAny(aRecord,
        "127.0.0.2", // SBL
        "127.0.0.3", // CSS
        "127.0.0.4", "127.0.0.5", "127.0.0.6", "127.0.0.7" // XBL
        );
  }

  @Override
  public String getKLineReason(List<String> aRecords) {
    List<String> subLists = new ArrayList<>();

    for (String aRecord : aRecords) {
      if(aRecord.equals("127.0.0.2")) {
        subLists.add("SBL");
      }
      else if(aRecord.equals("127.0.0.3")) {
        subLists.add("CSS");
      }
      else if(StringUtils.equalsAny(aRecord, "127.0.0.4", "127.0.0.5", "127.0.0.6", "127.0.0.7")) {
        subLists.add("XBL");
      }
      else if(StringUtils.equalsAny(aRecord, "127.0.0.10", "127.0.0.11")) {
        subLists.add("PBL");
      }
    }

    return super.getKLineReason(aRecords).replace("{subLists}", StringUtils.join(subLists, ", "));
  }
}
