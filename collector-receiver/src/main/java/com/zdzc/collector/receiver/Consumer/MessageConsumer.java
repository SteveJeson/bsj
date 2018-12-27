package com.zdzc.collector.receiver.Consumer;

import com.sun.xml.internal.ws.api.pipe.ServerTubeAssemblerContext;
import com.zdzc.collector.common.jenum.DataType;
import com.zdzc.collector.common.jenum.ProtocolType;
import com.zdzc.collector.common.jfinal.Config;
import com.zdzc.collector.receiver.db.DbConnectionPool;
import com.zdzc.collector.receiver.entity.BsjProtocol;
import com.zdzc.collector.receiver.entity.Protocol;
import com.zdzc.collector.receiver.util.TbUtil;
import org.apache.commons.lang.time.DateFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.crypto.Data;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class MessageConsumer {

    private static final Logger logger = LoggerFactory.getLogger(MessageConsumer.class);

    //保存设备号和轨迹序列号映射关系
    private static ConcurrentHashMap<String, Integer> seqNoMap = new ConcurrentHashMap<>();

    //保存设备号和报警序列号映射关系
    private static ConcurrentHashMap<String, Integer> alarmNoMap = new ConcurrentHashMap<>();

    private static String gpsDbPrefix = Config.get("gps.database.prefix");

    private static String alarmDbPrefix = Config.get("alarm.database.prefix");

    private static String gpsTbPrefix = Config.get("gps.table.prefix");

    private static String alarmTbPrefix = Config.get("alarm.table.prefix");

    private static int gpsBatchNum = Config.getInt("gps.insert.batch");

    private static int alarmBatchNum = Config.getInt("alarm.insert.batch");

    private static String gpsColumns = Config.get("gps.table.columns");

    //保存主表设备号
    private static List<String> deviceList = new ArrayList<>();

    public static Boolean consume(BsjProtocol protocol, final int index, ConcurrentHashMap<String, List<BsjProtocol>> mapList)  {
        int msgType = protocol.getMsgType();
        if (msgType == DataType.Registry.getValue()) {
            //收到登录消息，检测数据库是否有该设备，没有则入库该设备
            return consumeLoginInfo(protocol);
        }

        if (msgType == DataType.GPS.getValue() || msgType == DataType.ALARM.getValue()) {
            //位置、报警信息
            Map<String, String> map = getTbPath(protocol);
            String tbPath = map.get("tbPath");
            if (tbPath == null) {
                return false;
            }
            if (mapList.containsKey(tbPath)) {
                mapList.get(tbPath).add(protocol);
            } else {
                List<BsjProtocol> list = new ArrayList<>();
                mapList.put(tbPath, list);
            }

            if (msgType == DataType.GPS.getValue()) {
                toGpsBatchConsumer(mapList);
            }

        }

        return false;
    }

    public static Boolean toGpsBatchConsumer(ConcurrentHashMap<String, List<BsjProtocol>> mapList) {
        if (mapList.size() == gpsBatchNum) {
            try {

                for (Map.Entry<String, List<BsjProtocol>> entry : mapList.entrySet()) {
                    String tbPath = entry.getKey();
                    List<BsjProtocol> protocols = entry.getValue();
                    int strLen = gpsColumns.split(",").length;
                    //占位符
                    StringBuilder builder = new StringBuilder();
                    for (int i = 0;i < strLen;i++) {
                        builder.append("?");
                        if (i < strLen -1) {
                            builder.append(",");
                        }
                    }
                    String sql = "INSERT INTO " + tbPath + "("+gpsColumns+")values("+builder+")";
                    Connection connection = DbConnectionPool.getConnect();
                    PreparedStatement pst = connection.prepareStatement(sql);
                    for(BsjProtocol protocol : protocols){
                        pst.setString(1, protocol.getDeviceCode());
                        pst.setInt(2, protocol.getAlarmStatus());
                        pst.setInt(3, protocol.getVehicleStatus());
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                logger.error(e.toString());
            }

        }

        return false;
    }

    /**
     * 获取设备的表路径
     * @author liuwei
     * @return
     * @exception
     * @date 2018/12/27 15:22
     */
    public static Map<String, String> getTbPath(BsjProtocol protocol) {
        String deviceCode = protocol.getDeviceCode();
        int msgType = protocol.getMsgType();
        doSetSeqNo(deviceCode);
        Integer seqNo = seqNoMap.get(deviceCode);
        Integer alarmNo = alarmNoMap.get(deviceCode);
        if (seqNo == null || alarmNo == null) {
            return null;
        }
        Map<String, String> map = new HashMap<>();
        String dbName = "", tbName = "";
        if (msgType == DataType.GPS.getValue()) {
             dbName = TbUtil.getDbName(gpsDbPrefix, seqNo, "gps");
             tbName = TbUtil.getTableName(protocol.getDateTime(), gpsTbPrefix, seqNo, "gps");
        } else if (msgType == DataType.ALARM.getValue()) {
             dbName = TbUtil.getDbName(alarmDbPrefix, alarmNo, "alarm");
             tbName = TbUtil.getTableName(protocol.getDateTime(), alarmTbPrefix, alarmNo, "alarm");
        }

        String tbPath = dbName + "." + tbName;
        map.put("tbPath", tbPath);
        return map;
    }



    /**
     * 处理登录信息 -> 设备录入
     * @author liuwei
     * @return
     * @exception
     * @date 2018/12/27 10:13
     */
    public static Boolean consumeLoginInfo(BsjProtocol protocol) {
        String deviceCode = protocol.getDeviceCode();
        //如果内存中已保存设备号，说明主表已录入该设备，不需再查询数据库
        if (deviceList.contains(deviceCode)) {
            return true;
        }
        String mainSql = "SELECT trail_seq_no as seqNo, alarm_seq_no as alarmNo from gps_main.t_gps_main where device_code = ?";
        Connection connection = null;
        PreparedStatement pst = null;
        try {
            connection = DbConnectionPool.getConnect();
            pst = connection.prepareStatement(mainSql);
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
            closeConnection(pst);
        }
        return false;
    }

    /**
     * 查询序列号
     * @author liuwei
     * @return
     * @exception
     * @date 2018/12/27 14:25
     */
    public static void doSetSeqNo(String deviceCode) {
        Integer seqNo = seqNoMap.get(deviceCode);
        Integer alarmNo = alarmNoMap.get(deviceCode);
        if (seqNo != null && alarmNo != null) {
            return;
        }
        String mainSql = "SELECT trail_seq_no as seqNo, alarm_seq_no as alarmNo from gps_main.t_gps_main where device_code = ?";
        Connection connection = null;
        PreparedStatement pst = null;
        try {
            connection = DbConnectionPool.getConnect();
            pst = connection.prepareStatement(mainSql);
            pst.setString(1, deviceCode);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                 seqNo = rs.getInt("seqNo");
                 alarmNo = rs.getInt("alarmNo");
                 seqNoMap.put(deviceCode, seqNo);
                 alarmNoMap.put(deviceCode, alarmNo);
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e.toString());
        } finally {
            closeConnection(pst);
        }

    }

    /**
     * 关闭数据库连接
     * @author liuwei
     * @return
     * @exception
     * @date 2018/12/27 14:24
     */
    private static void closeConnection(PreparedStatement pst) {
        if (pst != null) {
            try {
                pst.close();
                pst.getConnection().close();
            } catch (SQLException e) {
                e.printStackTrace();
                logger.error(e.toString());
            }
        }
    }
}
