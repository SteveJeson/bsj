package com.zdzc.collector.sender.coder;

import com.zdzc.collector.common.jconst.SysConst;
import com.zdzc.collector.common.packet.Header;
import com.zdzc.collector.common.packet.Message;
import com.zdzc.collector.common.utils.ByteUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

public class JtProtocolDecoder extends ByteToMessageDecoder {

    private int headerLen = 12;
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf buffer, List<Object> out) throws Exception {
        if (buffer.readableBytes() > 2048) {
            return;
        }

        // 记录包头开始的index
        int beginReader;

        while (buffer.isReadable()) {
            if(buffer.readableBytes() < headerLen){
                return;
            }
            Header header = new Header();
            Message message = new Message();
            byte[] readable = new byte[buffer.readableBytes()];
            buffer.getBytes(buffer.readerIndex(), readable);
            System.out.println("可读的完整包 -> "+ ByteUtil.bytesToHexString(readable));
            // 获取包头开始的index
            beginReader = buffer.readerIndex();
            System.out.println("指针 -> "+beginReader);
            //读取开始标志
            buffer.readBytes(1);
            byte beginByte = buffer.getByte(0);
            if(!(SysConst.JTPROTOCOL_BEGINMARK == beginByte) && buffer.readableBytes() < headerLen){
                System.out.println("===不合法的包====");
                buffer.skipBytes(buffer.readableBytes());
                return;
            }


            System.out.println("指针 -> "+beginReader);
            //读取消息ID
            buffer.readBytes(2);
            System.out.println("指针 -> "+beginReader);

            //读取消息属性
            byte[] prop = new byte[2];
            buffer.getBytes(buffer.readerIndex(), prop);
            int contentLen = ByteUtil.twoBytesToInteger(prop);
            if(buffer.readableBytes() < contentLen){
                buffer.markReaderIndex();
                return;
            }
            System.out.println("消息体长度 -> "+contentLen);
            buffer.readBytes(2);

            //读取终端手机号
            byte[] terminalPhone = new byte[6];
            buffer.getBytes(buffer.readerIndex(), terminalPhone);
            String phone = ByteUtil.bytesToHexString(terminalPhone);
            System.out.println("手机号 -> "+ phone);
            header.setTerminalPhone(phone);
            message.setHeader(header);
            buffer.readBytes(6);
            System.out.println("剩余可读长度 -> "+buffer.readableBytes());

            //读取流水号
            byte[] flowIdByte = new byte[2];
            buffer.getBytes(buffer.readerIndex(), flowIdByte);
            System.out.println("流水号 -> "+ByteUtil.bytesToHexString(flowIdByte));
            buffer.readBytes(2);

            //读取消息体
            byte[] body = new byte[contentLen];
            buffer.getBytes(buffer.readerIndex(), body);
            System.out.println("消息体 -> "+ByteUtil.bytesToHexString(body));
            buffer.readBytes(contentLen);

            //读取校验码
            byte[] checkSum = new byte[1];
            buffer.getBytes(buffer.readerIndex(), checkSum);
            System.out.println("校验码 -> "+ ByteUtil.bytesToHexString(checkSum));
            buffer.readBytes(1);

            //读取结束符
            byte[] endMark = new byte[1];
            buffer.getBytes(buffer.readerIndex(), endMark);
            System.out.println("结束符 -> "+ByteUtil.bytesToHexString(endMark));
            buffer.readBytes(1);
            System.out.println("剩余可读字节数 -> "+buffer.readableBytes());

            out.add(message);
            // 标记包头开始的index
            buffer.markReaderIndex();

            if(buffer.readableBytes() == 0){
                break;
            }

//            if(buffer.readableBytes() < headerLen){
//                return;
//            }

            // 未读到包头，略过一个字节
            // 每次略过，一个字节，去读取，包头信息的开始标记
            buffer.resetReaderIndex();
//            buffer.readByte();

            // 当略过，一个字节之后，
            // 数据包的长度，又变得不满足
            // 此时，应该结束。等待后面的数据到达
//            if (buffer.readableBytes() < 1024) {
//                return;
//            }
        }

        System.out.println("可读长度 -> "+buffer.readableBytes());

    }
}
