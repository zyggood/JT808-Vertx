package com.jt808.protocol.message;

import io.vertx.core.buffer.Buffer;

/**
 * 信息点播/取消消息 (0x0303)
 * 终端向平台发送信息点播或取消点播的消息
 * <p>
 * 消息体数据格式:
 * - 信息类型 (BYTE, 1字节): 信息类型标识
 * - 点播/取消标志 (BYTE, 1字节): 0：取消；1：点播
 * <p>
 * 该消息用于终端向平台请求点播特定类型的信息或取消已点播的信息。
 * 平台收到该消息后应回复平台通用应答消息。
 */
public class T0303InfoDemandCancel extends JT808Message {

    /**
     * 消息ID常量
     */
    public static final int MESSAGE_ID = 0x0303;

    /**
     * 点播/取消标志常量
     */
    public static final byte FLAG_CANCEL = 0x00;  // 取消
    public static final byte FLAG_DEMAND = 0x01;  // 点播

    /**
     * 信息类型
     */
    private byte infoType;

    /**
     * 点播/取消标志
     * 0：取消；1：点播
     */
    private byte demandFlag;

    /**
     * 默认构造函数
     */
    public T0303InfoDemandCancel() {
        super();
    }

    /**
     * 带消息头的构造函数
     *
     * @param header 消息头
     */
    public T0303InfoDemandCancel(JT808Header header) {
        super(header);
    }

    /**
     * 完整构造函数
     *
     * @param infoType   信息类型
     * @param demandFlag 点播/取消标志
     */
    public T0303InfoDemandCancel(byte infoType, byte demandFlag) {
        super();
        this.infoType = infoType;
        this.demandFlag = demandFlag;
    }

    /**
     * 带消息头的完整构造函数
     *
     * @param header     消息头
     * @param infoType   信息类型
     * @param demandFlag 点播/取消标志
     */
    public T0303InfoDemandCancel(JT808Header header, byte infoType, byte demandFlag) {
        super(header);
        this.infoType = infoType;
        this.demandFlag = demandFlag;
    }

    /**
     * 创建信息点播消息
     *
     * @param infoType 信息类型
     * @return 信息点播消息实例
     */
    public static T0303InfoDemandCancel createDemand(byte infoType) {
        return new T0303InfoDemandCancel(infoType, FLAG_DEMAND);
    }

    /**
     * 创建信息取消消息
     *
     * @param infoType 信息类型
     * @return 信息取消消息实例
     */
    public static T0303InfoDemandCancel createCancel(byte infoType) {
        return new T0303InfoDemandCancel(infoType, FLAG_CANCEL);
    }

    @Override
    public int getMessageId() {
        return MESSAGE_ID;
    }

    @Override
    public Buffer encodeBody() {
        Buffer buffer = Buffer.buffer();
        // 信息类型 (1字节)
        buffer.appendByte(infoType);
        // 点播/取消标志 (1字节)
        buffer.appendByte(demandFlag);
        return buffer;
    }

    @Override
    public void decodeBody(Buffer body) {
        if (body == null || body.length() < 2) {
            throw new IllegalArgumentException("信息点播/取消消息体不能为空，至少需要2字节");
        }

        // 信息类型 (1字节)
        this.infoType = body.getByte(0);
        // 点播/取消标志 (1字节)
        this.demandFlag = body.getByte(1);
    }

    /**
     * 获取信息类型
     *
     * @return 信息类型
     */
    public byte getInfoType() {
        return infoType;
    }

    /**
     * 设置信息类型
     *
     * @param infoType 信息类型
     */
    public void setInfoType(byte infoType) {
        this.infoType = infoType;
    }

    /**
     * 获取信息类型的无符号值
     *
     * @return 信息类型的无符号值 (0-255)
     */
    public int getInfoTypeUnsigned() {
        return infoType & 0xFF;
    }

    /**
     * 获取点播/取消标志
     *
     * @return 点播/取消标志
     */
    public byte getDemandFlag() {
        return demandFlag;
    }

    /**
     * 设置点播/取消标志
     *
     * @param demandFlag 点播/取消标志
     */
    public void setDemandFlag(byte demandFlag) {
        this.demandFlag = demandFlag;
    }

    /**
     * 获取点播/取消标志的无符号值
     *
     * @return 点播/取消标志的无符号值 (0-255)
     */
    public int getDemandFlagUnsigned() {
        return demandFlag & 0xFF;
    }

    /**
     * 判断是否为点播操作
     *
     * @return true：点播；false：取消
     */
    public boolean isDemand() {
        return demandFlag == FLAG_DEMAND;
    }

    /**
     * 判断是否为取消操作
     *
     * @return true：取消；false：点播
     */
    public boolean isCancel() {
        return demandFlag == FLAG_CANCEL;
    }

    /**
     * 获取操作类型描述
     *
     * @return 操作类型描述
     */
    public String getOperationDescription() {
        return isDemand() ? "点播" : "取消";
    }

    @Override
    public String toString() {
        return "T0303InfoDemandCancel{" +
                "infoType=" + getInfoTypeUnsigned() +
                " (0x" + String.format("%02X", getInfoTypeUnsigned()) + ")" +
                ", demandFlag=" + getDemandFlagUnsigned() +
                " (" + getOperationDescription() + ")" +
                ", header=" + getHeader() +
                '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        T0303InfoDemandCancel that = (T0303InfoDemandCancel) obj;
        return infoType == that.infoType && demandFlag == that.demandFlag;
    }

    @Override
    public int hashCode() {
        int result = infoType;
        result = 31 * result + demandFlag;
        return result;
    }
}