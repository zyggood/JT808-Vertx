package com.jt808.protocol.message;

import io.vertx.core.buffer.Buffer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * T8702DriverIdentityInfoRequest 测试类
 */
class T8702DriverIdentityInfoRequestTest {

    private T8702DriverIdentityInfoRequest message;

    @BeforeEach
    void setUp() {
        message = new T8702DriverIdentityInfoRequest();
    }

    @Test
    @DisplayName("测试消息ID")
    void testMessageId() {
        assertEquals(0x8702, message.getMessageId());
    }

    @Test
    @DisplayName("测试默认构造函数")
    void testDefaultConstructor() {
        T8702DriverIdentityInfoRequest newMessage = new T8702DriverIdentityInfoRequest();
        assertNotNull(newMessage);
        assertEquals(0x8702, newMessage.getMessageId());
    }

    @Test
    @DisplayName("测试静态工厂方法")
    void testCreateMethod() {
        T8702DriverIdentityInfoRequest createdMessage = T8702DriverIdentityInfoRequest.create();
        assertNotNull(createdMessage);
        assertEquals(0x8702, createdMessage.getMessageId());
    }

    @Test
    @DisplayName("测试消息体编码")
    void testEncodeBody() {
        Buffer encoded = message.encodeBody();
        assertNotNull(encoded);
        assertEquals(0, encoded.length(), "上报驾驶员身份信息请求消息体应为空");
    }

    @Test
    @DisplayName("测试消息体解码 - 空消息体")
    void testDecodeBodyEmpty() {
        Buffer emptyBuffer = Buffer.buffer();
        assertDoesNotThrow(() -> message.decodeBody(emptyBuffer));
    }

    @Test
    @DisplayName("测试消息体解码 - null消息体")
    void testDecodeBodyNull() {
        assertDoesNotThrow(() -> message.decodeBody(null));
    }

    @Test
    @DisplayName("测试消息体解码 - 非空消息体异常")
    void testDecodeBodyNonEmptyException() {
        Buffer nonEmptyBuffer = Buffer.buffer(new byte[]{0x01, 0x02, 0x03});
        
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> message.decodeBody(nonEmptyBuffer)
        );
        
