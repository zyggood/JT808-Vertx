package com.jt808.protocol.message;

import com.jt808.protocol.constants.MessageTypes;
import com.jt808.protocol.factory.JT808MessageFactory;
import io.vertx.core.buffer.Buffer;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

/**
 * T0900数据上行透传测试类
 */
class T0900DataUplinkTransparentTransmissionTest {
    
    @Test
    void testMessageId() {
        T0900DataUplinkTransparentTransmission message = new T0900DataUplinkTransparentTransmission();
        assertEquals(0x0900, message.getMessageId());
    }
    
    @Test
    void testDefaultConstructor() {
        T0900DataUplinkTransparentTransmission message = new T0900DataUplinkTransparentTransmission();
        assertEquals(T0900DataUplinkTransparentTransmission.TransparentMessageType.GNSS_MODULE_DETAILED_POSITIONING_DATA, message.getMessageType());
        assertEquals(0, message.getMessageContentLength());
        assertArrayEquals(new byte[0], message.getMessageContent());
    }
    
    @Test
    void testConstructorWithByteArray() {
        byte[] content = {0x01, 0x02, 0x03, 0x04};
        T0900DataUplinkTransparentTransmission message = new T0900DataUplinkTransparentTransmission(
            T0900DataUplinkTransparentTransmission.TransparentMessageType.ROAD_TRANSPORT_CERTIFICATE_IC_CARD_INFO, content);
        
        assertEquals(T0900DataUplinkTransparentTransmission.TransparentMessageType.ROAD_TRANSPORT_CERTIFICATE_IC_CARD_INFO, message.getMessageType());
        assertEquals(4, message.getMessageContentLength());
        assertArrayEquals(content, message.getMessageContent());
    }
    
    @Test
    void testConstructorWithString() {
        String content = "测试透传数据";
        T0900DataUplinkTransparentTransmission message = new T0900DataUplinkTransparentTransmission(
            T0900DataUplinkTransparentTransmission.TransparentMessageType.SERIAL_PORT_1_TRANSPARENT, content);
        
        assertEquals(T0900DataUplinkTransparentTransmission.TransparentMessageType.SERIAL_PORT_1_TRANSPARENT, message.getMessageType());
        assertEquals(content, message.getMessageContentAsString());
        assertArrayEquals(content.getBytes(StandardCharsets.UTF_8), message.getMessageContent());
    }
    
    @Test
    void testGettersAndSetters() {
        T0900DataUplinkTransparentTransmission message = new T0900DataUplinkTransparentTransmission();
        
        // 测试消息类型
        message.setMessageType(T0900DataUplinkTransparentTransmission.TransparentMessageType.SERIAL_PORT_2_TRANSPARENT);
        assertEquals(T0900DataUplinkTransparentTransmission.TransparentMessageType.SERIAL_PORT_2_TRANSPARENT, message.getMessageType());
        
        // 测试字节数组内容
        byte[] content = {0x10, 0x20, 0x30};
        message.setMessageContent(content);
        assertArrayEquals(content, message.getMessageContent());
        assertEquals(3, message.getMessageContentLength());
        
        // 测试字符串内容
        String stringContent = "Hello World";
        message.setMessageContent(stringContent);
        assertEquals(stringContent, message.getMessageContentAsString());
        assertArrayEquals(stringContent.getBytes(StandardCharsets.UTF_8), message.getMessageContent());
    }
    
    @Test
    void testEncodeAndDecode() {
        String content = "透传测试数据";
        T0900DataUplinkTransparentTransmission original = new T0900DataUplinkTransparentTransmission(
            T0900DataUplinkTransparentTransmission.TransparentMessageType.USER_DEFINED_TRANSPARENT_F0, content);
        
        // 编码
        Buffer encoded = original.encode();
        assertNotNull(encoded);
        assertTrue(encoded.length() > 0);
        
        // 验证编码格式
        assertEquals((byte) 0xF0, encoded.getByte(0)); // 透传消息类型
        
        // 解码
        T0900DataUplinkTransparentTransmission decoded = T0900DataUplinkTransparentTransmission.decode(encoded);
        assertNotNull(decoded);
        
        // 验证解码结果
        assertEquals(original.getMessageType(), decoded.getMessageType());
        assertEquals(original.getMessageContentAsString(), decoded.getMessageContentAsString());
        assertArrayEquals(original.getMessageContent(), decoded.getMessageContent());
    }
    
