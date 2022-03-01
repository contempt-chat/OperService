package com.ircnet.service.operserv.squery;

import com.ircnet.library.common.User;
import org.springframework.beans.factory.annotation.Value;

import java.util.Map;
// Checked (FIXME)
/**
 * Handler for:
 *  - /SQUERY OperService ADMIN
 *  - /SQUERY OperService HELP ADMIN
 */
public class SQueryCommandAdmin extends SQueryCommand {
    @Value("${service.name}")
    private String serviceName;

    @Value("${service.squery.admin}")
    private String serviceAdmin;

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
        notice(from.getNick(), "Administrative info about %s:", serviceName);
        notice(from.getNick(), serviceAdmin);
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
