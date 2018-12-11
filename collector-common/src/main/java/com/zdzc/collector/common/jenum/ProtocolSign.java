package com.zdzc.collector.common.jenum;

/**
 * @Author liuwei
 * @Description 协议标识符枚举
 * @Date 2018/12/11 15:47
 */
public enum ProtocolSign {

    /**
     * 部标协议开始标识符
     */
    JT808_BEGINMARK("7E", "部标开始标识符"),

    /**
     * 沃瑞特C11协议开始标识符
     */
    WRT_BEGINMARK("TRV", "沃瑞特开始标识符"),

    /**
     * 沃瑞特C11协议结束标识符
     */
    WRT_ENDMARK("#", "沃瑞特结束标识符"),

    /**
     * 博实结开始标识符
     */
    BSJ_BEGINMARK("7878", "博实结开始标识符"),

    /**
     * 博实结结束标识符
     */
    BSJ_ENDMARK("0D0A","博实结结束标识符");

    private String value;

    private String desc;

    ProtocolSign(String value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    public String getValue() {
        return value;
    }

    public String getDesc() {
        return desc;
    }

}
