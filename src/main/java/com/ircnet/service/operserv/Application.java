package com.ircnet.service.operserv;

import com.ircnet.library.common.IRCTaskService;
import com.ircnet.library.service.IRCServiceTask;
import com.ircnet.service.operserv.kline.KLineService;
import com.ircnet.service.operserv.kline.KLineType;
import com.ircnet.service.operserv.persistence.PersistenceService;
import com.ircnet.service.operserv.tor.TorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.Banner;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

/**
 * Main class.
 */
@SpringBootApplication
public class Application extends SpringBootServletInitializer implements CommandLineRunner {
    private static final Logger LOGGER = LoggerFactory.getLogger(Application.class);

    @Autowired
    private IRCTaskService ircTaskService;

    @Autowired
    private IRCServiceTask ircServiceTask;

    @Autowired
    private KLineService klineService;

    @Autowired
    private PersistenceService persistenceService;

    @Autowired
    private TorService torService;

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

        // Load K-Lines from webservice on first start
        if(klineService.getKlineList().isEmpty()) {
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
