package com.zdzc.collector.sender.server;

import com.zdzc.collector.common.jenum.ProtocolSign;
import com.zdzc.collector.common.jenum.ProtocolType;
import com.zdzc.collector.common.jfinal.Config;
import com.zdzc.collector.common.packet.Message;
import com.zdzc.collector.sender.coder.ToWrtMessageDecoder;
import com.zdzc.collector.sender.handler.BsjMessageHandler;
import com.zdzc.collector.sender.handler.JtMessageHandler;
import com.zdzc.collector.sender.handler.WrtMessageHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Author liuwei
 * @Description TCP服务端处理类
 * @Date 2018/12/11 15:28
 */
public class EchoServerHandler extends ChannelInboundHandlerAdapter {

    private static final Logger logger = LoggerFactory.getLogger(EchoServerHandler.class);

    static AtomicInteger count = new AtomicInteger(0);

    public EchoServerHandler() {

    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        String protocolType =  Config.get("protocol.type");
        Message message;
        if (StringUtils.equals(protocolType, ProtocolType.WRT.getValue())) {
            String dataStr = (String)msg;
            System.out.println("source data -> " + dataStr);
            if(dataStr.indexOf(ProtocolSign.WRT_BEGINMARK.getValue()) != 0){
                return;
            }
            message = ToWrtMessageDecoder.decode(dataStr + ProtocolSign.WRT_ENDMARK.getValue());
        } else {
            message = (Message)msg;
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
                BsjMessageHandler.handler(ctx, message);
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
//        channels.add(ctx.channel());
        System.out.println("A new client connected -> " + ctx.channel().id().toString() + ", " + count.incrementAndGet());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        String channelId = ctx.channel().id().toString();
        System.out.println("A client disconnected -> " + channelId + ", " + count.decrementAndGet());
        String value = WrtMessageHandler.channelMap.get(channelId);
        if(StringUtils.isNotEmpty(value)){
            WrtMessageHandler.channelMap.remove(channelId, value);
            WrtMessageHandler.channelMap.remove(value, channelId);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
            throws Exception {
        logger.warn(cause.toString());
    }

}