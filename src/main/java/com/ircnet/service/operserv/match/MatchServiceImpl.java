package com.ircnet.service.operserv.match;

import com.ircnet.service.operserv.Util;
import com.ircnet.service.operserv.irc.IRCUser;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.web.util.matcher.IpAddressMatcher;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class MatchServiceImpl implements MatchService {
    @Autowired
    @Qualifier("userMapByUID")
    private Map<String, IRCUser> userMapByUID;

    @Override
    public boolean isMatching(IRCUser user, String username, String hostname, boolean isIpAddressOrRange, String sid,
                              boolean excludeSASL, boolean excludeIdent) {
        // Check SASL
        if(excludeSASL && user.getAccount() != null) {
            return false;
        }

        // Check ident
        if(excludeIdent && user.getUser().charAt(0) != '~' && user.getUser().charAt(0) != '-') {
            return false;
        }

        // Check SID
        if(StringUtils.isNotEmpty(sid) && !Util.matches(user.getServer().getSid(), sid)) {
            return false;
        }

        // Check username/ident
        if (username != null && !username.equals("*") && !Util.matches(user.getUser(), username)) {
            return false;
        }

        // Check match for IP address: exact (e.g. 1.2.3.4) or IP range (e.g. 1.2.3.0/24)
        if(isIpAddressOrRange) {
            IpAddressMatcher ipAddressMatcher = new IpAddressMatcher(hostname);

            if (ipAddressMatcher.matches(user.getIpAddress())) {
                return true;
            }
        }

        // Check match for IP address: wildcards (e.g. 1.2.3.*)
        if(Util.matches(user.getIpAddress(), hostname)) {
            return  true;
        }

        // Check if it matches the user's hostname
        if(hostname != null) {
            if (hostname.equals("*") || Util.matches(user.getHost(), hostname)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public List<IRCUser> findMatching(String username, String hostname, boolean isIpAddressOrRange, String sid,
                                      String accountName, boolean excludeSASL, boolean excludeIdent) {
        List<IRCUser> matchingUsers = new ArrayList<>();

        for (IRCUser user : userMapByUID.values()) {
            // Check SASL
            if(excludeSASL && user.getAccount() != null) {
                continue;
            }
            
            // Check ident
            if(excludeIdent && user.getUser().charAt(0) != '~' && user.getUser().charAt(0) != '-') {
                continue;
            }

            if(accountName != null) {
                if(accountName.equals("0")) {
                    if (user.getAccount() != null) {
                        // Searching for not logged-in users, but user is logged in
                        continue;
                    }
                }
                else if(user.getAccount() == null) {
                    // Searching for logged-in users, but user is not logged in
                    continue;
                }
                else if (!accountName.equals("*") && !Util.matches(user.getAccount(), accountName)) {
                    // Account name does not match
                    continue;
                }
            }

            if(isMatching(user, username, hostname, isIpAddressOrRange, sid, false, false)) {
                matchingUsers.add(user);
            }
        }

        return matchingUsers;
    }
}
