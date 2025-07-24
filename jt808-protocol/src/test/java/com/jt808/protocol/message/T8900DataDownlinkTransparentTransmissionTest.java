package com.jt808.protocol.message;

import com.jt808.protocol.constants.MessageTypes;
import com.jt808.protocol.factory.JT808MessageFactory;
import io.vertx.core.buffer.Buffer;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

/**
 * T8900数据下行透传测试类
 */
class T8900DataDownlinkTransparentTransmissionTest {
    
    @Test
    void testMessageId() {
        T8900DataDownlinkTransparentTransmission message = new T8900DataDownlinkTransparentTransmission();
        assertEquals(MessageTypes.Platform.DATA_DOWNLINK_TRANSPARENT_TRANSMISSION, message.getMessageId());
        assertEquals(0x8900, message.getMessageId());
    }
    
    @Test
    void testDefaultConstructor() {
        T8900DataDownlinkTransparentTransmission message = new T8900DataDownlinkTransparentTransmission();
        assertEquals(T8900DataDownlinkTransparentTransmission.TransparentMessageType.CUSTOM_TRANSPARENT_DATA, message.getMessageType());
        assertArrayEquals(new byte[0], message.getMessageContent());
        assertEquals(0, message.getMessageContentLength());
        assertEquals("", message.getMessageContentAsString());
    }
    
    @Test
    void testParameterizedConstructorWithBytes() {
        byte[] content = {0x01, 0x02, 0x03, 0x04};
        T8900DataDownlinkTransparentTransmission message = new T8900DataDownlinkTransparentTransmission(
            T8900DataDownlinkTransparentTransmission.TransparentMessageType.GNSS_MODULE_DETAILED_POSITIONING_DATA, content);
        
        assertEquals(T8900DataDownlinkTransparentTransmission.TransparentMessageType.GNSS_MODULE_DETAILED_POSITIONING_DATA, message.getMessageType());
        assertArrayEquals(content, message.getMessageContent());
        assertEquals(4, message.getMessageContentLength());
    }
    
    @Test
    void testParameterizedConstructorWithString() {
        String content = "Hello JT808";
        T8900DataDownlinkTransparentTransmission message = new T8900DataDownlinkTransparentTransmission(
            T8900DataDownlinkTransparentTransmission.TransparentMessageType.DRIVER_IDENTITY_IC_CARD_INFO, content);
        
        assertEquals(T8900DataDownlinkTransparentTransmission.TransparentMessageType.DRIVER_IDENTITY_IC_CARD_INFO, message.getMessageType());
        assertEquals(content, message.getMessageContentAsString());
        assertArrayEquals(content.getBytes(), message.getMessageContent());
    }
    
    @Test
    void testConstructorWithNullValues() {
        // 测试null消息类型
        T8900DataDownlinkTransparentTransmission message1 = new T8900DataDownlinkTransparentTransmission(null, new byte[]{1, 2, 3});
        assertEquals(T8900DataDownlinkTransparentTransmission.TransparentMessageType.CUSTOM_TRANSPARENT_DATA, message1.getMessageType());
        
        // 测试null内容
        T8900DataDownlinkTransparentTransmission message2 = new T8900DataDownlinkTransparentTransmission(
            T8900DataDownlinkTransparentTransmission.TransparentMessageType.GNSS_MODULE_DETAILED_POSITIONING_DATA, (byte[]) null);
        assertArrayEquals(new byte[0], message2.getMessageContent());
        
        // 测试null字符串内容
        T8900DataDownlinkTransparentTransmission message3 = new T8900DataDownlinkTransparentTransmission(
            T8900DataDownlinkTransparentTransmission.TransparentMessageType.GNSS_MODULE_DETAILED_POSITIONING_DATA, (String) null);
        assertArrayEquals(new byte[0], message3.getMessageContent());
    }
    
