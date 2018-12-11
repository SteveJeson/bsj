package com.zdzc.collector.sender;

import com.zdzc.collector.common.jfinal.Config;
import com.zdzc.collector.rabbitmq.init.MqInitializer;
import com.zdzc.collector.sender.server.NettyMqServer;
import com.zdzc.collector.tcpclient.core.ClientPoolManager;

/**
 * @Author liuwei
 * @Description Netty服务器启动类
 * @Date 2018/12/11 15:38
 */
public class Main {
    public static void main(String[] args){
        try {
            Config.use("application.properties");
            String remoteHost = Config.get("remote.server.host");
            int remotePort = Config.getInt("remote.server.port");
            int maxChannels = Config.getInt("client.channel.max");
            int serverPort = Config.getInt("netty.server.port");
            //初始化MQ配置
            MqInitializer.init();
            //初始化转发客户端连接池
            ClientPoolManager.init(remoteHost, remotePort, maxChannels);
            //启动Netty TCP服务
            NettyMqServer server = new NettyMqServer();
            server.doStart(serverPort);
        } catch (Exception e){
            e.printStackTrace();
        }
    }
}
