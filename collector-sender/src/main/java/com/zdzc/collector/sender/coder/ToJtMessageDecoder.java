package com.zdzc.collector.sender.coder;

import ch.qos.logback.core.encoder.ByteArrayUtil;
import com.zdzc.collector.common.coder.MsgDecoder;
import com.zdzc.collector.common.jconst.Command;
import com.zdzc.collector.common.jenum.DataType;
import com.zdzc.collector.common.jenum.ProtocolType;
import com.zdzc.collector.common.packet.Header;
import com.zdzc.collector.common.packet.Message;
import com.zdzc.collector.common.utils.ByteUtil;
import io.netty.handler.ssl.ApplicationProtocolConfig;
import io.netty.util.internal.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 部标808协议解析类
 */
public class ToJtMessageDecoder {

    private static final Logger logger = LoggerFactory.getLogger(ToJtMessageDecoder.class);

    /**
     * JT808数据解析
     * @param data
     */
    public static Message decode(byte[] data){
        String hexStr = StringUtil.toHexStringPadded(data);
        logger.info("source data -> "+hexStr);
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
        message.setHeader(header);//设置消息头
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
        message.setBody(buffer);//设置消息体
        message.setAll(StringUtil.toHexStringPadded(data));

        //设置应答消息
        setReplyBodyAndType(message);
        return message;
    }

    public static void decodeHeader(byte[] data, Header header){
        int msgId = ByteUtil.cutBytesToInt(data, 1, 2);
        int msgBodyProps = ByteUtil.cutBytesToInt(data, 2 + 1, 2);
        boolean hasSubPackage = (((msgBodyProps & 0x2000) >> 13) == 1);
        int msgBodyLength = (ByteUtil.cutBytesToInt(data, 2 + 1, 2) & 0x3ff);
        String terminalPhone = StringUtil.toHexStringPadded(ByteUtil.subByteArr(data, 5, 6));
        int flowId = ByteUtil.cutBytesToInt(data, 11, 2);
        header.setMsgId(msgId);
        header.setMsgBodyProps(msgBodyProps);
        header.setHasSubPackage(hasSubPackage);
        header.setMsgBodyLength(msgBodyLength);
        header.setTerminalPhone(terminalPhone);
        header.setMsgLength(data.length);
        header.setProtocolType(ProtocolType.JT808.getValue());
        header.setFlowId(flowId);
    }

    /**
     * 设置应答消息
     * @param message
     */
    public static void setReplyBodyAndType(Message message){
        int msgId = message.getHeader().getMsgId();
        String terminalPhone = message.getHeader().getTerminalPhone();
        int flowId = message.getHeader().getFlowId();
        byte[] body = message.getBody();
        String all = message.getAll();
        if (msgId == Command.MSG_ID_TERMINAL_REGISTER)
        {
            logger.info("【808】终端注册 ==> " + all);
            //1. 终端注册 ==> 终端注册应答
            byte[] sendMsg = newRegistryReplyMsg(0014, terminalPhone, flowId);
            message.setReplyBody(sendMsg);
            message.getHeader().setMsgType(DataType.Registry.getValue());
        }else if (msgId == Command.MSG_ID_TERMINAL_AUTHENTICATION)
        {
            logger.info("【808】终端鉴权 ==> " + all);
            //2. 终端鉴权 ==> 平台通用应答
            byte[] sendMsg = newCommonReplyMsg(0005, terminalPhone, flowId, msgId);
            message.setReplyBody(sendMsg);
            //查询终端属性
            byte[] sendBody = newQueryPropReplyMsg(0005, terminalPhone, flowId);
            message.setExtReplyBody(sendBody);
            message.getHeader().setMsgType(DataType.Authentication.getValue());
        }else if (msgId == Command.MSG_ID_TERMINAL_HEART_BEAT)
        {
            //3. 终端心跳-消息体为空 ==> 平台通用应答
            logger.info("【808】终端心跳 ==> " + all);
            SimpleDateFormat sdf = new SimpleDateFormat("yyMMddHHmmss");
            String time = sdf.format(new Date());
            byte[] msgBody = ByteArrayUtil.hexStringToByteArray(time);//自定义心跳body为当前时间
            //客户端消息应答
            byte[] sendMsg = newCommonReplyMsg(0005, terminalPhone, flowId, msgId);
            //设置回复信息
            message.setBody(msgBody);
            message.setReplyBody(sendMsg);
            message.getHeader().setMsgType(DataType.HEARTBEAT.getValue());
        }else if (msgId == Command.MSG_ID_TERMINAL_LOCATION_INFO_UPLOAD)
        {
            logger.info("【808】终端定位（单个） ==> " + all);
            //4. 位置信息汇报 ==> 平台通用应答
            byte[] sendMsg = newCommonReplyMsg(0005, terminalPhone, flowId, msgId);
            message.setReplyBody(sendMsg);
            int alarmSign = ByteUtil.cutBytesToInt(body, 0, 4);
            message.getHeader().setMsgType(alarmSign <= 0?DataType.GPS.getValue():DataType.ALARM.getValue());
        }else if (msgId == Command.MSG_ID_TERMINAL_LOCATION_INFO_BATCH_UPLOAD)
        {
            logger.info("【808】终端定位（批量） ==> " + all);
            //5.定位数据批量上传0x0704协议解析
            byte[] sendMsg = newCommonReplyMsg(0005, terminalPhone, flowId, msgId);
            message.setReplyBody(sendMsg);
            byte[] mb = ByteUtil.subByteArr(body, 5, body.length - 5);
            int alarmSign = ByteUtil.cutBytesToInt(mb, 0, 4);
            message.getHeader().setMsgType(alarmSign <= 0?DataType.GPS.getValue():DataType.ALARM.getValue());
        }else if (msgId == Command.MSG_ID_TERMINAL_PROP_QUERY_RESP)
        {
            logger.info("【808】终端属性查询应答 ==> " + all);
            //6.终端属性应答消息
            byte[] msgType = new byte[1];
            msgType[0] = 02;
            byte[] newBodyByte = ByteUtil.bytesMerge(msgType, body);
            message.setBody(newBodyByte);
            message.getHeader().setMsgType(DataType.Property.getValue());
        }else
        {
            logger.error("【808】未知消息类型，终端手机号 ==> "+terminalPhone);
        }
    }

