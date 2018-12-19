package com.zdzc.collector.receiver.coder;

import ch.qos.logback.core.encoder.ByteArrayUtil;
import com.zdzc.collector.common.jconst.Command;
import com.zdzc.collector.common.utils.ByteUtil;
import org.apache.commons.lang.StringUtils;

public class BsjMessageDecoder {

    public static void decode (byte[] data) {
        String hexStr = ByteArrayUtil.toHexString(data);
        System.out.println("source data -> " + hexStr);

        //协议类型
        byte type = data[0];
        System.out.println("协议类型 -> " + ByteUtil.byteToHex(type));
        //设备号
        byte[] deviceCodeByte = ByteUtil.subByteArr(data, 1, 8);
        System.out.println("设备号 -> " + ByteArrayUtil.toHexString(deviceCodeByte));
        //包长度
        byte[] bodyLen = ByteUtil.subByteArr(data, 9, 1);
        System.out.println("包长度 -> " + ByteArrayUtil.toHexString(bodyLen));
        //协议号
        byte[] msgId = ByteUtil.subByteArr(data, 10, 1);
        String msgIdStr = ByteArrayUtil.toHexString(msgId);
        System.out.println("协议号 -> " + msgIdStr);
        if (StringUtils.equals(msgIdStr, Command.BSJ_MSG_LOGIN)) {
            //登录
            decodeLogin(data);
        } else if (StringUtils.equals(msgIdStr, Command.BSJ_MSG_LOCATION)) {
            //定位
            decodeLocation(data);
        } else if (StringUtils.equals(msgIdStr, Command.BSJ_MSG_ALARM)) {
            //报警
            decodeAlarm(data);
        } else if (StringUtils.equals(msgIdStr, Command.BSJ_MSG_HEARTBEAT)) {
            //心跳
            decodeHeartBeat(data);
        }
    }

    private static void decodeLogin(byte[] data) {
        System.out.println("===收到登录消息===");
        //登录消息
        //设备号
        byte[] teminalPhoneByte = ByteUtil.subByteArr(data, 11, 8);
        System.out.println("设备号 -> " + ByteArrayUtil.toHexString(teminalPhoneByte));
        //类型识别码
        byte[] typeCode = ByteUtil.subByteArr(data, 19, 2);
        System.out.println("类型识别码 -> " + ByteArrayUtil.toHexString(typeCode));
        //时区语言
        byte[] timeZoneLang = ByteUtil.subByteArr(data, 21, 2);
        System.out.println("时区语言 -> " + ByteArrayUtil.toHexString(timeZoneLang));
        //信息序列号
        byte[] infoSeq = ByteUtil.subByteArr(data, 23, 2);
        System.out.println("信息序列号 -> " + ByteArrayUtil.toHexString(infoSeq));
        //错误校验
        byte[] checkCode = ByteUtil.subByteArr(data, 25, 2);
        System.out.println("错误校验 -> " + ByteArrayUtil.toHexString(checkCode));
    }

    private static void decodeLocation(byte[] data) {
        System.out.println("===收到定位消息===");
        //日期时间
        byte[] date = ByteUtil.subByteArr(data, 11, 6);
        System.out.println("日期时间 -> " + ByteArrayUtil.toHexString(date));
        //GPS信息卫星
        byte[] satellite = ByteUtil.subByteArr(data, 17, 1);
        System.out.println("GPS信息卫星 -> " + ByteArrayUtil.toHexString(satellite));
        //纬度
        byte[] lat = ByteUtil.subByteArr(data, 18, 4);
        System.out.println("纬度 -> " + ByteArrayUtil.toHexString(lat));
        //经度
        byte[] lon = ByteUtil.subByteArr(data, 22, 4);
        System.out.println("经度 -> " + ByteArrayUtil.toHexString(lon));
        //速度
        byte[] speed = ByteUtil.subByteArr(data, 26, 1);
        System.out.println("速度 -> " + ByteArrayUtil.toHexString(speed));
        //航向、状态
        byte[] status = ByteUtil.subByteArr(data, 27, 2);
        System.out.println("航向、状态 -> " + ByteArrayUtil.toHexString(status));
        //LBS
        byte[] lbs = ByteUtil.subByteArr(data, 29, 8);
        System.out.println("lbs -> " + ByteArrayUtil.toHexString(lbs));
        //保留位
        byte[] temp = ByteUtil.subByteArr(data, 37, 1);
        System.out.println("保留位 -> " + ByteArrayUtil.toHexString(temp));
        //数据上报模式
        byte[] dataMode = ByteUtil.subByteArr(data, 38, 1);
        System.out.println("数据上报模式 -> " + ByteArrayUtil.toHexString(dataMode));
        //GPS实时补传
        byte[] gpsFill = ByteUtil.subByteArr(data, 39, 1);
        System.out.println("GPS实时补传 -> " + ByteArrayUtil.toHexString(gpsFill));

        int count = data.length - 40 - 4;
        if (count > 0) {
            //附加扩展
            decodeExtenData(data, 40);
        }

        //序列号
        byte[] seq = ByteUtil.subByteArr(data, 40 + count, 2);
        System.out.println("序列号 -> " + ByteArrayUtil.toHexString(seq));
        //错误校验
        byte[] checkCode= ByteUtil.subByteArr(data, 40 + count + 2, 2);
        System.out.println("错误校验 -> " + ByteArrayUtil.toHexString(checkCode));
    }

