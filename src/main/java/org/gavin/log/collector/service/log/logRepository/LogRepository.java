package org.gavin.log.collector.service.log.logRepository;

import com.alibaba.fastjson.JSON;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.search.highlight.Highlighter;
import org.gavin.log.collector.service.log.LogDocument;
import org.gavin.log.collector.service.log.sender.vo.Caller;
import org.gavin.log.collector.service.log.sender.vo.LoggingEventVO;
import org.gavin.log.collector.service.log.sender.vo.throwable.ThrowableProxyVO;
import org.gavin.search.hawkeye.StandardRepository;

import java.io.IOException;

/**
 * ---------------------------------------------------
 * File:    LogRepository
 * Package: org.gavin.log.collector.service.log.logRepository
 * Project: log-collector
 * ---------------------------------------------------
 * Created by gavinguan on 2018/2/13 10:40.
 * Copyright © 2018 gavinguan. All rights reserved.
 */
public class LogRepository extends StandardRepository<LogDocument> {

    public static final String hostKey = "host";
    public static final String portKey = "port";
    public static final String messageKey = "message";
    public static final String timeStampKey = "timeStamp";
    public static final String loggerNameKey = "loggerName";
    public static final String threadNameKey = "threadName";
    public static final String callerKey = "caller";
    public static final String throwableKey = "throwable";

    public LogRepository(String directoryPath) throws IOException {
        super(directoryPath);
    }

    @Override
    protected void parseObject2InternalDocument(LogDocument logDocument, Document document) {

        LoggingEventVO eventVO = logDocument.getLoggingEventVO();

        document.add(new StringField(hostKey, logDocument.getHost(), Field.Store.YES));
        document.add(new StringField(portKey, ""+logDocument.getPort(), Field.Store.YES));
        document.add(new TextField(messageKey, eventVO.getMessage(), Field.Store.YES));
        document.add(new StringField(timeStampKey, ""+eventVO.getTimeStamp(), Field.Store.YES));
        document.add(new StringField(loggerNameKey, eventVO.getLoggerName(), Field.Store.YES));
        document.add(new StringField(threadNameKey, eventVO.getThreadName(), Field.Store.YES));
        document.add(new TextField(callerKey, JSON.toJSONString(eventVO.getCallerList()), Field.Store.YES));

        String throwable = "";
        if (eventVO.getThrowableProxyVO() != null) {
            throwable = JSON.toJSONString(eventVO.getThrowableProxyVO());
        }
        document.add(new TextField(throwableKey, throwable, Field.Store.YES));
    }

    @Override
    protected LogDocument parseInternalDocument2Object(Document document) {
        return parseInternalDocument2Object_(document);
    }

    @Override
    protected LogDocument parseInternalDocument2ObjectWithHighlighter(Document document, Highlighter highlighter, Analyzer analyzer) {

        LogDocument logDocument = parseInternalDocument2Object_(document);
        LoggingEventVO loggingEventVO = logDocument.getLoggingEventVO();

        try {
            loggingEventVO.setMessage(highlighter.getBestFragment(analyzer, messageKey, document.get(messageKey)));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return logDocument;
    }

    //--------------------------------------------------------------

    private LogDocument parseInternalDocument2Object_(Document document) {
        LogDocument logDocument = new LogDocument();
        LoggingEventVO loggingEventVO = new LoggingEventVO();
        logDocument.setLoggingEventVO(loggingEventVO);

        logDocument.setHost(document.get(hostKey));
        logDocument.setPort(Integer.valueOf(document.get(portKey)));

        loggingEventVO.setMessage(document.get(messageKey));
        loggingEventVO.setTimeStamp(Long.valueOf(document.get(timeStampKey)));
        loggingEventVO.setLoggerName(document.get(loggerNameKey));
        loggingEventVO.setThreadName(document.get(threadNameKey));

        loggingEventVO.setCallerList(JSON.parseArray(document.get(callerKey), Caller.class));
        if (!document.get(throwableKey).equals("")) {
            loggingEventVO.setThrowableProxyVO(JSON.parseObject(document.get(throwableKey), ThrowableProxyVO.class));
        }

        return logDocument;
    }
}
