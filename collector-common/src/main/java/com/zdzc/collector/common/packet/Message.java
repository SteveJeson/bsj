package com.zdzc.collector.common.packet;

/**
 * @Author liuwei
 * @Description 消息体实体类
 * @Date 2018/12/11 15:39
 */
public class Message {


    /**
     * 消息头
     */
    private Header header;

    /**
     * 消息体
     */
    private byte[] body;

    /**
     * 完整消息
     */
    private String all;

    /**
     * 通用应答消息
     */
    private byte[] replyBody;

    /**
     * 额外应答消息（如：查询终端属性）
     */
    private byte[] extReplyBody;

    /**
     * 推送消息体
     */
    private byte[] sendBody;
    /**
     * 是否粘包
     */
    private Boolean isStick;

    public Message() {
    }

    public Header getHeader() {
        return header;
    }

    public void setHeader(Header header) {
        this.header = header;
    }

    public byte[] getBody() {
        return body;
    }

    public void setBody(byte[] body) {
        this.body = body;
    }


    public byte[] getReplyBody() {
        return replyBody;
    }

    public void setReplyBody(byte[] replyBody) {
        this.replyBody = replyBody;
    }

    public String getAll() {
        return all;
    }

    public void setAll(String all) {
        this.all = all;
    }

    public byte[] getExtReplyBody() {
        return extReplyBody;
    }

    public void setExtReplyBody(byte[] extReplyBody) {
        this.extReplyBody = extReplyBody;
    }

    public Boolean getStick() {
        return isStick;
    }

    public void setStick(Boolean stick) {
        isStick = stick;
    }

    public byte[] getSendBody() {
        return sendBody;
    }

    public void setSendBody(byte[] sendBody) {
        this.sendBody = sendBody;
    }
}
