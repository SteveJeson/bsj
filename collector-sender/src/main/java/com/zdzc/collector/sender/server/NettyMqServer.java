package com.zdzc.collector.sender.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @Author liuwei
 * @Description Netty服务器类
 * @Date 2018/12/11 15:44
 */
public class NettyMqServer {
    private static final Logger log = LoggerFactory
            .getLogger(NettyMqServer.class);

    private EventLoopGroup bossGroup;

    private EventLoopGroup workerGroup;

    private Channel serverChannel;

    public NettyMqServer() {
        bossGroup = new NioEventLoopGroup();
        workerGroup = new NioEventLoopGroup();
    }

    private void start(int port) {
        try {
            // start server
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new NettyMqServerChannelInitializer());
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
