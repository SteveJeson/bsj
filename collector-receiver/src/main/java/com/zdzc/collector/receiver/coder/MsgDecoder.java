package com.zdzc.collector.receiver.coder;

import com.zdzc.collector.receiver.entity.Protocol;

import java.sql.SQLException;

public class MsgDecoder {
    public static Protocol decode (byte[] data) throws SQLException {
        return BsjMessageDecoder.decode(data);
    }
}
