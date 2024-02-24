package com.george.springbootinit.controller;

import cn.hutool.core.io.FileUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.george.springbootinit.annotation.AuthCheck;
import com.george.springbootinit.common.BaseResponse;
import com.george.springbootinit.common.DeleteRequest;
import com.george.springbootinit.common.ErrorCode;
import com.george.springbootinit.common.ResultUtils;
import com.george.springbootinit.constant.CommonConstant;
import com.george.springbootinit.constant.UserConstant;
import com.george.springbootinit.exception.BusinessException;
import com.george.springbootinit.exception.ThrowUtils;
import com.george.springbootinit.manager.AiManager;
import com.george.springbootinit.manager.RedisLimiterManager;
import com.george.springbootinit.model.dto.chart.*;
import com.george.springbootinit.model.entity.Chart;
import com.george.springbootinit.model.entity.User;
import com.george.springbootinit.model.vo.BiResponse;
import com.george.springbootinit.service.ChartService;
import com.george.springbootinit.service.UserService;
import com.george.springbootinit.utils.ExcelUtils;
import com.george.springbootinit.utils.SqlUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
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
