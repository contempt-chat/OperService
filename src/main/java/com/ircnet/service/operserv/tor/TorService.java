package com.ircnet.service.operserv.tor;

/**
 * Service for loading Tor exit nodes.
 */
public interface TorService {
    /**
     * Loads Tor exit nodes from configured URLs and creates K-Lines for each IP address.
     */
    void loadFromWeb();
}
