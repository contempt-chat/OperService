package com.ircnet.service.operserv.event;

import com.ircnet.library.common.event.EventBus;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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

    @Autowired
    private ServerEventListener serverEventListener;

    @Autowired
    private SQuitEventListener squitEventListener;

    @Autowired
    private PermissionDeniedEventListener permissionDeniedEventListener;

    @PostConstruct
    public void init() {
        eventBus.setCheckInheritance(false);

        eventBus.registerEventListener(unickEventListener);
        eventBus.registerEventListener(nickChangedEventListener);
        eventBus.registerEventListener(quitEventListener);
        eventBus.registerEventListener(serverEventListener);
        eventBus.registerEventListener(squitEventListener);
        eventBus.registerEventListener(squeryEventListener);
        eventBus.registerEventListener(youAreServiceEventListener);
        eventBus.registerEventListener(permissionDeniedEventListener);
    }
}
