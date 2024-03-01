package com.george.springbootinit.bizmq;


import com.george.springbootinit.common.ErrorCode;
import com.george.springbootinit.constant.ChartConstant;
import com.george.springbootinit.exception.BusinessException;
import com.george.springbootinit.exception.ThrowUtils;
import com.george.springbootinit.manager.AiManager;
import com.george.springbootinit.model.entity.Chart;
import com.george.springbootinit.service.ChartService;
import com.rabbitmq.client.Channel;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;


// 使用@Component注解标记该类为一个组件，让Spring框架能够扫描并将其纳入管理
@Component
// 使用@Slf4j注解生成日志记录器
@Slf4j
public class BiMessageConsumer {

    @Resource
    ChartService chartService;

    @Resource
    AiManager aiManager;

    /**
     * 接收消息的方法
     *
     * @param message     接收到的消息内容，是一个字符串类型
     * @param channel     消息所在的通道，可以通过该通道与 RabbitMQ 进行交互，例如手动确认消息、拒绝消息等
     * @param deliveryTag 消息的投递标签，用于唯一标识一条消息
     */
    // 使用@SneakyThrows注解简化异常处理
    @SneakyThrows
    // 使用@RabbitListener注解指定要监听的队列名称为"code_queue"，并设置消息的确认机制为手动确认
    @RabbitListener(queues = {BiMqConstant.BI_QUEUE_NAME}, ackMode = "MANUAL", concurrency = "2") // 设置并发消费者数量为2
    // @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag是一个方法参数注解,用于从消息头中获取投递标签(deliveryTag),
    // 在RabbitMQ中,每条消息都会被分配一个唯一的投递标签，用于标识该消息在通道中的投递状态和顺序。通过使用@Header(AmqpHeaders.DELIVERY_TAG)注解,可以从消息头中提取出该投递标签,并将其赋值给long deliveryTag参数。
    public void receiveMessage(String message, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
        log.info("receiveMessage message = {}", message);
        if (StringUtils.isBlank(message)) {
            // 如果更新失败，拒绝当前消息，不让消息重新进入队列
            channel.basicNack(deliveryTag, false, false);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "消息为空");
        }
        long chartId = Long.parseLong(message);
        Chart chart = chartService.getById(chartId);
        if (chart == null) {
            // 如果图表为空，拒绝消息并抛出业务异常
            channel.basicNack(deliveryTag, false, false);
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "图表为空");
        }
        //先修改图表状态为“执行中”，等执行完成后再修改为“已完成”
        Chart updateChart = new Chart();
        updateChart.setId(chart.getId());
        updateChart.setStatus("running");
        boolean updateR = chartService.updateById(updateChart);
        if (!updateR) {
            chartService.handleChartUpdateError(chart.getId(), "更新图表《执行中》状态失败");
        }

        // 调用 AI
        //AI处理，拿到返回结果
        //AI模型的ID
//        long biModelId = 1659171950288818178L;
        //String aiResult = aiManager.doChat(biModelId, userInput.toString());
        String aiResult = aiManager.doChatByXingHuo(buildUserInput(chart));
        
        //根据中括号拆分
        String[] splits = aiResult.split(ChartConstant.GEN_CONTENT_SPLITS);
        //最前面有个空字符串 + js代码 + 分析结论
        if (splits.length < 3) {
            channel.basicNack(deliveryTag, false, false);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "AI生成错误");
        }

        String genChart = splits[1].trim();
        String genResult = splits[2].trim();


        //得到AI结果，再更新一次数据库
        Chart updateChartResult = new Chart();
        updateChartResult.setId(chart.getId());
        updateChartResult.setGenChart(genChart);
        updateChartResult.setGenResult(genResult);
        //设置任务状态为成功，succeed
        updateChartResult.setStatus("succeed");
        boolean UpdateResult = chartService.updateById(updateChartResult);
        if (!UpdateResult) {
            channel.basicNack(deliveryTag, false, false);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "图表更新《成功》状态失败");
        }
    }

    /**
     * 死信队列,消费异常消息，把消息更新为failed
     *
     * @param message
     * @param channel
     * @param deliveryTag
     */
    @RabbitListener(queues = {BiMqConstant.BI_DLX_QUEUE_NAME}, ackMode = "MANUAL")
    public void receiveErrorMessage(String message, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
        if (StringUtils.isBlank(message)) {
            throwExceptionAndNackMessage(channel, deliveryTag);
        }
        log.info("receiveErrorMessage message = {}", message);
        long chartId = Long.parseLong(message);
        //把消息更新为failed
        Chart updateChartResult = new Chart();
        updateChartResult.setId(chartId);
        updateChartResult.setStatus("failed");
        boolean UpdateResult = chartService.updateById(updateChartResult);
        ThrowUtils.throwIf(!UpdateResult,ErrorCode.SYSTEM_ERROR,"死信队列消费消息失败");
        try {
            channel.basicAck(deliveryTag, false);
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    /**
     * 抛异常同时拒绝消息
     *
     * @param channel
     * @param deliveryTag
     */
    private void throwExceptionAndNackMessage(Channel channel, long deliveryTag) {
        try {
            channel.basicNack(deliveryTag, false, false);
        } catch (IOException e) {
            log.error(e.getMessage());
        }
        throw new BusinessException(ErrorCode.SYSTEM_ERROR);
    }

    private String buildUserInput(Chart chart) {
        // 获取图表的目标、类型和数据
        String goal = chart.getGoal();
        String chartType = chart.getChartType();
        String csvData = chart.getChartData();

        // 预设的用户的输入样式(参考)

        //构造用户输入
        StringBuilder userInput = new StringBuilder();
        userInput.append("分析需求：").append("\n");

        //拼接分析目标
        //如果图表类型不为空，拼接图表类型
        if (StringUtils.isNotBlank(chartType)) {
            goal += ",请使用" + chartType;
        }
        userInput.append(goal).append("\n");

        //拼接压缩后的数据
        userInput.append("原始数据：").append("\n");
        userInput.append(csvData).append("\n");

        return userInput.toString();
    }
}