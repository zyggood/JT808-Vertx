package com.jt808.protocol.message;

import com.jt808.protocol.constants.MessageTypes;
import io.vertx.core.buffer.Buffer;

import java.util.Arrays;
import java.util.Objects;

/**
 * T8900数据下行透传
 * 消息ID: 0x8900
 * 
 * 消息体数据格式：
 * - 透传消息类型 (BYTE, 1字节): 透传消息类型定义见表93
 * - 透传消息内容 (可变长度): 具体内容根据透传消息类型确定
 */
public class T8900DataDownlinkTransparentTransmission extends JT808Message {
    
    /**
     * 获取消息ID
     * @return 消息ID 0x8900
     */
    @Override
    public int getMessageId() {
        return 0x8900;
    }
    
    /**
     * 透传消息类型
     */
    private TransparentMessageType messageType;
    
    /**
     * 透传消息内容
     */
    private byte[] messageContent;
    
    /**
     * 透传消息类型枚举
     * 根据JT808协议表93定义
     */
    public enum TransparentMessageType {
        GNSS_MODULE_DETAILED_POSITIONING_DATA(0x00, "GNSS模块详细定位数据"),
        ROAD_TRANSPORT_CERTIFICATE_IC_CARD_INFO(0x0B, "道路运输证IC卡信息"),
        DRIVER_IDENTITY_IC_CARD_INFO(0x0C, "驾驶员身份IC卡信息"),
        GNSS_MODULE_DETAILED_POSITIONING_DATA_WITH_EXTENSION(0xF0, "GNSS模块详细定位数据(扩展)"),
        CUSTOM_TRANSPARENT_DATA(0xFF, "自定义透传数据");
        
        private final int value;
        private final String description;
        
        TransparentMessageType(int value, String description) {
            this.value = value;
            this.description = description;
        }
        
        public int getValue() {
            return value;
        }
        
        public String getDescription() {
            return description;
        }
        
        public static TransparentMessageType fromValue(int value) {
            for (TransparentMessageType type : values()) {
                if (type.value == value) {
                    return type;
                }
            }
            // 如果找不到匹配的类型，返回自定义透传数据类型
            return CUSTOM_TRANSPARENT_DATA;
        }
    }
    
    /**
     * 默认构造函数
     */
    public T8900DataDownlinkTransparentTransmission() {
        super();
        this.messageType = TransparentMessageType.CUSTOM_TRANSPARENT_DATA;
        this.messageContent = new byte[0];
    }
    
    /**
     * 构造函数
     * 
     * @param messageType 透传消息类型
     * @param messageContent 透传消息内容
     */
    public T8900DataDownlinkTransparentTransmission(TransparentMessageType messageType, byte[] messageContent) {
        super();
        this.messageType = messageType != null ? messageType : TransparentMessageType.CUSTOM_TRANSPARENT_DATA;
        this.messageContent = messageContent != null ? messageContent.clone() : new byte[0];
    }
    
    /**
     * 构造函数（使用字符串内容）
     * 
     * @param messageType 透传消息类型
     * @param messageContent 透传消息内容（字符串）
     */
    public T8900DataDownlinkTransparentTransmission(TransparentMessageType messageType, String messageContent) {
        super();
        this.messageType = messageType != null ? messageType : TransparentMessageType.CUSTOM_TRANSPARENT_DATA;
        this.messageContent = messageContent != null ? messageContent.getBytes() : new byte[0];
    }
    
    @Override
    public Buffer encodeBody() {
        Buffer buffer = Buffer.buffer();
        
        // 透传消息类型 (BYTE, 1字节)
        buffer.appendByte((byte) messageType.getValue());
        
        // 透传消息内容 (可变长度)
        if (messageContent != null && messageContent.length > 0) {
            buffer.appendBytes(messageContent);
        }
        
        return buffer;
    }
    
    @Override
    public void decodeBody(Buffer buffer) {
        if (buffer.length() < 1) {
            throw new IllegalArgumentException("消息体长度不足，至少需要1字节");
        }
        
        int offset = 0;
        
        // 透传消息类型 (BYTE, 1字节)
        byte messageTypeValue = buffer.getByte(offset);
        this.messageType = TransparentMessageType.fromValue(messageTypeValue & 0xFF);
        offset += 1;
        
        // 透传消息内容 (可变长度)
        int contentLength = buffer.length() - offset;
        if (contentLength > 0) {
            this.messageContent = new byte[contentLength];
            buffer.getBytes(offset, offset + contentLength, this.messageContent);
        } else {
            this.messageContent = new byte[0];
        }
    }
    
    /**
     * 静态解码方法
     */
    public static T8900DataDownlinkTransparentTransmission decode(Buffer buffer) {
        T8900DataDownlinkTransparentTransmission message = new T8900DataDownlinkTransparentTransmission();
        message.decodeBody(buffer);
        return message;
    }
    
    /**
     * 编码消息体（兼容旧接口）
     * @return 编码后的消息体
     */
    public Buffer encode() {
        return encodeBody();
    }
    
    /**
     * 获取透传消息内容的字符串表示
     */
    public String getMessageContentAsString() {
        if (messageContent == null || messageContent.length == 0) {
            return "";
        }
        return new String(messageContent);
    }
    
    /**
     * 设置透传消息内容（字符串）
     */
    public void setMessageContent(String content) {
        this.messageContent = content != null ? content.getBytes() : new byte[0];
    }
    
    /**
     * 获取透传消息内容长度
     */
    public int getMessageContentLength() {
        return messageContent != null ? messageContent.length : 0;
    }
    
    // Getters and Setters
    public TransparentMessageType getMessageType() {
        return messageType;
    }
    
    public void setMessageType(TransparentMessageType messageType) {
        this.messageType = messageType != null ? messageType : TransparentMessageType.CUSTOM_TRANSPARENT_DATA;
    }
    
    public byte[] getMessageContent() {
        return messageContent != null ? messageContent.clone() : new byte[0];
    }
    
    public void setMessageContent(byte[] messageContent) {
        this.messageContent = messageContent != null ? messageContent.clone() : new byte[0];
    }
    
    @Override
    public String toString() {
        return "T8900DataDownlinkTransparentTransmission{" +
                "messageType=" + messageType.getDescription() +
                ", messageContentLength=" + getMessageContentLength() +
                ", messageContent=" + (messageContent != null && messageContent.length <= 50 ? 
                    Arrays.toString(messageContent) : "[" + getMessageContentLength() + " bytes]") +
                ", messageId=0x" + Integer.toHexString(getMessageId()).toUpperCase() +
                "}";
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        T8900DataDownlinkTransparentTransmission that = (T8900DataDownlinkTransparentTransmission) o;
        return messageType == that.messageType &&
                Arrays.equals(messageContent, that.messageContent);
    }
    
    @Override
    public int hashCode() {
        int result = Objects.hash(messageType);
        result = 31 * result + Arrays.hashCode(messageContent);
        return result;
    }
}