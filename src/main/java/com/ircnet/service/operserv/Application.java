package com.ircnet.service.operserv;

import com.ircnet.library.common.IRCTaskService;
import com.ircnet.library.service.IRCServiceTask;
import com.ircnet.service.operserv.kline.KLine;
import com.ircnet.service.operserv.kline.KLineService;
import com.ircnet.service.operserv.kline.KLineType;
import com.ircnet.service.operserv.persistence.PersistenceService;
import com.ircnet.service.operserv.sasl.AccountService;
import com.ircnet.service.operserv.tor.TorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.Banner;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.List;
// Checked (FIXME)
/**
 * Main class.
 */
@SpringBootApplication
public class Application implements CommandLineRunner {
    private static final Logger LOGGER = LoggerFactory.getLogger(Application.class);

    @Autowired
    private IRCTaskService ircTaskService;

    @Autowired
    private IRCServiceTask ircServiceTask;

    @Autowired
    private AccountService accountService;

    @Autowired
    private KLineService klineService;

    @Autowired
    private PersistenceService persistenceService;

    @Autowired
    private TorService torService;

    @Autowired
    @Qualifier("authorizedAccounts")
    private List<String> authorizedAccounts;

    @Autowired
    @Qualifier("klineList")
    private List<KLine> klineList;

    public static void main(String[] args) {
        SpringApplication springApplication = new SpringApplication(Application.class);
        springApplication.setBannerMode(Banner.Mode.OFF);
        springApplication.setLogStartupInfo(false);
        springApplication.run(args);
    }

    @Override
    public void run(String... args) {
        Thread ircServiceThread = new Thread() {
            @Override
            public void run() {
                ircTaskService.run(ircServiceTask);
            }
        };

        // Load data from file
        persistenceService.load();

        // Load authorized SASL accounts from webservice on first start
        if(authorizedAccounts.isEmpty()) {
            accountService.loadFromAPI(null);
        }

        // Load K-Lines from webservice on first start
        if(klineList.isEmpty()) {
            klineService.loadFromAPI(null);
        }

        // Load Tor exit nodes on first start
        if(!klineService.hasAnyWithType(KLineType.TOR)) {
            torService.loadFromWeb();
        }

        // Start IRC service
        ircServiceThread.start();

        // Start scanner thread
        ScannerThread.getInstance().start();
    }
}
