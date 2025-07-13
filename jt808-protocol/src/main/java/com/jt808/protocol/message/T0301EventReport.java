package com.jt808.protocol.message;

import io.vertx.core.buffer.Buffer;

/**
 * 事件报告消息 (0x0301)
 * 终端向平台发送事件报告的消息
 * <p>
 * 消息体数据格式:
 * - 事件ID (BYTE, 1字节): 事件编码，由平台预设到终端
 * <p>
 * 事件项由平台通过事件设置消息(0x8301)预设到终端，由事件编码和事件名称组成。
 * 驾驶员在遇到相应事件时操作终端，触发事件报告发送到平台。
 * <p>
 * 该消息需要平台回复平台通用应答消息。
 */
public class T0301EventReport extends JT808Message {

    /**
     * 事件ID
     */
    private byte eventId;

    public T0301EventReport() {
        super();
    }

    public T0301EventReport(JT808Header header) {
        super(header);
    }

    public T0301EventReport(byte eventId) {
        super();
        this.eventId = eventId;
    }

    public T0301EventReport(JT808Header header, byte eventId) {
        super(header);
        this.eventId = eventId;
    }

    /**
     * 创建事件报告消息
     *
     * @param eventId 事件ID
     * @return 事件报告消息实例
     */
    public static T0301EventReport create(byte eventId) {
        return new T0301EventReport(eventId);
    }

    @Override
    public int getMessageId() {
        return 0x0301;
    }

    @Override
    public Buffer encodeBody() {
        Buffer buffer = Buffer.buffer();
        // 事件ID (1字节)
        buffer.appendByte(eventId);
        return buffer;
    }

    @Override
    public void decodeBody(Buffer body) {
        if (body == null || body.length() < 1) {
            throw new IllegalArgumentException("事件报告消息体不能为空，至少需要1字节的事件ID");
        }

        // 事件ID (1字节)
        this.eventId = body.getByte(0);
    }

    /**
     * 获取事件ID
     *
     * @return 事件ID
     */
    public byte getEventId() {
        return eventId;
    }

    /**
     * 设置事件ID
     *
     * @param eventId 事件ID
     */
    public void setEventId(byte eventId) {
        this.eventId = eventId;
    }

    /**
     * 获取事件ID的无符号值
     *
     * @return 事件ID的无符号值 (0-255)
     */
    public int getEventIdUnsigned() {
        return eventId & 0xFF;
    }

    @Override
    public String toString() {
        return "T0301EventReport{" +
                "eventId=" + getEventIdUnsigned() +
                " (0x" + String.format("%02X", getEventIdUnsigned()) + ")" +
                ", header=" + getHeader() +
                '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        T0301EventReport that = (T0301EventReport) obj;
        return eventId == that.eventId;
    }

    @Override
    public int hashCode() {
        return eventId;
    }
}