package com.george.springbootinit.job;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.george.springbootinit.constant.ChartConstant;
import com.george.springbootinit.manager.AiManager;
import com.george.springbootinit.mapper.ChartMapper;
import com.george.springbootinit.model.entity.Chart;
import com.george.springbootinit.service.ChartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author <a href="https://github.com/lieeew">leikooo</a>
 * @Description 向每隔 5分钟 执行一个在数据库中捞取数据的操作
 */
@Component
@Slf4j
public class ReGenChartData {
    @Resource
    private ChartMapper chartMapper;
    @Resource
    private AiManager aiManager;
    @Resource
    private ChartService chartService;
    @Resource
    private ThreadPoolExecutor threadPoolExecutor;

    @Scheduled(cron = "0 0/5 * * * ?") // Every 5 minutes
    public void doUpdateFailedChart() {
        QueryWrapper<Chart> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("status", "failed");
        List<Chart> failedCharts = chartMapper.selectList(queryWrapper);
        failedCharts.forEach(this::updateFailedChartAsync);
    }


}