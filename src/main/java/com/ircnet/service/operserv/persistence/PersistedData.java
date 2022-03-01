package com.ircnet.service.operserv.persistence;


import com.ircnet.service.operserv.kline.KLine;
import org.springframework.context.annotation.Bean;

import java.util.ArrayList;
import java.util.List;

/**
 * This class bundles data which should be persisted.
 */
public class PersistedData {
  /**
   * A list of K-Lines
   */
  private List<KLine> klineList;

  /**
   * A list of SASL accounts that are allowed to use this service.
   */
  private List<String> authorizedAccounts;

  public PersistedData() {
  }

  public List<KLine> getKlineList() {
    return klineList;
  }

  public void setKlineList(List<KLine> klineList) {
    this.klineList = klineList;
  }

  public List<String> getAuthorizedAccounts() {
    return authorizedAccounts;
  }

  public void setAuthorizedAccounts(List<String> authorizedAccounts) {
    this.authorizedAccounts = authorizedAccounts;
  }
}
