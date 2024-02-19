package com.ircnet.service.operserv.match;

import com.ircnet.service.operserv.irc.IRCUser;

import java.util.List;

/**
 * Contains methods for matching against wildcards or CIDR notation.
 */
public interface MatchService {
    /**
     * Checks if a user matches the given parameters.
     *
     * @param user A user that is connected to IRC
     * @param username username or ident pattern
     * @param hostname hostname pattern (wildcards or CIDR notation)
     * @param isIpAddressOrRange true if the hostname is an IP address or range
     * @param sid SID pattern
     * @param excludeSASL Do not match users that are authenticated via SASL
     * @param excludeUsersWithIdent Do not match users that have ident
     * @return true if the user matches the given parameters
     */
    boolean isMatching(IRCUser user, String username, String hostname, boolean isIpAddressOrRange, String sid,
                       boolean excludeSASL, boolean excludeUsersWithIdent);

    /**
     * Finds users that are connected to IRC that match the given parameters.
     *
     * @param username username or ident pattern
     * @param hostname hostname pattern (wildcards or CIDR notation)
     * @param isIpAddressOrRange true if the hostname is an IP address or range
     * @param sid SID pattern
     * @param account Account name or null to match anything
     * @param excludeUsersWithIdent Do not match users that have ident
     * @return A list of users that are connected to IRC
     */
    List<IRCUser> findMatching(String username, String hostname, boolean isIpAddressOrRange, String sid, String account, boolean excludeUsersWithIdent);
}
