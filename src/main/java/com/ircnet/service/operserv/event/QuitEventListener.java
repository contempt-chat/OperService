package com.ircnet.service.operserv.event;

import com.ircnet.library.common.connection.SingletonIRCConnectionService;
import com.ircnet.library.common.event.AbstractEventListener;
import com.ircnet.library.service.event.QuitEvent;
import com.ircnet.library.service.user.IRCUser;
import com.ircnet.service.operserv.ServiceProperties;
import com.ircnet.service.operserv.irc.UserService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class QuitEventListener extends AbstractEventListener<QuitEvent, SingletonIRCConnectionService> {
    private static final Logger LOGGER = LoggerFactory.getLogger(QuitEventListener.class);

    private UserService userService;
    private ServiceProperties properties;

    public QuitEventListener(SingletonIRCConnectionService ircConnectionService,
                             UserService userService,
                             ServiceProperties properties) {
        super(ircConnectionService);
        this.userService = userService;
        this.properties = properties;
    }

    protected void onEvent(QuitEvent event) {
        LOGGER.trace("QuitEvent uidOrNick={} message={}", event.getUid(), event.getMessage());

        IRCUser user = userService.findByUIDorNick(event.getUid());

        if(user == null) {
            LOGGER.error("Could not find user '{}'", event.getUid());
            return;
        }

        if(!event.getIRCConnection().isBurst() && StringUtils.isNotBlank(properties.getClientsChannel())) {
            ircConnectionService.notice(event.getIRCConnection(), properties.getClientsChannel(), "%s %s %s@%s QUIT message=%s", user.getUid(), user.getNick(), user.getUser(), user.getHost(), event.getMessage());
        }

        userService.remove(user);
    }
}

