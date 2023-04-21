package com.ircnet.service.operserv.tor;

import com.ircnet.library.common.connection.ConnectionStatus;
import com.ircnet.library.common.connection.IRCConnectionService;
import com.ircnet.library.service.IRCServiceTask;
import com.ircnet.service.operserv.ScannerThread;
import com.ircnet.service.operserv.kline.KLine;
import com.ircnet.service.operserv.kline.KLineService;
import com.ircnet.service.operserv.kline.KLineType;
import com.ircnet.service.operserv.persistence.PersistenceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.*;

// Checked (FIXME)
@Service
public class TorServiceImpl implements TorService {
    private static final Logger LOGGER = LoggerFactory.getLogger(TorServiceImpl.class);

    @Autowired
    @Qualifier("klineList")
    private List<KLine> klineList;

    @Autowired
    private KLineService klineService;

    @Value("${service.channel}")
    private String serviceChannel;

    @Autowired
    private IRCServiceTask ircServiceTask;

    @Autowired
    private IRCConnectionService ircConnectionService;

    @Autowired
    private PersistenceService persistenceService;

    @Override
    public void loadFromWeb() {
        Mono url1 = receiveIpList("https://lists.fissionrelays.net/tor/exits.txt");
        Mono url2 = receiveIpList("https://www.dan.me.uk/torlist/?exit");

        Mono.zip(url1, url2, (ipList1, ipList2) -> {
            Set<String> ipList = new HashSet<>();
            ipList.addAll((Collection<? extends String>) ipList1);
            ipList.addAll((Collection<? extends String>) ipList2);
            return ipList;
        }).subscribe(ipList -> {
            Set<String> ipSet = (Set<String>) ipList;

            if (!ipSet.isEmpty()) {
                List<KLine> newKLines = new ArrayList<>();

                for (String ip : ipSet) {
                    KLine kline = new KLine();
                    kline.setType(KLineType.TOR);
                    kline.setUsername("*");
                    kline.setHostname(ip);
                    kline.setIpAddressOrRange(true);
                    kline.setReason("Tor Exit Node");
                    newKLines.add(kline);
                }

                klineService.removeAllWithType(KLineType.TOR);
                klineList.addAll(newKLines);
                persistenceService.save();

                String message = String.format("Loaded %d Tor Exit Nodes", ipSet.size());
                LOGGER.info(message);

                if (ircServiceTask.getIRCConnection().getConnectionStatus() == ConnectionStatus.REGISTERED) {
                    ircConnectionService.notice(ircServiceTask.getIRCConnection(), serviceChannel, message);

                    ScannerThread.getInstance().runOnThread(new Runnable() {
                        @Override
                        public void run() {
                            for (KLine kline : newKLines) {
                                klineService.enforceKLine(kline, null, false, false);
                            }
                        }
                    });
                }
            }
        });
    }

    private Mono<List<String>> receiveIpList(String url) {
        return WebClient.create(url)
                .get()
                .retrieve()
                .bodyToFlux(String.class)
                .collectList()
                .onErrorResume(e -> {
                            String message = String.format("Could not load Tor exit nodes from %s: %s", url, e.getMessage());
                            LOGGER.info(message);

                            if (ircServiceTask.getIRCConnection().getConnectionStatus() == ConnectionStatus.REGISTERED) {
                                ircConnectionService.notice(ircServiceTask.getIRCConnection(), serviceChannel, message);
                            }

                            return Mono.just(new ArrayList<>());
                        }
                );
    }
}
