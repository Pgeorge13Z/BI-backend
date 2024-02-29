package com.george.springbootinit.mq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.util.Scanner;

public class DirectProducer {

    private static final String EXCHANGE_NAME = "direct-exchange";

    public static void main(String[] argv) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        try (Connection connection = factory.newConnection();
             Channel channel = connection.createChannel()) {
            channel.exchangeDeclare(EXCHANGE_NAME, "direct");

            Scanner scanner = new Scanner(System.in);
            while (scanner.hasNext()) {
                // 从用户输入中获取消息内容
                String userInput = scanner.nextLine();
                String[] strings = userInput.split(" ");

                // 如果输入内容不符合要求，读下一行
                if (strings.length < 1) {
                    continue;
                }

                //消息内容和路由键
                String message = strings[0];
                String routingKey = strings[1];

                // 将消息发送到指定的交换机（fanout交换机），不指定路由键（空字符串）
                channel.basicPublish(EXCHANGE_NAME, routingKey, null, message.getBytes("UTF-8"));
                // 打印成功发送的消息信息，包括消息内容和路由键
                System.out.println(" [x] Sent '" + message + " with routing:" + routingKey + "'");
            }


        }
    }
    //..
}