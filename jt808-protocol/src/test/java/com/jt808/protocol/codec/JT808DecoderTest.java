package com.jt808.protocol.codec;

import com.jt808.common.exception.ProtocolException;
import com.jt808.common.util.ByteUtils;
import com.jt808.protocol.message.JT808Header;
import com.jt808.protocol.message.JT808Message;
import com.jt808.protocol.message.T0200LocationReport;
import io.vertx.core.buffer.Buffer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JT808解码器测试
 */
class JT808DecoderTest {
    private static final Logger logger = LoggerFactory.getLogger(JT808DecoderTest.class);
    
    private JT808Decoder decoder;
    
    @BeforeEach
    void setUp() {
        decoder = new JT808Decoder();
    }
    
    @Test
    void testDecodeValidMessage() throws ProtocolException {
        // 构造一个有效的JT808消息
        Buffer messageBuffer = createValidMessage();
        
        JT808Message message;
        try {
            message = decoder.decode(messageBuffer);
        } catch (Exception e) {
            fail("解码失败: " + e.getMessage());
            return;
        }
        
        assertNotNull(message);
        assertNotNull(message.getHeader());
        assertEquals(0x0001, message.getMessageId());
    }
    
    @Test
    void testDecodeNullBuffer() {
        assertThrows(ProtocolException.class, () -> {
            decoder.decode(null);
        });
    }
    
    @Test
    void testDecodeShortBuffer() {
        Buffer shortBuffer = Buffer.buffer(new byte[]{0x7E, 0x01, 0x7E});
        
        assertThrows(ProtocolException.class, () -> {
            decoder.decode(shortBuffer);
        });
    }
    
    @Test
    void testDecodeInvalidStartFlag() {
        Buffer invalidBuffer = Buffer.buffer(new byte[]{0x7F, 0x01, 0x02, 0x03, 0x7E});
        
        assertThrows(ProtocolException.class, () -> {
            decoder.decode(invalidBuffer);
        });
    }
    
    @Test
    void testDecodeInvalidEndFlag() {
        Buffer invalidBuffer = Buffer.buffer(new byte[]{0x7E, 0x01, 0x02, 0x03, 0x7F});
        
        assertThrows(ProtocolException.class, () -> {
            decoder.decode(invalidBuffer);
        });
    }
    
    @Test
    void testDecodeMessageWithEscape() throws ProtocolException {
        // 构造包含转义字符的消息
        Buffer messageBuffer = createMessageWithEscape();
        
        JT808Message message;
        try {
            message = decoder.decode(messageBuffer);
        } catch (Exception e) {
            fail("解码失败: " + e.getMessage());
            return;
        }
        
        assertNotNull(message);
        assertNotNull(message.getHeader());
    }
    
    @Test
    void testDecodeHeaderInsufficientLength() {
        // 构造消息头长度不足的消息
        Buffer buffer = Buffer.buffer();
        buffer.appendByte((byte) 0x7E);
        // 添加少于12字节的数据
        buffer.appendBytes(new byte[]{0x00, 0x01, 0x00, 0x02, 0x01});
        buffer.appendByte((byte) 0x7E);
        
        assertThrows(ProtocolException.class, () -> {
            decoder.decode(buffer);
        });
    }
    
    @Test
    void testDecodeVersion2019Message() throws ProtocolException {
        // 构造2019版本的消息（包含协议版本号）
        Buffer messageBuffer = createVersion2019Message();
        
        JT808Message message;
        try {
            message = decoder.decode(messageBuffer);
        } catch (Exception e) {
            fail("解码失败: " + e.getMessage());
            return;
        }
        
        assertNotNull(message);
        assertNotNull(message.getHeader());
        assertEquals(1, message.getHeader().getProtocolVersion());
    }
    
    @Test
    void testDecodeSubpackageMessage() throws ProtocolException {
        // 构造分包消息
        Buffer messageBuffer = createSubpackageMessage();
        
        JT808Message message;
        try {
            message = decoder.decode(messageBuffer);
        } catch (Exception e) {
            fail("解码失败: " + e.getMessage());
            return;
        }
        
        assertNotNull(message);
        assertNotNull(message.getHeader());
        assertTrue(message.getHeader().isSubpackage());
        assertNotNull(message.getHeader().getPackageInfo());
    }
    
