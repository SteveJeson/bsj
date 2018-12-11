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
        String hexStr = StringUtil.toHexStringPadded(data);
        if (hexStr != null && hexStr.startsWith(ProtocolSign.BSJ_BEGINMARK.getValue())
                && hexStr.endsWith(ProtocolSign.BSJ_ENDMARK.getValue())
                && !hexStr.contains(ProtocolSign.BSJ_BEGINMARK.getValue() + ProtocolSign.BSJ_ENDMARK.getValue())){//消息未粘包
            decodeMessage(data);
        }else if (hexStr.contains(ProtocolSign.BSJ_BEGINMARK.getValue() + ProtocolSign.BSJ_ENDMARK.getValue())){//消息粘包
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
        header.setProtocolType(ProtocolType.BSJ.getValue());
        Message message = new Message();
        message.setHeader(header);//设置消息头
        message.setBody(data);
        message.setAll(StringUtil.toHexStringPadded(data));

        //设置应答消息
        setReplyBodyAndType(message);
        return message;
    }

    public static void setReplyBodyAndType(Message message){
        String hexStr = message.getAll();
        String protocolNum = hexStr.substring(6, 8);
        byte[] body = Arrays.copyOfRange(message.getBody(), 2, message.getBody().length - 4);
        String serverCheckCode = CrcItu16.CRC_16_X25(body);
        String checkCode = StringUtil.toHexStringPadded(Arrays.copyOfRange(message.getBody(), message.getBody().length - 4, message.getBody().length - 2));
        if (serverCheckCode.equals(checkCode)) {
            if (protocolNum.equals(Command.BSJ_MSG_LOGIN)) {

            } else if (protocolNum.equals(Command.BSJ_MSG_LOCATION)) {

            } else if (protocolNum.equals(Command.BSJ_MSG_ALARM)) {

            } else if (protocolNum.equals(Command.BSJ_MSG_HEARTBEAT)) {

            }
        }else {
            logger.error("验证码错误：" + hexStr);
        }
    }
}
