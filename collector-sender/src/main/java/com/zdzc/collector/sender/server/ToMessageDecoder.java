package com.zdzc.collector.sender.server;

import com.zdzc.collector.common.jconst.SysConst;
import com.zdzc.collector.common.jenum.ProtocolSign;
import com.zdzc.collector.common.packet.Message;
import com.zdzc.collector.common.utils.ByteUtil;
import com.zdzc.collector.sender.coder.ToBsjMessageDecoder;
import com.zdzc.collector.sender.coder.ToJtMessageDecoder;
import com.zdzc.collector.sender.coder.ToWrtMessageDecoder;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.List;

/**
 * @Author liuwei
 * @Description 消息解析类
 * @Date 2018/12/11 15:50
 */
public class ToMessageDecoder extends MessageToMessageDecoder {

    private static final Logger logger = LoggerFactory.getLogger(ToMessageDecoder.class);

    /**
     * 解析数据
     * @param channelHandlerContext
     * @param o
     * @param list
     * @throws Exception
     */
    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, Object o, List list) throws Exception {
        ByteBuf in = (ByteBuf)o;
        byte[] arr = new byte[in.readableBytes()];
        if(!in.hasArray()){
            in.getBytes(in.readerIndex(), arr);
        }

        List<String> markList = Arrays.asList(ProtocolSign.JT808_BEGINMARK.getValue(), ProtocolSign.WRT_BEGINMARK.getValue(), ProtocolSign.BSJ_BEGINMARK.getValue());
        String beginMark = ByteUtil.bytesToHexString(ByteUtil.subByteArr(arr,0,1));
        String wrtBeginMark;
        String bsjBeginMark;
        try {
            wrtBeginMark = new String(ByteUtil.subByteArr(arr, 0, 3), SysConst.DEFAULT_ENCODING);
            bsjBeginMark = ByteUtil.bytesToHexString(ByteUtil.subByteArr(arr, 0, 2));
            if(!markList.contains(beginMark.toUpperCase()) && !markList.contains(wrtBeginMark.toUpperCase()) && !markList.contains(bsjBeginMark.toUpperCase())){
                String hexStr = ByteUtil.bytesToHexString(arr);
                String str = new String(arr, SysConst.DEFAULT_ENCODING);
                logger.warn("未知协议内容: 十六进制字符串形式 -> {}, 普通字符串形式 -> {}", hexStr, str);
            }
            Message message = null;
            if(StringUtils.equals(markList.get(0), beginMark.toUpperCase())){
                //808协议
                String hexStr = ByteUtil.bytesToHexString(arr);
                logger.debug("source data -> {}", hexStr);
                message = ToJtMessageDecoder.decode(arr);
            }else if(StringUtils.equals(markList.get(1), wrtBeginMark.toUpperCase())){
                //沃瑞特协议
                String str = new String(arr, SysConst.DEFAULT_ENCODING);
                logger.debug("source data -> {}", str);
                message = ToWrtMessageDecoder.decode(str);
            }else if (StringUtils.equals(markList.get(2), bsjBeginMark.toUpperCase())){
                //博实结协议
                String hexStr = ByteUtil.bytesToHexString(arr);
                logger.debug("source data -> {}", hexStr);
                message = ToBsjMessageDecoder.decode(arr);
            }
            if(message != null){
                list.add(message);
            }
        } catch (UnsupportedEncodingException e) {
            logger.error(e.getMessage());
        }

    }

}