    @Test
    void testGettersAndSetters() {
        T8900DataDownlinkTransparentTransmission message = new T8900DataDownlinkTransparentTransmission();
        
        // 测试消息类型
        message.setMessageType(T8900DataDownlinkTransparentTransmission.TransparentMessageType.ROAD_TRANSPORT_CERTIFICATE_IC_CARD_INFO);
        assertEquals(T8900DataDownlinkTransparentTransmission.TransparentMessageType.ROAD_TRANSPORT_CERTIFICATE_IC_CARD_INFO, message.getMessageType());
        
        // 测试设置null消息类型
        message.setMessageType(null);
        assertEquals(T8900DataDownlinkTransparentTransmission.TransparentMessageType.CUSTOM_TRANSPARENT_DATA, message.getMessageType());
        
        // 测试字节数组内容
        byte[] content = {0x10, 0x20, 0x30};
        message.setMessageContent(content);
        assertArrayEquals(content, message.getMessageContent());
        assertEquals(3, message.getMessageContentLength());
        
        // 测试字符串内容
        String stringContent = "测试内容";
        message.setMessageContent(stringContent);
        assertEquals(stringContent, message.getMessageContentAsString());
        assertArrayEquals(stringContent.getBytes(), message.getMessageContent());
        
        // 测试设置null内容
        message.setMessageContent((byte[]) null);
        assertArrayEquals(new byte[0], message.getMessageContent());
        assertEquals(0, message.getMessageContentLength());
    }
    
    @Test
    void testEncodeAndDecode() {
        String content = "透传数据测试";
        T8900DataDownlinkTransparentTransmission original = new T8900DataDownlinkTransparentTransmission(
            T8900DataDownlinkTransparentTransmission.TransparentMessageType.DRIVER_IDENTITY_IC_CARD_INFO, content);
        
        // 编码
        Buffer encoded = original.encode();
        assertEquals(1 + content.getBytes().length, encoded.length()); // 1字节消息类型 + 内容长度
        
        // 验证编码内容
        assertEquals(0x0C, encoded.getByte(0) & 0xFF); // 驾驶员身份IC卡信息类型
        
        // 解码
        T8900DataDownlinkTransparentTransmission decoded = T8900DataDownlinkTransparentTransmission.decode(encoded);
        
        assertEquals(original.getMessageType(), decoded.getMessageType());
        assertEquals(original.getMessageContentAsString(), decoded.getMessageContentAsString());
        assertArrayEquals(original.getMessageContent(), decoded.getMessageContent());
        assertEquals(original, decoded);
    }
    
    @Test
    void testEncodeDecodeEmptyContent() {
        T8900DataDownlinkTransparentTransmission original = new T8900DataDownlinkTransparentTransmission(
            T8900DataDownlinkTransparentTransmission.TransparentMessageType.GNSS_MODULE_DETAILED_POSITIONING_DATA, new byte[0]);
        
        // 编码
        Buffer encoded = original.encode();
        assertEquals(1, encoded.length()); // 只有1字节消息类型
        assertEquals(0x00, encoded.getByte(0) & 0xFF);
        
        // 解码
        T8900DataDownlinkTransparentTransmission decoded = T8900DataDownlinkTransparentTransmission.decode(encoded);
        assertEquals(original.getMessageType(), decoded.getMessageType());
        assertEquals(0, decoded.getMessageContentLength());
        assertArrayEquals(new byte[0], decoded.getMessageContent());
    }
    
    @Test
    void testDecodeInvalidBuffer() {
        // 测试空缓冲区
        Buffer emptyBuffer = Buffer.buffer();
        assertThrows(IllegalArgumentException.class, () -> {
            T8900DataDownlinkTransparentTransmission.decode(emptyBuffer);
        });
    }
    
