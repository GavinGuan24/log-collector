package org.gavin.log.collector.service.log;

/**
 * ---------------------------------------------------
 * File:    ILogRepositoryService
 * Package: org.gavin.log.collector.service.log
 * Project: log-collector
 * ---------------------------------------------------
 * Created by gavinguan on 2018/2/13 09:32.
 * Copyright © 2018 gavinguan. All rights reserved.
 */
public interface ILogRepositoryService {

    /**
     * 永久关闭服务, 不可恢复, 这里是一个通知, 内部会异步停用
     */
    void shutdown();

    /**
     * 与pause()操作相反
     */
    void resume();

    /**
     * 暂停log收集与index化的进程, 这里是一个通知, 内部会异步暂停
     */
    void pause();

    /**
     * 模糊搜索, 作为测试
     */
    void searchTest(String key);
}
