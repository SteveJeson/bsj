package com.zdzc.collector.sender;

import com.zdzc.collector.common.jenum.ProtocolType;
import com.zdzc.collector.common.jfinal.Config;
import com.zdzc.collector.rabbitmq.init.MqInitializer;
import com.zdzc.collector.sender.consumer.MqConsumer;
import com.zdzc.collector.sender.handler.WrtMessageHandler;
import com.zdzc.collector.sender.server.NettyMqServer;
import com.zdzc.collector.tcpclient.core.ClientPoolManager;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @Author liuwei
 * @Description Netty服务程序入口类
 * @Date 2018/12/11 15:38
 */
public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args){
        try {
            Config.use("application.properties");
            String protocolType = Config.get("protocol.type");
            String remoteHost = Config.get("remote.server.host");
            int remotePort = Config.getInt("remote.server.port");
            int maxChannels = Config.getInt("client.channel.max");
            int serverPort = Config.getInt("netty.server.port");
            //初始化MQ配置
            MqInitializer.init();
            if (ProtocolType.BSJ.getValue().equals(Config.get("protocol.type"))){
                MqConsumer.initConsumer(MqInitializer.factory, MqInitializer.bsjCmdExchangeName, MqInitializer.bsjCmdQueueName);
            }
            //初始化转发客户端连接池
            ClientPoolManager.init(remoteHost, remotePort, maxChannels);
            if (StringUtils.equals(protocolType, ProtocolType.JT808.getValue())) {
                //初始化转发客户端连接池
                ClientPoolManager.init(remoteHost, remotePort, maxChannels);
            }
            //启动Netty TCP服务
            NettyMqServer server = new NettyMqServer();
            server.doStart(serverPort);

            if (StringUtils.equals(protocolType, ProtocolType.WRT.getValue())) {
                WrtMessageHandler.consume();
            }
        } catch (Exception e){
            logger.error(e.getMessage());
        }
    }
}
