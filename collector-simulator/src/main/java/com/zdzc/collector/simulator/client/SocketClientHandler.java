package com.zdzc.collector.simulator.client;

import com.zdzc.collector.simulator.util.ChannelUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class SocketClientHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        Channel channel = ctx.channel();

        ByteBuf buf = (ByteBuf)msg;
        byte[] b = new byte[buf.readableBytes()];
        buf.getBytes(buf.readerIndex(), b);
        String result = new String(b, "utf-8");
        System.out.println("receive server -> " + result);
        int seq = 1;

        //获取消息对应的callback
        SocketClient.CallbackService callbackService = ChannelUtils.<SocketClient.CallbackService>removeCallback(channel, seq);
        callbackService.receiveMessage(buf);
    }
}