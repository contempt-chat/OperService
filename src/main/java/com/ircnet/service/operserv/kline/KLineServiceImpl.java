package com.ircnet.service.operserv.kline;

import com.ircnet.library.common.User;
import com.ircnet.library.common.connection.ConnectionStatus;
import com.ircnet.library.common.connection.IRCConnectionService;
import com.ircnet.library.service.IRCServiceTask;
import com.ircnet.service.operserv.ScannerThread;
import com.ircnet.service.operserv.Util;
import com.ircnet.service.operserv.irc.IRCUser;
import com.ircnet.service.operserv.match.MatchService;
import com.ircnet.service.operserv.persistence.PersistenceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class KLineServiceImpl implements KLineService {
    private static final Logger LOGGER = LoggerFactory.getLogger(KLineServiceImpl.class);

    @Autowired
    @Qualifier("klineList")
    private List<KLine> klineList;

    @Autowired
    @Qualifier("userMapByUID")
    private Map<String, IRCUser> userMapByUID;

    @Autowired
    private MatchService matchService;

    @Autowired
    private PersistenceService persistenceService;

    @Autowired
    private IRCConnectionService ircConnectionService;

    @Autowired
    private IRCServiceTask ircServiceTask;

    @Value("${sasl-webservice.kline.url}")
    private String apiURL;

    @Value("${sasl-webservice.kline.username}")
    private String apiUsername;

    @Value("${sasl-webservice.kline.password}")
    private String apiPassword;

    @Value("${service.channel}")
    private String serviceChannel;

    @Value("${service.name}")
    private String serviceName;

    @Override
    public void create(User from, KLine kline, Long originalDuration) {
        klineList.add(kline);

        StringBuilder message = new StringBuilder();
        message.append(String.format("K-Line added by %s for %s [%s]", kline.getCreatedBy() != null ? kline.getCreatedBy() : serviceName, kline.toHostmask(), kline.getReason()));

        if(kline.getSid() != null) {
            message.append(String.format(" on %s", kline.getSid()));
        }

        if(kline.getExpirationDate() != null) {
            message.append(String.format(" expires in%s", Util.formatSeconds(originalDuration)));
        }

        if(from != null) {
            ircConnectionService.notice(ircServiceTask.getIRCConnection(), from.getNick(), message.toString());
        }

        ircConnectionService.notice(ircServiceTask.getIRCConnection(), serviceChannel, message.toString());

        ScannerThread.getInstance().runOnThread(new Runnable() {
            @Override
            public void run() {
                enforceKLine(kline, from, false);
            }
        });

        persistenceService.scheduleSave();
    }

    @Override
    public void enforceKLine(KLine kline, User from, boolean skipAuthenticatedUsers) {
        List<IRCUser> matchingUsers = isMatchingAnyUser(kline);

        if (!matchingUsers.isEmpty()) {
            Map<String, List<IRCUser>> sidUserMap = new HashMap<>();

            for (IRCUser user : matchingUsers) {
                List<IRCUser> userList = sidUserMap.get(user.getServer().getSid());

                if (userList == null) {
                    userList = new ArrayList<>();
                    sidUserMap.put(user.getServer().getSid(), userList);
                }

                userList.add(user);
            }

            for (Map.Entry<String, List<IRCUser>> entry : sidUserMap.entrySet()) {
                enforceKLine(kline, entry.getKey(), entry.getValue(), from);
            }
        }
    }

    void enforceKLine(KLine kline, String sid, List<IRCUser> userList, User from) {
        for (IRCUser user : userList) {
            if(from != null) {
                String message = String.format("Enforcing TKLine on %s for %s (%s@%s) matching %s [%s]",
                    user.getServer().getName(),
                    user.getNick(), user.getUser(), user.getHost(),
                    kline.toHostmask(),
                    kline.getReason());

                if (from != null) {
                    ircConnectionService.notice(ircServiceTask.getIRCConnection(), from.getNick(), message);
                }

                LOGGER.info(message);
                ircConnectionService.notice(ircServiceTask.getIRCConnection(), serviceChannel, message);

            }
        }

        enforceKLine(kline, sid);
    }

    @Override
    public void enforceKLine(KLine kline, String sid) {
        if (kline.getExpirationDate() != null) {
            long timeDiff = (kline.getExpirationDate().getTime() - System.currentTimeMillis()) / 1000L;
            // TODO: convert back to wdhms to avoid overflows
            ircConnectionService.send(ircServiceTask.getIRCConnection(), "ENCAP %s TKLINE %ss %s :%s", sid, timeDiff, kline.toHostmask(), kline.getReason());
        }
        else {
            // No expiration time configured. Add a TKLINE for 1 week. The ban resists in OperServ and will be enforced again
            // if the user returns later.
            ircConnectionService.send(ircServiceTask.getIRCConnection(), "ENCAP %s TKLINE 1w %s :%s", sid, kline.toHostmask(), kline.getReason());
        }

    }

    @Override
    public KLine findMatchingKLine(IRCUser user) {
        Date now = new Date();
        List<KLine> filteredKLineList = klineList.stream()
            .filter(e -> e.getExpirationDate() == null || e.getExpirationDate().after(now))
            .collect(Collectors.toList());

        for (KLine kline : filteredKLineList) {
            if (matchKLine(kline, user)) {
                return kline;
            }
        }

        return null;
    }

     @Override
     public KLine find(long id) {
        return klineList.stream().filter(e -> e.getId() != null && e.getId() == id).findFirst().orElse(null);
    }

    private boolean matchKLine(KLine kline, IRCUser user) {
        if (kline.getType() == KLineType.TOR && user.getAccount() != null) {
            // Do not apply K-Line for Tor exit nodes if the user is authenticated
            return false;
        }

        return matchService.isMatching(user, kline.getUsername(), kline.getHostname(), kline.isIpAddressOrRange(), kline.getSid());
    }

    @Override
    public List<KLine> findAllNotExpired() {
        Date now = new Date();
        List<KLine> filteredKLineList = klineList.stream()
            .filter(e -> e.getExpirationDate() == null || e.getExpirationDate().after(now))
            .collect(Collectors.toList());
        return Collections.unmodifiableList(filteredKLineList);
    }

/*    @Override
    public List<KLine> findAllWithTypes(KLineType... types) {
        Date now = new Date();
        List<KLineType> typeList = Arrays.asList(types);
        List<KLine> filteredKLineList = klineList.stream()
            .filter(e -> typeList.contains(e.getType()))
            .filter(e -> e.getExpirationDate() == null || e.getExpirationDate().after(now))
            .collect(Collectors.toList());
        return Collections.unmodifiableList(filteredKLineList);
    }*/

    @Override
    public boolean hasAnyWithType(KLineType type) {
        Date now = new Date();
        Optional<KLine> kline = klineList.stream()
                .filter(e -> e.getType() == type)
                .filter(e -> e.getExpirationDate() == null || e.getExpirationDate().after(now))
                .findFirst();
        return kline.isPresent();
    }


    @Override
    public List<KLine> findExpired() {
        Date now = new Date();
        List<KLine> filteredKLineList = klineList.stream()
            .filter(e -> e.getExpirationDate() != null && now.after(e.getExpirationDate()))
            .collect(Collectors.toList());
        return Collections.unmodifiableList(filteredKLineList);
    }

    private List<IRCUser> isMatchingAnyUser(KLine kline) {
        List<IRCUser> matchingUsers = new ArrayList<>();

        for (IRCUser user : userMapByUID.values()) {
            if (matchKLine(kline, user)) {
                matchingUsers.add(user);
            }
        }

        return matchingUsers;
    }

    @Override
    public void loadFromAPI(User from) {
        WebClient.create(apiURL)
                .get()
                .headers(headers -> headers.setBasicAuth(apiUsername, apiPassword))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<KLineDTO>>() {
                })
                .doOnError(e -> {
                    if (ircServiceTask.getIRCConnection().getConnectionStatus() == ConnectionStatus.REGISTERED) {
                        String message = String.format("Could not load K-Lines: API request failed: %s", e.getMessage());
                        ircConnectionService.notice(ircServiceTask.getIRCConnection(), serviceChannel, message);

                        if (from != null) {
                            ircConnectionService.notice(ircServiceTask.getIRCConnection(), from.getNick(), message);
                        }
                    }
                })
                .subscribe(
                        response -> {
                            // Remove K-Lines that came from webservice originally
                            klineList.removeIf(e -> e.getType() == KLineType.WEB);

                            List<KLine> webServiceKLineList = new ArrayList<>();
                            response.stream().map(e -> KLineMapper.map(e)).forEach(e -> webServiceKLineList.add(e));

                            LOGGER.info("Loaded {} K-Lines from webservice", webServiceKLineList.size());
                            klineList.addAll(webServiceKLineList);
                            persistenceService.save();


                            if (ircServiceTask.getIRCConnection().getConnectionStatus() == ConnectionStatus.REGISTERED) {
                                String message = String.format("Loaded %d K-Lines", klineList.size());
                                ircConnectionService.notice(ircServiceTask.getIRCConnection(), serviceChannel, message);

                                if (from != null) {
                                    ircConnectionService.notice(ircServiceTask.getIRCConnection(), from.getNick(), message);
                                }

                                ScannerThread.getInstance().runOnThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        for (KLine kline : klineList) {
                                            enforceKLine(kline, null, false);
                                        }
                                    }
                                });
                            }
                        }
                );
    }

    @Override
    public void loadFromAPI() {
        List<KLineDTO> response = WebClient.create(apiURL)
            .get()
            .headers(headers -> headers.setBasicAuth(apiUsername, apiPassword))
            .retrieve()
            .bodyToMono(new ParameterizedTypeReference<List<KLineDTO>>() {
            })
            .block();

        // Remove K-Lines that came from webservice originally
        klineList.removeIf(e -> e.getType() == KLineType.WEB);

        List<KLine> webServiceKLineList = new ArrayList<>();
        response.stream().map(e -> KLineMapper.map(e)).forEach(e -> webServiceKLineList.add(e));

        LOGGER.info("Loaded {} K-Lines from webservice", webServiceKLineList.size());
        klineList.addAll(webServiceKLineList);
        persistenceService.save();

        if (ircServiceTask.getIRCConnection().getConnectionStatus() == ConnectionStatus.REGISTERED) {
            String message = String.format("Loaded %d K-Lines", klineList.size());
            ircConnectionService.notice(ircServiceTask.getIRCConnection(), serviceChannel, message);

            ScannerThread.getInstance().runOnThread(new Runnable() {
                @Override
                public void run() {
                    for (KLine kline : klineList) {
                        enforceKLine(kline, null, false);
                    }
                }
            });
        }
    }

    @Override
    public void removeKLine(KLine kline, User from) {
        klineList.remove(kline);
        String message = String.format("Removed K-Line for %s", kline.toHostmask());

        if(from != null) {
            ircConnectionService.notice(ircServiceTask.getIRCConnection(), from.getNick(), message);
        }

        ircConnectionService.notice(ircServiceTask.getIRCConnection(), serviceChannel, message);
        persistenceService.scheduleSave();
    }

    @Override
    public void removeAllWithType(KLineType type) {
        klineList.removeIf(e -> e.getType() == type);
    }

    @Override
    public long createCheckSum() {
        return klineList.stream()
            .filter(e -> e.getType() == KLineType.WEB)
            .map(e -> e.getId())
            .collect(Collectors.summingLong(Long::longValue));
    }
}
