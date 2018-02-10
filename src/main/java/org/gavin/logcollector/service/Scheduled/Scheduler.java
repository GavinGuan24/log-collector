package org.gavin.logcollector.service.Scheduled;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * ---------------------------------------------------
 * File:    Scheduler
 * Package: org.gavin.logcollector.service.Scheduled
 * Project: logcollector
 * ---------------------------------------------------
 * Created by gavinguan on 2018/2/2 13:28.
 * Copyright © 2018 gavinguan. All rights reserved.
 */
@Component
public class Scheduler {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    //    @Scheduled(cron = "0 0/1 * * * ?")
    public void timer00() {

    }

}
