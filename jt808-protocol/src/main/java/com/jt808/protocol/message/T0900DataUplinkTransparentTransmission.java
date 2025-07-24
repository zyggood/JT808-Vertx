package com.jt808.protocol.message;

import com.jt808.protocol.constants.MessageTypes;
import io.vertx.core.buffer.Buffer;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * T0900数据上行透传
 * 消息ID: 0x0900
 */
public class T0900DataUplinkTransparentTransmission extends JT808Message {
    
    /**
     * 获取消息ID
     * @return 消息ID 0x0900
     */
    @Override
    public int getMessageId() {
        return 0x0900;
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
     */
    public enum TransparentMessageType {
        GNSS_MODULE_DETAILED_POSITIONING_DATA(0x00, "GNSS模块详细定位数据"),
        ROAD_TRANSPORT_CERTIFICATE_IC_CARD_INFO(0x0B, "道路运输证IC卡信息"),
        SERIAL_PORT_1_TRANSPARENT(0x41, "串口1透传消息"),
        SERIAL_PORT_2_TRANSPARENT(0x42, "串口2透传消息"),
        USER_DEFINED_TRANSPARENT_F0(0xF0, "用户自定义透传消息F0"),
        USER_DEFINED_TRANSPARENT_F1(0xF1, "用户自定义透传消息F1"),
        USER_DEFINED_TRANSPARENT_F2(0xF2, "用户自定义透传消息F2"),
        USER_DEFINED_TRANSPARENT_F3(0xF3, "用户自定义透传消息F3"),
        USER_DEFINED_TRANSPARENT_F4(0xF4, "用户自定义透传消息F4"),
        USER_DEFINED_TRANSPARENT_F5(0xF5, "用户自定义透传消息F5"),
        USER_DEFINED_TRANSPARENT_F6(0xF6, "用户自定义透传消息F6"),
        USER_DEFINED_TRANSPARENT_F7(0xF7, "用户自定义透传消息F7"),
        USER_DEFINED_TRANSPARENT_F8(0xF8, "用户自定义透传消息F8"),
        USER_DEFINED_TRANSPARENT_F9(0xF9, "用户自定义透传消息F9"),
        USER_DEFINED_TRANSPARENT_FA(0xFA, "用户自定义透传消息FA"),
        USER_DEFINED_TRANSPARENT_FB(0xFB, "用户自定义透传消息FB"),
        USER_DEFINED_TRANSPARENT_FC(0xFC, "用户自定义透传消息FC"),
        USER_DEFINED_TRANSPARENT_FD(0xFD, "用户自定义透传消息FD"),
        USER_DEFINED_TRANSPARENT_FE(0xFE, "用户自定义透传消息FE"),
        USER_DEFINED_TRANSPARENT_FF(0xFF, "用户自定义透传消息FF");
        
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
                if (type.getValue() == value) {
                    return type;
                }
            }
            // 如果是用户自定义范围(0xF0-0xFF)但不在枚举中，返回最接近的
            if (value >= 0xF0 && value <= 0xFF) {
                return USER_DEFINED_TRANSPARENT_F0;
            }
            throw new IllegalArgumentException("未知的透传消息类型: 0x" + Integer.toHexString(value).toUpperCase());
        }
    }
    
    /**
     * 默认构造函数
     */
    public T0900DataUplinkTransparentTransmission() {
        super();
        this.messageType = TransparentMessageType.GNSS_MODULE_DETAILED_POSITIONING_DATA;
        this.messageContent = new byte[0];
    }
    
    /**
     * 构造函数
     * @param messageType 透传消息类型
     * @param messageContent 透传消息内容（字节数组）
     */
    public T0900DataUplinkTransparentTransmission(TransparentMessageType messageType, byte[] messageContent) {
        super();
        this.messageType = messageType != null ? messageType : TransparentMessageType.GNSS_MODULE_DETAILED_POSITIONING_DATA;
        this.messageContent = messageContent != null ? messageContent.clone() : new byte[0];
    }
    
    /**
     * 构造函数
     * @param messageType 透传消息类型
     * @param messageContent 透传消息内容（字符串）
     */
    public T0900DataUplinkTransparentTransmission(TransparentMessageType messageType, String messageContent) {
        super();
        this.messageType = messageType != null ? messageType : TransparentMessageType.GNSS_MODULE_DETAILED_POSITIONING_DATA;
        this.messageContent = messageContent != null ? messageContent.getBytes(StandardCharsets.UTF_8) : new byte[0];
    }
    
    /**
     * 编码消息体
     * @return 编码后的消息体
     */
    @Override
    public Buffer encodeBody() {
        Buffer buffer = Buffer.buffer();
        buffer.appendByte((byte) messageType.getValue());
        if (messageContent != null && messageContent.length > 0) {
            buffer.appendBytes(messageContent);
        }
        return buffer;
    }
    
    /**
     * 解码消息体
     * @param body 消息体数据
     */
    @Override
    public void decodeBody(Buffer body) {
        if (body == null || body.length() < 1) {
            throw new IllegalArgumentException("消息体数据不能为空且长度至少为1字节");
        }
        
        // 读取透传消息类型
        int typeValue = body.getByte(0) & 0xFF;
        this.messageType = TransparentMessageType.fromValue(typeValue);
        
        // 读取透传消息内容
        if (body.length() > 1) {
            this.messageContent = new byte[body.length() - 1];
            body.getBytes(1, body.length(), this.messageContent);
        } else {
            this.messageContent = new byte[0];
        }
    }
    
    /**
     * 静态解码方法
     * @param buffer 消息体数据
     * @return 解码后的消息对象
     */
    public static T0900DataUplinkTransparentTransmission decode(Buffer buffer) {
        T0900DataUplinkTransparentTransmission message = new T0900DataUplinkTransparentTransmission();
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
    
    // Getters and Setters
    public TransparentMessageType getMessageType() {
        return messageType;
    }
    
    public void setMessageType(TransparentMessageType messageType) {
        this.messageType = messageType != null ? messageType : TransparentMessageType.GNSS_MODULE_DETAILED_POSITIONING_DATA;
    }
    
    public byte[] getMessageContent() {
        return messageContent != null ? messageContent.clone() : new byte[0];
    }
    
    public void setMessageContent(byte[] messageContent) {
        this.messageContent = messageContent != null ? messageContent.clone() : new byte[0];
    }
    
    public void setMessageContent(String messageContent) {
        this.messageContent = messageContent != null ? messageContent.getBytes(StandardCharsets.UTF_8) : new byte[0];
    }
    
    public String getMessageContentAsString() {
        return messageContent != null ? new String(messageContent, StandardCharsets.UTF_8) : "";
    }
    
    public int getMessageContentLength() {
        return messageContent != null ? messageContent.length : 0;
    }
    
    @Override
    public String toString() {
        return "T0900DataUplinkTransparentTransmission{" +
                "messageType=" + messageType +
                ", messageContentLength=" + getMessageContentLength() +
                ", messageContent='" + getMessageContentAsString() + '\'' +
                '}';
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        T0900DataUplinkTransparentTransmission that = (T0900DataUplinkTransparentTransmission) o;
        return messageType == that.messageType &&
                java.util.Arrays.equals(messageContent, that.messageContent);
    }
    
    @Override
    public int hashCode() {
        int result = Objects.hash(messageType);
        result = 31 * result + java.util.Arrays.hashCode(messageContent);
        return result;
    }
}