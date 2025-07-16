package com.jt808.protocol.message;

import com.jt808.protocol.factory.JT808MessageFactory;
import io.vertx.core.buffer.Buffer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * T0500VehicleControlResponse 车辆控制应答消息测试类
 */
class T0500VehicleControlResponseTest {

    private static final Logger logger = LoggerFactory.getLogger(T0500VehicleControlResponseTest.class);

    private T0500VehicleControlResponse message;
    private T0200LocationReport locationReport;

    @BeforeEach
    void setUp() {
        message = new T0500VehicleControlResponse();
        
        // 创建测试用的位置信息汇报
        locationReport = new T0200LocationReport();
        locationReport.setAlarmFlag(0x00000001);
        locationReport.setStatusFlag(0x00000002);
        locationReport.setLatitude(39908692);  // 北京纬度
        locationReport.setLongitude(116397477); // 北京经度
        locationReport.setAltitude(50);
        locationReport.setSpeed(60);
        locationReport.setDirection(90);
        locationReport.setDateTime(LocalDateTime.of(2024, 1, 15, 14, 30, 45));
    }

    @Test
    void testMessageId() {
        // 测试消息ID
        assertEquals(0x0500, message.getMessageId());
        logger.info("消息ID测试通过: 0x{}", Integer.toHexString(message.getMessageId()).toUpperCase());
    }

    @Test
    void testConstructors() {
        // 测试默认构造函数
        T0500VehicleControlResponse defaultMessage = new T0500VehicleControlResponse();
        assertEquals(0, defaultMessage.getResponseSerialNumber());
        assertNull(defaultMessage.getLocationReport());
        
        // 测试带参数构造函数
        T0500VehicleControlResponse paramMessage = new T0500VehicleControlResponse(12345, locationReport);
        assertEquals(12345, paramMessage.getResponseSerialNumber());
        assertEquals(locationReport, paramMessage.getLocationReport());
        
        logger.info("构造函数测试通过");
    }

    @Test
    void testCreateMethods() {
        // 测试创建带位置信息的应答消息
        T0500VehicleControlResponse withLocation = T0500VehicleControlResponse.create(12345, locationReport);
        assertEquals(12345, withLocation.getResponseSerialNumber());
        assertEquals(locationReport, withLocation.getLocationReport());
        assertTrue(withLocation.hasLocationReport());
        
        // 测试创建不带位置信息的应答消息
        T0500VehicleControlResponse withoutLocation = T0500VehicleControlResponse.create(54321);
        assertEquals(54321, withoutLocation.getResponseSerialNumber());
        assertNull(withoutLocation.getLocationReport());
        assertFalse(withoutLocation.hasLocationReport());
        
        logger.info("静态工厂方法测试通过");
    }

    @Test
    void testGettersAndSetters() {
        // 测试应答流水号
        message.setResponseSerialNumber(9999);
        assertEquals(9999, message.getResponseSerialNumber());
        
        // 测试位置信息汇报
        message.setLocationReport(locationReport);
        assertEquals(locationReport, message.getLocationReport());
        
        logger.info("Getter/Setter方法测试通过");
    }

    @Test
    void testHasLocationReport() {
        // 测试无位置信息
        assertFalse(message.hasLocationReport());
        
        // 测试有位置信息
        message.setLocationReport(locationReport);
        assertTrue(message.hasLocationReport());
        
        // 测试设置为null
        message.setLocationReport(null);
        assertFalse(message.hasLocationReport());
        
        logger.info("位置信息检查测试通过");
    }

    @Test
    void testUnsignedSerialNumber() {
        // 测试正数
        message.setResponseSerialNumber(12345);
        assertEquals(12345L, message.getResponseSerialNumberUnsigned());
        
        // 测试负数（模拟无符号值）
        message.setResponseSerialNumber(-1);
        assertEquals(4294967295L, message.getResponseSerialNumberUnsigned());
        
        // 测试最大值
        message.setResponseSerialNumber(65535);
        assertEquals(65535L, message.getResponseSerialNumberUnsigned());
        
        logger.info("无符号流水号测试通过");
    }

    @Test
    void testDescriptionMethods() {
        // 测试消息描述
        assertEquals("车辆控制应答", message.getMessageDescription());
        
        // 测试无位置信息的应答描述
        assertEquals("控制应答，无位置信息", message.getResponseDescription());
        
        // 测试有位置信息的应答描述
        message.setLocationReport(locationReport);
        assertEquals("控制成功，包含位置信息", message.getResponseDescription());
        
        logger.info("描述方法测试通过");
    }

