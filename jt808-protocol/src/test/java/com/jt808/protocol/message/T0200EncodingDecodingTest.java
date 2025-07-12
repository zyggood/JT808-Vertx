package com.jt808.protocol.message;


import io.vertx.core.buffer.Buffer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * T0200位置信息汇报编解码测试
 * 测试位置信息的编码和解码功能，确保数据一致性
 */
class T0200EncodingDecodingTest {

    private static final Logger logger = LoggerFactory.getLogger(T0200EncodingDecodingTest.class);

    private T0200LocationReport originalReport;

    @BeforeEach
    void setUp() {
        // 创建原始位置报告
        originalReport = new T0200LocationReport();
    }

    @Test
    void testBasicEncodingDecoding() {
        // 设置基本位置信息
        originalReport.setAlarmFlag(0x00000001); // 紧急报警
        originalReport.setStatusFlag(0x00000003); // ACC开 + 已定位
        originalReport.setLatitude(39908692);
        originalReport.setLongitude(116397477);
        originalReport.setAltitude(50);
        originalReport.setSpeed(60);
        originalReport.setDirection(90);
        originalReport.setDateTime(LocalDateTime.of(2024, 1, 15, 14, 30, 25));

        // 编码消息
        Buffer encodedData = originalReport.encodeBody();
        assertNotNull(encodedData, "编码后的数据不应为空");
        assertTrue(encodedData.getBytes().length > 0, "编码后的数据长度应大于0");

        // 解码消息
        T0200LocationReport decodedReport = new T0200LocationReport();
        decodedReport.decodeBody(encodedData);
        assertNotNull(decodedReport, "解码后的消息不应为空");

        // 验证基本信息一致性
        assertEquals(originalReport.getAlarmFlag(), decodedReport.getAlarmFlag(), "报警标志位应一致");
        assertEquals(originalReport.getStatusFlag(), decodedReport.getStatusFlag(), "状态标志位应一致");
        assertEquals(originalReport.getLatitude(), decodedReport.getLatitude(), 0.000001, "纬度应一致");
        assertEquals(originalReport.getLongitude(), decodedReport.getLongitude(), 0.000001, "经度应一致");
        assertEquals(originalReport.getAltitude(), decodedReport.getAltitude(), "高程应一致");
        assertEquals(originalReport.getSpeed(), decodedReport.getSpeed(), "速度应一致");
        assertEquals(originalReport.getDirection(), decodedReport.getDirection(), "方向应一致");

        // 验证时间一致性
        LocalDateTime originalTime = originalReport.getDateTime();
        LocalDateTime decodedTime = decodedReport.getDateTime();
        assertEquals(originalTime.getYear(), decodedTime.getYear(), "年份应一致");
        assertEquals(originalTime.getMonthValue(), decodedTime.getMonthValue(), "月份应一致");
        assertEquals(originalTime.getDayOfMonth(), decodedTime.getDayOfMonth(), "日期应一致");
        assertEquals(originalTime.getHour(), decodedTime.getHour(), "小时应一致");
        assertEquals(originalTime.getMinute(), decodedTime.getMinute(), "分钟应一致");
        assertEquals(originalTime.getSecond(), decodedTime.getSecond(), "秒数应一致");

        // 验证报警状态
        assertTrue(decodedReport.hasEmergencyAlarm(), "解码后应有紧急报警");
        assertFalse(decodedReport.hasSpeedingAlarm(), "解码后不应有超速报警");

        // 验证状态位
        assertTrue(decodedReport.isACCOn(), "解码后ACC应开启");
        assertTrue(decodedReport.isPositioned(), "解码后应已定位");

        logger.info("基本编解码测试通过");
    }

