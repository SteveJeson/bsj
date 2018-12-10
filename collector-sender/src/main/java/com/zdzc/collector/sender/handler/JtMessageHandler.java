package com.zdzc.collector.sender.handler;

import ch.qos.logback.core.encoder.ByteArrayUtil;
import com.rabbitmq.client.Channel;
import com.zdzc.collector.common.jenum.DataType;
import com.zdzc.collector.common.packet.Message;
import com.zdzc.collector.common.utils.ByteUtil;
import com.zdzc.collector.rabbitmq.core.MqSender;
import com.zdzc.collector.rabbitmq.init.MqInitializer;
import com.zdzc.collector.tcpclient.core.ClientPoolManager;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class JtMessageHandler {

    private static final Logger logger = LoggerFactory.getLogger(JtMessageHandler.class);

    private static final AtomicInteger gpsNum = new AtomicInteger(0);
    private static final AtomicInteger alarmNum = new AtomicInteger(0);
    private static final AtomicInteger heartbeatNum = new AtomicInteger(0);

    public static void handler(ChannelHandlerContext ctx, Message message){
        //给客户端发送应答消息
        if(message.getReplyBody() != null){
            ctx.writeAndFlush(Unpooled.copiedBuffer(message.getReplyBody()));
        }
        if(message.getExtReplyBody() != null){
            ctx.writeAndFlush(Unpooled.copiedBuffer(message.getExtReplyBody()));
        }
        //将收到的定位、报警、心跳消息推送至Rabbitmq
        int msgType = message.getHeader().getMsgType();
        if(msgType == DataType.GPS.getValue()){
            //定位
//                System.out.println("gps num ==> " + gpsNum.intValue());
            toSendGpsMessage(message);
        }else if(msgType == DataType.ALARM.getValue()){
            //报警
//                System.out.println("alarm num ==> " + alarmNum.intValue());
            toSendAlarmMessage(message);

        }else if(msgType == DataType.HEARTBEAT.getValue()){
            //心跳
//                System.out.println("heartbeat num ==> " + heartbeatNum.intValue());
            toSendHeartBeatMessage(message);
        }
    }

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
        //转发
        ClientPoolManager.send(message.getAll());
    }

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

        //报警推送到电动车平台MQ一份
        CopyOnWriteArrayList<Channel> chs = MqInitializer.businessChannels;
        Channel ch = chs.get(alarmNum.intValue() % chs.size());
        String qn = MqInitializer.businessQueuePrefix +
                (alarmNum.intValue() % MqInitializer.businessQueueCount + MqInitializer.businessQueueStart);
        MqSender.send(ch, message, qn);
        //转发
        ClientPoolManager.send(message.getAll());
    }

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
        //转发
        ClientPoolManager.send(message.getAll());
    }
}
