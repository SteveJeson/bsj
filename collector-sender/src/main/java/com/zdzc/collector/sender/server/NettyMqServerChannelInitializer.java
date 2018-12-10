package com.zdzc.collector.sender.server;

import com.zdzc.collector.rabbitmq.core.MqSender;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.timeout.IdleStateHandler;

public class NettyMqServerChannelInitializer extends
        ChannelInitializer<SocketChannel> {

    private MqSender mqSender;

    public NettyMqServerChannelInitializer(MqSender mqSender) {
        this.mqSender = mqSender;
    }

    @Override
    public void initChannel(SocketChannel ch) throws Exception {
        // Reader ilde time 3 minutes
        ch.pipeline().addLast(new IdleStateHandler(5 * 60, 0, 0));
        ch.pipeline().addLast(new HeartBeatHandler());
        ch.pipeline().addLast(new ToMessageDecoder());
        ch.pipeline().addLast(new EchoServerHandler(mqSender));
    }
}
