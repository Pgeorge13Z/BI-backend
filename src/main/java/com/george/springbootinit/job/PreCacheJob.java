package com.george.springbootinit.job;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.george.springbootinit.common.ResultUtils;
import com.george.springbootinit.constant.RedisKeyName;
import com.george.springbootinit.constant.UserConstant;
import com.george.springbootinit.model.entity.Chart;
import com.george.springbootinit.model.entity.User;
import com.george.springbootinit.service.ChartService;
import com.george.springbootinit.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;


@Component
@Slf4j
public class PreCacheJob {
    @Resource
    private ChartService chartService;

    @Resource
    RedisTemplate<String, Object> redisTemplate;

    @Resource
    RedissonClient redissonClient;


    @Scheduled(cron = "0 05  23 * * *")
    public void doCacheRecommendUser() {
        RLock lock = redissonClient.getLock(RedisKeyName.PreCacheJob_LOCK);
        try {
            if (lock.tryLock(0, -1, TimeUnit.MILLISECONDS)) {

                ValueOperations<String, Object> valueOperations = redisTemplate.opsForValue();

                // 设置方法的redisKey
                String redisKey = String.format(RedisKeyName.PreCacheJob_LOCK + ":%s", UserConstant.ADMIN_ID);

                QueryWrapper<Chart> chartQueryWrapper = new QueryWrapper<>();
                chartQueryWrapper.eq("userId", UserConstant.ADMIN_ID);

                //从数据库读，并存入redis中
                Page<Chart> chartPage = chartService.page(new Page<>(1, 4), chartQueryWrapper);

                //写缓存
                try {
                    valueOperations.set(redisKey, chartPage, 30000, TimeUnit.MINUTES);
                } catch (Exception e) {
                    log.error("redis set key error", e);
                }
            }


        } catch (InterruptedException e) {
            log.error("doCacheRecommendUser lock error", e);
        } finally {
            //只释放自己的锁
            if (lock.isHeldByCurrentThread()) {
                System.out.println("unLock: " + Thread.currentThread().getId());
                lock.unlock();
            }
        }


    }
}
