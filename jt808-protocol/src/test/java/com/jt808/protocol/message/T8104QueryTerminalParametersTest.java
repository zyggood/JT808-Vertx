package com.jt808.protocol.message;

import com.jt808.protocol.factory.JT808MessageFactory;
import io.vertx.core.buffer.Buffer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * T8104查询终端参数消息测试
 */
class T8104QueryTerminalParametersTest {

    @Test
    void testMessageId() {
        T8104QueryTerminalParameters message = new T8104QueryTerminalParameters();
        assertEquals(0x8104, message.getMessageId());
    }

    @Test
    void testConstructors() {
        // 测试默认构造函数
        T8104QueryTerminalParameters message1 = new T8104QueryTerminalParameters();
        assertNotNull(message1);
        assertEquals(0x8104, message1.getMessageId());

        // 测试带头部的构造函数
        JT808Header header = new JT808Header();
        header.setMessageId(0x8104);
        header.setPhoneNumber("13800138000");
        header.setSerialNumber(1);

        T8104QueryTerminalParameters message2 = new T8104QueryTerminalParameters(header);
        assertNotNull(message2);
        assertEquals(0x8104, message2.getMessageId());
        assertEquals(header, message2.getHeader());
    }

    @Test
    void testEncodingDecoding() {
        T8104QueryTerminalParameters message = new T8104QueryTerminalParameters();

        // 测试编码 - 消息体应该为空
        Buffer encoded = message.encodeBody();
        assertNotNull(encoded);
        assertEquals(0, encoded.length());

        // 测试解码 - 空消息体
        Buffer emptyBuffer = Buffer.buffer();
        message.decodeBody(emptyBuffer);
        // 解码空消息体不应该抛出异常
    }

    @Test
    void testFactoryCreation() {
        JT808MessageFactory factory = JT808MessageFactory.getInstance();

        // 测试工厂是否支持0x8104消息
        assertTrue(factory.isSupported(0x8104));

        // 测试工厂创建消息
        JT808Message message = factory.createMessage(0x8104);
        assertNotNull(message);
        assertInstanceOf(T8104QueryTerminalParameters.class, message);
        assertEquals(0x8104, message.getMessageId());
    }

    @Test
    void testCompleteMessageEncodingDecoding() {
        // 创建完整消息
        T8104QueryTerminalParameters originalMessage = new T8104QueryTerminalParameters();

        // 设置消息头
        JT808Header header = new JT808Header();
        header.setMessageId(0x8104);
        header.setPhoneNumber("13800138000");
        header.setSerialNumber(123);
        originalMessage.setHeader(header);

        // 使用工厂编码消息
        JT808MessageFactory factory = JT808MessageFactory.getInstance();
        Buffer encodedData = factory.encodeMessage(originalMessage);
        assertNotNull(encodedData);
        assertTrue(encodedData.length() > 0); // 应该包含消息头

        // 解码消息
        try {
            JT808Message decodedMessage = factory.parseMessage(encodedData);
            assertNotNull(decodedMessage);
            assertInstanceOf(T8104QueryTerminalParameters.class, decodedMessage);
            assertEquals(0x8104, decodedMessage.getMessageId());

            // 验证消息头信息
            JT808Header decodedHeader = decodedMessage.getHeader();
            assertNotNull(decodedHeader);
            assertEquals(0x8104, decodedHeader.getMessageId());
            assertEquals("13800138000", decodedHeader.getPhoneNumber());
            assertEquals(123, decodedHeader.getSerialNumber());
        } catch (Exception e) {
            fail("解码消息时发生异常: " + e.getMessage());
        }
    }

    @Test
    void testToString() {
        T8104QueryTerminalParameters message = new T8104QueryTerminalParameters();

        // 测试toString方法
        String result = message.toString();
        assertNotNull(result);
        assertTrue(result.contains("T8104QueryTerminalParameters"));
        assertTrue(result.contains("header"));
    }

    @Test
    void testMessageBodyLength() {
        T8104QueryTerminalParameters message = new T8104QueryTerminalParameters();

        // 消息体长度应该为0
        Buffer body = message.encodeBody();
        assertEquals(0, body.length());
    }

    @Test
    void testDecodeNonEmptyBody() {
        T8104QueryTerminalParameters message = new T8104QueryTerminalParameters();

        // 测试解码非空消息体（虽然协议规定为空，但要确保不会出错）
        Buffer nonEmptyBuffer = Buffer.buffer().appendBytes(new byte[]{0x01, 0x02, 0x03});

        // 解码非空消息体不应该抛出异常
        assertDoesNotThrow(() -> message.decodeBody(nonEmptyBuffer));
    }

    @Test
    void testMultipleInstances() {
        // 测试创建多个实例
        T8104QueryTerminalParameters message1 = new T8104QueryTerminalParameters();
        T8104QueryTerminalParameters message2 = new T8104QueryTerminalParameters();

        assertNotSame(message1, message2);
        assertEquals(message1.getMessageId(), message2.getMessageId());

        // 两个实例的编码结果应该相同
        Buffer encoded1 = message1.encodeBody();
        Buffer encoded2 = message2.encodeBody();
        assertEquals(encoded1.length(), encoded2.length());
        assertEquals(0, encoded1.length());
        assertEquals(0, encoded2.length());
    }
}