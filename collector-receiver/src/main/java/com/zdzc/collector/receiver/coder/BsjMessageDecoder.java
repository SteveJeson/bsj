package com.zdzc.collector.receiver.coder;

import ch.qos.logback.core.encoder.ByteArrayUtil;
import com.zdzc.collector.common.coder.MsgDecoder;
import com.zdzc.collector.common.jconst.Command;
import com.zdzc.collector.common.jenum.DataType;
import com.zdzc.collector.common.jenum.ProtocolType;
import com.zdzc.collector.common.utils.ByteUtil;
import com.zdzc.collector.receiver.entity.BsjProtocol;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateFormatUtils;

import java.util.Calendar;
import java.util.Date;

/**
 * 博实结协议解析类
 * @Author liuwei
 * @Description
 * @Date 2018/12/25 10:30
 */
public class BsjMessageDecoder {

    /**
     * 解析入口方法
     * @author liuwei
     * @return
     * @exception
     * @date 2018/12/25 10:30
     */
    public static BsjProtocol decode (byte[] data) {
        BsjProtocol protocol = new BsjProtocol();
        String hexStr = ByteArrayUtil.toHexString(data);
//        System.out.println("source data -> " + hexStr);

        //协议类型
        byte type = data[0];
//        System.out.println("协议类型 -> " + ByteUtil.byteToHex(type));
        protocol.setProtocolType(ProtocolType.BSJ.getValue());
        //设备号
        byte[] deviceCodeByte = ByteUtil.subByteArr(data, 1, 8);
        String deviceCodeStr = ByteArrayUtil.toHexString(deviceCodeByte);
//        System.out.println("设备号 -> " + deviceCodeStr);
        protocol.setDeviceCode(deviceCodeStr);
        //包长度
        byte[] bodyLen = ByteUtil.subByteArr(data, 9, 1);
//        System.out.println("包长度 -> " + ByteArrayUtil.toHexString(bodyLen));
        //协议号
        byte[] msgId = ByteUtil.subByteArr(data, 10, 1);
        String msgIdStr = ByteArrayUtil.toHexString(msgId);
//        System.out.println("协议号 -> " + msgIdStr);
        if (StringUtils.equals(msgIdStr, Command.BSJ_MSG_LOGIN)) {
            //登录
            decodeLogin(protocol, data);
        } else if (StringUtils.equals(msgIdStr, Command.BSJ_MSG_LOCATION)) {
            //定位
            decodeLocation(protocol, data);
        } else if (StringUtils.equals(msgIdStr, Command.BSJ_MSG_ALARM)) {
            //报警
            decodeAlarm(protocol, data);
        } else if (StringUtils.equals(msgIdStr, Command.BSJ_MSG_HEARTBEAT)) {
            //心跳
            decodeHeartBeat(protocol, data);
        }

        return protocol;
    }

    /**
     * 解析登录信息
     * @author liuwei
     * @return
     * @exception
     * @date 2018/12/25 10:31
     */
    private static void decodeLogin(BsjProtocol protocol, byte[] data) {
//        System.out.println("===收到登录消息===");
        //登录消息
        //设备号
        byte[] teminalPhoneByte = ByteUtil.subByteArr(data, 11, 8);
//        System.out.println("设备号 -> " + ByteArrayUtil.toHexString(teminalPhoneByte));
        //类型识别码
        byte[] typeCode = ByteUtil.subByteArr(data, 19, 2);
//        System.out.println("类型识别码 -> " + ByteArrayUtil.toHexString(typeCode));
        //时区语言
        byte[] timeZoneLang = ByteUtil.subByteArr(data, 21, 2);
//        System.out.println("时区语言 -> " + ByteArrayUtil.toHexString(timeZoneLang));
        //信息序列号
        byte[] infoSeq = ByteUtil.subByteArr(data, 23, 2);
//        System.out.println("信息序列号 -> " + ByteArrayUtil.toHexString(infoSeq));
        //错误校验
        byte[] checkCode = ByteUtil.subByteArr(data, 25, 2);
//        System.out.println("错误校验 -> " + ByteArrayUtil.toHexString(checkCode));
        protocol.setMsgType(DataType.Registry.getValue());
    }