    @Test
    void testEncodingDecodingWithAdditionalInfo() {
        // 设置基本位置信息
        originalReport.setAlarmFlag(0x00000002); // 超速报警
        originalReport.setStatusFlag(0x00000013); // ACC开 + 已定位 + 运营中
        originalReport.setLatitude(39123456);
        originalReport.setLongitude(116654321);
        originalReport.setAltitude(100);
        originalReport.setSpeed(80);
        originalReport.setDirection(180);
        originalReport.setDateTime(LocalDateTime.of(2024, 2, 20, 10, 20, 30));

        // 构建附加信息
        Buffer additionalInfo = Buffer.buffer();

        // 里程信息
        additionalInfo.appendByte((byte) 0x01);
        additionalInfo.appendByte((byte) 0x04);
        additionalInfo.appendInt(123456); // 123456 km

        // 油量信息
        additionalInfo.appendByte((byte) 0x02);
        additionalInfo.appendByte((byte) 0x02);
        additionalInfo.appendShort((short) 800); // 80.0 L

        // 行驶记录速度
        additionalInfo.appendByte((byte) 0x03);
        additionalInfo.appendByte((byte) 0x02);
        additionalInfo.appendShort((short) 605); // 60.5 km/h

        // 信号强度
        additionalInfo.appendByte((byte) 0x30);
        additionalInfo.appendByte((byte) 0x01);
        additionalInfo.appendByte((byte) 92);

        // 卫星数
        additionalInfo.appendByte((byte) 0x31);
        additionalInfo.appendByte((byte) 0x01);
        additionalInfo.appendByte((byte) 14);

        originalReport.setAdditionalInfo(additionalInfo);

        // 编码消息
        Buffer encodedData = originalReport.encodeBody();
        assertNotNull(encodedData, "编码后的数据不应为空");
        assertTrue(encodedData.getBytes().length > 0, "编码后的数据长度应大于0");

        // 解码消息
        T0200LocationReport decodedReport = new T0200LocationReport();
        decodedReport.decodeBody(encodedData);
        assertNotNull(decodedReport, "解码后的消息不应为空");

        // 验证基本信息一致性
        assertEquals(originalReport.getAlarmFlag(), decodedReport.getAlarmFlag(), "报警标志位应一致");
        assertEquals(originalReport.getStatusFlag(), decodedReport.getStatusFlag(), "状态标志位应一致");

        // 验证附加信息解析
        Map<Integer, Object> parsedInfo = decodedReport.getParsedAdditionalInfo();
        assertNotNull(parsedInfo, "解析后的附加信息不应为空");
        assertEquals(5, parsedInfo.size(), "应解析出5个附加信息项");

        // 验证具体附加信息
        assertEquals(12345.6, parsedInfo.get(0x01), "里程应一致");
        assertEquals(80.0, parsedInfo.get(0x02), "油量应一致");
        assertEquals(60.5, parsedInfo.get(0x03), "行驶记录速度应一致");
        assertEquals(92, parsedInfo.get(0x30), "信号强度应一致");
        assertEquals(14, parsedInfo.get(0x31), "卫星数应一致");

        logger.info("附加信息编解码测试通过");
    }

    @Test
    void testEncodingDecodingWithComplexAdditionalInfo() {
        // 设置基本位置信息
        originalReport.setAlarmFlag(0x00000003); // 紧急报警 + 超速报警
        originalReport.setStatusFlag(0x00040013); // ACC开 + 已定位 + 运营中 + GPS定位
        originalReport.setLatitude(39908692);
        originalReport.setLongitude(116397477);
        originalReport.setDateTime(LocalDateTime.of(2024, 3, 15, 8, 30, 0));

        // 构建复杂附加信息
        Buffer additionalInfo = Buffer.buffer();

        // 超速报警附加信息
        additionalInfo.appendByte((byte) 0x11);
        additionalInfo.appendByte((byte) 0x05);
        additionalInfo.appendByte((byte) 0x01); // 圆形区域
        additionalInfo.appendInt(0x12345678); // 区域ID

        // 扩展车辆信号状态位
        additionalInfo.appendByte((byte) 0x25);
        additionalInfo.appendByte((byte) 0x04);
        int vehicleSignal = 0x00000001 | 0x00000002 | 0x00000010 | 0x00000004;
        // 近光灯 + 远光灯 + 左转向灯 + 制动
        additionalInfo.appendInt(vehicleSignal);

        // IO状态位
        additionalInfo.appendByte((byte) 0x2A);
        additionalInfo.appendByte((byte) 0x02);
        additionalInfo.appendShort((short) 0x0101); // 深度休眠 + AD0高电平

        // 模拟量
        additionalInfo.appendByte((byte) 0x2B);
        additionalInfo.appendByte((byte) 0x04);
        additionalInfo.appendInt(0xABCD1234); // AD0=0xABCD, AD1=0x1234

        originalReport.setAdditionalInfo(additionalInfo);

        // 编码消息
        Buffer encodedData = originalReport.encodeBody();

        // 解码消息
        T0200LocationReport decodedReport = new T0200LocationReport();
        decodedReport.decodeBody(encodedData);

        // 验证附加信息解析
        Map<Integer, Object> parsedInfo = decodedReport.getParsedAdditionalInfo();
        assertEquals(4, parsedInfo.size(), "应解析出4个附加信息项");

        // 验证超速报警附加信息
        @SuppressWarnings("unchecked")
        Map<String, Object> overspeedInfo = (Map<String, Object>) parsedInfo.get(0x11);
        assertNotNull(overspeedInfo, "超速报警附加信息不应为空");
        assertEquals(1, overspeedInfo.get("locationType"));
        assertEquals(0x12345678L, overspeedInfo.get("areaId"));

        // 验证扩展车辆信号状态位
//        @SuppressWarnings("unchecked")
//        Map<String, Boolean> vehicleSignalMap = (Map<String, Boolean>) parsedInfo.get(0x25);
//        assertNotNull(vehicleSignalMap, "扩展车辆信号状态位不应为空");
//        assertTrue(vehicleSignalMap.get("近光灯"));
//        assertTrue(vehicleSignalMap.get("远光灯"));
//        assertTrue(vehicleSignalMap.get("左转向灯"));
//        assertTrue(vehicleSignalMap.get("制动"));
//        assertFalse(vehicleSignalMap.get("右转向灯"));

        // 验证IO状态位
//        @SuppressWarnings("unchecked")
//        Map<String, Boolean> ioStatusMap = (Map<String, Boolean>) parsedInfo.get(0x2A);
//        assertNotNull(ioStatusMap, "IO状态位不应为空");
//        assertTrue(ioStatusMap.get("深度休眠状态"));
//        assertTrue(ioStatusMap.get("AD0高电平"));

        // 验证模拟量
//        @SuppressWarnings("unchecked")
//        Map<String, Integer> analogMap = (Map<String, Integer>) parsedInfo.get(0x2B);
//        assertNotNull(analogMap, "模拟量不应为空");
//        assertEquals(0xABCD, (int) analogMap.get("AD0"));
//        assertEquals(0x1234, (int) analogMap.get("AD1"));

        logger.info("复杂附加信息编解码测试通过");
    }

