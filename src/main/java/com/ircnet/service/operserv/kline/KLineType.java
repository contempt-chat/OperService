package com.ircnet.service.operserv.kline;

/**
 * K-Line types.
 */
public enum KLineType {
    /**
     * K-Line has been created by the web service.
     */
    WEB,

    /**
     * K-Line has been generated by a list of Tor exit nodes.
     */
    TOR,

    /**
     * K-Line has been generated by DNSBL check.
     */
    DNSBL
}
