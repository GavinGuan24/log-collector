package org.gavin.logcollector.config.Component;

import org.gavin.logCollector.service.log.LogReceiver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * ---------------------------------------------------
 * File:    ComponentConfig
 * Package: org.gavin.logcollector.config.Component
 * Project: logcollector
 * ---------------------------------------------------
 * Created by gavinguan on 2018/2/6 16:38.
 * Copyright © 2018 gavinguan. All rights reserved.
 */

@Configuration
public class ComponentConfig {

    @Bean(name = "LogReceiver")
    public LogReceiver getLogReceiver() {
        return new LogReceiver();
    }

}
