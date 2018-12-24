package com.zdzc.collector.receiver.coder;

import ch.qos.logback.core.encoder.ByteArrayUtil;
import com.sun.prism.shader.Solid_TextureRGB_AlphaTest_Loader;
import com.zdzc.collector.common.coder.MsgDecoder;
import com.zdzc.collector.common.jconst.Command;
import com.zdzc.collector.common.utils.ByteUtil;
import com.zdzc.collector.common.utils.DateUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.commons.lang.time.DateUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

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
        byte[] dateBytes = ByteUtil.subByteArr(data, 11, 6);
        Date date = decodeDateTime(dateBytes);
        String dateStr = DateFormatUtils.format(date, "20yy年MM月dd日HH时mm分ss秒");
        System.out.println("日期时间 -> " + dateStr);
        //GPS信息卫星
        byte[] satNumBytes = ByteUtil.subByteArr(data, 17, 1);
        int satNum = decodeSatelliteNum(satNumBytes);
        System.out.println("GPS定位卫星数 -> " + satNum);
        //纬度
        byte[] lat = ByteUtil.subByteArr(data, 18, 4);
        double latDouble = MsgDecoder.decodeLatOrLon(lat) * 1000000;
        System.out.println("纬度 -> " + latDouble);
        //经度
        byte[] lon = ByteUtil.subByteArr(data, 22, 4);
        double lonDouble = MsgDecoder.decodeLatOrLon(lon) * 1000000;
        System.out.println("经度 -> " + lonDouble);
        //速度
        byte[] speed = ByteUtil.subByteArr(data, 26, 1);
        double speedDouble = ByteUtil.byteToInteger(speed) * 10;
        System.out.println("速度 -> " + speedDouble);
        //航向、状态
        byte[] directionByte = ByteUtil.subByteArr(data, 27, 2);
        int directionInt = decodeDirection(directionByte);
        System.out.println("航向、状态(方向) -> " + directionInt);
        //GPS实时补传
        int gpsFill = ByteUtil.cutBytesToInt(data, 39, 1);
        System.out.println("GPS实时补传 -> " + gpsFill);

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
        byte[] dateBytes = ByteUtil.subByteArr(data, 11, 6);
        Date date = decodeDateTime(dateBytes);
        String dateStr = DateFormatUtils.format(date, "20yy年MM月dd日HH时mm分ss秒");
        System.out.println("日期时间 -> " + dateStr);
        //GPS信息长度+卫星数
        byte[] satNumBytes = ByteUtil.subByteArr(data, 17, 1);
        int satNum = decodeSatelliteNum(satNumBytes);
        System.out.println("GPS定位卫星数 -> " + satNum);
        //纬度
        byte[] lat = ByteUtil.subByteArr(data, 18, 4);
        double latDouble = MsgDecoder.decodeLatOrLon(lat) * 1000000;
        System.out.println("纬度 -> " + latDouble);
        //经度
        byte[] lon = ByteUtil.subByteArr(data, 22, 4);
        double lonDouble = MsgDecoder.decodeLatOrLon(lon) * 1000000;
        System.out.println("经度 -> " + lonDouble);
        //速度
        byte[] speed = ByteUtil.subByteArr(data, 26, 1);
        double speedDouble = ByteUtil.byteToInteger(speed) * 10;
        System.out.println("速度 -> " + speedDouble);
        //航向、状态 -> 方向角
        byte[] directionByte = ByteUtil.subByteArr(data, 27, 2);
        int directionInt = decodeDirection(directionByte);
        System.out.println("航向、状态(方向) -> " + directionInt);
        //电压等级
        int voltageGrade = ByteUtil.cutBytesToInt(data, 39, 1);
        System.out.println("电压等级 -> " + voltageGrade);
        //GPS信号强度
        int wifi = ByteUtil.cutBytesToInt(data, 40, 1);
        System.out.println("GPS信号强度等级 -> " + wifi);

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
        //电压等级
        int voltageGrade = ByteUtil.cutBytesToInt(data, 12, 1);
        System.out.println("电压等级 -> " + voltageGrade);
        //GSM信号强度
        int wifi = ByteUtil.cutBytesToInt(data, 13, 1);
        System.out.println("GSM信号强度等级 -> " + wifi);
        //附加扩展
        int count = data.length - 16 - 4;
        if (count > 0) {
            decodeExtenData(data, 16);
        }
        //序列号
        byte[] seq = ByteUtil.subByteArr(data, 16 + count, 2);
        System.out.println("序列号 -> " + ByteArrayUtil.toHexString(seq));
        //错误校验
        byte[] checkCode= ByteUtil.subByteArr(data, 16 + count + 2, 2);
        System.out.println("错误校验 -> " + ByteArrayUtil.toHexString(checkCode));
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
            byte[] extenData = ByteUtil.subByteArr(data, i + 4, extenLen - extenMsgId.length);
            System.out.println("扩展数据 -> " +ByteArrayUtil.toHexString(extenData));
        }
    }

    /**
     * 解析方向角 0~360 正北为0 顺时针
     * @author liuwei
     * @return
     * @exception
     * @date 2018/12/24 15:57
     */
    private static int decodeDirection(byte[] hexBytes) {
        String hexStr = ByteArrayUtil.toHexString(hexBytes);
        String biStr = ByteUtil.hexStrToBinary16(hexStr);
        String reverse = StringUtils.reverse(biStr);
        String subStr = reverse.substring(0, 10);
        int direction = Integer.parseInt(StringUtils.reverse(subStr), 2);
        return direction;
    }

    /**
     * 解析定位卫星数量
     * @author liuwei
     * @return
     * @exception
     * @date 2018/12/24 16:25
     */
    private static int decodeSatelliteNum(byte[] hexBytes) {
        String srcStr = ByteArrayUtil.toHexString(hexBytes);
        char[] chars = srcStr.toCharArray();
        char second = chars[1];
        int satelliteNum = Integer.parseInt(String.valueOf(second), 16);
        return satelliteNum;
    }

    public static Date decodeDateTime(byte[] hexBytes) {
        if (hexBytes.length < 6) {
            return null;
        }
        byte yearByte = hexBytes[0];
        byte monthByte = hexBytes[1];
        byte dayByte = hexBytes[2];
        byte hourByte = hexBytes[3];
        byte minuteByte = hexBytes[4];
        byte secondByte = hexBytes[5];

        int year = ByteUtil.oneByteToInteger(yearByte);
        int month = ByteUtil.oneByteToInteger(monthByte);
        int day = ByteUtil.oneByteToInteger(dayByte);
        int hour = ByteUtil.oneByteToInteger(hourByte);
        int minute = ByteUtil.oneByteToInteger(minuteByte);
        int second = ByteUtil.oneByteToInteger(secondByte);

        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month-1, day, hour, minute, second);
        Date date = calendar.getTime();
        return date;
    }

}
