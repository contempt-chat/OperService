package com.ircnet.service.operserv.squery;

import com.ircnet.library.common.User;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;

import java.util.Map;
// Checked (FIXME)
/**
 * Handler for: /SQUERY OperService HELP
 */
public class SQueryCommandHelp extends SQueryCommand {
    @Value("${service.name}")
    private String serviceName;

    private Map<String, SQueryCommand> squeryCommandMap;

    public SQueryCommandHelp(String commandName) {
        super(commandName);
    }

    /**
     * Handler for: /SQUERY OperService HELP
     *
     * @param from User who sent the SQUERY
     * @param tags IRCv3 message tags
     * @param message "HELP" or "HELP <command>"
     */
    @Override
    public void processCommand(User from, Map<String, String> tags, String message) {
        String[] parts = message.split(" ");

        if(parts.length == 1) {
            // HELP
            notice(from.getNick(), "%s help index", serviceName); // FIXME: index?
            notice(from.getNick(), "Use /SQUERY %s %s <topic>", serviceName, commandName);
            notice(from.getNick(), "Available topics: %s", StringUtils.join((squeryCommandMap).keySet(), ", "));
        }
        else {
            // HELP <command>
            SQueryCommand squeryCommand = squeryCommandMap.get(parts[1]);

            if(squeryCommand != null) {
                squeryCommand.processHelp(from, message);
            }
            else {
                notice(from.getNick(), "No such help topic: \"%s\". Use /SQUERY %s %s", parts[1], serviceName, commandName);
            }
        }
    }

    /**
     * Handler for: /SQUERY OperService HELP HELP ;-)
     *
     * @param from User who sent the SQUERY
     * @param message "HELP HELP"
     */
    @Override
    public void processHelp(User from, String message) {
        String[] parts = message.split(" ");
        notice(from.getNick(), "No such help topic: \"%s\". Use /SQUERY %s HELP", parts[1], serviceName);
    }

    public void setSqueryCommandMap(Map<String, SQueryCommand> squeryCommandMap) {
        this.squeryCommandMap = squeryCommandMap;
    }
}
