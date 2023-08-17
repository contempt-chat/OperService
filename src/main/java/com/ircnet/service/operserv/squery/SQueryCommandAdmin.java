package com.ircnet.service.operserv.squery;

import com.ircnet.library.common.User;
import com.ircnet.service.operserv.ServiceProperties;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

/**
 * Handler for:
 *  - /SQUERY OperService ADMIN
 *  - /SQUERY OperService HELP ADMIN
 */
public class SQueryCommandAdmin extends SQueryCommand {
    @Autowired
    private ServiceProperties properties;

    public SQueryCommandAdmin(String commandName) {
        super(commandName);
    }

    /**
     * Handler for: /SQUERY OperService ADMIN
     *
     * @param from User who sent the SQUERY
     * @param tags IRCv3 message tags
     * @param message "ADMIN"
     */
    @Override
    public void processCommand(User from, Map<String, String> tags, String message) {
        notice(from.getNick(), "Administrative info about %s:", properties.getName());
        notice(from.getNick(), properties.getSquery().getAdmin());
    }

    /**
     * Handler for: /SQUERY OperService HELP ADMIN
     *
     * @param from User who sent the SQUERY
     * @param message "HELP ADMIN"
     */
    @Override
    public void processHelp(User from, String message) {
        notice(from.getNick(), "Shows administrative information");
    }
}
