package com.george.springbootinit.mq;

import com.rabbitmq.client.*;

public class DirectConsumer {

  private static final String EXCHANGE_NAME = "direct-exchange";

  public static void main(String[] argv) throws Exception {
      ConnectionFactory factory = new ConnectionFactory();
      factory.setHost("localhost");
      Connection connection = factory.newConnection();

      //创建两个队列来模拟消费广播消息的功能
      Channel channel = connection.createChannel();
      channel.exchangeDeclare(EXCHANGE_NAME, "direct");

      // 创建队列1.
      String queueName = "A_queue";
      // 在通道上声明一个队列，指定队列名，
      channel.queueDeclare(queueName, true, false, false, null);
      channel.queueBind(queueName, EXCHANGE_NAME, "flagA");

      String queueName2 = "B_queue";
      channel.queueDeclare(queueName2, true, false, false, null);
      channel.queueBind(queueName2, EXCHANGE_NAME, "flagB");


      // 创建支付回调函数1，怎么处理消息
      DeliverCallback deliverCallback1 = (consumerTag, delivery) -> {
          String message = new String(delivery.getBody(), "UTF-8");
          System.out.println(" [A1] Received '"+delivery.getEnvelope().getRoutingKey()+":" + message + "'");
          // 手动确认消息
          channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
      };

      DeliverCallback deliverCallback1_2 = (consumerTag, delivery) -> {
          String message = new String(delivery.getBody(), "UTF-8");
          System.out.println(" [A2] Received '"+delivery.getEnvelope().getRoutingKey()+":" + message + "'");
          // 手动确认消息
          channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
      };

      DeliverCallback deliverCallback2 = (consumerTag, delivery) -> {
          String message = new String(delivery.getBody(), "UTF-8");
          System.out.println(" [B] Received '"+delivery.getEnvelope().getRoutingKey()+":" + message + "'");
      };

      // 开始监听消息队列1,一个消息队列的消息可以有多种处理方法
      channel.basicConsume(queueName, false, deliverCallback1_2, consumerTag -> { });
      channel.basicConsume(queueName, false, deliverCallback1, consumerTag -> { });

      // 开始监听消息队列2
      channel.basicConsume(queueName2, true, deliverCallback2, consumerTag -> { });
  }
}