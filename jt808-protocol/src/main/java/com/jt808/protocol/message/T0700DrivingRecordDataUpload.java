package com.jt808.protocol.message;

import io.vertx.core.buffer.Buffer;
import java.util.Arrays;
import java.util.Objects;

/**
 * 0x0700 行驶记录数据上传
 * 终端收到行驶记录数据采集命令后，上传相应的行驶记录数据
 */
public class T0700DrivingRecordDataUpload extends JT808Message {

    /**
     * 应答流水号 - 对应的行驶记录数据采集命令消息的流水号
     */
    private int responseSerialNumber;

    /**
     * 命令字 - 对应平台发出的命令字
     */
    private byte commandWord;

    /**
     * 数据块 - 数据块内容格式见 GB/T 19056 中相关内容，包含 GB/T 19056 要求的完整数据包
     */
    private byte[] dataBlock;

    public T0700DrivingRecordDataUpload() {
    }

    public T0700DrivingRecordDataUpload(int responseSerialNumber, byte commandWord) {
        this.responseSerialNumber = responseSerialNumber;
        this.commandWord = commandWord;
        this.dataBlock = new byte[0];
    }

    public T0700DrivingRecordDataUpload(int responseSerialNumber, byte commandWord, byte[] dataBlock) {
        this.responseSerialNumber = responseSerialNumber;
        this.commandWord = commandWord;
        this.dataBlock = dataBlock != null ? dataBlock : new byte[0];
    }

    public int getResponseSerialNumber() {
        return responseSerialNumber;
    }

    public void setResponseSerialNumber(int responseSerialNumber) {
        this.responseSerialNumber = responseSerialNumber;
    }

    public byte getCommandWord() {
        return commandWord;
    }

    public void setCommandWord(byte commandWord) {
        this.commandWord = commandWord;
    }

    public byte[] getDataBlock() {
        return dataBlock != null ? Arrays.copyOf(dataBlock, dataBlock.length) : new byte[0];
    }

    public void setDataBlock(byte[] dataBlock) {
        this.dataBlock = dataBlock != null ? Arrays.copyOf(dataBlock, dataBlock.length) : new byte[0];
    }

    /**
     * 创建行驶记录数据上传消息（仅命令字）
     * 
     * @param responseSerialNumber 应答流水号
     * @param commandWord 命令字
     * @return 行驶记录数据上传消息
     */
    public static T0700DrivingRecordDataUpload create(int responseSerialNumber, byte commandWord) {
        return new T0700DrivingRecordDataUpload(responseSerialNumber, commandWord);
    }

    /**
     * 创建行驶记录数据上传消息（带数据块）
     * 
     * @param responseSerialNumber 应答流水号
     * @param commandWord 命令字
     * @param dataBlock 数据块
     * @return 行驶记录数据上传消息
     */
    public static T0700DrivingRecordDataUpload create(int responseSerialNumber, byte commandWord, byte[] dataBlock) {
        return new T0700DrivingRecordDataUpload(responseSerialNumber, commandWord, dataBlock);
    }

    /**
     * 创建驾驶员身份记录数据上传消息
     * 
     * @param responseSerialNumber 应答流水号
     * @param dataBlock 驾驶员身份记录数据
     * @return 行驶记录数据上传消息
     */
    public static T0700DrivingRecordDataUpload createDriverIdentityRecord(int responseSerialNumber, byte[] dataBlock) {
        return new T0700DrivingRecordDataUpload(responseSerialNumber, (byte) 0x08, dataBlock);
    }

    /**
     * 创建实时时间数据上传消息
     * 
     * @param responseSerialNumber 应答流水号
     * @param dataBlock 实时时间数据
     * @return 行驶记录数据上传消息
     */
    public static T0700DrivingRecordDataUpload createRealTimeData(int responseSerialNumber, byte[] dataBlock) {
        return new T0700DrivingRecordDataUpload(responseSerialNumber, (byte) 0x09, dataBlock);
    }

    /**
     * 创建累计行驶里程数据上传消息
     * 
     * @param responseSerialNumber 应答流水号
     * @param dataBlock 累计行驶里程数据
     * @return 行驶记录数据上传消息
     */
    public static T0700DrivingRecordDataUpload createMileageData(int responseSerialNumber, byte[] dataBlock) {
        return new T0700DrivingRecordDataUpload(responseSerialNumber, (byte) 0x0A, dataBlock);
    }

