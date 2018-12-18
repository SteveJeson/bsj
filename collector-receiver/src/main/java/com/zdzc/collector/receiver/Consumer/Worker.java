package com.zdzc.collector.receiver.Consumer;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DeliverCallback;

import java.io.IOException;

public class Worker implements Runnable {

    private Channel channel;

    private String queueName;

    public Worker (Channel channel, String queueName) throws IOException {
       this.channel = channel;
       this.queueName = queueName;
    }

    private void doWork() throws IOException {
        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), "UTF-8");

            try {
                System.out.println(queueName + " -> " + message);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    Thread.currentThread().interrupt();
                }
            } finally {
                channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
            }
        };
        channel.basicConsume(queueName, false, deliverCallback, consumerTag -> { });

    }

    @Override
    public void run() {
        try{
            doWork();
        }catch (Exception e) {
            e.printStackTrace();
        }
    }
}
