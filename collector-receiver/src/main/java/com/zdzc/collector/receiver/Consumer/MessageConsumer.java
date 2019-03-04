package com.zdzc.collector.receiver.Consumer;

import com.zdzc.collector.common.jenum.DataType;
import com.zdzc.collector.common.jenum.ProtocolType;
import com.zdzc.collector.common.jfinal.Config;
import com.zdzc.collector.receiver.db.DbConnectionPool;
import com.zdzc.collector.receiver.entity.BsjProtocol;
import com.zdzc.collector.receiver.util.TbUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class MessageConsumer {

    private static final Logger logger = LoggerFactory.getLogger(MessageConsumer.class);

    //保存设备号和轨迹序列号映射关系
    private static ConcurrentHashMap<String, Integer> seqNoMap = new ConcurrentHashMap<>();

    //保存设备号和报警序列号映射关系
    private static ConcurrentHashMap<String, Integer> alarmNoMap = new ConcurrentHashMap<>();

    //设备的最新信息
    private static ConcurrentHashMap<String, BsjProtocol> snapMap = new ConcurrentHashMap<>();

    private static String gpsDbPrefix = Config.get("gps.database.prefix");

    private static String alarmDbPrefix = Config.get("alarm.database.prefix");

    private static String gpsTbPrefix = Config.get("gps.table.prefix");

    private static String alarmTbPrefix = Config.get("alarm.table.prefix");

    private static int gpsBatchNum = Config.getInt("gps.insert.batch");

    private static int alarmBatchNum = Config.getInt("alarm.insert.batch");

    private static String gpsColumns = Config.get("gps.table.columns");

    private static String alarmColumns = Config.get("alarm.table.columns");

    //保存主表设备号
    private static List<String> deviceList = new ArrayList<>();

    /**
     * 消费者
     * @author liuwei
     * @return
     * @exception
     * @date 2019/1/3 15:45
     */
    public static Boolean consume(BsjProtocol protocol, final int index, ConcurrentHashMap<String, List<BsjProtocol>> mapList)  {
        int msgType = protocol.getMsgType();
        if (msgType == DataType.Registry.getValue()) {
            //收到登录消息，检测数据库是否有该设备，没有则入库该设备
            return consumeLoginInfo(protocol);
        }

        //更新主表ICCID
        updateMain(protocol);
        //更新快照表
        updateSnapMap(protocol);
        //插入轨迹报警表
        if (msgType == DataType.GPS.getValue() || msgType == DataType.ALARM.getValue()) {

            //位置、报警信息
            Map<String, String> map = getTbPath(protocol);
            String tbPath = map.get("tbPath");
            if (tbPath == null) {
                return false;
            }
//            if (mapList.containsKey(tbPath)) {
//                mapList.get(tbPath).add(protocol);
//            } else {
//                List<BsjProtocol> list = new ArrayList<>();
//                mapList.put(tbPath, list);
//            }
//
//            int batchNum = 1;
//            if (msgType == DataType.GPS.getValue()) {
//                batchNum = gpsBatchNum;
//            } else if (msgType == DataType.ALARM.getValue()) {
//                batchNum = alarmBatchNum;
//            }
//            if (index % batchNum != 0) {
//                return true;
//            }


            String columns = "";
            if (msgType == DataType.GPS.getValue()) {
                columns = gpsColumns;
            } else if (msgType == DataType.ALARM.getValue()) {
                columns = alarmColumns;
            }
//            if (batchNum <= 1) {
//                return toSingleConsumer(protocol, columns, tbPath);
//            } else {
//                return toBatchConsumer(mapList, columns);
//            }
            return toSingleConsumer(protocol, columns, tbPath);
        }

        return true;
    }

    public static Boolean toSingleConsumer(BsjProtocol protocol, String columns, String tbPath) {
        Boolean result = true;
        int strLen = columns.split(",").length;
        //占位符
        StringBuilder builder = new StringBuilder();
        for (int i = 0;i < strLen;i++) {
            builder.append("?");
            if (i < strLen -1) {
                builder.append(",");
            }
        }
        String sql = "INSERT INTO " + tbPath + "("+columns+")values("+builder+")";
        Connection connection = null;
        PreparedStatement pst;
        try{
            connection = DbConnectionPool.getConnect();
            pst = connection.prepareStatement(sql);
            toSetParams(pst, protocol);

            pst.execute();
            pst.close();
        }catch (Exception e) {
            logger.error("插入轨迹数据到 " + tbPath, e);
            result = false;
        }finally {
            if(connection != null){
                try {
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                    logger.error(e.getMessage());
                }
            }
        }
        return result;
    }

    /**
     * 批量消费位置报警数据
     * @author liuwei
     * @return
     * @exception
     * @date 2018/12/28 13:28
     */
    public static Boolean toBatchConsumer(ConcurrentHashMap<String, List<BsjProtocol>> mapList, String columns) {
        boolean result = true;
        for (Map.Entry<String, List<BsjProtocol>> entry : mapList.entrySet()) {
            String tbPath = entry.getKey();
            List<BsjProtocol> protocols = entry.getValue();
            logger.debug("tbpath -> {}", tbPath);
            int strLen = columns.split(",").length;
            //占位符
            StringBuilder builder = new StringBuilder();
            for (int i = 0;i < strLen;i++) {
                builder.append("?");
                if (i < strLen -1) {
                    builder.append(",");
                }
            }
            String sql = "INSERT INTO " + tbPath + "("+columns+")values("+builder+")";
            Connection connection = null;
            PreparedStatement pst;
            try{
                connection = DbConnectionPool.getConnect();
//                connection.setAutoCommit(false);
                pst = connection.prepareStatement(sql);
                for(BsjProtocol protocol : protocols){
                    toSetParams(pst, protocol);
                    pst.addBatch();
                }
                pst.executeBatch();
                pst.close();
                mapList.clear();
            }catch (Exception e) {
                e.printStackTrace();
                logger.error(e.getLocalizedMessage());
                result = false;
            }finally {
                if (connection != null) {
                    try {
//                        connection.commit();
//                        connection.setAutoCommit(true);
                        connection.close();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        return result;
    }

    /**
     * 更新快照表
     * @author liuwei
     * @return
     * @exception
     * @date 2018/12/28 16:30
     */
    public static void updateSnapMap(BsjProtocol protocol) {
        String sql;
        String deviceCode = protocol.getDeviceCode();
        if (!snapMap.containsKey(deviceCode)) {
            snapMap.put(deviceCode, protocol);
            return;
        }
        Connection connection = null;
        PreparedStatement pst = null;
        try {
            connection = DbConnectionPool.getConnect();
            int msgType = protocol.getMsgType();
            if (msgType == DataType.GPS.getValue()) {
                sql = "UPDATE GPS_MAIN.T_GPS_SNAPSHOT SET alarm_status = ?, vehicle_status = ?, lat = ?, lon = ?, speed = ?, direction = ?," +
                        " time = ?, mile = ?, satellite_num = ?, location_time = ?, voltage = ?, miles = ? where device_code = ?";
                pst = connection.prepareStatement(sql);
                long alarmStatus = protocol.getAlarmStatus();
                pst.setLong(1, alarmStatus);
                long vehicleStatus = protocol.getVehicleStatus();
                pst.setLong(2, vehicleStatus);
                double lat = protocol.getLat();
                pst.setDouble(3, lat);
                double lon = protocol.getLon();
                pst.setDouble(4, lon);
                double speed = protocol.getSpeed();
                pst.setDouble(5, speed);
                double direction = protocol.getDirection();
                pst.setDouble(6, direction);
                Date time = protocol.getDateTime();
                String timeStr = DateFormatUtils.format(time, "yyMMddHHmmss");
                pst.setString(7, timeStr);
                double mile = protocol.getMile();
                pst.setDouble(8, mile);
                int satNum = protocol.getSatelliteNum();
                pst.setInt(9, satNum);
                Date locationTime = protocol.getLocationTime();
                pst.setString(10, DateFormatUtils.format(locationTime, "yyyy-MM-dd HH:mm:ss"));
                int voltage = protocol.getVoltage();
                pst.setInt(11, voltage);
                double miles = protocol.getMiles();
                pst.setDouble(12, miles);
                pst.setString(13, deviceCode);
            } else if (msgType == DataType.ALARM.getValue()) {
                sql = "UPDATE GPS_MAIN.T_GPS_SNAPSHOT SET alarm_status = ?, vehicle_status = ?, lat = ?, lon = ?, speed = ?, direction = ?," +
                        " time = ?, mile = ?, satellite_num = ?, alarm_time = ?, voltage = ?, miles = ? where device_code = ?";
                pst = connection.prepareStatement(sql);
                pst.setLong(1, protocol.getAlarmStatus());
                pst.setLong(2, protocol.getVehicleStatus());
                pst.setDouble(3, protocol.getLat());
                pst.setDouble(4, protocol.getLon());
                pst.setDouble(5, protocol.getSpeed());
                pst.setDouble(6, protocol.getDirection());
                pst.setString(7, DateFormatUtils.format(protocol.getDateTime(), "yyMMddHHmmss"));
                pst.setDouble(8, protocol.getMile());
                pst.setInt(9, protocol.getSatelliteNum());
                pst.setString(10, DateFormatUtils.format(protocol.getAlarmTime(), "yyyy-MM-dd HH:mm:ss"));
                pst.setInt(11, protocol.getVoltage());
                pst.setDouble(12, protocol.getMiles());
                pst.setString(13, deviceCode);
            } else if (msgType == DataType.HEARTBEAT.getValue()) {
                sql = "UPDATE GPS_MAIN.T_GPS_SNAPSHOT SET alarm_status = ?, vehicle_status = ?," +
                        " mile = ?, heartbeat_time = ?, voltage = ?, miles = ? where device_code = ?";
                pst = connection.prepareStatement(sql);
                pst.setLong(1, protocol.getAlarmStatus());
                pst.setLong(2, protocol.getVehicleStatus());
                pst.setDouble(3, protocol.getMile());
                pst.setString(4, DateFormatUtils.format(protocol.getHeartBeatTime(), "yyMMddHHmmss"));
                pst.setInt(5, protocol.getVoltage());
                pst.setDouble(6, protocol.getMiles());
                pst.setString(7, protocol.getDeviceCode());
            }
            pst.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e.getMessage());
        } finally {
            closeConnection(pst, connection);
        }
    }

    /**
     * 更新主表ICCID
     * @author liuwei
     * @return
     * @exception
     * @date 2019/1/3 9:39
     */
    public static void updateMain(BsjProtocol protocol) {
        String iccid = protocol.getIccid();
        if (StringUtils.isNotEmpty(iccid)) {
            //todo 更新主表ICCID
            Connection connection = null;
            PreparedStatement pst = null;
            try {
                connection = DbConnectionPool.getConnect();
                String sql = "UPDATE GPS_MAIN.T_GPS_MAIN SET iccid = ? where device_code = ?";
                pst = connection.prepareStatement(sql);
                pst.setString(1, protocol.getIccid());
                pst.setString(2, protocol.getDeviceCode());
                pst.executeUpdate();
            } catch (Exception e) {
                e.printStackTrace();
                logger.error(e.getMessage());
            } finally {
                closeConnection(pst, connection);
            }
        }
    }

    /**
     * 设置SQL语句参数
     * @author liuwei
     * @return
     * @exception
     * @date 2018/12/28 15:38
     */
    private static void toSetParams(PreparedStatement pst, BsjProtocol protocol) throws SQLException {
        int msgType = protocol.getMsgType();
        if (msgType == DataType.GPS.getValue()) {
            pst.setString(1, protocol.getDeviceCode());
            pst.setLong(2, protocol.getAlarmStatus());
            pst.setLong(3, protocol.getVehicleStatus());
            pst.setDouble(4, protocol.getLat());
            pst.setDouble(5, protocol.getLon());
            pst.setDouble(6, protocol.getSpeed());
            pst.setDouble(7, protocol.getDirection());
            String time = DateFormatUtils.format(protocol.getDateTime(), "yyMMddHHmmss");
            pst.setString(8, time);
            pst.setDouble(9, protocol.getMile());
            pst.setInt(10, protocol.getSatelliteNum());
            pst.setInt(11, protocol.getVoltage());
            pst.setInt(12, protocol.getGpsFill());
        } else if (msgType == DataType.ALARM.getValue()) {
            pst.setString(1, protocol.getDeviceCode());
            pst.setLong(2, protocol.getAlarmStatus());
            pst.setLong(3, protocol.getVehicleStatus());
            pst.setDouble(4, protocol.getLat());
            pst.setDouble(5, protocol.getLon());
            pst.setDouble(6, protocol.getSpeed());
            pst.setDouble(7, protocol.getDirection());
            String time = DateFormatUtils.format(protocol.getDateTime(), "yyMMddHHmmss");
            pst.setString(8, time);
            pst.setDouble(9, protocol.getMile());
            pst.setInt(10, protocol.getSatelliteNum());
            pst.setInt(11, 0);
            pst.setInt(12,protocol.getVoltage());
            pst.setInt(13, protocol.getSignLevel());
            pst.setInt(14, protocol.getVoltageLevel());
        }
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

                //录入快照表
                String insertSnapSql = "INSERT INTO GPS_MAIN.T_GPS_SNAPSHOT(device_code, protocol_type) values(?, ?)";
                PreparedStatement pstt = pst.getConnection().prepareStatement(insertSnapSql);
                pstt.setString(1, deviceCode);
                pstt.setString(2, ProtocolType.BSJ.getValue());
                pstt.execute();
                pstt.close();
            }
            deviceList.add(deviceCode);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e.toString());
        } finally {
            closeConnection(pst, connection);
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
            closeConnection(pst, connection);
        }

    }

    /**
     * 关闭数据库连接
     * @author liuwei
     * @return
     * @exception
     * @date 2018/12/27 14:24
     */
    private static void closeConnection(PreparedStatement pst, Connection connection) {
        if (pst != null) {
            try {
                pst.close();
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
                logger.error(e.toString());
            }
        }
    }
}
