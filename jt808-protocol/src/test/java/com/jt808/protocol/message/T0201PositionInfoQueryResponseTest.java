package com.jt808.protocol.message;

import com.jt808.protocol.factory.JT808MessageFactory;
import io.vertx.core.buffer.Buffer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * T0201PositionInfoQueryResponse 单元测试
 * 
 * @author JT808 Protocol Team
 * @version 1.0
 * @since 1.0
 */
class T0201PositionInfoQueryResponseTest {
    
    private T0201PositionInfoQueryResponse message;
    private T0200LocationReport locationReport;
    
    @BeforeEach
    void setUp() {
        message = new T0201PositionInfoQueryResponse();
        
        // 创建测试用的位置信息汇报
        locationReport = new T0200LocationReport();
        locationReport.setLatitudeDegrees(39.908692);
        locationReport.setLongitudeDegrees(116.397477);
        locationReport.setAltitude(50);
        locationReport.setSpeed(60);
        locationReport.setDirection(90);
        locationReport.setDateTime(LocalDateTime.of(2024, 1, 15, 14, 30, 45));
    }
    
    @Test
    void testMessageId() {
        assertEquals(0x0201, message.getMessageId());
    }
    
    @Test
    void testDefaultConstructor() {
        T0201PositionInfoQueryResponse msg = new T0201PositionInfoQueryResponse();
        assertEquals(0x0201, msg.getMessageId());
        assertEquals(0, msg.getResponseSerialNumber());
        assertNotNull(msg.getLocationReport());
    }
    
    @Test
    void testParameterizedConstructor() {
        T0201PositionInfoQueryResponse msg = new T0201PositionInfoQueryResponse(12345, locationReport);
        assertEquals(0x0201, msg.getMessageId());
        assertEquals(12345, msg.getResponseSerialNumber());
        assertEquals(locationReport, msg.getLocationReport());
    }
    
    @Test
    void testParameterizedConstructorWithNullLocationReport() {
        T0201PositionInfoQueryResponse msg = new T0201PositionInfoQueryResponse(12345, null);
        assertEquals(12345, msg.getResponseSerialNumber());
        assertNotNull(msg.getLocationReport());
    }
    
    @Test
    void testGettersAndSetters() {
        message.setResponseSerialNumber(54321);
        assertEquals(54321, message.getResponseSerialNumber());
        
        message.setLocationReport(locationReport);
        assertEquals(locationReport, message.getLocationReport());
    }
    
    @Test
    void testMessageDescription() {
        assertEquals("位置信息查询应答", message.getMessageDescription());
    }
    
    @Test
    void testStaticFactoryCreate() {
        T0201PositionInfoQueryResponse msg = T0201PositionInfoQueryResponse.create(98765, locationReport);
        assertEquals(0x0201, msg.getMessageId());
        assertEquals(98765, msg.getResponseSerialNumber());
        assertEquals(locationReport, msg.getLocationReport());
    }
    
    @Test
    void testStaticFactoryCreateWithPosition() {
        T0201PositionInfoQueryResponse msg = T0201PositionInfoQueryResponse.createWithPosition(
            11111, 39.908692, 116.397477, 50, 60, 90);
        
        assertEquals(0x0201, msg.getMessageId());
        assertEquals(11111, msg.getResponseSerialNumber());
        assertNotNull(msg.getLocationReport());
        assertEquals(39.908692, msg.getLocationReport().getLatitudeDegrees(), 0.000001);
        assertEquals(116.397477, msg.getLocationReport().getLongitudeDegrees(), 0.000001);
        assertEquals(50, msg.getLocationReport().getAltitude());
        assertEquals(60, msg.getLocationReport().getSpeed());
        assertEquals(90, msg.getLocationReport().getDirection());
        assertNotNull(msg.getLocationReport().getDateTime());
    }
    
