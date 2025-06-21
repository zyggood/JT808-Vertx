package com.jt808.protocol.codec;

import com.jt808.common.JT808Constants;
import com.jt808.common.exception.ProtocolException;
import com.jt808.common.util.ByteUtils;
import com.jt808.protocol.message.JT808Header;
import com.jt808.protocol.message.JT808Message;
import io.vertx.core.buffer.Buffer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * JT808解码器测试
 */
class JT808DecoderTest {
    
    private JT808Decoder decoder;
    
    @BeforeEach
    void setUp() {
        decoder = new JT808Decoder();
    }
    
    @Test
    void testDecodeValidMessage() throws ProtocolException {
        // 构造一个有效的JT808消息
        Buffer messageBuffer = createValidMessage();
        
        JT808Message message = decoder.decode(messageBuffer);
        
        assertNotNull(message);
        assertNotNull(message.getHeader());
        assertEquals(0x0001, message.getMessageId());
    }
    
    @Test
    void testDecodeNullBuffer() {
        assertThrows(ProtocolException.class, () -> {
            decoder.decode(null);
        });
    }
    
    @Test
    void testDecodeShortBuffer() {
        Buffer shortBuffer = Buffer.buffer(new byte[]{0x7E, 0x01, 0x7E});
        
        assertThrows(ProtocolException.class, () -> {
            decoder.decode(shortBuffer);
        });
    }
    
    @Test
    void testDecodeInvalidStartFlag() {
        Buffer invalidBuffer = Buffer.buffer(new byte[]{0x7F, 0x01, 0x02, 0x03, 0x7E});
        
        assertThrows(ProtocolException.class, () -> {
            decoder.decode(invalidBuffer);
        });
    }
    
    @Test
    void testDecodeInvalidEndFlag() {
        Buffer invalidBuffer = Buffer.buffer(new byte[]{0x7E, 0x01, 0x02, 0x03, 0x7F});
        
        assertThrows(ProtocolException.class, () -> {
            decoder.decode(invalidBuffer);
        });
    }
    
    @Test
    void testDecodeMessageWithEscape() throws ProtocolException {
        // 构造包含转义字符的消息
        Buffer messageBuffer = createMessageWithEscape();
        
        JT808Message message = decoder.decode(messageBuffer);
        
        assertNotNull(message);
        assertNotNull(message.getHeader());
    }
    
    @Test
    void testDecodeHeaderInsufficientLength() {
        // 构造消息头长度不足的消息
        Buffer buffer = Buffer.buffer();
        buffer.appendByte((byte) 0x7E);
        // 添加少于12字节的数据
        buffer.appendBytes(new byte[]{0x00, 0x01, 0x00, 0x02, 0x01});
        buffer.appendByte((byte) 0x7E);
        
        assertThrows(ProtocolException.class, () -> {
            decoder.decode(buffer);
        });
    }
    
    @Test
    void testDecodeVersion2019Message() throws ProtocolException {
        // 构造2019版本的消息（包含协议版本号）
        Buffer messageBuffer = createVersion2019Message();
        
        JT808Message message = decoder.decode(messageBuffer);
        
        assertNotNull(message);
        assertNotNull(message.getHeader());
        assertEquals(1, message.getHeader().getProtocolVersion());
    }
    
    @Test
    void testDecodeSubpackageMessage() throws ProtocolException {
        // 构造分包消息
        Buffer messageBuffer = createSubpackageMessage();
        
        JT808Message message = decoder.decode(messageBuffer);
        
        assertNotNull(message);
        assertNotNull(message.getHeader());
        assertTrue(message.getHeader().isSubpackage());
        assertNotNull(message.getHeader().getPackageInfo());
    }
    
