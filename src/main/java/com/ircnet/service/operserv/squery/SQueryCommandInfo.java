package com.ircnet.service.operserv.squery;

import com.ircnet.library.common.User;
import org.springframework.beans.factory.annotation.Value;

import java.util.Map;
// Checked (FIXME)
/**
 * Handler for:
 *  - /SQUERY OperService INFO
 *  - /SQUERY OperService HELP INFO
 */
public class SQueryCommandInfo extends SQueryCommand {
    @Value("${service.squery.info}")
    private String serviceInfo;

    public SQueryCommandInfo(String commandName) {
        super(commandName);
    }

    /**
     * Handler for: /SQUERY OperService INFO
     *
     * @param from User who sent the SQUERY
     * @param tags IRCv3 message tags
     * @param message "INFO"
     */
    @Override
    public void processCommand(User from, Map<String, String> tags, String message) {
        notice(from.getNick(), serviceInfo);
    }

    /**
     * Handler for: /SQUERY OperService HELP INFO
     *
     * @param from User who sent the SQUERY
     * @param message "HELP INFO"
     */
    @Override
    public void processHelp(User from, String message) {
        notice(from.getNick(), "Shows information about this software");
    }
}
