package com.george.springbootinit.constant;

public interface RedisKeyName {
    /**
     * 死信队列定时任务锁
     */
    String ReGenChart_LOCK ="bi:preCacheJob:doCache:lock";

    /**
     * 展示用户的缓存
     */
    String List_MyChart = "bi:chartController:listMyChartByPage";
}
