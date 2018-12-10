package com.zdzc.collector.common.utils;

import io.netty.util.CharsetUtil;

public class ByteUtil {

    /**
     * 截取byte数组并转化成int类型
     * @param data
     * @param from
     * @param len
     * @return
     */
    public static int cutBytesToInt(byte[] data, int from, int len){
        byte[] buffer = new byte[len];
        System.arraycopy(data, from, buffer, 0, len);
        return byteToInteger(buffer);
    }

    /**
     * 将byte类型转化为int类型
     * @param value
     * @return
     */
    public static int byteToInteger(byte[] value)
    {
        int result;
        if (value.length == 1)
        {
            result = oneByteToInteger(value[0]);
        }
        else if (value.length == 2)
        {
            result = twoBytesToInteger(value);
        }
        else if (value.length == 3)
        {
            result = threeBytesToInteger(value);
        }
        else if (value.length == 4)
        {
            result = fourBytesToInteger(value);
        }
        else
        {
            result = fourBytesToInteger(value);
        }
        return result;
    }

    /**
     * 把一个byte转化位整形,通常为指令用
     * @param value
     * @return
     */
    public static int oneByteToInteger(byte value)
    {
        return (int)value & 0xFF;
    }


    /**
     * 把一个2位的数组转化位整形
     * @param value
     * @return
     */
    public static int twoBytesToInteger(byte[] value)
    {
        int temp0 = value[0] & 0xFF;
        int temp1 = value[1] & 0xFF;
        return ((temp0 << 8) + temp1);
    }

    /**
     * 把一个3位的数组转化位整形
     * @param value
     * @return
     */
    public static int threeBytesToInteger(byte[] value)
    {
        int temp0 = value[0] & 0xFF;
        int temp1 = value[1] & 0xFF;
        int temp2 = value[2] & 0xFF;
        return ((temp0 << 16) + (temp1 << 8) + temp2);
    }

    /**
     * 把一个4位的数组转化位整形,通常为指令用
     * @param value
     * @return
     */
    public static int fourBytesToInteger(byte[] value)
    {
        int temp0 = value[0] & 0xFF;
        int temp1 = value[1] & 0xFF;
        int temp2 = value[2] & 0xFF;
        int temp3 = value[3] & 0xFF;
        return ((temp0 << 24) + (temp1 << 16) + (temp2 << 8) + temp3);
    }

    /**
     * 截取byte数组
     * @param src
     * @param offset
     * @param length
     * @return
     */
    public static byte[] subByteArr(byte[] src, int offset, int length) {
        byte[] dest = new byte[length];
        System.arraycopy(src, offset, dest, 0, length);
        return dest;
    }

    /**
     * 把一个整形该为1位的byte数组
     *
     * @param value
     * @return
     * @throws Exception
     */
    public static byte[] integerTo1Bytes(int value)
    {
        byte[] result = new byte[1];
        result[0] = (byte)(value & 0xFF);
        return result;
    }

    /**
     * 把一个整形改为2位的byte数组
     *
     * @param value
     * @return
     * @throws Exception
     */
    public static byte[] integerTo2Bytes(int value)
    {
        byte[] result = new byte[2];
        result[0] = (byte)((value >> 8) & 0xFF);
        result[1] = (byte)(value & 0xFF);
        return result;
    }

    /**
     * 字符串==>BCD字节数组
     *
     * @param str
     * @return BCD字节数组
     */
    public static byte[] string2Bcd(String str)
    {
        // 奇数,前补零
        if ((str.length() & 0x1) == 1)
        {
            str = "0" + str;
        }

        byte[] ret = new byte[str.length() / 2];
        byte[] bs = str.getBytes(CharsetUtil.UTF_8);
        for (int i = 0; i < ret.length; i++)
        {
            byte high = ascII2Bcd(bs[2 * i]);
            byte low = ascII2Bcd(bs[2 * i + 1]);

            // TODO 只遮罩BCD低四位?
            ret[i] = (byte)((high << 4) | low);
        }
        return ret;
    }

    private static byte ascII2Bcd(byte asc)
    {
        if ((asc >= '0') && (asc <= '9'))
            return (byte)(asc - '0');
        else if ((asc >= 'A') && (asc <= 'F'))
            return (byte)(asc - 'A' + 10);
        else if ((asc >= 'a') && (asc <= 'f'))
            return (byte)(asc - 'a' + 10);
        else
            return (byte)(asc - 48);
    }

    /**
     * 合并byte数组
     * @param bt1
     * @param bt2
     * @return
     */
    public static byte[] bytesMerge(byte[] bt1, byte[] bt2){
        byte[] bt3 = new byte[bt1.length+bt2.length];
        System.arraycopy(bt1, 0, bt3, 0, bt1.length);
        System.arraycopy(bt2, 0, bt3, bt1.length, bt2.length);
        return bt3;
    }


    /**
     * 字节数组转16进制
     * @param bytes 需要转换的byte数组
     * @return  转换后的Hex字符串
     */
    public static String bytesToHex(byte[] bytes) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < bytes.length; i++) {
            String hex = Integer.toHexString(bytes[i] & 0xFF);
            if (hex.length() < 2) {
                sb.append(0);
            }
            sb.append(hex);
        }
        return sb.toString();
    }
}
