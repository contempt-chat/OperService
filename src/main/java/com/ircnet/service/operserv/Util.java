package com.ircnet.service.operserv;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOCase;
import org.apache.commons.validator.routines.InetAddressValidator;
import org.springframework.security.web.util.matcher.IpAddressMatcher;
// Checked (FIXME)
/**
 * Helper methods.
 */
public class Util {
    /**
     * Checks if a string matches a pattern.
     * The wildcard matcher uses the characters '?' and '*' to represent a single or multiple (zero or more)
     * wildcard characters. This is used by the ircd to match hostmasks.
     *
     * @param text A text
     * @param pattern A pattern
     * @return true if the text matches the pattern
     */
    public static boolean matches(String text, String pattern) {
        return FilenameUtils.wildcardMatch(text, pattern, IOCase.INSENSITIVE);
    }

    /**
     * Determines if a string is IPv4 or IPv6 and returns the address family.
     *
     * @param ipAddressOrRange An ip address or an IP range
     * @return The address family
     */
    public static IpAddressFamily findAddressFamily(String ipAddressOrRange) {
        int slashIndex = ipAddressOrRange.indexOf("/");

        String ipAddress;

        if(slashIndex != -1) {
            ipAddress = ipAddressOrRange.substring(0, slashIndex);
        }
        else {
            ipAddress = ipAddressOrRange;
        }

        InetAddressValidator addressValidator = new InetAddressValidator();

        if(addressValidator.isValidInet4Address(ipAddress)) {
            return IpAddressFamily.IPV4;
        }
        else if(addressValidator.isValidInet6Address(ipAddress)) {
            return IpAddressFamily.IPV6;
        }
        else {
            return null;
        }
    }

    /**
     * Determines if a string is an IPv4 or IPv6 address or a range in CIDR notation.
     *
     * @param input A string to check
     * @return true of the string is an IPv4 or IPv6 address or a range in CIDR notation
     */
    public static boolean isIpAddressOrRange(String input) {
        return findAddressFamily(input) != null;
    }

    /**
     * Appends a path to an URL.
     *
     * @param baseURL A base URL
     * @param path A path
     * @return The concatenated URL
     */
    public static String appendPathToURL(String baseURL, String path) {
        StringBuilder url = new StringBuilder(baseURL);

        if (!baseURL.endsWith("/")) {
            url.append("/");
        }

        url.append(path);
        return url.toString();
    }

    /**
     * Converts seconds into a string like "1 week 2 days 3 hours 4 minutes 5 seconds".
     *
     * @param inputSeconds Seconds
     * @return A string like "1 week 2 days 3 hours 4 minutes 5 seconds"
     */
    public static String formatSeconds(long inputSeconds) {
        int weeks = (int) (inputSeconds / (7 * 24 * 3600));
        inputSeconds = inputSeconds % (7 * 24 * 3600);

        int days = (int) (inputSeconds / (24 * 3600));
        inputSeconds = inputSeconds % (24 * 3600);

        int hours = (int) (inputSeconds / 3600);
        inputSeconds %= 3600;

        int minutes = (int) (inputSeconds / 60);
        inputSeconds %= 60;

        int seconds = (int) inputSeconds;

        StringBuilder result = new StringBuilder();

        if (weeks != 0) {
            result.append(String.format(" %d %s", weeks, weeks == 1 ? "week" : "weeks"));
        }
        if (days != 0) {
            result.append(String.format(" %d %s", days, days == 1 ? "day" : "days"));
        }
        if (hours != 0) {
            result.append(String.format(" %d %s", hours, hours == 1 ? "hour" : "hours"));
        }
        if (minutes != 0) {
            result.append(String.format(" %d %s", minutes, minutes == 1 ? "minute" : "minutes"));
        }
        if (seconds != 0) {
            result.append(String.format(" %d %s", seconds, seconds == 1 ? "second" : "seconds"));
        }

        return result.toString();
    }

    /**
     * Checks if an IPv4 address is a local or private IP address.
     *
     * @param ipAddress An IPv4 address
     * @return true if the IPv4 address is local or private
     */
    public static boolean isPrivateIPv4Address(String ipAddress) {
        final String[] privateRanges = { "127.0.0.0/8", "10.0.0.0/8", "172.16.0.0/12", "192.168.0.0/16" };

        for(String privateRange : privateRanges) {
            IpAddressMatcher ipAddressMatcher = new IpAddressMatcher(privateRange);

            if (ipAddressMatcher.matches(ipAddress)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Checks if an IPv6 address is a local or private IP address.
     *
     * @param ipAddress An IPv6 address
     * @return true if the IPv6 address is local or private
     */
    public static boolean isPrivateIPv6Address(String ipAddress) {
        final String[] privateRanges = { "::1/128", "fe80::/10", "fec0::/10", "fc00::/7", "ff00::/8" };

        for(String privateRange : privateRanges) {
            IpAddressMatcher ipAddressMatcher = new IpAddressMatcher(privateRange);

            if (ipAddressMatcher.matches(ipAddress)) {
                return true;
            }
        }

        return false;
    }
}
