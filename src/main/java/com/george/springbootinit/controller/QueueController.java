package com.george.springbootinit.controller;

import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 帖子接口
 */
@RestController
@RequestMapping("/queue")
@Slf4j
@Profile({"dev","local"})
public class QueueController {

    @Resource
    private ThreadPoolExecutor threadPoolExecutor;

    @GetMapping("/add")
    public void add(String name) {
        // 使用CompletableFuture执行一个异步任务
        CompletableFuture.runAsync(()->{
            // 打印一条日志信息，包括任务名称和执行线程的名称
            log.info("任务执行中："+name+",执行人"+Thread.currentThread().getName());
            try {
                //休眠十分钟
                Thread.sleep(600000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        },threadPoolExecutor);
    }

    @GetMapping("/get")
    public String get() {
        // 创建一个HashMap存储线程池的状态信息
        HashMap<String, Object> map = new HashMap<>();
        // 获取线程池的队列长度
        int size = threadPoolExecutor.getQueue().size();
        map.put("队列长度",size);
        // 获取线程池接收到的任务总数
        long taskCount = threadPoolExecutor.getTaskCount();
        map.put("任务总数",taskCount);
        // 获取线程池已经完成的任务总数
        long completedTaskCount = threadPoolExecutor.getCompletedTaskCount();
        map.put("已完成的任务总数",completedTaskCount);
        // 获取线程池中正在执行任务的线程数
        int activeCount = threadPoolExecutor.getActiveCount();
        map.put("正在工作的线程数",activeCount);
        //将map转换为JSON字符串并返回
        return JSONUtil.toJsonStr(map);
    }

}
