package com.zdzc.collector.rabbitmq.handler;

import ch.qos.logback.core.encoder.ByteArrayUtil;
import com.rabbitmq.client.Channel;
import com.zdzc.collector.common.jenum.DataType;
import com.zdzc.collector.common.packet.Message;
import com.zdzc.collector.common.utils.ByteUtil;
import com.zdzc.collector.rabbitmq.core.MqSender;
import com.zdzc.collector.rabbitmq.init.MqInitializer;
import io.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Author liuwei
 * @Description 队列消息处理类
 * @Date 2018/12/12 10:34
 */
public class MqMessageHandler {

    private static final Logger logger = LoggerFactory.getLogger(MqMessageHandler.class);

    private static AtomicInteger gpsNum = new AtomicInteger(0);
    private static AtomicInteger alarmNum = new AtomicInteger(0);
    private static AtomicInteger heartbeatNum = new AtomicInteger(0);

    /**
     * 队列消息处理
     * @author liuwei
     * @return
     * @exception
     * @date 2018/12/12 11:04
     */
    public static void handler(Message message) {
        //将收到的定位、报警、心跳消息推送至Rabbitmq
        int msgType = message.getHeader().getMsgType();
        if(msgType == DataType.GPS.getValue()){
            //定位
            toSendGpsMessage(message);
        }else if(msgType == DataType.ALARM.getValue()){
            //报警
            toSendAlarmMessage(message);

        }else if(msgType == DataType.HEARTBEAT.getValue()){
            //心跳
            toSendHeartBeatMessage(message);
        }

    }

    /**
     * 发送定位消息到MQ
     * @author liuwei
     * @return
     * @exception
     * @date 2018/12/12 11:04
     */
    public static void toSendGpsMessage(Message message){
        gpsNum.incrementAndGet();
        CopyOnWriteArrayList<Channel> channels = MqInitializer.gpsChannels;
        Channel channel = channels.get(gpsNum.intValue() % channels.size());
        String queueName = MqInitializer.gpsQueuePrefix + (gpsNum.intValue() % MqInitializer.gpsQueueCount + 1);
        byte[] sign = new byte[1];
        sign[0] = 01;
        byte[] newBody = ByteUtil.bytesMerge(sign, message.getBody());
        byte[] sendMsg = ByteUtil.bytesMerge(
                ByteArrayUtil.hexStringToByteArray(message.getHeader().getTerminalPhone()), newBody);
        String hex = ByteArrayUtil.toHexString(sendMsg);
        message.setSendBody(hex.getBytes(CharsetUtil.UTF_8));
        MqSender.send(channel, message, queueName);
        logger.debug("[GPS] {} to queue -> {}", hex, queueName);
    }

    /**
     * 发送报警消息到MQ
     * @author liuwei
     * @return
     * @exception
     * @date 2018/12/12 11:05
     */
    public static void toSendAlarmMessage(Message message){
        alarmNum.incrementAndGet();
        CopyOnWriteArrayList<Channel> channels = MqInitializer.alarmChannels;
        Channel channel = channels.get(alarmNum.intValue() % channels.size());
        String queueName = MqInitializer.alarmQueuePrefix + (alarmNum.intValue() % MqInitializer.alarmQueueCount + 1);
        byte[] sendMsg = ByteUtil.bytesMerge(ByteArrayUtil
                .hexStringToByteArray(message.getHeader().getTerminalPhone()), message.getBody());
        String hex = ByteArrayUtil.toHexString(sendMsg);
        message.setSendBody(hex.getBytes(CharsetUtil.UTF_8));
        MqSender.send(channel, message, queueName);
        logger.debug("[ALARM] {} to queue -> {}", hex, queueName);
        //报警推送到电动车平台MQ一份
        CopyOnWriteArrayList<Channel> chs = MqInitializer.businessChannels;
        Channel ch = chs.get(alarmNum.intValue() % chs.size());
        String qn = MqInitializer.businessQueuePrefix +
                (alarmNum.intValue() % MqInitializer.businessQueueCount + MqInitializer.businessQueueStart);
        MqSender.send(ch, message, qn);
        logger.debug("[ALARM] {} to queue -> {}", hex, qn);
    }

    /**
     * 发送心跳消息到MQ
     * @author liuwei
     * @return
     * @exception
     * @date 2018/12/12 11:05
     */
    public static void toSendHeartBeatMessage(Message message){
        heartbeatNum.incrementAndGet();
        CopyOnWriteArrayList<Channel> channels = MqInitializer.heartbeatChannels;
        Channel channel = channels.get(heartbeatNum.intValue() % channels.size());
        String queueName = MqInitializer.heartbeatQueuePrefix + (heartbeatNum.intValue() % MqInitializer.heartbeatQueueCount + 1);
        byte[] sendMsg = ByteUtil.bytesMerge(ByteArrayUtil
                .hexStringToByteArray(message.getHeader().getTerminalPhone()), message.getBody());
        String hex = ByteArrayUtil.toHexString(sendMsg);
        message.setSendBody(hex.getBytes(CharsetUtil.UTF_8));
        MqSender.send(channel, message, queueName);
        logger.debug("[HEARTBEAT] {} to queue -> {}", hex, queueName);
    }
}
