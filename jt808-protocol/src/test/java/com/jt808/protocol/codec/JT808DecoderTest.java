package com.jt808.protocol.codec;

import com.jt808.common.exception.ProtocolException;
import com.jt808.common.util.ByteUtils;
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
        content.appendUnsignedShort(0x0005); // 消息体长度为5
        
        // 终端手机号
        byte[] phoneBcd = ByteUtils.toBCD(123456789012L, 6);
        content.appendBytes(phoneBcd);
        
        // 消息流水号
        content.appendUnsignedShort(1);
        
        // 消息体（终端通用应答：应答流水号2字节 + 应答ID2字节 + 结果1字节）
        content.appendUnsignedShort(1); // 应答流水号
        content.appendUnsignedShort(0x8001); // 应答ID
        content.appendByte((byte) 0x00); // 结果：成功
        
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

    @Test
    void testDecodeComplexMessage() throws ProtocolException {
        String hex = "020000d40123456789017fff000004000000080006eeb6ad02633df7013800030063200707192359642f000000400101020a0a02010a1e00640001b2070003640e200707192359000100000061646173200827111111010101652f000000410202020a0000000a1e00c8000516150006c81c20070719235900020000000064736d200827111111020202662900000042031e012c00087a23000a2c2a200707192359000300000074706d732008271111110303030067290000004304041e0190000bde31000d90382007071923590004000000006273642008271111110404049d";

        // 将十六进制字符串转换为字节数组
        byte[] bytes = ByteUtils.hexToBytes(hex);

        // 创建包含起始和结束标识位的完整消息
        Buffer buffer = Buffer.buffer();
        buffer.appendByte((byte) 0x7E); // 起始标识位
        buffer.appendBytes(bytes);
        buffer.appendByte((byte) 0x7E); // 结束标识位

        // 解码消息
        JT808Message message = decoder.decode(buffer);

        // 验证解码结果
        assertNotNull(message, "解码后的消息不应为null");
        assertNotNull(message.getHeader(), "消息头不应为null");

        // 验证消息ID (0x0200 = 位置信息汇报)
        assertEquals(0x0200, message.getMessageId(), "消息ID应为0x0200");

        // 验证终端手机号
        assertEquals("12345678901", message.getHeader().getPhoneNumber(), "终端手机号应为123456789017");

        System.out.println("消息解码成功:");
        System.out.println("消息ID: 0x" + Integer.toHexString(message.getMessageId()).toUpperCase());
        System.out.println("终端手机号: " + message.getHeader().getPhoneNumber());
        System.out.println("消息体长度: " + message.getHeader().getBodyLength());
    }
}