    /**
     * 解析位置信息
     * @author liuwei
     * @return
     * @exception
     * @date 2018/12/25 10:31
     */
    private static void decodeLocation(BsjProtocol protocol, byte[] data) {
        protocol.setMsgType(DataType.GPS.getValue());
//        System.out.println("===收到定位消息===");
        //日期时间
        byte[] dateBytes = ByteUtil.subByteArr(data, 11, 6);
        Date date = decodeDateTime(dateBytes);
        String dateStr = DateFormatUtils.format(date, "yyMMddHHmmss");
//        System.out.println("日期时间 -> " + dateStr);
        protocol.setDateTime(date);
        //GPS信息卫星
        byte[] satNumBytes = ByteUtil.subByteArr(data, 17, 1);
        int satNum = decodeSatelliteNum(satNumBytes);
//        System.out.println("GPS定位卫星数 -> " + satNum);
        protocol.setSatelliteNum(satNum);
        //纬度
        byte[] lat = ByteUtil.subByteArr(data, 18, 4);
        double latDouble = MsgDecoder.decodeLatOrLon(lat) * 1000000;
//        System.out.println("纬度 -> " + latDouble);
        protocol.setLat(latDouble);
        //经度
        byte[] lon = ByteUtil.subByteArr(data, 22, 4);
        double lonDouble = MsgDecoder.decodeLatOrLon(lon) * 1000000;
//        System.out.println("经度 -> " + lonDouble);
        protocol.setLon(lonDouble);
        //速度
        byte[] speed = ByteUtil.subByteArr(data, 26, 1);
        double speedDouble = ByteUtil.byteToInteger(speed) * 10;
//        System.out.println("速度 -> " + speedDouble);
        protocol.setSpeed(speedDouble);
        //航向、状态
        byte[] directionByte = ByteUtil.subByteArr(data, 27, 2);
        int directionInt = decodeDirection(directionByte);
//        System.out.println("航向、状态(方向) -> " + directionInt);
        protocol.setDirection(directionInt);
        //GPS实时补传
        int gpsFill = ByteUtil.cutBytesToInt(data, 39, 1);
//        System.out.println("GPS实时补传 -> " + gpsFill);
        protocol.setGpsFill(gpsFill);
        int count = data.length - 40 - 4;
        if (count > 0) {
            //附加扩展
            decodeExtenData(protocol, data, 40);
        }

        //序列号
        byte[] seq = ByteUtil.subByteArr(data, 40 + count, 2);
//        System.out.println("序列号 -> " + ByteArrayUtil.toHexString(seq));
        //错误校验
        byte[] checkCode= ByteUtil.subByteArr(data, 40 + count + 2, 2);
//        System.out.println("错误校验 -> " + ByteArrayUtil.toHexString(checkCode));
    }

    /**
     * 解析报警信息
     * @author liuwei
     * @return
     * @exception
     * @date 2018/12/25 10:31
     */
    private static void decodeAlarm(BsjProtocol protocol, byte[] data) {
        protocol.setMsgType(DataType.ALARM.getValue());
//        System.out.println("===收到报警消息");
        //日期时间
        byte[] dateBytes = ByteUtil.subByteArr(data, 11, 6);
        Date date = decodeDateTime(dateBytes);
        String dateStr = DateFormatUtils.format(date, "yyMMddHHmmss");
//        System.out.println("日期时间 -> " + dateStr);
        protocol.setDateTime(date);
        //GPS信息长度+卫星数
        byte[] satNumBytes = ByteUtil.subByteArr(data, 17, 1);
        int satNum = decodeSatelliteNum(satNumBytes);
//        System.out.println("GPS定位卫星数 -> " + satNum);
        protocol.setSatelliteNum(satNum);
        //纬度
        byte[] lat = ByteUtil.subByteArr(data, 18, 4);
        double latDouble = MsgDecoder.decodeLatOrLon(lat) * 1000000;
//        System.out.println("纬度 -> " + latDouble);
        protocol.setLat(latDouble);
        //经度
        byte[] lon = ByteUtil.subByteArr(data, 22, 4);
        double lonDouble = MsgDecoder.decodeLatOrLon(lon) * 1000000;
//        System.out.println("经度 -> " + lonDouble);
        protocol.setLon(lonDouble);
        //速度
        byte[] speed = ByteUtil.subByteArr(data, 26, 1);
        double speedDouble = ByteUtil.byteToInteger(speed) * 10;
//        System.out.println("速度 -> " + speedDouble);
        protocol.setSpeed(speedDouble);
        //航向、状态 -> 方向角
        byte[] directionByte = ByteUtil.subByteArr(data, 27, 2);
        int directionInt = decodeDirection(directionByte);
//        System.out.println("航向、状态(方向) -> " + directionInt);
        protocol.setDirection(directionInt);
        //电压等级
        int voltageLevel = ByteUtil.cutBytesToInt(data, 39, 1);
//        System.out.println("电压等级 -> " + voltageLevel);
        protocol.setVoltageLevel(voltageLevel);
        //GPS信号强度等级
        int signLevel = ByteUtil.cutBytesToInt(data, 40, 1);
//        System.out.println("GPS信号强度等级 -> " + signLevel);
        protocol.setSignLevel(signLevel);

        int count = data.length - 43 - 4;
        if (count > 0) {
            decodeExtenData(protocol, data, 43);
        }

        //序列号
        byte[] seq = ByteUtil.subByteArr(data, 43 + count, 2);
//        System.out.println("序列号 -> " + ByteArrayUtil.toHexString(seq));
        //错误校验
        byte[] checkCode= ByteUtil.subByteArr(data, 43 + count + 2, 2);
//        System.out.println("错误校验 -> " + ByteArrayUtil.toHexString(checkCode));

    }

