package com.zdzc.collector.simulator;

import com.zdzc.collector.simulator.client.ChannelUtils;
import com.zdzc.collector.simulator.client.TcpClient;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;

import java.io.UnsupportedEncodingException;


public class Main {
    public static void main(String[] args) throws InterruptedException, UnsupportedEncodingException {
        String str = "IamChannel-";
        String deviceCode = "code";
        for (int i = 1;i <= 2;i++) {
            Channel channel = TcpClient.getChannel();
            String id = channel.id().toString();
            String body = str + i + "-" + id;
            String key = deviceCode + i;
            ChannelUtils.putCallback2DataMap(channel, key, id);
            channel.writeAndFlush(Unpooled.copiedBuffer(body.getBytes("utf-8")));
        }
    }
}
