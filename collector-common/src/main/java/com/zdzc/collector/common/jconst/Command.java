package com.zdzc.collector.common.jconst;

/**
 * @Author liuwei
 * @Description 标识符、指令类  msg是上行指令  cmd是下行指令
 * @Date 2018/12/11 15:17
 */
public class Command {

    /**
     * 标识位
     */
    public static final int PKG_DELIMITER = 0x7e;

    /**
     * 字符编码格式
     */
    public static final String STRING_ENCODING = "GBK";

    /**
     * 鉴权码
     */
    public static final String REPLYTOKEN = "1234567890Z";

    /**
     * 终端心跳
     */
    public static final int MSG_ID_TERMINAL_HEART_BEAT = 0x0002;

    /**
     * 终端注册
     */
    public static final int MSG_ID_TERMINAL_REGISTER = 0x0100;

    /**
     * 终端鉴权
     */
    public static final int MSG_ID_TERMINAL_AUTHENTICATION = 0x0102;

    /**
     * 位置信息汇报
     */
    public static final int MSG_ID_TERMINAL_LOCATION_INFO_UPLOAD = 0x0200;

    /**
     * 定位数据批量上传
     */
    public static final int MSG_ID_TERMINAL_LOCATION_INFO_BATCH_UPLOAD = 0x0704;

    /**
     * 查询终端属性应答
     */
    public static final int MSG_ID_TERMINAL_PROP_QUERY_RESP = 0x0107;


    /**
     * 平台通用应答
     */
    public static final int CMD_COMMON_RESP = 0x8001;

    /**
     * 终端注册应答
     */
    public static final int CMD_TERMINAL_REGISTER_RESP = 0x8100;

    /**
     * 查询终端属性
     */
    public static final int CMD_TERMINAL_PROP_QUERY = 0x8107;

    /**
     * 登录数据ID
     */
    public static final String WRT_MSG_ID_LOGIN = "AP00";

    /**
     * 定位数据ID
     */
    public static final String WRT_MSG_ID_TERMINAL_LOCATION = "YP03";

    /**
     * 报警数据ID
     */
    public static final String WRT_MSG_ID_TERMINAL_ALARM = "YP05";

    /**
     * 心跳数据ID
     */
    public static final String WRT_MSG_ID_TERMINAL_HEARTBEAT = "YP07";

    /**
     * 状态数据ID
     */
    public static final String WRT_MSG_ID_TERMINAL_STATUS = "AP57";

    /**
     * IMSI数据ID
     */
    public static final String WRT_MSG_ID_TERMINAL_IMSI = "YP02";

    /**
     * 控制器数据ID
     */
    public static final String WRT_MSG_ID_TERMINAL_CONTROLLER = "AP90";

    /**
     * 登录数据应答
     */
    public static final String WRT_MSG_LOGIN_RESP = "BP00";

    /**
     * 定位数据应答
     */
    public static final String WRT_MSG_LOCATION_RESP = "ZP03";

    /**
     * 报警数据应答
     */
    public static final String WRT_MSG_ALARM_RESP = "ZP05";

    /**
     * 心跳数据应答
     */
    public static final String WRT_MSG_HEARTBEAT_RESP = "ZP07";

    /**
     * 状态数据应答
     */
    public static final String WRT_MSG_STATUS_RESP = "BP57,OK";

    /**
     * IMSI数据应答
     */
    public static final String WRT_MSG_IMSI_RESP = "ZP02";

    /**
     * 控制器数据应答
     */
    public static final String WRT_MSG_CONTROLLER_RESP = "BP90";

    /**
     * 设置定位数据上传时间间隔的设备回复
     */
    public static final String MSG_GPS_INTERVAL_RESP = "XP02";

    /**
     * 设置设防撤防状态的设备回复
     */
    public static final String MSG_DEFENCE_RESP = "AP02";

    /**
     * 设置油电断开状态的设备回复
     */
    public static final String MSG_POWER_STOP_RESP = "AP03";

    /**
     * 设置油电恢复状态的设备回复
     */
    public static final String MSG_POWER_RECOVER_RESP = "AP04";

    /**
     * 设置超速的设备回复
     */
    public static final String MSG_OVERSPEED_RESP = "AP74";

    /**
     * 设置心跳上传频率的设备回复
     */
    public static final String MSG_HEART_INTERVAL_RESP = "CP03";

    /**
     * 设置IP域名的设备回复
     */
    public static final String MSG_IP_RESP = "AP64";

    /**
     * 控制器下发指令回复
     */
    public static final String WRT_CMD_CONTROLLER_RESP = "AP91";

    /**
     * HTTP请求参数单个数据标识
     */
    public static final String HTTP_DATA_SINGLE = "deviceDataChanged";

    /**
     * HTTP请求参数批量数据标识
     */
    public static final String HTTP_DATA_BATCH = "deviceDatasChanged";

    public static final String HTTP_REQUEST_IDENTIFY = "OceanConnect";

    /**
     * 博实结登录数据ID
     */
    public static final String BSJ_MSG_LOGIN = "01";

    /**
     * 博实结定位数据ID
     */
    public static final String BSJ_MSG_LOCATION = "22";

    /**
     * 博实结报警数据ID
     */
    public static final String BSJ_MSG_ALARM = "26";

    /**
     * 博实结心跳数据ID
     */
    public static final String BSJ_MSG_HEARTBEAT = "13";

    /**
     * 博实结回复数据包长度
     */
    public static final String BSJ_MSG_RETURN_LENGTH = "05";

    /**
     * 博实结下发指令回复ID
     */
    public static final String BSJ_MSG_REPLY = "15";

}