    @Test
    void testTransparentMessageTypeEnum() {
        // 测试枚举值
        assertEquals(0x00, T0900DataUplinkTransparentTransmission.TransparentMessageType.GNSS_MODULE_DETAILED_POSITIONING_DATA.getValue());
        assertEquals(0x0B, T0900DataUplinkTransparentTransmission.TransparentMessageType.ROAD_TRANSPORT_CERTIFICATE_IC_CARD_INFO.getValue());
        assertEquals(0x41, T0900DataUplinkTransparentTransmission.TransparentMessageType.SERIAL_PORT_1_TRANSPARENT.getValue());
        assertEquals(0x42, T0900DataUplinkTransparentTransmission.TransparentMessageType.SERIAL_PORT_2_TRANSPARENT.getValue());
        assertEquals(0xF0, T0900DataUplinkTransparentTransmission.TransparentMessageType.USER_DEFINED_TRANSPARENT_F0.getValue());
        assertEquals(0xFF, T0900DataUplinkTransparentTransmission.TransparentMessageType.USER_DEFINED_TRANSPARENT_FF.getValue());
        
        // 测试fromValue方法
        assertEquals(T0900DataUplinkTransparentTransmission.TransparentMessageType.GNSS_MODULE_DETAILED_POSITIONING_DATA,
                    T0900DataUplinkTransparentTransmission.TransparentMessageType.fromValue(0x00));
        assertEquals(T0900DataUplinkTransparentTransmission.TransparentMessageType.ROAD_TRANSPORT_CERTIFICATE_IC_CARD_INFO,
                    T0900DataUplinkTransparentTransmission.TransparentMessageType.fromValue(0x0B));
        assertEquals(T0900DataUplinkTransparentTransmission.TransparentMessageType.SERIAL_PORT_1_TRANSPARENT,
                    T0900DataUplinkTransparentTransmission.TransparentMessageType.fromValue(0x41));
        assertEquals(T0900DataUplinkTransparentTransmission.TransparentMessageType.USER_DEFINED_TRANSPARENT_FF,
                    T0900DataUplinkTransparentTransmission.TransparentMessageType.fromValue(0xFF));
        
        // 测试用户自定义范围的处理
        assertEquals(T0900DataUplinkTransparentTransmission.TransparentMessageType.USER_DEFINED_TRANSPARENT_F5,
                    T0900DataUplinkTransparentTransmission.TransparentMessageType.fromValue(0xF5)); // 已定义的用户自定义值
        
        // 测试无效值
        assertThrows(IllegalArgumentException.class, () -> {
            T0900DataUplinkTransparentTransmission.TransparentMessageType.fromValue(0x99);
        });
    }
    
    @Test
    void testToString() {
        T0900DataUplinkTransparentTransmission message = new T0900DataUplinkTransparentTransmission(
            T0900DataUplinkTransparentTransmission.TransparentMessageType.GNSS_MODULE_DETAILED_POSITIONING_DATA, "test");
        
        String toString = message.toString();
        assertNotNull(toString);
        assertTrue(toString.contains("T0900DataUplinkTransparentTransmission"));
        assertTrue(toString.contains("GNSS_MODULE_DETAILED_POSITIONING_DATA"));
        assertTrue(toString.contains("test"));
    }
    
    @Test
    void testEqualsAndHashCode() {
        byte[] content = {0x01, 0x02, 0x03};
        T0900DataUplinkTransparentTransmission message1 = new T0900DataUplinkTransparentTransmission(
            T0900DataUplinkTransparentTransmission.TransparentMessageType.SERIAL_PORT_1_TRANSPARENT, content);
        T0900DataUplinkTransparentTransmission message2 = new T0900DataUplinkTransparentTransmission(
            T0900DataUplinkTransparentTransmission.TransparentMessageType.SERIAL_PORT_1_TRANSPARENT, content);
        T0900DataUplinkTransparentTransmission message3 = new T0900DataUplinkTransparentTransmission(
            T0900DataUplinkTransparentTransmission.TransparentMessageType.SERIAL_PORT_2_TRANSPARENT, content);
        
        // 测试equals
        assertEquals(message1, message2);
        assertNotEquals(message1, message3);
        assertNotEquals(message1, null);
        assertNotEquals(message1, "string");
        
        // 测试hashCode
        assertEquals(message1.hashCode(), message2.hashCode());
    }
    
    @Test
    void testInvalidDecodeBuffer() {
        // 测试空缓冲区
        assertThrows(IllegalArgumentException.class, () -> {
            T0900DataUplinkTransparentTransmission.decode(null);
        });
        
        // 测试长度不足的缓冲区
        assertThrows(IllegalArgumentException.class, () -> {
            T0900DataUplinkTransparentTransmission.decode(Buffer.buffer());
        });
    }
    
    @Test
    void testNullParameterHandling() {
        // 测试null消息类型
        T0900DataUplinkTransparentTransmission message = new T0900DataUplinkTransparentTransmission(null, "test");
        assertEquals(T0900DataUplinkTransparentTransmission.TransparentMessageType.GNSS_MODULE_DETAILED_POSITIONING_DATA, message.getMessageType());
        
        // 测试null内容
        message = new T0900DataUplinkTransparentTransmission(
            T0900DataUplinkTransparentTransmission.TransparentMessageType.SERIAL_PORT_1_TRANSPARENT, (byte[]) null);
        assertEquals(0, message.getMessageContentLength());
        assertArrayEquals(new byte[0], message.getMessageContent());
        
        // 测试setter的null处理
        message.setMessageType(null);
        assertEquals(T0900DataUplinkTransparentTransmission.TransparentMessageType.GNSS_MODULE_DETAILED_POSITIONING_DATA, message.getMessageType());
        
        message.setMessageContent((byte[]) null);
        assertEquals(0, message.getMessageContentLength());
        
        message.setMessageContent((String) null);
        assertEquals("", message.getMessageContentAsString());
    }
    
