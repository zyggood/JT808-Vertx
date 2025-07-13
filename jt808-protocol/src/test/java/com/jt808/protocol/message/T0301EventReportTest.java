package com.jt808.protocol.message;

import com.jt808.protocol.factory.JT808MessageFactory;
import io.vertx.core.buffer.Buffer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * T0301事件报告消息测试
 */
class T0301EventReportTest {

    private T0301EventReport message;
    private JT808MessageFactory factory;

    @BeforeEach
    void setUp() {
        message = new T0301EventReport();
        factory = JT808MessageFactory.getInstance();
    }

    @Test
    void testMessageId() {
        assertEquals(0x0301, message.getMessageId());
    }

    @Test
    void testConstructors() {
        // 默认构造函数
        T0301EventReport msg1 = new T0301EventReport();
        assertEquals(0, msg1.getEventId());

        // 带事件ID的构造函数
        T0301EventReport msg2 = new T0301EventReport((byte) 5);
        assertEquals((byte) 5, msg2.getEventId());

        // 带消息头的构造函数
        JT808Header header = new JT808Header();
        T0301EventReport msg3 = new T0301EventReport(header);
        assertEquals(header, msg3.getHeader());

        // 带消息头和事件ID的构造函数
        T0301EventReport msg4 = new T0301EventReport(header, (byte) 10);
        assertEquals(header, msg4.getHeader());
        assertEquals((byte) 10, msg4.getEventId());
    }

    @Test
    void testStaticFactoryMethod() {
        T0301EventReport msg = T0301EventReport.create((byte) 15);
        assertEquals((byte) 15, msg.getEventId());
    }

    @Test
    void testEncodeDecodeBasic() {
        // 测试基本编解码
        message.setEventId((byte) 20);

        Buffer encoded = message.encodeBody();
        assertEquals(1, encoded.length()); // 只有1字节的事件ID
        assertEquals((byte) 20, encoded.getByte(0));

        T0301EventReport decoded = new T0301EventReport();
        decoded.decodeBody(encoded);

        assertEquals((byte) 20, decoded.getEventId());
    }

    @Test
    void testEncodeDecodeConsistency() {
        // 测试编解码一致性
        byte[] testEventIds = {0, 1, 127, -1, -128}; // 测试边界值

        for (byte eventId : testEventIds) {
            message.setEventId(eventId);

            Buffer encoded = message.encodeBody();
            T0301EventReport decoded = new T0301EventReport();
            decoded.decodeBody(encoded);

            assertEquals(eventId, decoded.getEventId(),
                    "事件ID " + (eventId & 0xFF) + " 编解码不一致");
        }
    }

    @Test
    void testGetEventIdUnsigned() {
        // 测试无符号事件ID获取
        message.setEventId((byte) -1); // -1在byte中表示255
        assertEquals(255, message.getEventIdUnsigned());

        message.setEventId((byte) -128); // -128在byte中表示128
        assertEquals(128, message.getEventIdUnsigned());

        message.setEventId((byte) 127);
        assertEquals(127, message.getEventIdUnsigned());

        message.setEventId((byte) 0);
        assertEquals(0, message.getEventIdUnsigned());
    }

    @Test
    void testDecodeBodyExceptions() {
        // 测试解码异常
        assertThrows(IllegalArgumentException.class, () -> {
            message.decodeBody(null);
        });

        assertThrows(IllegalArgumentException.class, () -> {
            message.decodeBody(Buffer.buffer());
        });
    }

    @Test
    void testToString() {
        message.setEventId((byte) 25);
        String str = message.toString();
        assertTrue(str.contains("T0301EventReport"));
        assertTrue(str.contains("eventId=25"));
        assertTrue(str.contains("0x19")); // 25的十六进制表示
    }

    @Test
    void testEqualsAndHashCode() {
        T0301EventReport msg1 = new T0301EventReport((byte) 30);
        T0301EventReport msg2 = new T0301EventReport((byte) 30);
        T0301EventReport msg3 = new T0301EventReport((byte) 31);

        assertEquals(msg1, msg2);
        assertEquals(msg1.hashCode(), msg2.hashCode());
        assertNotEquals(msg1, msg3);
        assertNotEquals(msg1, null);
        assertNotEquals(msg1, "string");
    }

    @Test
    void testGetterSetter() {
        // 测试getter和setter
        assertEquals(0, message.getEventId()); // 默认值

        message.setEventId((byte) 100);
        assertEquals((byte) 100, message.getEventId());
        assertEquals(100, message.getEventIdUnsigned());

        message.setEventId((byte) -50);
        assertEquals((byte) -50, message.getEventId());
        assertEquals(206, message.getEventIdUnsigned()); // -50 & 0xFF = 206
    }

    @Test
    void testCompleteFlow() {
        // 测试完整流程：创建 -> 设置 -> 编码 -> 解码 -> 验证
        T0301EventReport original = T0301EventReport.create((byte) 88);

        // 编码
        Buffer encoded = original.encodeBody();

        // 解码
        T0301EventReport decoded = new T0301EventReport();
        decoded.decodeBody(encoded);

        // 验证
        assertEquals(original, decoded);
        assertEquals(original.getEventId(), decoded.getEventId());
        assertEquals(original.getEventIdUnsigned(), decoded.getEventIdUnsigned());
    }

    @Test
    void testBoundaryValues() {
        // 测试边界值
        byte[] boundaryValues = {
                (byte) 0x00,    // 最小值
                (byte) 0x01,    // 最小正值
                (byte) 0x7F,    // 最大正值
                (byte) 0x80,    // 最小负值
                (byte) 0xFF     // 最大值
        };

        for (byte value : boundaryValues) {
            message.setEventId(value);

            Buffer encoded = message.encodeBody();
            T0301EventReport decoded = new T0301EventReport();
            decoded.decodeBody(encoded);

            assertEquals(value, decoded.getEventId());
            assertEquals(value & 0xFF, decoded.getEventIdUnsigned());
        }
    }

    @Test
    void testMessageFactoryIntegration() {
        // 测试与消息工厂的集成
        // 注册消息类型（如果尚未注册）
        if (!factory.isSupported(0x0301)) {
            factory.registerMessage(0x0301, T0301EventReport::new);
        }

        // 使用工厂创建消息
        T0301EventReport factoryMessage = (T0301EventReport) factory.createMessage(0x0301);
        assertNotNull(factoryMessage);
        assertEquals(0x0301, factoryMessage.getMessageId());

        // 设置事件ID并测试
        factoryMessage.setEventId((byte) 99);
        assertEquals((byte) 99, factoryMessage.getEventId());
    }
}