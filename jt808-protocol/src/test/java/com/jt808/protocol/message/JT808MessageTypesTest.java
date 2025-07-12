package com.jt808.protocol.message;

import com.jt808.protocol.codec.JT808Decoder;
import com.jt808.protocol.codec.JT808Encoder;
import io.vertx.core.buffer.Buffer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JT808消息类型测试
 */
class JT808MessageTypesTest {

    private JT808Decoder decoder;
    private JT808Encoder encoder;

    @BeforeEach
    void setUp() {
        decoder = new JT808Decoder();
        encoder = new JT808Encoder();
    }

    @Test
    void testTerminalHeartbeat() {
        // 测试终端心跳消息
        T0002TerminalHeartbeat heartbeat = new T0002TerminalHeartbeat();
        JT808Header header = new JT808Header(0x0002, "13800138000", 1);
        heartbeat.setHeader(header);

        // 编码
        Buffer encoded = heartbeat.encodeBody();
        assertEquals(0, encoded.length(), "心跳消息体应为空");

        // 解码
        T0002TerminalHeartbeat decoded = new T0002TerminalHeartbeat();
        decoded.setHeader(header);
        decoded.decodeBody(encoded);

        assertEquals(0x0002, decoded.getMessageId());
    }

    @Test
    void testTerminalRegister() {
        // 测试终端注册消息
        T0100TerminalRegister register = new T0100TerminalRegister();
        register.setProvinceId(31); // 上海
        register.setCityId(1);
        register.setManufacturerId("MANU1");
        register.setTerminalModel("MODEL001");
        register.setTerminalId("TERM001");
        register.setPlateColor((byte) 1); // 蓝色
        register.setPlateNumber("沪A12345");

        JT808Header header = new JT808Header(0x0100, "13800138000", 1);
        register.setHeader(header);

        // 编码
        Buffer encoded = register.encodeBody();
        assertTrue(encoded.length() > 0, "注册消息体不应为空");

        // 解码
        T0100TerminalRegister decoded = new T0100TerminalRegister();
        decoded.setHeader(header);
        decoded.decodeBody(encoded);

        assertEquals(0x0100, decoded.getMessageId());
        assertEquals(31, decoded.getProvinceId());
        assertEquals(1, decoded.getCityId());
        assertEquals("MANU1", decoded.getManufacturerId());
        assertEquals("MODEL001", decoded.getTerminalModel());
        assertEquals("TERM001", decoded.getTerminalId());
        assertEquals(1, decoded.getPlateColor());
        assertEquals("沪A12345", decoded.getPlateNumber());
    }

    @Test
    void testTerminalAuth() {
        // 测试终端鉴权消息
        T0102TerminalAuth auth = new T0102TerminalAuth("AUTH123456");
        JT808Header header = new JT808Header(0x0102, "13800138000", 1);
        auth.setHeader(header);

        // 编码
        Buffer encoded = auth.encodeBody();
        assertTrue(encoded.length() > 0, "鉴权消息体不应为空");

        // 解码
        T0102TerminalAuth decoded = new T0102TerminalAuth();
        decoded.setHeader(header);
        decoded.decodeBody(encoded);

        assertEquals(0x0102, decoded.getMessageId());
        assertEquals("AUTH123456", decoded.getAuthCode());
        assertTrue(decoded.isAuthCodeValid());
        assertFalse(decoded.is2019Version());
    }

    @Test
    void testTerminalAuth2019() {
        // 测试2019版本终端鉴权消息
        T0102TerminalAuth auth = new T0102TerminalAuth("AUTH123456", "123456789012345", "V1.0.0");
        JT808Header header = new JT808Header(0x0102, "13800138000", 1);
        auth.setHeader(header);

        // 编码
        Buffer encoded = auth.encodeBody();
        assertTrue(encoded.length() > 0, "鉴权消息体不应为空");

        // 解码
        T0102TerminalAuth decoded = new T0102TerminalAuth();
        decoded.setHeader(header);
        decoded.decodeBody(encoded);

        assertEquals(0x0102, decoded.getMessageId());
        assertEquals("AUTH123456", decoded.getAuthCode());
        assertEquals("123456789012345", decoded.getImei());
        assertEquals("V1.0.0", decoded.getSoftwareVersion());
        assertTrue(decoded.isAuthCodeValid());
        assertTrue(decoded.is2019Version());
    }

