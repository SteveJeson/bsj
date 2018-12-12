package com.zdzc.collector.httpserver;

import com.zdzc.collector.common.jfinal.Config;
import com.zdzc.collector.httpserver.server.HttpServer;
import com.zdzc.collector.rabbitmq.init.MqInitializer;
import com.zdzc.collector.tcpclient.core.ClientPoolManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @Author liuwei
 * @Description 服务程序入口类
 * @Date 2018/12/11 15:32
 */
public class Main {

    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args)  {
        try{
            Config.use("application.properties");

            String remoteHost = Config.get("remote.server.host");
            int remotePort = Config.getInt("remote.server.port");
            int maxChannels = Config.getInt("client.channel.max");
            //初始化MQ配置
            MqInitializer.init();
            //初始化转发客户端连接池
            ClientPoolManager.init(remoteHost, remotePort, maxChannels);

            int port = Config.getInt("http.server.port");
            HttpServer.start(port);
        }catch (Exception e) {
            logger.error(e.getMessage());
        }
    }
}
