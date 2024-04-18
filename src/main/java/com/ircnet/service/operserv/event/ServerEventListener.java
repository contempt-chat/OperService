package com.ircnet.service.operserv.event;

import com.ircnet.library.common.event.AbstractEventListener;
import com.ircnet.library.service.event.ServerEvent;
import com.ircnet.library.service.server.IRCServer;
import com.ircnet.service.operserv.irc.ServerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ServerEventListener extends AbstractEventListener<ServerEvent> {
    private static final Logger LOGGER = LoggerFactory.getLogger(ServerEventListener.class);

    @Autowired
    private ServerService serverService;

    protected void onEvent(ServerEvent event) {
        LOGGER.debug("ServerEvent serverName={} hopCount={} sid={} info='{}'", event.getServerName(), event.getHopCount(), event.getSid(), event.getInfo());
        IRCServer server = new IRCServer();
        server.setSid(event.getSid());
        server.setName(event.getServerName());
        server.setHopCount(event.getHopCount());
        server.setInfo(event.getInfo());
        serverService.add(server);
    }
}
