package com.ircnet.service.operserv.squery;

import com.ircnet.library.common.User;
import com.ircnet.service.operserv.kline.KLine;
import com.ircnet.service.operserv.kline.KLineService;
import com.ircnet.service.operserv.kline.KLineType;
import org.apache.commons.cli.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import javax.annotation.PostConstruct;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
// Checked (FIXME)
/**
 * Handler for:
 *  - /SQUERY OperService KLINES
 *  - /SQUERY OperService HELP KLINES
 */
public class SQueryCommandKLineList extends SQueryCommand {
    private static final Logger LOGGER = LoggerFactory.getLogger(SQueryCommandKLineList.class);

    private static final String PARAMETER_ALL = "all";
    private static final String PARAMETER_FROM = "from";
    private static final String PARAMETER_SID = "sid";
    private static final String PARAMETER_TOR = "tor";
    private static final String PARAMETER_DNSBL = "dnsbl";

    @Autowired
    private KLineService klineService;

    @Value("${service.name}")
    private String serviceName;

    private Options options;

    public SQueryCommandKLineList(String commandName) {
        super(commandName);
    }

    /**
     * Prepares the option parser.
     */
    @PostConstruct
    protected void init() {
        this.options = new Options();

        Option all = Option.builder(PARAMETER_ALL)
                .argName(PARAMETER_ALL)
                .desc("Show all K-Lines")
                .build();
        options.addOption(all);

        Option from = Option.builder(PARAMETER_FROM)
            .hasArg()
            .argName(PARAMETER_FROM)
            .desc("Show only K-Lines set by this user")
            .build();
        options.addOption(from);

        Option sid = Option.builder(PARAMETER_SID)
            .hasArg()
            .argName(PARAMETER_SID)
            .desc("SID pattern")
            .build();

        options.addOption(sid);

        Option tor = Option.builder(PARAMETER_TOR)
                .argName(PARAMETER_TOR)
                .desc("Show only K-Lines for Tor exit nodes")
                .build();
        options.addOption(tor);

        Option dnsbl = Option.builder(PARAMETER_DNSBL)
                .argName(PARAMETER_DNSBL)
                .desc("Show only K-Lines from DNSBL")
                .build();
        options.addOption(dnsbl);
    }

    /**
     * Handler for: /SQUERY OperService KLINE
     *
     * @param from User who sent the SQUERY
     * @param tags IRCv3 message tags
     * @param message "KLINE [options]"
     */
    @Override
    public void processCommand(User from, Map<String, String> tags, String message) {
        String[] parts = message.split(" ");

        if(parts.length < 1) {
            sendSyntax(from.getNick());
            return;
        }

        try {
            String[] requestArguments = Arrays.copyOfRange(parts, 1, parts.length);
            CommandLineParser parser = new DefaultParser();
            CommandLine commandLine = parser.parse(options, requestArguments);

            boolean showAll = commandLine.hasOption(PARAMETER_ALL);
            String createdBy = null;
            String sid = null;
            boolean showOnlyTor = false;
            boolean showOnlyDNSBL = false;

            if (commandLine.hasOption(PARAMETER_FROM)) {
                createdBy = commandLine.getOptionValue(PARAMETER_FROM);
            }

            if (commandLine.hasOption(PARAMETER_SID)) {
                sid = commandLine.getOptionValue(PARAMETER_SID);
            }

            if (commandLine.hasOption(PARAMETER_TOR)) {
                showOnlyTor = true;
            }

            if (commandLine.hasOption(PARAMETER_DNSBL)) {
                showOnlyDNSBL = true;
            }

            List<KLine> klineList;

            if(showAll) {
                klineList = klineService.findAllNotExpired();
            }
            else if (showOnlyTor) {
                klineList = klineService.findAllWithTypes(KLineType.TOR);
            }
            else if (showOnlyDNSBL) {
                klineList = klineService.findAllWithTypes(KLineType.DNSBL);
            }
            else {
                klineList = klineService.findAllWithTypes(KLineType.SYNCED, KLineType.NOT_SYNCED);
            }

            int count = 0;

            for (KLine kline : klineList) {
                if (createdBy != null && !StringUtils.equalsIgnoreCase(kline.getCreatedBy(), createdBy)) {
                    continue;
                }

                if(sid != null && !StringUtils.equalsIgnoreCase(kline.getSid(), sid)) {
                    continue;
                }

                if (showOnlyTor) {
                    notice(from.getNick(), "%s", kline.toHostmask());
                }
                else if(kline.getType() == KLineType.TOR) {
                    notice(from.getNick(), "%s (%s)", kline.toHostmask(), kline.getReason());
                }
                else {
                    StringBuilder content = new StringBuilder(kline.toHostmask());

                    if(kline.getCreatedBy() != null) {
                        content.append(" by ");
                        content.append(kline.getCreatedBy());
                    }

                    if(kline.getSid() != null) {
                        content.append(String.format(" on %s", kline.getSid()));
                    }

                    if (kline.getExpirationDate() != null) {
                        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm");
                        content.append(", expires: ");
                        content.append(sdf.format(kline.getExpirationDate()));
                    }

                    content.append(String.format(" (%s)", kline.getReason()));
                    notice(from.getNick(), content.toString());
                }

                count++;
            }

            if (showOnlyTor) {
                notice(from.getNick(), "Found %d K-Lines for Tor exit nodes", count);
            }
            else if (showOnlyDNSBL) {
                notice(from.getNick(), "Found %d K-Lines from DNSBLs", count);
            }
            else {
                StringBuilder content = new StringBuilder(String.format("Found %d K-Lines", count));

                if(createdBy != null) {
                    content.append(String.format(" set by %s", createdBy));
                }

                if(sid != null) {
                    content.append(String.format(" on %s", sid));
                }

                notice(from.getNick(), content.toString());
            }
        }
        catch (ParseException e) {
            LOGGER.debug("Failed to parse '{}' from {}", message, from, e);
            sendSyntax(from.getNick());
        }
    }


    /**
     * Handler for: /SQUERY OperService HELP KLINES
     *
     * @param from User who sent the SQUERY
     * @param message "HELP KLINES"
     */
    @Override
    public void processHelp(User from, String message) {
        notice(from.getNick(), "Lists K-Lines");
        sendSyntax(from.getNick());
    }

    private void sendSyntax(String nick) {
        notice(nick, "Usage: /SQUERY %s %s [options]", serviceName, commandName);
        sendOptionSyntax(nick, options);
    }
}
