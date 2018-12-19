package com.zdzc.collector.receiver.Consumer;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DeliverCallback;
import com.zdzc.collector.receiver.coder.MsgDecoder;

import java.io.IOException;

public class Worker {

    private Channel channel;

    private String queueName;

    public Worker (Channel channel, String queueName) throws IOException {
       this.channel = channel;
       this.queueName = queueName;
    }

    public void doWork() throws IOException {
        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            try {
                MsgDecoder.decode(delivery.getBody());
            } finally {
                channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
            }
        };
        channel.basicConsume(queueName, false, deliverCallback, consumerTag -> { });

    }

}