    @Test
    void testTransparentMessageTypeEnum() {
        // 测试枚举值
        assertEquals(0x00, T8900DataDownlinkTransparentTransmission.TransparentMessageType.GNSS_MODULE_DETAILED_POSITIONING_DATA.getValue());
        assertEquals(0x0B, T8900DataDownlinkTransparentTransmission.TransparentMessageType.ROAD_TRANSPORT_CERTIFICATE_IC_CARD_INFO.getValue());
        assertEquals(0x0C, T8900DataDownlinkTransparentTransmission.TransparentMessageType.DRIVER_IDENTITY_IC_CARD_INFO.getValue());
        assertEquals(0xF0, T8900DataDownlinkTransparentTransmission.TransparentMessageType.GNSS_MODULE_DETAILED_POSITIONING_DATA_WITH_EXTENSION.getValue());
        assertEquals(0xFF, T8900DataDownlinkTransparentTransmission.TransparentMessageType.CUSTOM_TRANSPARENT_DATA.getValue());
        
        // 测试描述
        assertEquals("GNSS模块详细定位数据", T8900DataDownlinkTransparentTransmission.TransparentMessageType.GNSS_MODULE_DETAILED_POSITIONING_DATA.getDescription());
        assertEquals("道路运输证IC卡信息", T8900DataDownlinkTransparentTransmission.TransparentMessageType.ROAD_TRANSPORT_CERTIFICATE_IC_CARD_INFO.getDescription());
        assertEquals("驾驶员身份IC卡信息", T8900DataDownlinkTransparentTransmission.TransparentMessageType.DRIVER_IDENTITY_IC_CARD_INFO.getDescription());
        assertEquals("GNSS模块详细定位数据(扩展)", T8900DataDownlinkTransparentTransmission.TransparentMessageType.GNSS_MODULE_DETAILED_POSITIONING_DATA_WITH_EXTENSION.getDescription());
        assertEquals("自定义透传数据", T8900DataDownlinkTransparentTransmission.TransparentMessageType.CUSTOM_TRANSPARENT_DATA.getDescription());
        
        // 测试fromValue方法
        assertEquals(T8900DataDownlinkTransparentTransmission.TransparentMessageType.GNSS_MODULE_DETAILED_POSITIONING_DATA, 
                    T8900DataDownlinkTransparentTransmission.TransparentMessageType.fromValue(0x00));
        assertEquals(T8900DataDownlinkTransparentTransmission.TransparentMessageType.ROAD_TRANSPORT_CERTIFICATE_IC_CARD_INFO, 
                    T8900DataDownlinkTransparentTransmission.TransparentMessageType.fromValue(0x0B));
        assertEquals(T8900DataDownlinkTransparentTransmission.TransparentMessageType.DRIVER_IDENTITY_IC_CARD_INFO, 
                    T8900DataDownlinkTransparentTransmission.TransparentMessageType.fromValue(0x0C));
        
        // 测试未知值返回自定义透传数据类型
        assertEquals(T8900DataDownlinkTransparentTransmission.TransparentMessageType.CUSTOM_TRANSPARENT_DATA, 
                    T8900DataDownlinkTransparentTransmission.TransparentMessageType.fromValue(0x99));
    }
    
    @Test
    void testToString() {
        byte[] content = {0x01, 0x02, 0x03};
        T8900DataDownlinkTransparentTransmission message = new T8900DataDownlinkTransparentTransmission(
            T8900DataDownlinkTransparentTransmission.TransparentMessageType.GNSS_MODULE_DETAILED_POSITIONING_DATA, content);
        
        String str = message.toString();
        assertTrue(str.contains("T8900DataDownlinkTransparentTransmission"));
        assertTrue(str.contains("messageType=GNSS模块详细定位数据"));
        assertTrue(str.contains("messageContentLength=3"));
        assertTrue(str.contains("messageId=0x8900"));
    }
    
    @Test
    void testToStringWithLongContent() {
        // 测试长内容的toString（应该显示字节数而不是完整内容）
        byte[] longContent = new byte[100];
        for (int i = 0; i < longContent.length; i++) {
            longContent[i] = (byte) (i % 256);
        }
        
        T8900DataDownlinkTransparentTransmission message = new T8900DataDownlinkTransparentTransmission(
            T8900DataDownlinkTransparentTransmission.TransparentMessageType.CUSTOM_TRANSPARENT_DATA, longContent);
        
        String str = message.toString();
        assertTrue(str.contains("[100 bytes]"));
        assertFalse(str.contains("[0, 1, 2")); // 不应该显示完整数组内容
    }
    
