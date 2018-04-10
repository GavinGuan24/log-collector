package org.gavin.log.collector.service.log.impl;

import org.apache.lucene.index.Term;
import org.gavin.log.collector.service.log.LogDocument;
import org.gavin.log.collector.service.log.LogReceiver;
import org.gavin.log.collector.service.log.ILogRepositoryService;
import org.gavin.log.collector.service.log.logRepository.LogRepository;
import org.gavin.search.hawkeye.query.PagingQuery;
import org.gavin.search.hawkeye.query.QueryTemplateBuilder;
import org.gavin.search.hawkeye.result.PagingQueryResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

/**
 * ---------------------------------------------------
 * File:    LogRepositoryServiceImpl
 * Package: org.gavin.log.collector.service.log.impl
 * Project: log-collector
 * ---------------------------------------------------
 * Created by gavinguan on 2018/2/13 09:33.
 * Copyright © 2018 gavinguan. All rights reserved.
 */
@Service("LogRepositoryServiceImpl")
public class LogRepositoryServiceImpl implements ILogRepositoryService {

    private static Logger logger = LoggerFactory.getLogger(LogRepositoryServiceImpl.class);

    @Autowired
    private LogReceiver logReceiver;
    @Value("${ymlConfig.hawkeyeRepositoryPath}")
    private String repositoryPath;

    private boolean needShutdown;
    private LogRepository logRepository;


    public LogRepositoryServiceImpl() {
        this.needShutdown = false;
    }

    @Override
    public void shutdown() {
        this.needShutdown = true;
    }

    @PostConstruct
    private void startRunLoop() {
        //重建 repository : -> gavin: 如何将这些index数据安全持久化 ?
        boolean error = false;
        try {
            logRepository = new LogRepository(repositoryPath);
        } catch (Exception e) {
            e.printStackTrace();
            error = true;
        }

        if (error) {
            logger.error("LogRepository rebuild error");
            return;
        }

        new Thread(this::absorbLogBuffer).start();
    }

    private void absorbLogBuffer() {
        while (!needShutdown) {
            if (logReceiver != null && !logReceiver.logBufferIsEmpty()) {
                LogDocument logDocument = logReceiver.pollLog();
                try {
                    logRepository.addDocument(logDocument);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }


    public void searchTest(String key) {
        QueryTemplateBuilder queryTemplateBuilder = new QueryTemplateBuilder() {
            @Override
            protected List<Term> parseMustKey(String s) {
                return null;
            }

            @Override
            protected List<Term> parseFilterKey(String s) {
                return null;
            }

            @Override
            protected List<Term> parseShouldListKey(String s) {
                List<Term> termList = new ArrayList<>();
                termList.add(new Term(LogRepository.messageKey, s));
                return termList;
            }

            @Override
            protected List<Term> parseMustNotKey(String s) {
                return null;
            }
        };
//        PagingQuery pagingQuery = new PagingQuery(queryTemplateBuilder.fuzzyQuery("key"), 1, 10, true);
//        PagingQueryResult<>  logRepository.pagingSearch(pagingQuery);

    }





}
