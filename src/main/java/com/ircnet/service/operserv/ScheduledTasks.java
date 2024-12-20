package com.ircnet.service.operserv;

import com.ircnet.service.operserv.kline.KLineService;
import com.ircnet.service.operserv.tor.TorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Contains tasks that will be executed periodically.
 */
@Component
public class ScheduledTasks {
    private static final Logger LOGGER = LoggerFactory.getLogger(ScheduledTasks.class);

    @Autowired
    private TorService torService;

    @Autowired
    private KLineService klineService;

    /**
     * Downloads new lists of Tor exit nodes and creates K-Lines.
     */
    @Scheduled(cron = "${tor.reloadNodesCron}")
    public void reloadTorExitNodeList() {
        torService.loadFromWeb();
    }

    /**
     * Removes expired K-Lines.
     * This is just to free memory, expired K-Lines will not be enforced anymore even though they are still
     * stored.
     */
    @Scheduled(cron = "${kline.removeExpiredCron}")
    public void removedExpiredKLines() {
        klineService.removedExpiredKLines();
    }
}
