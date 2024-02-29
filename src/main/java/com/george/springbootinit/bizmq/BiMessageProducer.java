package com.george.springbootinit.bizmq;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class BiMessageProducer {
    @Resource
    private RabbitTemplate rabbitTemplate;

    /**
     *
     * @param message  消息内容，要发送的具体消息
     */
    public void sendMessage(String message) {
        // 使用rabbitTemplate的convertAndSend方法将消息发送到指定的交换机和路由键
        rabbitTemplate.convertAndSend(BiMqConstant.BI_EXCHANGE_NAME,BiMqConstant.BI_ROUTING_KEY,message);
    }
}
