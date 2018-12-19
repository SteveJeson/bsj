package com.zdzc.collector.simulator.pool;


import com.zdzc.collector.simulator.client.SelfDefineEncodeHandler;
import com.zdzc.collector.simulator.client.SocketClientHandler;
import com.zdzc.collector.simulator.util.ChannelUtils;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.Attribute;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

public class NettyChannelPool {
    private Channel[] channels;
    private Object [] locks;
    private static final int MAX_CHANNEL_COUNT = 4;

    public NettyChannelPool() {
        this.channels = new Channel[MAX_CHANNEL_COUNT];
        this.locks = new Object[MAX_CHANNEL_COUNT];
        for (int i = 0; i < MAX_CHANNEL_COUNT; i++) {
            this.locks[i] = new Object();
        }
    }

    /**
     * 同步获取netty channel
     */
    public Channel syncGetChannel() throws InterruptedException {
        //产生一个随机数,随机的从数组中获取channel
        int index = new Random().nextInt(MAX_CHANNEL_COUNT);
        Channel channel = channels[index];
        //如果能获取到,直接返回
        if (channel != null && channel.isActive()) {
            return channel;
        }

        synchronized (locks[index]) {
            channel = channels[index];
            //这里必须再次做判断,当锁被释放后，之前等待的线程已经可以直接拿到结果了。
            if (channel != null && channel.isActive()) {
                return channel;
            }

            //开始跟服务端交互，获取channel
            channel = connectToServer();

            channels[index] = channel;
        }

        return channel;
    }

    private Channel connectToServer() throws InterruptedException {
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
                        pipeline.addLast(new SelfDefineEncodeHandler());
                        pipeline.addLast(new SocketClientHandler());
                    }
                });

        ChannelFuture channelFuture = bootstrap.connect("192.168.1.161", 8899);
        Channel channel = channelFuture.sync().channel();

        //为刚刚创建的channel，初始化channel属性
        Attribute<Map<Integer,Object>> attribute = channel.attr(ChannelUtils.DATA_MAP_ATTRIBUTEKEY);
        ConcurrentHashMap<Integer, Object> dataMap = new ConcurrentHashMap<>();
        attribute.set(dataMap);
        return channel;
    }
}