    /**
     * 解析心跳信息
     * @author liuwei
     * @return
     * @exception
     * @date 2018/12/25 10:31
     */
    private static void decodeHeartBeat(BsjProtocol protocol, byte[] data) {
        protocol.setMsgType(DataType.HEARTBEAT.getValue());
//        System.out.println("===收到心跳消息===");
        //电压等级
        int voltageLevel = ByteUtil.cutBytesToInt(data, 12, 1);
//        System.out.println("电压等级 -> " + voltageLevel);
        protocol.setVoltageLevel(voltageLevel);
        //GSM信号强度
        int signLevel = ByteUtil.cutBytesToInt(data, 13, 1);
//        System.out.println("GSM信号强度等级 -> " + signLevel);
        protocol.setSignLevel(signLevel);
        //附加扩展
        int count = data.length - 16 - 4;
        if (count > 0) {
            decodeExtenData(protocol, data, 16);
        }
        //序列号
        byte[] seq = ByteUtil.subByteArr(data, 16 + count, 2);
//        System.out.println("序列号 -> " + ByteArrayUtil.toHexString(seq));
        //错误校验
        byte[] checkCode= ByteUtil.subByteArr(data, 16 + count + 2, 2);
//        System.out.println("错误校验 -> " + ByteArrayUtil.toHexString(checkCode));
    }

    /**
     * 解析扩展数据
     * @author liuwei
     * @return
     * @exception
     * @date 2018/12/25 13:55
     */
    private static void decodeExtenData(BsjProtocol protocol, byte[] data, int from) {
        //附加扩展
        for (int i = from;i < data.length;i++) {
            //扩展长度
            int extenLen = ByteUtil.cutBytesToInt(data, i, 2);
//            System.out.println("扩展长度 -> " + extenLen);
            //扩展指令
            byte[] extenMsgId = ByteUtil.subByteArr(data, i + 2, 2);
//            System.out.println("扩展指令 -> " + ByteArrayUtil.toHexString(extenMsgId));
            //扩展数据
            byte[] extenData = ByteUtil.subByteArr(data, i + 4, extenLen - extenMsgId.length);
//            System.out.println("扩展数据 -> " +ByteArrayUtil.toHexString(extenData));
            int extenMsgIdInt = ByteUtil.byteToInteger(extenMsgId);
            if (extenMsgIdInt == Command.BSJ_EXTENTION_ICCID) {
                //ICCID
                String iccid = ByteArrayUtil.toHexString(extenData);
//                System.out.println("ICCID -> " + iccid);
                protocol.setIccid(iccid);
            } else if (extenMsgIdInt == Command.BSJ_EXTENTION_STATUS_INFO) {
                //状态信息
                int statusInfo = ByteUtil.byteToInteger(extenData);
//                System.out.println("状态信息 -> " + statusInfo);
                protocol.setVehicleStatus(statusInfo);
            } else if (extenMsgIdInt == Command.BSJ_EXTENTION_ALARM_INFO) {
                //报警信息
                int alarmInfo = ByteUtil.byteToInteger(extenData);
//                System.out.println("报警信息 -> " + alarmInfo);
                protocol.setAlarmStatus(alarmInfo);
            } else if (extenMsgIdInt == Command.BSJ_EXTENTION_MILE_INFO) {
                //里程信息
                int mile = ByteUtil.cutBytesToInt(extenData, 0, 4);
                int miles = ByteUtil.cutBytesToInt(extenData, 4, 4);
//                System.out.println("当日里程 -> " + mile);
//                System.out.println("总里程 -> " + miles);
                protocol.setMile(mile);
                protocol.setMiles(miles);
            } else if (extenMsgIdInt == Command.BSJ_EXTENTION_VOLTAGE_INFO) {
                //主电源电压
                int voltage = ByteUtil.byteToInteger(extenData);
//                System.out.println("主电源电压 -> " + voltage);
                protocol.setVoltage(voltage);
            }

            i = i + extenLen + 2;
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

    /**
     * 解析日期时间
     * @author liuwei
     * @return
     * @exception
     * @date 2018/12/25 10:32
     */
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
        calendar.set(year + 2000, month-1, day, hour, minute, second);
        Date date = calendar.getTime();
        return date;
    }

}
