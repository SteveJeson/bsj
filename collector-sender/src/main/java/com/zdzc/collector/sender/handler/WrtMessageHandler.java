package com.zdzc.collector.sender.handler;

import com.rabbitmq.client.Channel;
import com.zdzc.collector.common.jconst.Command;
import com.zdzc.collector.common.jconst.SysConst;
import com.zdzc.collector.common.jenum.DataType;
import com.zdzc.collector.common.jenum.ProtocolType;
import com.zdzc.collector.common.packet.Message;
import com.zdzc.collector.rabbitmq.core.MqSender;
import com.zdzc.collector.rabbitmq.init.MqInitializer;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Author liuwei
 * @Description 沃瑞特消息处理类
 * @Date 2018/12/11 15:51
 */
public class WrtMessageHandler {

    private static final Logger logger = LoggerFactory.getLogger(JtMessageHandler.class);

    private static AtomicInteger gpsNum = new AtomicInteger(0);
    private static AtomicInteger alarmNum = new AtomicInteger(0);
    private static AtomicInteger heartbeatNum = new AtomicInteger(0);
    private static AtomicInteger controllerNum = new AtomicInteger(0);

    public static ConcurrentHashMap<String, String> channelMap = new ConcurrentHashMap<>();

    public static void handler(ChannelHandlerContext ctx, Message message) throws Exception {
        String channelId = ctx.channel().id().toString();
        //给客户端发送应答消息
        if(message.getReplyBody() != null){
            ctx.writeAndFlush(Unpooled.copiedBuffer(message.getReplyBody()));
        }

        if(message.getExtReplyBody() != null){
            ctx.writeAndFlush(Unpooled.copiedBuffer(message.getExtReplyBody()));
        }

        if(StringUtils.equals(Command.WRT_MSG_ID_LOGIN, message.getHeader().getMsgIdStr())){
            String terminalPhone = message.getHeader().getTerminalPhone();
            if(!channelMap.containsKey(terminalPhone)){
                channelMap.put(terminalPhone, channelId);
                logger.info("saved key value -> {} : {}", terminalPhone, channelId);
            }

            if(!channelMap.containsKey(channelId)){
                channelMap.put(channelId, terminalPhone);
                logger.info("saved key value -> {} : {}", channelId, terminalPhone);
            }
            return;
        }

        String deviceCode = channelMap.get(channelId);
        if(StringUtils.isEmpty(deviceCode)){
            logger.warn("没有登录 -> {}", message.getAll());
            return;
        }
        message.getHeader().setTerminalPhone(deviceCode);
        toSendWrtMessage(message);
        List<String> replyCmd = Arrays.asList(Command.MSG_GPS_INTERVAL_RESP, Command.MSG_DEFENCE_RESP, Command.MSG_POWER_STOP_RESP,
                Command.MSG_POWER_RECOVER_RESP, Command.MSG_OVERSPEED_RESP, Command.MSG_HEART_INTERVAL_RESP, Command.MSG_IP_RESP);
        if(replyCmd.contains(message.getHeader().getMsgIdStr())){
            message.setSendBody(message.getAll().getBytes());
            MqSender.send(MqInitializer.replyChannel, message, MqInitializer.wrtCmdReplyQueueName);
        }
    }

    /**
     * 推送消息到MQ -> 沃瑞特C11
     * @param message
     * @throws Exception
     */
    public static void toSendWrtMessage(Message message) throws Exception{
        Channel channel = null;
        String queueName = "";
        int msgType = message.getHeader().getMsgType();
        if (msgType == DataType.GPS.getValue()){
            //定位
            gpsNum.incrementAndGet();
            CopyOnWriteArrayList<Channel> channels = MqInitializer.wrtGpsChannels;
            channel = channels.get(gpsNum.intValue() % channels.size());
            queueName = MqInitializer.wrtGpsQueuePrefix + (gpsNum.intValue() % MqInitializer.wrtGpsQueueCount + 1);
            String body = new String(message.getBody(), SysConst.DEFAULT_ENCODING);
            String sendMsg = ProtocolType.WRT.getValue() + message.getHeader().getTerminalPhone() + body;
            message.setSendBody(sendMsg.getBytes(Charset.forName(SysConst.DEFAULT_ENCODING)));
            logger.debug("[GPS] {} to queue -> {}", sendMsg, queueName);
        }else if (msgType == DataType.ALARM.getValue()){
            //报警
            alarmNum.incrementAndGet();
            CopyOnWriteArrayList<Channel> channels = MqInitializer.wrtAlarmChannels;
            channel = channels.get(alarmNum.intValue() % channels.size());
            queueName = MqInitializer.wrtAlarmQueuePrefix + (alarmNum.intValue() % MqInitializer.wrtAlarmQueueCount + 1);
            String body = new String(message.getBody(), SysConst.DEFAULT_ENCODING);
            String sendMsg = ProtocolType.WRT.getValue() + message.getHeader().getTerminalPhone() + body;
            message.setSendBody(sendMsg.getBytes(Charset.forName(SysConst.DEFAULT_ENCODING)));
            logger.debug("[ALARM] {} to queue -> {}", sendMsg, queueName);
        }else if (msgType == DataType.HEARTBEAT.getValue()){
            //心跳
            heartbeatNum.incrementAndGet();
            CopyOnWriteArrayList<Channel> channels = MqInitializer.wrtHeartbeatChannels;
            channel = channels.get(heartbeatNum.intValue() % channels.size());
            queueName = MqInitializer.wrtHeartbeatQueuePrefix + (heartbeatNum.intValue() % MqInitializer.wrtHeartbeatQueueCount + 1);
            String body = new String(message.getBody(), SysConst.DEFAULT_ENCODING);
            String sendMsg = ProtocolType.WRT.getValue() + message.getHeader().getTerminalPhone() + body;
            message.setSendBody(sendMsg.getBytes(Charset.forName(SysConst.DEFAULT_ENCODING)));
            logger.debug("[HEARTBEAT] {} to queue -> {}", sendMsg, queueName);
        }else if (msgType == DataType.CONTROLLER.getValue()){
            //控制器
            controllerNum.incrementAndGet();
            CopyOnWriteArrayList<Channel> channels = MqInitializer.wrtControllerChannels;
            channel = channels.get(controllerNum.intValue() % channels.size());
            queueName = MqInitializer.wrtControllerQueuePrefix + (controllerNum.intValue() % MqInitializer.wrtControllerQueueCount + 1);
            String body = new String(message.getBody(), SysConst.DEFAULT_ENCODING);
            String sendMsg = ProtocolType.WRT.getValue() + message.getHeader().getTerminalPhone() + body;
            message.setSendBody(sendMsg.getBytes(Charset.forName(SysConst.DEFAULT_ENCODING)));
            logger.debug("[CONTROLLER] {} to queue -> {}", sendMsg, queueName);
        }
        MqSender.send(channel, message, queueName);
    }

}