    /**
     * 终端注册消息应答
     * @param msgBodyProps
     * @param phone
     * @param flowId
     * @return
     */
    public static byte[] newRegistryReplyMsg(int msgBodyProps, String phone, int flowId)
    {
        //7E
        //8100            消息ID
        //0004            消息体属性
        //018512345678    手机号
        //0015            消息流水号
        //0015            应答流水号
        //04              结果(00成功, 01车辆已被注册, 02数据库中无该车辆, 03终端已被注册, 04数据库中无该终端)  无车辆与无终端有什么区别 ?
        //313C             鉴权码
        //7E
        int len = 0;
        // 1. 0x7e
        byte[] bt1 = ByteUtil.integerTo1Bytes(Command.PKG_DELIMITER);
        len += bt1.length;
        // 2. 消息ID word(16)
        byte[] bt2 = ByteUtil.integerTo2Bytes(Command.CMD_TERMINAL_REGISTER_RESP);
        len += bt2.length;
        // 3.消息体属性
        byte[] bt3 = ByteUtil.integerTo2Bytes(msgBodyProps);
        len += bt3.length;
        // 4. 终端手机号 bcd[6]
        byte[] bt4 = ByteUtil.string2Bcd(phone);
        len += bt4.length;
        // 5. 消息流水号 word(16),按发送顺序从 0 开始循环累加
        byte[] bt5 = ByteUtil.integerTo2Bytes(flowId);
        len += bt5.length;
        // 6. 应答流水号
        byte[] bt6 = ByteUtil.integerTo2Bytes(flowId);
        len += bt6.length;
        // 7. 成功
        byte[] bt7 = ByteUtil.integerTo1Bytes(0);
        len += bt7.length;
        // 8. 鉴权码
        byte[] bt8 = new byte[0];
        try {
            bt8 = Command.REPLYTOKEN.getBytes(Command.STRING_ENCODING);
        } catch (UnsupportedEncodingException e) {
            logger.error("replytoken parse error: "+e.getMessage());
        }
        len += bt8.length;
        ByteBuffer buffer = ByteBuffer.allocate(len);
        buffer.put(bt1);
        buffer.put(bt2);
        buffer.put(bt3);
        buffer.put(bt4);
        buffer.put(bt5);
        buffer.put(bt6);
        buffer.put(bt7);
        buffer.put(bt8);
        // 校验码
        int checkSum = MsgDecoder.calculateChecksum(buffer.array(), 1, buffer.array().length);
        byte[] bt9 = ByteUtil.integerTo1Bytes(checkSum);
        len += bt9.length;
        len += bt1.length;
        ByteBuffer buf = ByteBuffer.allocate(len);
        buf.put(buffer.array());
        buf.put(bt9);
        //结束符
        buf.put(bt1);

        // 转义
        return MsgDecoder.doSendEscape(buf.array(), 1, buf.array().length - 1);
    }

