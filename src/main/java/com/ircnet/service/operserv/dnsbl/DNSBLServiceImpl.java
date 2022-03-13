package com.ircnet.service.operserv.dnsbl;

import com.ircnet.service.operserv.IpAddressFamily;
import com.ircnet.service.operserv.irc.IRCUser;
import com.ircnet.service.operserv.kline.KLine;
import com.ircnet.service.operserv.kline.KLineService;
import com.ircnet.service.operserv.kline.KLineType;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.xbill.DNS.*;
import org.xbill.DNS.lookup.LookupSession;
import org.xbill.DNS.lookup.NoSuchDomainException;

import java.net.UnknownHostException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
public class DNSBLServiceImpl implements DNSBLervice {
  /**
   * Maximum number of attempts to resolve a DNSBL hostname.
   */
  private static final int MAX_RESOLVE_ATTEMPTS = 3;

  /**
   * Defines how long an IP address should be TKLINEd that has been found in a DNSBL.
   */
  private static final long TKLINE_DURATION = 24L * 60L * 60L;

  private static final Logger LOGGER = LoggerFactory.getLogger(DNSBLServiceImpl.class);

  @Value("${service.name}")
  private String serviceName;

  @Autowired
  private KLineService klineService;

  private ThreadPoolExecutor executor;
  private Map<String, DNSBLResult> resultMap;
  private List<DNSBLProvider> providers;

  public DNSBLServiceImpl() {
    this.executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(5);
    this.resultMap = new ConcurrentHashMap<>();
    this.providers = new ArrayList<>();
    this.providers.add(new DNSBLProvider("Spamhaus ZEN", "zen.spamhaus.org", "You are listed in Spamhaus ZEN blocklist"));
    this.providers.add(new DNSBLProvider("all.s5h.net", "all.s5h.net", "You are listed at all.s5h.net"));
    this.providers.add(new DNSBLProvider("DroneBL", "dnsbl.dronebl.org", "You are listed in DroneBL - http://dronebl.org/lookup?ip={ip}"));
    this.providers.add(new DNSBLProvider("EFnet RBL", "rbl.efnetrbl.org", "https://rbl.efnetrbl.org/?i={ip}"));
    this.providers.add(new DNSBLProvider("Tornevall", "dnsbl.tornevall.org", "You are listed in Tornevall blacklist"));
  }

  @Override
  public void check(IRCUser user) {
    // If we are here, no K-Line exists currently.
    DNSBLResult entry = resultMap.get(user.getIpAddress());

    if (entry != null && entry.getStatus() == DNSBLStatus.NOT_LISTED) {
      // Hostname has already been checked. TODO: add expiration date.
      return;
    }
    else {
      // Create a new request
      DNSBLRequest request = new DNSBLRequest();
      request.setIpAddress(user.getIpAddress());
      request.setUser(user);

      // Process it in a new thread
      executor.submit(() -> {
        checkListed(request);
      });

      LOGGER.debug("Created DNSBL check for {} (user: {}!{}@{})", request.getIpAddress(), user.getNick(), user.getUser(), user.getHost());
    }
  }

