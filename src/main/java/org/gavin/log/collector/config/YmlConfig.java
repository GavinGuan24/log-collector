package org.gavin.log.collector.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * ---------------------------------------------------
 * File:    YmlConfig
 * Package: org.gavin.logcollector.config
 * Project: logcollector
 * ---------------------------------------------------
 * Created by gavinguan on 2018/2/2 10:55.
 * Copyright © 2018 gavinguan. All rights reserved.
 */

@Component
@ConfigurationProperties(prefix = "ymlConfig")
public class YmlConfig {

    private String context;

    public YmlConfig() {}

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }
}
