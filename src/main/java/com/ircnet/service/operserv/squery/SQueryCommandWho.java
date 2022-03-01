package com.ircnet.service.operserv.squery;

import com.ircnet.library.common.User;
import com.ircnet.service.operserv.Util;
import com.ircnet.service.operserv.irc.IRCUser;
import com.ircnet.service.operserv.match.MatchService;
import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Handler for:
 *  - /SQUERY OperService WHO
 *  - /SQUERY OperService HELP WHO
 */
public class SQueryCommandWho extends SQueryCommand {
    private static final Logger LOGGER = LoggerFactory.getLogger(SQueryCommandWho.class);

    private static final String PARAMETER_SID = "sid";

    @Autowired
    private MatchService matchService;

    @Value("${service.name}")
    private String serviceName;

    private Options options;

    public SQueryCommandWho(String commandName) {
        super(commandName);
    }

    /**
     * Prepares the option parser.
     */
    @PostConstruct
    protected void init() {
        this.options = new Options();

        Option sid = Option.builder(PARAMETER_SID)
                .hasArg()
                .argName(PARAMETER_SID)
                .desc("SID pattern")
                .build();

        options.addOption(sid);
    }

    /**
     * Handler for: /SQUERY OperService WHO
     *
     * @param from User who sent the SQUERY
     * @param tags IRCv3 message tags
     * @param message "WHO [options] <user@host>"
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

            if (commandLine.getArgList().size() < 1) {
                sendSyntax(from.getNick());
                return;
            }

            String sid = null;

            if (commandLine.hasOption(PARAMETER_SID)) {
                sid = commandLine.getOptionValue(PARAMETER_SID);
            }

            String hostmask = commandLine.getArgList().get(0);

            if (!hostmask.contains("@")) {
                sendSyntax(from.getNick());
                return;
            }

            String identAndHost[] = hostmask.split("@");

            if (identAndHost.length != 2) {
                sendSyntax(from.getNick());
                return;
            }

            boolean isIpAddressOrRange = Util.isIpAddressOrRange(identAndHost[1]);
            List<IRCUser> matchingUsers = matchService.findMatching(identAndHost[0], identAndHost[1], isIpAddressOrRange, sid);

            if (!matchingUsers.isEmpty()) {
                notice(from.getNick(), "Users matching hostmask %s%s", hostmask, sid != null ? (" on " + sid) : "");

                for (IRCUser user : matchingUsers) {
                    notice(from.getNick(), "  %s (%s@%s / %s) on %s %s", user.getNick(), user.getUser(), user.getHost(), user.getIpAddress(), user.getSid(),
                            (!user.getAccount().equals("*") ? "logged in as " + user.getAccount() : ""));
                }
            }
            else {
                notice(from.getNick(), "No users could be found that match hostmask %s%s", hostmask, sid != null ? (" on " + sid) : "");
            }
        }
        catch (ParseException e) {
            LOGGER.debug("Failed to parse '{}' from {}", message, from, e);
            sendSyntax(from.getNick());
        }
    }

    /**
     * Handler for: /SQUERY OperService HELP WHO
     *
     * @param from User who sent the SQUERY
     * @param message "HELP WHO"
     */
    @Override
    public void processHelp(User from, String message) {
        String nick = from.getNick();

        String[] args = message.split(" ");

        if (args.length > 2 && args[2].equalsIgnoreCase("EXAMPLES")) {
            notice(nick, "WHO Examples:");
            notice(nick, "/SQUERY %s %s foo@1.2.3.0/24", serviceName, commandName);
            notice(nick, "  Finds users with ident \"foo\" that belong to the ip range 1.2.3.0/24");

            notice(nick, "/SQUERY %s %s -sid 00A *@*.example.com", serviceName, commandName);
            notice(nick, "  Finds users with any ident that have a hostname matching *.example.com on servers whose SID is starting with 00A");
        } else {
            sendSyntax(nick);
        }
    }

    private void sendSyntax(String nick) {
        notice(nick, "Usage: /SQUERY %s %s <user@host>", serviceName, commandName);
        sendOptionSyntax(nick, options);
        notice(nick, "For examples use /SQUERY %s HELP %s EXAMPLES", serviceName, commandName);
    }
}
