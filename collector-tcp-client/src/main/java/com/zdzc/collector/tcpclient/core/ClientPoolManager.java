package com.zdzc.collector.tcpclient.core;

import ch.qos.logback.core.encoder.ByteArrayUtil;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.pool.FixedChannelPool;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.net.InetSocketAddress;
import java.util.concurrent.ExecutionException;

/**
 * @Author liuwei
 * @Description TCP客户端连接池管理类
 * @Date 2018/12/11 15:13
 */
public class ClientPoolManager {

    public static FixedChannelPool channelPool;

    public static void init(String remoteHost, int remotePort, int maxChannel) {

        EventLoopGroup group = new NioEventLoopGroup(1);

        Bootstrap bootstrap = new Bootstrap();

        // 连接池每次初始化一个连接的时候都会根据这个值去连接服务器
        InetSocketAddress remoteAddress = InetSocketAddress.createUnresolved(remoteHost, remotePort);
        bootstrap.group(group).channel(NioSocketChannel.class).option(ChannelOption.TCP_NODELAY, true)
                .remoteAddress(remoteAddress);

        // 初始化连接池
        channelPool = new FixedChannelPool(bootstrap, new ClientChannelPoolHandler(), maxChannel);
        System.out.println("Connected remote TCP server -> " + remoteHost + ":" + remotePort);
    }

    public static void send(String message){
        Channel channel = null;
        try {
            channel = channelPool.acquire().sync().get();
            channel.write(ByteArrayUtil.hexStringToByteArray(message));
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } finally {
            channelPool.release(channel);
        }

    }
}
