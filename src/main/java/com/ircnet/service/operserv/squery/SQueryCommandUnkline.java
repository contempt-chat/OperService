package com.ircnet.service.operserv.squery;

import com.ircnet.library.common.User;
import com.ircnet.service.operserv.kline.KLineService;
import org.apache.commons.cli.*;
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
 *  - /SQUERY OperService UNKLINE
 *  - /SQUERY OperService HELP UNKLINE
 */
public class SQueryCommandUnkline extends SQueryCommand {
    private static final Logger LOGGER = LoggerFactory.getLogger(SQueryCommandUnkline.class);

    @Autowired
    private KLineService klineService;

    @Value("${service.name}")
    private String serviceName;

    private Options options;

    public SQueryCommandUnkline(String commandName) {
        super(commandName);
    }

    /**
     * Prepares the option parser.
     */
    @PostConstruct
    protected void init() {
        this.options = new Options();

        Option sid = Option.builder("sid")
            .hasArg()
            .argName("sid")
            .desc("SID pattern")
            .build();

        options.addOption(sid);
    }

    /**
     * Handler for: /SQUERY OperService UNKLINE
     *
     * @param from User who sent the SQUERY
     * @param tags IRCv3 message tags
     * @param message "UNKLINE [options]"
     */
    @Override
    public void processCommand(User from, Map<String, String> tags, String message) {
        String[] parts = message.split(" ");

        if(parts.length < 2) {
            sendSyntax(from.getNick());
            return;
        }

        try {
            String[] requestArguments = Arrays.copyOfRange(parts, 1, parts.length);
            CommandLineParser parser = new DefaultParser();
            CommandLine commandLine = parser.parse(options, requestArguments);

            if (commandLine.getArgList().size() != 1) {
                sendSyntax(from.getNick());
                return;
            }

            String sid = null;

            if (commandLine.hasOption("sid")) {
                sid = commandLine.getOptionValue("sid");
            }

            String hostmask = commandLine.getArgList().get(0);
            klineService.remove(from, hostmask, sid);
        }
        catch (ParseException e) {
            LOGGER.debug("Failed to parse '{}' from {}", message, from, e);
            sendSyntax(from.getNick());
        }
    }

    /**
     * Handler for: /SQUERY OperService HELP UNKLINE
     *
     * @param from User who sent the SQUERY
     * @param message "HELP UNKLINE"
     */
    @Override
    public void processHelp(User from, String message) {
        notice(from.getNick(), "Removes a K-Line");
        sendSyntax(from.getNick());
    }

    private void sendSyntax(String nick) {
        notice(nick, "Usage: /SQUERY %s %s [options] <user@host>", serviceName, commandName);
        sendOptionSyntax(nick, options);
    }
}
