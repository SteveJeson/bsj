package com.zdzc.collector.receiver.coder;

import com.zdzc.collector.receiver.druid.Crud;

import java.sql.ResultSet;
import java.sql.SQLException;

public class MsgDecoder {
    public static void decode (byte[] data) {
        BsjMessageDecoder.decode(data);
        String sql = "select * from gps_main.t_gps_main";
        ResultSet resultSet = Crud.select(sql);
        try {
            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String deviceCode = resultSet.getString("device_code");
                int seqNo = resultSet.getInt("trail_seq_no");
                System.out.println("id -> " + id + ";deviceCode -> " + deviceCode + ";seqNo -> " + seqNo);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