        assertTrue(exception.getMessage().contains("上报驾驶员身份信息请求消息体应为空"));
        assertTrue(exception.getMessage().contains("3 字节数据"));
    }

    @Test
    @DisplayName("测试消息体解码 - 单字节非空消息体异常")
    void testDecodeBodySingleByteException() {
        Buffer singleByteBuffer = Buffer.buffer(new byte[]{0x01});
        
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> message.decodeBody(singleByteBuffer)
        );
        
        assertTrue(exception.getMessage().contains("上报驾驶员身份信息请求消息体应为空"));
        assertTrue(exception.getMessage().contains("1 字节数据"));
    }

    @Test
    @DisplayName("测试获取消息描述")
    void testGetMessageDescription() {
        String description = message.getMessageDescription();
        assertEquals("上报驾驶员身份信息请求", description);
    }

    @Test
    @DisplayName("测试toString方法")
    void testToString() {
        String str = message.toString();
        assertNotNull(str);
        assertTrue(str.contains("T8702DriverIdentityInfoRequest"));
        assertTrue(str.contains("messageId=0x8702"));
        assertTrue(str.contains("description='上报驾驶员身份信息请求'"));
        assertTrue(str.contains("bodyLength=0"));
    }

    @Test
    @DisplayName("测试equals方法")
    void testEquals() {
        T8702DriverIdentityInfoRequest message1 = new T8702DriverIdentityInfoRequest();
        T8702DriverIdentityInfoRequest message2 = new T8702DriverIdentityInfoRequest();
        T8702DriverIdentityInfoRequest message3 = T8702DriverIdentityInfoRequest.create();
        
        // 测试相等性
        assertEquals(message1, message2);
        assertEquals(message1, message3);
        assertEquals(message, message1);
        
        // 测试自反性
        assertEquals(message, message);
        
        // 测试与null的比较
        assertNotEquals(message, null);
        
        // 测试与不同类型对象的比较
        assertNotEquals(message, "not a message");
        
        // 测试与其他消息类型的比较
        T8201PositionInfoQuery otherMessage = new T8201PositionInfoQuery();
        assertNotEquals(message, otherMessage);
    }

    @Test
    @DisplayName("测试hashCode方法")
    void testHashCode() {
        T8702DriverIdentityInfoRequest message1 = new T8702DriverIdentityInfoRequest();
        T8702DriverIdentityInfoRequest message2 = new T8702DriverIdentityInfoRequest();
        
        // 相等的对象应该有相同的hashCode
        assertEquals(message1.hashCode(), message2.hashCode());
        assertEquals(message.hashCode(), message1.hashCode());
        
        // hashCode应该等于消息ID
        assertEquals(0x8702, message.hashCode());
    }

    @Test
    @DisplayName("测试编码解码一致性")
    void testEncodeDecodeConsistency() {
        // 编码
        Buffer encoded = message.encodeBody();
        
        // 解码
        T8702DriverIdentityInfoRequest decodedMessage = new T8702DriverIdentityInfoRequest();
        assertDoesNotThrow(() -> decodedMessage.decodeBody(encoded));
        
        // 验证解码后的消息与原消息相等
        assertEquals(message, decodedMessage);
        assertEquals(message.getMessageId(), decodedMessage.getMessageId());
        assertEquals(message.getMessageDescription(), decodedMessage.getMessageDescription());
        
        // 验证重新编码的结果一致
        Buffer reEncoded = decodedMessage.encodeBody();
        assertEquals(encoded.length(), reEncoded.length());
        assertEquals(0, reEncoded.length());
    }

    @Test
    @DisplayName("测试消息体为空的特性")
    void testEmptyBodyCharacteristic() {
        // 验证消息体确实为空
        Buffer body = message.encodeBody();
        assertEquals(0, body.length(), "上报驾驶员身份信息请求消息的消息体应该为空");
        
        // 验证解码空消息体不会出错
        T8702DriverIdentityInfoRequest newMessage = new T8702DriverIdentityInfoRequest();
        assertDoesNotThrow(() -> newMessage.decodeBody(Buffer.buffer()));
        
        // 验证解码后编码结果一致
        Buffer reEncoded = newMessage.encodeBody();
        assertEquals(0, reEncoded.length());
    }

    @Test
    @DisplayName("测试多次编码解码")
    void testMultipleEncodeDecodeOperations() {
        for (int i = 0; i < 10; i++) {
            // 编码
            Buffer encoded = message.encodeBody();
            assertEquals(0, encoded.length());
            
            // 解码
            T8702DriverIdentityInfoRequest decodedMessage = new T8702DriverIdentityInfoRequest();
            assertDoesNotThrow(() -> decodedMessage.decodeBody(encoded));
            
            // 验证一致性
            assertEquals(message, decodedMessage);
        }
    }

    @Test
    @DisplayName("测试工厂方法创建的消息")
    void testFactoryCreatedMessage() {
        T8702DriverIdentityInfoRequest factoryMessage = T8702DriverIdentityInfoRequest.create();
        
        // 验证基本属性
        assertEquals(0x8702, factoryMessage.getMessageId());
        assertEquals("上报驾驶员身份信息请求", factoryMessage.getMessageDescription());
        
        // 验证编码
        Buffer encoded = factoryMessage.encodeBody();
        assertEquals(0, encoded.length());
        
        // 验证与默认构造函数创建的消息相等
        assertEquals(message, factoryMessage);
        assertEquals(message.hashCode(), factoryMessage.hashCode());
    }

    @Test
    @DisplayName("测试边界条件")
    void testBoundaryConditions() {
        // 测试最大长度的非空buffer
        byte[] largeData = new byte[1000];
        for (int i = 0; i < largeData.length; i++) {
            largeData[i] = (byte) (i % 256);
        }
        Buffer largeBuffer = Buffer.buffer(largeData);
        
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> message.decodeBody(largeBuffer)
        );
        
        assertTrue(exception.getMessage().contains("1000 字节数据"));
    }

    @Test
    @DisplayName("测试消息ID的十六进制表示")
    void testMessageIdHexRepresentation() {
        String hexId = Integer.toHexString(message.getMessageId()).toUpperCase();
        assertEquals("8702", hexId);
        
        String toString = message.toString();
        assertTrue(toString.contains("0x8702"));
    }

    @Test
    @DisplayName("测试实际使用场景")
    void testRealWorldScenario() {
        // 模拟平台发送请求
        T8702DriverIdentityInfoRequest request = T8702DriverIdentityInfoRequest.create();
        
        // 验证请求消息
        assertNotNull(request);
        assertEquals(0x8702, request.getMessageId());
        assertEquals("上报驾驶员身份信息请求", request.getMessageDescription());
        
        // 模拟编码发送
        Buffer encodedRequest = request.encodeBody();
        assertEquals(0, encodedRequest.length());
        
        // 模拟终端接收解码
        T8702DriverIdentityInfoRequest receivedRequest = new T8702DriverIdentityInfoRequest();
        assertDoesNotThrow(() -> receivedRequest.decodeBody(encodedRequest));
        
        // 验证接收到的请求与原请求一致
        assertEquals(request, receivedRequest);
        assertEquals(request.getMessageId(), receivedRequest.getMessageId());
        assertEquals(request.getMessageDescription(), receivedRequest.getMessageDescription());
    }
}