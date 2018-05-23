package org.gavin.log.collector.service.log.impl;

import org.apache.lucene.index.Term;
import org.gavin.log.collector.service.log.LogDocument;
import org.gavin.log.collector.service.log.LogReceiver;
import org.gavin.log.collector.service.log.ILogRepositoryService;
import org.gavin.log.collector.service.log.logRepository.LogRepository;
import org.gavin.search.hawkeye.query.QueryTemplateBuilder;
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

    @Value("${ymlConfig.hawkeyeRepositoryPath}")
    private String repositoryPath;
    @Value("${ymlConfig.absorbLogThreadSize}")
    private Integer absorbLogThreadSize;

    private boolean needShutdown;
    private int absorbLogThreadCount;


    @Autowired
    private LogReceiver logReceiver;

    private LogRepository logRepository;


    public LogRepositoryServiceImpl() {
        this.needShutdown = false;
        this.absorbLogThreadCount = 0;
    }

    @Override
    public void shutdown() {
        //停用 logReceiver
        try {
            logReceiver.shutdown();
        } catch (Exception e) {
            e.printStackTrace();
        }

        needShutdown = true;
        //确保停用所有 absorbLog 线程
        while (absorbLogThreadCount > 0) {
            try {
                Thread.sleep(10);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        //尝试获取剩余log数据, 并index化
        if (logReceiver != null) {
            List<LogDocument> logDocumentList = logReceiver.pollLogs(64);
            while (logDocumentList.size() > 0) {
                for (LogDocument logDocument : logDocumentList) {
                    try {
                        logRepository.addDocument(logDocument);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                logDocumentList = logReceiver.pollLogs(64);
            }
        }
        //正确关闭 logRepository
        //feature: 正确开启与关闭待实现
    }

    @Override
    public void resume() {
        //feature: 继续日志服务待实现
    }

    @Override
    public void pause() {
        //feature: 暂停日志服务待实现
    }

    @PostConstruct
    private void startRunLoop() {
        if (logReceiver == null) {
            logger.error("LogReceiver 未注入");
            return;
        }
        try {
            //这里传入的repositoryPath, 这个文件夹下如果有其他文件或writeLock, 会无法初始化, 这个问题下个版本解决吧
            //feature: 正确开启与关闭待实现
            logRepository = new LogRepository(repositoryPath);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("LogRepository rebuild error");
            return;
        }

        //启动至少 2个线程处理 logReceiver 中的日志
        if (!(absorbLogThreadSize != null && absorbLogThreadSize >= 2)) absorbLogThreadSize = 2;
        for (int i = 0; i < absorbLogThreadSize; i++) {
            new Thread(this::absorbLogBuffer).start();
        }
    }

    private void incrementAbsorbLogCoreThreadCount() {
        synchronized (this) {
            absorbLogThreadCount += 1;
        }
    }

    private void decrementAbsorbLogCoreThreadCount() {
        synchronized (this) {
            absorbLogThreadCount -= 1;
        }
    }

    private void absorbLogBuffer() {
        incrementAbsorbLogCoreThreadCount();
        while (!needShutdown) {
            if (logReceiver != null) {
                //尝试获取16条log数据, 并index化
                List<LogDocument> logDocumentList = logReceiver.pollLogs(16);
                if (logDocumentList.size() > 0) {
                    try {
                        logRepository.addDocuments(logDocumentList);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        decrementAbsorbLogCoreThreadCount();
    }


    //feature: 搜索功能待实现
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
