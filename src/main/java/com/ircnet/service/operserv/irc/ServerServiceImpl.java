package com.ircnet.service.operserv.irc;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class ServerServiceImpl implements ServerService {
    @Autowired
    @Qualifier("serverMap")
    private Map<String, IRCServer> serverMap;

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
