package com.ircnet.service.operserv.event;

import com.ircnet.library.common.connection.IRCConnectionService;
import com.ircnet.library.common.event.AbstractEventListener;
import com.ircnet.library.service.event.ServerEvent;
import com.ircnet.library.service.server.IRCServer;
import com.ircnet.service.operserv.irc.ServerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ServerEventListener extends AbstractEventListener<ServerEvent, IRCConnectionService> {
    private static final Logger LOGGER = LoggerFactory.getLogger(ServerEventListener.class);

    private ServerService serverService;

    public ServerEventListener(IRCConnectionService ircConnectionService, ServerService serverService) {
        super(ircConnectionService);
        this.serverService = serverService;
    }

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
