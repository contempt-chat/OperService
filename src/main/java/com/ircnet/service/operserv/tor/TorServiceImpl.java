package com.ircnet.service.operserv.tor;

import com.ircnet.library.common.connection.ConnectionStatus;
import com.ircnet.library.common.connection.IRCConnectionService;
import com.ircnet.library.service.connection.IRCServiceConnection;
import com.ircnet.service.operserv.ScannerThread;
import com.ircnet.service.operserv.ServiceProperties;
import com.ircnet.service.operserv.kline.KLine;
import com.ircnet.service.operserv.kline.KLineService;
import com.ircnet.service.operserv.kline.KLineType;
import com.ircnet.service.operserv.persistence.PersistenceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Service
public class TorServiceImpl implements TorService {
    private static final Logger LOGGER = LoggerFactory.getLogger(TorServiceImpl.class);

    @Autowired
    private KLineService klineService;

    @Autowired
    private IRCServiceConnection ircServiceConnection;

    @Autowired
    private IRCConnectionService ircConnectionService;

    @Autowired
    private PersistenceService persistenceService;

    @Autowired
    private ServiceProperties properties;

    @Override
    public void loadFromWeb() {
        Mono url1 = receiveIpList("https://www.dan.me.uk/torlist/?exit");

        url1.subscribe(ipList -> {
            Collection<String> ipCollection = (Collection<String>) ipList;

            if (!ipCollection.isEmpty()) {
                klineService.removeAllWithType(KLineType.TOR);
                List<KLine> newKLines = new ArrayList<>();

                for (String ip : ipCollection) {
                    KLine kline = new KLine();
                    kline.setId(UUID.randomUUID().toString());
                    kline.setType(KLineType.TOR);
                    kline.setUsername("*");
                    kline.setHostname(ip);
                    kline.setIpAddressOrRange(true);
                    kline.setReason("Tor Exit Node");
                    kline.setIdentException(true);
                    kline.setSaslException(true);
                    newKLines.add(kline);
                }

                klineService.add(newKLines);

                String message = String.format("Loaded %d Tor Exit Nodes", ipCollection.size());
                LOGGER.info(message);

                persistenceService.save();

                if (ircServiceConnection.getConnectionStatus() == ConnectionStatus.REGISTERED) {
                    //ircConnectionService.notice(ircServiceTask.getIRCConnection(), properties.getChannel(), message);

                    ScannerThread.getInstance().runOnThread(new Runnable() {
                        @Override
                        public void run() {
                            for (KLine kline : newKLines) {
                                klineService.enforceKLine(kline, null, false);
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

                            if (ircServiceConnection.getConnectionStatus() == ConnectionStatus.REGISTERED) {
                                // TODO
                                ircConnectionService.notice(ircServiceConnection, properties.getChannel(), message);
                            }

                            return Mono.just(new ArrayList<>());
                        }
                );
    }
}
