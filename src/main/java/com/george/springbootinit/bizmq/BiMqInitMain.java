package com.george.springbootinit.bizmq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.util.HashMap;
import java.util.Map;

//http://localhost:15672/#/

/**
 * 队列初始化函数
 * 注意：只能执行一次！！！ 第二次会报错
 */
public class BiMqInitMain {
    public static void main(String[] args) {
        try {
            ConnectionFactory factory = new ConnectionFactory();
            Connection connection = factory.newConnection();
            Channel channel = connection.createChannel();
            // 声明死信队列
            channel.exchangeDeclare(BiMqConstant.BI_DLX_EXCHANGE_NAME, "direct");
            channel.queueDeclare(BiMqConstant.BI_DLX_QUEUE_NAME, true, false, false, null);
            channel.queueBind(BiMqConstant.BI_DLX_QUEUE_NAME, BiMqConstant.BI_DLX_EXCHANGE_NAME, BiMqConstant.BI_DLX_ROUTING_KEY);

            channel.exchangeDeclare(BiMqConstant.BI_EXCHANGE_NAME, "direct");

            Map<String, Object> arg = new HashMap<String, Object>();
            arg.put("x-dead-letter-exchange", BiMqConstant.BI_DLX_EXCHANGE_NAME);
            arg.put("x-dead-letter-routing-key", BiMqConstant.BI_DLX_ROUTING_KEY);
            channel.queueDeclare(BiMqConstant.BI_QUEUE_NAME, true, false, false, arg);
            channel.queueBind(BiMqConstant.BI_QUEUE_NAME, BiMqConstant.BI_EXCHANGE_NAME, BiMqConstant.BI_ROUTING_KEY);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

