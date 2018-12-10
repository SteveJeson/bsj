package com.zdzc.collector.rabbitmq.init;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.zdzc.collector.common.jenum.ProtocolType;
import com.zdzc.collector.common.jfinal.P;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeoutException;

public class MqInitializer {

    public static String hostname = P.get("mq.server.hostname");

    public static String username = P.get("mq.server.username");

    public static String password = P.get("mq.server.password");

    public static int port = P.getInt("mq.server.port");

    public static int interval = P.getInt("mq.server.net.interval");

    public static String protocolType = P.get("protocol.type");

    public static int gpsConnCount = P.getInt("gps.connection.count");

    public static int wrtGpsConnCount = P.getInt("wrt.gps.connection.count", 0);

    public static int gpsChannelCount = P.getInt("gps.channel.count");

    public static int wrtGpsChannelCount = P.getInt("wrt.gps.channel.count", 0);

    public static String gpsQueuePrefix = P.get("gps.queue.prefix");

    public static String wrtGpsQueuePrefix = P.get("wrt.gps.queue.prefix");

    public static int gpsQueueCount = P.getInt("gps.queue.count");

    public static int wrtGpsQueueCount = P.getInt("wrt.gps.queue.count",0);

    public static int gpsQueueStart = P.getInt("gps.queue.start");

    public static int wrtGpsQueueStart = P.getInt("wrt.gps.queue.start", 0);

    public static int alarmConnCount = P.getInt("alarm.connection.count");

    public static int wrtAlarmConnCount = P.getInt("wrt.alarm.connection.count", 0);

    public static int alarmChannelCount = P.getInt("alarm.channel.count");

    public static int wrtAlarmChannelCount = P.getInt("wrt.alarm.channel.count", 0);

    public static String alarmQueuePrefix = P.get("alarm.queue.prefix");

    public static String wrtAlarmQueuePrefix = P.get("wrt.alarm.queue.prefix");

    public static int alarmQueueCount = P.getInt("alarm.queue.count");

    public static int wrtAlarmQueueCount = P.getInt("wrt.alarm.queue.count", 0);

    public static int alarmQueueStart = P.getInt("alarm.queue.start");

    public static int wrtAlarmQueueStart = P.getInt("wrt.alarm.queue.start", 0);

    public static int heartbeatConnCount = P.getInt("heartbeat.connection.count");

    public static int wrtHeartbeatConnCount = P.getInt("wrt.heartbeat.connection.count", 0);

    public static int heartbeatChannelCount = P.getInt("heartbeat.channel.count");

    public static int wrtHeartbeatChannelCount = P.getInt("wrt.heartbeat.channel.count", 0);

    public static String heartbeatQueuePrefix = P.get("heartbeat.queue.prefix");

    public static String wrtHeartbeatQueuePrefix = P.get("wrt.heartbeat.queue.prefix");

    public static int heartbeatQueueCount = P.getInt("heartbeat.queue.count");

    public static int wrtHeartbeatQueueCount = P.getInt("wrt.heartbeat.queue.count", 0);

    public static int heartbeatQueueStart = P.getInt("heartbeat.queue.start");

    public static int wrtHeartbeatQueueStart = P.getInt("wrt.heartbeat.queue.start", 0);

    public static int businessConnCount = P.getInt("business.connection.count");

    public static int businessChannelCount = P.getInt("business.channel.count");

    public static String businessQueuePrefix = P.get("business.queue.prefix");

    public static int businessQueueCount = P.getInt("business.queue.count");

    public static int businessQueueStart = P.getInt("business.queue.start");

    public static int wrtControllerConnCount = P.getInt("wrt.controller.connection.count", 0);

    public static int wrtControllerChannelCount = P.getInt("wrt.controller.channel.count", 0);

    public static String wrtControllerQueuePrefix = P.get("wrt.controller.queue.prefix");

    public static int wrtControllerQueueCount = P.getInt("wrt.controller.queue.count", 0);

    public static int wrtControllerQueueStart = P.getInt("wrt.controller.queue.start", 0);

    public static String wrtCmdReplyQueueName = P.get("wrt.command.queue.reply.name");

    public static ConnectionFactory factory;

    public static final Logger logger = LoggerFactory.getLogger(MqInitializer.class);

    public static CopyOnWriteArrayList<Channel> gpsChannels = new CopyOnWriteArrayList<>();

    public static CopyOnWriteArrayList<Channel> wrtGpsChannels = new CopyOnWriteArrayList<>();

    public static CopyOnWriteArrayList<Channel> alarmChannels = new CopyOnWriteArrayList<>();

    public static CopyOnWriteArrayList<Channel> wrtAlarmChannels = new CopyOnWriteArrayList<>();

    public static CopyOnWriteArrayList<Channel> heartbeatChannels = new CopyOnWriteArrayList<>();

    public static CopyOnWriteArrayList<Channel> wrtHeartbeatChannels = new CopyOnWriteArrayList<>();

    public static CopyOnWriteArrayList<Channel> wrtControllerChannels = new CopyOnWriteArrayList<>();

    public static CopyOnWriteArrayList<Channel> businessChannels = new CopyOnWriteArrayList<>();