    @Test
    void testFactoryCreation() {
        JT808MessageFactory factory = JT808MessageFactory.getInstance();
        JT808Message message = factory.createMessage(MessageTypes.Terminal.DATA_UPLINK_TRANSPARENT_TRANSMISSION);
        
        assertNotNull(message);
        assertInstanceOf(T0900DataUplinkTransparentTransmission.class, message);
        assertEquals(0x0900, message.getMessageId());
    }
    
    @Test
    void testEmptyContent() {
        T0900DataUplinkTransparentTransmission message = new T0900DataUplinkTransparentTransmission(
            T0900DataUplinkTransparentTransmission.TransparentMessageType.GNSS_MODULE_DETAILED_POSITIONING_DATA, new byte[0]);
        
        Buffer encoded = message.encode();
        assertEquals(1, encoded.length()); // 只有消息类型，没有内容
        
        T0900DataUplinkTransparentTransmission decoded = T0900DataUplinkTransparentTransmission.decode(encoded);
        assertEquals(0, decoded.getMessageContentLength());
        assertEquals("", decoded.getMessageContentAsString());
    }
    
    @Test
    void testLargeContent() {
        // 测试大数据量
        byte[] largeContent = new byte[1024];
        for (int i = 0; i < largeContent.length; i++) {
            largeContent[i] = (byte) (i % 256);
        }
        
        T0900DataUplinkTransparentTransmission message = new T0900DataUplinkTransparentTransmission(
            T0900DataUplinkTransparentTransmission.TransparentMessageType.USER_DEFINED_TRANSPARENT_FF, largeContent);
        
        Buffer encoded = message.encode();
        assertEquals(1025, encoded.length()); // 1字节类型 + 1024字节内容
        
        T0900DataUplinkTransparentTransmission decoded = T0900DataUplinkTransparentTransmission.decode(encoded);
        assertEquals(1024, decoded.getMessageContentLength());
        assertArrayEquals(largeContent, decoded.getMessageContent());
    }
    
    @Test
    void testAllUserDefinedTypes() {
        // 测试所有用户自定义类型
        T0900DataUplinkTransparentTransmission.TransparentMessageType[] userDefinedTypes = {
            T0900DataUplinkTransparentTransmission.TransparentMessageType.USER_DEFINED_TRANSPARENT_F0,
            T0900DataUplinkTransparentTransmission.TransparentMessageType.USER_DEFINED_TRANSPARENT_F1,
            T0900DataUplinkTransparentTransmission.TransparentMessageType.USER_DEFINED_TRANSPARENT_F2,
            T0900DataUplinkTransparentTransmission.TransparentMessageType.USER_DEFINED_TRANSPARENT_F3,
            T0900DataUplinkTransparentTransmission.TransparentMessageType.USER_DEFINED_TRANSPARENT_F4,
            T0900DataUplinkTransparentTransmission.TransparentMessageType.USER_DEFINED_TRANSPARENT_F5,
            T0900DataUplinkTransparentTransmission.TransparentMessageType.USER_DEFINED_TRANSPARENT_F6,
            T0900DataUplinkTransparentTransmission.TransparentMessageType.USER_DEFINED_TRANSPARENT_F7,
            T0900DataUplinkTransparentTransmission.TransparentMessageType.USER_DEFINED_TRANSPARENT_F8,
            T0900DataUplinkTransparentTransmission.TransparentMessageType.USER_DEFINED_TRANSPARENT_F9,
            T0900DataUplinkTransparentTransmission.TransparentMessageType.USER_DEFINED_TRANSPARENT_FA,
            T0900DataUplinkTransparentTransmission.TransparentMessageType.USER_DEFINED_TRANSPARENT_FB,
            T0900DataUplinkTransparentTransmission.TransparentMessageType.USER_DEFINED_TRANSPARENT_FC,
            T0900DataUplinkTransparentTransmission.TransparentMessageType.USER_DEFINED_TRANSPARENT_FD,
            T0900DataUplinkTransparentTransmission.TransparentMessageType.USER_DEFINED_TRANSPARENT_FE,
            T0900DataUplinkTransparentTransmission.TransparentMessageType.USER_DEFINED_TRANSPARENT_FF
        };
        
        for (int i = 0; i < userDefinedTypes.length; i++) {
            T0900DataUplinkTransparentTransmission.TransparentMessageType type = userDefinedTypes[i];
            assertEquals(0xF0 + i, type.getValue());
            
            T0900DataUplinkTransparentTransmission message = new T0900DataUplinkTransparentTransmission(type, "test" + i);
            Buffer encoded = message.encode();
            T0900DataUplinkTransparentTransmission decoded = T0900DataUplinkTransparentTransmission.decode(encoded);
            
            assertEquals(type, decoded.getMessageType());
            assertEquals("test" + i, decoded.getMessageContentAsString());
        }
    }
}