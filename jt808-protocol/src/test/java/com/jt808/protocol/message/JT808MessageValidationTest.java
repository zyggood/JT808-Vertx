package com.jt808.protocol.message;

import com.jt808.protocol.codec.JT808Decoder;
import com.jt808.protocol.codec.JT808Encoder;
import com.jt808.common.util.ByteUtils;
import io.vertx.core.buffer.Buffer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JT808消息验证测试 - 包含边界条件、异常情况和数据验证
 */
class JT808MessageValidationTest {
    
    private JT808Decoder decoder;
    private JT808Encoder encoder;
    
    @BeforeEach
    void setUp() {
        decoder = new JT808Decoder();
        encoder = new JT808Encoder();
    }
    
    @Test
    @DisplayName("测试终端通用应答消息体长度验证")
    void testTerminalCommonResponseBodyLengthValidation() {
        T0001TerminalCommonResponse response = new T0001TerminalCommonResponse();
        JT808Header header = new JT808Header(0x0001, "13800138000", 1);
        response.setHeader(header);
        
        // 测试空消息体
        Buffer emptyBody = Buffer.buffer();
        assertThrows(IllegalArgumentException.class, () -> {
            response.decodeBody(emptyBody);
        }, "空消息体应抛出异常");
        
        // 测试长度不足的消息体
        Buffer shortBody = Buffer.buffer();
        shortBody.appendUnsignedShort(123); // 只有2字节
        assertThrows(IllegalArgumentException.class, () -> {
            response.decodeBody(shortBody);
        }, "长度不足的消息体应抛出异常");
        
        // 测试正确长度的消息体
        Buffer validBody = Buffer.buffer();
        validBody.appendUnsignedShort(123); // 应答流水号
        validBody.appendUnsignedShort(0x8001); // 应答ID
        validBody.appendByte((byte) 0x00); // 结果
        assertDoesNotThrow(() -> {
            response.decodeBody(validBody);
        }, "正确长度的消息体不应抛出异常");
    }
    
    @ParameterizedTest
    @ValueSource(bytes = {0x00, 0x01, 0x02, 0x03, 0x04})
    @DisplayName("测试终端通用应答不同结果码")
    void testTerminalCommonResponseResults(byte result) {
        T0001TerminalCommonResponse response = new T0001TerminalCommonResponse(123, 0x8001, result);
        JT808Header header = new JT808Header(0x0001, "13800138000", 1);
        response.setHeader(header);
        
        // 编码解码测试
        Buffer encoded = response.encodeBody();
        T0001TerminalCommonResponse decoded = new T0001TerminalCommonResponse();
        decoded.setHeader(header);
        decoded.decodeBody(encoded);
        
        assertEquals(result, decoded.getResult());
        assertNotNull(decoded.getResultDescription());
        
        // 验证成功状态
        if (result == 0x00) {
            assertTrue(decoded.isSuccess());
        } else {
            assertFalse(decoded.isSuccess());
        }
    }
    
    @Test
    @DisplayName("测试终端注册消息字段边界值")
    void testTerminalRegisterBoundaryValues() {
        T0100TerminalRegister register = new T0100TerminalRegister();
        
        // 测试省域ID边界值
        register.setProvinceId(0); // 最小值
        assertEquals(0, register.getProvinceId());
        
        register.setProvinceId(65535); // 最大值
        assertEquals(65535, register.getProvinceId());
        
        // 测试制造商ID长度
        String longManufacturerId = "A".repeat(10); // 超长制造商ID
        register.setManufacturerId(longManufacturerId);
        assertEquals(longManufacturerId, register.getManufacturerId());
        
        // 测试车牌颜色边界值
        register.setPlateColor((byte) 0);
        assertEquals(0, register.getPlateColor());
        
        register.setPlateColor((byte) 255);
        assertEquals((byte) 255, register.getPlateColor());
    }
    
    @ParameterizedTest
    @CsvSource({
        "1, 蓝色",
        "2, 黄色", 
        "3, 黑色",
        "4, 白色",
        "9, 其他"
    })
    @DisplayName("测试车牌颜色描述")
    void testPlateColorDescription(byte color, String expectedDescription) {
        T0100TerminalRegister register = new T0100TerminalRegister();
        register.setPlateColor(color);
        
        String description = register.getPlateColorDescription();
        assertEquals(expectedDescription, description);
    }
    
