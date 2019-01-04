package com.zdzc.collector.receiver.util;

import com.zdzc.collector.common.jfinal.Config;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class TbUtil {

    public static String getDayOfMonth (Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int daysInMonth = calendar.get(Calendar.DAY_OF_MONTH);
        int month = calendar.get(Calendar.MONTH) + 1;
        return monthToLetter (month) + daysInMonth;
    }

    /**
     * 将月份转化为字母编号(轨迹信息)
     * @author liuwei
     * @return
     * @exception
     * @date 2018/12/26 15:36
     */
    private static String monthToLetter (int month) {
        String[] letters = { "a", "b", "c", "d" };

        return letters[(month - 1) % 4];
    }

    /**
     * 获取数据库和表序号（轨迹、报警）
     * @author liuwei
     * @return
     * @exception
     * @date 2018/12/26 15:39
     */
    private static Map<String, Integer> getDbNoAndTbNo (int seqNo, int rate, int mul, int sub) {
        Map<String, Integer> map = new HashMap<>();
        int dbNo = 1, tbNo = 1;
        String seqStr = String.valueOf(seqNo);
        //数据库序号
        dbNo = Integer.parseInt(seqStr.substring(0, seqStr.length() - sub));
        int indexOfTb = seqNo - dbNo * mul;
        tbNo = (indexOfTb - 1) / rate + 1; //数据库序号
        map.put("dbNo", dbNo);
        map.put("tbNo", tbNo);
        return map;
    }

    /**
     * 获取数据库和表序号（轨迹、报警）
     * @author liuwei
     * @return
     * @exception
     * @date 2018/12/26 15:45
     */
    private static Map<String, Integer> getDbNoAndTbNo (int seqNo, String type) {
        Map<String, Integer> map = getRateParams (type);
        return getDbNoAndTbNo (seqNo, map.get("rate"), map.get("mul"), map.get("sub"));
    }

    /**
     * 获取数据库名
     * @author liuwei
     * @return
     * @exception
     * @date 2018/12/26 15:45
     */
    public static String getDbName (String dbPrex, int seqNo, String type) {
        Map<String, Integer> map = getDbNoAndTbNo (seqNo, type);
        return dbPrex + "_" + map.get("dbNo");
    }

    /**
     * 获取表名
     * @author liuwei
     * @return
     * @exception
     * @date 2018/12/26 15:46
     */
    public static String getTableName (Date time, String tbPrex, int seqNo, String type) {
        String letterOfMonth = getDayOfMonth (time);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(time);
        int month = calendar.get(Calendar.MONTH) + 1;
        if (type.equals ("alarm")) {
            letterOfMonth = monthToLetter (month);
        }
        Map<String, Integer> map = getDbNoAndTbNo (seqNo, type);
        return tbPrex + "_" + letterOfMonth + "_" + map.get("tbNo");
    }

    /**
     * 判断序号是否在规则范围之内（轨迹、报警）
     * @author liuwei
     * @return
     * @exception
     * @date 2018/12/26 15:50
     */
    private static Boolean isSeqNoInScope (int seqNo, int range, int mul, int sub) {
        if (seqNo == 0) {
            return true;
        }
        String seqStr = String.valueOf(seqNo);
        int firstNum = Integer.parseInt(seqStr.substring(0, seqStr.length() - sub));
        int indexOfTb = seqNo - firstNum * mul;
        if ((indexOfTb + 1) <= range) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 创建新的序列号（轨迹、报警）
     * @author liuwei
     * @return
     * @exception
     * @date 2018/12/26 15:51
     */
    private static int createLatestSeqNo (int seqNo, int range, int mul, int sub) {
        Boolean flag = isSeqNoInScope (seqNo, range, mul, sub);
        if (flag) {
            return seqNo + 1;
        } else {
            String seqStr = String.valueOf(seqNo);
            int firstNum = Integer.parseInt(seqStr.substring(0, seqStr.length() - sub));
            return (firstNum + 1) * mul + 1;
        }
    }

    /**
     * 创建新的序列号（轨迹、报警）
     * @author liuwei
     * @return
     * @exception
     * @date 2018/12/26 15:53
     */
    public static int createLatestSeqNo (int seqNo, String maxNum, String type) {

        int range = Integer.parseInt (maxNum), mul = (int)Math.pow (10, maxNum.length()), sub = maxNum.length();

        return createLatestSeqNo (seqNo, range, mul, sub);
    }

   /**
    * 根据类型获取不同参数
    * @author liuwei
    * @return
    * @exception
    * @date 2018/12/26 16:14
    */
    private static Map<String, Integer> getRateParams (String type) {
        Map<String, Integer> map = new HashMap<>();
        int rate = Config.getInt("gps.tableRecord.max");
        int maxNum = Config.getInt("gps.db.maxNum");
        int mul = (int)Math.pow (10, String.valueOf(maxNum).length());
        map.put("rate", rate);
        map.put("mul", mul);
        map.put("sub", String.valueOf(maxNum).length());
        switch (type) {
            case "alarm":
                int maxNumOfAlarm = Config.getInt("alarm.db.maxNum");
                int rateOfAlarm = Config.getInt("alarm.tableRecord.max");
                map.put("rate", rateOfAlarm);
                map.put("mul", (int)Math.pow(10, String.valueOf(maxNumOfAlarm).length()));
                map.put("sub", String.valueOf(maxNumOfAlarm).length());
                break;

        }
        return map;
    }
}