    @Test
    void testEqualsAndHashCode() {
        byte[] content = {0x10, 0x20, 0x30};
        T8900DataDownlinkTransparentTransmission message1 = new T8900DataDownlinkTransparentTransmission(
            T8900DataDownlinkTransparentTransmission.TransparentMessageType.DRIVER_IDENTITY_IC_CARD_INFO, content);
        T8900DataDownlinkTransparentTransmission message2 = new T8900DataDownlinkTransparentTransmission(
            T8900DataDownlinkTransparentTransmission.TransparentMessageType.DRIVER_IDENTITY_IC_CARD_INFO, content.clone());
        T8900DataDownlinkTransparentTransmission message3 = new T8900DataDownlinkTransparentTransmission(
            T8900DataDownlinkTransparentTransmission.TransparentMessageType.GNSS_MODULE_DETAILED_POSITIONING_DATA, content);
        
        // 测试equals
        assertEquals(message1, message2);
        assertNotEquals(message1, message3);
        assertNotEquals(message1, null);
        assertNotEquals(message1, "not a message");
        
        // 测试hashCode
        assertEquals(message1.hashCode(), message2.hashCode());
        assertNotEquals(message1.hashCode(), message3.hashCode());
    }
    
    @Test
    void testFactoryCreation() {
        JT808MessageFactory factory = JT808MessageFactory.getInstance();
        JT808Message message = factory.createMessage(MessageTypes.Platform.DATA_DOWNLINK_TRANSPARENT_TRANSMISSION);
        
        assertNotNull(message);
        assertInstanceOf(T8900DataDownlinkTransparentTransmission.class, message);
        assertEquals(0x8900, message.getMessageId());
    }
    
    @Test
    void testDifferentMessageTypes() {
        String content = "测试数据";
        
        // 测试所有消息类型
        T8900DataDownlinkTransparentTransmission.TransparentMessageType[] types = {
            T8900DataDownlinkTransparentTransmission.TransparentMessageType.GNSS_MODULE_DETAILED_POSITIONING_DATA,
            T8900DataDownlinkTransparentTransmission.TransparentMessageType.ROAD_TRANSPORT_CERTIFICATE_IC_CARD_INFO,
            T8900DataDownlinkTransparentTransmission.TransparentMessageType.DRIVER_IDENTITY_IC_CARD_INFO,
            T8900DataDownlinkTransparentTransmission.TransparentMessageType.GNSS_MODULE_DETAILED_POSITIONING_DATA_WITH_EXTENSION,
            T8900DataDownlinkTransparentTransmission.TransparentMessageType.CUSTOM_TRANSPARENT_DATA
        };
        
        for (T8900DataDownlinkTransparentTransmission.TransparentMessageType type : types) {
            T8900DataDownlinkTransparentTransmission message = new T8900DataDownlinkTransparentTransmission(type, content);
            
            // 编码解码测试
            Buffer encoded = message.encode();
            T8900DataDownlinkTransparentTransmission decoded = T8900DataDownlinkTransparentTransmission.decode(encoded);
            
            assertEquals(type, decoded.getMessageType());
            assertEquals(content, decoded.getMessageContentAsString());
            assertEquals(message, decoded);
        }
    }
    
    @Test
    void testChineseContent() {
        String chineseContent = "中文透传数据测试";
        T8900DataDownlinkTransparentTransmission message = new T8900DataDownlinkTransparentTransmission(
            T8900DataDownlinkTransparentTransmission.TransparentMessageType.CUSTOM_TRANSPARENT_DATA, chineseContent);
        
        // 编码解码测试
        Buffer encoded = message.encode();
        T8900DataDownlinkTransparentTransmission decoded = T8900DataDownlinkTransparentTransmission.decode(encoded);
        
        assertEquals(chineseContent, decoded.getMessageContentAsString());
        assertArrayEquals(chineseContent.getBytes(StandardCharsets.UTF_8), decoded.getMessageContent());
    }
    
    @Test
    void testBinaryContent() {
        // 测试二进制内容
        byte[] binaryContent = new byte[256];
        for (int i = 0; i < 256; i++) {
            binaryContent[i] = (byte) i;
        }
        
        T8900DataDownlinkTransparentTransmission message = new T8900DataDownlinkTransparentTransmission(
            T8900DataDownlinkTransparentTransmission.TransparentMessageType.GNSS_MODULE_DETAILED_POSITIONING_DATA, binaryContent);
        
        // 编码解码测试
        Buffer encoded = message.encode();
        assertEquals(257, encoded.length()); // 1字节类型 + 256字节内容
        
        T8900DataDownlinkTransparentTransmission decoded = T8900DataDownlinkTransparentTransmission.decode(encoded);
        assertArrayEquals(binaryContent, decoded.getMessageContent());
        assertEquals(256, decoded.getMessageContentLength());
    }
}