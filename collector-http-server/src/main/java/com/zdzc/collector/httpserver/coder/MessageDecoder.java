package com.zdzc.collector.httpserver.coder;

import com.zdzc.collector.common.coder.MsgDecoder;
import com.zdzc.collector.common.jenum.ProtocolType;
import com.zdzc.collector.common.packet.Header;
import com.zdzc.collector.common.packet.Message;
import com.zdzc.collector.common.utils.ByteUtil;
import com.zdzc.collector.httpserver.server.HttpServerHandler;
import io.netty.util.internal.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author liuwei
 * @Description 消息解码类
 * @Date 2018/12/11 15:41
 */
public class MessageDecoder {

    private static final Logger logger = LoggerFactory.getLogger(HttpServerHandler.class);

    public static List<Message> decode(List<String> msgList){
        List<Message> messages = new ArrayList<>();
        for (String message : msgList) {
            logger.info("source data -> {}", message);
            Message msg = toMessageDecoder(message);
            messages.add(msg);
        }

        return messages;
    }

    public static Message toMessageDecoder(String hexStr){
        byte[] data = ByteUtil.hexToByteArray(hexStr);
        byte[] bs = MsgDecoder.doReceiveEscape(data);
        String hex = StringUtil.toHexStringPadded(bs);
        Boolean isValid = MsgDecoder.validateChecksum(bs);
        if(!isValid){
            logger.error("校验码验证错误：" + hex + " src -> " + hexStr);
            return null;
        }
        return decodeMessage(data);
    }

    /**
     * 解析消息
     * @param data
     * @return
     */
    public static Message decodeMessage(byte[] data){
        //设置消息头
        Header header = new Header();
        decodeHeader(data, header);
        Message message = new Message();
        //设置消息头
        message.setHeader(header);
        int msgBodyByteStartIndex = 12 + 1;
        // 3. 消息体
        // 有子包信息,消息体起始字节后移四个字节:消息包总数(word(16))+包序号(word(16))
        if (header.hasSubPackage())
        {
            msgBodyByteStartIndex = 16 + 1;
        }
        //设置消息体
        byte[] buffer = new byte[header.getMsgBodyLength()];
        System.arraycopy(data, msgBodyByteStartIndex, buffer, 0,header.getMsgBodyLength());
        //设置消息体
        message.setBody(buffer);
        message.setAll(StringUtil.toHexStringPadded(data));

        //设置应答消息
        return message;
    }

    private static void decodeHeader(byte[] data, Header header){
        int msgId = ByteUtil.cutBytesToInt(data, 1, 2);
        int msgBodyProps = ByteUtil.cutBytesToInt(data, 2 + 1, 2);
        boolean hasSubPackage = (((msgBodyProps & 0x2000) >> 13) == 1);
        int msgBodyLength = (ByteUtil.cutBytesToInt(data, 2 + 1, 2) & 0x3ff);
        String terminalPhone = StringUtil.toHexStringPadded(ByteUtil.subByteArr(data, 5, 6));
        int flowId = ByteUtil.cutBytesToInt(data, 11, 2);
        header.setMsgId(msgId);
        header.setHasSubPackage(hasSubPackage);
        header.setMsgBodyLength(msgBodyLength);
        header.setTerminalPhone(terminalPhone);
        header.setMsgLength(data.length);
        header.setProtocolType(ProtocolType.JT808.getValue());
        header.setFlowId(flowId);
    }
}
