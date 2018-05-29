package org.gavin.log.collector.controller;

import org.gavin.log.collector.service.TestServiceImpl;
import org.gavin.log.collector.service.log.ILogRepositoryService;
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
    @Autowired
    private ILogRepositoryService repositoryService;

    @RequestMapping(value = "/searchLog", method = RequestMethod.GET)
    public String searchLog(String key) throws Exception {
        if (key == null || key.length() == 0) return "关键字无效";
        repositoryService.searchTest(key);
        return "已搜索";
    }

}
