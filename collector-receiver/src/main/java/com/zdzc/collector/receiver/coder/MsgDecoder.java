package com.zdzc.collector.receiver.coder;

import ch.qos.logback.core.encoder.ByteArrayUtil;
import com.zdzc.collector.receiver.entity.BsjProtocol;

import java.sql.SQLException;

public class MsgDecoder {
    public static BsjProtocol decode (byte[] data) throws SQLException {
        System.out.println(ByteArrayUtil.toHexString(data));
        return BsjMessageDecoder.decode(data);
    }
}
