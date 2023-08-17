package com.ircnet.service.operserv.event;

import com.ircnet.library.common.connection.IRCConnectionService;
import com.ircnet.library.common.event.AbstractEventListener;
import com.ircnet.library.service.IRCServiceTask;
import com.ircnet.library.service.event.SQueryEvent;
import com.ircnet.service.operserv.ServiceProperties;
import com.ircnet.service.operserv.squery.SQueryCommand;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Event for SQUERY message.
 */
@Component
public class SQueryEventListener extends AbstractEventListener<SQueryEvent> {
    @SuppressWarnings("unused")
    private static final Logger LOGGER = LoggerFactory.getLogger(SQueryEventListener.class);

    @Autowired
    private IRCConnectionService ircConnectionService;

    @Autowired
    private IRCServiceTask ircServiceTask;

    @Autowired
    private ServiceProperties properties;

    @Qualifier("squeryCommandMap")
    @Autowired
    private Map<String, SQueryCommand> squeryCommandMap;

    public SQueryEventListener() {
    }

    protected void onEvent(SQueryEvent event) {
        String nick = event.getFrom().getNick();

        if (StringUtils.isEmpty(event.getMessage())) {
            return;
        }

        String[] parts = event.getMessage().split(" ");

        SQueryCommand squeryCommand = squeryCommandMap.get(parts[0]);

        if(squeryCommand != null) {
            squeryCommand.processCommand(event.getFrom(), event.getMessageTags(), event.getMessage());
        }

        else {
            ircConnectionService.notice(ircServiceTask.getIRCConnection(), nick,
                "Unrecognized command: \"%s\". Use /SQUERY %s HELP\n", parts[0], properties.getName());
        }
    }
}
