package com.ircnet.service.operserv.match;

import com.ircnet.service.operserv.irc.IRCUser;

import java.util.List;
// Checked (FIXME)
/**
 * Contains methods for matching against wildcards or CIDR notation.
 */
public interface MatchService {
    /**
     * Checks if an user matches the given parameters.
     *
     * @param user An user that is connected to IRC
     * @param username username or ident pattern
     * @param hostname hostname pattern (wildcards or CIDR notation)
     * @param isIpAddressOrRange true if the hostname is an IP address or range
     * @param sid SID pattern
     * @return true if the user matches the given parameters
     */
    boolean isMatching(IRCUser user, String username, String hostname, boolean isIpAddressOrRange, String sid);

    /**
     * Finds users that are connected to IRC that match the given parameters.
     *
     * @param username username or ident pattern
     * @param hostname hostname pattern (wildcards or CIDR notation)
     * @param isIpAddressOrRange true if the hostname is an IP address or range
     * @param sid SID pattern
     * @return A list of users that are connected to IRC
     */
    List<IRCUser> findMatching(String username, String hostname, boolean isIpAddressOrRange, String sid);
}
