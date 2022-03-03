package com.ircnet.service.operserv.event;

import com.ircnet.library.common.event.AbstractEventListener;
import com.ircnet.library.service.event.PermissionDeniedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class PermissionDeniedEventListener extends AbstractEventListener<PermissionDeniedEvent> {
  private static final Logger LOGGER = LoggerFactory.getLogger(PermissionDeniedEventListener.class);

  @Value("${service.channel}")
  private String serviceChannel;

  protected void onEvent(PermissionDeniedEvent event) {
    String message = String.format("Received permission denied from %s", event.getServerName());
    LOGGER.error(message);
    ircConnectionService.notice(event.getIRCConnection(), serviceChannel, message);
  }
}
