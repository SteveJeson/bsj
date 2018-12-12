package com.zdzc.collector.sender.protocol;

public class JtProtocol {

    private int beginMark = 0x7E;

    private JtHeader jtHeader;

    private int contentLen;

    private byte[] content;

    public JtHeader getJtHeader() {
        return jtHeader;
    }

    public void setJtHeader(JtHeader jtHeader) {
        this.jtHeader = jtHeader;
    }

    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }

    public int getContentLen() {
        return contentLen;
    }

    public void setContentLen(int contentLen) {
        this.contentLen = contentLen;
    }

    public int getBeginMark() {
        return beginMark;
    }

    public void setBeginMark(int beginMark) {
        this.beginMark = beginMark;
    }
}
