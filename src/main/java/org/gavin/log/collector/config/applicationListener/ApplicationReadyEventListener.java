package org.gavin.log.collector.config.applicationListener;

import org.gavin.log.collector.service.log.LogReceiver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;

/**
 * ---------------------------------------------------
 * File:    ApplicationReadyEventListener
 * Package: org.gavin.logcollector.config.applicationListener
 * Project: logcollector
 * ---------------------------------------------------
 * Created by gavinguan on 2018/2/2 14:06.
 * Copyright © 2018 gavinguan. All rights reserved.
 */
public class ApplicationReadyEventListener implements ApplicationListener<ApplicationReadyEvent> {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public void onApplicationEvent(ApplicationReadyEvent applicationReadyEvent) {
        LogReceiver logReceiver = applicationReadyEvent.getApplicationContext().getBean(LogReceiver.class);
        boolean error = false;
        try {
            logReceiver.startupListener();
        } catch (Exception e) {
            e.printStackTrace();
            error = true;
        }
        if (error) logger.error("LogReceiver.init failed");
    }
}
