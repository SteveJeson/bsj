package com.zdzc.simulator;

import ch.qos.logback.core.encoder.ByteArrayUtil;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.pool.FixedChannelPool;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.bytes.ByteArrayDecoder;
import io.netty.handler.codec.bytes.ByteArrayEncoder;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

import java.net.InetSocketAddress;
import java.util.concurrent.ExecutionException;

/**
 * @Author liuwei
 * @Description TCP客户端连接池管理类
 * @Date 2018/12/11 15:13
 */
public class SimulatorClientPoolManager {

    public static FixedChannelPool channelPool;

    public static void init(String remoteHost, int remotePort, int maxChannel) throws Exception {

        EventLoopGroup group = new NioEventLoopGroup(1);

        Bootstrap bootstrap = new Bootstrap();

        // 连接池每次初始化一个连接的时候都会根据这个值去连接服务器
        bootstrap = bootstrap.group(group).channel(NioSocketChannel.class).option(ChannelOption.TCP_NODELAY, true)
                .handler(new ChannelInitializer<Channel>() {
                    @Override
                    protected void initChannel(Channel channel) throws Exception {
                        NioSocketChannel ch = (NioSocketChannel) channel;
                        // 客户端逻辑处理   ClientHandler这个也是咱们自己编写的，继承ChannelInboundHandlerAdapter，实现你自己的逻辑
                        ch.pipeline().addLast(new SimulatorClientHandler());
                        //对 String 对象自动编码,属于出站站处理器
                        ch.pipeline().addLast(new ByteArrayEncoder());
                    }
                });
        for (int i = 0; i < maxChannel; i++) {
            bootstrap.connect(remoteHost, remotePort).sync();
            Thread.sleep(100);
        }
    }

}
