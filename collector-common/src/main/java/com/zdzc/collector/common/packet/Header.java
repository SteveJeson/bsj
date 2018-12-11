package com.zdzc.collector.common.packet;

/**
 * @Author liuwei
 * @Description 消息头实体类
 * @Date 2018/12/11 15:28
 */
public class Header {

    /**
     * 整条消息的长度
     */
    private int msgLength;

    /**
     * 协议类型
     */
    private String protocolType;

    /**
     * 消息ID
     */
    private int msgId;

    /**
     * 字符串形式消息ID
     */
    private String msgIdStr;

    /**
     * 消息体长度
     */
    private int msgBodyLength;

    /**
     * 终端手机号
     */
    private String terminalPhone;

    /**
     * 消息流水号
     */
    private int flowId;

    /**
     * 消息体属性
     */
    private int msgBodyProps;

    /**
     * 是否有子包
     */
    private boolean hasSubPackage;

    /**
     * 消息类型：位置、报警、心跳等
     */
    private int msgType;

    public int getMsgLength() {
        return msgLength;
    }

    public void setMsgLength(int msgLength) {
        this.msgLength = msgLength;
    }

    public String getProtocolType() {
        return protocolType;
    }

    public void setProtocolType(String protocolType) {
        this.protocolType = protocolType;
    }

    public int getMsgId() {
        return msgId;
    }

    public void setMsgId(int msgId) {
        this.msgId = msgId;
    }

    public int getMsgBodyLength() {
        return msgBodyLength;
    }

    public void setMsgBodyLength(int msgBodyLength) {
        this.msgBodyLength = msgBodyLength;
    }

    public String getTerminalPhone() {
        return terminalPhone;
    }

    public void setTerminalPhone(String terminalPhone) {
        this.terminalPhone = terminalPhone;
    }

    public int getFlowId() {
        return flowId;
    }

    public void setFlowId(int flowId) {
        this.flowId = flowId;
    }

    public int getMsgBodyProps() {
        return msgBodyProps;
    }

    public void setMsgBodyProps(int msgBodyProps) {
        this.msgBodyProps = msgBodyProps;
    }

    public boolean hasSubPackage() {
        return hasSubPackage;
    }

    public void setHasSubPackage(boolean hasSubPackage) {
        this.hasSubPackage = hasSubPackage;
    }

    public int getMsgType() {
        return msgType;
    }

    public void setMsgType(int msgType) {
        this.msgType = msgType;
    }

    public String getMsgIdStr() {
        return msgIdStr;
    }

    public void setMsgIdStr(String msgIdStr) {
        this.msgIdStr = msgIdStr;
    }
}
