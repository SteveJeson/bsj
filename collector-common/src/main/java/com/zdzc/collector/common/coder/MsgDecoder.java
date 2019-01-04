package com.zdzc.collector.common.coder;

import com.zdzc.collector.common.utils.ByteUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * @Author liuwei
 * @Description 通用消息解码类
 * @Date 2018/12/11 15:44
 */
public class MsgDecoder {

    private static final Logger logger = LoggerFactory.getLogger(MsgDecoder.class);

    /**
     * 验证校验和(JT808协议)
     *
     * @param data
     * @return
     */
    public static Boolean validateChecksum(byte[] data) {
        // 1. 去掉分隔符之后，最后一位就是校验码
        int checkSumInPkg = data[data.length - 1 - 1] & 0xFF;
        int calculatedCheckSum = calculateChecksum(data, 1, data.length - 1 - 1);
        if (checkSumInPkg != calculatedCheckSum) {
            return false;
        }
        return true;
    }

    /**
     * 计算校验和(JT808协议) -> 部标808
     * 从开始标识符后一位到校验和前一位做异或运算
     *
     * @param data
     * @param from
     * @param to
     * @return
     */
    public static int calculateChecksum(byte[] data, int from, int to) {
        int cs = 0;
        for (int i = from; i < to; i++) {
            cs ^= data[i];

        }
        return cs & 0xFF;
    }

    /**
     * 转义还原(JT808协议)
     * 0x7d 0x01 -> 0x7d
     * 0x7d 0x02 -> 0x7e
     *
     * @param data
     * @return
     */
    public static byte[] doReceiveEscape(byte[] data) {
        List<Byte> list = new LinkedList<>();
        for (int i = 0; i < data.length; i++) {
            if (data[i] == 0x7d && data[i + 1] == 0x01) {
                list.add((byte) 0x7d);
                i++;
            } else if (data[i] == 0x7d && data[i + 1] == 0x02) {
                list.add((byte) 0x7e);
                i++;
            } else {
                list.add(data[i]);
            }
        }
        ByteBuffer bb = ByteBuffer.allocate(list.size());
        for (Byte b : list) {
            bb.put(b);
        }
        return bb.array();
    }

    /**
     * 发送消息时转义
     *
     * @param data
     * @param start
     * @param end
     * @return
     */
    public static byte[] doSendEscape(byte[] data, int start, int end) {
        List<Byte> list = new LinkedList<>();
        for (int i = 0; i < start; i++) {
            list.add(data[i]);
        }
        for (int i = start; i < end; i++) {
            if (data[i] == 0x7e) {
                list.add((byte) 0x7d);
                list.add((byte) 0x02);
            } else if (data[i] == 0x7d) {
                list.add((byte) 0x7d);
                list.add((byte) 0x01);
            } else {
                list.add(data[i]);
            }
        }
        for (int i = end; i < data.length; i++) {
            list.add(data[i]);
        }
        ByteBuffer buffer = ByteBuffer.allocate(list.size());
        for (Byte b : list) {
            buffer.put(b);
        }
        return buffer.array();
    }

    /**
     * 拆包
     *
     * @param info      消息
     * @param beginMark 起始标志
     * @param endMark
     * @return
     */
    public static List<String> dealPackageSplicing(String info, String beginMark, String endMark) {
        List<String> messages = new ArrayList<>();
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
        } else {
            logger.warn("unknown message: " + info);
        }
        return messages;
    }

    /**
     * description 
     * @author liuwei
     * @return 博实结协议经纬度计算公司
     * @exception
     * @date 2018/12/24 14:34
     */
    public static double decodeLatOrLon(byte[] latOrLon) {
         int latOrLonInt = ByteUtil.fourBytesToInteger(latOrLon);
         return Double.valueOf(latOrLonInt)/(30000 * 60);
    }

}