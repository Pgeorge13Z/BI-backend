package com.george.springbootinit.job;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.george.springbootinit.bizmq.BiMessageProducer;
import com.george.springbootinit.bizmq.BiMqConstant;
import com.george.springbootinit.constant.ChartConstant;
import com.george.springbootinit.constant.RedisKeyName;
import com.george.springbootinit.manager.AiManager;
import com.george.springbootinit.mapper.ChartMapper;
import com.george.springbootinit.model.entity.Chart;
import com.george.springbootinit.service.ChartService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

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
    private ChartService chartService;
    @Resource
    private ThreadPoolExecutor threadPoolExecutor;

    @Resource
    private BiMessageProducer biMessageProducer;

    @Resource
    RedissonClient redissonClient;

    @Scheduled(cron = "0 0/5 * * * ?") // Every 5 minutes
    public void doUpdateFailedChart() {
        RLock lock = redissonClient.getLock(RedisKeyName.ReGenChart_LOCK);
        try {
//            第一个参数：等待获取锁的时间，这里设置为0表示立即尝试获取锁；
//            第二个参数：等待获取锁的时间，设置为-1表示无限等待；
//            第三个参数：时间单位，这里设置为毫秒。
            if (lock.tryLock(0,-1, TimeUnit.MILLISECONDS)){
                QueryWrapper<Chart> queryWrapper = new QueryWrapper<>();
                queryWrapper.eq("status", "failed");
                List<Chart> failedCharts = chartMapper.selectList(queryWrapper);
                failedCharts.forEach(this::updateFailedChartAsync);
            }
        }
        catch (Exception e) {
            log.error("ReGenChart lock error",e);
        }
        finally {
            //只能释放自己的锁
            if (lock.isHeldByCurrentThread()) {
                System.out.println("unlock:"+Thread.currentThread().getId());
                lock.unlock();
            }
        }
    }

    /**
     * 同步更新失败的图表
     *
     * @param chart
     */
    private void updateFailedChart(final Chart chart) {
        // 将状态为failed的chart 检查内容完整性 并赋值为默认值 重新添加进入消息队列里。

        // 如果原始数据为空，则删除该条记录,跳过这一条记录
        String chartData = chart.getChartData();
        if (chartData.length() < 10) {
            log.info("原始数据太短，请重新添加该条记录"+chart.getName());
            // 删除该条记录
            chartMapper.deleteById(chart.getId());
            return; // 直接返回，不再执行下面的代码
        }

        Chart updateChart = new Chart();
        updateChart.setId(chart.getId());
        // 重新设置目标为默认值
        updateChart.setGoal("请分析数据情况");
        // 重新设置图表的类型
        updateChart.setChartType("柱状图");

        chartService.updateById(updateChart);

        biMessageProducer.sendMessage(BiMqConstant.BI_EXCHANGE_NAME, BiMqConstant.BI_ROUTING_KEY, String.valueOf(chart.getId()));
    }

    /**
     * 异步更新失败的图表
     *
     * @param chart
     */
    private void updateFailedChartAsync(final Chart chart) {
        CompletableFuture.runAsync(() -> {
            updateFailedChart(chart);
        }, threadPoolExecutor);
    }

}