    @Test
    void testEncodeWithoutLocation() {
        // 测试不包含位置信息的编码
        message.setResponseSerialNumber(12345);
        
        Buffer encoded = message.encodeBody();
        assertNotNull(encoded);
        assertEquals(2, encoded.length()); // 只有应答流水号2字节
        
        // 验证编码内容
        assertEquals((byte) 0x30, encoded.getByte(0)); // 12345的高字节
        assertEquals((byte) 0x39, encoded.getByte(1)); // 12345的低字节
        
        logger.info("无位置信息编码测试通过，编码长度: {} 字节", encoded.length());
    }

    @Test
    void testEncodeWithLocation() {
        // 测试包含位置信息的编码
        message.setResponseSerialNumber(12345);
        message.setLocationReport(locationReport);
        
        Buffer encoded = message.encodeBody();
        assertNotNull(encoded);
        assertTrue(encoded.length() > 2); // 应答流水号2字节 + 位置信息
        
        // 验证应答流水号部分
        assertEquals((byte) 0x30, encoded.getByte(0)); // 12345的高字节
        assertEquals((byte) 0x39, encoded.getByte(1)); // 12345的低字节
        
        logger.info("包含位置信息编码测试通过，编码长度: {} 字节", encoded.length());
    }

    @Test
    void testDecodeWithoutLocation() {
        // 测试解码不包含位置信息的消息
        Buffer data = Buffer.buffer().appendByte((byte) 0x30).appendByte((byte) 0x39); // 12345
        
        message.decodeBody(data);
        assertEquals(12345, message.getResponseSerialNumber());
        assertNull(message.getLocationReport());
        assertFalse(message.hasLocationReport());
        
        logger.info("无位置信息解码测试通过");
    }

    @Test
    void testDecodeWithLocation() {
        // 先编码一个包含位置信息的消息
        T0500VehicleControlResponse originalMessage = new T0500VehicleControlResponse(12345, locationReport);
        Buffer encoded = originalMessage.encodeBody();
        
        // 解码
        T0500VehicleControlResponse decodedMessage = new T0500VehicleControlResponse();
        decodedMessage.decodeBody(encoded);
        
        // 验证解码结果
        assertEquals(12345, decodedMessage.getResponseSerialNumber());
        assertNotNull(decodedMessage.getLocationReport());
        assertTrue(decodedMessage.hasLocationReport());
        
        logger.info("包含位置信息解码测试通过");
    }

    @Test
    void testEncodeDecodeConsistency() {
        // 测试编解码一致性（不包含位置信息）
        message.setResponseSerialNumber(54321);
        
        Buffer encoded = message.encodeBody();
        T0500VehicleControlResponse decoded = new T0500VehicleControlResponse();
        decoded.decodeBody(encoded);
        
        assertEquals(message.getResponseSerialNumber(), decoded.getResponseSerialNumber());
        assertEquals(message.hasLocationReport(), decoded.hasLocationReport());
        
        // 测试编解码一致性（包含位置信息）
        message.setLocationReport(locationReport);
        
        Buffer encodedWithLocation = message.encodeBody();
        T0500VehicleControlResponse decodedWithLocation = new T0500VehicleControlResponse();
        decodedWithLocation.decodeBody(encodedWithLocation);
        
        assertEquals(message.getResponseSerialNumber(), decodedWithLocation.getResponseSerialNumber());
        assertEquals(message.hasLocationReport(), decodedWithLocation.hasLocationReport());
        
        logger.info("编解码一致性测试通过");
    }

    @Test
    void testDecodeInvalidData() {
        // 测试解码无效数据
        assertThrows(IllegalArgumentException.class, () -> {
            message.decodeBody(null);
        });
        
        assertThrows(IllegalArgumentException.class, () -> {
            message.decodeBody(Buffer.buffer().appendByte((byte) 0x01)); // 长度不足2字节
        });
        
        assertThrows(IllegalArgumentException.class, () -> {
            message.decodeBody(Buffer.buffer()); // 空Buffer
        });
        
        logger.info("无效数据解码异常测试通过");
    }

    @Test
    void testToString() {
        // 测试toString方法
        message.setResponseSerialNumber(12345);
        
        String str1 = message.toString();
        assertTrue(str1.contains("T0500VehicleControlResponse"));
        assertTrue(str1.contains("responseSerialNumber=12345"));
        assertTrue(str1.contains("hasLocationReport=false"));
        
        message.setLocationReport(locationReport);
        String str2 = message.toString();
        assertTrue(str2.contains("hasLocationReport=true"));
        
        logger.info("toString方法测试通过");
    }

