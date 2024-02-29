package com.george.springbootinit.mq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

public class FanoutConsumer {
  private static final String EXCHANGE_NAME = "fanout-exchange";

  public static void main(String[] argv) throws Exception {
    ConnectionFactory factory = new ConnectionFactory();
    factory.setHost("localhost");
    Connection connection = factory.newConnection();

    //创建两个队列来模拟消费广播消息的功能
    Channel channel = connection.createChannel();

    // 声明交换机，通道绑定交换机
    channel.exchangeDeclare(EXCHANGE_NAME, "fanout");

    // 创建队列1.
    String queueName = "xiaowang_queue";
    // 在通道上声明一个队列，指定队列名，
    channel.queueDeclare(queueName, true, false, false, null);
    channel.queueBind(queueName, EXCHANGE_NAME, "");

    String queueName2 = "xiaoli_queue";
    channel.queueDeclare(queueName2, true, false, false, null);
    channel.queueBind(queueName2, EXCHANGE_NAME, "");

    System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

    // 创建支付回调函数1，怎么处理消息
    DeliverCallback deliverCallback1 = (consumerTag, delivery) -> {
        String message = new String(delivery.getBody(), "UTF-8");
        System.out.println(" [小王] Received '" + message + "'");
    };

    DeliverCallback deliverCallback2 = (consumerTag, delivery) -> {
      String message = new String(delivery.getBody(), "UTF-8");
      System.out.println(" [小李] Received '" + message + "'");
    };

    // 开始监听消息队列1
    channel.basicConsume(queueName, true, deliverCallback1, consumerTag -> { });
    // 开始监听消息队列2
    channel.basicConsume(queueName2, true, deliverCallback2, consumerTag -> { });
  }
}