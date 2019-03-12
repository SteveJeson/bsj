package com.zdzc.collector.sender.handler;

import com.rabbitmq.client.Channel;
import com.zdzc.collector.common.coder.MsgDecoder;
import com.zdzc.collector.common.jconst.Command;
import com.zdzc.collector.common.jconst.SysConst;
import com.zdzc.collector.common.jenum.ProtocolSign;
import com.zdzc.collector.common.jenum.ProtocolType;
import com.zdzc.collector.common.packet.Message;
import com.zdzc.collector.common.utils.ByteUtil;
import com.zdzc.collector.common.utils.CrcItu16;
import com.zdzc.collector.rabbitmq.core.MqSender;
import com.zdzc.collector.rabbitmq.init.MqInitializer;
import com.zdzc.collector.sender.coder.ToBsjMessageDecoder;
import com.zdzc.collector.sender.coder.ToWrtMessageDecoder;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.internal.StringUtil;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Administrator on 2018/12/11 0011.
 */
public class BsjMessageHandler extends ChannelInboundHandlerAdapter{

    private static final Logger logger = LoggerFactory.getLogger(BsjMessageHandler.class);

    public static ConcurrentHashMap<String, Object> channelMap = new ConcurrentHashMap<>();

    public static AtomicInteger count = new AtomicInteger(0);

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        byte[] arr = (byte[]) msg;
        String hexStr = StringUtil.toHexStringPadded(arr).toUpperCase();
        logger.info("source: " + hexStr);
        String channelId = ctx.channel().id().toString();
        if (hexStr != null && hexStr.startsWith(ProtocolSign.BSJ_BEGINMARK.getValue())
                && hexStr.endsWith(ProtocolSign.BSJ_ENDMARK.getValue())
                && !hexStr.contains(ProtocolSign.BSJ_ENDMARK.getValue() + ProtocolSign.BSJ_BEGINMARK.getValue())){
            //非粘包信息处理
            dealMessage(ctx, hexStr, arr, channelId);
        } else if (hexStr.contains(ProtocolSign.BSJ_ENDMARK.getValue() + ProtocolSign.BSJ_BEGINMARK.getValue())){
            //粘包信息处理
//            logger.info("handle stick message -> {}", hexStr);
            List<String> list = MsgDecoder.dealPackageSplicing(hexStr, ProtocolSign.BSJ_BEGINMARK.getValue(), ProtocolSign.BSJ_ENDMARK.getValue());
            for (String data : list){
                dealMessage(ctx, data, StringUtil.decodeHexDump(data), channelId);
            }
        }
//        ctx.writeAndFlush(Unpooled.copiedBuffer(StringUtil.decodeHexDump("787805010001D9DC0D0A")));
//        ctx.writeAndFlush(Unpooled.copiedBuffer("787805010001D9DC0D0A".getBytes()));
        logger.debug("connections num: " + channelMap.size() / 2);
        ReferenceCountUtil.release(msg);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        String channelId = ctx.channel().id().toString();
        String deviceCode = (String)channelMap.get(channelId);
        logger.info("a connection disConnected --- channelId: " +  channelId + " deviceCode: " + deviceCode);
        channelMap.remove(channelId);
        channelMap.remove(deviceCode);
    }

    //    public static void handler(ChannelHandlerContext ctx, Message message) {
