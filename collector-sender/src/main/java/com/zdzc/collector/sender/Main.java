package com.zdzc.collector.sender;

import com.zdzc.collector.common.jfinal.P;
import com.zdzc.collector.rabbitmq.init.MqInitializer;
import com.zdzc.collector.sender.server.NettyMqServer;
import com.zdzc.collector.tcpclient.core.ClientPoolManager;

public class Main {
    public static void main(String[] args){
        try {
            P.use("application.properties");
            String remoteHost = P.get("remote.server.host");
            int remotePort = P.getInt("remote.server.port");
            int maxChannels = P.getInt("client.channel.max");
            int serverPort = P.getInt("netty.server.port");
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
