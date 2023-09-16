package com.ircnet.service.operserv.event;

import com.ircnet.library.common.event.AbstractEventListener;
import com.ircnet.library.service.event.SQuitEvent;
import com.ircnet.service.operserv.irc.IRCServer;
import com.ircnet.service.operserv.irc.ServerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("squitEventListener")
public class SQuitEventListener extends AbstractEventListener<SQuitEvent> {
    private static final Logger LOGGER = LoggerFactory.getLogger(SQuitEventListener.class);

    @Autowired
    private ServerService serverService;

    protected void onEvent(SQuitEvent event) {
        LOGGER.trace("SQuitEvent sender={} serverName={} reason='{}'", event.getSender(), event.getServerName(), event.getReason());
        IRCServer server = serverService.findByName(event.getServerName());

        if(server != null) {
            serverService.remove(server.getSid());
        }
    }
}