    /**
     * 创建一个有效的JT808消息
     */
    private Buffer createValidMessage() {
        Buffer buffer = Buffer.buffer();
        
        // 标识位
        buffer.appendByte((byte) 0x7E);
        
        // 消息内容（不包含转义）
        Buffer content = Buffer.buffer();
        
        // 消息ID（2字节）
        content.appendUnsignedShort(0x0001);
        
        // 消息体属性（2字节）- 不分包，无加密
        content.appendUnsignedShort(0x0000);
        
        // 终端手机号（6字节BCD）
        byte[] phoneBcd = ByteUtils.toBCD(123456789012L, 6);
        content.appendBytes(phoneBcd);
        
        // 消息流水号（2字节）
        content.appendUnsignedShort(1);
        
        // 消息体（空）
        
        // 校验码（1字节）
        byte checksum = ByteUtils.calculateChecksum(content.getBytes(), 0, content.length());
        content.appendByte(checksum);
        
        // 转义处理
        Buffer escaped = ByteUtils.escape(content);
        buffer.appendBuffer(escaped);
        
        // 结束标识位
        buffer.appendByte((byte) 0x7E);
        
        return buffer;
    }
    
    /**
     * 创建包含转义字符的消息
     */
    private Buffer createMessageWithEscape() {
        Buffer buffer = Buffer.buffer();
        
        // 标识位
        buffer.appendByte((byte) 0x7E);
        
        // 消息内容（包含需要转义的字符）
        Buffer content = Buffer.buffer();
        
        // 消息ID
        content.appendUnsignedShort(0x0001);
        
        // 消息体属性
        content.appendUnsignedShort(0x0005); // 消息体长度为5
        
        // 终端手机号
        byte[] phoneBcd = ByteUtils.toBCD(123456789012L, 6);
        content.appendBytes(phoneBcd);
        
        // 消息流水号
        content.appendUnsignedShort(1);
        
        // 消息体（终端通用应答：应答流水号2字节 + 应答ID2字节 + 结果1字节）
        content.appendUnsignedShort(1); // 应答流水号
        content.appendUnsignedShort(0x8001); // 应答ID
        content.appendByte((byte) 0x00); // 结果：成功
        
        // 校验码
        byte checksum = ByteUtils.calculateChecksum(content.getBytes(), 0, content.length());
        content.appendByte(checksum);
        
        // 转义处理
        Buffer escaped = ByteUtils.escape(content);
        buffer.appendBuffer(escaped);
        
        // 结束标识位
        buffer.appendByte((byte) 0x7E);
        
        return buffer;
    }
    
    /**
     * 创建2019版本的消息
     */
    private Buffer createVersion2019Message() {
        Buffer buffer = Buffer.buffer();
        
        // 标识位
        buffer.appendByte((byte) 0x7E);
        
        // 消息内容
        Buffer content = Buffer.buffer();
        
        // 消息ID
        content.appendUnsignedShort(0x0001);
        
        // 消息体属性（设置版本标识位）
        content.appendUnsignedShort(0x4000); // 设置版本标识位
        
        // 协议版本号（2019版本）
        content.appendByte((byte) 0x01);
        
        // 终端手机号
        byte[] phoneBcd = ByteUtils.toBCD(123456789012L, 6);
        content.appendBytes(phoneBcd);
        
        // 消息流水号
        content.appendUnsignedShort(1);
        
        // 校验码
        byte checksum = ByteUtils.calculateChecksum(content.getBytes(), 0, content.length());
        content.appendByte(checksum);
        
        // 转义处理
        Buffer escaped = ByteUtils.escape(content);
        buffer.appendBuffer(escaped);
        
        // 结束标识位
        buffer.appendByte((byte) 0x7E);
        
        return buffer;
    }
    
    /**
     * 创建分包消息
     */
    private Buffer createSubpackageMessage() {
        Buffer buffer = Buffer.buffer();
        
        // 标识位
        buffer.appendByte((byte) 0x7E);
        
        // 消息内容
        Buffer content = Buffer.buffer();
        
        // 消息ID
        content.appendUnsignedShort(0x0001);
        
        // 消息体属性（设置分包标识位）
        content.appendUnsignedShort(0x2000); // 设置分包标识位
        
        // 终端手机号
        byte[] phoneBcd = ByteUtils.toBCD(123456789012L, 6);
        content.appendBytes(phoneBcd);
        
        // 消息流水号
        content.appendUnsignedShort(1);
        
        // 分包信息
        content.appendUnsignedShort(3); // 总包数
        content.appendUnsignedShort(1); // 包序号
        
        // 校验码
        byte checksum = ByteUtils.calculateChecksum(content.getBytes(), 0, content.length());
        content.appendByte(checksum);
        
        // 转义处理
        Buffer escaped = ByteUtils.escape(content);
        buffer.appendBuffer(escaped);
        
        // 结束标识位
        buffer.appendByte((byte) 0x7E);
        
        return buffer;
    }