    @Test
    void testEquals() {
        // 测试equals方法
        T0500VehicleControlResponse message1 = new T0500VehicleControlResponse(12345, null);
        T0500VehicleControlResponse message2 = new T0500VehicleControlResponse(12345, null);
        T0500VehicleControlResponse message3 = new T0500VehicleControlResponse(54321, null);
        
        assertEquals(message1, message2);
        assertNotEquals(message1, message3);
        assertNotEquals(message1, null);
        assertNotEquals(message1, "string");
        
        // 测试包含位置信息的equals
        message1.setLocationReport(locationReport);
        message2.setLocationReport(locationReport);
        assertEquals(message1, message2);
        
        logger.info("equals方法测试通过");
    }

    @Test
    void testHashCode() {
        // 测试hashCode方法
        T0500VehicleControlResponse message1 = new T0500VehicleControlResponse(12345, null);
        T0500VehicleControlResponse message2 = new T0500VehicleControlResponse(12345, null);
        
        assertEquals(message1.hashCode(), message2.hashCode());
        
        message1.setLocationReport(locationReport);
        message2.setLocationReport(locationReport);
        assertEquals(message1.hashCode(), message2.hashCode());
        
        logger.info("hashCode方法测试通过");
    }

    @Test
    void testResponseStatusConstants() {
        // 测试应答状态常量
        assertEquals("控制成功", T0500VehicleControlResponse.ResponseStatus.SUCCESS);
        assertEquals("控制失败", T0500VehicleControlResponse.ResponseStatus.FAILURE);
        assertEquals("控制超时", T0500VehicleControlResponse.ResponseStatus.TIMEOUT);
        assertEquals("不支持该控制", T0500VehicleControlResponse.ResponseStatus.UNSUPPORTED);
        
        logger.info("应答状态常量测试通过");
    }

    @Test
    void testMessageFactoryIntegration() {
        // 测试消息工厂集成
        JT808MessageFactory factory = JT808MessageFactory.getInstance();
        JT808Message createdMessage = factory.createMessage(0x0500);
        
        assertNotNull(createdMessage);
        assertInstanceOf(T0500VehicleControlResponse.class, createdMessage);
        
        logger.info("消息工厂集成测试通过");
    }

    @Test
    void testRealWorldScenario() {
        // 测试真实场景：车辆控制成功应答
        T0500VehicleControlResponse successResponse = T0500VehicleControlResponse.create(12345, locationReport);
        
        // 验证应答信息
        assertEquals(12345, successResponse.getResponseSerialNumber());
        assertTrue(successResponse.hasLocationReport());
        assertEquals("控制成功，包含位置信息", successResponse.getResponseDescription());
        
        // 编解码测试
        Buffer encoded = successResponse.encodeBody();
        T0500VehicleControlResponse decoded = new T0500VehicleControlResponse();
        decoded.decodeBody(encoded);
        
        assertEquals(successResponse.getResponseSerialNumber(), decoded.getResponseSerialNumber());
        assertEquals(successResponse.hasLocationReport(), decoded.hasLocationReport());
        
        logger.info("真实场景测试通过：车辆控制成功应答");
    }

    @Test
    void testFailureScenario() {
        // 测试失败场景：车辆控制失败应答（无位置信息）
        T0500VehicleControlResponse failureResponse = T0500VehicleControlResponse.create(12345);
        
        // 验证应答信息
        assertEquals(12345, failureResponse.getResponseSerialNumber());
        assertFalse(failureResponse.hasLocationReport());
        assertEquals("控制应答，无位置信息", failureResponse.getResponseDescription());
        
        // 编解码测试
        Buffer encoded = failureResponse.encodeBody();
        assertEquals(2, encoded.length()); // 只有应答流水号
        
        T0500VehicleControlResponse decoded = new T0500VehicleControlResponse();
        decoded.decodeBody(encoded);
        
        assertEquals(failureResponse.getResponseSerialNumber(), decoded.getResponseSerialNumber());
        assertEquals(failureResponse.hasLocationReport(), decoded.hasLocationReport());
        
        logger.info("失败场景测试通过：车辆控制失败应答");
    }

    @Test
    void testBoundaryValues() {
        // 测试边界值
        
        // 最小流水号
        message.setResponseSerialNumber(0);
        assertEquals(0, message.getResponseSerialNumber());
        assertEquals(0L, message.getResponseSerialNumberUnsigned());
        
        // 最大流水号（16位无符号）
        message.setResponseSerialNumber(65535);
        assertEquals(65535, message.getResponseSerialNumber());
        assertEquals(65535L, message.getResponseSerialNumberUnsigned());
        
        // 编解码测试
        Buffer encoded = message.encodeBody();
        T0500VehicleControlResponse decoded = new T0500VehicleControlResponse();
        decoded.decodeBody(encoded);
        assertEquals(65535, decoded.getResponseSerialNumber());
        
        logger.info("边界值测试通过");
    }
}