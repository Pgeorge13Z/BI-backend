package com.george.springbootinit.mq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.MessageProperties;

import java.util.Scanner;

public class MultiProducer {

  private static final String TASK_QUEUE_NAME = "task_queue";

  public static void main(String[] argv) throws Exception {
    ConnectionFactory factory = new ConnectionFactory();
    factory.setHost("localhost");
    try (Connection connection = factory.newConnection();
         Channel channel = connection.createChannel()) {
        // 队列持久化，durable设置为true，服务器重启后队列不丢失
        channel.queueDeclare(TASK_QUEUE_NAME, true, false, false, null);
        // 接受用户的输入作为发送的信息
        //String message = String.join(" ", argv);

        //修改为循环内的Scanner来接收输入
        Scanner scanner = new Scanner(System.in);
        //使用循环，每当用户在控制台输入一行文本，将其作为消息发送
        while (scanner.hasNext()) {
            String message = scanner.nextLine();
            // 发布消息到队列，设置消息持久化
            channel.basicPublish("", TASK_QUEUE_NAME,
                    MessageProperties.PERSISTENT_TEXT_PLAIN,
                    message.getBytes("UTF-8"));
            // 输出到控制台，表示消息已发送
            System.out.println(" [x] Sent '" + message + "'");
        }


    }
  }

}