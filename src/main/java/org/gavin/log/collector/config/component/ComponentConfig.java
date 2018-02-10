package org.gavin.log.collector.config.component;

import org.gavin.log.collector.service.log.LogReceiver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * ---------------------------------------------------
 * File:    ComponentConfig
 * Package: org.gavin.logcollector.config.component
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
