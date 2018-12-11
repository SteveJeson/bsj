package com.zdzc.collector.sender.coder;

import com.zdzc.collector.common.jconst.Command;
import com.zdzc.collector.common.jenum.DataType;
import com.zdzc.collector.common.jenum.ProtocolSign;
import com.zdzc.collector.common.jenum.ProtocolType;
import com.zdzc.collector.common.packet.Header;
import com.zdzc.collector.common.packet.Message;
import com.zdzc.collector.common.utils.DateUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @Author liuwei
 * @Description 沃瑞特C11协议解析类
 * @Date 2018/12/11 15:50
 */
public class ToWrtMessageDecoder {

    private static final Logger logger = LoggerFactory.getLogger(ToWrtMessageDecoder.class);

    /**
     * 沃瑞特C11协议数据解析
     * @param data
     * @return
     */
    public static Message decode(String data){
        if (data != null && data.startsWith(ProtocolSign.WRT_BEGINMARK.getValue())
                && data.endsWith(ProtocolSign.WRT_ENDMARK.getValue())
                && !data.contains(ProtocolSign.WRT_ENDMARK.getValue()+ProtocolSign.WRT_BEGINMARK.getValue())) {
            //直接处理
            return decodeMessage(data);
        } else {
            //粘包数据
            Header header = new Header();
            header.setProtocolType(ProtocolType.WRT.getValue());
            Message message = new Message();
            message.setHeader(header);
            message.setStick(true);
            message.setAll(data);
            return message;
        }
    }

    /**
     * 拆包 -> 沃瑞特C11
     * @param info
     * @return
     */
    public static List<String> dealPackageSplicing(String info){
        List<String> messages = new ArrayList<>();
        String beginMark = ProtocolSign.WRT_BEGINMARK.getValue();
        String endMark = ProtocolSign.WRT_ENDMARK.getValue();
        String[] msgArr = info.split(endMark + beginMark);
        if (msgArr.length != 1) {
            for (int i = 0; i < msgArr.length; i++) {
                String message;
                if (i == 0) {
                    message = msgArr[i] + endMark;
                } else if (i == msgArr.length - 1) {
                    message = beginMark + msgArr[i];
                } else {
                    message = beginMark + msgArr[i] + endMark;
                }
                messages.add(message);
            }
        }else {
            logger.warn("unknown message: " + info);
        }
        return messages;
    }

    /**
     * 解析消息 -> 沃瑞特C11
     * @param data
     * @return
     */
    public static Message decodeMessage(String data){
        //设置消息头
        Header header = new Header();
        decodeHeader(data, header);
        Message message = new Message();
        message.setHeader(header);
        message.setStick(false);
        message.setAll(data);
        //设置消息体
        String body = data.substring(7, data.length()-1);
        message.setBody(body.getBytes());
        //设置应答消息
        setReplyMessage(message);
        return message;
    }

    /**
     * 解析消息头 -> 沃瑞特C11
     * @param data
     * @param header
     */
    public static void decodeHeader(String data, Header header){
        String msgIdStr = data.substring(3, 7);
        header.setMsgIdStr(msgIdStr);
        header.setProtocolType(ProtocolType.WRT.getValue());
        if(StringUtils.equals(Command.WRT_MSG_ID_LOGIN, msgIdStr)){
            String terminalPhone = data.substring(7, 22);
            header.setTerminalPhone(terminalPhone);
        }
    }

    /**
     * 设置应答消息 -> 沃瑞特C11
     * @param message
     */
    public static void setReplyMessage(Message message){
        String msgIdStr = message.getHeader().getMsgIdStr();
        String beginMark = ProtocolSign.WRT_BEGINMARK.getValue();
        String endMark = ProtocolSign.WRT_ENDMARK.getValue();
        String resp;
        switch (msgIdStr){
            case Command.WRT_MSG_ID_LOGIN:
                String date = DateFormatUtils.format(new Date(DateUtil.getUTCTime()), "yyyyMMddHHmmss");
                resp = Command.WRT_MSG_LOGIN_RESP + date;
                break;
            case Command.WRT_MSG_ID_TERMINAL_LOCATION:
                resp = Command.WRT_MSG_LOCATION_RESP;
                message.getHeader().setMsgType(DataType.GPS.getValue());
                break;
            case Command.WRT_MSG_ID_TERMINAL_ALARM:
                resp = Command.WRT_MSG_ALARM_RESP;
                message.getHeader().setMsgType(DataType.ALARM.getValue());
                break;
            case Command.WRT_MSG_ID_TERMINAL_HEARTBEAT:
                resp = Command.WRT_MSG_HEARTBEAT_RESP;
                message.getHeader().setMsgType(DataType.HEARTBEAT.getValue());
                break;
            case Command.WRT_MSG_ID_TERMINAL_CONTROLLER:
                resp = Command.WRT_MSG_CONTROLLER_RESP;
                message.getHeader().setMsgType(DataType.CONTROLLER.getValue());
                break;
            case Command.WRT_MSG_ID_TERMINAL_STATUS:
                resp = Command.WRT_MSG_STATUS_RESP;
                message.getHeader().setMsgType(DataType.ALARM.getValue());
                break;
            case Command.WRT_MSG_ID_TERMINAL_IMSI:
                resp = Command.WRT_MSG_IMSI_RESP;
                message.getHeader().setMsgType(DataType.ALARM.getValue());
                break;
            default:
                resp = "";
                logger.warn("未知的消息ID -> {}", msgIdStr);
                break;
        }
        if(StringUtils.isEmpty(resp)){
            return;
        }
        String replyMsg = beginMark + resp + endMark;
        message.setReplyBody(replyMsg.getBytes());
    }

}
