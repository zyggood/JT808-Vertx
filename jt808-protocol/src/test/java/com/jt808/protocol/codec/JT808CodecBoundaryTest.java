package com.jt808.protocol.codec;

import com.jt808.protocol.message.*;
import io.vertx.core.buffer.Buffer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDateTime;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JT808编解码边界条件测试
 */
class JT808CodecBoundaryTest {
    
    private JT808Encoder encoder;
    private JT808Decoder decoder;
    
    @BeforeEach
    void setUp() {
        encoder = new JT808Encoder();
        decoder = new JT808Decoder();
    }
    
    @Test
    @DisplayName("测试空消息体编解码")
    void testEmptyMessageBody() {
        // 心跳消息没有消息体
        T0002TerminalHeartbeat heartbeat = new T0002TerminalHeartbeat();
        JT808Header header = new JT808Header(0x0002, "13800138000", 1);
        heartbeat.setHeader(header);
        
        // 编码
        Buffer encoded = encoder.encode(heartbeat);
        assertNotNull(encoded);
        assertTrue(encoded.length() > 0);
        
        // 解码
        try {
            JT808Message decoded = decoder.decode(encoded);
            assertNotNull(decoded);
            assertEquals(0x0002, decoded.getMessageId());
            assertEquals("13800138000", decoded.getHeader().getPhoneNumber());
        } catch (Exception e) {
            fail("解码失败: " + e.getMessage());
        }
    }
    
    @Test
    @DisplayName("测试最大长度手机号")
    void testMaxLengthPhoneNumber() {
        // 使用12位手机号（最大长度）
        String maxPhoneNumber = "138001380001";
        
        T0002TerminalHeartbeat heartbeat = new T0002TerminalHeartbeat();
        JT808Header header = new JT808Header(0x0002, maxPhoneNumber, 1);
        heartbeat.setHeader(header);
        
        // 编码解码测试
        Buffer encoded = encoder.encode(heartbeat);
        try {
            JT808Message decoded = decoder.decode(encoded);
            assertEquals(maxPhoneNumber, decoded.getHeader().getPhoneNumber());
        } catch (Exception e) {
            fail("解码失败: " + e.getMessage());
        }
    }
    
    @Test
    @DisplayName("测试最小长度手机号")
    void testMinLengthPhoneNumber() {
        // 使用较短的手机号
        String minPhoneNumber = "138";
        
        T0002TerminalHeartbeat heartbeat = new T0002TerminalHeartbeat();
        JT808Header header = new JT808Header(0x0002, minPhoneNumber, 1);
        heartbeat.setHeader(header);
        
        // 编码解码测试
        Buffer encoded = encoder.encode(heartbeat);
        try {
            JT808Message decoded = decoder.decode(encoded);
            assertEquals(minPhoneNumber, decoded.getHeader().getPhoneNumber());
        } catch (Exception e) {
            fail("解码失败: " + e.getMessage());
        }
    }
    
    @Test
    @DisplayName("测试最大流水号")
    void testMaxSerialNumber() {
        int maxSerial = 0xFFFF; // 16位最大值
        
        T0002TerminalHeartbeat heartbeat = new T0002TerminalHeartbeat();
        JT808Header header = new JT808Header(0x0002, "13800138000", maxSerial);
        heartbeat.setHeader(header);
        
        Buffer encoded = encoder.encode(heartbeat);
        try {
            JT808Message decoded = decoder.decode(encoded);
            assertEquals(maxSerial, decoded.getHeader().getSerialNumber());
        } catch (Exception e) {
            fail("解码失败: " + e.getMessage());
        }
    }
    
    @Test
    @DisplayName("测试零流水号")
    void testZeroSerialNumber() {
        T0002TerminalHeartbeat heartbeat = new T0002TerminalHeartbeat();
        JT808Header header = new JT808Header(0x0002, "13800138000", 0);
        heartbeat.setHeader(header);
        
        Buffer encoded = encoder.encode(heartbeat);
        try {
            JT808Message decoded = decoder.decode(encoded);
            assertEquals(0, decoded.getHeader().getSerialNumber());
        } catch (Exception e) {
            fail("解码失败: " + e.getMessage());
        }
    }
    