  private void checkListed(DNSBLRequest request) {
    LOGGER.debug("Started DNSBL lookup for {} (user: {}!{}@{})", request.getIpAddress(), request.getUser().getNick(), request.getUser().getUser(), request.getUser().getHost());

    DNSBLResult existingResult = resultMap.get(request.getIpAddress());

    if(existingResult != null) {
      LOGGER.debug("{} was already checked, result: {}", existingResult.getStatus());
      return;
    }

    String reversedIpAddress;

    if(request.getUser().getIpAddressFamily() == IpAddressFamily.IPV4) {
      reversedIpAddress = reverseIPv4Address(request.getIpAddress());
    }
    else {
      try {
        reversedIpAddress = reverseIPv6Address(request.getIpAddress());
      }
      catch (Exception e) {
        LOGGER.error("Could not reverse {}", request.getIpAddress());
        return;
      }
    }

    AtomicBoolean isListed = new AtomicBoolean(false);

    // A map containing providers and number of resolve attempts.
    // At the end this map must be empty, which means that all DNSBLs have been checked.
    Map<DNSBLProvider, Integer> providerToCheck = new HashMap<>();

    for (DNSBLProvider provider : this.providers) {
      providerToCheck.put(provider, 1);
    }

    while (!providerToCheck.isEmpty() && !isListed.get()) {
      Iterator<Map.Entry<DNSBLProvider, Integer>> iterator = providerToCheck.entrySet().iterator();

      // Iterate through DNSBL providers
      while (iterator.hasNext()) {
        Map.Entry<DNSBLProvider, Integer> entry = iterator.next();
        DNSBLProvider provider = entry.getKey();
        int attempt = entry.getValue();

        try {
          String dnsblHost = reversedIpAddress + "." + provider.getDomainName();
          LookupSession s = LookupSession.defaultBuilder().build();
          Name lookup;

          try {
            LOGGER.trace("Looking up {} (attempt: {})", dnsblHost, attempt);
            lookup = Name.fromString(dnsblHost);
          }
          catch (TextParseException e) {
            LOGGER.error("Could not parse '{}'", dnsblHost, e);
            iterator.remove();
            continue;
          }

          // Resolve hostname
          LOGGER.trace("Resolving {} (attempt: {})", dnsblHost, attempt);
          s.lookupAsync(lookup, Type.A)
              .whenComplete(
                  (answers, ex) -> {
                    if (ex == null && !answers.getRecords().isEmpty()) {
                      // Found an A record
                      LOGGER.info("{} is listed in {}", request.getIpAddress(), provider.getName());
                      // Add K-Line
                      createKLine(request, provider);
                      // Add to cache
                      resultMap.put(request.getIpAddress(), new DNSBLResult(DNSBLStatus.LISTED));
                      // Stop searching for more matches
                      isListed.set(true);
                      iterator.remove();
                    }
                    else {
                      if (ex.getCause() instanceof NoSuchDomainException || (answers != null && CollectionUtils.isEmpty(answers.getRecords()))) {
                        // NXDOMAIN or at least no A record: not DNSBL listed
                        LOGGER.debug("{} is not listed in {}", request.getIpAddress(), provider.getName());
                        // Add to cache
                        resultMap.put(request.getIpAddress(), new DNSBLResult(DNSBLStatus.NOT_LISTED));
                        // Continue with next DNSBL provider
                        iterator.remove();
                      }
                      else {
                        // Usually timeout
                        LOGGER.debug("Could not check if {} is listed in {} (attempt: {}): {}", request.getIpAddress(), provider.getName(), attempt, ex.getMessage());

                        if(entry.getValue() >= MAX_RESOLVE_ATTEMPTS) {
                          LOGGER.warn("Could not resolve {} after {} attempts - giving up", dnsblHost, attempt);
                          iterator.remove();
                        }
                        else {
                          // Increment number of attempts
                          entry.setValue(entry.getValue() + 1);
                        }
                      }
                    }
                  })
              .toCompletableFuture()
              .get();
        }
        catch (Exception e) {
          // Handled above
        }

        if (isListed.get()) {
          break;
        }
      }

      if(!providerToCheck.isEmpty() && !isListed.get()) {
        // At least one hostname could not be resolved because the NS was unreachable. Wait before we try it again.
        try {
          Thread.sleep(1000L);
        }
        catch (InterruptedException e) {
        }
      }
    }

    LOGGER.debug("Finished DNSBL lookup for {} (user: {}!{}@{})", request.getIpAddress(), request.getUser().getNick(), request.getUser().getUser(), request.getUser().getHost());
  }

  private void createKLine(DNSBLRequest request, DNSBLProvider provider) {
    KLine kline = new KLine();
    kline.setType(KLineType.DNSBL);
    kline.setUsername("*");
    kline.setHostname(request.getIpAddress());
    kline.setIpAddressOrRange(true);
    kline.setExpirationDate(new Date(System.currentTimeMillis() + TKLINE_DURATION * 1000L));
    kline.setReason(String.format(provider.getKLineReason().replace("{ip}", request.getIpAddress())));

    klineService.create(null, kline, TKLINE_DURATION, false);
  }

  /**
   * Reverses an IPv4 address.
   * Example: 127.0.0.1 becomes 1.0.0.127
   *
   * @param ipAddress An IPv4 address
   * @return The reversed IPv4 address
   */
  private String reverseIPv4Address(String ipAddress) {
    final String[] bytes = ipAddress.split("\\.");
    String reversedIpAddress = bytes[3] + "." + bytes[2] + "." + bytes[1] + "." + bytes[0];
    return reversedIpAddress;
  }

  /**
   * Reverses an IPv6 address.
   * Example: ::1 comes 1.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0
   *
   * @param ipAddress An IPv6 address
   * @return The reversed IPv6 address
   */
  private String reverseIPv6Address(String ipAddress) throws UnknownHostException {
    return ReverseMap.fromAddress(ipAddress, Address.IPv6)
            .toString()
            .replace(".ip6.arpa.", "");
  }
}
