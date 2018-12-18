package com.zdzc.collector.sender.server;

import com.zdzc.collector.common.jenum.ProtocolSign;
import com.zdzc.collector.common.jenum.ProtocolType;
import com.zdzc.collector.common.jfinal.Config;
import com.zdzc.collector.sender.coder.JtProtocolDecoder;
import com.zdzc.collector.sender.handler.BsjMessageHandler;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.bytes.ByteArrayDecoder;
import io.netty.handler.codec.bytes.ByteArrayEncoder;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.timeout.IdleStateHandler;
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
            ch.pipeline().addLast(new JtProtocolDecoder());
            ch.pipeline().addLast(new EchoServerHandler());
        } else if (StringUtils.equals(protocolType, ProtocolType.WRT.getValue())) {
            ByteBuf delimiter = Unpooled.copiedBuffer(ProtocolSign.WRT_ENDMARK.getValue().getBytes());
            ch.pipeline().addLast(new DelimiterBasedFrameDecoder(2048, delimiter));
            ch.pipeline().addLast(new StringDecoder());
            ch.pipeline().addLast(new EchoServerHandler());
        } else if (StringUtils.equals(protocolType, ProtocolType.BSJ.getValue())){
//            ch.pipeline().addLast(new ToMessageDecoder());
//            ch.pipeline().addLast(new ObjectEncoder());
            ch.pipeline().addLast(new ByteArrayDecoder());
            ch.pipeline().addLast(new BsjMessageHandler());
        } else {
            ch.pipeline().addLast(new ToMessageDecoder());
        }
//        ch.pipeline().addLast(new EchoServerHandler());

    }
}