    @Test
    @DisplayName("测试最长车牌号")
    void testMaxLengthPlateNumber() {
        T0100TerminalRegister register = new T0100TerminalRegister();
        register.setProvinceId(31);
        register.setCityId(1);
        register.setManufacturerId("MANU");
        register.setTerminalModel("MODEL");
        register.setTerminalId("TERMINAL");
        register.setPlateColor((byte) 1);
        
        // 测试较长的车牌号
        String longPlateNumber = "沪A123456789";
        register.setPlateNumber(longPlateNumber);
        
        JT808Header header = new JT808Header(0x0100, "13800138000", 1);
        register.setHeader(header);
        
        Buffer encoded = encoder.encode(register);
        try {
            JT808Message decoded = decoder.decode(encoded);
            assertTrue(decoded instanceof T0100TerminalRegister);
            T0100TerminalRegister decodedRegister = (T0100TerminalRegister) decoded;
            assertEquals(longPlateNumber, decodedRegister.getPlateNumber());
        } catch (Exception e) {
            fail("解码失败: " + e.getMessage());
        }
    }
    
    @Test
    @DisplayName("测试空车牌号")
    void testEmptyPlateNumber() {
        T0100TerminalRegister register = new T0100TerminalRegister();
        register.setProvinceId(31);
        register.setCityId(1);
        register.setManufacturerId("MANU");
        register.setTerminalModel("MODEL");
        register.setTerminalId("TERMINAL");
        register.setPlateColor((byte) 1);
        register.setPlateNumber(""); // 空车牌号
        
        JT808Header header = new JT808Header(0x0100, "13800138000", 1);
        register.setHeader(header);
        
        Buffer encoded = encoder.encode(register);
        try {
            JT808Message decoded = decoder.decode(encoded);
            assertTrue(decoded instanceof T0100TerminalRegister);
            T0100TerminalRegister decodedRegister = (T0100TerminalRegister) decoded;
            assertEquals("", decodedRegister.getPlateNumber());
        } catch (Exception e) {
            fail("解码失败: " + e.getMessage());
        }
    }
    
    @ParameterizedTest
    @ValueSource(bytes = {0, 1, 2, 9, (byte) 255})
    @DisplayName("测试各种车牌颜色")
    void testVariousPlateColors(byte plateColor) {
        T0100TerminalRegister register = new T0100TerminalRegister();
        register.setProvinceId(31);
        register.setCityId(1);
        register.setManufacturerId("MANU");
        register.setTerminalModel("MODEL");
        register.setTerminalId("TERMINAL");
        register.setPlateColor(plateColor);
        register.setPlateNumber("沪A12345");
        
        JT808Header header = new JT808Header(0x0100, "13800138000", 1);
        register.setHeader(header);
        
        Buffer encoded = encoder.encode(register);
        try {
            JT808Message decoded = decoder.decode(encoded);
            assertTrue(decoded instanceof T0100TerminalRegister);
            T0100TerminalRegister decodedRegister = (T0100TerminalRegister) decoded;
            assertEquals(plateColor, decodedRegister.getPlateColor());
        } catch (Exception e) {
            fail("解码失败: " + e.getMessage());
        }
    }
    
    @Test
    @DisplayName("测试极限坐标值")
    void testExtremeLongitudeLatitude() {
        T0200LocationReport location = new T0200LocationReport();
        
        // 测试极限坐标值
        location.setLatitudeDegrees(90.0);    // 北极
        location.setLongitudeDegrees(180.0);  // 国际日期变更线
        location.setAltitude(8848);           // 珠峰高度
        location.setSpeedKmh(999.9);          // 极高速度
        location.setDirection(359);           // 最大方向角
        location.setDateTime(LocalDateTime.now());
        
        JT808Header header = new JT808Header(0x0200, "13800138000", 1);
        location.setHeader(header);
        
        Buffer encoded = encoder.encode(location);
        try {
            JT808Message decoded = decoder.decode(encoded);
            assertTrue(decoded instanceof T0200LocationReport);
            T0200LocationReport decodedLocation = (T0200LocationReport) decoded;
            
            // 验证坐标精度（允许小的误差）
            assertEquals(90.0, decodedLocation.getLatitudeDegrees(), 0.000001);
            assertEquals(180.0, decodedLocation.getLongitudeDegrees(), 0.000001);
            assertEquals(8848, decodedLocation.getAltitude());
            assertEquals(999.9, decodedLocation.getSpeedKmh(), 0.1);
            assertEquals(359, decodedLocation.getDirection());
        } catch (Exception e) {
            fail("解码失败: " + e.getMessage());
        }
    }
    
