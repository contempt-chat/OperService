package com.ircnet.service.operserv.persistence;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ircnet.service.operserv.kline.KLine;
import com.ircnet.service.operserv.kline.KLineService;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class PersistenceServiceImpl implements PersistenceService {
  private static final String KLINE_FILE_NAME = "data.json";
  private static final Logger LOGGER = LoggerFactory.getLogger(PersistenceServiceImpl.class);

  private boolean savePending;

  @Autowired
  @Qualifier("klineList")
  private List<KLine> klineList;

  @Autowired
  @Qualifier("authorizedAccounts")
  private List<String> authorizedAccounts;

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  @Lazy
  private KLineService klineService;

  @Autowired
  private TaskScheduler taskScheduler;

  @Override
  public void scheduleSave() {
    if(!this.savePending) {
      this.savePending = true;
      taskScheduler.schedule(
              () -> {
                save();
                this.savePending = false;
              },
              new Date(OffsetDateTime.now().plusSeconds(10).toInstant().toEpochMilli())
      );
    }
  }

  @Override
  public void save() {
    File file = new File(KLINE_FILE_NAME);

    try {
      List<KLine> klinesToSave = klineService.findAllNotExpired();
      PersistedData dataToSave = new PersistedData();
      dataToSave.setKlineList(klineList);
      dataToSave.setAuthorizedAccounts(authorizedAccounts);
      objectMapper.writeValue(file, dataToSave);
      LOGGER.debug("Saved {} K-Lines and {} SASL accounts to {}", klinesToSave.size(), authorizedAccounts.size(), KLINE_FILE_NAME);
    }
    catch (IOException e) {
      LOGGER.error("Could not K-Lines to {}", KLINE_FILE_NAME, e);
    }
  }

  @Override
  public void load() {
    File file = new File(KLINE_FILE_NAME);

    try {
      PersistedData data = objectMapper.readValue(file, PersistedData.class);

      List<KLine> klinesFromFile = data.getKlineList() != null ? data.getKlineList() : new ArrayList<>();

      if(CollectionUtils.isNotEmpty(klinesFromFile)) {
        klineList.clear();
        klineList.addAll(klinesFromFile);
      }

      List<String> authorizedAccountsFromFile = data.getAuthorizedAccounts() != null ? data.getAuthorizedAccounts() : new ArrayList<>();

      if(CollectionUtils.isNotEmpty(authorizedAccountsFromFile)) {
        authorizedAccounts.clear();
        authorizedAccounts.addAll(authorizedAccountsFromFile);
      }

      LOGGER.info("Loaded {} K-Lines and {} SASL accounts from {}", klinesFromFile.size(), authorizedAccountsFromFile.size(), KLINE_FILE_NAME);
    }
    catch (IOException e) {
      LOGGER.error("Could not load K-Lines from {}", KLINE_FILE_NAME);
    }
  }
}
