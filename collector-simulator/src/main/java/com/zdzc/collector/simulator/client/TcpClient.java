package com.zdzc.collector.simulator.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.bytes.ByteArrayDecoder;
import io.netty.handler.codec.bytes.ByteArrayEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.Attribute;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TcpClient {

    public static Channel getChannel () throws InterruptedException {
        EventLoopGroup eventLoopGroup = new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(eventLoopGroup)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.SO_KEEPALIVE, Boolean.TRUE)
                .option(ChannelOption.TCP_NODELAY, Boolean.TRUE)
                .handler(new LoggingHandler(LogLevel.INFO))
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ChannelPipeline pipeline = ch.pipeline();
//                        pipeline.addLast(new SelfDefineEncodeHandler());
                        pipeline.addLast(new TcpClientHandler());
                        pipeline.addLast(new ByteArrayEncoder());
                        pipeline.addLast(new ByteArrayDecoder());
                    }
                });

        ChannelFuture channelFuture = bootstrap.connect("192.168.1.161", 8899);
        Channel channel = channelFuture.sync().channel();

        //为刚刚创建的channel，初始化channel属性
        Attribute<Map<String,Object>> attribute = channel.attr(ChannelUtils.ATTR);
        ConcurrentHashMap<String, Object> dataMap = new ConcurrentHashMap<>();
        attribute.set(dataMap);

        return channel;
    }
}
