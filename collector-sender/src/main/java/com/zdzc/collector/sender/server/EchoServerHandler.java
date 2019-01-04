package com.zdzc.collector.sender.server;

import ch.qos.logback.core.encoder.ByteArrayUtil;
import com.zdzc.collector.common.coder.ToJtMessageDecoder;
import com.zdzc.collector.common.jenum.ProtocolSign;
import com.zdzc.collector.common.jenum.ProtocolType;
import com.zdzc.collector.common.jfinal.Config;
import com.zdzc.collector.common.packet.Message;
import com.zdzc.collector.common.utils.ByteUtil;
import com.zdzc.collector.sender.coder.ToWrtMessageDecoder;
import com.zdzc.collector.sender.handler.JtMessageHandler;
import com.zdzc.collector.sender.handler.WrtMessageHandler;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @Author liuwei
 * @Description TCP服务端处理类
 * @Date 2018/12/11 15:28
 */
public class EchoServerHandler extends ChannelInboundHandlerAdapter {

    private static final Logger logger = LoggerFactory.getLogger(EchoServerHandler.class);

    public EchoServerHandler() {

    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        String protocolType =  Config.get("protocol.type");
        Message message;
        if (StringUtils.equals(protocolType, ProtocolType.WRT.getValue())) {
            String dataStr = (String)msg;
            if(dataStr.indexOf(ProtocolSign.WRT_BEGINMARK.getValue()) != 0){
                return;
            }
            String data = dataStr + ProtocolSign.WRT_ENDMARK.getValue();
            logger.debug("source data -> {}", data);
            message = ToWrtMessageDecoder.decode(data);
        } else if (StringUtils.equals(protocolType, ProtocolType.JT808.getValue())) {
            byte[] data = (byte[])msg;
            String hex = ByteArrayUtil.toHexString(data);
            if(data.length == 0 || StringUtils.equals(hex.toUpperCase(), ProtocolSign.JT808_BEGINMARK.getValue())){
                return;
            }
            byte[] mark = ByteArrayUtil.hexStringToByteArray(ProtocolSign.JT808_BEGINMARK.getValue());
            byte[] newData = ByteUtil.bytesMerge(mark, data);
            logger.debug("source data -> {}", ByteArrayUtil.toHexString(newData));
            message = ToJtMessageDecoder.decode(newData);
        } else {
            message = (Message) msg;
        }

        try {
            if(StringUtils.equals(ProtocolType.JT808.getValue(), message.getHeader().getProtocolType())){
                //808
                JtMessageHandler.handler(ctx, message);
            }else if(StringUtils.equals(ProtocolType.WRT.getValue(), message.getHeader().getProtocolType())){
                //wrt
                WrtMessageHandler.handler(ctx, message);
            }else if(StringUtils.equals(ProtocolType.BSJ.getValue(), message.getHeader().getProtocolType())){
                //博实结
//                BsjMessageHandler.handler(ctx, message);
            }

        }catch (Exception e){
            logger.error(e.getMessage());
        }
        finally {
            ReferenceCountUtil.release(msg);
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        // A closed channel will be removed from ChannelGroup automatically
        ChannelGroups.add(ctx.channel());
        System.out.println("A new client connected -> " + ctx.channel().id().toString() + ", " + ChannelGroups.size());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        String channelId = ctx.channel().id().toString();
        System.out.println("A client disconnected -> " + channelId + ", " + ChannelGroups.size());
        String protocolType = Config.get("protocol.type");
        if (StringUtils.equals(protocolType, ProtocolType.WRT.getValue())) {
            String value = WrtMessageHandler.channelMap.get(channelId).toString();
            if(StringUtils.isNotEmpty(value)){
                WrtMessageHandler.channelMap.remove(channelId, value);
                WrtMessageHandler.channelMap.remove(value, ctx.channel());
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
            throws Exception {
        logger.warn(cause.toString());
        Channel channel = ctx.channel();
        if(channel.isActive()){
            ctx.close();
        }
    }

}