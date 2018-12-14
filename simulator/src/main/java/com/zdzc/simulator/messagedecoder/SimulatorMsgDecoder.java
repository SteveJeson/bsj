package com.zdzc.simulator.messagedecoder;

import com.zdzc.collector.common.jenum.ProtocolSign;
import com.zdzc.collector.common.utils.ByteUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @Description TODO
 * @Author Administrator
 * @Date 2018/12/14 0014 11:40
 **/
public class SimulatorMsgDecoder extends MessageToMessageDecoder{

    private static final Logger logger = LoggerFactory.getLogger(SimulatorMsgDecoder.class);

    @Override
    protected void decode(ChannelHandlerContext ctx, Object msg, List list) throws Exception {
        ByteBuf in = (ByteBuf)msg;
        byte[] arr = new byte[in.readableBytes()];
        if(!in.hasArray()){
            in.getBytes(in.readerIndex(), arr);
        }
        String[] marks = {ProtocolSign.BSJ_BEGINMARK.getValue()};
        String bsjBeginMark = ByteUtil.bytesToHexString(ByteUtil.subByteArr(arr, 0, 2));

        if (marks[0].equals(bsjBeginMark)){
            //博实结协议

        }else {
            logger.warn("位置协议内容：" + ByteUtil.bytesToHexString(arr));
        }
    }
}
