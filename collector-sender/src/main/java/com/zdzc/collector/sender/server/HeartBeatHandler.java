package com.zdzc.collector.sender.server;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @Author liuwei
 * @Description TCP服务端心跳处理类
 * @Date 2018/12/11 15:31 
 */
public class HeartBeatHandler extends ChannelInboundHandlerAdapter {
    private static final Logger log = LoggerFactory
            .getLogger(HeartBeatHandler.class);

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt)
            throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) evt;
            if (event.state() == IdleState.READER_IDLE) {
                // Read timeout
                // ctx.disconnect(); //Channel disconnect
                log.info("READER_IDLE: read timeout from "
                        + ctx.channel().remoteAddress());
            } else if (event.state() == IdleState.WRITER_IDLE) {
                log.info("--- Write Idle ---");
            } else if (event.state() == IdleState.ALL_IDLE) {
                log.info("--- All_IDLE ---");
            }
        }
    }
}
