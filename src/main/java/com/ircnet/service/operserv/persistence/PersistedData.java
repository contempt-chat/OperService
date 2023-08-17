package com.ircnet.service.operserv.persistence;


import com.ircnet.service.operserv.kline.KLine;

import java.util.List;

/**
 * This class bundles data which should be persisted.
 */
public class PersistedData {
  /**
   * A list of K-Lines
   */
  private List<KLine> klineList;

  public PersistedData() {
  }

  public List<KLine> getKlineList() {
    return klineList;
  }

  public void setKlineList(List<KLine> klineList) {
    this.klineList = klineList;
  }
}
