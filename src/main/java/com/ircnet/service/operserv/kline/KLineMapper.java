package com.ircnet.service.operserv.kline;

import com.ircnet.service.operserv.Util;
import org.apache.commons.lang3.StringUtils;

import java.util.Date;
import java.util.UUID;

public class KLineMapper {
  private KLineMapper() {
  }

  public static KLine map(KLineDTO klineDTO) {
    KLine kline = new KLine();
    kline.setId(UUID.randomUUID().toString());
    kline.setWebPortalId(klineDTO.getWebPortalId());
    kline.setUsername(StringUtils.isNotBlank(klineDTO.getUsername()) ? klineDTO.getUsername() : "*");
    kline.setHostname(klineDTO.getHostname());
    kline.setIpAddressOrRange(Util.isIpAddressOrRange(klineDTO.getHostname()));
    kline.setReason(klineDTO.getReason());

    if(StringUtils.isNotBlank(klineDTO.getSid())) {
      kline.setSid(klineDTO.getSid());
    }

    kline.setCreatedBy(klineDTO.getRequestedBy());

    if(klineDTO.getWebPortalId() != null) {
      kline.setType(KLineType.WEB);
    }

    if (klineDTO.getDuration() != null && klineDTO.getDuration() > 0) {
      kline.setExpirationDate(new Date(System.currentTimeMillis() + klineDTO.getDuration() * 1000L));
    }

    kline.setSaslException(klineDTO.isSaslException());
    kline.setIdentException(klineDTO.isIdentException());

    return kline;
  }
}
