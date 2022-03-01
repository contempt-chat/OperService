package com.ircnet.service.operserv.squery;

import com.ircnet.library.common.User;
import com.ircnet.service.operserv.sasl.AccountService;
import org.apache.commons.cli.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
// Checked (FIXME)
/**
 * Handler for:
 *  - /SQUERY OperService STATUS
 *  - /SQUERY OperService HELP STATUS
 */
public class SQueryCommandStatus extends SQueryCommand {
    private static final Logger LOGGER = LoggerFactory.getLogger(SQueryCommandStatus.class);

    @Autowired
    private AccountService accountService;

    @Value("${service.name}")
    private String serviceName;

    private Options options;

    public SQueryCommandStatus(String commandName) {
        super(commandName);
    }

    /**
     * Prepares the option parser.
     */
    @PostConstruct
    protected void init() {
        this.options = new Options();
    }

    /**
     * Handler for: /SQUERY OperService STATUS
     *
     * @param from User who sent the SQUERY
     * @param tags IRCv3 message tags
     * @param message "STATUS"
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

            List<String> accounts = accountService.findAll();
            notice(from.getNick(), "Authorized accounts: %s", StringUtils.join(accounts, ", "));
        }
        catch (ParseException e) {
            LOGGER.debug("Failed to parse '{}' from {}", message, from, e);
            sendSyntax(from.getNick());
        }
    }

    /**
     * Handler for: /SQUERY OperService HELP STATUS
     *
     * @param from User who sent the SQUERY
     * @param message "HELP STATUS"
     */
    @Override
    public void processHelp(User from, String message) {
        notice(from.getNick(), "Fetches authorized accounts and K-Lines from webservice");
    }

    private void sendSyntax(String nick) {
        notice(nick, "Usage: /SQUERY %s %s", serviceName, commandName);
        sendOptionSyntax(nick, options);
    }
}
