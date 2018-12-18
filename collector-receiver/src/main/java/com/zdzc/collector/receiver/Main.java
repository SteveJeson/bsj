package com.zdzc.collector.receiver;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.zdzc.collector.receiver.Consumer.Worker;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class Main {

    public static void main(String[] args) throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("192.168.1.187");
        factory.setUsername("admin");
        factory.setPassword("123456");
        Connection connection = factory.newConnection();

        for (int i = 1;i <= 1;i++) {
            Channel channel = connection.createChannel();
            String queueName = "task_queue_" + i;
            //指定队列持久化
            channel.queueDeclare(queueName, true, false, false, null);

            //指定该消费者同时只接收一条消息
            channel.basicQos(100);
            Worker worker = new Worker(channel, queueName);
            worker.run();
        }
    }



}
