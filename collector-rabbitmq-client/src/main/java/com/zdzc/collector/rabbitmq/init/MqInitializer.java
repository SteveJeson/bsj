package com.zdzc.collector.rabbitmq.init;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.zdzc.collector.common.jenum.ProtocolType;
import com.zdzc.collector.common.jfinal.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeoutException;

/**
 * @Author liuwei
 * @Description MQ初始化类
 * @Date 2018/12/11 15:43
 */
public class MqInitializer {

    public static String hostname = Config.get("mq.server.hostname");

    public static String username = Config.get("mq.server.username");

    public static String password = Config.get("mq.server.password");

    public static int port = Config.getInt("mq.server.port");

    public static int interval = Config.getInt("mq.server.net.interval");

    public static String protocolType = Config.get("protocol.type");

    public static int gpsConnCount = Config.getInt("gps.connection.count");

    public static int wrtGpsConnCount = Config.getInt("wrt.gps.connection.count", 0);

    public static int gpsChannelCount = Config.getInt("gps.channel.count");

    public static int wrtGpsChannelCount = Config.getInt("wrt.gps.channel.count", 0);

    public static String gpsQueuePrefix = Config.get("gps.queue.prefix");

    public static String wrtGpsQueuePrefix = Config.get("wrt.gps.queue.prefix");

    public static int gpsQueueCount = Config.getInt("gps.queue.count");

    public static int wrtGpsQueueCount = Config.getInt("wrt.gps.queue.count",0);

    public static int gpsQueueStart = Config.getInt("gps.queue.start");

    public static int wrtGpsQueueStart = Config.getInt("wrt.gps.queue.start", 0);

    public static int alarmConnCount = Config.getInt("alarm.connection.count");

    public static int wrtAlarmConnCount = Config.getInt("wrt.alarm.connection.count", 0);

    public static int alarmChannelCount = Config.getInt("alarm.channel.count");

    public static int wrtAlarmChannelCount = Config.getInt("wrt.alarm.channel.count", 0);

    public static String alarmQueuePrefix = Config.get("alarm.queue.prefix");

    public static String wrtAlarmQueuePrefix = Config.get("wrt.alarm.queue.prefix");

    public static int alarmQueueCount = Config.getInt("alarm.queue.count");

    public static int wrtAlarmQueueCount = Config.getInt("wrt.alarm.queue.count", 0);

    public static int alarmQueueStart = Config.getInt("alarm.queue.start");

    public static int wrtAlarmQueueStart = Config.getInt("wrt.alarm.queue.start", 0);

    public static int heartbeatConnCount = Config.getInt("heartbeat.connection.count");

    public static int wrtHeartbeatConnCount = Config.getInt("wrt.heartbeat.connection.count", 0);

    public static int heartbeatChannelCount = Config.getInt("heartbeat.channel.count");

    public static int wrtHeartbeatChannelCount = Config.getInt("wrt.heartbeat.channel.count", 0);

    public static String heartbeatQueuePrefix = Config.get("heartbeat.queue.prefix");

    public static String wrtHeartbeatQueuePrefix = Config.get("wrt.heartbeat.queue.prefix");

    public static int heartbeatQueueCount = Config.getInt("heartbeat.queue.count");

    public static int wrtHeartbeatQueueCount = Config.getInt("wrt.heartbeat.queue.count", 0);

    public static int heartbeatQueueStart = Config.getInt("heartbeat.queue.start");

    public static int wrtHeartbeatQueueStart = Config.getInt("wrt.heartbeat.queue.start", 0);

    public static int businessConnCount = Config.getInt("business.connection.count");

    public static int businessChannelCount = Config.getInt("business.channel.count");

    public static String businessQueuePrefix = Config.get("business.queue.prefix");

    public static int businessQueueCount = Config.getInt("business.queue.count");

    public static int businessQueueStart = Config.getInt("business.queue.start");

    public static int wrtControllerConnCount = Config.getInt("wrt.controller.connection.count", 0);

    public static int wrtControllerChannelCount = Config.getInt("wrt.controller.channel.count", 0);

    public static String wrtControllerQueuePrefix = Config.get("wrt.controller.queue.prefix");

