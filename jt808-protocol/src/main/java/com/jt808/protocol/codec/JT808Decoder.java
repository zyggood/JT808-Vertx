package com.jt808.protocol.codec;

import com.jt808.common.JT808Constants;
import com.jt808.common.exception.ProtocolException;
import com.jt808.common.util.ByteUtils;
import com.jt808.protocol.message.*;
import io.vertx.core.buffer.Buffer;

/**
 * JT808消息解码器
 */
public class JT808Decoder {
    
    /**
     * 解码JT808消息
     * @param buffer 原始数据
     * @return 解码后的消息
     * @throws ProtocolException 协议异常
     */
    public JT808Message decode(Buffer buffer) throws ProtocolException {
        if (buffer == null || buffer.length() < 5) {
            throw new ProtocolException("消息长度不足");
        }
        
        // 检查标识位
        if (buffer.getByte(0) != JT808Constants.PROTOCOL_FLAG || 
            buffer.getByte(buffer.length() - 1) != JT808Constants.PROTOCOL_FLAG) {
            throw new ProtocolException("消息标识位错误");
        }
        
        // 去除标识位
        Buffer messageBuffer = buffer.getBuffer(1, buffer.length() - 1);
        
        // 反转义处理
        Buffer unescapedBuffer = ByteUtils.unescape(messageBuffer);
        
        if (unescapedBuffer.length() < 12) {
            throw new ProtocolException("消息长度不足");
        }
        
        // 分离校验码
        int messageLength = unescapedBuffer.length() - 1;
        byte receivedChecksum = unescapedBuffer.getByte(messageLength);
        Buffer dataBuffer = unescapedBuffer.getBuffer(0, messageLength);
        
        // 验证校验码
        byte calculatedChecksum = ByteUtils.calculateChecksum(dataBuffer, 0, dataBuffer.length());
        if (receivedChecksum != calculatedChecksum) {
            throw new ProtocolException("校验码错误");
        }
        
        // 解码消息头
        JT808Header header = decodeHeader(dataBuffer);
        
        // 计算消息头长度
        int headerLength = calculateHeaderLength(header);
        
        // 解码消息体
        Buffer bodyBuffer = null;
        if (dataBuffer.length() > headerLength) {
            bodyBuffer = dataBuffer.getBuffer(headerLength, dataBuffer.length());
        }
        
        // 创建消息对象（这里需要根据消息ID创建具体的消息类型）
        JT808Message message = createMessage(header.getMessageId());
        message.setHeader(header);
        message.setChecksum(receivedChecksum);
        
        // 解码消息体
        if (bodyBuffer != null && bodyBuffer.length() > 0) {
            message.decodeBody(bodyBuffer);
        }
        
        return message;
    }
    
    /**
     * 解码消息头
     * @param buffer 数据缓冲区
     * @return 消息头
     * @throws ProtocolException 协议异常
     */
    private JT808Header decodeHeader(Buffer buffer) throws ProtocolException {
        if (buffer.length() < 12) {
            throw new ProtocolException("消息头长度不足");
        }
        
        JT808Header header = new JT808Header();
        int offset = 0;
        
        // 消息ID（2字节）
        header.setMessageId(buffer.getUnsignedShort(offset));
        offset += 2;
        
        // 消息体属性（2字节）
        header.setMessageProperty(buffer.getUnsignedShort(offset));
        offset += 2;
        
        // 协议版本号（1字节，2019版本才有）
        if (isVersion2019(header.getMessageProperty())) {
            header.setProtocolVersion(buffer.getByte(offset));
            offset += 1;
        }
        
        // 终端手机号（6字节BCD码）
        byte[] phoneBcd = new byte[6];
        buffer.getBytes(offset, offset + 6, phoneBcd);
        // 解码BCD并保持12位格式（保留前导零）
        StringBuilder phoneBuilder = new StringBuilder();
        for (byte b : phoneBcd) {
            int high = (b >> 4) & 0x0F;
            int low = b & 0x0F;
            phoneBuilder.append(high).append(low);
        }
        // 移除前导零，但保留至少一位数字
        String phoneStr = phoneBuilder.toString();
        phoneStr = phoneStr.replaceAll("^0+", ""); // 移除开头的0
        if (phoneStr.isEmpty()) {
            phoneStr = "0";
        }
        header.setPhoneNumber(phoneStr);
        offset += 6;
        
        // 消息流水号（2字节）
        header.setSerialNumber(buffer.getUnsignedShort(offset));
        offset += 2;
        
        // 消息包封装项（分包时才有，4字节）
        if (header.isSubpackage()) {
            if (buffer.length() < offset + 4) {
                throw new ProtocolException("分包信息长度不足");
            }
            int totalPackages = buffer.getUnsignedShort(offset);
            int packageSequence = buffer.getUnsignedShort(offset + 2);
            header.setPackageInfo(new JT808Header.PackageInfo(totalPackages, packageSequence));
        }
        
        return header;
    }
    
    /**
     * 计算消息头长度
     * @param header 消息头
     * @return 消息头长度
     */
    private int calculateHeaderLength(JT808Header header) {
        int length = 12; // 基本长度
        
        // 2019版本有协议版本号
        if (header.getProtocolVersion() != 0) {
            length += 1;
        }
        
        // 分包时有分包信息
        if (header.isSubpackage()) {
            length += 4;
        }
        
        return length;
    }
    
    /**
     * 判断是否为2019版本协议
     * @param messageProperty 消息体属性
     * @return true表示2019版本
     */
    private boolean isVersion2019(int messageProperty) {
        // 2019版本在消息体属性的第14位（从0开始计数）有版本标识位
        // 当第14位为1时，表示2019版本协议
        return (messageProperty & 0x4000) != 0;
    }
    
    /**
     * 根据消息ID创建消息对象
     * @param messageId 消息ID
     * @return 消息对象
     */
    private JT808Message createMessage(int messageId) {
        // 根据消息ID创建对应的消息实例
        switch (messageId) {
            case 0x0001: // 终端通用应答
                return new T0001TerminalCommonResponse();
            case 0x0002: // 终端心跳
                return new T0002TerminalHeartbeat();
            case 0x0100: // 终端注册
                return new T0100TerminalRegister();
            case 0x0102: // 终端鉴权
                return new T0102TerminalAuth();
            case 0x0104: // 查询终端参数应答
                return new T0104QueryTerminalParametersResponse();
            case 0x0200: // 位置信息汇报
                return new T0200LocationReport();
            case 0x8001: // 平台通用应答
                return new T8001PlatformCommonResponse();
            case 0x8100: // 终端注册应答
                return new T8100TerminalRegisterResponse();
            case 0x8104: // 查询终端参数
                return new T8104QueryTerminalParameters();
            case 0x8106: // 查询指定终端参数
                return new T8106QuerySpecificTerminalParameters();
            default:
                // 未知消息类型，使用通用消息
                return new GenericJT808Message(messageId);
        }
    }
    
    /**
     * 通用JT808消息实现
     */
    private static class GenericJT808Message extends JT808Message {
        private final int messageId;
        private Buffer bodyData;
        
        public GenericJT808Message(int messageId) {
            this.messageId = messageId;
        }
        
        @Override
        public int getMessageId() {
            return messageId;
        }
        
        @Override
        public Buffer encodeBody() {
            return bodyData != null ? bodyData : Buffer.buffer();
        }
        
        @Override
        public void decodeBody(Buffer body) {
            this.bodyData = body;
        }
    }
}