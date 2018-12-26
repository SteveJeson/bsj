package com.zdzc.collector.receiver.Consumer;

import com.zdzc.collector.common.jenum.DataType;
import com.zdzc.collector.receiver.db.DbConnectionPool;
import com.zdzc.collector.receiver.entity.BsjProtocol;
import com.zdzc.collector.receiver.entity.Protocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class MessageConsumer {

    private static final Logger logger = LoggerFactory.getLogger(MessageConsumer.class);

    private static ConcurrentHashMap<String, Integer> seqNoMap = new ConcurrentHashMap<>();

    private static ConcurrentHashMap<String, Integer> alarmNoMap = new ConcurrentHashMap<>();

    public static void consume(List<Protocol> protocolList) {
        try {
            String sql = "";
            Connection connection = DbConnectionPool.getConnect();
            PreparedStatement pst = connection.prepareStatement(sql);

        } catch (Exception e) {
            logger.error(e.toString());
        }

    }

    public static Boolean consume(BsjProtocol protocol, int index, PreparedStatement pst) throws SQLException {
        int msgType = protocol.getMsgType();
        if (msgType == DataType.Registry.getValue()) {
            //收到登录消息，检测数据库是否有该设备，没有则入库该设备
            return consumeLoginInfo(protocol);
        }
        //填充sql占位数据
        pst.setString(1, protocol.getDeviceCode());
        pst.addBatch();
        if (index % 100 == 0) {
            pst.executeBatch();
        }
        pst.clearBatch();
        return true;
    }

    public static Boolean consumeLoginInfo(BsjProtocol protocol) {
        String sql = "select count(*) from gps_main.t_gps_main where device_code = ?";
        String deviceCode = protocol.getDeviceCode();
        try {
            Connection connection = DbConnectionPool.getConnect();
            PreparedStatement pst = connection.prepareStatement(sql);
            pst.setString(1, deviceCode);
            ResultSet resultSet = pst.executeQuery();
            resultSet.next();
            int row = resultSet.getInt(1);

            //todo 获取轨迹和报警序列号
            String selectMaxSeq = "SELECT MAX(trail_seq_no) as seqNo,MAX(alarm_seq_no) as alarmNo from t_gps_main";
            pst.clearParameters();
            ResultSet rs = pst.executeQuery(selectMaxSeq);
            int seqNo = 0, alarmNo = 0;
            while (rs.next()) {
                seqNo = rs.getInt("seqNo");
                alarmNo = rs.getInt("alarmNo");
                System.out.println("seqNo -> " + seqNo + ", alarmNo -> " + alarmNo);
            }

            if (row > 0) {
                //todo 保存序列号到内存中
                seqNoMap.put(deviceCode, seqNo);
                alarmNoMap.put(deviceCode, alarmNo);
            } else {
                //todo 保存序列号到内存中，并且将当前设备插入主表
                String insertMain = "insert into gps_main.t_gps_main()";
            }
//            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
