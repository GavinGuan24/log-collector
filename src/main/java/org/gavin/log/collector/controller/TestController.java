package org.gavin.log.collector.controller;

import org.gavin.log.collector.service.TestServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * ---------------------------------------------------
 * File:    TestController
 * Package: org.gavin.logcollector.controller
 * Project: logcollector
 * ---------------------------------------------------
 * Created by gavinguan on 2018/2/2 11:05.
 * Copyright © 2018 gavinguan. All rights reserved.
 */

@RestController
@RequestMapping("/test")
public class TestController {

    @Autowired
    private TestServiceImpl testService;

    @RequestMapping(value = "/log", method = RequestMethod.GET)
    public String server() throws Exception {
        return "0";
    }

}