    @Test
    void testDecodeComplexMessage() throws ProtocolException {
        String hex = "020000d40123456789017fff000004000000080006eeb6ad02633df7013800030063200707192359642f000000400101020a0a02010a1e00640001b2070003640e200707192359000100000061646173200827111111010101652f000000410202020a0000000a1e00c8000516150006c81c20070719235900020000000064736d200827111111020202662900000042031e012c00087a23000a2c2a200707192359000300000074706d732008271111110303030067290000004304041e0190000bde31000d90382007071923590004000000006273642008271111110404049d";

        // 将十六进制字符串转换为字节数组
        byte[] bytes = ByteUtils.hexToBytes(hex);

        // 创建包含起始和结束标识位的完整消息
        Buffer buffer = Buffer.buffer();
        buffer.appendByte((byte) 0x7E); // 起始标识位
        buffer.appendBytes(bytes);
        buffer.appendByte((byte) 0x7E); // 结束标识位

        // 解码消息
        JT808Message message;
        try {
            message = decoder.decode(buffer);
        } catch (Exception e) {
            fail("解码失败: " + e.getMessage());
            return;
        }

        // 验证解码结果
        assertNotNull(message, "解码后的消息不应为null");
        assertNotNull(message.getHeader(), "消息头不应为null");

        // 验证消息ID (0x0200 = 位置信息汇报)
        assertEquals(0x0200, message.getMessageId(), "消息ID应为0x0200");

        // 验证终端手机号
        assertEquals("12345678901", message.getHeader().getPhoneNumber(), "终端手机号应为12345678901");

        // 验证这是一个T0200LocationReport消息
        assertTrue(message instanceof T0200LocationReport, "消息应为T0200LocationReport类型");
        
        T0200LocationReport locationReport = (T0200LocationReport) message;
        
        // 验证位置信息字段
        logger.info("消息解码成功:");
        logger.info("消息ID: 0x{}", Integer.toHexString(message.getMessageId()).toUpperCase());
        logger.info("终端手机号: {}", message.getHeader().getPhoneNumber());
        logger.info("消息体长度: {}", message.getHeader().getBodyLength());
        logger.info("报警标志位: 0x{}", Integer.toHexString(locationReport.getAlarmFlag()).toUpperCase());
        logger.info("状态位: 0x{}", Integer.toHexString(locationReport.getStatusFlag()).toUpperCase());
        logger.info("纬度: {}°", locationReport.getLatitudeDegrees());
        logger.info("经度: {}°", locationReport.getLongitudeDegrees());
        logger.info("高程: {}m", locationReport.getAltitude());
        logger.info("速度: {}km/h", locationReport.getSpeedKmh());
        logger.info("方向: {}°", locationReport.getDirection());
        logger.info("时间: {}", locationReport.getDateTime());
        
        // 验证基本字段不为空或默认值
        assertNotNull(locationReport.getDateTime(), "时间不应为null");
        assertTrue(locationReport.getLatitude() != 0 || locationReport.getLongitude() != 0, "经纬度不应都为0");
        
        logger.info("message header: {}", message.getHeader().toString());
        logger.info("完整位置报告: {}", locationReport.toString());
    }

    @Test
    public void testEncodeDecodeLocationReport() {
        try {
            // 创建位置报告消息
            T0200LocationReport originalReport = new T0200LocationReport();
            
            // 设置消息头
            JT808Header header = new JT808Header();
            header.setMessageId(0x0200);
            header.setPhoneNumber("123456789012");
            header.setSerialNumber(12345);
            originalReport.setHeader(header);
            
            // 设置位置信息
            originalReport.setAlarmFlag(0x00000001); // 紧急报警
            originalReport.setStatusFlag(0x00000002); // 已定位
            originalReport.setLatitude(39906000); // 北京纬度
            originalReport.setLongitude(116397000); // 北京经度
            originalReport.setAltitude(100);
            originalReport.setSpeed(600); // 60km/h
            originalReport.setDirection(180);
            originalReport.setDateTime(java.time.LocalDateTime.of(2023, 3, 15, 14, 30, 0)); // 2023-03-15 14:30:00
            
            logger.info("原始报告: {}", originalReport);
            
            // 编码
            JT808Encoder encoder = new JT808Encoder();
            Buffer encoded = encoder.encode(originalReport);
            logger.info("编码后: {}", encoded.toString());
            
            // 解码
            JT808Decoder decoder = new JT808Decoder();
            JT808Message decoded = decoder.decode(encoded);
            logger.info("解码报告: {}", decoded);
            
            // 验证解码成功
            assertNotNull(decoded);
            assertTrue(decoded instanceof T0200LocationReport);
            
        } catch (Exception e) {
            e.printStackTrace();
            fail("测试失败: " + e.getMessage());
        }
    }
    
