package com.jt808.protocol.message;

import com.jt808.protocol.factory.JT808MessageFactory;
import io.vertx.core.buffer.Buffer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * T8106查询指定终端参数消息测试
 */
class T8106QuerySpecificTerminalParametersTest {

    private T8106QuerySpecificTerminalParameters message;
    private JT808MessageFactory factory;

    @BeforeEach
    void setUp() {
        message = new T8106QuerySpecificTerminalParameters();
        factory = JT808MessageFactory.getInstance();
    }

    @Test
    void testMessageId() {
        assertEquals(0x8106, message.getMessageId());
    }

    @Test
    void testDefaultConstructor() {
        T8106QuerySpecificTerminalParameters msg = new T8106QuerySpecificTerminalParameters();
        assertNotNull(msg.getParameterIds());
        assertTrue(msg.getParameterIds().isEmpty());
        assertEquals(0, msg.getParameterCount());
    }

    @Test
    void testConstructorWithHeader() {
        JT808Header header = new JT808Header();
        header.setMessageId(0x8106);

        T8106QuerySpecificTerminalParameters msg = new T8106QuerySpecificTerminalParameters(header);
        assertNotNull(msg.getParameterIds());
        assertTrue(msg.getParameterIds().isEmpty());
        assertEquals(header, msg.getHeader());
    }

    @Test
    void testConstructorWithParameterIds() {
        List<Integer> parameterIds = Arrays.asList(0x0001, 0x0013, 0x0020);
        T8106QuerySpecificTerminalParameters msg = new T8106QuerySpecificTerminalParameters(parameterIds);

        assertEquals(3, msg.getParameterCount());
        assertEquals(parameterIds, msg.getParameterIds());
    }

    @Test
    void testSetAndGetParameterIds() {
        List<Integer> parameterIds = Arrays.asList(0x0001, 0x0013, 0x0020);
        message.setParameterIds(parameterIds);

        assertEquals(3, message.getParameterCount());
        assertEquals(parameterIds, message.getParameterIds());

        // 测试返回的是副本
        List<Integer> returnedIds = message.getParameterIds();
        returnedIds.add(0x0030);
        assertEquals(3, message.getParameterCount()); // 原始数据不应该被修改
    }

    @Test
    void testAddParameterId() {
        message.addParameterId(0x0001);
        message.addParameterId(0x0013);

        assertEquals(2, message.getParameterCount());
        assertTrue(message.getParameterIds().contains(0x0001));
        assertTrue(message.getParameterIds().contains(0x0013));
    }

    @Test
    void testEncodeBodyEmpty() {
        Buffer encoded = message.encodeBody();

        assertNotNull(encoded);
        assertEquals(1, encoded.length()); // 只有参数总数字段
        assertEquals(0, encoded.getByte(0)); // 参数总数为0
    }

    @Test
    void testEncodeBodyWithParameters() {
        message.addParameterId(0x0001);
        message.addParameterId(0x0013);
        message.addParameterId(0x0020);

        Buffer encoded = message.encodeBody();

        assertNotNull(encoded);
        assertEquals(13, encoded.length()); // 1字节参数总数 + 3*4字节参数ID

        // 验证参数总数
        assertEquals(3, encoded.getUnsignedByte(0));

        // 验证参数ID
        assertEquals(0x0001, encoded.getInt(1));
        assertEquals(0x0013, encoded.getInt(5));
        assertEquals(0x0020, encoded.getInt(9));
    }

    @Test
    void testDecodeBodyEmpty() {
        Buffer body = Buffer.buffer();
        body.appendByte((byte) 0); // 参数总数为0

        message.decodeBody(body);

        assertEquals(0, message.getParameterCount());
        assertTrue(message.getParameterIds().isEmpty());
    }

    @Test
    void testDecodeBodyWithParameters() {
        Buffer body = Buffer.buffer();
        body.appendByte((byte) 3); // 参数总数为3
        body.appendInt(0x0001);
        body.appendInt(0x0013);
        body.appendInt(0x0020);

        message.decodeBody(body);

        assertEquals(3, message.getParameterCount());
        List<Integer> parameterIds = message.getParameterIds();
        assertEquals(Arrays.asList(0x0001, 0x0013, 0x0020), parameterIds);
    }

