package com.zdzc.collector.sender.server;

import com.zdzc.collector.common.jfinal.P;
import com.zdzc.collector.rabbitmq.core.MqSender;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NettyMqServer {
    private static final Logger log = LoggerFactory
            .getLogger(NettyMqServer.class);

    private EventLoopGroup bossGroup;

    private EventLoopGroup workerGroup;

    private Channel serverChannel;

    private MqSender mqSender;

    public NettyMqServer() {
        bossGroup = new NioEventLoopGroup();
        workerGroup = new NioEventLoopGroup();
        mqSender = new MqSender();
    }

    private void start(int port) {
        try {
            // start server
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new NettyMqServerChannelInitializer(mqSender));
            b.option(ChannelOption.SO_BACKLOG, 128);
            b.childOption(ChannelOption.SO_KEEPALIVE, false);
            b.childOption(ChannelOption.TCP_NODELAY, true);
            b.childOption(ChannelOption.SO_REUSEADDR, true);
            ChannelFuture f = b.bind(port).sync();
            serverChannel = f.channel();

            // start mq listener
//            startMqListener();

            log.info("Netty Server start ok，waiting for clients on port " + port);
        } catch (Exception ex) {
            log.error("Server start error: " + ex.getMessage());
            stop();
        }
    }

    /*private void startMqListener() {
        // Not familiar with Spring AMQP, don't know how to Manually ack to the
        // MQ server if one message is consumed.
        // Problem: If ack-mode is Automatic, The message will be lost if you
        // want some control
        *//*
         * @SuppressWarnings("resource") ApplicationContext applicationContext =
         * new FileSystemXmlApplicationContext( "classpath:rmqConfig.xml");
         * MqListener mqListener = (MqListener)
         * applicationContext.getBean("messageListener");
         *//*

        // Another way to start a MQ listener, just use the rabbit mq java
        // client API
        // Manually ack to MQ server if one message is consumed.
        MqReceiver mqReceiver = new MqReceiver();
        mqReceiver.start();
    }*/

    private void stop() {
        if (serverChannel != null) {
            serverChannel.close();
        }
        if (workerGroup != null) {
            workerGroup.shutdownGracefully();
        }
        if (bossGroup != null) {
            bossGroup.shutdownGracefully();
        }

        log.info("Server is shut down");
    }

    public void doStart(int port) {
        try {
            start(port);
        } catch (Exception e) {
            log.error("Server start error: " + e.getMessage());
        }
    }

    public void doStop() {
        stop();
    }
}
