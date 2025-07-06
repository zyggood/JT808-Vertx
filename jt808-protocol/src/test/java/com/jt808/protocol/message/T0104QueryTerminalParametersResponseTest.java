package com.jt808.protocol.message;

import com.jt808.protocol.factory.JT808MessageFactory;
import io.vertx.core.buffer.Buffer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import static org.junit.jupiter.api.Assertions.*;

/**
 * T0104QueryTerminalParametersResponse 单元测试
 */
class T0104QueryTerminalParametersResponseTest {

    private T0104QueryTerminalParametersResponse message;
    private JT808MessageFactory factory;

    @BeforeEach
    void setUp() {
        message = new T0104QueryTerminalParametersResponse();
        factory = JT808MessageFactory.getInstance();
    }

    @Test
    void testMessageId() {
        assertEquals(0x0104, message.getMessageId());
    }

    @Test
    void testDefaultConstructor() {
        T0104QueryTerminalParametersResponse msg = new T0104QueryTerminalParametersResponse();
        assertEquals(0, msg.getResponseSerialNumber());
        assertEquals(0, msg.getParameterCount());
        assertNotNull(msg.getParameterItems());
        assertTrue(msg.getParameterItems().isEmpty());
    }

    @Test
    void testConstructorWithSerialNumber() {
        T0104QueryTerminalParametersResponse msg = new T0104QueryTerminalParametersResponse(12345);
        assertEquals(12345, msg.getResponseSerialNumber());
        assertEquals(0, msg.getParameterCount());
        assertNotNull(msg.getParameterItems());
        assertTrue(msg.getParameterItems().isEmpty());
    }

    @Test
    void testResponseSerialNumber() {
        message.setResponseSerialNumber(54321);
        assertEquals(54321, message.getResponseSerialNumber());
    }

    @Test
    void testAddDwordParameter() {
        message.addDwordParameter(0x0001, 0x12345678L);
        assertEquals(1, message.getParameterCount());
        assertEquals(Long.valueOf(0x12345678L), message.getDwordParameter(0x0001));
    }

    @Test
    void testAddWordParameter() {
        message.addWordParameter(0x0002, 0x1234);
        assertEquals(1, message.getParameterCount());
        assertEquals(Integer.valueOf(0x1234), message.getWordParameter(0x0002));
    }

    @Test
    void testAddByteParameter() {
        message.addByteParameter(0x0003, (byte) 0x56);
        assertEquals(1, message.getParameterCount());
        assertEquals(Byte.valueOf((byte) 0x56), message.getByteParameter(0x0003));
    }

    @Test
    void testAddStringParameter() {
        message.addStringParameter(0x0004, "test string");
        assertEquals(1, message.getParameterCount());
        assertEquals("test string", message.getStringParameter(0x0004));
    }

    @Test
    void testAddBytesParameter() {
        byte[] testBytes = {0x01, 0x02, 0x03, 0x04};
        message.addBytesParameter(0x0005, testBytes);
        assertEquals(1, message.getParameterCount());
        
        ParameterItem item = message.getParameterItem(0x0005);
        assertNotNull(item);
        assertArrayEquals(testBytes, item.getValueBytes());
    }

    @Test
    void testAddParameterItem() {
        ParameterItem item = 
            ParameterItem.createDwordParameter(0x0006, 0x87654321L);
        message.addParameterItem(item);
        assertEquals(1, message.getParameterCount());
        assertEquals(Long.valueOf(0x87654321L), message.getDwordParameter(0x0006));
    }

    @Test
    void testAddNullParameterItem() {
        message.addParameterItem(null);
        assertEquals(0, message.getParameterCount());
    }

    @Test
    void testRemoveParameter() {
        message.addDwordParameter(0x0001, 0x12345678L);
        message.addWordParameter(0x0002, 0x1234);
        assertEquals(2, message.getParameterCount());
        
        assertTrue(message.removeParameter(0x0001));
        assertEquals(1, message.getParameterCount());
        assertNull(message.getDwordParameter(0x0001));
        assertNotNull(message.getWordParameter(0x0002));
        
        assertFalse(message.removeParameter(0x0001)); // 已删除，应返回false
    }

    @Test
    void testClearParameters() {
        message.addDwordParameter(0x0001, 0x12345678L);
        message.addWordParameter(0x0002, 0x1234);
        assertEquals(2, message.getParameterCount());
        
        message.clearParameters();
        assertEquals(0, message.getParameterCount());
        assertTrue(message.getParameterItems().isEmpty());
    }

    @Test
    void testGetParameterItem() {
        message.addDwordParameter(0x0001, 0x12345678L);
        
        ParameterItem item = message.getParameterItem(0x0001);
        assertNotNull(item);
        assertEquals(0x0001, item.getParameterId());
        assertEquals(Long.valueOf(0x12345678L), item.getDwordValue());
        
        assertNull(message.getParameterItem(0x9999)); // 不存在的参数
    }

    @Test
    void testEncodeDecodeEmptyBody() {
        message.setResponseSerialNumber(12345);
        
        Buffer encoded = message.encodeBody();
        assertNotNull(encoded);
        assertEquals(3, encoded.length()); // 2字节流水号 + 1字节参数个数
        
        T0104QueryTerminalParametersResponse decoded = new T0104QueryTerminalParametersResponse();
        decoded.decodeBody(encoded);
        
        assertEquals(12345, decoded.getResponseSerialNumber());
        assertEquals(0, decoded.getParameterCount());
    }

