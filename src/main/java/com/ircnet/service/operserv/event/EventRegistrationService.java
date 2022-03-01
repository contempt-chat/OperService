package com.ircnet.service.operserv.event;

import com.ircnet.library.common.event.EventBus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * This class registers event listeners.
 */
@Component
public class EventRegistrationService {
    @Autowired
    private EventBus eventBus;

    @Autowired
    private YouAreServiceEventListener youAreServiceEventListener;

    @Autowired
    private SQueryEventListener squeryEventListener;

    @Autowired
    private UNickEventListener unickEventListener;

    @Autowired
    private NickChangedEventListener nickChangedEventListener;

    @Autowired
    private QuitEventListener quitEventListener;

    @PostConstruct
    public void init() {
        eventBus.setCheckInheritance(false);

        /**
         * Registration of event listeners.
         */
        eventBus.registerEventListener(unickEventListener);
        eventBus.registerEventListener(nickChangedEventListener);
        eventBus.registerEventListener(quitEventListener);
        eventBus.registerEventListener(squeryEventListener);
        eventBus.registerEventListener(youAreServiceEventListener);
    }
}
