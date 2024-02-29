package com.george.springbootinit.mq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.util.Scanner;

public class FanoutProducer {

  private static final String EXCHANGE_NAME = "fanout-exchange";

  public static void main(String[] argv) throws Exception {
    ConnectionFactory factory = new ConnectionFactory();
    factory.setHost("localhost");
    try (Connection connection = factory.newConnection();
         Channel channel = connection.createChannel()) {
        // 声明fanout类型的交换机
        channel.exchangeDeclare(EXCHANGE_NAME, "fanout");
        // 创建一个Scanner对象来读取用户输入
        Scanner scanner = new Scanner(System.in);
        while (scanner.hasNext()) {
            // 从用户输入中获取消息内容
            String message = scanner.nextLine();
            // 将消息发送到指定的交换机（fanout交换机），不指定路由键（空字符串）
            channel.basicPublish(EXCHANGE_NAME, "", null, message.getBytes("UTF-8"));
            // 打印发送的消息内容
            System.out.println(" [x] Sent '" + message + "'");
        }
    }
  }
}