////        Message message = (Message)msg;
//        String channelId = ctx.channel().id().toString();
//        System.out.println("receiver: " + channelId);
//        if (message != null && !message.getStick()){
//            if (message.getReplyBody() != null){
//                ctx.writeAndFlush(Unpooled.copiedBuffer(message.getReplyBody()));
//                System.out.println("sender: " + channelId);
//            }
//            //对于新登录的设备，服务器会维护两个channelId, 设备id的对应关系表
//            if (StringUtils.equals(Command.BSJ_MSG_LOGIN, message.getHeader().getMsgIdStr())){
//                String deviceCode = message.getHeader().getTerminalPhone();
//                if(!channelMap.containsKey(deviceCode)){
//                    channelMap.put(deviceCode, channelId);
//                    logger.info("saved key value -> {} : {}", deviceCode, channelId);
//                }
//                if(!channelMap.containsKey(channelId)){
//                    channelMap.put(channelId, deviceCode);
//                    logger.info("saved key value -> {} : {}", channelId, deviceCode);
//                }
//            }
//
//            String deviceCode = channelMap.get(channelId);
//            if(StringUtils.isEmpty(deviceCode)){
//                logger.warn("找不到所属设备号 -> {}", message.getAll());
//                return;
//            }
//            message.getHeader().setTerminalPhone(deviceCode);
//            sendMsgToMq(message);
//            System.out.println("channelId: " + channelId + " count: " + count.getAndIncrement());
//        } else {
//            logger.info("handle stick message -> {}", message.getAll());
//            List<String> list = MsgDecoder.dealPackageSplicing(message.getAll(), ProtocolSign.BSJ_BEGINMARK.getValue(), ProtocolSign.BSJ_ENDMARK.getValue());
//            for (String data : list){
//                Message msg1 = ToBsjMessageDecoder.decodeMessage(StringUtil.decodeHexDump(data));
//                handler(ctx, msg1);
//            }
//        }
//    }

