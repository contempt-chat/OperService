package com.ircnet.service.operserv.irc;

/**
 * Represents a linked IRC server.
 */
public class IRCServer {
    /**
     * Server ID.
     */
    private String sid;

    /**
     * Name.
     */
    private String name;

    /**
     * HOP count.
     */
    private int hopCount;

    /**
     * Info / Description.
     */
    private String info;

    public String getSid() {
        return sid;
    }

    public void setSid(String sid) {
        this.sid = sid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getHopCount() {
        return hopCount;
    }

    public void setHopCount(int hopCount) {
        this.hopCount = hopCount;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }
}