    @Test
    void testEncodeDecodeWithParameters() {
        message.setResponseSerialNumber(54321);
        message.addDwordParameter(0x0001, 0x12345678L);
        message.addWordParameter(0x0002, 0x1234);
        message.addByteParameter(0x0003, (byte) 0x56);
        message.addStringParameter(0x0004, "test");
        
        Buffer encoded = message.encodeBody();
        assertNotNull(encoded);
        
        T0104QueryTerminalParametersResponse decoded = new T0104QueryTerminalParametersResponse();
        decoded.decodeBody(encoded);
        
        assertEquals(54321, decoded.getResponseSerialNumber());
        assertEquals(4, decoded.getParameterCount());
        assertEquals(Long.valueOf(0x12345678L), decoded.getDwordParameter(0x0001));
        assertEquals(Integer.valueOf(0x1234), decoded.getWordParameter(0x0002));
        assertEquals(Byte.valueOf((byte) 0x56), decoded.getByteParameter(0x0003));
        assertEquals("test", decoded.getStringParameter(0x0004));
    }

    @Test
    void testDecodeNullBody() {
        message.decodeBody(null);
        assertEquals(0, message.getResponseSerialNumber());
        assertEquals(0, message.getParameterCount());
    }

    @Test
    void testDecodeShortBody() {
        Buffer shortBuffer = Buffer.buffer().appendByte((byte) 0x01); // 只有1字节
        message.decodeBody(shortBuffer);
        assertEquals(0, message.getResponseSerialNumber());
        assertEquals(0, message.getParameterCount());
    }

    @Test
    void testDecodeIncompleteParameters() {
        Buffer buffer = Buffer.buffer();
        buffer.appendUnsignedShort(12345); // 应答流水号
        buffer.appendByte((byte) 2); // 声明有2个参数
        buffer.appendUnsignedInt(0x0001); // 第一个参数ID
        buffer.appendByte((byte) 4); // 参数长度
        buffer.appendUnsignedInt(0x12345678); // 参数值
        // 缺少第二个参数
        
        message.decodeBody(buffer);
        assertEquals(12345, message.getResponseSerialNumber());
        assertEquals(1, message.getParameterCount()); // 只解析了一个完整的参数
        assertEquals(Long.valueOf(0x12345678L), message.getDwordParameter(0x0001));
    }

    @Test
    void testFactoryCreation() {
        JT808Message created = factory.createMessage(0x0104);
        assertNotNull(created);
        assertInstanceOf(T0104QueryTerminalParametersResponse.class, created);
        assertEquals(0x0104, created.getMessageId());
    }

    @Test
    void testSetParameterItems() {
        java.util.List<ParameterItem> items = new java.util.ArrayList<>();
        items.add(ParameterItem.createDwordParameter(0x0001, 0x12345678L));
        items.add(ParameterItem.createWordParameter(0x0002, 0x1234));
        
        message.setParameterItems(items);
        assertEquals(2, message.getParameterCount());
        assertEquals(Long.valueOf(0x12345678L), message.getDwordParameter(0x0001));
        assertEquals(Integer.valueOf(0x1234), message.getWordParameter(0x0002));
        
        // 测试设置null
        message.setParameterItems(null);
        assertEquals(0, message.getParameterCount());
    }

    @Test
    void testGetParameterItemsReturnsDefensiveCopy() {
        message.addDwordParameter(0x0001, 0x12345678L);
        
        java.util.List<ParameterItem> items = message.getParameterItems();
        assertEquals(1, items.size());
        
        // 修改返回的列表不应影响原始数据
        items.clear();
        assertEquals(1, message.getParameterCount()); // 原始数据应该不变
    }

    @Test
    void testToString() {
        message.setResponseSerialNumber(12345);
        message.addDwordParameter(0x0001, 0x12345678L);
        message.addWordParameter(0x0002, 0x1234);
        
        String str = message.toString();
        assertNotNull(str);
        assertTrue(str.contains("T0104QueryTerminalParametersResponse"));
        assertTrue(str.contains("responseSerialNumber=12345"));
        assertTrue(str.contains("parameterCount=2"));
    }

    @Test
    void testLargeParameterList() {
        message.setResponseSerialNumber(65535); // 使用unsigned short的最大值
        
        // 添加大量参数
        for (int i = 1; i <= 100; i++) {
            message.addDwordParameter(i, i * 1000L);
        }
        
        assertEquals(100, message.getParameterCount());
        
        // 编码解码测试
        Buffer encoded = message.encodeBody();
        T0104QueryTerminalParametersResponse decoded = new T0104QueryTerminalParametersResponse();
        decoded.decodeBody(encoded);
        
        assertEquals(65535, decoded.getResponseSerialNumber());
        assertEquals(100, decoded.getParameterCount());
        
        // 验证部分参数
        assertEquals(Long.valueOf(1000L), decoded.getDwordParameter(1));
        assertEquals(Long.valueOf(50000L), decoded.getDwordParameter(50));
        assertEquals(Long.valueOf(100000L), decoded.getDwordParameter(100));
    }

    @Test
    void testMessageBodyLength() {
        message.setResponseSerialNumber(12345);
        message.addDwordParameter(0x0001, 0x12345678L); // 4字节ID + 1字节长度 + 4字节值 = 9字节
        message.addWordParameter(0x0002, 0x1234); // 4字节ID + 1字节长度 + 2字节值 = 7字节
        
        Buffer encoded = message.encodeBody();
        // 2字节流水号 + 1字节参数个数 + 9字节参数1 + 7字节参数2 = 19字节
        assertEquals(19, encoded.length());
    }
}