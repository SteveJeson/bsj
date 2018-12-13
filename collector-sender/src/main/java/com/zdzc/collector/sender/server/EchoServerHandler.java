package com.zdzc.collector.sender.server;

import com.zdzc.collector.common.jenum.ProtocolType;
import com.zdzc.collector.common.packet.Message;
import com.zdzc.collector.sender.handler.BsjMessageHandler;
import com.zdzc.collector.sender.handler.JtMessageHandler;
import com.zdzc.collector.sender.handler.WrtMessageHandler;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.concurrent.GlobalEventExecutor;
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

    static ChannelGroup channels = new DefaultChannelGroup(
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
        channels.add(ctx.channel());
        System.out.println("A new client connected -> " + ctx.channel().id().toString() + ", " + channels.size());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        Channel c = channels.find(ctx.channel().id());
        String channelId = ctx.channel().id().toString();
        System.out.println("A client disconnected -> " + channelId + ", " + channels.size());
        String value = WrtMessageHandler.channelMap.get(channelId);
        if(StringUtils.isNotEmpty(value)){
            WrtMessageHandler.channelMap.remove(channelId, value);
            WrtMessageHandler.channelMap.remove(value, channelId);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
            throws Exception {
        logger.warn(cause.getMessage());
    }

}