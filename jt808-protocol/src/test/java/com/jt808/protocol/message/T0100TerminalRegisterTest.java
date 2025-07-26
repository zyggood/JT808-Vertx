package com.jt808.protocol.message;

import io.vertx.core.buffer.Buffer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * T0100终端注册消息测试类
 */
public class T0100TerminalRegisterTest {

    private T0100TerminalRegister message;

    @BeforeEach
    void setUp() {
        message = new T0100TerminalRegister();
    }

    @Test
    void testMessageId() {
        assertEquals(0x0100, message.getMessageId());
    }

    @Test
    void testConstructorWithHeader() {
        JT808Header header = new JT808Header(0x0100, "13800138000", 123);
        T0100TerminalRegister msg = new T0100TerminalRegister(header);
        assertEquals(header, msg.getHeader());
        assertEquals(0x0100, msg.getMessageId());
    }

    @Test
    void testSettersAndGetters() {
        message.setProvinceId(11); // 北京
        message.setCityId(100); // 北京市
        message.setManufacturerId("ABCDE");
        message.setTerminalModel("MODEL123");
        message.setTerminalId("TERM001");
        message.setPlateColor((byte) 1); // 蓝色
        message.setPlateNumber("京A12345");

        assertEquals(11, message.getProvinceId());
        assertEquals(100, message.getCityId());
        assertEquals("ABCDE", message.getManufacturerId());
        assertEquals("MODEL123", message.getTerminalModel());
        assertEquals("TERM001", message.getTerminalId());
        assertEquals(1, message.getPlateColor());
        assertEquals("京A12345", message.getPlateNumber());
    }

    @Test
    void testEncodeAndDecode() {
        // 设置测试数据
        message.setProvinceId(11); // 北京
        message.setCityId(100); // 北京市
        message.setManufacturerId("ABCDE");
        message.setTerminalModel("MODEL123");
        message.setTerminalId("TERM001");
        message.setPlateColor((byte) 1); // 蓝色
        message.setPlateNumber("京A12345");

        // 编码
        Buffer encoded = message.encodeBody();

        // 验证编码长度：2+2+5+20+7+1+车牌号长度(GBK编码)
        int plateNumberLength;
        try {
            plateNumberLength = "京A12345".getBytes("GBK").length;
        } catch (Exception e) {
            plateNumberLength = "京A12345".getBytes().length;
        }
        int expectedLength = 2 + 2 + 5 + 20 + 7 + 1 + plateNumberLength;
        assertEquals(expectedLength, encoded.length());

        // 解码
        T0100TerminalRegister decoded = new T0100TerminalRegister();
        decoded.decodeBody(encoded);

        // 验证解码结果
        assertEquals(message.getProvinceId(), decoded.getProvinceId());
        assertEquals(message.getCityId(), decoded.getCityId());
        assertEquals(message.getManufacturerId(), decoded.getManufacturerId());
        assertEquals(message.getTerminalModel(), decoded.getTerminalModel());
        assertEquals(message.getTerminalId(), decoded.getTerminalId());
        assertEquals(message.getPlateColor(), decoded.getPlateColor());
        assertEquals(message.getPlateNumber(), decoded.getPlateNumber());
    }

    @Test
    void testEncodeWithNullValues() {
        // 只设置必要字段
        message.setProvinceId(11);
        message.setCityId(100);
        message.setPlateColor((byte) 0); // 未上牌

        // 编码
        Buffer encoded = message.encodeBody();

        // 解码
        T0100TerminalRegister decoded = new T0100TerminalRegister();
        decoded.decodeBody(encoded);

        assertEquals(11, decoded.getProvinceId());
        assertEquals(100, decoded.getCityId());
        assertEquals("", decoded.getManufacturerId());
        assertEquals("", decoded.getTerminalModel());
        assertEquals("", decoded.getTerminalId());
        assertEquals(0, decoded.getPlateColor());
        assertEquals("", decoded.getPlateNumber());
    }

    @Test
    void testFieldLengthLimits() {
        // 测试字段长度限制
        String longManufacturerId = "ABCDEFGHIJKLMNOP"; // 超过5字节
        String longTerminalModel = "VERYLONGMODELNAMETHATEXCEEDSTWENTYBYTES"; // 超过20字节
        String longTerminalId = "VERYLONGTERMINALID"; // 超过7字节

        message.setManufacturerId(longManufacturerId);
        message.setTerminalModel(longTerminalModel);
        message.setTerminalId(longTerminalId);

        Buffer encoded = message.encodeBody();
        T0100TerminalRegister decoded = new T0100TerminalRegister();
        decoded.decodeBody(encoded);

        // 验证字段被截断到正确长度
        assertEquals("ABCDE", decoded.getManufacturerId());
        assertEquals("VERYLONGMODELNAMETHA", decoded.getTerminalModel()); // 20字节截断
        assertEquals("VERYLON", decoded.getTerminalId());
    }

