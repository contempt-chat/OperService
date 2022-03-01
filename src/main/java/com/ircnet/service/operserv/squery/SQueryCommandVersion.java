package com.ircnet.service.operserv.squery;

import com.ircnet.library.common.User;
import com.ircnet.service.operserv.Constants;

import java.util.Map;
// Checked (FIXME)
/**
 * Handler for:
 *  - /SQUERY OperService VERSION
 *  - /SQUERY OperService HELP VERSION
 */
public class SQueryCommandVersion extends SQueryCommand {
    public SQueryCommandVersion(String commandName) {
        super(commandName);
    }

    /**
     * Handler for: /SQUERY OperService VERSION
     *
     * @param from User who sent the SQUERY
     * @param tags IRCv3 message tags
     * @param message "VERSION"
     */
    @Override
    public void processCommand(User from, Map<String, String> tags, String message) {
        notice(from.getNick(), "OperService v%s", Constants.VERSION);
    }

    /**
     * Handler for: /SQUERY OperService HELP VERSION
     *
     * @param from User who sent the SQUERY
     * @param message "HELP VERSION"
     */
    @Override
    public void processHelp(User from, String message) {
        notice(from.getNick(), "Shows the current version");
    }
}