    /**
     * 创建一个有效的JT808消息
     */
    private Buffer createValidMessage() {
        Buffer buffer = Buffer.buffer();
        
        // 标识位
        buffer.appendByte((byte) 0x7E);
        
        // 消息内容（不包含转义）
        Buffer content = Buffer.buffer();
        
        // 消息ID（2字节）
        content.appendUnsignedShort(0x0001);
        
        // 消息体属性（2字节）- 不分包，无加密
        content.appendUnsignedShort(0x0000);
        
        // 终端手机号（6字节BCD）
        byte[] phoneBcd = ByteUtils.toBCD(123456789012L, 6);
        content.appendBytes(phoneBcd);
        
        // 消息流水号（2字节）
        content.appendUnsignedShort(1);
        
        // 消息体（空）
        
        // 校验码（1字节）
        byte checksum = ByteUtils.calculateChecksum(content.getBytes(), 0, content.length());
        content.appendByte(checksum);
        
        // 转义处理
        Buffer escaped = ByteUtils.escape(content);
        buffer.appendBuffer(escaped);
        
        // 结束标识位
        buffer.appendByte((byte) 0x7E);
        
        return buffer;
    }
    
    /**
     * 创建包含转义字符的消息
     */
    private Buffer createMessageWithEscape() {
        Buffer buffer = Buffer.buffer();
        
        // 标识位
        buffer.appendByte((byte) 0x7E);
        
        // 消息内容（包含需要转义的字符）
        Buffer content = Buffer.buffer();
        
        // 消息ID
        content.appendUnsignedShort(0x0001);
        
        // 消息体属性
        content.appendUnsignedShort(0x0002); // 消息体长度为2
        
        // 终端手机号
        byte[] phoneBcd = ByteUtils.toBCD(123456789012L, 6);
        content.appendBytes(phoneBcd);
        
        // 消息流水号
        content.appendUnsignedShort(1);
        
        // 消息体（包含转义字符）
        content.appendByte((byte) 0x7E); // 需要转义
        content.appendByte((byte) 0x7D); // 需要转义
        
        // 校验码
        byte checksum = ByteUtils.calculateChecksum(content.getBytes(), 0, content.length());
        content.appendByte(checksum);
        
        // 转义处理
        Buffer escaped = ByteUtils.escape(content);
        buffer.appendBuffer(escaped);
        
        // 结束标识位
        buffer.appendByte((byte) 0x7E);
        
        return buffer;
    }
    
    /**
     * 创建2019版本的消息
     */
    private Buffer createVersion2019Message() {
        Buffer buffer = Buffer.buffer();
        
        // 标识位
        buffer.appendByte((byte) 0x7E);
        
        // 消息内容
        Buffer content = Buffer.buffer();
        
        // 消息ID
        content.appendUnsignedShort(0x0001);
        
        // 消息体属性（设置版本标识位）
        content.appendUnsignedShort(0x4000); // 设置版本标识位
        
        // 协议版本号（2019版本）
        content.appendByte((byte) 0x01);
        
        // 终端手机号
        byte[] phoneBcd = ByteUtils.toBCD(123456789012L, 6);
        content.appendBytes(phoneBcd);
        
        // 消息流水号
        content.appendUnsignedShort(1);
        
        // 校验码
        byte checksum = ByteUtils.calculateChecksum(content.getBytes(), 0, content.length());
        content.appendByte(checksum);
        
        // 转义处理
        Buffer escaped = ByteUtils.escape(content);
        buffer.appendBuffer(escaped);
        
        // 结束标识位
        buffer.appendByte((byte) 0x7E);
        
        return buffer;
    }
    
    /**
     * 创建分包消息
     */
    private Buffer createSubpackageMessage() {
        Buffer buffer = Buffer.buffer();
        
        // 标识位
        buffer.appendByte((byte) 0x7E);
        
        // 消息内容
        Buffer content = Buffer.buffer();
        
        // 消息ID
        content.appendUnsignedShort(0x0001);
        
        // 消息体属性（设置分包标识位）
        content.appendUnsignedShort(0x2000); // 设置分包标识位
        
        // 终端手机号
        byte[] phoneBcd = ByteUtils.toBCD(123456789012L, 6);
        content.appendBytes(phoneBcd);
        
        // 消息流水号
        content.appendUnsignedShort(1);
        
        // 分包信息
        content.appendUnsignedShort(3); // 总包数
        content.appendUnsignedShort(1); // 包序号
        
        // 校验码
        byte checksum = ByteUtils.calculateChecksum(content.getBytes(), 0, content.length());
        content.appendByte(checksum);
        
        // 转义处理
        Buffer escaped = ByteUtils.escape(content);
        buffer.appendBuffer(escaped);
        
        // 结束标识位
        buffer.appendByte((byte) 0x7E);
        
        return buffer;
    }
}