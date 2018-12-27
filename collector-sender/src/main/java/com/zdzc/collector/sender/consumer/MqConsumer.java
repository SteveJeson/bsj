package com.zdzc.collector.sender.consumer;

import com.rabbitmq.client.*;
import com.rabbitmq.client.Channel;
import com.zdzc.collector.common.jconst.SysConst;
import com.zdzc.collector.sender.handler.BsjMessageHandler;
import io.netty.buffer.Unpooled;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * @Description TODO
 * @Author Administrator
 * @Date 2018/12/26 0026 13:37
 **/
public class MqConsumer {

    private static final Logger logger = LoggerFactory.getLogger(MqConsumer.class);

    /**
     * 监听消息队列，收到消息后解析并发给对应的终端
     * @param exchangeName
     * @param cmdQueueName
     */
    public static void initConsumer(ConnectionFactory factory, String exchangeName, String cmdQueueName) throws IOException, TimeoutException {
        Channel channel = factory.newConnection().createChannel();
        channel.exchangeDeclare(exchangeName, "fanout", true);
        channel.queueDeclare(cmdQueueName, true, false, false, null);
        channel.queueBind(cmdQueueName, exchangeName, "");
        Consumer consumer = new DefaultConsumer(channel){
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                super.handleDelivery(consumerTag, envelope, properties, body);
                String message = new String(SysConst.DEFAULT_ENCODING);
                String[] info = message.split(",");
                if (info.length > 1) {
                    io.netty.channel.Channel channel1 = (io.netty.channel.Channel) BsjMessageHandler.channelMap.get(info[0]);
                    if (channel1 != null){
                        channel1.writeAndFlush(Unpooled.buffer().writeBytes(info[1].getBytes()));
                    }else {
                        logger.error("未找到该设备号对应的通道：" + info[0]);
                    }
                }else {
                    logger.error("unknown message: " + message);
                }
            }
        };
        channel.basicConsume(cmdQueueName, true, consumer);
    }
}
