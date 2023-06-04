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
    private static final String PARAMETER_ACCOUNT = "account";

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

        Option account = Option.builder(PARAMETER_ACCOUNT)
            .hasArg()
            .argName(PARAMETER_ACCOUNT)
            .desc("account name")
            .build();
        options.addOption(account);
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
            String account = null;

            if (commandLine.hasOption(PARAMETER_SID)) {
                sid = commandLine.getOptionValue(PARAMETER_SID);
            }
            if (commandLine.hasOption(PARAMETER_ACCOUNT)) {
                account = commandLine.getOptionValue(PARAMETER_ACCOUNT);
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

            StringBuilder stringBuilder = new StringBuilder("Searching for users matching hostmask ");
            stringBuilder.append(hostmask);

            if(sid != null) {
                stringBuilder.append(" on ");
                stringBuilder.append(sid);
            }

            if(account != null) {
                stringBuilder.append(" logged in as \"");
                stringBuilder.append(account);
                stringBuilder.append("\"");
            }

            notice(from.getNick(), stringBuilder.toString());

            List<IRCUser> matchingUsers = matchService.findMatching(identAndHost[0], identAndHost[1], isIpAddressOrRange, sid, account);

            if (!matchingUsers.isEmpty()) {
                for (IRCUser user : matchingUsers) {
                    stringBuilder = new StringBuilder(user.getNick());
                    stringBuilder.append(String.format(" (%s@%s", user.getUser(), user.getHost()));

                    if(!user.getHost().equals(user.getIpAddress())) {
                        stringBuilder.append(" / ");
                        stringBuilder.append(user.getIpAddress());
                    }

                    stringBuilder.append(")");
                    stringBuilder.append(" on ");
                    stringBuilder.append(user.getSid());

                    if(user.getAccount() != null) {
                        stringBuilder.append(" logged in as ");
                        stringBuilder.append(user.getAccount());
                    }

                    notice(from.getNick(), stringBuilder.toString());
                }

                notice(from.getNick(), "Found %d users", matchingUsers.size());
            }
            else {
                notice(from.getNick(), "No users could be found");

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

            notice(nick, "/SQUERY %s %s -account someone *@*", serviceName, commandName);
            notice(nick, "  Finds users logged in as \"someone\"");

            notice(nick, "/SQUERY %s %s -account 0 *@*", serviceName, commandName);
            notice(nick, "  Finds users that are not logged in");
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