    @Test
    void testEncodeBody() {
        message.setResponseSerialNumber(12345);
        message.setLocationReport(locationReport);
        
        Buffer encoded = message.encodeBody();
        assertNotNull(encoded);
        assertTrue(encoded.length() >= 2); // 至少包含应答流水号
        
        // 验证应答流水号编码
        assertEquals(12345, encoded.getUnsignedShort(0));
    }
    
    @Test
    void testEncodeBodyWithNullLocationReport() {
        message.setResponseSerialNumber(12345);
        message.setLocationReport(null);
        
        Buffer encoded = message.encodeBody();
        assertNotNull(encoded);
        assertEquals(2, encoded.length()); // 只有应答流水号
    }
    
    @Test
    void testDecodeBody() {
        // 先编码一个完整的消息
        T0201PositionInfoQueryResponse originalMsg = new T0201PositionInfoQueryResponse(54321, locationReport);
        Buffer encoded = originalMsg.encodeBody();
        
        // 然后解码
        T0201PositionInfoQueryResponse decodedMsg = new T0201PositionInfoQueryResponse();
        decodedMsg.decodeBody(encoded);
        
        assertEquals(54321, decodedMsg.getResponseSerialNumber());
        assertNotNull(decodedMsg.getLocationReport());
        assertEquals(locationReport.getLatitudeDegrees(), decodedMsg.getLocationReport().getLatitudeDegrees(), 0.000001);
        assertEquals(locationReport.getLongitudeDegrees(), decodedMsg.getLocationReport().getLongitudeDegrees(), 0.000001);
    }
    
