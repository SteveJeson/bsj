package com.zdzc.collector.sender.server;

import com.zdzc.collector.common.jenum.ProtocolType;
import com.zdzc.collector.common.packet.Message;
import com.zdzc.collector.sender.handler.JtMessageHandler;
import com.zdzc.collector.sender.handler.WrtMessageHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.concurrent.GlobalEventExecutor;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EchoServerHandler extends ChannelInboundHandlerAdapter {

    private static final Logger logger = LoggerFactory.getLogger(EchoServerHandler.class);

    static final ChannelGroup channels = new DefaultChannelGroup(
            GlobalEventExecutor.INSTANCE);

    public EchoServerHandler() {

    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        try {
            Message message = (Message) (msg);
            if(StringUtils.equals(ProtocolType.JT808.getValue(), message.getHeader().getProtocolType())){
                //808
                JtMessageHandler.handler(ctx, message);
            }else if(StringUtils.equals(ProtocolType.WRT.getValue(), message.getHeader().getProtocolType())){
                //wrt
                try {
                    WrtMessageHandler.handler(ctx, message);
                } catch (Exception e) {
                    logger.error(e.getMessage());
                }
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
        channels.add(ctx.channel());
        System.out.println("clients num ==> "+ channels.size());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        logger.warn("Disconnected client -> " + ctx.channel().remoteAddress());
        System.out.println("clients num ==> " + channels.size());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
            throws Exception {
//        ctx.close();
        logger.warn(cause.getMessage());
    }

}