    @Test
    @DisplayName("测试零坐标值")
    void testZeroCoordinates() {
        T0200LocationReport location = new T0200LocationReport();
        
        // 测试零坐标（几内亚湾）
        location.setLatitudeDegrees(0.0);
        location.setLongitudeDegrees(0.0);
        location.setAltitude(0);
        location.setSpeedKmh(0.0);
        location.setDirection(0);
        location.setDateTime(LocalDateTime.now());
        
        JT808Header header = new JT808Header(0x0200, "13800138000", 1);
        location.setHeader(header);
        
        Buffer encoded = encoder.encode(location);
        try {
            JT808Message decoded = decoder.decode(encoded);
            assertTrue(decoded instanceof T0200LocationReport);
            T0200LocationReport decodedLocation = (T0200LocationReport) decoded;
            
            assertEquals(0.0, decodedLocation.getLatitudeDegrees());
            assertEquals(0.0, decodedLocation.getLongitudeDegrees());
            assertEquals(0, decodedLocation.getAltitude());
            assertEquals(0.0, decodedLocation.getSpeedKmh());
            assertEquals(0, decodedLocation.getDirection());
        } catch (Exception e) {
            fail("解码失败: " + e.getMessage());
        }
    }
    
    @Test
    @DisplayName("测试最长鉴权码")
    void testMaxLengthAuthCode() {
        // 创建一个很长的鉴权码
        StringBuilder longAuthCode = new StringBuilder();
        for (int i = 0; i < 100; i++) {
            longAuthCode.append("AUTH");
        }
        
        T0102TerminalAuth auth = new T0102TerminalAuth(longAuthCode.toString());
        JT808Header header = new JT808Header(0x0102, "13800138000", 1);
        auth.setHeader(header);
        
        Buffer encoded = encoder.encode(auth);
        try {
            JT808Message decoded = decoder.decode(encoded);
            assertTrue(decoded instanceof T0102TerminalAuth);
            T0102TerminalAuth decodedAuth = (T0102TerminalAuth) decoded;
            assertEquals(longAuthCode.toString(), decodedAuth.getAuthCode());
        } catch (Exception e) {
            fail("解码失败: " + e.getMessage());
        }
    }
    
    @Test
    @DisplayName("测试包含特殊字符的鉴权码")
    void testSpecialCharacterAuthCode() {
        // 包含各种特殊字符的鉴权码
        String specialAuthCode = "AUTH@#$%^&*()_+-={}[]|\\:;\"'<>?,./~`";
        
        T0102TerminalAuth auth = new T0102TerminalAuth(specialAuthCode);
        JT808Header header = new JT808Header(0x0102, "13800138000", 1);
        auth.setHeader(header);
        
        Buffer encoded = encoder.encode(auth);
        try {
            JT808Message decoded = decoder.decode(encoded);
            assertTrue(decoded instanceof T0102TerminalAuth);
            T0102TerminalAuth decodedAuth = (T0102TerminalAuth) decoded;
            assertEquals(specialAuthCode, decodedAuth.getAuthCode());
        } catch (Exception e) {
            fail("解码失败: " + e.getMessage());
        }
    }
    
    @Test
    @DisplayName("测试所有报警标志位")
    void testAllAlarmFlags() {
        T0200LocationReport location = new T0200LocationReport();
        
        // 设置所有报警标志位
        int allAlarms = 0xFFFFFFFF;
        location.setAlarmFlag(allAlarms);
        location.setStatusFlag(0xFFFFFFFF);
        location.setLatitudeDegrees(31.230416);
        location.setLongitudeDegrees(121.473701);
        location.setDateTime(LocalDateTime.now());
        
        JT808Header header = new JT808Header(0x0200, "13800138000", 1);
        location.setHeader(header);
        
        Buffer encoded = encoder.encode(location);
        try {
            JT808Message decoded = decoder.decode(encoded);
            
            assertTrue(decoded instanceof T0200LocationReport);
            T0200LocationReport decodedLocation = (T0200LocationReport) decoded;
            assertEquals(allAlarms, decodedLocation.getAlarmFlag());
            assertEquals(0xFFFFFFFF, decodedLocation.getStatusFlag());
        } catch (Exception e) {
            fail("解码失败: " + e.getMessage());
        }
    }
    
