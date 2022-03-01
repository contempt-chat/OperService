package com.ircnet.service.operserv.squery;

import com.ircnet.library.common.User;
import com.ircnet.library.common.connection.IRCConnectionService;
import com.ircnet.library.service.IRCServiceTask;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;
// Checked (FIXME)
/**
 * Handler for:
 *  - /SQUERY OperService COMMAND
 *  - /SQUERY OperService HELP COMMAND
 */
public abstract class SQueryCommand {
    @Autowired
    protected IRCConnectionService ircConnectionService;

    @Autowired
    protected IRCServiceTask ircServiceTask;

    protected String commandName;

    public SQueryCommand(String commandName) {
        this.commandName = commandName;
    }

    /**
     * Sends a notice.
     *
     * @param target Target nick
     * @param format format string
     * @param args Arguments
     */
    protected void notice(String target, String format, Object... args) {
        ircConnectionService.notice(ircServiceTask.getIRCConnection(), target, format, args);
    }

    /**
     * Handler for: /SQUERY OperService COMMAND
     *
     * @param from User who sent the SQUERY
     * @param tags IRCv3 message tags
     * @param message A message starting with "COMMAND"
     */
    abstract public void processCommand(User from, Map<String, String> tags, String message);

    /**
     * Handler for: /SQUERY OperService HELP COMMAND
     *
     * @param from User who sent the SQUERY
     * @param message "HELP COMMAND"
     */
    abstract public void processHelp(User from, String message);

    protected void sendOptionSyntax(String nick, Options options) {
        for(Option option : options.getOptions()) {
            StringBuilder stringBuilder = new StringBuilder();

            if (option.getOpt() == null) {
                stringBuilder.append("   ").append("--").append(option.getLongOpt());
            } else {
                stringBuilder.append("-").append(option.getOpt());
                if (option.hasLongOpt()) {
                    stringBuilder.append(',').append("--").append(option.getLongOpt());
                }
            }

            if (option.hasArg()) {
                String argName = option.getArgName();
                if (argName != null && argName.length() == 0) {
                    stringBuilder.append(' ');
                } else {
                    stringBuilder.append(option.hasLongOpt() ? " " : " ");
                    stringBuilder.append("<").append(argName != null ? option.getArgName() : "arg").append(">");
                }
            }

            notice(nick, " %-20s %s", stringBuilder.toString(), option.getDescription() != null ? option.getDescription() : "");
        }
    }

    public String getCommandName() {
        return commandName;
    }
}
