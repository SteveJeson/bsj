package com.zdzc.collector.receiver.Consumer;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DeliverCallback;
import com.zdzc.collector.receiver.coder.MsgDecoder;
import com.zdzc.collector.receiver.entity.BsjProtocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class Worker implements Runnable {

    private Channel channel;

    private String queueName;

    private AtomicInteger count = new AtomicInteger(0);

    private static final Logger logger = LoggerFactory.getLogger(Worker.class);

    public Worker (Channel channel, String queueName) {
       this.channel = channel;
       this.queueName = queueName;
    }

    public void doWork() throws Exception {
        ConcurrentHashMap<String, List<BsjProtocol>> mapList = new ConcurrentHashMap<>();

        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            try {
                BsjProtocol protocol = MsgDecoder.decode(delivery.getBody());
//                long remainCount = channel.messageCount(queueName);

                Boolean flag = MessageConsumer.consume(protocol, count.incrementAndGet(), mapList);
                if (flag) {
                    System.out.println(Thread.currentThread().getName() + " ack -> " + count.intValue());
//                    logger.debug("{} {} ack -> {}", Thread.currentThread().getName(),  queueName, count.intValue());
                    channel.basicAck(delivery.getEnvelope().getDeliveryTag(), true);
                } else {
                    channel.basicNack(delivery.getEnvelope().getDeliveryTag(), true, true);
                }

            } catch (Exception e) {
                e.printStackTrace();
                channel.basicNack(delivery.getEnvelope().getDeliveryTag(), true, true);
            }

        };
        channel.basicConsume(queueName, false, deliverCallback, consumerTag -> { });

    }

    @Override
    public void run() {
        try {
            doWork();
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e.toString());
        }
    }
}
