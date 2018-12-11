package com.zdzc.collector.sender.server;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.timeout.IdleStateHandler;

/**
 * @Author liuwei
 * @Description Netty服务器通道初始化类
 * @Date 2018/12/11 15:45
 */
public class NettyMqServerChannelInitializer extends
        ChannelInitializer<SocketChannel> {

    public NettyMqServerChannelInitializer() {

    }

    @Override
    public void initChannel(SocketChannel ch) throws Exception {
        // Reader ilde time 3 minutes
        ch.pipeline().addLast(new IdleStateHandler(5 * 60, 0, 0));
        ch.pipeline().addLast(new HeartBeatHandler());
        ch.pipeline().addLast(new ToMessageDecoder());
        ch.pipeline().addLast(new EchoServerHandler());
    }
}
