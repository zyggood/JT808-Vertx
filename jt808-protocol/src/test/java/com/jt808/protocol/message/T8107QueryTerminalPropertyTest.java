package com.jt808.protocol.message;

import com.jt808.protocol.factory.JT808MessageFactory;
import io.vertx.core.buffer.Buffer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * T8107查询终端属性消息测试类
 */
@DisplayName("T8107查询终端属性消息测试")
public class T8107QueryTerminalPropertyTest {

    private T8107QueryTerminalProperty message;

    @BeforeEach
    void setUp() {
        message = new T8107QueryTerminalProperty();
    }

    @Test
    @DisplayName("测试消息ID")
    void testMessageId() {
        assertEquals(0x8107, message.getMessageId());
    }

    @Test
    @DisplayName("测试默认构造函数")
    void testDefaultConstructor() {
        T8107QueryTerminalProperty msg = new T8107QueryTerminalProperty();
        assertNotNull(msg);
        assertEquals(0x8107, msg.getMessageId());
        assertNull(msg.getHeader());
    }

    @Test
    @DisplayName("测试带消息头的构造函数")
    void testConstructorWithHeader() {
        JT808Header header = new JT808Header();
        header.setMessageId(0x8107);
        header.setPhoneNumber("13800138000");
        header.setSerialNumber(12345);

        T8107QueryTerminalProperty msg = new T8107QueryTerminalProperty(header);
        assertNotNull(msg);
        assertEquals(0x8107, msg.getMessageId());
        assertNotNull(msg.getHeader());
        assertEquals(header, msg.getHeader());
    }

    @Test
    @DisplayName("测试工厂创建消息")
    void testFactoryCreateMessage() {
        JT808MessageFactory factory = JT808MessageFactory.getInstance();
        JT808Message msg = factory.createMessage(0x8107);

        assertNotNull(msg);
        assertInstanceOf(T8107QueryTerminalProperty.class, msg);
        assertEquals(0x8107, msg.getMessageId());
    }

    @Test
    @DisplayName("测试消息体编码")
    void testEncodeBody() {
        Buffer encoded = message.encodeBody();

        assertNotNull(encoded);
        assertEquals(0, encoded.length()); // 消息体为空
    }

    @Test
    @DisplayName("测试消息体解码")
    void testDecodeBody() {
        // 测试空消息体解码
        Buffer emptyBody = Buffer.buffer();
        assertDoesNotThrow(() -> message.decodeBody(emptyBody));

        // 测试非空消息体解码（虽然协议规定为空，但应该能处理）
        Buffer nonEmptyBody = Buffer.buffer().appendBytes(new byte[]{0x01, 0x02, 0x03});
        assertDoesNotThrow(() -> message.decodeBody(nonEmptyBody));
    }

    @Test
    @DisplayName("测试编解码一致性")
    void testEncodeDecodeConsistency() {
        // 编码
        Buffer encoded = message.encodeBody();

        // 解码
        T8107QueryTerminalProperty decoded = new T8107QueryTerminalProperty();
        decoded.decodeBody(encoded);

        // 验证一致性
        assertEquals(message.getMessageId(), decoded.getMessageId());

        // 重新编码验证
        Buffer reEncoded = decoded.encodeBody();
        assertEquals(encoded.length(), reEncoded.length());
        assertArrayEquals(encoded.getBytes(), reEncoded.getBytes());
    }

    @Test
    @DisplayName("测试toString方法")
    void testToString() {
        String result = message.toString();

        assertNotNull(result);
        assertTrue(result.contains("T8107QueryTerminalProperty"));
        assertTrue(result.contains("header"));

        // 测试带消息头的toString
        JT808Header header = new JT808Header();
        header.setMessageId(0x8107);
        header.setPhoneNumber("13800138000");
        header.setSerialNumber(12345);

        T8107QueryTerminalProperty msgWithHeader = new T8107QueryTerminalProperty(header);
        String resultWithHeader = msgWithHeader.toString();

        assertNotNull(resultWithHeader);
        assertTrue(resultWithHeader.contains("T8107QueryTerminalProperty"));
        assertTrue(resultWithHeader.contains("header"));
    }

    @Test
    @DisplayName("测试消息工厂支持检查")
    void testFactorySupport() {
        JT808MessageFactory factory = JT808MessageFactory.getInstance();

        assertTrue(factory.isSupported(0x8107));
        assertTrue(factory.getSupportedMessageIds().contains(0x8107));
    }

    @Test
    @DisplayName("测试完整的消息处理流程")
    void testCompleteMessageFlow() {
        // 1. 创建消息头
        JT808Header header = new JT808Header();
        header.setMessageId(0x8107);
        header.setPhoneNumber("13800138000");
        header.setSerialNumber(12345);

        // 2. 创建消息
        T8107QueryTerminalProperty originalMessage = new T8107QueryTerminalProperty(header);

        // 3. 使用工厂编码
        JT808MessageFactory factory = JT808MessageFactory.getInstance();
        Buffer fullEncoded = factory.encodeMessage(originalMessage);

        assertNotNull(fullEncoded);
        assertTrue(fullEncoded.length() > 0); // 包含消息头

        // 4. 使用工厂解码
        try {
            JT808Message parsedMessage = factory.parseMessage(fullEncoded);

            assertNotNull(parsedMessage);
            assertInstanceOf(T8107QueryTerminalProperty.class, parsedMessage);
            assertEquals(0x8107, parsedMessage.getMessageId());

            T8107QueryTerminalProperty queryMessage = (T8107QueryTerminalProperty) parsedMessage;
            assertNotNull(queryMessage.getHeader());
            assertEquals(0x8107, queryMessage.getHeader().getMessageId());

        } catch (Exception e) {
            fail("消息解析失败: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("测试消息体为空的特性")
    void testEmptyBodyCharacteristic() {
        // 验证消息体确实为空
        Buffer body = message.encodeBody();
        assertEquals(0, body.length(), "查询终端属性消息的消息体应该为空");

        // 验证解码空消息体不会出错
        T8107QueryTerminalProperty newMessage = new T8107QueryTerminalProperty();
        assertDoesNotThrow(() -> newMessage.decodeBody(Buffer.buffer()));

        // 验证解码后编码结果一致
        Buffer reEncoded = newMessage.encodeBody();
        assertEquals(0, reEncoded.length());
    }

    @Test
    @DisplayName("测试消息类型常量")
    void testMessageTypeConstant() {
        // 验证消息ID常量值
        assertEquals(0x8107, message.getMessageId());
        assertEquals(33031, message.getMessageId()); // 0x8107的十进制值
    }
}