package com.zdzc.collector.common.jenum;

/**
 * @Author liuwei
 * @Description 协议类型枚举
 * @Date 2018/12/11 15:47
 */
public enum ProtocolType {

    /**
     * 部标808协议
     */
    JT808("01", "部标808协议"),

    /**
     * 沃瑞特C11协议
     */
    WRT("02", "沃瑞特C11协议"),

    /**
     * 博实结私有协议
     */
    BSJ("03", "博实结协议");


    private String value;

    private String desc;

    ProtocolType(String value, String desc) {
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