//    public static void sendMsgToMq(Message message){
//        Channel channel = null;
//        String queueName = null;
//        if (message != null && message.getHeader() != null){
//            if (Command.BSJ_MSG_LOGIN.equals(message.getHeader().getMsgIdStr())){
//                //博实结登录信息
//                channel = MqInitializer.bsjLoginChannel;
//                queueName = MqInitializer.bsjLoginQueueName;
//                String msg = ProtocolType.BSJ.getValue() + message.getHeader().getTerminalPhone() + message.getAll().substring(4, message.getAll().length() - 4);
//                message.setSendBody(msg.getBytes(Charset.forName(SysConst.DEFAULT_ENCODING)));
//            }else if (Command.BSJ_MSG_LOCATION.equals(message.getHeader().getMsgIdStr())){
//                //博实结定位信息
//                channel = MqInitializer.bsjLocationChannel;
//                queueName = MqInitializer.bsjLocationQueueName;
//                String msg = ProtocolType.BSJ.getValue() + message.getHeader().getTerminalPhone() + message.getAll().substring(4, message.getAll().length() - 4);
//                message.setSendBody(msg.getBytes(Charset.forName(SysConst.DEFAULT_ENCODING)));
//            }else if (Command.BSJ_MSG_ALARM.equals(message.getHeader().getMsgIdStr())){
//                //博实结报警信息
//                channel = MqInitializer.bsjAlarmChannel;
//                queueName = MqInitializer.bsjAlarmQueueName;
//                String msg = ProtocolType.BSJ.getValue() + message.getHeader().getTerminalPhone() + message.getAll().substring(4, message.getAll().length() - 4);
//                message.setSendBody(msg.getBytes(Charset.forName(SysConst.DEFAULT_ENCODING)));
//            }else if (Command.BSJ_MSG_HEARTBEAT.equals(message.getHeader().getMsgIdStr())){
//                //博实结心跳信息
//                channel = MqInitializer.bsjHeartbeatChannel;
//                queueName = MqInitializer.bsjHeartbeatQueueName;
//                String msg = ProtocolType.BSJ.getValue() + message.getHeader().getTerminalPhone() + message.getAll().substring(4, message.getAll().length() - 4);
//                message.setSendBody(msg.getBytes(Charset.forName(SysConst.DEFAULT_ENCODING)));
//            }
//        }
//        MqSender.send(channel, message, queueName);
//    }

    private void dealMessage(ChannelHandlerContext ctx, String message, byte[] data, String channelId) throws Exception{
        if (message != null && message.startsWith(ProtocolSign.BSJ_BEGINMARK.getValue())
                && message.endsWith(ProtocolSign.BSJ_ENDMARK.getValue())
                && !message.contains(ProtocolSign.BSJ_ENDMARK.getValue() + ProtocolSign.BSJ_BEGINMARK.getValue())){
            String protocolNum = message.substring(6, 8);
            byte[] body = Arrays.copyOfRange(data, 2, data.length - 4);
            String serverCheckCode = generateServerCheckCode(CrcItu16.CRC_16_X25(body).toUpperCase());
            String checkCode = message.substring(message.length() - 8, message.length() - 4).toUpperCase();
            String serialNum = message.substring(message.length() - 12, message.length() - 8);
            if (serverCheckCode.equals(checkCode)){
                if (Command.BSJ_MSG_LOGIN.equals(protocolNum)){
                    //对于新登录的设备，服务器会维护两个channelId, 设备id的对应关系表
                    String deviceCode = message.substring(8 ,24);
                    if(!channelMap.containsKey(deviceCode)){
                        channelMap.put(deviceCode, ctx.channel());
//                        logger.info("saved key value -> {} : {}", deviceCode, channelId);
                    }
                    if(!channelMap.containsKey(channelId)){
                        channelMap.put(channelId, deviceCode);
//                        logger.info("saved key value -> {} : {}", channelId, deviceCode);
                    }
                    sendToMq(message, channelId, MqInitializer.bsjLoginChannel, MqInitializer.bsjLoginQueueName);
                    String reply = generateReply(Command.BSJ_MSG_LOGIN, serialNum);
                    logger.info("login reply:" + reply);
                    ctx.writeAndFlush(Unpooled.buffer().writeBytes(StringUtil.decodeHexDump(reply)));
                } else if (Command.BSJ_MSG_HEARTBEAT.equals(protocolNum)){
                    //处理心跳信息
                    sendToMq(message, channelId, MqInitializer.bsjHeartbeatChannel, MqInitializer.bsjHeartbeatQueueName);
                    String reply = generateReply(Command.BSJ_MSG_HEARTBEAT, serialNum);
                    ctx.writeAndFlush(Unpooled.buffer().writeBytes(StringUtil.decodeHexDump(reply)));
                } else if (Command.BSJ_MSG_LOCATION.equals(protocolNum)){
                    //处理位置信息
                    sendToMq(message, channelId, MqInitializer.bsjLocationChannel, MqInitializer.bsjLocationQueueName);
                } else if (Command.BSJ_MSG_ALARM.equals(protocolNum)){
                    //处理报警信息
                    sendToMq(message, channelId, MqInitializer.bsjAlarmChannel, MqInitializer.bsjAlarmQueueName);
                    String reply = generateReply(Command.BSJ_MSG_ALARM, serialNum);
                    ctx.writeAndFlush(Unpooled.buffer().writeBytes(StringUtil.decodeHexDump(reply)));
                } else if (Command.BSJ_MSG_REPLY.equals(protocolNum)){
                    //处理下发指令回复的信息
                    String deviceCode = (String)channelMap.get(channelId);
                    message = deviceCode + message;
                    Message msg = new Message();
                    msg.setSendBody(StringUtil.decodeHexDump(message));
                    MqSender.send(MqInitializer.bsjReplyChannel, msg, MqInitializer.bsjCmdReplyQueueName);
//                    sendToMq(message, channelId, MqInitializer.bsjReplyChannel, MqInitializer.bsjCmdReplyQueueName);
                }
            }
        }
    }

    private void sendToMq(String message, String channelId, Channel mqChannel, String queueName){
        String deviceCode = (String)channelMap.get(channelId);
        String mqMessage = ProtocolType.BSJ.getValue() + deviceCode + message.substring(4, message.length() - 4);
        Message msg = new Message();
        msg.setSendBody(StringUtil.decodeHexDump(mqMessage));
        MqSender.send(mqChannel, msg, queueName);
    }

    private String generateReply(String replyType, String serialNum){
        String returnBody = Command.BSJ_MSG_RETURN_LENGTH + replyType + serialNum;
        byte[] btBody = StringUtil.decodeHexDump(returnBody);
        String checkNum = CrcItu16.CRC_16_X25(btBody);
        String result = ProtocolSign.BSJ_BEGINMARK.getValue() + returnBody + checkNum + ProtocolSign.BSJ_ENDMARK.getValue();
        return result;
    }

    private String generateServerCheckCode(String checkCode){
        if (checkCode != null){
            if (checkCode.length() == 3){
                System.out.println(checkCode);
                checkCode = "0" + checkCode;
            }else if (checkCode.length() == 2){
                checkCode = "00" + checkCode;
            }else if (checkCode.length() == 1){
                checkCode = "000" + checkCode;
            }
            return checkCode;
        }
        return null;
    }
}
