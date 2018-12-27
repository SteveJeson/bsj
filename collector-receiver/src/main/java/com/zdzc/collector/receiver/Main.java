package com.zdzc.collector.receiver;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.zdzc.collector.common.jfinal.Config;
import com.zdzc.collector.receiver.Consumer.Worker;
import com.zdzc.collector.receiver.db.DbConnectionPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) throws IOException, TimeoutException {
        Config.use("application.properties");
        DbConnectionPool pool = new DbConnectionPool();
        pool.init();

        String host = Config.get("mq.server.hostname");
        int port = Config.getInt("mq.server.port");
        String userName = Config.get("mq.server.username");
        String pwd = Config.get("mq.server.password");
        int interval = Config.getInt("mq.server.net.interval");
        String gpsQueueName = Config.get("gps.queue.name");
        int gpsPrefetch = Config.getInt("gps.queue.prefetch");
        String heartQueueName = Config.get("heartbeat.queue.name");
        int heartPrefetch = Config.getInt("login.queue.prefetch");
        String alarmQueueName = Config.get("alarm.queue.name");
        int alarmPrefetch = Config.getInt("login.queue.prefetch");
        String loginQueueName = Config.get("login.queue.name");
        int loginPrefetch = Config.getInt("login.queue.prefetch");
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(host);
        factory.setPort(port);
        factory.setUsername(userName);
        factory.setPassword(pwd);
        factory.setAutomaticRecoveryEnabled(true);
        factory.setNetworkRecoveryInterval(interval);
        Connection connection = factory.newConnection();

        Channel channel = connection.createChannel();
        channel.queueDeclare(loginQueueName, true, false, false, null);
        channel.basicQos(loginPrefetch);
        Worker worker = new Worker(channel, loginQueueName);
        try {
            worker.run();
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e.toString());
        }


    }

}
