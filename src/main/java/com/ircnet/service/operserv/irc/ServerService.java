package com.ircnet.service.operserv.irc;

public interface ServerService {
    /**
     * Adds a server.
     *
     * @param server A server
     */
    void add(IRCServer server);

    /**
     * Finds a server by SID.
     *
     * @param sid A SID
     * @return A server or null if the server could not be found
     */
    IRCServer findBySID(String sid);

    /**
     * Finds a server by name.
     *
     * @param name A name
     * @return A server or null if the server could not be found
     */
    IRCServer findByName(String name);

    /**
     * Removes a server.
     *
     * @param sid A SID
     */
    void remove(String sid);
}
