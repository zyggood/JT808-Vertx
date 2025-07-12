package com.jt808.protocol.message;

import io.vertx.core.buffer.Buffer;

/**
 * JT808消息基类
 */
public abstract class JT808Message {

    /**
     * 消息头
     */
    private JT808Header header;

    /**
     * 消息体
     */
    private Buffer body;

    /**
     * 校验码
     */
    private byte checksum;

    public JT808Message() {
    }

    public JT808Message(JT808Header header) {
        this.header = header;
    }

    /**
     * 获取消息ID
     *
     * @return 消息ID
     */
    public abstract int getMessageId();

    /**
     * 编码消息体
     *
     * @return 编码后的消息体
     */
    public abstract Buffer encodeBody();

    /**
     * 解码消息体
     *
     * @param body 消息体数据
     */
    public abstract void decodeBody(Buffer body);

    public JT808Header getHeader() {
        return header;
    }

    public void setHeader(JT808Header header) {
        this.header = header;
    }

    public Buffer getBody() {
        return body;
    }

    public void setBody(Buffer body) {
        this.body = body;
    }

    public byte getChecksum() {
        return checksum;
    }

    public void setChecksum(byte checksum) {
        this.checksum = checksum;
    }

    @Override
    public String toString() {
        return "JT808Message{" +
                "header=" + header +
                ", messageId=0x" + Integer.toHexString(getMessageId()).toUpperCase() +
                ", checksum=0x" + Integer.toHexString(checksum & 0xFF).toUpperCase() +
                '}';
    }
}