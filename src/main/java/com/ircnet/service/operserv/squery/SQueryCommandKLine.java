package com.ircnet.service.operserv.squery;

import com.ircnet.library.common.User;
import com.ircnet.service.operserv.Util;
import com.ircnet.service.operserv.Wdhms2sec;
import com.ircnet.service.operserv.kline.KLineService;
import org.apache.commons.cli.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.Map;
// Checked (FIXME)
/**
 * Handler for:
 *  - /SQUERY OperService KLINE
 *  - /SQUERY OperService HELP KLINE
 */
public class SQueryCommandKLine extends SQueryCommand {
    private static final Logger LOGGER = LoggerFactory.getLogger(SQueryCommandKLine.class);

    private static final String PARAMETER_TIME = "time";
    private static final String PARAMETER_SID = "sid";
    private static final String PARAMETER_TEST = "test";

    @Autowired
    private KLineService klineService;

    @Value("${service.name}")
    private String serviceName;

    private Options options;

    public SQueryCommandKLine(String commandName) {
        super(commandName);
    }

    /**
     * Prepares the option parser.
     */
    @PostConstruct
    protected void init() {
        this.options = new Options();

        Option min = Option.builder(PARAMETER_TIME)
                .hasArg()
                .argName(PARAMETER_TIME)
                .desc("expiration time in format 1w2d3h4m5s")
                .build();

        options.addOption(min);

        Option sid = Option.builder(PARAMETER_SID)
            .hasArg()
            .argName(PARAMETER_SID)
            .desc("SID pattern")
            .build();

        options.addOption(sid);

        Option max = Option.builder("test")
                .desc("only simulate K-LINE and show who would be banned")
                .build();

        options.addOption(max);
    }

    /**
     * Handler for: /SQUERY OperService KLINE
     *
     * @param from User who sent the SQUERY
     * @param tags IRCv3 message tags
     * @param message "KLINE [options] <hostmask> <reason>"
     */
    @Override
    public void processCommand(User from, Map<String, String> tags, String message) {
        String[] parts = message.split(" ");

        if(parts.length < 3) {
            sendSyntax(from.getNick());
            return;
        }

        try {
            String[] requestArguments = Arrays.copyOfRange(parts, 1, parts.length);
            CommandLineParser parser = new DefaultParser();
            CommandLine commandLine = parser.parse(options, requestArguments);

            if (commandLine.getArgList().size() < 2) {
                sendSyntax(from.getNick());
                return;
            }

            Long durationSeconds = null;
            String sid = null;
            boolean dryRun = false;

            if (commandLine.hasOption(PARAMETER_TEST)) {
                dryRun = true;
            }

            if (commandLine.hasOption(PARAMETER_SID)) {
                sid = commandLine.getOptionValue(PARAMETER_SID);
            }

            if(commandLine.hasOption(PARAMETER_TIME)) {
                String durationString = commandLine.getOptionValue(PARAMETER_TIME);

                try {
                    durationSeconds = new Wdhms2sec(durationString).parse();

                    if(durationSeconds <= 0) {
                        throw new IllegalStateException();
                    }
                }
                catch (Exception e) {
                    LOGGER.info("Illegal duration '{}' from {}", durationString, from.getNick());
                    sendSyntax(from.getNick());
                }
            }

            String hostmask = commandLine.getArgList().get(0);
            String reason = StringUtils.join(Arrays.copyOfRange(commandLine.getArgs(), 1, commandLine.getArgs().length), " ");

            if(!hostmask.contains("@")) {
                sendSyntax(from.getNick());
            }

            String identAndHost[] = hostmask.split("@");

            if(identAndHost.length != 2) {
                sendSyntax(from.getNick());
                return;
            }

            // Save K-Line
            klineService.create(identAndHost[0],
                    identAndHost[1],
                    Util.isIpAddressOrRange(hostmask),
                    reason,
                    from,
                    tags.get("account"),
                    durationSeconds,
                    sid,
                    dryRun,
                    false);
        }
        catch (ParseException e) {
            LOGGER.debug("Failed to parse '{}' from {}", message, from, e);
            sendSyntax(from.getNick());
        }
    }

    /**
     * Handler for: /SQUERY OperService HELP KLINE
     *
     * @param from User who sent the SQUERY
     * @param message "HELP KLINE"
     */
    @Override
    public void processHelp(User from, String message) {
        notice(from.getNick(), "Adds a K-LINE");
        sendSyntax(from.getNick());
    }

    private void sendSyntax(String nick) {
        notice(nick, "Usage: /SQUERY %s %s [options] <user@host> <reason>", serviceName, commandName);
        sendOptionSyntax(nick, options);
        notice(nick, "For examples use /SQUERY %s HELP %s EXAMPLES", serviceName, commandName);
    }
}