    /**
     * 创建脉冲系数数据上传消息
     * 
     * @param responseSerialNumber 应答流水号
     * @param dataBlock 脉冲系数数据
     * @return 行驶记录数据上传消息
     */
    public static T0700DrivingRecordDataUpload createPulseCoefficientData(int responseSerialNumber, byte[] dataBlock) {
        return new T0700DrivingRecordDataUpload(responseSerialNumber, (byte) 0x0B, dataBlock);
    }

    /**
     * 创建车辆信息数据上传消息
     * 
     * @param responseSerialNumber 应答流水号
     * @param dataBlock 车辆信息数据
     * @return 行驶记录数据上传消息
     */
    public static T0700DrivingRecordDataUpload createVehicleInfoData(int responseSerialNumber, byte[] dataBlock) {
        return new T0700DrivingRecordDataUpload(responseSerialNumber, (byte) 0x0C, dataBlock);
    }

    /**
     * 检查是否包含数据块
     * 
     * @return true-包含数据块，false-不包含数据块
     */
    public boolean hasDataBlock() {
        return dataBlock != null && dataBlock.length > 0;
    }

    /**
     * 获取数据块长度
     * 
     * @return 数据块长度
     */
    public int getDataBlockLength() {
        return dataBlock != null ? dataBlock.length : 0;
    }

    /**
     * 获取应答流水号的无符号值
     * 
     * @return 应答流水号的无符号值
     */
    public long getResponseSerialNumberUnsigned() {
        return Integer.toUnsignedLong(responseSerialNumber);
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
        return "行驶记录数据上传";
    }

    /**
     * 获取命令字描述
     * 
     * @return 命令字描述
     */
    public String getCommandDescription() {
        switch (commandWord) {
            case 0x08:
                return "驾驶员身份记录";
            case 0x09:
                return "实时时间";
            case 0x0A:
                return "累计行驶里程";
            case 0x0B:
                return "脉冲系数";
            case 0x0C:
                return "车辆信息";
            default:
                return "未知命令字: 0x" + String.format("%02X", commandWord);
        }
    }

    @Override
    public int getMessageId() {
        return 0x0700;
    }

    @Override
    public Buffer encodeBody() {
        Buffer buffer = Buffer.buffer();
        
        // 写入应答流水号（2字节）
        buffer.appendUnsignedShort(responseSerialNumber);
        
        // 写入命令字（1字节）
        buffer.appendByte(commandWord);
        
        // 写入数据块（可变长度）
        if (dataBlock != null && dataBlock.length > 0) {
            buffer.appendBytes(dataBlock);
        }
        
        return buffer;
    }

    @Override
    public void decodeBody(Buffer buffer) {
        if (buffer == null || buffer.length() < 3) {
            throw new IllegalArgumentException("行驶记录数据上传消息体长度不能少于3字节");
        }
        
        // 读取应答流水号（2字节）
        this.responseSerialNumber = buffer.getUnsignedShort(0);
        
        // 读取命令字（1字节）
        this.commandWord = buffer.getByte(2);
        
        // 读取数据块（剩余字节）
        if (buffer.length() > 3) {
            int dataLength = buffer.length() - 3;
            this.dataBlock = new byte[dataLength];
            buffer.getBytes(3, buffer.length(), this.dataBlock);
        } else {
            this.dataBlock = new byte[0];
        }
    }

    @Override
    public String toString() {
        return "T0700DrivingRecordDataUpload{" +
                "responseSerialNumber=" + responseSerialNumber +
                ", commandWord=0x" + String.format("%02X", commandWord) +
                ", dataBlockLength=" + getDataBlockLength() +
                ", commandDescription='" + getCommandDescription() + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        T0700DrivingRecordDataUpload that = (T0700DrivingRecordDataUpload) o;
        return responseSerialNumber == that.responseSerialNumber &&
                commandWord == that.commandWord &&
                Arrays.equals(dataBlock, that.dataBlock);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(responseSerialNumber, commandWord);
        result = 31 * result + Arrays.hashCode(dataBlock);
        return result;
    }

    /**
     * 常用命令字常量
     */
    public static class CommandWord {
        /** 驾驶员身份记录 */
        public static final byte DRIVER_IDENTITY_RECORD = 0x08;
        
        /** 实时时间 */
        public static final byte REAL_TIME_DATA = 0x09;
        
        /** 累计行驶里程 */
        public static final byte MILEAGE_DATA = 0x0A;
        
        /** 脉冲系数 */
        public static final byte PULSE_COEFFICIENT_DATA = 0x0B;
        
        /** 车辆信息 */
        public static final byte VEHICLE_INFO_DATA = 0x0C;
    }
}