    @Test
    void testDecodeBodyWithInsufficientLength() {
        Buffer shortBuffer = Buffer.buffer().appendByte((byte) 0x01);
        
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            message.decodeBody(shortBuffer);
        });
        
        assertTrue(exception.getMessage().contains("位置信息查询应答消息体长度不足"));
    }
    
    @Test
    void testDecodeBodyWithNullBuffer() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            message.decodeBody(null);
        });
        
        assertTrue(exception.getMessage().contains("位置信息查询应答消息体长度不足"));
    }
    
    @Test
    void testDecodeBodyWithOnlySerialNumber() {
        Buffer buffer = Buffer.buffer().appendUnsignedShort(12345);
        
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            message.decodeBody(buffer);
        });
        
        assertTrue(exception.getMessage().contains("位置信息查询应答消息体缺少位置信息汇报数据"));
    }
    
    @Test
    void testEncodeDecodeRoundTrip() {
        T0201PositionInfoQueryResponse originalMsg = new T0201PositionInfoQueryResponse(54321, locationReport);
        
        // 编码
        Buffer encoded = originalMsg.encodeBody();
        
        // 解码
        T0201PositionInfoQueryResponse decodedMsg = new T0201PositionInfoQueryResponse();
        decodedMsg.decodeBody(encoded);
        
        // 验证往返一致性
        assertEquals(originalMsg.getResponseSerialNumber(), decodedMsg.getResponseSerialNumber());
        assertEquals(originalMsg.getLocationReport().getLatitudeDegrees(), 
            decodedMsg.getLocationReport().getLatitudeDegrees(), 0.000001);
        assertEquals(originalMsg.getLocationReport().getLongitudeDegrees(), 
            decodedMsg.getLocationReport().getLongitudeDegrees(), 0.000001);
        assertEquals(originalMsg.getLocationReport().getAltitude(), 
            decodedMsg.getLocationReport().getAltitude());
        assertEquals(originalMsg.getLocationReport().getSpeed(), 
            decodedMsg.getLocationReport().getSpeed());
        assertEquals(originalMsg.getLocationReport().getDirection(), 
            decodedMsg.getLocationReport().getDirection());
    }
    
    @Test
    void testFactoryCreation() {
        JT808MessageFactory factory = JT808MessageFactory.getInstance();
        JT808Message msg = factory.createMessage(0x0201);
        
        assertNotNull(msg);
        assertInstanceOf(T0201PositionInfoQueryResponse.class, msg);
        assertEquals(0x0201, msg.getMessageId());
    }
    
    @Test
    void testToString() {
        message.setResponseSerialNumber(12345);
        message.setLocationReport(locationReport);
        
        String str = message.toString();
        assertNotNull(str);
        assertTrue(str.contains("T0201PositionInfoQueryResponse"));
        assertTrue(str.contains("messageId=0x201"));
        assertTrue(str.contains("位置信息查询应答"));
        assertTrue(str.contains("responseSerialNumber=12345"));
        assertTrue(str.contains("latitude="));
        assertTrue(str.contains("longitude="));
    }
    
    @Test
    void testToStringWithNullLocationReport() {
        message.setResponseSerialNumber(12345);
        message.setLocationReport(null);
        
        String str = message.toString();
        assertNotNull(str);
        assertTrue(str.contains("locationReport=null"));
    }
    
    @Test
    void testEquals() {
        T0201PositionInfoQueryResponse msg1 = new T0201PositionInfoQueryResponse(12345, locationReport);
        T0201PositionInfoQueryResponse msg2 = new T0201PositionInfoQueryResponse(12345, locationReport);
        T0201PositionInfoQueryResponse msg3 = new T0201PositionInfoQueryResponse(54321, locationReport);
        
        assertEquals(msg1, msg1); // 自反性
        assertEquals(msg1, msg2); // 对称性
        assertNotEquals(msg1, msg3); // 不同的应答流水号
        assertNotEquals(msg1, null);
        assertNotEquals(msg1, "not a message");
    }
    
    @Test
    void testEqualsWithNullLocationReport() {
        T0201PositionInfoQueryResponse msg1 = new T0201PositionInfoQueryResponse();
        msg1.setResponseSerialNumber(12345);
        msg1.setLocationReport(null);
        
        T0201PositionInfoQueryResponse msg2 = new T0201PositionInfoQueryResponse();
        msg2.setResponseSerialNumber(12345);
        msg2.setLocationReport(null);
        
        assertEquals(msg1, msg2);
    }
    
    @Test
    void testHashCode() {
        T0201PositionInfoQueryResponse msg1 = new T0201PositionInfoQueryResponse(12345, locationReport);
        T0201PositionInfoQueryResponse msg2 = new T0201PositionInfoQueryResponse(12345, locationReport);
        
        assertEquals(msg1.hashCode(), msg2.hashCode());
    }
    
    @Test
    void testHashCodeWithNullLocationReport() {
        T0201PositionInfoQueryResponse msg1 = new T0201PositionInfoQueryResponse();
        msg1.setResponseSerialNumber(12345);
        msg1.setLocationReport(null);
        
        T0201PositionInfoQueryResponse msg2 = new T0201PositionInfoQueryResponse();
        msg2.setResponseSerialNumber(12345);
        msg2.setLocationReport(null);
        
        assertEquals(msg1.hashCode(), msg2.hashCode());
    }
    
    @Test
    void testMultipleEncodingsConsistency() {
        message.setResponseSerialNumber(77777);
        message.setLocationReport(locationReport);
        
        Buffer encoded1 = message.encodeBody();
        Buffer encoded2 = message.encodeBody();
        
        assertEquals(encoded1.length(), encoded2.length());
        assertArrayEquals(encoded1.getBytes(), encoded2.getBytes());
    }
    
    @Test
    void testBoundaryValues() {
        // 测试边界值
        T0201PositionInfoQueryResponse msg = new T0201PositionInfoQueryResponse();
        
        // 最小值
        msg.setResponseSerialNumber(0);
        Buffer encoded = msg.encodeBody();
        assertEquals(0, encoded.getUnsignedShort(0));
        
        // 最大值 (WORD类型)
        msg.setResponseSerialNumber(65535);
        encoded = msg.encodeBody();
        assertEquals(65535, encoded.getUnsignedShort(0));
    }
}