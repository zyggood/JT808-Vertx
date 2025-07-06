package com.jt808.protocol.example;

import com.jt808.protocol.factory.JT808MessageFactory;
import com.jt808.protocol.message.*;
import com.jt808.protocol.util.ChecksumUtils;
import com.jt808.protocol.util.EscapeUtils;
import com.jt808.common.exception.ProtocolException;
import io.vertx.core.buffer.Buffer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JT808组件使用示例
 * 展示如何使用消息工厂、校验码工具和转义工具
 */
class JT808ComponentsExample {
    
    private static final Logger logger = LoggerFactory.getLogger(JT808ComponentsExample.class);
    
    @Test
    @DisplayName("完整的消息处理流程示例")
    void completeMessageProcessingExample() throws ProtocolException {
        // 1. 使用消息工厂创建消息
        JT808MessageFactory factory = JT808MessageFactory.getInstance();
        
        // 创建终端心跳消息
        JT808Message heartbeat = factory.createMessage(0x0002);
        assertInstanceOf(T0002TerminalHeartbeat.class, heartbeat);
        
        // 设置消息头
        JT808Header header = new JT808Header(0x0002, "13800138000", 1);
        heartbeat.setHeader(header);
        
        logger.info("1. 创建消息: {}", heartbeat.getClass().getSimpleName());
        
        // 2. 编码消息
        Buffer encodedMessage = factory.encodeMessage(heartbeat);
        logger.info("2. 编码后消息长度: {} 字节", encodedMessage.length());
        
        // 3. 验证校验码
        ChecksumUtils.ChecksumResult checksumResult = ChecksumUtils.verifyCompleteMessage(encodedMessage);
        assertTrue(checksumResult.isValid());
        logger.info("3. 校验码验证: {}", checksumResult.getMessage());
        logger.info("   实际校验码: 0x{}", String.format("%02X", checksumResult.getActualChecksum() & 0xFF));
        logger.info("   期望校验码: 0x{}", String.format("%02X", checksumResult.getExpectedChecksum() & 0xFF));
        
        // 4. 解析消息
        JT808Message parsedMessage = factory.parseMessage(encodedMessage);
        assertEquals(heartbeat.getMessageId(), parsedMessage.getMessageId());
        logger.info("4. 解析消息成功: {}", parsedMessage.getClass().getSimpleName());
    }
    
    @Test
    @DisplayName("转义处理示例")
    void escapeProcessingExample() {
        // 创建包含需要转义字符的数据
        Buffer originalData = Buffer.buffer(new byte[]{
            0x01, 0x7E, 0x02, 0x7D, 0x03, 0x7E, 0x7D, 0x04
        });
        
        logger.info("原始数据: {}", bytesToHex(originalData.getBytes()));
        
        // 1. 检查是否需要转义
        boolean needsEscape = EscapeUtils.needsEscape(originalData);
        assertTrue(needsEscape);
        logger.info("1. 需要转义: {}", needsEscape);
        
        // 2. 统计需要转义的字节数
        int escapeCount = EscapeUtils.countEscapeBytes(originalData);
        assertEquals(4, escapeCount);
        logger.info("2. 需要转义的字节数: {}", escapeCount);
        
        // 3. 计算转义后的长度
        int escapedLength = EscapeUtils.calculateEscapedLength(originalData.length(), escapeCount);
        logger.info("3. 转义后预期长度: {}", escapedLength);
        
        // 4. 执行转义
        Buffer escapedData = EscapeUtils.escape(originalData);
        assertEquals(escapedLength, escapedData.length());
        logger.info("4. 转义后数据: {}", bytesToHex(escapedData.getBytes()));
        
        // 5. 验证转义数据
        EscapeUtils.EscapeValidationResult validation = EscapeUtils.validateEscapedData(escapedData);
        assertTrue(validation.isValid());
        logger.info("5. 转义数据验证: {}", validation.getMessage());
        
        // 6. 反转义
        Buffer unescapedData = EscapeUtils.unescape(escapedData);
        assertArrayEquals(originalData.getBytes(), unescapedData.getBytes());
        logger.info("6. 反转义后数据: {}", bytesToHex(unescapedData.getBytes()));
        logger.info("7. 转义/反转义对称性验证: 通过");
    }
    
