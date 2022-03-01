package com.ircnet.service.operserv.sasl;

import com.ircnet.library.common.User;
import com.ircnet.library.common.connection.ConnectionStatus;
import com.ircnet.library.common.connection.IRCConnectionService;
import com.ircnet.library.service.IRCServiceTask;
import com.ircnet.service.operserv.Util;
import com.ircnet.service.operserv.persistence.PersistenceService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Collections;
import java.util.List;

// Checked (FIXME)
@Service
public class AccountServiceImpl implements AccountService {
  private static final Logger LOGGER = LoggerFactory.getLogger(AccountServiceImpl.class);

  @Autowired
  @Qualifier("authorizedAccounts")
  private List<String> authorizedAccounts;

  @Autowired
  private IRCServiceTask ircServiceTask;

  @Autowired
  private IRCConnectionService ircConnectionService;

  @Autowired
  private PersistenceService persistenceService;

  @Value("${sasl-webservice.kline.url}")
  private String apiURL;

  @Value("${sasl-webservice.kline.username}")
  private String apiUsername;

  @Value("${sasl-webservice.kline.password}")
  private String apiPassword;

  @Value("${service.channel}")
  private String serviceChannel;

  @Override
  public boolean isAuthorized(String account) {
    if(StringUtils.isBlank(account)) {
      return false;
    }

    return authorizedAccounts.stream().anyMatch(account::equalsIgnoreCase);
  }

  @Override
  public void loadFromAPI(User from) {
    String url = Util.appendPathToURL(apiURL, "/authorized-accounts");

    WebClient.create(url)
        .get()
        .headers(headers -> headers.setBasicAuth(apiUsername, apiPassword))
        .retrieve()
        .bodyToMono(new ParameterizedTypeReference<List<String>>(){})
        .doOnError(e -> {
          if(ircServiceTask.getIRCConnection().getConnectionStatus() == ConnectionStatus.REGISTERED) {
            String message = String.format("Could not load SASL accounts: API request failed: %s", e.getMessage());
            ircConnectionService.notice(ircServiceTask.getIRCConnection(), serviceChannel, message);

            if(from != null) {
                ircConnectionService.notice(ircServiceTask.getIRCConnection(), from.getNick(), message);
            }
          }
        })
        .subscribe(
            response -> {
              authorizedAccounts.clear();
              authorizedAccounts.addAll(response);
              persistenceService.save();

              LOGGER.info("Loaded {} accounts from SASL webservice", authorizedAccounts.size());

              if(from != null) {
                String message = String.format("Reloaded accounts");
                  ircConnectionService.notice(ircServiceTask.getIRCConnection(), serviceChannel, message);
                  ircConnectionService.notice(ircServiceTask.getIRCConnection(), from.getNick(), message);
              }
            }
        );
  }

  @Override
  public List<String> findAll() {
    return Collections.unmodifiableList(authorizedAccounts);
  }
}