    @Test
    void testLocationReport() {
        // 测试位置信息汇报消息
        T0200LocationReport location = new T0200LocationReport();
        location.setAlarmFlag(0x00000001); // 紧急报警
        location.setStatusFlag(0x00000002); // ACC开
        location.setLatitudeDegrees(31.230416); // 上海纬度
        location.setLongitudeDegrees(121.473701); // 上海经度
        location.setAltitude(10); // 海拔10米
        location.setSpeedKmh(60.5); // 60.5km/h
        location.setDirection(90); // 正东方向
        location.setDateTime(LocalDateTime.of(2024, 1, 15, 14, 30, 0));

        JT808Header header = new JT808Header(0x0200, "13800138000", 1);
        location.setHeader(header);

        // 编码
        Buffer encoded = location.encodeBody();
        assertTrue(encoded.length() > 0, "位置汇报消息体不应为空");

        // 解码
        T0200LocationReport decoded = new T0200LocationReport();
        decoded.setHeader(header);
        decoded.decodeBody(encoded);

        assertEquals(0x0200, decoded.getMessageId());
        assertEquals(0x00000001, decoded.getAlarmFlag());
        assertEquals(0x00000002, decoded.getStatusFlag());
        assertEquals(31.230416, decoded.getLatitudeDegrees(), 0.000001);
        assertEquals(121.473701, decoded.getLongitudeDegrees(), 0.000001);
        assertEquals(10, decoded.getAltitude());
        assertEquals(60.5, decoded.getSpeedKmh(), 0.1);
        assertEquals(90, decoded.getDirection());
        assertNotNull(decoded.getDateTime());
    }

    @Test
    void testPlatformCommonResponse() {
        // 测试平台通用应答消息
        T8001PlatformCommonResponse response = new T8001PlatformCommonResponse(123, 0x0200, (byte) 0x00);
        JT808Header header = new JT808Header(0x8001, "13800138000", 1);
        response.setHeader(header);

        // 编码
        Buffer encoded = response.encodeBody();
        assertEquals(5, encoded.length(), "平台通用应答消息体长度应为5字节");

        // 解码
        T8001PlatformCommonResponse decoded = new T8001PlatformCommonResponse();
        decoded.setHeader(header);
        decoded.decodeBody(encoded);

        assertEquals(0x8001, decoded.getMessageId());
        assertEquals(123, decoded.getResponseSerialNumber());
        assertEquals(0x0200, decoded.getResponseMessageId());
        assertEquals((byte) 0x00, decoded.getResult());
        assertTrue(decoded.isSuccess());
        assertEquals("成功/确认", decoded.getResultDescription());
    }

    @Test
    void testTerminalRegisterResponse() {
        // 测试终端注册应答消息
        T8100TerminalRegisterResponse response =
                T8100TerminalRegisterResponse.createSuccessResponse(123, "AUTH123456");
        JT808Header header = new JT808Header(0x8100, "13800138000", 1);
        response.setHeader(header);

        // 编码
        Buffer encoded = response.encodeBody();
        assertTrue(encoded.length() > 3, "注册应答消息体长度应大于3字节");

        // 解码
        T8100TerminalRegisterResponse decoded = new T8100TerminalRegisterResponse();
        decoded.setHeader(header);
        decoded.decodeBody(encoded);

        assertEquals(0x8100, decoded.getMessageId());
        assertEquals(123, decoded.getResponseSerialNumber());
        assertEquals((byte) 0x00, decoded.getResult());
        assertEquals("AUTH123456", decoded.getAuthCode());
        assertTrue(decoded.isSuccess());
        assertEquals("成功", decoded.getResultDescription());
    }

    @Test
    void testTerminalCommonResponse() {
        // 测试终端通用应答消息
        T0001TerminalCommonResponse response =
                T0001TerminalCommonResponse.createSuccessResponse(456, 0x8001);
        JT808Header header = new JT808Header(0x0001, "13800138000", 1);
        response.setHeader(header);

        // 编码
        Buffer encoded = response.encodeBody();
        assertEquals(5, encoded.length(), "终端通用应答消息体长度应为5字节");

        // 解码
        T0001TerminalCommonResponse decoded = new T0001TerminalCommonResponse();
        decoded.setHeader(header);
        decoded.decodeBody(encoded);

        assertEquals(0x0001, decoded.getMessageId());
        assertEquals(456, decoded.getResponseSerialNumber());
        assertEquals(0x8001, decoded.getResponseMessageId());
        assertEquals((byte) 0x00, decoded.getResult());
        assertTrue(decoded.isSuccess());
        assertEquals("成功/确认", decoded.getResultDescription());
    }
}