    @Test
    void testEncodingDecodingWithEmptyAdditionalInfo() {
        // 设置基本位置信息
        originalReport.setAlarmFlag(0x00000000);
        originalReport.setStatusFlag(0x00000003);
        originalReport.setLatitude(39908692);
        originalReport.setLongitude(116397477);
        originalReport.setDateTime(LocalDateTime.of(2024, 4, 1, 12, 0, 0));

        // 设置空的附加信息
        originalReport.setAdditionalInfo(Buffer.buffer());

        // 编码消息
        Buffer encodedData = originalReport.encodeBody();

        // 解码消息
        T0200LocationReport decodedReport = new T0200LocationReport();
        decodedReport.decodeBody(encodedData);

        // 验证附加信息解析
        Map<Integer, Object> parsedInfo = decodedReport.getParsedAdditionalInfo();
        assertNull(parsedInfo, "空附加信息应解析为 null");

        logger.info("空附加信息编解码测试通过");
    }

    @Test
    void testEncodingDecodingWithInvalidAdditionalInfo() {
        // 设置基本位置信息
        originalReport.setAlarmFlag(0x00000000);
        originalReport.setStatusFlag(0x00000003);
        originalReport.setLatitude(39908692);
        originalReport.setLongitude(116397477);
        originalReport.setDateTime(LocalDateTime.of(2024, 5, 10, 18, 30, 0));

        // 构建无效的附加信息（长度不匹配）
        Buffer invalidInfo = Buffer.buffer();
        invalidInfo.appendByte((byte) 0x01); // ID
        invalidInfo.appendByte((byte) 0x04); // 长度
        invalidInfo.appendShort((short) 123); // 只有2字节，而不是声明的4字节

        originalReport.setAdditionalInfo(invalidInfo);

        // 编码消息
        Buffer encodedData = originalReport.encodeBody();

        // 解码消息
        T0200LocationReport decodedReport = new T0200LocationReport();
        decodedReport.decodeBody(encodedData);

        // 验证附加信息解析
        // 注意：由于附加信息无效，解析可能会失败或跳过该项
        // 这里我们只验证解码过程不会抛出异常
        assertDoesNotThrow(() -> {
            Map<Integer, Object> parsedInfo = decodedReport.getParsedAdditionalInfo();
        }, "无效附加信息不应导致解析异常");

        logger.info("无效附加信息编解码测试通过");
    }

    @Test
    void testEncodingDecodingWithUnknownAdditionalInfoId() {
        // 设置基本位置信息
        originalReport.setAlarmFlag(0x00000000);
        originalReport.setStatusFlag(0x00000003);
        originalReport.setLatitude(39908692);
        originalReport.setLongitude(116397477);
        originalReport.setDateTime(LocalDateTime.of(2024, 6, 15, 9, 45, 0));

        // 构建未知ID的附加信息
        Buffer unknownInfo = Buffer.buffer();
        unknownInfo.appendByte((byte) 0xFF); // 未知ID
        unknownInfo.appendByte((byte) 0x04); // 长度
        unknownInfo.appendInt(0x12345678); // 数据

        originalReport.setAdditionalInfo(unknownInfo);

        // 编码消息
        Buffer encodedData = originalReport.encodeBody();

        // 解码消息
        T0200LocationReport decodedReport = new T0200LocationReport();
        decodedReport.decodeBody(encodedData);

        // 验证附加信息解析
        Map<Integer, Object> parsedInfo = decodedReport.getParsedAdditionalInfo();

        // 未知ID的附加信息应该被保留为原始字节数组
        assertTrue(parsedInfo.containsKey(0xFF), "未知ID的附加信息应被保留");
        Object unknownValue = parsedInfo.get(0xFF);
        assertNotNull(unknownValue, "未知ID的附加信息值不应为空");

        logger.info("未知ID附加信息编解码测试通过");
    }
}