    @Test
    @DisplayName("测试位置信息汇报精度验证")
    void testLocationReportPrecision() {
        T0200LocationReport location = new T0200LocationReport();
        
        // 测试经纬度精度
        double testLatitude = 31.123456789; // 高精度纬度
        double testLongitude = 121.987654321; // 高精度经度
        
        location.setLatitudeDegrees(testLatitude);
        location.setLongitudeDegrees(testLongitude);
        
        JT808Header header = new JT808Header(0x0200, "13800138000", 1);
        location.setHeader(header);
        
        // 编码解码测试
        Buffer encoded = location.encodeBody();
        T0200LocationReport decoded = new T0200LocationReport();
        decoded.setHeader(header);
        decoded.decodeBody(encoded);
        
        // 验证精度保持（JT808协议精度为1/1000000度）
        assertEquals(testLatitude, decoded.getLatitudeDegrees(), 0.000001);
        assertEquals(testLongitude, decoded.getLongitudeDegrees(), 0.000001);
    }
    
    @Test
    @DisplayName("测试位置信息汇报报警标志位")
    void testLocationReportAlarmFlags() {
        T0200LocationReport location = new T0200LocationReport();
        
        // 测试多个报警标志位组合
        int alarmFlag = 0x00000001 | 0x00000002 | 0x00000004; // 紧急报警 + 超速报警 + 疲劳驾驶
        location.setAlarmFlag(alarmFlag);
        
        JT808Header header = new JT808Header(0x0200, "13800138000", 1);
        location.setHeader(header);
        
        // 编码解码测试
        Buffer encoded = location.encodeBody();
        T0200LocationReport decoded = new T0200LocationReport();
        decoded.setHeader(header);
        decoded.decodeBody(encoded);
        
        assertEquals(alarmFlag, decoded.getAlarmFlag());
        
        // 验证报警状态检查方法
        assertTrue(decoded.hasEmergencyAlarm());
        assertTrue(decoded.hasSpeedingAlarm());
        assertTrue(decoded.hasFatigueAlarm());
        assertFalse(decoded.hasGNSSAntennaFault());
    }
    
    @Test
    @DisplayName("测试终端鉴权码验证")
    void testTerminalAuthCodeValidation() {
        // 测试空鉴权码
        T0102TerminalAuth authEmpty = new T0102TerminalAuth("");
        assertFalse(authEmpty.isAuthCodeValid());
        
        // 测试null鉴权码
        T0102TerminalAuth authNull = new T0102TerminalAuth();
        authNull.setAuthCode(null);
        assertFalse(authNull.isAuthCodeValid());
        
        // 测试有效鉴权码
        T0102TerminalAuth authValid = new T0102TerminalAuth("AUTH123456");
        // 验证鉴权码不为空
        assertNotNull(authValid.getAuthCode());
        assertFalse(authValid.getAuthCode().isEmpty());
        
        // 测试超长鉴权码
        String longAuthCode = "A".repeat(100);
        T0102TerminalAuth authLong = new T0102TerminalAuth(longAuthCode);
        assertTrue(authLong.isAuthCodeValid()); // 只要不为空就认为有效
        assertEquals(longAuthCode, authLong.getAuthCode());
    }
    
    @Test
    @DisplayName("测试2019版本终端鉴权消息")
    void testTerminalAuth2019Version() {
        // 测试完整的2019版本鉴权消息
        T0102TerminalAuth auth = new T0102TerminalAuth("AUTH123", "123456789012345", "V2.1.0");
        JT808Header header = new JT808Header(0x0102, "13800138000", 1);
        auth.setHeader(header);
        
        assertTrue(auth.is2019Version());
        assertEquals("123456789012345", auth.getImei());
        assertEquals("V2.1.0", auth.getSoftwareVersion());
        
        // 编码解码测试
        Buffer encoded = auth.encodeBody();
        T0102TerminalAuth decoded = new T0102TerminalAuth();
        decoded.setHeader(header);
        decoded.decodeBody(encoded);
        
        assertTrue(decoded.is2019Version());
        assertEquals("AUTH123", decoded.getAuthCode());
        assertEquals("123456789012345", decoded.getImei());
        assertEquals("V2.1.0", decoded.getSoftwareVersion());
    }
    
