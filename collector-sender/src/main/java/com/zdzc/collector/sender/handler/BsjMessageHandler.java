package com.zdzc.collector.sender.handler;

import com.rabbitmq.client.Channel;
import com.zdzc.collector.common.coder.MsgDecoder;
import com.zdzc.collector.common.jconst.Command;
import com.zdzc.collector.common.jconst.SysConst;
import com.zdzc.collector.common.jenum.ProtocolSign;
import com.zdzc.collector.common.jenum.ProtocolType;
import com.zdzc.collector.common.packet.Message;
import com.zdzc.collector.rabbitmq.core.MqSender;
import com.zdzc.collector.rabbitmq.init.MqInitializer;
import com.zdzc.collector.sender.coder.ToBsjMessageDecoder;
import com.zdzc.collector.sender.coder.ToWrtMessageDecoder;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.internal.StringUtil;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Administrator on 2018/12/11 0011.
 */
public class BsjMessageHandler {

    private static final Logger logger = LoggerFactory.getLogger(BsjMessageHandler.class);

    public static ConcurrentHashMap<String, String> channelMap = new ConcurrentHashMap<>();

    public static void handler(ChannelHandlerContext ctx, Message message){
        String channelId = ctx.channel().id().toString();
        if (message != null && !message.getStick()){
            if (message.getReplyBody() != null){
                ctx.writeAndFlush(Unpooled.copiedBuffer(message.getReplyBody()));
            }
            //对于新登录的设备，服务器会维护两个channelId, 设备id的对应关系表
            if (StringUtils.equals(Command.BSJ_MSG_LOGIN, message.getHeader().getMsgIdStr())){
                String deviceCode = message.getHeader().getTerminalPhone();
                if(!channelMap.containsKey(deviceCode)){
                    channelMap.put(deviceCode, channelId);
                    logger.info("saved key value -> {} : {}", deviceCode, channelId);
                }
                if(!channelMap.containsKey(channelId)){
                    channelMap.put(channelId, deviceCode);
                    logger.info("saved key value -> {} : {}", channelId, deviceCode);
                }
            }

            String deviceCode = channelMap.get(channelId);
            if(StringUtils.isEmpty(deviceCode)){
                logger.warn("找不到所属设备号 -> {}", message.getAll());
                return;
            }
            message.getHeader().setTerminalPhone(deviceCode);
            sendMsgToMq(message);
        } else {
            logger.info("handle stick message -> {}", message.getAll());
            List<String> list = MsgDecoder.dealPackageSplicing(message.getAll(), ProtocolSign.BSJ_BEGINMARK.getValue(), ProtocolSign.BSJ_ENDMARK.getValue());
            for (String data : list){
                Message msg = ToBsjMessageDecoder.decodeMessage(StringUtil.decodeHexDump(data));
                handler(ctx, msg);
            }
        }
    }

    public static void sendMsgToMq(Message message){
        Channel channel = null;
        String queueName = null;
        if (message != null && message.getHeader() != null){
            if (Command.BSJ_MSG_LOGIN.equals(message.getHeader().getMsgIdStr())){
                //博实结登录信息
                channel = MqInitializer.bsjLoginChannel;
                queueName = MqInitializer.bsjLoginQueueName;
                String msg = ProtocolType.BSJ.getValue() + message.getHeader().getTerminalPhone() + message.getAll().substring(4, message.getAll().length() - 4);
                message.setSendBody(msg.getBytes(Charset.forName(SysConst.DEFAULT_ENCODING)));
            }else if (Command.BSJ_MSG_LOCATION.equals(message.getHeader().getMsgIdStr())){
                //博实结定位信息
                channel = MqInitializer.bsjLocationChannel;
                queueName = MqInitializer.bsjLocationQueueName;
                String msg = ProtocolType.BSJ.getValue() + message.getHeader().getTerminalPhone() + message.getAll().substring(4, message.getAll().length() - 4);
                message.setSendBody(msg.getBytes(Charset.forName(SysConst.DEFAULT_ENCODING)));
            }else if (Command.BSJ_MSG_ALARM.equals(message.getHeader().getMsgIdStr())){
                //博实结报警信息
                channel = MqInitializer.bsjAlarmChannel;
                queueName = MqInitializer.bsjAlarmQueueName;
                String msg = ProtocolType.BSJ.getValue() + message.getHeader().getTerminalPhone() + message.getAll().substring(4, message.getAll().length() - 4);
                message.setSendBody(msg.getBytes(Charset.forName(SysConst.DEFAULT_ENCODING)));
            }else if (Command.BSJ_MSG_HEARTBEAT.equals(message.getHeader().getMsgIdStr())){
                //博实结心跳信息
                channel = MqInitializer.bsjHeartbeatChannel;
                queueName = MqInitializer.bsjHeartbeatQueueName;
                String msg = ProtocolType.BSJ.getValue() + message.getHeader().getTerminalPhone() + message.getAll().substring(4, message.getAll().length() - 4);
                message.setSendBody(msg.getBytes(Charset.forName(SysConst.DEFAULT_ENCODING)));
            }
        }
        MqSender.send(channel, message, queueName);
    }
}
