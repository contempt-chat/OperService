package com.ircnet.service.operserv.event;

import com.ircnet.library.common.event.AbstractEventListener;
import com.ircnet.library.service.event.NickChangeEvent;
import com.ircnet.service.operserv.ServiceProperties;
import com.ircnet.service.operserv.irc.IRCUser;
import com.ircnet.service.operserv.irc.UserService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Event for NICK message.
 */
@Component
public class NickChangedEventListener extends AbstractEventListener<NickChangeEvent> {
    private static final Logger LOGGER = LoggerFactory.getLogger(NickChangedEventListener.class);

    @Autowired
    private UserService userService;

    @Autowired
    private ServiceProperties properties;

    protected void onEvent(NickChangeEvent event) {
        LOGGER.trace("NickEvent uid={} newNick={}", event.getUid(), event.getNewNick());

        IRCUser user = userService.findByUID(event.getUid());
        String oldNick = user.getNick();
        userService.rename(user, event.getNewNick());
        LOGGER.trace("Changed nick of {} to {}", oldNick, user.getNick());

        if(!event.getIRCConnection().isBurst() && StringUtils.isNotBlank(properties.getClientsChannel())) {
            ircConnectionService.notice(event.getIRCConnection(), properties.getClientsChannel(), "%s %s %s@%s %s",
                event.getUid(), oldNick, user.getUser(), user.getHost(), user.getNick());
        }
    }
}
