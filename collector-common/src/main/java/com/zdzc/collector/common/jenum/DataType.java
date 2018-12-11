package com.zdzc.collector.common.jenum;

/**
 * @Author liuwei
 * @Description 数据类型
 * @Date 2018/12/11 15:26
 */
public enum DataType {
    /**
     * 终端定位数据
     */
    GPS(1, "终端定位"),

    /**
     * 终端报警数据
     */
    ALARM(2, "终端报警"),

    /**
     * 终端心跳数据
     */
    HEARTBEAT(3, "终端心跳"),

    /**
     * 终端注册数据
     */
    Registry(4, "终端注册"),

    /**
     * 终端鉴权数据
     */
    Authentication(5, "终端鉴权"),

    /**
     * 终端属性数据
     */
    Property(6, "终端属性"),

    /**
     * 控制器数据
     */
    CONTROLLER(7, "控制器");

    private int value;

    private String desc;

    DataType(int value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    public int getValue() {
        return value;
    }

    public String getDesc() {
        return desc;
    }

    /**
     * 根据值返回名称
     * @param value
     * @return
     */
    public static DataType getStatusByValue(int value){
        for(DataType status : DataType.values()){
            if(status.getValue() == value){
                return status;
            }
        }
        return null;
    }

    /**
     * 根据值返回描述
     * @param value
     * @return
     */
    public static String getDescByValue(int value){
        return getStatusByValue(value).getDesc();
    }

}
