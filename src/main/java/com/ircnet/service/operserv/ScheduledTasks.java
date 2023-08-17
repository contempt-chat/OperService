package com.ircnet.service.operserv;

import com.ircnet.library.service.IRCServiceTask;
import com.ircnet.service.operserv.kline.KLine;
import com.ircnet.service.operserv.kline.KLineService;
import com.ircnet.service.operserv.tor.TorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

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

    @Autowired
    protected IRCServiceTask ircServiceTask;

    @Autowired
    @Qualifier("klineList")
    private List<KLine> klineList;

    /**
     * Downloads new lists of Tor exit nodes and creates K-Lines.
     */
    @Scheduled(cron = "${tor.reloadNodesCron}")
    public void reloadTorExitNodeList() {
        torService.loadFromWeb();
    }

    /**
     * Removes expired K-Lines.
     *
     * This is just to free memory, expired K-Lines will not be enforced anymore even though they are still
     * stored.
     */
    @Scheduled(cron = "${kline.removeExpiredCron}")
    public void removedExpiredKLines() {
        List<KLine> expiredKLines = klineService.findExpired();

        if(expiredKLines.isEmpty()) {
            return;
        }

        for (KLine kline : expiredKLines) {
            klineList.remove(kline);
            LOGGER.debug("Removed expired K-Line for {}", kline.toHostmask());
        }
    }
}