    @Test
    @DisplayName("测试消息体长度边界")
    void testMessageBodyLengthBoundary() {
        // 测试接近最大消息体长度的情况
        T0100TerminalRegister register = new T0100TerminalRegister();
        register.setProvinceId(31);
        register.setCityId(1);
        
        // 创建很长的字符串字段
        StringBuilder longString = new StringBuilder();
        for (int i = 0; i < 50; i++) {
            longString.append("MANUFACTURER");
        }
        
        register.setManufacturerId(longString.toString());
        register.setTerminalModel(longString.toString());
        register.setTerminalId(longString.toString());
        register.setPlateColor((byte) 1);
        register.setPlateNumber("沪A12345");
        
        JT808Header header = new JT808Header(0x0100, "13800138000", 1);
        register.setHeader(header);
        
        // 应该能够正常编解码
        assertDoesNotThrow(() -> {
            Buffer encoded = encoder.encode(register);
            JT808Message decoded = decoder.decode(encoded);
            assertNotNull(decoded);
        });
    }
    
    @Test
    @DisplayName("测试转义字符处理")
    void testEscapeCharacterHandling() {
        // 创建包含需要转义字符的消息
        T0102TerminalAuth auth = new T0102TerminalAuth("AUTH\u007E\u007D"); // 包含0x7E和0x7D
        JT808Header header = new JT808Header(0x0102, "13800138000", 1);
        auth.setHeader(header);
        
        Buffer encoded = encoder.encode(auth);
        
        // 检查编码后的数据是否正确处理了转义
        byte[] encodedBytes = encoded.getBytes();
        boolean hasEscapeSequence = false;
        for (int i = 0; i < encodedBytes.length - 1; i++) {
            if (encodedBytes[i] == 0x7D && 
                (encodedBytes[i + 1] == 0x01 || encodedBytes[i + 1] == 0x02)) {
                hasEscapeSequence = true;
                break;
            }
        }
        
        assertTrue(hasEscapeSequence, "应该包含转义序列");
        
        // 解码应该恢复原始数据
        try {
            JT808Message decoded = decoder.decode(encoded);
            assertTrue(decoded instanceof T0102TerminalAuth);
            T0102TerminalAuth decodedAuth = (T0102TerminalAuth) decoded;
            assertEquals("AUTH\u007E\u007D", decodedAuth.getAuthCode());
        } catch (Exception e) {
            fail("解码失败: " + e.getMessage());
        }
    }
    
    @Test
    @DisplayName("测试校验码计算")
    void testChecksumCalculation() {
        T0002TerminalHeartbeat heartbeat = new T0002TerminalHeartbeat();
        JT808Header header = new JT808Header(0x0002, "13800138000", 1);
        heartbeat.setHeader(header);
        
        Buffer encoded = encoder.encode(heartbeat);
        try {
            byte[] bytes = encoded.getBytes();
            
            // 验证消息格式：0x7E + 数据 + 校验码 + 0x7E
            assertEquals(0x7E, bytes[0]); // 起始标识
            assertEquals(0x7E, bytes[bytes.length - 1]); // 结束标识
            
            // 手动计算校验码
            byte calculatedChecksum = 0;
            for (int i = 1; i < bytes.length - 2; i++) { // 排除起始、结束标识和校验码本身
                calculatedChecksum ^= bytes[i];
            }
            
            byte messageChecksum = bytes[bytes.length - 2];
            assertEquals(calculatedChecksum, messageChecksum, "校验码计算错误");
        } catch (Exception e) {
            fail("解码失败: " + e.getMessage());
        }
    }
    
    @Test
    @DisplayName("测试分包消息处理")
    void testSubpackageMessage() {
        // 创建一个需要分包的大消息
        T0100TerminalRegister register = new T0100TerminalRegister();
        register.setProvinceId(31);
        register.setCityId(1);
        
        // 创建超长字符串以触发分包
        StringBuilder veryLongString = new StringBuilder();
        for (int i = 0; i < 200; i++) {
            veryLongString.append("VERY_LONG_MANUFACTURER_ID_");
        }
        
        register.setManufacturerId(veryLongString.toString());
        register.setTerminalModel("MODEL");
        register.setTerminalId("TERMINAL");
        register.setPlateColor((byte) 1);
        register.setPlateNumber("沪A12345");
        
        JT808Header header = new JT808Header(0x0100, "13800138000", 1);
        header.setSubpackage(true);
        header.setPackageInfo(new JT808Header.PackageInfo(2, 1));
        register.setHeader(header);
        
        // 应该能够正常编码
        assertDoesNotThrow(() -> {
            Buffer encoded = encoder.encode(register);
            assertNotNull(encoded);
            assertTrue(encoded.length() > 0);
        });
    }
}