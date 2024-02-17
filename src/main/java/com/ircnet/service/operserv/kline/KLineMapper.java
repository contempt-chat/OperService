package com.ircnet.service.operserv.kline;

import com.ircnet.service.operserv.Util;
import org.apache.commons.lang3.StringUtils;

import java.util.Date;

public class KLineMapper {
  private KLineMapper() {
  }

  public static KLine map(KLineDTO klineDTO) {
    KLine kline = new KLine();
    kline.setId(klineDTO.getId());
    kline.setUsername(StringUtils.isNotBlank(klineDTO.getUsername()) ? klineDTO.getUsername() : "*");
    kline.setHostname(klineDTO.getHostname());
    kline.setIpAddressOrRange(Util.isIpAddressOrRange(klineDTO.getHostname()));
    kline.setReason(klineDTO.getReason());

    if(StringUtils.isNotBlank(klineDTO.getSid())) {
      kline.setSid(klineDTO.getSid());
    }

    kline.setCreatedBy(klineDTO.getRequestedBy());

    if(klineDTO.getId() != null) {
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
