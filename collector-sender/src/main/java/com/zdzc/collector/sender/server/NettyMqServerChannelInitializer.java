package com.zdzc.collector.sender.server;

import ch.qos.logback.core.encoder.ByteArrayUtil;
import com.zdzc.collector.common.jenum.ProtocolSign;
import com.zdzc.collector.common.jenum.ProtocolType;
import com.zdzc.collector.common.jfinal.Config;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.bytes.ByteArrayDecoder;
import io.netty.handler.codec.string.StringDecoder;
import org.apache.commons.lang.StringUtils;

/**
 * @Author liuwei
 * @Description Netty服务器通道初始化类
 * @Date 2018/12/11 15:45
 */
public class NettyMqServerChannelInitializer extends
        ChannelInitializer<SocketChannel> {

    public NettyMqServerChannelInitializer() {

    }

    @Override
    public void initChannel(SocketChannel ch) throws Exception {
        String protocolType = Config.get("protocol.type");
        // Reader ilde time 3 minutes
//        ch.pipeline().addLast(new IdleStateHandler(5 * 60, 0, 0));
        ch.pipeline().addLast(new HeartBeatHandler());
        if(StringUtils.equals(protocolType, ProtocolType.JT808.getValue())){
//            ch.pipeline().addLast(new JtProtocolDecoder());
            ByteBuf delimiter = Unpooled.copiedBuffer(ByteArrayUtil.hexStringToByteArray(ProtocolSign.JT808_BEGINMARK.getValue()));
            ch.pipeline().addLast(new DelimiterBasedFrameDecoder(2048, false, delimiter));
            ch.pipeline().addLast(new ByteArrayDecoder());
        } else if (StringUtils.equals(protocolType, ProtocolType.WRT.getValue())) {
            ByteBuf delimiter = Unpooled.copiedBuffer(ProtocolSign.WRT_ENDMARK.getValue().getBytes());
            ch.pipeline().addLast(new DelimiterBasedFrameDecoder(2048, delimiter));
            ch.pipeline().addLast(new StringDecoder());
        } else {
            ch.pipeline().addLast(new ToMessageDecoder());
        }
        ch.pipeline().addLast(new EchoServerHandler());
    }
}
