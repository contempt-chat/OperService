package com.ircnet.service.operserv.irc;

import com.ircnet.library.service.server.IRCServer;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ServerServiceImpl implements ServerService {
    /**
     * A map containing all servers mapped by SID.
     */
    protected final Map<String, IRCServer> serverMap;

    public ServerServiceImpl() {
        this.serverMap = new ConcurrentHashMap<>();
    }

    @Override
    public void add(IRCServer server) {
        serverMap.put(server.getSid(), server);
    }

    @Override
    public IRCServer findBySID(String sid) {
        return serverMap.get(sid);
    }

    @Override
    public IRCServer findByName(String name) {
        return serverMap.values().stream()
                .filter(e -> e.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }

    @Override
    public void remove(String sid) {
        serverMap.remove(sid);
    }
}
