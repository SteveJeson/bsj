package com.zdzc.collector.httpserver.handler;

import com.zdzc.collector.common.packet.Message;
import com.zdzc.collector.rabbitmq.handler.MqMessageHandler;
import com.zdzc.collector.tcpclient.core.ClientPoolManager;

import java.util.List;

/**
 * @Author liuwei
 * @Description 消息处理类
 * @Date 2018/12/11 15:42
 */
public class MessageHandler {

    public static void handler(List<Message> messageList) {
        for (Message message : messageList) {
            MqMessageHandler.handler(message);
//            ClientPoolManager.send(message.getAll());
        }
    }

}
