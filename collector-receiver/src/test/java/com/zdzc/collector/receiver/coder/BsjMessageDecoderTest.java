package com.zdzc.collector.receiver.coder;

import ch.qos.logback.core.encoder.ByteArrayUtil;
import org.junit.Test;

import static org.junit.Assert.*;

public class BsjMessageDecoderTest {

    @Test
    public void decode() {
        String msg = "7878222211021B06010DC5026DDEC00C3BFEE625140001CC00262C000EBA00000000062B7D0D0A";
        byte[] arr = ByteArrayUtil.hexStringToByteArray(msg);
        BsjMessageDecoder.decode(arr);
    }
}