package com.zdzc.collector.sender.handler;

import com.zdzc.collector.common.packet.Message;
import com.zdzc.collector.rabbitmq.handler.MqMessageHandler;
import com.zdzc.collector.tcpclient.core.ClientPoolManager;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;

/**
 * @Author liuwei
 * @Description 部标808协议数据处理类
 * @Date 2018/12/11 15:35
 */
public class JtMessageHandler {

    public static void handler(ChannelHandlerContext ctx, Message message){
        //给客户端发送应答消息
        if(message.getReplyBody() != null){
            ctx.writeAndFlush(Unpooled.copiedBuffer(message.getReplyBody()));
        }
        if(message.getExtReplyBody() != null){
            ctx.writeAndFlush(Unpooled.copiedBuffer(message.getExtReplyBody()));
        }
        //将收到的定位、报警、心跳消息推送至Rabbitmq
        MqMessageHandler.handler(message);
        //转发到车管通服务器
        ClientPoolManager.send(message.getAll());
    }

}
