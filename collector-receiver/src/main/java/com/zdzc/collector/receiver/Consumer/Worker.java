package com.zdzc.collector.receiver.Consumer;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DeliverCallback;
import com.zdzc.collector.receiver.coder.MsgDecoder;
import com.zdzc.collector.receiver.db.DbConnectionPool;
import com.zdzc.collector.receiver.entity.Protocol;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class Worker {

    private Channel channel;

    private String queueName;

    private AtomicInteger count = new AtomicInteger(0);

    public Worker (Channel channel, String queueName) throws IOException {
       this.channel = channel;
       this.queueName = queueName;
    }

    public void doWork() throws Exception {
        Connection connection = DbConnectionPool.getConnect();
        PreparedStatement pst = connection.prepareStatement("");

        List<Protocol> protocolList = new ArrayList<>();
        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            try {
                Protocol protocol = MsgDecoder.decode(delivery.getBody());
                //// TODO: 2018/12/25
                MessageConsumer.consume(protocol, count.incrementAndGet(), pst);
                protocolList.add(protocol);
                long remainCount = channel.messageCount(queueName);
                if (protocolList.size() >= 100 || remainCount <= 0) {
                    //批量处理
                    //do something

                    if (true) {
                        channel.basicAck(delivery.getEnvelope().getDeliveryTag(), true);
                    } else {
                        channel.basicNack(delivery.getEnvelope().getDeliveryTag(), true, true);
                    }

                    protocolList.clear();
                }


            } catch (SQLException e) {
                e.printStackTrace();
            }

        };
        channel.basicConsume(queueName, false, deliverCallback, consumerTag -> { });

    }

}
