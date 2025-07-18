package com.jt808.protocol.message;

import io.vertx.core.buffer.Buffer;

import java.util.Objects;

/**
 * 行驶记录数据采集命令 (0x8700)
 * 平台向终端发送行驶记录仪数据采集命令
 * 数据格式见 GB/T 19056 相关要求
 */
public class T8700DrivingRecordDataCollection extends JT808Message {

    /**
     * 命令字 (1字节)
     * 命令字列表见 GB/T 19056 中相关要求
     */
    private byte commandWord;

    /**
     * 数据块 (可变长度)
     * 数据块内容格式见 GB/T 19056 中相关内容，包含 GB/T 19056 要求的完整数据包，可为空
     */
    private Buffer dataBlock;

    public T8700DrivingRecordDataCollection() {
        super();
    }

    public T8700DrivingRecordDataCollection(JT808Header header) {
        super(header);
    }

    /**
     * 构造行驶记录数据采集命令消息
     *
     * @param commandWord 命令字
     * @param dataBlock   数据块
     */
    public T8700DrivingRecordDataCollection(byte commandWord, Buffer dataBlock) {
        this.commandWord = commandWord;
        this.dataBlock = dataBlock;
    }

    /**
     * 创建只有命令字的消息（无数据块）
     *
     * @param commandWord 命令字
     * @return 行驶记录数据采集命令消息
     */
    public static T8700DrivingRecordDataCollection createCommandOnly(byte commandWord) {
        return new T8700DrivingRecordDataCollection(commandWord, null);
    }

    /**
     * 创建带数据块的消息
     *
     * @param commandWord 命令字
     * @param dataBlock   数据块
     * @return 行驶记录数据采集命令消息
     */
    public static T8700DrivingRecordDataCollection createWithDataBlock(byte commandWord, Buffer dataBlock) {
        return new T8700DrivingRecordDataCollection(commandWord, dataBlock);
    }

    /**
     * 创建带数据块的消息（字节数组形式）
     *
     * @param commandWord 命令字
     * @param dataBytes   数据块字节数组
     * @return 行驶记录数据采集命令消息
     */
    public static T8700DrivingRecordDataCollection createWithDataBlock(byte commandWord, byte[] dataBytes) {
        Buffer dataBlock = dataBytes != null && dataBytes.length > 0 ? Buffer.buffer(dataBytes) : null;
        return new T8700DrivingRecordDataCollection(commandWord, dataBlock);
    }

    @Override
    public int getMessageId() {
        return 0x8700;
    }

    @Override
    public Buffer encodeBody() {
        Buffer buffer = Buffer.buffer();

        // 命令字 (1字节)
        buffer.appendByte(commandWord);

        // 数据块 (可变长度，可为空)
        if (dataBlock != null && dataBlock.length() > 0) {
            buffer.appendBuffer(dataBlock);
        }

        return buffer;
    }

    @Override
    public void decodeBody(Buffer body) {
        int index = 0;

        // 命令字 (1字节)
        if (body.length() > index) {
            commandWord = body.getByte(index);
            index += 1;
        }

        // 数据块 (剩余字节)
        if (index < body.length()) {
            dataBlock = body.getBuffer(index, body.length());
        } else {
            dataBlock = null;
        }
    }

    /**
     * 获取命令字
     *
     * @return 命令字
     */
    public byte getCommandWord() {
        return commandWord;
    }

    /**
     * 设置命令字
     *
     * @param commandWord 命令字
     */
    public void setCommandWord(byte commandWord) {
        this.commandWord = commandWord;
    }

    /**
     * 获取数据块
     *
     * @return 数据块
     */
    public Buffer getDataBlock() {
        return dataBlock;
    }

    /**
     * 设置数据块
     *
     * @param dataBlock 数据块
     */
    public void setDataBlock(Buffer dataBlock) {
        this.dataBlock = dataBlock;
    }

    /**
     * 获取数据块字节数组
     *
     * @return 数据块字节数组，如果数据块为空则返回null
     */
    public byte[] getDataBlockBytes() {
        return dataBlock != null ? dataBlock.getBytes() : null;
    }

    /**
     * 设置数据块字节数组
     *
     * @param dataBytes 数据块字节数组
     */
    public void setDataBlockBytes(byte[] dataBytes) {
        this.dataBlock = dataBytes != null && dataBytes.length > 0 ? Buffer.buffer(dataBytes) : null;
    }

    /**
     * 检查是否有数据块
     *
     * @return 如果有数据块返回true，否则返回false
     */
    public boolean hasDataBlock() {
        return dataBlock != null && dataBlock.length() > 0;
    }

    /**
     * 获取数据块长度
     *
     * @return 数据块长度，如果数据块为空则返回0
     */
    public int getDataBlockLength() {
        return dataBlock != null ? dataBlock.length() : 0;
    }

    @Override
    public String toString() {
        return "T8700DrivingRecordDataCollection{" +
                "commandWord=0x" + String.format("%02X", commandWord & 0xFF) +
                ", dataBlockLength=" + getDataBlockLength() +
                ", hasDataBlock=" + hasDataBlock() +
                "}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        T8700DrivingRecordDataCollection that = (T8700DrivingRecordDataCollection) o;
        return commandWord == that.commandWord &&
                Objects.equals(dataBlock, that.dataBlock);
    }

    @Override
    public int hashCode() {
        return Objects.hash(commandWord, dataBlock);
    }

    /**
     * 常用命令字常量
     * 具体命令字定义参见 GB/T 19056 标准
     */
    public static class CommandWords {
        // 注意：以下常量仅为示例，具体命令字值需要参考 GB/T 19056 标准
        
        /** 读取行驶记录仪基本信息 */
        public static final byte READ_BASIC_INFO = (byte) 0x01;
        
        /** 读取行驶记录仪参数 */
        public static final byte READ_PARAMETERS = (byte) 0x02;
        
        /** 读取行驶记录仪时间 */
        public static final byte READ_TIME = (byte) 0x03;
        
        /** 设置行驶记录仪时间 */
        public static final byte SET_TIME = (byte) 0x04;
        
        /** 读取车辆信息 */
        public static final byte READ_VEHICLE_INFO = (byte) 0x05;
        
        /** 读取驾驶员信息 */
        public static final byte READ_DRIVER_INFO = (byte) 0x06;
        
        /** 读取行驶状态记录 */
        public static final byte READ_DRIVING_STATUS = (byte) 0x07;
        
        /** 读取事故疑点记录 */
        public static final byte READ_ACCIDENT_RECORD = (byte) 0x08;
        
        /** 读取超时驾驶记录 */
        public static final byte READ_OVERTIME_DRIVING = (byte) 0x09;
        
        /** 读取速度状态日志 */
        public static final byte READ_SPEED_STATUS_LOG = (byte) 0x0A;
    }
}