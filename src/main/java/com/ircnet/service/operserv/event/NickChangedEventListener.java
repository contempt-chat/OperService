package com.ircnet.service.operserv.event;

import com.ircnet.library.common.connection.IRCConnectionService;
import com.ircnet.library.common.event.AbstractEventListener;
import com.ircnet.library.service.event.NickChangeEvent;
import com.ircnet.library.service.user.IRCUser;
import com.ircnet.service.operserv.ServiceProperties;
import com.ircnet.service.operserv.irc.UserService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Event for NICK message.
 */
@Component
public class NickChangedEventListener extends AbstractEventListener<NickChangeEvent, IRCConnectionService> {
    private static final Logger LOGGER = LoggerFactory.getLogger(NickChangedEventListener.class);

    private UserService userService;
    private ServiceProperties properties;

    public NickChangedEventListener(IRCConnectionService ircConnectionService,
                                    UserService userService,
                                    ServiceProperties properties) {
        super(ircConnectionService);
        this.userService = userService;
        this.properties = properties;
    }

    protected void onEvent(NickChangeEvent event) {
        LOGGER.trace("NickEvent uid={} newNick={}", event.getUid(), event.getNewNick());

        IRCUser user = userService.findByUID(event.getUid());
        String oldNick = user.getNick();
        userService.rename(user, event.getNewNick());

        if(!event.getIRCConnection().isBurst() && StringUtils.isNotBlank(properties.getClientsChannel())) {
            ircConnectionService.notice(event.getIRCConnection(), properties.getClientsChannel(), "%s %s %s@%s %s",
                event.getUid(), oldNick, user.getUser(), user.getHost(), user.getNick());
        }
    }
}