    @Test
    void testDecodeBodyNullBuffer() {
        message.decodeBody(null);
        assertEquals(0, message.getParameterCount());
    }

    @Test
    void testDecodeBodyInsufficientLength() {
        Buffer body = Buffer.buffer();
        body.appendByte((byte) 2); // 声明有2个参数
        body.appendInt(0x0001); // 但只提供1个参数

        assertThrows(IllegalArgumentException.class, () -> {
            message.decodeBody(body);
        });
    }

    @Test
    void testFactoryCreation() {
        JT808Message created = factory.createMessage(0x8106);

        assertNotNull(created);
        assertInstanceOf(T8106QuerySpecificTerminalParameters.class, created);
        assertEquals(0x8106, created.getMessageId());
    }

    @Test
    void testCompleteMessageEncodingDecoding() {
        // 创建原始消息
        T8106QuerySpecificTerminalParameters originalMessage = new T8106QuerySpecificTerminalParameters();
        originalMessage.addParameterId(0x0001);
        originalMessage.addParameterId(0x0013);

        // 设置消息头
        JT808Header header = new JT808Header();
        header.setMessageId(0x8106);
        header.setPhoneNumber("13800138000");
        header.setSerialNumber(1001);
        header.setProtocolVersion((byte) 0x01);

        originalMessage.setHeader(header);

        // 编码消息体
        Buffer encodedBody = originalMessage.encodeBody();

        // 解码消息体
        T8106QuerySpecificTerminalParameters decodedMessage = new T8106QuerySpecificTerminalParameters();
        decodedMessage.decodeBody(encodedBody);

        // 验证解码结果
        assertEquals(originalMessage.getParameterCount(), decodedMessage.getParameterCount());
        assertEquals(originalMessage.getParameterIds(), decodedMessage.getParameterIds());
    }

    @Test
    void testToString() {
        message.addParameterId(0x0001);
        message.addParameterId(0x0013);

        String str = message.toString();

        assertNotNull(str);
        assertTrue(str.contains("T8106QuerySpecificTerminalParameters"));
        assertTrue(str.contains("parameterCount=2"));
        assertTrue(str.contains("parameterIds=[1, 19]"));
    }

    @Test
    void testMessageBodyLength() {
        // 测试不同参数数量的消息体长度
        assertEquals(1, message.encodeBody().length()); // 0个参数

        message.addParameterId(0x0001);
        assertEquals(5, message.encodeBody().length()); // 1个参数

        message.addParameterId(0x0013);
        assertEquals(9, message.encodeBody().length()); // 2个参数

        message.addParameterId(0x0020);
        assertEquals(13, message.encodeBody().length()); // 3个参数
    }

    @Test
    void testLargeParameterList() {
        // 测试大量参数的情况
        for (int i = 1; i <= 100; i++) {
            message.addParameterId(i);
        }

        assertEquals(100, message.getParameterCount());

        Buffer encoded = message.encodeBody();
        assertEquals(401, encoded.length()); // 1 + 100*4

        T8106QuerySpecificTerminalParameters decoded = new T8106QuerySpecificTerminalParameters();
        decoded.decodeBody(encoded);

        assertEquals(100, decoded.getParameterCount());
        for (int i = 1; i <= 100; i++) {
            assertTrue(decoded.getParameterIds().contains(i));
        }
    }

    @Test
    void testMultipleInstances() {
        T8106QuerySpecificTerminalParameters msg1 = new T8106QuerySpecificTerminalParameters();
        T8106QuerySpecificTerminalParameters msg2 = new T8106QuerySpecificTerminalParameters();

        msg1.addParameterId(0x0001);
        msg2.addParameterId(0x0013);

        assertEquals(1, msg1.getParameterCount());
        assertEquals(1, msg2.getParameterCount());
        assertNotEquals(msg1.getParameterIds(), msg2.getParameterIds());
    }
}