    /**
     * 通用消息应答
     * @param msgBodyProps
     * @param phone
     * @param flowId
     * @param msgId
     * @return
     */
    public static byte[] newCommonReplyMsg(int msgBodyProps, String phone, int flowId, int msgId)
    {
        //7E
        //8100            消息ID
        //0004            消息体属性
        //018512345678    手机号
        //0015            消息流水号
        //0015            应答流水号
        //04              结果(00成功, 01车辆已被注册, 02数据库中无该车辆, 03终端已被注册, 04数据库中无该终端)  无车辆与无终端有什么区别 ?
        //313C             鉴权码
        //7E
        int len = 0;
        // 1. 0x7e
        byte[] bt1 = ByteUtil.integerTo1Bytes(Command.PKG_DELIMITER);
        len += bt1.length;
        // 2. 消息ID word(16)
        byte[] bt2 = ByteUtil.integerTo2Bytes(Command.CMD_COMMON_RESP);
        len += bt2.length;
        // 3.消息体属性
        byte[] bt3 = ByteUtil.integerTo2Bytes(msgBodyProps);
        len += bt3.length;
        // 4. 终端手机号 bcd[6]
        byte[] bt4 = ByteUtil.string2Bcd(phone);
        len += bt4.length;
        // 5. 消息流水号 word(16),按发送顺序从 0 开始循环累加
        byte[] bt5 = ByteUtil.integerTo2Bytes(flowId);
        len += bt5.length;
        // 6. 应答流水号
        byte[] bt6 = ByteUtil.integerTo2Bytes(flowId);
        len += bt6.length;
        // 7. 对应终端消息ID
        byte[] bt7 = ByteUtil.integerTo2Bytes(msgId);
        len += bt7.length;
        // 8. 成功
        byte[] bt8 = ByteUtil.integerTo1Bytes(0);
        len += bt8.length;

        ByteBuffer buffer = ByteBuffer.allocate(len);
        buffer.put(bt1);
        buffer.put(bt2);
        buffer.put(bt3);
        buffer.put(bt4);
        buffer.put(bt5);
        buffer.put(bt6);
        buffer.put(bt7);
        buffer.put(bt8);
        // 校验码
        int checkSum = MsgDecoder.calculateChecksum(buffer.array(), 1, buffer.array().length);
        byte[] bt9 = ByteUtil.integerTo1Bytes(checkSum);
        len += bt9.length;
        len += bt1.length;
        ByteBuffer buf = ByteBuffer.allocate(len);
        buf.put(buffer.array());
        buf.put(bt9);
        //结束符
        buf.put(bt1);
        // 转义
        return MsgDecoder.doSendEscape(buf.array(), 1, buf.array().length - 1);
    }

    /**
     * 终端鉴权 ==> 查询终端属性
     * @param msgBodyProps
     * @param phone
     * @param flowId
     * @return
     */
    public static byte[] newQueryPropReplyMsg(int msgBodyProps, String phone, int flowId){
        int len = 0;
        // 1. 0x7e
        byte[] bt1 = ByteUtil.integerTo1Bytes(Command.PKG_DELIMITER);
        len += bt1.length;
        // 2. 消息ID word(16)
        byte[] bt2 = ByteUtil.integerTo2Bytes(Command.CMD_TERMINAL_PROP_QUERY);
        len += bt2.length;
        // 3.消息体属性
        byte[] bt3 = ByteUtil.integerTo2Bytes(msgBodyProps);
        len += bt3.length;
        // 4. 终端手机号 bcd[6]
        byte[] bt4 = ByteUtil.string2Bcd(phone);
        len += bt4.length;
        // 5. 消息流水号 word(16),按发送顺序从 0 开始循环累加
        byte[] bt5 = ByteUtil.integerTo2Bytes(flowId);
        len += bt5.length;
        ByteBuffer buffer = ByteBuffer.allocate(len);
        buffer.put(bt1);
        buffer.put(bt2);
        buffer.put(bt3);
        buffer.put(bt4);
        buffer.put(bt5);

        // 6.校验码
        int checkSum = MsgDecoder.calculateChecksum(buffer.array(), 1, buffer.array().length);
        byte[] bt9 = ByteUtil.integerTo1Bytes(checkSum);
        len += bt9.length;
        len += bt1.length;
        ByteBuffer buf = ByteBuffer.allocate(len);
        buf.put(buffer.array());
        buf.put(bt9);
        // 7. 0x7e
        buf.put(bt1);

        // 转义
        return MsgDecoder.doSendEscape(buf.array(), 1, buf.array().length - 1);
    }
}
