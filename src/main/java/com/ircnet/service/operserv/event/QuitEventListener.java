package com.ircnet.service.operserv.event;

import com.ircnet.library.common.event.AbstractEventListener;
import com.ircnet.library.service.event.QuitEvent;
import com.ircnet.service.operserv.irc.IRCUser;
import com.ircnet.service.operserv.irc.UserService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class QuitEventListener extends AbstractEventListener<QuitEvent> {
    private static final Logger LOGGER = LoggerFactory.getLogger(QuitEventListener.class);

    @Autowired
    private UserService userService;

    @Value("${service.channel.clients:#{null}}")
    private String clientsChannel;

    protected void onEvent(QuitEvent event) {
        LOGGER.trace("QuitEvent uidOrNick={} message={}", event.getUid(), event.getMessage());

        IRCUser user = userService.findByUIDorNick(event.getUid());

        if(user != null) {
            if(!event.getIRCConnection().isBurst() && StringUtils.isNotBlank(clientsChannel)) {
                ircConnectionService.notice(event.getIRCConnection(), clientsChannel, "%s %s %s@%s QUIT message=%s",
                    user.getUid(), user.getNick(), user.getUser(), user.getHost(), event.getMessage());
            }

            userService.remove(user);
        }
    }
}

