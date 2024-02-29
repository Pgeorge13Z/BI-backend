package com.george.springbootinit.mq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.nio.charset.StandardCharsets;

// 定义一个名为SingleProducer的公开类，用于实现消息发送功能
public class SingleProducer {
    // 定义一个静态常量字符串QUEUE_NAME，值为“hello”，表示我们要向名为“hello”的队列发送信息
    private final static String QUEUE_NAME = "hello";

    public static void main(String[] argv) throws Exception {
        // 创建一个ConnectionFactory对象，这个对象可以用于创建到RabbitMQ服务器的连接。
        ConnectionFactory factory = new ConnectionFactory();
        // 设置ConnectionFactory对象的主机名，表示将连接到本地的RabbitMQ
        factory.setHost("localhost");
        // 如果修改了用户名和密码或者端口，需要对应的修改
        // factory.setUsername();
        // factory.setPassword();
        // factory.setPort();

        // 使用ConnectionFactory建立连接，用于和RabbitMQ服务器交互
        try (Connection connection = factory.newConnection();
             // 通过已建立的连接创建一个新的通道
             Channel channel = connection.createChannel()) {
            // 在通道上声明一个队列，我们在此指定的队列名为"hello"，
            // queueName：消息队列名称（注意，同名称的消息队列，只能用同样的参数创建一次）
            // durable：消息队列重启后，消息是否丢失
            // exclusive：是否只允许当前这个创建消息队列的连接操作消息队列
            // autoDelete：没有人用队列后，是否要删除队列
            channel.queueDeclare(QUEUE_NAME, false, false, false, null);
            // 创建要发送的消息，这里我们将要发送的消息内容设置为"Hello World!"
            String message = "Hello World!";
            // 使用channel.basicPublish方法将消息发布到指定的队列中。这里我们指定的队列名为"hello"
            channel.basicPublish("", QUEUE_NAME, null, message.getBytes(StandardCharsets.UTF_8));
            System.out.println(" [x] Sent '" + message + "'");
        }
    }
}