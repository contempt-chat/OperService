package com.ircnet.service.operserv.event;

import com.ircnet.library.common.event.AbstractEventListener;
import com.ircnet.library.service.event.UNickEvent;
import com.ircnet.service.operserv.DNSBL.DNSBLervice;
import com.ircnet.service.operserv.IpAddressFamily;
import com.ircnet.service.operserv.Util;
import com.ircnet.service.operserv.irc.IRCUser;
import com.ircnet.service.operserv.irc.UserService;
import com.ircnet.service.operserv.kline.KLine;
import com.ircnet.service.operserv.kline.KLineService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class UNickEventListener extends AbstractEventListener<UNickEvent> {
    private static final Logger LOGGER = LoggerFactory.getLogger(UNickEventListener.class);

    @Autowired
    private UserService userService;

    @Autowired
    private KLineService klineService;

    @Autowired
    private DNSBLervice dblService;

    @Value("${service.channel}")
    private String serviceChannel;

    @Value("${service.channel.clients:#{null}}")
    private String clientsChannel;

    protected void onEvent(UNickEvent event) {
        LOGGER.trace("UNickEvent sid={} uid={} nick={} user={} host={} ipAddress={} userModes={} account={} realName={}",
                event.getSid(), event.getUid(), event.getNick(), event.getUser(), event.getHost(), event.getIpAddress(),
                event.getUserModes(), event.getAccount(), event.getRealName());

        if(!event.getIRCConnection().isBurst() && StringUtils.isNotBlank(clientsChannel)) {
            ircConnectionService.notice(event.getIRCConnection(), clientsChannel, "%s %s %s@%s CONN %s account=%s realName=%s",
                event.getUid(), event.getNick(), event.getUser(), event.getHost(), event.getIpAddress(),
                event.getAccount(), event.getRealName());
        }

        // Add user
        IRCUser user = new IRCUser();
        user.setSid(event.getSid());
        user.setUid(event.getUid());
        user.setNick(event.getNick());
        user.setUser(event.getUser());
        user.setHost(event.getHost());
        user.setIpAddress(event.getIpAddress());

        IpAddressFamily ipAddressFamily = Util.findAddressFamily(user.getIpAddress());

        if(ipAddressFamily != null) {
            user.setIpAddressFamily(ipAddressFamily);
        }
        else {
            LOGGER.error("Could not determine address type for '{}'", user.getIpAddress());
        }

        user.setUserModes(event.getUserModes());
        user.setAccount(event.getAccount());
        user.setRealName(event.getRealName());

        userService.add(user);

        // Check if the user matches a K-Line. Should we wait for end of burst?
        KLine kline = klineService.findMatchingKLine(user);

        if(kline != null) {
            String message = String.format("Enforcing TKLine on %s for %s (%s@%s) matching %s: %s",
                user.getSid(), user.getNick(), user.getUser(), user.getHost(), kline.toHostmask(), kline.getReason());
            LOGGER.info(message);
            ircConnectionService.notice(event.getIRCConnection(), serviceChannel, message);
            klineService.enforceKLine(kline, user.getSid());
        }

        /*
         * Check if the IP address is listed in DNBLs, if:
         *  1. it is an IPv4 (what about IPv6 support?)
         *  2. the IPv4 is not a private or cloaked
         *  3. if the user is not logged in
         */
        if("~-^".contains(String.valueOf(user.getUser().charAt(0)))
            && (user.getIpAddressFamily() == IpAddressFamily.IPV4 && !Util.isPrivateIPv4Address(user.getIpAddress()))
            && (user.getIpAddressFamily() == IpAddressFamily.IPV6 && !Util.isPrivateIPv6Address(user.getIpAddress()))
            && "*".equals(user.getAccount())
            && !user.getIpAddress().equals("255.255.255.255")) {
            dblService.check(user);
        }
    }
}
