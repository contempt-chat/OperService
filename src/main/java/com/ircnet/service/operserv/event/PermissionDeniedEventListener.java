package com.ircnet.service.operserv.event;

import com.ircnet.library.common.event.AbstractEventListener;
import com.ircnet.library.service.event.PermissionDeniedEvent;
import com.ircnet.service.operserv.ServiceProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PermissionDeniedEventListener extends AbstractEventListener<PermissionDeniedEvent> {
  private static final Logger LOGGER = LoggerFactory.getLogger(PermissionDeniedEventListener.class);

  @Autowired
  private ServiceProperties properties;

  protected void onEvent(PermissionDeniedEvent event) {
    String message = String.format("Received permission denied from %s", event.getServerName());
    LOGGER.error(message);
    ircConnectionService.notice(event.getIRCConnection(), properties.getChannel(), message);
  }
}
