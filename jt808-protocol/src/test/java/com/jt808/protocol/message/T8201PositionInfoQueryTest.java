package com.jt808.protocol.message;

import com.jt808.protocol.factory.JT808MessageFactory;
import io.vertx.core.buffer.Buffer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("T8201位置信息查询消息测试")
class T8201PositionInfoQueryTest {
    
    private T8201PositionInfoQuery message;
    private JT808MessageFactory factory;
    
    @BeforeEach
    void setUp() {
        message = new T8201PositionInfoQuery();
        factory = JT808MessageFactory.getInstance();
    }
    
    @Test
    @DisplayName("测试消息ID")
    void testMessageId() {
        assertEquals(0x8201, message.getMessageId());
    }
    
    @Test
    @DisplayName("测试默认构造函数")
    void testDefaultConstructor() {
        assertNotNull(message);
        assertEquals(0x8201, message.getMessageId());
    }
    
    @Test
    @DisplayName("测试消息描述")
    void testMessageDescription() {
        assertEquals("位置信息查询", message.getMessageDescription());
    }
    
    @Test
    @DisplayName("测试静态工厂方法")
    void testStaticFactoryMethod() {
        T8201PositionInfoQuery createdMessage = T8201PositionInfoQuery.create();
        assertNotNull(createdMessage);
        assertEquals(0x8201, createdMessage.getMessageId());
        assertEquals("位置信息查询", createdMessage.getMessageDescription());
    }
    
    @Test
    @DisplayName("测试消息编码")
    void testEncodeBody() {
        Buffer encoded = message.encodeBody();
        
        // 验证编码结果
        assertNotNull(encoded);
        assertEquals(0, encoded.length()); // 消息体为空，长度应为0
    }
    
    @Test
    @DisplayName("测试消息解码 - 空消息体")
    void testDecodeBodyWithEmptyBuffer() {
        // 测试空Buffer
        Buffer emptyBuffer = Buffer.buffer();
        assertDoesNotThrow(() -> message.decodeBody(emptyBuffer));
        
        // 测试null Buffer
        assertDoesNotThrow(() -> message.decodeBody(null));
    }
    
    @Test
    @DisplayName("测试消息解码 - 非空消息体应抛出异常")
    void testDecodeBodyWithNonEmptyBuffer() {
        // 构造非空Buffer
        Buffer nonEmptyBuffer = Buffer.buffer();
        nonEmptyBuffer.appendByte((byte) 0x01);
        
        // 验证抛出异常
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> message.decodeBody(nonEmptyBuffer)
        );
        
        assertTrue(exception.getMessage().contains("位置信息查询消息体应为空"));
        assertTrue(exception.getMessage().contains("1 字节数据"));
    }
    
    @Test
    @DisplayName("测试编解码往返一致性")
    void testEncodeDecodeRoundTrip() {
        // 编码
        Buffer encoded = message.encodeBody();
        
        // 解码
        T8201PositionInfoQuery decoded = new T8201PositionInfoQuery();
        assertDoesNotThrow(() -> decoded.decodeBody(encoded));
        
        // 验证一致性（消息体为空，主要验证不抛出异常）
        assertEquals(message.getMessageId(), decoded.getMessageId());
        assertEquals(message.getMessageDescription(), decoded.getMessageDescription());
    }
    
    @Test
    @DisplayName("测试工厂创建")
    void testFactoryCreation() {
        JT808Message factoryMessage = factory.createMessage(0x8201);
        assertInstanceOf(T8201PositionInfoQuery.class, factoryMessage);
        assertEquals(0x8201, factoryMessage.getMessageId());
    }
    
    @Test
    @DisplayName("测试toString方法")
    void testToString() {
        String result = message.toString();
        assertNotNull(result);
        assertTrue(result.contains("T8201PositionInfoQuery"));
        assertTrue(result.contains("messageId=0x8201"));
        assertTrue(result.contains("位置信息查询"));
        assertTrue(result.contains("bodyLength=0"));
    }
    
    @Test
    @DisplayName("测试equals方法")
    void testEquals() {
        T8201PositionInfoQuery message1 = new T8201PositionInfoQuery();
        T8201PositionInfoQuery message2 = new T8201PositionInfoQuery();
        T8201PositionInfoQuery message3 = T8201PositionInfoQuery.create();
        
        // 测试相等性
        assertEquals(message1, message2);
        assertEquals(message1, message3);
        assertEquals(message2, message3);
        
        // 测试自反性
        assertEquals(message1, message1);
        
        // 测试与null的比较
        assertNotEquals(message1, null);
        
        // 测试与不同类型对象的比较
        assertNotEquals(message1, "string");
    }
    
    @Test
    @DisplayName("测试hashCode方法")
    void testHashCode() {
        T8201PositionInfoQuery message1 = new T8201PositionInfoQuery();
        T8201PositionInfoQuery message2 = new T8201PositionInfoQuery();
        
        // 相等的对象应该有相同的hashCode
        assertEquals(message1.hashCode(), message2.hashCode());
        
        // hashCode应该等于消息ID
        assertEquals(0x8201, message1.hashCode());
    }
    
    @Test
    @DisplayName("测试多次编码的一致性")
    void testMultipleEncodeConsistency() {
        Buffer encoded1 = message.encodeBody();
        Buffer encoded2 = message.encodeBody();
        
        assertEquals(encoded1.length(), encoded2.length());
        assertEquals(0, encoded1.length());
        assertEquals(0, encoded2.length());
    }
    
    @Test
    @DisplayName("测试解码不同长度的非空消息体")
    void testDecodeBodyWithDifferentNonEmptyLengths() {
        // 测试1字节数据
        Buffer buffer1 = Buffer.buffer().appendByte((byte) 0x01);
        IllegalArgumentException exception1 = assertThrows(
            IllegalArgumentException.class,
            () -> message.decodeBody(buffer1)
        );
        assertTrue(exception1.getMessage().contains("1 字节数据"));
        
        // 测试多字节数据
        Buffer buffer5 = Buffer.buffer().appendBytes(new byte[]{0x01, 0x02, 0x03, 0x04, 0x05});
        IllegalArgumentException exception5 = assertThrows(
            IllegalArgumentException.class,
            () -> message.decodeBody(buffer5)
        );
        assertTrue(exception5.getMessage().contains("5 字节数据"));
    }
    
    @Test
    @DisplayName("测试消息的不可变性")
    void testMessageImmutability() {
        // 由于消息体为空，主要测试消息ID和描述的不可变性
        int originalMessageId = message.getMessageId();
        String originalDescription = message.getMessageDescription();
        
        // 多次调用应返回相同的值
        assertEquals(originalMessageId, message.getMessageId());
        assertEquals(originalDescription, message.getMessageDescription());
        
        // 编码操作不应影响消息状态
        message.encodeBody();
        assertEquals(originalMessageId, message.getMessageId());
        assertEquals(originalDescription, message.getMessageDescription());
        
        // 解码空消息体不应影响消息状态
        message.decodeBody(Buffer.buffer());
        assertEquals(originalMessageId, message.getMessageId());
        assertEquals(originalDescription, message.getMessageDescription());
    }
}