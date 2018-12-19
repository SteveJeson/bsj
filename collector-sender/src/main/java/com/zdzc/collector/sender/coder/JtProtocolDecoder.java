package com.zdzc.collector.sender.coder;

import ch.qos.logback.core.encoder.ByteArrayUtil;
import com.zdzc.collector.common.coder.MsgDecoder;
import com.zdzc.collector.common.coder.ToJtMessageDecoder;
import com.zdzc.collector.common.jconst.SysConst;
import com.zdzc.collector.common.jenum.ProtocolType;
import com.zdzc.collector.common.packet.Header;
import com.zdzc.collector.common.packet.Message;
import com.zdzc.collector.common.utils.ByteUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.List;

/**
 * @Author liuwei
 * @Description 部标808解码器类
 * @Date 2018/12/14 11:17
 */
public class JtProtocolDecoder extends ByteToMessageDecoder {

    private static final Logger logger = LoggerFactory.getLogger(JtProtocolDecoder.class);

    private int headerLen = 12;

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf buffer, List<Object> out) {

        //数据包太大不处理，防止恶意流量攻击
        if (buffer.readableBytes() > SysConst.JTPROTOCOL_MAX_BYTES) {
            buffer.release();
            return;
        }
        //接收到的数据包
        byte[] b = new byte[buffer.readableBytes()];
        buffer.getBytes(buffer.readerIndex(), b);
        logger.debug("source data -> {}", ByteArrayUtil.toHexString(b));
        //先转义还原再做处理
//        byte[] bs =  MsgDecoder.doReceiveEscape(b);
//        buffer.clear();
//        buffer.writeBytes(bs);

        while (buffer.isReadable()) {
            if(buffer.readableBytes() < headerLen){
                return;
            }
            byte[] readable = new byte[buffer.readableBytes()];
            buffer.getBytes(buffer.readerIndex(), readable);
            String hexPacket = ByteUtil.bytesToHexString(readable);
//            System.out.println("可读的完整包 -> " + hexPacket);

            //1、读取开始标识符
            byte[] beginMark = new byte[1];
            buffer.getBytes(buffer.readerIndex(), beginMark);
//            System.out.println("开始标识符 -> " + ByteArrayUtil.toHexString(beginMark));
            if(SysConst.JTPROTOCOL_BEGINMARK != ByteUtil.byteToInteger(beginMark)){
                logger.warn("不合法的包 -> {}", hexPacket);
//                buffer.skipBytes(buffer.readableBytes());
                buffer.readBytes(buffer.readableBytes());
                return;
            }
            byte[] propByte = new byte[2];
            buffer.getBytes(buffer.readerIndex() + 3, propByte);
            //判断消息体长度是否足够，消息体长度由消息体属性计算得来
            int contentLen = ByteUtil.byteToInteger(propByte) & 0x3ff;
            if(buffer.readableBytes() < contentLen + headerLen + 3){
                return;
            }

            Header header = new Header();
            Message message = new Message();
            //设置协议类型
            header.setProtocolType(ProtocolType.JT808.getValue());
            //读掉开始标识符的一个字节
            buffer.readBytes(1);
            //2、读取消息ID
            byte[] msgId = new byte[2];
            buffer.getBytes(buffer.readerIndex(), msgId);
            header.setMsgId(ByteUtil.byteToInteger(msgId));
            buffer.readBytes(2);
//            System.out.println("消息ID -> " + ByteUtil.bytesToHexString(msgId));
            //3、读取消息属性
            byte[] prop = new byte[2];
            buffer.getBytes(buffer.readerIndex(), prop);
            int bodyProp = ByteUtil.byteToInteger(prop);
            boolean hasSubPackage = (((ByteUtil.byteToInteger(prop) & 0x2000) >> 13) == 1);
            header.setMsgBodyProps(bodyProp);
            header.setHasSubPackage(hasSubPackage);
            buffer.readBytes(2);
//            System.out.println("消息属性 -> " + ByteUtil.bytesToHexString(prop));
            int bodyLen = ByteUtil.twoBytesToInteger(prop);
            header.setMsgBodyLength(bodyLen);
            //4、读取终端手机号
            byte[] terminalPhone = new byte[6];
            buffer.getBytes(buffer.readerIndex(), terminalPhone);
            buffer.readBytes(6);
            String phone = ByteUtil.bytesToHexString(terminalPhone);
//            System.out.println("手机号 -> "+ phone);
            header.setTerminalPhone(phone);
            //5、读取流水号
            byte[] flowIdByte = new byte[2];
            buffer.getBytes(buffer.readerIndex(), flowIdByte);
            buffer.readBytes(2);
            header.setFlowId(ByteUtil.byteToInteger(flowIdByte));
//            System.out.println("流水号 -> "+ByteUtil.bytesToHexString(flowIdByte));
            //6、读取消息体
            if(hasSubPackage){
                buffer.readBytes(4);
            }
            byte[] body = new byte[contentLen];
            buffer.getBytes(buffer.readerIndex(), body);
            buffer.readBytes(contentLen);
            message.setBody(body);
//            System.out.println("消息体 -> "+ByteUtil.bytesToHexString(body));

            //7、读取校验码
            byte[] checkSum = new byte[1];
            buffer.getBytes(buffer.readerIndex(), checkSum);
            int checkSumInt = ByteUtil.byteToInteger(checkSum);
            buffer.readBytes(1);
//            System.out.println("校验码 -> "+ ByteUtil.bytesToHexString(checkSum));
            //8、读取结束符
            byte[] endMark = new byte[1];
            buffer.getBytes(buffer.readerIndex(), endMark);
            buffer.readBytes(1);
//            System.out.println("结束符 -> "+ByteUtil.bytesToHexString(endMark));

            int len = beginMark.length + msgId.length + prop.length + terminalPhone.length
                    + flowIdByte.length + body.length + checkSum.length + endMark.length;
            ByteBuffer byteBuffer = ByteBuffer.allocate(len);
            byteBuffer.put(beginMark).put(msgId).put(prop).put(terminalPhone).put(flowIdByte).put(body).put(checkSum).put(endMark);
            byte[] arr = byteBuffer.array();
            String all = ByteArrayUtil.toHexString(arr);
            int code = MsgDecoder.calculateChecksum(arr, 1, arr.length - 1 - 1);
            if(code != checkSumInt){
                logger.error("checksum error -> {} expected: {}, actual: {}", all, checkSumInt, code);
            }
            //设置header
            message.setHeader(header);
            //设置完整消息包
            message.setAll(all);
            //设置应答消息
            ToJtMessageDecoder.setReplyBodyAndType(message);
            //设置发送到MQ的消息体
            ToJtMessageDecoder.toSetSendBody(message);
            out.add(message);
            buffer.markReaderIndex();
        }
    }
}
