package com.zdzc.collector.receiver.MqClients;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.zdzc.collector.common.jfinal.Config;
import com.zdzc.collector.receiver.Consumer.Worker;

import java.io.IOException;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeoutException;

public class JTMqClient {

    public static int gpsConnCount = Config.getInt("gps.connection.count");

    public static String gpsQueuePrefix = Config.get("gps.queue.prefix");

    public static int gpsQueueCount = Config.getInt("gps.queue.count");

    public static int gpsQueuePrefetch = Config.getInt("gps.queue.prefetch");

    public static int gpsQueueStart = Config.getInt("gps.queue.start");

    public static int alarmConnCount = Config.getInt("alarm.connection.count");

    public static int alarmChannelCount = Config.getInt("alarm.channel.count");

    public static int alarmQueuePrefetch = Config.getInt("alarm.queue.prefetch");

    public static String alarmQueuePrefix = Config.get("alarm.queue.prefix");

    public static int alarmQueueCount = Config.getInt("alarm.queue.count");

    public static int alarmQueueStart = Config.getInt("alarm.queue.start");

    public static int heartbeatConnCount = Config.getInt("heartbeat.connection.count");

    public static int heartbeatChannelCount = Config.getInt("heartbeat.channel.count");

    public static String heartbeatQueuePrefix = Config.get("heartbeat.queue.prefix");

    public static int heartbeatQueueCount = Config.getInt("heartbeat.queue.count");

    public static int heartbeatQueuePrefetch = Config.getInt("heartbeat.queue.prefetch");

    public static int heartbeatQueueStart = Config.getInt("heartbeat.queue.start");

    public static CopyOnWriteArrayList<Channel> gpsChannels = new CopyOnWriteArrayList<>();

    public static CopyOnWriteArrayList<Channel> alarmChannels = new CopyOnWriteArrayList<>();

    public static CopyOnWriteArrayList<Channel> heartbeatChannels = new CopyOnWriteArrayList<>();

    public static void init() throws IOException, TimeoutException {
        String host = Config.get("mq.server.hostname");
        int port = Config.getInt("mq.server.port");
        String userName = Config.get("mq.server.username");
        String pwd = Config.get("mq.server.password");
        int interval = Config.getInt("mq.server.net.interval");
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(host);
        factory.setPort(port);
        factory.setUsername(userName);
        factory.setPassword(pwd);
        factory.setAutomaticRecoveryEnabled(true);
        factory.setNetworkRecoveryInterval(interval);
        for (int i = 1; i <= gpsQueueCount; i++) {
            Connection connection = factory.newConnection();
            Channel channel = connection.createChannel();
            String queueName = gpsQueuePrefix + i;
            channel.queueDeclare(queueName, true, false, false, null);
            channel.basicQos(gpsQueuePrefetch);
            Worker worker = new Worker(channel, queueName);
            Thread thread = new Thread(worker);
            thread.setName("GPS");
            thread.start();
        }

        for (int i = 1; i <= alarmQueueCount; i++) {
            Connection connection = factory.newConnection();
            Channel channel = connection.createChannel();
            String queueName = alarmQueuePrefix + i;
            channel.queueDeclare(queueName, true, false, false, null);
            channel.basicQos(alarmQueuePrefetch);
            Worker worker = new Worker(channel, queueName);
            Thread thread = new Thread(worker);
            thread.setName("ALARM");
            thread.start();
        }

        for (int i = 1; i <= heartbeatQueueCount; i++) {
            Connection connection = factory.newConnection();
            Channel channel = connection.createChannel();
            String queueName = heartbeatQueuePrefix + i;
            channel.queueDeclare(queueName, true, false, false, null);
            channel.basicQos(heartbeatQueuePrefetch);
            Worker worker = new Worker(channel, queueName);
            Thread thread = new Thread(worker);
            thread.setName("HEARTBEAT");
            thread.start();
        }
    }
}
