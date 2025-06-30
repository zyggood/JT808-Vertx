package com.jt808.protocol.factory;

import com.jt808.protocol.message.*;
import com.jt808.protocol.factory.JT808MessageFactory.GenericJT808Message;
import com.jt808.common.exception.ProtocolException;
import io.vertx.core.buffer.Buffer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JT808消息工厂测试类
 */
class JT808MessageFactoryTest {
    
    private JT808MessageFactory factory;
    
    @BeforeEach
    void setUp() {
        factory = JT808MessageFactory.getInstance();
    }
    
    @Test
    @DisplayName("测试单例模式")
    void testSingleton() {
        JT808MessageFactory factory1 = JT808MessageFactory.getInstance();
        JT808MessageFactory factory2 = JT808MessageFactory.getInstance();
        assertSame(factory1, factory2);
    }
    
    @Test
    @DisplayName("测试创建已知消息类型")
    void testCreateKnownMessage() {
        // 测试终端消息
        JT808Message message1 = factory.createMessage(0x0001);
        assertInstanceOf(T0001TerminalCommonResponse.class, message1);
        
        JT808Message message2 = factory.createMessage(0x0002);
        assertInstanceOf(T0002TerminalHeartbeat.class, message2);
        
        JT808Message message3 = factory.createMessage(0x0100);
        assertInstanceOf(T0100TerminalRegister.class, message3);
        
        JT808Message message4 = factory.createMessage(0x0102);
        assertInstanceOf(T0102TerminalAuth.class, message4);
        
        JT808Message message5 = factory.createMessage(0x0200);
        assertInstanceOf(T0200LocationReport.class, message5);
        
        // 测试平台消息
        JT808Message message6 = factory.createMessage(0x8001);
        assertInstanceOf(T8001PlatformCommonResponse.class, message6);
        
        JT808Message message7 = factory.createMessage(0x8100);
        assertInstanceOf(T8100TerminalRegisterResponse.class, message7);
    }
    
    @Test
    @DisplayName("测试创建未知消息类型")
    void testCreateUnknownMessage() {
        JT808Message message = factory.createMessage(0x9999);
        assertInstanceOf(GenericJT808Message.class, message);
        assertEquals(0x9999, message.getMessageId());
    }
    
    @Test
    @DisplayName("测试消息类型支持检查")
    void testIsSupported() {
        assertTrue(factory.isSupported(0x0001));
        assertTrue(factory.isSupported(0x0002));
        assertTrue(factory.isSupported(0x0100));
        assertTrue(factory.isSupported(0x8001));
        assertTrue(factory.isSupported(0x8100));
        
        assertFalse(factory.isSupported(0x9999));
    }
    
    @Test
    @DisplayName("测试获取支持的消息ID")
    void testGetSupportedMessageIds() {
        var supportedIds = factory.getSupportedMessageIds();
        
        assertTrue(supportedIds.contains(0x0001));
        assertTrue(supportedIds.contains(0x0002));
        assertTrue(supportedIds.contains(0x0100));
        assertTrue(supportedIds.contains(0x0102));
        assertTrue(supportedIds.contains(0x0200));
        assertTrue(supportedIds.contains(0x8001));
        assertTrue(supportedIds.contains(0x8100));
        
        assertFalse(supportedIds.contains(0x9999));
    }
    
    @Test
    @DisplayName("测试注册自定义消息类型")
    void testRegisterMessage() {
        // 注册前检查
        assertFalse(factory.isSupported(0x9001));
        
        // 注册自定义消息
        factory.registerMessage(0x9001, () -> new CustomMessage(0x9001));
        
        // 注册后检查
        assertTrue(factory.isSupported(0x9001));
        
        JT808Message message = factory.createMessage(0x9001);
        assertInstanceOf(CustomMessage.class, message);
        assertEquals(0x9001, message.getMessageId());
    }
    
    @Test
    @DisplayName("测试通用消息的功能")
    void testGenericMessage() {
        GenericJT808Message message = new GenericJT808Message(0x9999);
        
        assertEquals(0x9999, message.getMessageId());
        
        // 测试消息体数据
        Buffer testData = Buffer.buffer("test data");
        message.setBodyData(testData);
        assertEquals(testData, message.getBodyData());
        
        // 测试编码
        Buffer encoded = message.encodeBody();
        assertEquals(testData, encoded);
        
        // 测试解码
        Buffer newData = Buffer.buffer("new test data");
        message.decodeBody(newData);
        assertEquals(newData, message.getBodyData());
    }
    
    @Test
    @DisplayName("测试通用消息的空数据处理")
    void testGenericMessageEmptyData() {
        GenericJT808Message message = new GenericJT808Message(0x9999);
        
        // 未设置数据时应返回空Buffer
        Buffer encoded = message.encodeBody();
        assertNotNull(encoded);
        assertEquals(0, encoded.length());
        
        assertNull(message.getBodyData());
    }
    
    /**
     * 自定义消息类，用于测试
     */
    private static class CustomMessage extends JT808Message {
        private final int messageId;
        
        public CustomMessage(int messageId) {
            this.messageId = messageId;
        }
        
        @Override
        public int getMessageId() {
            return messageId;
        }
        
        @Override
        public Buffer encodeBody() {
            return Buffer.buffer();
        }
        
        @Override
        public void decodeBody(Buffer body) {
            // 空实现
        }
    }
}