    public static int wrtControllerQueueCount = Config.getInt("wrt.controller.queue.count", 0);

    public static int wrtControllerQueueStart = Config.getInt("wrt.controller.queue.start", 0);

    public static String wrtCmdQueueName = Config.get("wrt.command.queue.name");

    public static String wrtCmdReplyQueueName = Config.get("wrt.command.queue.reply.name");

    public static String bsjLoginQueueName = Config.get("bsj.login.queue.name");

    public static String bsjLocationQueueName = Config.get("bsj.location.queue.name");

    public static String bsjAlarmQueueName = Config.get("bsj.alarm.queue.name");

    public static String bsjHeartbeatQueueName = Config.get("bsj.heartbeat.queue.name");

    public static String bsjCmdExchangeName = Config.get("bsj.command.exchange.name");

    public static String bsjCmdQueueName = Config.get("bsj.command.queue.name");

    public static String bsjCmdReplyQueueName = Config.get("bsj.command.queue.reply.name");

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

    public static Channel bsjLoginChannel;

    public static Channel bsjLocationChannel;

    public static Channel bsjAlarmChannel;

    public static Channel bsjHeartbeatChannel;

    public static Channel bsjReplyChannel;

    public static Channel wrtCmdChannel;

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
        }else if(ProtocolType.BSJ.getValue().equals(protocolType)){
            logger.info("即将配置消息队列 -> {}", ProtocolType.BSJ.getDesc());
            configBsjMq();
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
        createQueues(factory.newConnection(), wrtCmdReplyQueueName, replyChannel);
        createQueues(factory.newConnection(), wrtCmdQueueName, wrtCmdChannel);

    }

    /**
     * 配置博实结协议消息队列
     * @throws Exception
     */
    private static void configBsjMq() throws IOException, TimeoutException{
        //登录
        Connection loginConnection = factory.newConnection();
        bsjLoginChannel = loginConnection.createChannel();
        bsjLoginChannel.queueDeclare(bsjLoginQueueName, true, false, false, null);
        //位置
        Connection locationConnection = factory.newConnection();
        bsjLocationChannel = locationConnection.createChannel();
        bsjLocationChannel.queueDeclare(bsjLocationQueueName, true, false, false, null);
        //报警
        Connection alarmConnection = factory.newConnection();
        bsjAlarmChannel = alarmConnection.createChannel();
        bsjAlarmChannel.queueDeclare(bsjAlarmQueueName, true, false, false, null);
        //心跳
        Connection heartbeatConnection = factory.newConnection();
        bsjHeartbeatChannel = heartbeatConnection.createChannel();
        bsjHeartbeatChannel.queueDeclare(bsjHeartbeatQueueName, true, false, false, null);
        //下发消息回复
        Connection replyConnection = factory.newConnection();
        bsjReplyChannel = replyConnection.createChannel();
        bsjReplyChannel.queueDeclare(bsjCmdReplyQueueName, true, false, false, null);
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
     * @param queuePrefix 队列前缀
     * @param channelCount 通道数量
     * @param queueCount 队列数量
     * @param queueStart 队列起始数
     * @param channels 通道集合
     */
    public static void createQueues(Connection connection, String queuePrefix, int channelCount, int queueCount, int queueStart, CopyOnWriteArrayList<Channel> channels){
        for(int i = 0;i < channelCount;i++){
            try {
                Channel channel = connection.createChannel();
                for(int j = 0;j < queueCount;j++){
                    String queueName = queuePrefix + (queueStart + j);
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
//            if (ProtocolType.BSJ.getValue().equals(protocolType)){
//                bsjReplyChannel = connection.createChannel();
//                bsjReplyChannel.queueDeclare(queueName, true, false, false, null);
//            }else if (ProtocolType.WRT.getValue().equals(protocolType)) {
//                replyChannel = connection.createChannel();
//                replyChannel.queueDeclare(queueName, true, false, false, null);
//            }

    public static void createQueues(Connection connection, String queueName, Channel channel){
        try {
            channel = connection.createChannel();
            channel.queueDeclare(queueName, true, false, false, null);

        } catch (IOException e) {
            logger.error(e.toString());
        }
    }
}
