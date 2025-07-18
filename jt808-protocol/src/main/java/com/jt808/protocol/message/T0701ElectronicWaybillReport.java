package com.jt808.protocol.message;

import io.vertx.core.buffer.Buffer;
import java.util.Arrays;
import java.util.Objects;

/**
 * 0x0701 电子运单上报
 * 终端上报电子运单数据
 */
public class T0701ElectronicWaybillReport extends JT808Message {

    /**
     * 电子运单长度 - DWORD类型，表示电子运单内容的字节长度
     */
    private long waybillLength;

    /**
     * 电子运单内容 - 电子运单数据包
     */
    private byte[] waybillContent;

    public T0701ElectronicWaybillReport() {
        this.waybillLength = 0;
        this.waybillContent = new byte[0];
    }

    public T0701ElectronicWaybillReport(long waybillLength, byte[] waybillContent) {
        this.waybillLength = waybillLength;
        this.waybillContent = waybillContent != null ? waybillContent : new byte[0];
    }

    public T0701ElectronicWaybillReport(byte[] waybillContent) {
        this.waybillContent = waybillContent != null ? waybillContent : new byte[0];
        this.waybillLength = this.waybillContent.length;
    }

    public long getWaybillLength() {
        return waybillLength;
    }

    public void setWaybillLength(long waybillLength) {
        this.waybillLength = waybillLength;
    }

    public byte[] getWaybillContent() {
        return waybillContent;
    }

    public void setWaybillContent(byte[] waybillContent) {
        this.waybillContent = waybillContent != null ? waybillContent : new byte[0];
        this.waybillLength = this.waybillContent.length;
    }

    /**
     * 创建电子运单上报消息
     * @param waybillContent 电子运单内容
     * @return 电子运单上报消息实例
     */
    public static T0701ElectronicWaybillReport create(byte[] waybillContent) {
        return new T0701ElectronicWaybillReport(waybillContent);
    }

    /**
     * 创建电子运单上报消息
     * @param waybillLength 电子运单长度
     * @param waybillContent 电子运单内容
     * @return 电子运单上报消息实例
     */
    public static T0701ElectronicWaybillReport create(long waybillLength, byte[] waybillContent) {
        return new T0701ElectronicWaybillReport(waybillLength, waybillContent);
    }

    /**
     * 创建空的电子运单上报消息
     * @return 电子运单上报消息实例
     */
    public static T0701ElectronicWaybillReport createEmpty() {
        return new T0701ElectronicWaybillReport();
    }

    /**
     * 从字节数组创建电子运单上报消息
     * @param data 字节数组数据
     * @return 电子运单上报消息实例
     */
    public static T0701ElectronicWaybillReport fromBytes(byte[] data) {
        T0701ElectronicWaybillReport message = new T0701ElectronicWaybillReport();
        if (data != null && data.length >= 4) {
            Buffer buffer = Buffer.buffer(data);
            message.decodeBody(buffer);
        }
        return message;
    }

    /**
     * 检查是否有电子运单内容
     * @return 如果有内容返回true，否则返回false
     */
    public boolean hasWaybillContent() {
        return waybillContent != null && waybillContent.length > 0;
    }

    /**
     * 获取实际的电子运单内容长度
     * @return 电子运单内容的实际字节长度
     */
    public int getActualWaybillLength() {
        return waybillContent != null ? waybillContent.length : 0;
    }

    /**
     * 检查长度字段与实际内容长度是否一致
     * @return 如果一致返回true，否则返回false
     */
    public boolean isLengthConsistent() {
        return waybillLength == getActualWaybillLength();
    }

    /**
     * 获取消息描述
     * @return 消息描述字符串
     */
    public String getMessageDescription() {
        return "电子运单上报";
    }

    @Override
    public int getMessageId() {
        return 0x0701;
    }

    @Override
    public Buffer encodeBody() {
        Buffer buffer = Buffer.buffer();
        
        // 写入电子运单长度（4字节DWORD）
        buffer.appendUnsignedInt(waybillLength);
        
        // 写入电子运单内容（可变长度）
        if (waybillContent != null && waybillContent.length > 0) {
            buffer.appendBytes(waybillContent);
        }
        
        return buffer;
    }

    @Override
    public void decodeBody(Buffer buffer) {
        if (buffer == null || buffer.length() < 4) {
            throw new IllegalArgumentException("电子运单上报消息体长度不能少于4字节");
        }
        
        // 读取电子运单长度（4字节DWORD）
        this.waybillLength = buffer.getUnsignedInt(0);
        
        // 读取电子运单内容（剩余字节）
        if (buffer.length() > 4) {
            int contentLength = buffer.length() - 4;
            this.waybillContent = new byte[contentLength];
            buffer.getBytes(4, buffer.length(), this.waybillContent);
        } else {
            this.waybillContent = new byte[0];
        }
    }

    @Override
    public String toString() {
        return String.format("T0701ElectronicWaybillReport{waybillLength=%d, contentLength=%d, hasContent=%s}",
                waybillLength, getActualWaybillLength(), hasWaybillContent());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        T0701ElectronicWaybillReport that = (T0701ElectronicWaybillReport) o;
        return waybillLength == that.waybillLength &&
                Arrays.equals(waybillContent, that.waybillContent);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(waybillLength);
        result = 31 * result + Arrays.hashCode(waybillContent);
        return result;
    }
}