package com.zdzc.collector.rabbitmq.core;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.MessageProperties;
import com.zdzc.collector.common.packet.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class MqSender {

    private static final Logger logger = LoggerFactory.getLogger(MqSender.class);

    public MqSender(){

    }

    public void send(Channel channel, Message message, String queueName){
        try {
            channel.basicPublish("", queueName, MessageProperties.PERSISTENT_TEXT_PLAIN, message.getSendBody());
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
    }

}