    @Test
    void testPlateColorDescription() {
        message.setPlateColor((byte) 1);
        assertEquals("蓝色", message.getPlateColorDescription());

        message.setPlateColor((byte) 2);
        assertEquals("黄色", message.getPlateColorDescription());

        message.setPlateColor((byte) 3);
        assertEquals("黑色", message.getPlateColorDescription());

        message.setPlateColor((byte) 4);
        assertEquals("白色", message.getPlateColorDescription());

        message.setPlateColor((byte) 9);
        assertEquals("其他", message.getPlateColorDescription());

        message.setPlateColor((byte) 99);
        assertEquals("未知(99)", message.getPlateColorDescription());
    }

    @Test
    void testVinScenario() {
        // 测试车牌颜色为0时的VIN场景
        message.setProvinceId(31); // 上海
        message.setCityId(100);
        message.setManufacturerId("TESLA");
        message.setTerminalModel("MODEL3");
        message.setTerminalId("TES001");
        message.setPlateColor((byte) 0); // 未上牌
        message.setPlateNumber("LRWXB2B41JG123456"); // VIN码

        Buffer encoded = message.encodeBody();
        T0100TerminalRegister decoded = new T0100TerminalRegister();
        decoded.decodeBody(encoded);

        assertEquals(31, decoded.getProvinceId());
        assertEquals(100, decoded.getCityId());
        assertEquals("TESLA", decoded.getManufacturerId());
        assertEquals("MODEL3", decoded.getTerminalModel());
        assertEquals("TES001", decoded.getTerminalId());
        assertEquals(0, decoded.getPlateColor());
        assertEquals("LRWXB2B41JG123456", decoded.getPlateNumber());
    }

    @Test
    void testEmptyPlateNumber() {
        // 测试空车牌号
        message.setProvinceId(11);
        message.setCityId(100);
        message.setManufacturerId("MANU1");
        message.setTerminalModel("MODEL1");
        message.setTerminalId("TERM1");
        message.setPlateColor((byte) 1);
        message.setPlateNumber(""); // 空车牌号

        Buffer encoded = message.encodeBody();
        T0100TerminalRegister decoded = new T0100TerminalRegister();
        decoded.decodeBody(encoded);

        assertEquals("", decoded.getPlateNumber());
    }

    @Test
    void testToString() {
        message.setProvinceId(11);
        message.setCityId(100);
        message.setManufacturerId("ABCDE");
        message.setTerminalModel("MODEL123");
        message.setTerminalId("TERM001");
        message.setPlateColor((byte) 1);
        message.setPlateNumber("京A12345");

        String result = message.toString();
        assertTrue(result.contains("T0100TerminalRegister"));
        assertTrue(result.contains("provinceId=11"));
        assertTrue(result.contains("cityId=100"));
        assertTrue(result.contains("manufacturerId='ABCDE'"));
        assertTrue(result.contains("terminalModel='MODEL123'"));
        assertTrue(result.contains("terminalId='TERM001'"));
        assertTrue(result.contains("plateColor=1"));
        assertTrue(result.contains("plateNumber='京A12345'"));
    }

    @Test
    void testMessageBodyLength() {
        // 测试消息体长度计算
        message.setProvinceId(11);
        message.setCityId(100);
        message.setManufacturerId("ABCDE");
        message.setTerminalModel("MODEL123");
        message.setTerminalId("TERM001");
        message.setPlateColor((byte) 1);
        message.setPlateNumber("京A12345");

        Buffer encoded = message.encodeBody();

        // 验证各字段在编码中的位置和长度
        assertEquals(11, encoded.getUnsignedShort(0)); // 省域ID
        assertEquals(100, encoded.getUnsignedShort(2)); // 市县域ID

        // 制造商ID (5字节)
        byte[] manufacturerBytes = encoded.getBytes(4, 9);
        assertEquals("ABCDE", new String(manufacturerBytes).trim().replace("\0", ""));

        // 终端型号 (20字节)
        byte[] modelBytes = encoded.getBytes(9, 29);
        assertEquals("MODEL123", new String(modelBytes).trim().replace("\0", ""));

        // 终端ID (7字节)
        byte[] terminalIdBytes = encoded.getBytes(29, 36);
        assertEquals("TERM001", new String(terminalIdBytes).trim().replace("\0", ""));

        // 车牌颜色 (1字节)
        assertEquals(1, encoded.getByte(36));

        // 车辆标识 (剩余字节，GBK编码)
        byte[] plateBytes = encoded.getBytes(37, encoded.length());
        String plateNumber;
        try {
            plateNumber = new String(plateBytes, "GBK");
        } catch (Exception e) {
            plateNumber = new String(plateBytes);
        }
        assertEquals("京A12345", plateNumber);
    }
}