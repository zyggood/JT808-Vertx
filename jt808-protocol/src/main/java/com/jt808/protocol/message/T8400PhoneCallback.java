package com.jt808.protocol.message;

import io.vertx.core.buffer.Buffer;

import java.nio.charset.Charset;
import java.util.Objects;

/**
 * 电话回拨消息 (0x8400)
 * 平台向终端下发电话回拨指令
 */
public class T8400PhoneCallback extends JT808Message {

    /**
     * 消息ID
     */
    public static final int MESSAGE_ID = 0x8400;

    /**
     * 标志常量
     */
    public static class CallFlag {
        /**
         * 普通通话
         */
        public static final byte NORMAL_CALL = 0x00;

        /**
         * 监听
         */
        public static final byte MONITOR = 0x01;
    }

    /**
     * 标志：0-普通通话；1-监听
     */
    private byte flag;

    /**
     * 电话号码（最长20字节）
     */
    private String phoneNumber;

    /**
     * 默认构造函数
     */
    public T8400PhoneCallback() {
        super();
    }

    /**
     * 带Header的构造函数
     *
     * @param header 消息头
     */
    public T8400PhoneCallback(JT808Header header) {
        super(header);
    }

    /**
     * 构造电话回拨消息
     *
     * @param flag        标志
     * @param phoneNumber 电话号码
     */
    public T8400PhoneCallback(byte flag, String phoneNumber) {
        this.flag = flag;
        this.phoneNumber = phoneNumber;
    }

    /**
     * 创建普通通话回拨消息
     *
     * @param phoneNumber 电话号码
     * @return 电话回拨消息
     */
    public static T8400PhoneCallback createNormalCall(String phoneNumber) {
        return new T8400PhoneCallback(CallFlag.NORMAL_CALL, phoneNumber);
    }

    /**
     * 创建监听回拨消息
     *
     * @param phoneNumber 电话号码
     * @return 电话回拨消息
     */
    public static T8400PhoneCallback createMonitorCall(String phoneNumber) {
        return new T8400PhoneCallback(CallFlag.MONITOR, phoneNumber);
    }

    @Override
    public int getMessageId() {
        return MESSAGE_ID;
    }

    @Override
    public Buffer encodeBody() {
        Buffer buffer = Buffer.buffer();

        // 标志 (1字节)
        buffer.appendByte(flag);

        // 电话号码 (STRING，最长20字节)
        if (phoneNumber != null && !phoneNumber.isEmpty()) {
            byte[] phoneBytes = phoneNumber.getBytes(Charset.forName("GBK"));
            if (phoneBytes.length > 20) {
                throw new IllegalArgumentException("电话号码长度不能超过20字节，当前长度: " + phoneBytes.length + " 字节");
            }
            buffer.appendBytes(phoneBytes);
        }

        return buffer;
    }

    @Override
    public void decodeBody(Buffer body) {
        if (body == null || body.length() < 1) {
            throw new IllegalArgumentException("电话回拨消息体长度至少为1字节，实际长度: " + (body != null ? body.length() : 0) + " 字节");
        }

        int index = 0;

        // 标志 (1字节)
        flag = body.getByte(index);
        index += 1;

        // 电话号码 (剩余字节，GBK编码)
        if (index < body.length()) {
            byte[] phoneBytes = body.getBytes(index, body.length());
            phoneNumber = new String(phoneBytes, Charset.forName("GBK"));
        } else {
            phoneNumber = "";
        }
    }

    /**
     * 检查是否为普通通话
     *
     * @return true表示普通通话
     */
    public boolean isNormalCall() {
        return flag == CallFlag.NORMAL_CALL;
    }

    /**
     * 检查是否为监听
     *
     * @return true表示监听
     */
    public boolean isMonitor() {
        return flag == CallFlag.MONITOR;
    }

    /**
     * 获取标志描述
     *
     * @return 标志描述
     */
    public String getFlagDescription() {
        switch (flag) {
            case CallFlag.NORMAL_CALL:
                return "普通通话";
            case CallFlag.MONITOR:
                return "监听";
            default:
                return "未知标志(" + (flag & 0xFF) + ")";
        }
    }

    /**
     * 获取标志的无符号值
     *
     * @return 无符号标志值
     */
    public int getFlagUnsigned() {
        return flag & 0xFF;
    }

    // Getter和Setter方法

    public byte getFlag() {
        return flag;
    }

    public void setFlag(byte flag) {
        this.flag = flag;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    @Override
    public String toString() {
        return "T8400PhoneCallback{" +
                "flag=" + getFlagDescription() + "(" + getFlagUnsigned() + ")" +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", messageId=0x" + Integer.toHexString(getMessageId()).toUpperCase() +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        T8400PhoneCallback that = (T8400PhoneCallback) o;
        return flag == that.flag && Objects.equals(phoneNumber, that.phoneNumber);
    }

    @Override
    public int hashCode() {
        return Objects.hash(flag, phoneNumber);
    }
}