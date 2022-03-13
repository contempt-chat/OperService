package com.ircnet.service.operserv;

import com.ircnet.service.operserv.kline.KLine;
import com.ircnet.service.operserv.kline.KLineType;
import com.ircnet.service.operserv.persistence.PersistenceService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import javax.annotation.PostConstruct;
import java.util.List;

/**
 * Fix for existing DNSBL K-Lines.
 */
public class Patch13032022 {
    @Autowired
    @Qualifier("klineList")
    private List<KLine> klineList;

    @Autowired
    private PersistenceService persistenceService;

    @PostConstruct
    public void patch() {
        for (KLine kline : klineList) {
            if(StringUtils.containsAny(kline.getReason(), "Spamhaus", "all.s5h.net", "DroneBL", "efnetrbl.org", "Tornevall")) {
                kline.setType(KLineType.DNSBL);
            }
        }

        persistenceService.save();
    }
}