    @Test
    @DisplayName("校验码计算示例")
    void checksumCalculationExample() {
        // 创建测试数据
        byte[] messageData = {
            (byte) 0x00, (byte) 0x02,  // 消息ID
            (byte) 0x00, (byte) 0x00,  // 消息体属性
            (byte) 0x01, (byte) 0x38, (byte) 0x00, (byte) 0x13, (byte) 0x80, (byte) 0x00,  // 终端手机号(BCD)
            (byte) 0x00, (byte) 0x01   // 消息流水号
        };
        
        logger.info("消息数据: {}", bytesToHex(messageData));
        
        // 1. 计算校验码
        byte checksum = ChecksumUtils.calculateChecksum(messageData);
        logger.info("1. 计算的校验码: 0x{}", String.format("%02X", checksum & 0xFF));
        
        // 2. 验证校验码
        boolean isValid = ChecksumUtils.verifyChecksum(messageData, checksum);
        assertTrue(isValid);
        logger.info("2. 校验码验证: {}", (isValid ? "通过" : "失败"));
        
        // 3. 使用Buffer计算校验码
        Buffer buffer = Buffer.buffer(messageData);
        byte bufferChecksum = ChecksumUtils.calculateChecksum(buffer);
        assertEquals(checksum, bufferChecksum);
        logger.info("3. Buffer校验码: 0x{}", String.format("%02X", bufferChecksum & 0xFF));
        
        // 4. 部分数据校验码计算
        byte partialChecksum = ChecksumUtils.calculateChecksum(messageData, 2, 4);
        logger.info("4. 部分数据校验码: 0x{}", String.format("%02X", partialChecksum & 0xFF));
        
        // 5. 创建完整消息并验证
        Buffer completeMessage = Buffer.buffer();
        completeMessage.appendByte((byte) 0x7E);  // 起始标识位
        completeMessage.appendBytes(messageData); // 消息数据
        completeMessage.appendByte(checksum);     // 校验码
        completeMessage.appendByte((byte) 0x7E);  // 结束标识位
        
        ChecksumUtils.ChecksumResult result = ChecksumUtils.verifyCompleteMessage(completeMessage);
        assertTrue(result.isValid());
        logger.info("5. 完整消息校验: {}", result);
    }
    
    @Test
    @DisplayName("自定义消息类型示例")
    void customMessageTypeExample() {
        JT808MessageFactory factory = JT808MessageFactory.getInstance();
        
        // 1. 注册自定义消息类型
        factory.registerMessage(0x9001, () -> new CustomTestMessage(0x9001));
        
        // 2. 检查是否支持
        assertTrue(factory.isSupported(0x9001));
        logger.info("1. 自定义消息类型 0x9001 注册成功");
        
        // 3. 创建自定义消息
        JT808Message customMessage = factory.createMessage(0x9001);
        assertInstanceOf(CustomTestMessage.class, customMessage);
        assertEquals(0x9001, customMessage.getMessageId());
        logger.info("2. 创建自定义消息: {}", customMessage.getClass().getSimpleName());
        
        // 4. 获取所有支持的消息ID
        var supportedIds = factory.getSupportedMessageIds();
        assertTrue(supportedIds.contains(0x9001));
        logger.info("3. 支持的消息类型数量: {}", supportedIds.size());
    }
    
    @Test
    @DisplayName("错误处理示例")
    void errorHandlingExample() {
        // 1. 无效的转义数据
        Buffer invalidEscapedData = Buffer.buffer(new byte[]{0x01, 0x7E, 0x02}); // 包含未转义的0x7E
        EscapeUtils.EscapeValidationResult escapeResult = EscapeUtils.validateEscapedData(invalidEscapedData);
        assertFalse(escapeResult.isValid());
        logger.info("1. 无效转义数据检测: {}", escapeResult.getMessage());
        
        // 2. 校验码错误的消息
        Buffer invalidMessage = Buffer.buffer();
        invalidMessage.appendByte((byte) 0x7E);
        invalidMessage.appendBytes(new byte[]{0x01, 0x02, 0x03, 0x04});
        invalidMessage.appendByte((byte) 0x99); // 错误的校验码
        invalidMessage.appendByte((byte) 0x7E);
        
        ChecksumUtils.ChecksumResult checksumResult = ChecksumUtils.verifyCompleteMessage(invalidMessage);
        assertFalse(checksumResult.isValid());
        logger.info("2. 校验码错误检测: {}", checksumResult.getMessage());
        
        // 3. 消息长度不足
        Buffer shortMessage = Buffer.buffer(new byte[]{0x7E, 0x01, 0x7E});
        ChecksumUtils.ChecksumResult shortResult = ChecksumUtils.verifyCompleteMessage(shortMessage);
        assertFalse(shortResult.isValid());
        logger.info("3. 消息长度不足检测: {}", shortResult.getMessage());
    }
    
    /**
     * 将字节数组转换为十六进制字符串
     */
    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X ", b & 0xFF));
        }
        return sb.toString().trim();
    }
    
    /**
     * 自定义测试消息类
     */
    private static class CustomTestMessage extends JT808Message {
        private final int messageId;
        private Buffer bodyData;
        
        public CustomTestMessage(int messageId) {
            this.messageId = messageId;
        }
        
        @Override
        public int getMessageId() {
            return messageId;
        }
        
        @Override
        public Buffer encodeBody() {
            return bodyData != null ? bodyData : Buffer.buffer("Custom Message Body");
        }
        
        @Override
        public void decodeBody(Buffer body) {
            this.bodyData = body;
        }
    }
}