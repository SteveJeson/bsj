package com.zdzc.collector.receiver.coder;

import ch.qos.logback.core.encoder.ByteArrayUtil;
import com.zdzc.collector.common.utils.ByteUtil;
import com.zdzc.collector.common.utils.DateUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.commons.lang.time.DateUtils;
import org.junit.Assert;
import org.junit.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static org.junit.Assert.*;

public class BsjMessageDecoderTest {

    @Test
    public void decode() {
        String msg = "7878222211021B06010DC5026DDEC00C3BFEE625140001CC00262C000EBA00000000062B7D0D0A";
        byte[] arr = ByteArrayUtil.hexStringToByteArray(msg);
        BsjMessageDecoder.decode(arr);
    }


    @Test
    public void testHexToBinary() {
        byte[] hexBytes = {0x15, (byte)0x4C};
        String hexStr = ByteArrayUtil.toHexString(hexBytes);
        String biStr = ByteUtil.hexStrToBinary16(hexStr);
        System.out.println(biStr);
        String reverse = StringUtils.reverse(biStr);
        String subStr = reverse.substring(0, 10);
        System.out.println("sub -> " +subStr);
        int actual = Integer.parseInt(StringUtils.reverse(subStr), 2);
        System.out.println(actual);
        int expected = 332;
        Assert.assertEquals("", expected, actual);
    }

    @Test
    public void testHexSub() {
        byte[] src = {(byte)0xCF};
        String srcStr = ByteArrayUtil.toHexString(src);
        System.out.println(srcStr);
        char[] chars = srcStr.toCharArray();
        char first = chars[0];
        char second = chars[1];
        System.out.println(first);
        System.out.println(second);
        int gpsLen = Integer.parseInt(String.valueOf(first), 16);
        int satelliteNum = Integer.parseInt(String.valueOf(second), 16);
        System.out.println(gpsLen);
        System.out.println(satelliteNum);
    }

    @Test
    public void decodeDateTime() throws ParseException {
        byte[] src = {0x09, 0X03, 0X17, 0X0F, 0X32, 0X17};
        Date date = BsjMessageDecoder.decodeDateTime(src);
        String dateStr = DateFormatUtils.format(date, "20yy年MM月dd日HH时mm分ss秒");
        System.out.println(dateStr);

        int a = 0x00B2;
        byte[] arr = {0x00, (byte)0xB2};
        int b = ByteUtil.byteToInteger(arr);
        System.out.println("a -> " + a);
        System.out.println("b -> " + b);
        System.out.println(a==b);
    }
}