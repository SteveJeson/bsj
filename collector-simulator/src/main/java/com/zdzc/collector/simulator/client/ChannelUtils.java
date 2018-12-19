package com.zdzc.collector.simulator.client;

import io.netty.channel.Channel;
import io.netty.util.AttributeKey;

import java.util.Map;

public class ChannelUtils {
    public static final AttributeKey<Map<String, Object>> ATTR = AttributeKey.valueOf("dataMap");
    public static <T> void putCallback2DataMap(Channel channel, String deviceCode, T callback) {
        channel.attr(ATTR).get().put(deviceCode, callback);
    }

    public static <T> T removeCallback(Channel channel, String deviceCode) {
        return (T) channel.attr(ATTR).get().remove(deviceCode);
    }
}