    public static Channel replyChannel;

    /**
     * 配置MQ
     * @throws IOException
     * @throws TimeoutException
     */
    public static void init() throws IOException, TimeoutException {
        setFactory();
        logger.info("Mq Server -> {}", hostname);
        if(ProtocolType.JT808.getValue().equals(protocolType)){
            logger.info("即将配置消息队列 -> {}", ProtocolType.JT808.getDesc());
            configJtMq();
        }else if(ProtocolType.WRT.getValue().equals(protocolType)){
            logger.info("即将配置消息队列 -> {}", ProtocolType.WRT.getDesc());
            configWrtMq();
        }
    }

    /**
     * 配置部标808协议消息队列
     */
    private static void configJtMq() throws IOException, TimeoutException {
        //位置
        for(int i = 0;i < gpsConnCount;i++){
            logger.info("create GPS connection -> {}", i+1);
            createQueues(factory.newConnection(), gpsQueuePrefix, gpsChannelCount, gpsQueueCount, gpsQueueStart, gpsChannels);
        }

        //报警
        for(int i = 0;i < alarmConnCount;i++){
            logger.info("create ALARM connection -> {}", i+1);
            createQueues(factory.newConnection(), alarmQueuePrefix, alarmChannelCount, alarmQueueCount, alarmQueueStart, alarmChannels);
        }

        //心跳
        for(int i = 0;i < heartbeatConnCount;i++){
            logger.info("create HEARTBEAT connection -> {}", i+1);
            createQueues(factory.newConnection(), heartbeatQueuePrefix, heartbeatChannelCount, heartbeatQueueCount, heartbeatQueueStart, heartbeatChannels);
        }

        //业务
        for(int i = 0;i < businessConnCount;i++){
            logger.info("create BUSINESS connection -> {}", i+1);
            createQueues(factory.newConnection(), businessQueuePrefix, businessChannelCount, businessQueueCount, businessQueueStart, businessChannels);
        }
    }

    /**
     * 配置沃瑞特协议消息队列
     * @throws IOException
     * @throws TimeoutException
     */
    private static void configWrtMq() throws IOException, TimeoutException {
        //位置
        for(int i = 0;i < wrtGpsConnCount;i++){
            logger.info("create GPS connection -> {}", i+1);
            createQueues(factory.newConnection(), wrtGpsQueuePrefix, wrtGpsChannelCount, wrtGpsQueueCount, wrtGpsQueueStart, wrtGpsChannels);
        }

        //报警
        for(int i =0;i < wrtAlarmConnCount;i++){
            logger.info("create ALARM connection -> {}", i+1);
            createQueues(factory.newConnection(), wrtAlarmQueuePrefix, wrtAlarmChannelCount, wrtAlarmQueueCount, wrtAlarmQueueStart, wrtAlarmChannels);
        }

        //心跳
        for(int i = 0;i < wrtHeartbeatConnCount;i++){
            logger.info("create HEARTBEAT connection -> {}", i+1);
            createQueues(factory.newConnection(), wrtHeartbeatQueuePrefix, wrtHeartbeatChannelCount, wrtHeartbeatQueueCount, wrtHeartbeatQueueStart, wrtHeartbeatChannels);
        }

        //控制器
        for(int i = 0;i < wrtControllerConnCount;i++){
            logger.info("create CONTROLLER connection -> {}", i+1);
            createQueues(factory.newConnection(), wrtControllerQueuePrefix, wrtControllerChannelCount, wrtControllerQueueCount, wrtControllerQueueStart, wrtControllerChannels);
        }

        createQueues(factory.newConnection(), wrtCmdReplyQueueName);
    }

    /**
     * Rabbitmq 工厂配置
     */
    private static void setFactory(){
        factory = new ConnectionFactory();
        factory.setHost(hostname);
        factory.setUsername(username);
        factory.setPassword(password);
        factory.setPort(port);
        factory.setAutomaticRecoveryEnabled(true);
        factory.setNetworkRecoveryInterval(interval);
    }

    /**通过规则创建消息队列
     * @param connection 连接对象
     * @param QueuePrefix 队列前缀
     * @param channelCount 通道数量
     * @param queueCount 队列数量
     * @param queueStart 队列起始数
     * @param channels 通道集合
     */
    public static void createQueues(Connection connection, String QueuePrefix, int channelCount, int queueCount, int queueStart, CopyOnWriteArrayList<Channel> channels){
        for(int i = 0;i < channelCount;i++){
            try {
                Channel channel = connection.createChannel();
                for(int j = 0;j < queueCount;j++){
                    String queueName = QueuePrefix + (queueStart + j);
                    channel.queueDeclare(queueName,true,false,false,null);
                }
               channels.add(channel);

            } catch (IOException e) {
                logger.error(e.getMessage());
            }
        }
    }

    /**
     * 直接创建消息队列
     * @param connection
     * @param queueName
     */
    public static void createQueues(Connection connection, String queueName){
        try {
            replyChannel = connection.createChannel();
            replyChannel.queueDeclare(queueName, true, false, false, null);
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
    }

}
