package com.jt808.protocol.message;

import com.jt808.protocol.constants.MessageTypes;
import io.vertx.core.buffer.Buffer;

import java.util.Objects;

/**
 * T8805单条存储多媒体数据检索上传命令
 * 消息ID: 0x8805
 * 
 * 消息体数据格式：
 * - 多媒体ID (DWORD, 4字节): >0
 * - 删除标志 (BYTE, 1字节): 0-保留，1-删除
 */
public class T8805SingleMultimediaDataRetrievalUploadCommand extends JT808Message {
    
    /**
     * 获取消息ID
     * @return 消息ID 0x8805
     */
    @Override
    public int getMessageId() {
        return 0x8805;
    }
    
    /**
     * 多媒体ID
     */
    private long multimediaId;
    
    /**
     * 删除标志
     */
    private DeleteFlag deleteFlag;
    
    /**
     * 删除标志枚举
     */
    public enum DeleteFlag {
        KEEP(0, "保留"),
        DELETE(1, "删除");
        
        private final int value;
        private final String description;
        
        DeleteFlag(int value, String description) {
            this.value = value;
            this.description = description;
        }
        
        public int getValue() {
            return value;
        }
        
        public String getDescription() {
            return description;
        }
        
        public static DeleteFlag fromValue(int value) {
            for (DeleteFlag flag : values()) {
                if (flag.value == value) {
                    return flag;
                }
            }
            throw new IllegalArgumentException("未知的删除标志值: " + value);
        }
    }
    
    /**
     * 默认构造函数
     */
    public T8805SingleMultimediaDataRetrievalUploadCommand() {
        super();
        this.multimediaId = 0L;
        this.deleteFlag = DeleteFlag.KEEP;
    }
    
    /**
     * 构造函数
     * 
     * @param multimediaId 多媒体ID
     * @param deleteFlag 删除标志
     */
    public T8805SingleMultimediaDataRetrievalUploadCommand(long multimediaId, DeleteFlag deleteFlag) {
        super();
        if (multimediaId <= 0) {
            throw new IllegalArgumentException("多媒体ID必须大于0");
        }
        this.multimediaId = multimediaId;
        this.deleteFlag = deleteFlag != null ? deleteFlag : DeleteFlag.KEEP;
    }
    
    @Override
    public Buffer encodeBody() {
        Buffer buffer = Buffer.buffer();
        
        // 多媒体ID (DWORD, 4字节)
        buffer.appendUnsignedInt(multimediaId);
        
        // 删除标志 (BYTE, 1字节)
        buffer.appendByte((byte) deleteFlag.getValue());
        
        return buffer;
    }
    
    @Override
    public void decodeBody(Buffer buffer) {
        if (buffer.length() < 5) {
            throw new IllegalArgumentException("消息体长度不足，至少需要5字节");
        }
        
        int offset = 0;
        
        // 多媒体ID (DWORD, 4字节)
        this.multimediaId = buffer.getUnsignedInt(offset);
        offset += 4;
        
        if (this.multimediaId <= 0) {
            throw new IllegalArgumentException("多媒体ID必须大于0，当前值: " + this.multimediaId);
        }
        
        // 删除标志 (BYTE, 1字节)
        byte deleteFlagValue = buffer.getByte(offset);
        this.deleteFlag = DeleteFlag.fromValue(deleteFlagValue & 0xFF);
    }
    
    /**
     * 静态解码方法
     */
    public static T8805SingleMultimediaDataRetrievalUploadCommand decode(Buffer buffer) {
        T8805SingleMultimediaDataRetrievalUploadCommand command = new T8805SingleMultimediaDataRetrievalUploadCommand();
        command.decodeBody(buffer);
        return command;
    }
    
    /**
     * 编码消息体（兼容旧接口）
     * @return 编码后的消息体
     */
    public Buffer encode() {
        return encodeBody();
    }
    
    // Getters and Setters
    public long getMultimediaId() {
        return multimediaId;
    }
    
    public void setMultimediaId(long multimediaId) {
        if (multimediaId <= 0) {
            throw new IllegalArgumentException("多媒体ID必须大于0");
        }
        this.multimediaId = multimediaId;
    }
    
    public DeleteFlag getDeleteFlag() {
        return deleteFlag;
    }
    
    public void setDeleteFlag(DeleteFlag deleteFlag) {
        this.deleteFlag = deleteFlag != null ? deleteFlag : DeleteFlag.KEEP;
    }
    
    @Override
    public String toString() {
        return "T8805SingleMultimediaDataRetrievalUploadCommand{" +
                "multimediaId=" + multimediaId +
                ", deleteFlag=" + deleteFlag.getDescription() +
                ", messageId=0x" + Integer.toHexString(getMessageId()).toUpperCase() +
                "}";
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        T8805SingleMultimediaDataRetrievalUploadCommand that = (T8805SingleMultimediaDataRetrievalUploadCommand) o;
        return multimediaId == that.multimediaId &&
                deleteFlag == that.deleteFlag;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(multimediaId, deleteFlag);
    }
}