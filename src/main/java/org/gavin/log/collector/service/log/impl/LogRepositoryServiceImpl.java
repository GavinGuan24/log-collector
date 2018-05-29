package org.gavin.log.collector.service.log.impl;

import com.alibaba.fastjson.JSON;
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
    //日志接收者
    private LogReceiver logReceiver;

    @Value("${ymlConfig.hawkeyeRepositoryPath}")
    //日志 index化 仓库
    private String repositoryPath;
    private LogRepository logRepository;

    //状态值
    private boolean needShutdown;       //用于传递信息: 需要永久停用服务
    private int absorbLogThreadCount;   //记录日志index化线程总数, 最大值已被锁定为 2, 详见 startRunLoop() 方法
    private boolean pauseCollect;       //当该值为 true, 即刻开始, absorbLogBuffer() 内对日志信息的处理操作变为 "丢弃所有日志信息"



    public LogRepositoryServiceImpl() {
        this.needShutdown = false;
        this.absorbLogThreadCount = 0;
        this.pauseCollect = false;
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
        //关闭 logRepository
        try {
            logRepository.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    //继续日志服务
    public void resume() {
        pauseCollect = false;
    }

    @Override
    //暂停日志服务
    public void pause() {
        pauseCollect = true;
    }

    @PostConstruct
    private void startRunLoop() {
        if (logReceiver == null) {
            logger.error("LogReceiver can't autowired");
            return;
        }
        try {
            //这里传入的repositoryPath, 如果正在被另一个程序使用, 未测试过 "读取操作" 是否可用, "写入操作" 是一定会出现冲突的
            //如果以后没有需求, 这里就不考虑同时被几个同类型的程序使用的情况
            //logRepository 没有 logRepository.close() 时, 如果此时所有indexWrite操作均已完毕, 下次也可以继续使用
            logRepository = new LogRepository(repositoryPath);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("LogRepository open failed");
            return;
        }

        //启动 2 个线程处理 logReceiver 中的日志, 选择 "2 个" 的理由:
        //1. 充分利用 logReceiver.pollLogs() 与 logRepository.addDocuments()的时间差
        //2. logRepository.addDocuments() 测试时耗时是 logReceiver.pollLogs() 10+倍
        //3. 虽然logRepository.addDocuments() 是锁同步的, 但其实内部写入文件才是硬瓶颈, 且不可被优化
        //   而 logReceiver.logBuffer 的内容是一直被消耗的, 纵使其短期内内容爆炸
        //   logReceiver.pollLogs() 的调用耗时也大概率的不及logRepository.addDocuments()的调用耗时 久
        new Thread(this::absorbLogBuffer).start();
        new Thread(this::absorbLogBuffer).start();
    }

    //logRepository.addDocuments 是运行瓶颈, 且无法优化
    //尝试获取多个的文档 (一次性写入更多个会提高执行效率)
    private void absorbLogBuffer() {
        synchronized (this) {
            absorbLogThreadCount += 1;
        }
        if (logReceiver != null) {
            while (!needShutdown) {
                //尝试获取 64 条log数据, 并index化
                List<LogDocument> logDocumentList = logReceiver.pollLogs(64);
                if (pauseCollect) continue;
                try {
                    /** TODO: 这里休眠是为了节省cpu给别的线程
                     * 不仅仅是为了 log-conllector的线程, 包括其他程序的.
                     * 我注意到如果不休眠, cpu将会几乎被 absorbLogBuffer 吃掉,
                     * 而休眠会很好解决这个问题. 在测试时, 1000ms 内的休眠结果对cpu占用会骤降,
                     * 且结果近似, 所以在同时考虑到大量日志爆发的场景, 我决定将该数据设置为15ms.
                     * 当然, 15ms 也是参考项目[hawkeye](https://github.com/GavinGuan24/hawkeye)
                     * 连续多次写入时, 每次写入16个测试数据, 平均耗时11ms.
                     *
                     * 确切的说, 我会考虑将这个值交给用户自行设置, 因为每台机子的性能不同, 这个值应该参考实际环境
                     */
                    Thread.sleep(15);
                    logRepository.addDocuments(logDocumentList);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        synchronized (this) {
            absorbLogThreadCount -= 1;
        }
        logger.debug("absorbLogThreadCount:{}", absorbLogThreadCount);
    }

    @Override
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

        try {
            PagingQuery pagingQuery = new PagingQuery(queryTemplateBuilder.fuzzyQuery(key), 1, 10, true);
            PagingQueryResult<LogDocument> result = logRepository.pagingSearch(pagingQuery);

            logger.info(JSON.toJSONString(result));
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


}
