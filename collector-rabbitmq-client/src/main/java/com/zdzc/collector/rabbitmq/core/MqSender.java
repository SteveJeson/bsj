package com.zdzc.collector.rabbitmq.core;

import ch.qos.logback.core.encoder.ByteArrayUtil;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.MessageProperties;
import com.zdzc.collector.common.jenum.DataType;
import com.zdzc.collector.common.jenum.ProtocolType;
import com.zdzc.collector.common.jfinal.Config;
import com.zdzc.collector.common.packet.Message;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * @Author liuwei
 * @Description MQ消息发送类
 * @Date 2018/12/11 15:43
 */
public class MqSender {

    private static final Logger logger = LoggerFactory.getLogger(MqSender.class);

    public static void send(Channel channel, Message message, String queueName){
        try {
            String type = Config.get("protocol.type");
            if (StringUtils.equals(type, ProtocolType.JT808.getValue()) &&
                    (message.getHeader().getMsgType() != DataType.HEARTBEAT.getValue())) {
                String all = message.getAll();
                String body = ByteArrayUtil.toHexString(message.getBody());
                if (!all.contains(body)) {
                    logger.error("body -> {} not belong to -> {}", body, all);
                }
            }
            channel.basicPublish("", queueName, MessageProperties.PERSISTENT_TEXT_PLAIN, message.getSendBody());
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
    }

}