    private static void decodeAlarm(byte[] data) {
        System.out.println("===收到报警消息");
        //日期时间
        byte[] date = ByteUtil.subByteArr(data, 11, 6);
        System.out.println("日期时间 -> " + ByteArrayUtil.toHexString(date));
        //GPS信息长度+卫星数
        byte[] gpsLenAndSatNum = ByteUtil.subByteArr(data, 17, 1);
        System.out.println("GPS信息长度+卫星数 -> " + ByteArrayUtil.toHexString(gpsLenAndSatNum));
        //纬度
        byte[] lat = ByteUtil.subByteArr(data, 18, 4);
        System.out.println("纬度 -> " + ByteArrayUtil.toHexString(lat));
        //经度
        byte[] lon = ByteUtil.subByteArr(data, 22, 4);
        System.out.println("经度 -> " + ByteArrayUtil.toHexString(lon));
        //速度
        byte[] speed = ByteUtil.subByteArr(data, 26, 1);
        System.out.println("速度 -> " + ByteArrayUtil.toHexString(speed));
        //航向、状态
        byte[] status = ByteUtil.subByteArr(data, 27, 2);
        System.out.println("航向、状态 -> " + ByteArrayUtil.toHexString(status));
        //LBS长度
        byte[] lbsLen = ByteUtil.subByteArr(data, 29, 1);
        System.out.println("LBS长度 -> " + ByteArrayUtil.toHexString(lbsLen));
        //LBS信息
        byte[] lbs = ByteUtil.subByteArr(data, 30, 8);
        System.out.println("lbs -> " + ByteArrayUtil.toHexString(lbs));
        //终端信息内容
        byte[] terminalContent = ByteUtil.subByteArr(data, 38, 1);
        System.out.println("终端信息内容 -> " + ByteArrayUtil.toHexString(terminalContent));
        //电压等级
        byte[] voltageGrade = ByteUtil.subByteArr(data, 39, 1);
        System.out.println("电压等级 -> " + ByteArrayUtil.toHexString(voltageGrade));
        //GPS信号强度
        byte[] wifi = ByteUtil.subByteArr(data, 40, 1);
        System.out.println("GPS信号强度 -> " + ByteArrayUtil.toHexString(wifi));
        //语言
        byte[] language = ByteUtil.subByteArr(data, 41, 2);
        System.out.println("语言 -> " + ByteArrayUtil.toHexString(language));

        int count = data.length - 43 - 4;
        if (count > 0) {
            decodeExtenData(data, 43);
        }

        //序列号
        byte[] seq = ByteUtil.subByteArr(data, 43 + count, 2);
        System.out.println("序列号 -> " + ByteArrayUtil.toHexString(seq));
        //错误校验
        byte[] checkCode= ByteUtil.subByteArr(data, 43 + count + 2, 2);
        System.out.println("错误校验 -> " + ByteArrayUtil.toHexString(checkCode));

    }

    private static void decodeHeartBeat(byte[] data) {
        System.out.println("===收到心跳消息===");
    }

    private static void decodeExtenData(byte[] data, int from) {
        //附加扩展
        for (int i = from;i < data.length;i++) {
            //扩展长度
            int extenLen = ByteUtil.cutBytesToInt(data, i, 2);
            System.out.println("扩展长度 -> " + extenLen);
            //扩展指令
            byte[] extenMsgId = ByteUtil.subByteArr(data, i + 2, 2);
            System.out.println("扩展指令 -> " + ByteArrayUtil.toHexString(extenMsgId));
            //扩展数据
            byte[] extenData = ByteUtil.subByteArr(data, i + 4, extenLen - 4);
            System.out.println("扩展数据 -> " +ByteArrayUtil.toHexString(extenData));
        }
    }
}
