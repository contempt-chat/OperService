package com.ircnet.service.operserv.squery;

import com.ircnet.library.common.User;
import com.ircnet.service.operserv.kline.KLineService;
import com.ircnet.service.operserv.sasl.AccountService;
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
 *  - /SQUERY OperService RELOAD
 *  - /SQUERY OperService HELP RELOAD
 */
public class SQueryCommandReload extends SQueryCommand {
    private static final Logger LOGGER = LoggerFactory.getLogger(SQueryCommandReload.class);

    private static final String PARAMETER_ALL = "all";
    private static final String PARAMETER_ACCOUNTS = "accounts";
    private static final String PARAMETER_KLINES = "klines";

    @Autowired
    private AccountService accountService;

    @Autowired
    private KLineService klineService;

    @Value("${service.name}")
    private String serviceName;

    private Options options;

    public SQueryCommandReload(String commandName) {
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
            .desc("Loads all data from webservice")
            .build();

        Option accounts = Option.builder(PARAMETER_ACCOUNTS)
            .argName(PARAMETER_ACCOUNTS)
            .desc("Loads accounts from webservice")
            .build();

        Option klines = Option.builder(PARAMETER_KLINES)
            .argName(PARAMETER_KLINES)
            .desc("Loads K-Lines from webservice")
            .build();

        options.addOption(all);
        options.addOption(accounts);
        options.addOption(klines);
    }

    /**
     * Handler for: /SQUERY OperService RELOAD
     *
     * @param from User who sent the SQUERY
     * @param tags IRCv3 message tags
     * @param message "RELOAD [options]"
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

            boolean loadAccounts;
            boolean loadKLines;

            if (commandLine.hasOption(PARAMETER_ALL)) {
                loadAccounts = true;
                loadKLines = true;
            }
            else {
                loadAccounts = commandLine.hasOption(PARAMETER_ACCOUNTS);
                loadKLines = commandLine.hasOption(PARAMETER_KLINES);
            }

            if(!loadAccounts && !loadKLines) {
                sendSyntax(from.getNick());
                return;
            }

            if(loadAccounts) {
                accountService.loadFromAPI(from);
            }

            if(loadKLines) {
                klineService.loadFromAPI(from);
            }
        }
        catch (ParseException e) {
            LOGGER.debug("Failed to parse '{}' from {}", message, from, e);
            sendSyntax(from.getNick());
        }
    }

    /**
     * Handler for: /SQUERY OperService HELP RELOAD
     *
     * @param from User who sent the SQUERY
     * @param message "HELP RELOAD"
     */
    @Override
    public void processHelp(User from, String message) {
        notice(from.getNick(), "Fetches authorized accounts and K-Lines from webservice");
        sendSyntax(from.getNick());
    }

    private void sendSyntax(String nick) {
        notice(nick, "Usage: /SQUERY %s %s [options]", serviceName, commandName);
        sendOptionSyntax(nick, options);
    }
}
