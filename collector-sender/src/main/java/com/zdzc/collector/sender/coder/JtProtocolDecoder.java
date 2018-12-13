package com.zdzc.collector.sender.coder;

import ch.qos.logback.core.encoder.ByteArrayUtil;
import com.zdzc.collector.common.coder.MsgDecoder;
import com.zdzc.collector.common.jconst.SysConst;
import com.zdzc.collector.common.packet.Header;
import com.zdzc.collector.common.packet.Message;
import com.zdzc.collector.common.utils.ByteUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class JtProtocolDecoder extends ByteToMessageDecoder {

    private int headerLen = 12;

    private static java.util.concurrent.atomic.AtomicInteger count = new AtomicInteger(1);

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf buffer, List<Object> out) throws Exception {
        System.out.println("=========================第"+count.getAndIncrement()+"个数据包=======================");
        if (buffer.readableBytes() > 2048) {
            return;
        }

        byte[] b = new byte[buffer.readableBytes()];
        buffer.getBytes(buffer.readerIndex(), b);
//        System.out.println("完整元数据 -> "+ ByteArrayUtil.toHexString(b));
        byte[] bs =  MsgDecoder.doReceiveEscape(b);
//        System.out.println("转义还原 -> "+ByteArrayUtil.toHexString(bs));
        buffer.clear();
        buffer.writeBytes(bs);

        byte[] r = new byte[buffer.readableBytes()];
        buffer.getBytes(buffer.readerIndex(), r);
//        System.out.println("剩余可读包 -> "+ByteArrayUtil.toHexString(r));

        while (buffer.isReadable()) {
            if(buffer.readableBytes() < headerLen){
                return;
            }

            Header header = new Header();
            Message message = new Message();
            byte[] readable = new byte[buffer.readableBytes()];
            buffer.getBytes(buffer.readerIndex(), readable);
            String hexPacket = ByteUtil.bytesToHexString(readable);
            System.out.println("可读的完整包 -> " + hexPacket);

            //1、读取开始标识符
            byte[] beginMark = new byte[1];
            buffer.getBytes(buffer.readerIndex(), beginMark);
            System.out.println("开始标识符 -> " + ByteArrayUtil.toHexString(beginMark));
            if(!(SysConst.JTPROTOCOL_BEGINMARK == ByteUtil.byteToInteger(beginMark))){
                System.out.println("不合法的包 -> " + hexPacket);
                buffer.skipBytes(buffer.readableBytes());
                return;
            }

            byte[] propByte = new byte[2];
            buffer.getBytes(buffer.readerIndex() + 3, propByte);
            boolean hasSubPackage = (((ByteUtil.byteToInteger(propByte) & 0x2000) >> 13) == 1);

            int contentLen = ByteUtil.byteToInteger(propByte) & 0x3ff;
            if(buffer.readableBytes() < contentLen + headerLen + 3){
                return;
            }

            //读掉开始标识符的一个字节
            buffer.readBytes(1);

            //2、读取消息ID
            byte[] msgId = new byte[2];
            buffer.getBytes(buffer.readerIndex(), msgId);
            header.setMsgId(ByteUtil.byteToInteger(msgId));
            buffer.readBytes(2);
            System.out.println("消息ID -> " + ByteUtil.bytesToHexString(msgId));

            //3、读取消息属性
            byte[] prop = new byte[2];
            buffer.getBytes(buffer.readerIndex(), prop);
            int bodyProp = ByteUtil.byteToInteger(prop);
            header.setMsgBodyProps(bodyProp);
            buffer.readBytes(2);
            System.out.println("消息属性 -> " + ByteUtil.bytesToHexString(prop));
            int bodyLen = ByteUtil.twoBytesToInteger(prop);
            header.setMsgBodyLength(bodyLen);


            //4、读取终端手机号
            byte[] terminalPhone = new byte[6];
            buffer.getBytes(buffer.readerIndex(), terminalPhone);
            buffer.readBytes(6);
            String phone = ByteUtil.bytesToHexString(terminalPhone);
            System.out.println("手机号 -> "+ phone);
            header.setTerminalPhone(phone);


            //5、读取流水号
            byte[] flowIdByte = new byte[2];
            buffer.getBytes(buffer.readerIndex(), flowIdByte);
            buffer.readBytes(2);
            header.setFlowId(ByteUtil.byteToInteger(flowIdByte));
            System.out.println("流水号 -> "+ByteUtil.bytesToHexString(flowIdByte));

            //6、读取消息体
            if(hasSubPackage){
                buffer.readBytes(4);
            }
            byte[] body = new byte[contentLen];
            buffer.getBytes(buffer.readerIndex(), body);
            buffer.readBytes(contentLen);
            message.setBody(body);
            System.out.println("消息体 -> "+ByteUtil.bytesToHexString(body));

            //7、读取校验码
            byte[] checkSum = new byte[1];
            buffer.getBytes(buffer.readerIndex(), checkSum);
            buffer.readBytes(1);
            System.out.println("校验码 -> "+ ByteUtil.bytesToHexString(checkSum));

            //8、读取结束符
            byte[] endMark = new byte[1];
            buffer.getBytes(buffer.readerIndex(), endMark);
            buffer.readBytes(1);
            System.out.println("结束符 -> "+ByteUtil.bytesToHexString(endMark));

            int len = beginMark.length + msgId.length + prop.length + terminalPhone.length
                    + flowIdByte.length + body.length + checkSum.length + endMark.length;
            ByteBuffer byteBuffer = ByteBuffer.allocate(len);
            byteBuffer.put(beginMark).put(msgId).put(prop).put(terminalPhone).put(flowIdByte).put(body).put(checkSum).put(endMark);

            message.setHeader(header);
            message.setAll(ByteArrayUtil.toHexString(byteBuffer.array()));

            out.add(message);

            buffer.markReaderIndex();

        }

        System.out.println("可读长度 -> "+buffer.readableBytes());

    }
}
