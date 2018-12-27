package com.zdzc.collector.receiver.Consumer;

import com.zdzc.collector.common.jenum.DataType;
import com.zdzc.collector.common.jenum.ProtocolType;
import com.zdzc.collector.common.jfinal.Config;
import com.zdzc.collector.receiver.entity.BsjProtocol;
import com.zdzc.collector.receiver.util.TbUtil;
import org.apache.commons.lang.time.DateFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class MessageConsumer {

    private static final Logger logger = LoggerFactory.getLogger(MessageConsumer.class);

    //保存设备号和轨迹序列号映射关系
    private static ConcurrentHashMap<String, Integer> seqNoMap = new ConcurrentHashMap<>();

    //保存设备号和报警序列号映射关系
    private static ConcurrentHashMap<String, Integer> alarmNoMap = new ConcurrentHashMap<>();

    //保存主表设备号
    private static List<String> deviceList = new ArrayList<>();

    public static Boolean consume(BsjProtocol protocol, final int index, PreparedStatement pst) throws SQLException {
        int msgType = protocol.getMsgType();
        if (msgType == DataType.Registry.getValue()) {
            //收到登录消息，检测数据库是否有该设备，没有则入库该设备
            return consumeLoginInfo(protocol, pst);
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


    /**
     * 处理登录信息 -> 设备录入
     * @author liuwei
     * @return
     * @exception
     * @date 2018/12/27 10:13
     */
    public static Boolean consumeLoginInfo(BsjProtocol protocol, PreparedStatement pst) {
        String deviceCode = protocol.getDeviceCode();
        //如果内存中已保存设备号，说明主表已录入该设备，不需再查询数据库
        if (deviceList.contains(deviceCode)) {
            return true;
        }
        String mainSql = "SELECT trail_seq_no as seqNo, alarm_seq_no as alarmNo from t_gps_main where device_code = ?";
//        Connection connection = null;
//        PreparedStatement pst = null;
        try {
//            connection = DbConnectionPool.getConnect();
//            pst = connection.prepareStatement(mainSql);
            pst.setString(1, deviceCode);
            ResultSet resultSet = pst.executeQuery();
            if (resultSet.next()) {
                //主表已录入该设备
                int seqNo = resultSet.getInt("seqNo");
                int alarmNo = resultSet.getInt("alarmNo");
                seqNoMap.put(deviceCode, seqNo);
                alarmNoMap.put(deviceCode, alarmNo);
            } else {
                //主表不存在该设备，则录入该设备
                //先查询数据库当前最大序列号
                String selectMaxSeq = "SELECT MAX(trail_seq_no) as seqNo,MAX(alarm_seq_no) as alarmNo from t_gps_main";
                pst.clearParameters();
                ResultSet rs = pst.executeQuery(selectMaxSeq);
                rs.next();
                int maxSeqNo = rs.getInt("seqNo");
                int maxAlarmNo = rs.getInt("alarmNo");
                //计算当前设备录入主表的序列号
                String maxGpsNum = Config.get("gps.db.maxNum");
                int latestSeqNo = TbUtil.createLatestSeqNo(maxSeqNo, maxGpsNum, "gps");
                String maxAlarmNum = Config.get("alarm.db.maxNum");
                int latestAlarmNo = TbUtil.createLatestSeqNo(maxAlarmNo, maxAlarmNum, "alarm");
                //录入主表
                String insertMainSql = "INSERT INTO GPS_MAIN.T_GPS_MAIN(device_code, trail_seq_no, alarm_seq_no, protocol_type, remark, status) values (?, ?, ?, ?, ?, ?)";
//                pst.clearParameters();
                PreparedStatement pstm = pst.getConnection().prepareStatement(insertMainSql);
//                pst = connection.prepareStatement(insertMainSql);
                pstm.setString(1, deviceCode);
                pstm.setInt(2, latestSeqNo);
                pstm.setInt(3, latestAlarmNo);
                pstm.setString(4, ProtocolType.BSJ.getValue());
                String remark = "于" + DateFormatUtils.format(new Date(), "yy年MM月dd日HH时mm分ss秒") + "录入";
                pstm.setString(5, remark);
                pstm.setInt(6, 10);
                pstm.execute();
                seqNoMap.put(deviceCode, latestSeqNo);
                alarmNoMap.put(deviceCode, latestAlarmNo);
                pstm.close();
            }
            deviceList.add(deviceCode);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e.toString());
        } finally {
            /*if (pst != null) {
                try {
                    pst.close();
//                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                    logger.error(e.toString());
                }
            }*/
        }
        return false;
    }
}