    @Test
    public void testAlarmFlagParsing() {
        // 测试报警标志位解析功能
        T0200LocationReport report = new T0200LocationReport();
        
        // 测试单个报警位
        report.setAlarmFlag(0x00000001); // 紧急报警
        assertTrue(report.hasEmergencyAlarm(), "应该检测到紧急报警");
        assertFalse(report.hasSpeedingAlarm(), "不应该检测到超速报警");
        
        report.setAlarmFlag(0x00000002); // 超速报警
        assertFalse(report.hasEmergencyAlarm(), "不应该检测到紧急报警");
        assertTrue(report.hasSpeedingAlarm(), "应该检测到超速报警");
        
        // 测试多个报警位组合
        report.setAlarmFlag(0x00000003); // 紧急报警 + 超速报警
        assertTrue(report.hasEmergencyAlarm(), "应该检测到紧急报警");
        assertTrue(report.hasSpeedingAlarm(), "应该检测到超速报警");
        assertFalse(report.hasFatigueAlarm(), "不应该检测到疲劳驾驶");
        
        // 测试高位报警
        report.setAlarmFlag(0x04000000); // 车辆被盗
        assertTrue(report.hasVehicleTheft(), "应该检测到车辆被盗报警");
        assertFalse(report.hasEmergencyAlarm(), "不应该检测到其他报警");
        
        // 测试复杂组合
        report.setAlarmFlag(0x0400000B); // 车辆被盗 + 危险预警 + 超速 + 紧急
        assertTrue(report.hasEmergencyAlarm(), "应该检测到紧急报警");
        assertTrue(report.hasSpeedingAlarm(), "应该检测到超速报警");
        assertTrue(report.hasDangerWarning(), "应该检测到危险预警");
        assertTrue(report.hasVehicleTheft(), "应该检测到车辆被盗");
        assertFalse(report.hasFatigueAlarm(), "不应该检测到疲劳驾驶");
        
        // 测试报警描述功能
        java.util.List<String> alarms = report.getActiveAlarmDescriptions();
        assertEquals(4, alarms.size(), "应该有4个激活的报警");
        assertTrue(alarms.contains("紧急报警"), "应该包含紧急报警描述");
        assertTrue(alarms.contains("超速报警"), "应该包含超速报警描述");
        assertTrue(alarms.contains("危险预警"), "应该包含危险预警描述");
        assertTrue(alarms.contains("车辆被盗"), "应该包含车辆被盗描述");
        
        // 测试无报警情况
        report.setAlarmFlag(0x00000000);
        java.util.List<String> noAlarms = report.getActiveAlarmDescriptions();
        assertTrue(noAlarms.isEmpty(), "无报警时应该返回空列表");
        
        logger.info("报警标志位解析测试通过");
    }
    
    @Test
    public void testAllAlarmFlags() {
        // 测试所有定义的报警标志位
        T0200LocationReport report = new T0200LocationReport();
        
        // 测试所有单独的报警位
        int[] alarmBits = {
            0x00000001, // 紧急报警
            0x00000002, // 超速报警
            0x00000004, // 疲劳驾驶
            0x00000008, // 危险预警
            0x00000010, // GNSS模块故障
            0x00000020, // GNSS天线未接或被剪断
            0x00000040, // GNSS天线短路
            0x00000080, // 终端主电源欠压
            0x00000100, // 终端主电源掉电
            0x00000200, // 终端LCD或显示器故障
            0x00000400, // TTS模块故障
            0x00000800, // 摄像头故障
            0x00040000, // 当天累计驾驶超时
            0x00080000, // 超时停车
            0x00100000, // 进出区域报警
            0x00200000, // 进出路线报警
            0x00400000, // 路段行驶时间不足/过长
            0x00800000, // 路线偏离报警
            0x01000000, // 车辆VSS故障
            0x02000000, // 车辆油量异常
            0x04000000, // 车辆被盗
            0x08000000, // 车辆非法点火
            0x10000000, // 车辆非法位移
            0x20000000, // 碰撞侧翻报警
            0x40000000  // 非法开门报警
        };
        
        for (int alarmBit : alarmBits) {
            report.setAlarmFlag(alarmBit);
            java.util.List<String> alarms = report.getActiveAlarmDescriptions();
            assertEquals(1, alarms.size(), 
                "每个报警位应该只激活一个报警: 0x" + Integer.toHexString(alarmBit));
        }
        
        // 测试所有报警位同时激活
        int allAlarms = 0;
        for (int alarmBit : alarmBits) {
            allAlarms |= alarmBit;
        }
        report.setAlarmFlag(allAlarms);
        java.util.List<String> allActiveAlarms = report.getActiveAlarmDescriptions();
        assertEquals(25, allActiveAlarms.size(), "所有报警位激活时应该有25个报警");
        
        logger.info("所有报警标志位测试通过，共测试了 {} 个报警位", alarmBits.length);
        logger.info("激活所有报警时的描述: {}", String.join(", ", allActiveAlarms));
    }

}