    @Test
    @DisplayName("测试平台通用应答创建方法")
    void testPlatformCommonResponseCreation() {
        // 测试成功应答创建
        T8001PlatformCommonResponse successResponse = 
            T8001PlatformCommonResponse.createSuccessResponse(123, 0x0200);
        assertTrue(successResponse.isSuccess());
        assertEquals((byte) 0x00, successResponse.getResult());
        
        // 测试失败应答创建
        T8001PlatformCommonResponse failureResponse = 
            T8001PlatformCommonResponse.createFailureResponse(456, 0x0100, (byte) 0x01);
        assertFalse(failureResponse.isSuccess());
        assertEquals((byte) 0x01, failureResponse.getResult());
        assertEquals("失败", failureResponse.getResultDescription());
    }
    
    @Test
    @DisplayName("测试终端注册应答不同结果")
    void testTerminalRegisterResponseResults() {
        // 测试成功注册应答
        T8100TerminalRegisterResponse successResponse = 
            T8100TerminalRegisterResponse.createSuccessResponse(123, "AUTH789");
        assertTrue(successResponse.isSuccess());
        assertEquals("AUTH789", successResponse.getAuthCode());
        
        // 测试失败注册应答
        T8100TerminalRegisterResponse failureResponse = 
            T8100TerminalRegisterResponse.createFailureResponse(456, (byte) 0x01);
        assertFalse(failureResponse.isSuccess());
        assertNull(failureResponse.getAuthCode());
        assertEquals("车辆已被注册", failureResponse.getResultDescription());
        
        // 测试其他失败原因
        T8100TerminalRegisterResponse otherFailure = 
            T8100TerminalRegisterResponse.createFailureResponse(789, (byte) 0x02);
        assertEquals("数据库中无该车辆", otherFailure.getResultDescription());
    }
    
    @Test
    @DisplayName("测试消息编解码一致性")
    void testMessageEncodeDecodeConsistency() {
        // 创建一个复杂的位置汇报消息
        T0200LocationReport original = new T0200LocationReport();
        original.setAlarmFlag(0x12345678);
        original.setStatusFlag(0x87654321);
        original.setLatitudeDegrees(39.908722); // 北京纬度
        original.setLongitudeDegrees(116.397496); // 北京经度
        original.setAltitude(50);
        original.setSpeedKmh(80.5);
        original.setDirection(135);
        original.setDateTime(LocalDateTime.of(2024, 6, 22, 12, 30, 45));
        
        JT808Header header = new JT808Header(0x0200, "13912345678", 999);
        original.setHeader(header);
        
        // 编码
        Buffer encoded = original.encodeBody();
        
        // 解码
        T0200LocationReport decoded = new T0200LocationReport();
        decoded.setHeader(header);
        decoded.decodeBody(encoded);
        
        // 验证所有字段一致性
        assertEquals(original.getAlarmFlag(), decoded.getAlarmFlag());
        assertEquals(original.getStatusFlag(), decoded.getStatusFlag());
        assertEquals(original.getLatitudeDegrees(), decoded.getLatitudeDegrees(), 0.000001);
        assertEquals(original.getLongitudeDegrees(), decoded.getLongitudeDegrees(), 0.000001);
        assertEquals(original.getAltitude(), decoded.getAltitude());
        assertEquals(original.getSpeedKmh(), decoded.getSpeedKmh(), 0.1);
        assertEquals(original.getDirection(), decoded.getDirection());
        assertEquals(original.getDateTime(), decoded.getDateTime());
    }
    
    @Test
    @DisplayName("测试心跳消息的简单性")
    void testHeartbeatSimplicity() {
        T0002TerminalHeartbeat heartbeat = new T0002TerminalHeartbeat();
        JT808Header header = new JT808Header(0x0002, "13800138000", 1);
        heartbeat.setHeader(header);
        
        // 心跳消息体应为空
        Buffer encoded = heartbeat.encodeBody();
        assertEquals(0, encoded.length(), "心跳消息体应为空");
        
        // 解码空消息体不应出错
        T0002TerminalHeartbeat decoded = new T0002TerminalHeartbeat();
        decoded.setHeader(header);
        assertDoesNotThrow(() -> {
            decoded.decodeBody(encoded);
        });
        
        assertEquals(0x0002, decoded.getMessageId());
    }
}