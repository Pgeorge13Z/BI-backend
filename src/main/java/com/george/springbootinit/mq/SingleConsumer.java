package com.george.springbootinit.mq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import java.nio.charset.StandardCharsets;

public class SingleConsumer {

    private final static String QUEUE_NAME = "hello";

    public static void main(String[] argv) throws Exception {
        // 这部分与singleProducer一样
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        channel.queueDeclare(QUEUE_NAME, false, false, false, null);
        System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

        // 定义如何处理消息，创建了一个新的DeliverCallback来处理接收到的消息
        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            // 将消息转换为字符串
            String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
            // 在控制台打印已经接受消息的信息
            System.out.println(" [x] Received '" + message + "'");
        };
        //监听消息， 在channel上开始消费队列中的消息，接收到的消息会传递给diliverCallback来处理，会持续阻塞。
        //String queue, boolean autoAck, DeliverCallback deliverCallback, CancelCallback cancelCallback
        channel.basicConsume(QUEUE_NAME, true, deliverCallback, consumerTag -> { });
    }
}