package com.zdzc.collector.common;

import ch.qos.logback.core.encoder.ByteArrayUtil;
import com.zdzc.collector.common.coder.MsgDecoder;
import com.zdzc.collector.common.utils.ByteUtil;
import org.junit.Assert;
import org.junit.Test;

import java.text.DecimalFormat;
import java.util.List;

public class MsgDecoderTest {

    @Test
    public void validateChecksumTrue() {
        String str = "7e02000034013900000043000c000000000000000001ceac800727eeee00000000000016060100000230010031010057080000000000000000fc020000fd020000ce7e";
        byte[] arr = ByteArrayUtil.hexStringToByteArray(str);
        arr = MsgDecoder.doReceiveEscape(arr);
        boolean result = MsgDecoder.validateChecksum(arr);
        Assert.assertTrue(result);
    }

    @Test
    public void validateChecksumFalse() {
        String str = "7d0202000034013900000043000c000000000000000001ceac800727e7d01010000000000016060100000230010031010057080000000000000000fc0200007d01020000cc7e";
        byte[] arr = ByteArrayUtil.hexStringToByteArray(str);
        arr = MsgDecoder.doReceiveEscape(arr);
        boolean result = MsgDecoder.validateChecksum(arr);
        Assert.assertFalse(result);
    }

    @Test
    public void doSendEscape() {
        String str = "7e02000034013900000043000c000000000000000001ceac800727eeee00000000000016060100000230010031010057080000000000000000fc020000fd020000ce7e";
        byte[] arr = ByteArrayUtil.hexStringToByteArray(str);
        byte[] arr1 = MsgDecoder.doSendEscape(arr, 0, arr.length -1);
        String str1 = ByteArrayUtil.toHexString(arr1);
        String dst = "7d0202000034013900000043000c000000000000000001ceac800727eeee00000000000016060100000230010031010057080000000000000000fc020000fd020000ce7e";
        Assert.assertEquals(str1, dst);
    }

    @Test
    public void dealPackageSplicing() {
        String str1 = "TRVAP00353456789012345#";
        String str2 = "TRVYP03080524A2232.9806N11404.9355E000.1061830323.8706000908000102000,460,0,9520,3671,6000#";
        List<String> strList = MsgDecoder.dealPackageSplicing(str1+str2, "TRV", "#");
        boolean result1 = (strList.get(0).equals(str1));
        boolean result2 = (strList.get(1).equals(str2));
        Assert.assertTrue(result1 && result2);
    }

    @Test
    public void dealPackageSplicingError() {
        String str = "#";
        List<String> list = MsgDecoder.dealPackageSplicing(str, "TRV", "#");
        Assert.assertEquals(list.size(), 0);
    }

    @Test
    public void decodeLatOrLon() {
        String hex = "026B3F3E";
        byte[] latByte = ByteArrayUtil.hexStringToByteArray(hex);
        double lat = MsgDecoder.decodeLatOrLon(latByte);
        DecimalFormat df = new DecimalFormat("#.0");
        boolean result = df.format(lat * 1000000).contains("22546096");
        Assert.assertTrue(result);
    }

    @Test
    public void test() {
        byte[] arr = {(byte)0x10};
        double speed = ByteUtil.byteToInteger(arr);
        double result = 160;
        Assert.assertEquals(result, speed * 10, 0.01);
    }
}