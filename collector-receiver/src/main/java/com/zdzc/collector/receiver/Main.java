package com.zdzc.collector.receiver;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.zdzc.collector.common.jfinal.Config;
import com.zdzc.collector.receiver.Consumer.Worker;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class Main {

    public static void main(String[] args) throws IOException, TimeoutException {
        Config.use("application.properties");
        String host = Config.get("mq.server.hostname");
        int port = Config.getInt("mq.server.port");
        String userName = Config.get("mq.server.username");
        String pwd = Config.get("mq.server.password");
        int interval = Config.getInt("mq.server.net.interval");
        String queueName = Config.get("gps.queue.name");
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(host);
        factory.setPort(port);
        factory.setUsername(userName);
        factory.setPassword(pwd);
        factory.setAutomaticRecoveryEnabled(true);
        factory.setNetworkRecoveryInterval(interval);
        Connection connection = factory.newConnection();

        Channel channel = connection.createChannel();
        //指定队列持久化
        channel.queueDeclare(queueName, true, false, false, null);

        //指定该消费者同时只接收一条消息
        channel.basicQos(1);
        Worker worker = new Worker(channel, queueName);
        worker.doWork();
    }

}
