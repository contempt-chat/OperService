package com.ircnet.service.operserv.kline;

import com.ircnet.library.common.User;
import com.ircnet.library.common.connection.ConnectionStatus;
import com.ircnet.library.common.connection.IRCConnectionService;
import com.ircnet.library.service.IRCServiceTask;
import com.ircnet.service.operserv.Constants;
import com.ircnet.service.operserv.Util;
import com.ircnet.service.operserv.irc.IRCUser;
import com.ircnet.service.operserv.match.MatchService;
import com.ircnet.service.operserv.persistence.PersistenceService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class KLineServiceImpl implements KLineService {
    private static final Logger LOGGER = LoggerFactory.getLogger(KLineServiceImpl.class);

    @Autowired
    @Qualifier("klineList")
    private List<KLine> klineList;

    @Autowired
    @Qualifier("webServiceKLineList")
    private List<KLine> webServiceKLineList;

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

    @Override // FIXME: too many parameters, rewrite this method
    public void create(String username, String hostname, boolean isIpAddressOrRange, String reason, User from, String fromAccount, Long duration, String sid, boolean dryRun, boolean isLocal) {
        KLineDTO klineDTO = new KLineDTO();
        klineDTO.setUsername(username);
        klineDTO.setHostname(hostname);
        klineDTO.setReason(reason);
        klineDTO.setRequestedBy(fromAccount);

        if (duration != null && duration > 0) {
            klineDTO.setDuration(duration);
        }

        if(sid != null) {
            klineDTO.setSid(sid);
        }

        if (!dryRun && !isLocal) {
            WebClient.create(apiURL)
                    .post()
                    .headers(headers -> headers.setBasicAuth(apiUsername, apiPassword))
                    .body(Mono.just(klineDTO), KLineDTO.class)
                    .retrieve()
                    .bodyToMono(KLineDTO.class)
                    .doOnError(e -> {
                        if ((e instanceof WebClientResponseException) && ((WebClientResponseException) e).getStatusCode() == HttpStatus.CONFLICT) {
                            // K-Line exists already on webservice
                            if(from != null) {
                                ircConnectionService.notice(ircServiceTask.getIRCConnection(), from.getNick(), "Entry exists already.");
                            }
                        }
                        else {
                            String message = String.format("Could not add K-Line: API request failed: %s", e.getMessage());

                            if(from != null) {
                                ircConnectionService.notice(ircServiceTask.getIRCConnection(), from.getNick(), message);
                            }

                            ircConnectionService.notice(ircServiceTask.getIRCConnection(), serviceChannel, message);
                            processCreate(from, klineDTO, duration, dryRun);
                        }
                    })
                    .subscribe(
                            response -> {
                                processCreate(from, response, duration, dryRun);
                            }
                    );
        }
        else {
            processCreate(from, klineDTO, duration, dryRun);
        }
    }

    private void processCreate(User from, KLineDTO klineDTO, Long originalDuration, boolean dryRun) {
        KLine kline = map(klineDTO);

        if (klineDTO.getDuration() != null && klineDTO.getDuration() > 0) {
            kline.setExpirationDate(new Date(System.currentTimeMillis() + klineDTO.getDuration() * 1000L));
        }

        if (!dryRun) {
            klineList.add(kline);
        }

        StringBuilder message = new StringBuilder();

        if(dryRun) {
            message.append(Constants.DRY_RUN_TAG);
            message.append(" ");
        }

        message.append(String.format("Added K-LINE for %s", kline.toHostmask()));

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

        enforceKLine(kline, from, false, dryRun);

        if(!dryRun) {
            persistenceService.scheduleSave();
        }
    }

    @Override
    public void enforceKLine(KLine kline, User from, boolean skipAuthenticatedUsers, boolean dryRun) {
        List<IRCUser> matchingUsers = isMatchingAnyUser(kline);

        if (!matchingUsers.isEmpty()) {
            Map<String, List<IRCUser>> sidUserMap = new HashMap<>();

            for (IRCUser user : matchingUsers) {
                List<IRCUser> userList = sidUserMap.get(user.getSid());

                if (userList == null) {
                    userList = new ArrayList<>();
                    sidUserMap.put(user.getSid(), userList);
                }

                userList.add(user);
            }

            for (Map.Entry<String, List<IRCUser>> entry : sidUserMap.entrySet()) {
                enforceKLine(kline, entry.getKey(), entry.getValue(), from, dryRun);
            }
        }
    }

    void enforceKLine(KLine kline, String sid, List<IRCUser> userList, User from, boolean dryRun) {
        for (IRCUser user : userList) {
            if(from != null || !dryRun) {
                String message = String.format("%sEnforcing TKLine on %s for %s (%s@%s) matching %s: %s",
                    dryRun ? Constants.DRY_RUN_TAG : "",
                    user.getSid(),
                    user.getNick(), user.getUser(), user.getHost(),
                    kline.toHostmask(),
                    kline.getReason());

                if (from != null) {
                    ircConnectionService.notice(ircServiceTask.getIRCConnection(), from.getNick(), message);
                }

                if (!dryRun) {
                    LOGGER.info(message);
                    ircConnectionService.notice(ircServiceTask.getIRCConnection(), serviceChannel, message);
                }
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

    private KLine map(KLineDTO klineDTO) {
        KLine kline = new KLine();
        kline.setId(klineDTO.getId());
        kline.setUsername(StringUtils.isNotBlank(klineDTO.getUsername()) ? klineDTO.getUsername() : "*");
        kline.setHostname(klineDTO.getHostname());
        kline.setIpAddressOrRange(Util.isIpAddressOrRange(klineDTO.getHostname()));
        kline.setReason(klineDTO.getReason());
        kline.setSid(klineDTO.getSid());
        kline.setCreatedBy(klineDTO.getRequestedBy());

        if(klineDTO.getId() != null) {
            kline.setType(KLineType.SYNCED);
        }
        else {
            kline.setType(KLineType.NOT_SYNCED);
        }

        if (klineDTO.getDuration() != null) {
            kline.setExpirationDate(new Date(System.currentTimeMillis() + klineDTO.getDuration() * 1000L));
        }

        return kline;
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

    private boolean matchKLine(KLine kline, IRCUser user) {
        if (kline.getType() == KLineType.TOR && !user.getAccount().isEmpty() && !user.getAccount().equals("*")) {
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

    @Override
    public List<KLine> findAllWithTypes(KLineType... types) {
        Date now = new Date();
        List<KLineType> typeList = Arrays.asList(types);
        List<KLine> filteredKLineList = klineList.stream()
            .filter(e -> typeList.contains(e.getType()))
            .filter(e -> e.getExpirationDate() == null || e.getExpirationDate().after(now))
            .collect(Collectors.toList());
        return Collections.unmodifiableList(filteredKLineList);
    }

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
                            klineList.removeAll(webServiceKLineList);
                            webServiceKLineList.clear();

                            response.stream().map(e -> map(e)).forEach(e -> webServiceKLineList.add(e));

                            LOGGER.info("Loaded {} K-Lines from webservice", webServiceKLineList.size());
                            klineList.addAll(webServiceKLineList);
                            persistenceService.save();


                            if (ircServiceTask.getIRCConnection().getConnectionStatus() == ConnectionStatus.REGISTERED) {
                                String message = String.format("Loaded %d K-Lines", klineList.size());
                                ircConnectionService.notice(ircServiceTask.getIRCConnection(), serviceChannel, message);

                                if (from != null) {
                                    ircConnectionService.notice(ircServiceTask.getIRCConnection(), from.getNick(), message);
                                }

                                for (KLine kline : klineList) {
                                    enforceKLine(kline, null, false, false);
                                }
                            }
                        }
                );
    }

    @Override
    public void remove(User from, String hostmask, String sid) {
        List<KLine> klines = klineList.stream()
            .filter(e -> e.toHostmask().equals(hostmask))
            .filter(e -> StringUtils.equalsIgnoreCase(e.getSid(), sid))
            .collect(Collectors.toList());

        if (klines.isEmpty()) {
            if(from != null) {
                ircConnectionService.notice(ircServiceTask.getIRCConnection(), from.getNick(), "K-line could not be found");
            }

            return;
        }

        for (KLine kline : klines) {
            String url = Util.appendPathToURL(apiURL, String.valueOf(kline.getId()));

            WebClient.create(url.toString())
                    .delete()
                    .headers(headers -> headers.setBasicAuth(apiUsername, apiPassword))
                    .retrieve()
                    .toBodilessEntity()
                    .doOnError(e -> {
                        String message = String.format("Could not remove TKLine: API request failed: %s", e.getMessage());

                        if(from != null) {
                            ircConnectionService.notice(ircServiceTask.getIRCConnection(), from.getNick(), message);
                        }

                        ircConnectionService.notice(ircServiceTask.getIRCConnection(), serviceChannel, message);
                    })
                    .subscribe(
                            response -> {
                                ircConnectionService.send(ircServiceTask.getIRCConnection(), "ENCAP %s UNTKLINE %s", "*", hostmask);
                                klineList.remove(kline);
                                String message = String.format("Removed K-Line for %s", hostmask);

                                if(from != null) {
                                    ircConnectionService.notice(ircServiceTask.getIRCConnection(), from.getNick(), message);
                                }

                                ircConnectionService.notice(ircServiceTask.getIRCConnection(), serviceChannel, message);
                                persistenceService.scheduleSave();
                            }
                    );
        }
    }

    @Override
    public void removeAllWithType(KLineType type) {
        klineList.removeIf(e -> e.getType() == type);
    }
}
