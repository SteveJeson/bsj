package com.zdzc.collector.simulator.server;

import com.zdzc.collector.simulator.client.ChannelUtils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.util.concurrent.atomic.AtomicInteger;

public class TcpServerHandler extends ChannelInboundHandlerAdapter {
    private static AtomicInteger count = new AtomicInteger(0);

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        Channel channel = ctx.channel();
        ByteBuf buf = (ByteBuf)msg;

        byte[] result = new byte[buf.readableBytes()];
        buf.getBytes(buf.readerIndex(), result);
        String body = new String(result, "utf-8");
        System.out.println("receive from -> "+ channel.id().toString() + " -> " + body);

        //6、将数据写回到客户端
        String channelId = ctx.channel().id().toString();
        channel.writeAndFlush(Unpooled.copiedBuffer(channelId.getBytes("utf-8")));
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("A new client connected -> " + ctx.channel().id().toString() + ", " + count.incrementAndGet());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("A client disconnected -> " + ctx.channel().id().toString() + ", " + count.decrementAndGet());
    }

}