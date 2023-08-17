package com.ircnet.service.operserv.persistence;


import com.ircnet.service.operserv.kline.KLine;
import lombok.Data;

import java.util.List;

/**
 * This class bundles data which should be persisted.
 */
@Data
public class PersistedData {
  /**
   * A list of K-Lines
   */
  private List<KLine> klineList;
}
