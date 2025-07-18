package com.jt808.protocol.message;

import io.vertx.core.buffer.Buffer;
import java.util.Objects;

/**
 * 0x8701 行驶记录参数下传命令
 * 平台向终端发送行驶记录仪参数下传命令
 * 数据格式见 GB/T 19056 相关要求
 */
public class T8701DrivingRecordParameterTransmission extends JT808Message {

    /**
     * 命令字 (1字节)
     * 命令字列表见 GB/T 19056 中相关要求
     */
    private byte commandWord;

    /**
     * 数据块 (可变长度)
     * 数据块内容格式见 GB/T 19056 中相关内容，包含 GB/T 19056 要求的完整数据包
     */
    private Buffer dataBlock;

    public T8701DrivingRecordParameterTransmission() {
        super();
    }

    public T8701DrivingRecordParameterTransmission(JT808Header header) {
        super(header);
    }

    /**
     * 构造行驶记录参数下传命令消息
     *
     * @param commandWord 命令字
     * @param dataBlock 数据块
     */
    public T8701DrivingRecordParameterTransmission(byte commandWord, Buffer dataBlock) {
        this.commandWord = commandWord;
        this.dataBlock = dataBlock;
    }

    /**
     * 创建仅包含命令字的消息
     *
     * @param commandWord 命令字
     * @return 行驶记录参数下传命令消息
     */
    public static T8701DrivingRecordParameterTransmission createCommandOnly(byte commandWord) {
        return new T8701DrivingRecordParameterTransmission(commandWord, null);
    }

    /**
     * 创建包含命令字和数据块的消息
     *
     * @param commandWord 命令字
     * @param dataBlock 数据块
     * @return 行驶记录参数下传命令消息
     */
    public static T8701DrivingRecordParameterTransmission createWithDataBlock(byte commandWord, Buffer dataBlock) {
        return new T8701DrivingRecordParameterTransmission(commandWord, dataBlock);
    }

    /**
     * 创建包含命令字和数据块的消息
     *
     * @param commandWord 命令字
     * @param dataBytes 数据块字节数组
     * @return 行驶记录参数下传命令消息
     */
    public static T8701DrivingRecordParameterTransmission createWithDataBlock(byte commandWord, byte[] dataBytes) {
        Buffer dataBlock = dataBytes != null && dataBytes.length > 0 ? Buffer.buffer(dataBytes) : null;
        return new T8701DrivingRecordParameterTransmission(commandWord, dataBlock);
    }

    /**
     * 创建设置车辆信息参数消息
     *
     * @param dataBlock 车辆信息参数数据
     * @return 行驶记录参数下传命令消息
     */
    public static T8701DrivingRecordParameterTransmission createSetVehicleInfo(byte[] dataBlock) {
        return new T8701DrivingRecordParameterTransmission((byte) 0x82, Buffer.buffer(dataBlock));
    }

    /**
     * 创建设置初始里程参数消息
     *
     * @param dataBlock 初始里程参数数据
     * @return 行驶记录参数下传命令消息
     */
    public static T8701DrivingRecordParameterTransmission createSetInitialMileage(byte[] dataBlock) {
        return new T8701DrivingRecordParameterTransmission((byte) 0x83, Buffer.buffer(dataBlock));
    }

    /**
     * 创建设置时间参数消息
     *
     * @param dataBlock 时间参数数据
     * @return 行驶记录参数下传命令消息
     */
    public static T8701DrivingRecordParameterTransmission createSetTime(byte[] dataBlock) {
        return new T8701DrivingRecordParameterTransmission((byte) 0x84, Buffer.buffer(dataBlock));
    }

    /**
     * 创建设置脉冲系数参数消息
     *
     * @param dataBlock 脉冲系数参数数据
     * @return 行驶记录参数下传命令消息
     */
    public static T8701DrivingRecordParameterTransmission createSetPulseCoefficient(byte[] dataBlock) {
        return new T8701DrivingRecordParameterTransmission((byte) 0x85, Buffer.buffer(dataBlock));
    }

    @Override
    public int getMessageId() {
        return 0x8701;
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
        if (body == null || body.length() < 1) {
            throw new IllegalArgumentException("行驶记录参数下传命令消息体长度不能少于1字节");
        }
        
        int index = 0;

        // 命令字 (1字节)
        commandWord = body.getByte(index);
        index += 1;

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

    /**
     * 获取命令字的无符号值
     *
     * @return 命令字的无符号值
     */
    public int getCommandWordUnsigned() {
        return Byte.toUnsignedInt(commandWord);
    }

    /**
     * 获取消息描述
     *
     * @return 消息描述
     */
    public String getMessageDescription() {
        return "行驶记录参数下传命令";
    }

    /**
     * 获取命令字描述
     *
     * @return 命令字描述
     */
    public String getCommandDescription() {
        switch (commandWord) {
            case (byte) 0x82:
                return "设置车辆信息";
            case (byte) 0x83:
                return "设置初始里程";
            case (byte) 0x84:
                return "设置时间";
            case (byte) 0x85:
                return "设置脉冲系数";
            case (byte) 0x86:
                return "设置车辆特征系数";
            case (byte) 0x87:
                return "设置车牌号";
            case (byte) 0x88:
                return "设置车辆类型";
            default:
                return "未知命令字: 0x" + String.format("%02X", commandWord);
        }
    }

    @Override
    public String toString() {
        return "T8701DrivingRecordParameterTransmission{" +
                "commandWord=0x" + String.format("%02X", commandWord & 0xFF) +
                ", dataBlockLength=" + getDataBlockLength() +
                ", hasDataBlock=" + hasDataBlock() +
                ", commandDescription='" + getCommandDescription() + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        T8701DrivingRecordParameterTransmission that = (T8701DrivingRecordParameterTransmission) o;
        return commandWord == that.commandWord &&
                Objects.equals(dataBlock, that.dataBlock);
    }

    @Override
    public int hashCode() {
        return Objects.hash(commandWord, dataBlock);
    }

    /**
     * 常用命令字常量
     * 注意：以下常量仅为示例，具体命令字值需要参考 GB/T 19056 标准
     */
    public static class CommandWord {
        /** 设置车辆信息 */
        public static final byte SET_VEHICLE_INFO = (byte) 0x82;
        
        /** 设置初始里程 */
        public static final byte SET_INITIAL_MILEAGE = (byte) 0x83;
        
        /** 设置时间 */
        public static final byte SET_TIME = (byte) 0x84;
        
        /** 设置脉冲系数 */
        public static final byte SET_PULSE_COEFFICIENT = (byte) 0x85;
        
        /** 设置车辆特征系数 */
        public static final byte SET_VEHICLE_FEATURE_COEFFICIENT = (byte) 0x86;
        
        /** 设置车牌号 */
        public static final byte SET_LICENSE_PLATE = (byte) 0x87;
        
        /** 设置车辆类型 */
        public static final byte SET_VEHICLE_TYPE = (byte) 0x88;
    }
}