package com.zdzc.collector.tcpclient.core;

import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.pool.ChannelPoolHandler;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.bytes.ByteArrayDecoder;
import io.netty.handler.codec.bytes.ByteArrayEncoder;

/**
 * @Author liuwei
 * @Description TCP客户端连接池处理类
 * @Date 2018/12/11 15:11
 */
public class ClientChannelPoolHandler implements ChannelPoolHandler {
    @Override
    public void channelReleased(Channel channel) throws Exception {
        // 刷新管道里的数据
        //flush掉所有写回的数据
        channel.writeAndFlush(Unpooled.EMPTY_BUFFER);
    }

    @Override
    public void channelAcquired(Channel channel) throws Exception {

    }

    @Override
    public void channelCreated(Channel channel) throws Exception {
        NioSocketChannel ch = (NioSocketChannel) channel;

        // 客户端逻辑处理   ClientHandler这个也是咱们自己编写的，继承ChannelInboundHandlerAdapter，实现你自己的逻辑
        ch.pipeline().addLast(new ClientHandler());
        //对 String 对象自动编码,属于出站站处理器
        ch.pipeline().addLast(new ByteArrayEncoder());
        ch.pipeline().addLast(new ByteArrayDecoder());
    }
}
