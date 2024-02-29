package com.george.springbootinit.mq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

public class MultiConsumer {

  private static final String TASK_QUEUE_NAME = "task_queue";

  public static void main(String[] argv) throws Exception {
    ConnectionFactory factory = new ConnectionFactory();
    factory.setHost("localhost");
    final Connection connection = factory.newConnection();
    final Channel channel = connection.createChannel();

    channel.queueDeclare(TASK_QUEUE_NAME, true, false, false, null);
    System.out.println(" [*] Waiting for messages. To exit press CTRL+C");
    // 设置预计计数为1，这样RabbitMQ就会在给消费者新信息之前等待先前的消息被确认。
    //channel.basicQos(1);

      //怎么处理消息,创建消息接收回调函数，以便接收消息。
    DeliverCallback deliverCallback = (consumerTag, delivery) -> {
        //将接收的消息转为字符串
        String message = new String(delivery.getBody(), "UTF-8");

        try {
            System.out.println(" [x] Received '" + message + "'");

            //发送确认消息，确认消息已经被处理,在autoack为false时开启
            //channel.basicAck(delivery.getEnvelope().getDeliveryTag(),false);

            // 处理工作,模拟处理消息所花费的时间,机器处理能力有限(接收一条消息,20秒后再接收下一条消息)
            Thread.sleep(20000);
//            doWork(message);
        } catch (InterruptedException e) {
            e.printStackTrace();
            // 发生异常后，拒绝确认消息，发送拒绝消息，并不重新投递该消息
            // multiple:表示批量确认，也就是说是否需要一次性确认所有的历史消息，直到当前这条消息为主。
            // requeue: 表示是否重新入队，true：重新入队处理 false：直接丢弃
            channel.basicNack(delivery.getEnvelope().getDeliveryTag(),false,false);
        } finally {
            System.out.println(" [x] Done");
            // 手动发送应答,告诉RabbitMQ消息已经被处理
            channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
        }
    };
    // 开始消费消息，传入队列名称，是否自动确认，投递回调和消费者取消回调。，接收到的消息会传递给diliverCallback来处理
    //channel.basicConsume(TASK_QUEUE_NAME, true, deliverCallback, consumerTag -> { });

      //监听，修改autoAck 自动确认机制为false，即手动确认消息完成。
    channel.basicConsume(TASK_QUEUE_NAME, false, deliverCallback, consumerTag -> { });
  }

  // 用于模拟消息处理的函数，消息中的每一个'.'字符都会让线程暂停一秒钟
//  private static void doWork(String task) {
//    for (char ch : task.toCharArray()) {
//        if (ch == '.') {
//            try {
//                Thread.sleep(1000);
//            } catch (InterruptedException _ignored) {
//                Thread.currentThread().interrupt();
//            }
//        }
//    }
//  }
}