package com.zdzc.collector.sender.coder;

import com.zdzc.collector.common.coder.MsgDecoder;
import com.zdzc.collector.common.jconst.Command;
import com.zdzc.collector.common.jenum.ProtocolSign;
import com.zdzc.collector.common.jenum.ProtocolType;
import com.zdzc.collector.common.packet.Header;
import com.zdzc.collector.common.packet.Message;
import com.zdzc.collector.common.utils.CrcItu16;
import io.netty.util.internal.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.misc.CRC16;

import java.util.Arrays;

/**
 * 博实结协议解析类
 */
public class ToBsjMessageDecoder {
    private static final Logger logger = LoggerFactory.getLogger(ToBsjMessageDecoder.class);

    /**
     * 博实结数据解析
     * @param data
     */
    public static Message decode(byte[] data){
        String hexStr = StringUtil.toHexStringPadded(data).toUpperCase();
        //消息未粘包
        if (hexStr != null && hexStr.startsWith(ProtocolSign.BSJ_BEGINMARK.getValue())
                && hexStr.endsWith(ProtocolSign.BSJ_ENDMARK.getValue())
                && !hexStr.contains(ProtocolSign.BSJ_BEGINMARK.getValue() + ProtocolSign.BSJ_ENDMARK.getValue())){
            return decodeMessage(data);
        }else if (hexStr.contains(ProtocolSign.BSJ_BEGINMARK.getValue() + ProtocolSign.BSJ_ENDMARK.getValue())){
            //消息粘包
            Header header = new Header();
            header.setProtocolType(ProtocolType.BSJ.getValue());
            Message message = new Message();
            message.setHeader(header);
            message.setStick(true);
            message.setAll(hexStr);
            return message;
        }else {//无法识别的消息类型
            logger.warn("unknow message: " + hexStr);
        }
        return null;
    }

    /**
     * 解析消息
     * @param data
     * @return
     */
    public static Message decodeMessage(byte[] data){
        String hexStr = StringUtil.toHexStringPadded(data);
        //设置消息头
        Header header = new Header();
        header.setProtocolType(ProtocolType.BSJ.getValue());
        header.setTerminalPhone(hexStr.substring(8, 24));
        Message message = new Message();
        //设置消息头
        message.setHeader(header);
        message.setAll(hexStr);
        message.setBody(data);
        message.setStick(false);
        //设置应答消息
        setReplyBodyAndType(message, data);
        return message;
    }

    /**
     * 组装服务端回复信息
     * @param message
     * @param data
     */
    public static void setReplyBodyAndType(Message message, byte[] data){
        String hexStr = message.getAll();
        String protocolNum = hexStr.substring(6, 8);
        byte[] body = Arrays.copyOfRange(message.getBody(), 2, data.length - 4);
        String serverCheckCode = CrcItu16.CRC_16_X25(body).toUpperCase();
        String checkCode = hexStr.substring(hexStr.length() - 8, hexStr.length() - 4).toUpperCase();
        String serialNum = hexStr.substring(hexStr.length() - 12, hexStr.length() - 8);
        if (serverCheckCode.equals(checkCode)) {
            if (protocolNum.equals(Command.BSJ_MSG_LOGIN)) {
                message.getHeader().setMsgIdStr(Command.BSJ_MSG_LOGIN);
                message.setReplyBody(StringUtil.decodeHexDump(generateReply(Command.BSJ_MSG_LOGIN, serialNum)));
            } else if (protocolNum.equals(Command.BSJ_MSG_ALARM)) {
                message.getHeader().setMsgIdStr(Command.BSJ_MSG_ALARM);
                message.setReplyBody(StringUtil.decodeHexDump(generateReply(Command.BSJ_MSG_ALARM, serialNum)));
            } else if (protocolNum.equals(Command.BSJ_MSG_HEARTBEAT)) {
                message.getHeader().setMsgIdStr(Command.BSJ_MSG_HEARTBEAT);
                message.setReplyBody(StringUtil.decodeHexDump(generateReply(Command.BSJ_MSG_HEARTBEAT, serialNum)));
            } else if (protocolNum.equals(Command.BSJ_MSG_LOCATION)){
                message.getHeader().setMsgIdStr(Command.BSJ_MSG_LOCATION);
            }
        }else {
            logger.error("验证码错误：" + hexStr);
        }
    }

    public static String generateReply(String replyType, String serialNum){
        String returnBody = Command.BSJ_MSG_RETURN_LENGTH + replyType + serialNum;
        byte[] btBody = StringUtil.decodeHexDump(returnBody);
        String checkNum = CrcItu16.CRC_16_X25(btBody);
        String result = ProtocolSign.BSJ_BEGINMARK.getValue() + returnBody + checkNum + ProtocolSign.BSJ_ENDMARK.getValue();
        return result;
    }
}
