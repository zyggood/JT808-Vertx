package com.jt808.protocol.message;

import io.vertx.core.buffer.Buffer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 附加信息解析测试
 * 测试T0200LocationReport中的附加信息解析功能
 */
class AdditionalInfoTest {

    private static final Logger logger = LoggerFactory.getLogger(AdditionalInfoTest.class);
    
    private T0200LocationReport report;
    
    @BeforeEach
    void setUp() {
        report = new T0200LocationReport();
    }
    
    @Test
    void testBasicAdditionalInfo() {
        // 测试基本附加信息解析
        Buffer additionalInfo = Buffer.buffer();
        
        // 添加里程信息 (ID: 0x01, 长度: 4字节)
        additionalInfo.appendByte((byte) 0x01);
        additionalInfo.appendByte((byte) 0x04);
        additionalInfo.appendInt(12345); // 里程值 (km)
        
        // 添加油量信息 (ID: 0x02, 长度: 2字节)
        additionalInfo.appendByte((byte) 0x02);
        additionalInfo.appendByte((byte) 0x02);
        additionalInfo.appendShort((short) 500); // 油量值 (0.1L)
        
        report.setAdditionalInfo(additionalInfo);
        
        Map<Integer, Object> parsedInfo = report.getParsedAdditionalInfo();
        
        // 验证里程信息
        assertTrue(parsedInfo.containsKey(0x01), "应该包含里程信息");
        assertEquals(1234.5, parsedInfo.get(0x01), "里程值应该正确");
        
        // 验证油量信息
        assertTrue(parsedInfo.containsKey(0x02), "应该包含油量信息");
        assertEquals(50.0, parsedInfo.get(0x02), "油量值应该正确");
    }
    
    @Test
    void testSpeedAndAlarmInfo() {
        // 测试速度和报警相关附加信息
        Buffer additionalInfo = Buffer.buffer();
        
        // 添加行驶记录速度 (ID: 0x03, 长度: 2字节)
        additionalInfo.appendByte((byte) 0x03);
        additionalInfo.appendByte((byte) 0x02);
        additionalInfo.appendShort((short) 800); // 速度值 (0.1km/h)
        
        // 添加报警事件ID (ID: 0x04, 长度: 2字节)
        additionalInfo.appendByte((byte) 0x04);
        additionalInfo.appendByte((byte) 0x02);
        additionalInfo.appendShort((short) 1001); // 事件ID
        
        report.setAdditionalInfo(additionalInfo);
        
        Map<Integer, Object> parsedInfo = report.getParsedAdditionalInfo();
        
        // 验证行驶记录速度
        assertTrue(parsedInfo.containsKey(0x03), "应该包含行驶记录速度");
        assertEquals(80.0, parsedInfo.get(0x03), "速度值应该正确");
        
        // 验证报警事件ID
        assertTrue(parsedInfo.containsKey(0x04), "应该包含报警事件ID");
        assertEquals(1001, parsedInfo.get(0x04), "事件ID应该正确");
    }
    
    @Test
    void testOverspeedAlarmInfo() {
        // 测试超速报警附加信息 (ID: 0x11)
        Buffer additionalInfo = Buffer.buffer();
        
        additionalInfo.appendByte((byte) 0x11);
        additionalInfo.appendByte((byte) 0x05); // 长度: 5字节
        additionalInfo.appendByte((byte) 0x01); // 位置类型: 圆形区域
        additionalInfo.appendInt(0x12345678); // 区域或路段ID
        
        report.setAdditionalInfo(additionalInfo);
        
        Map<Integer, Object> parsedInfo = report.getParsedAdditionalInfo();
        
        assertTrue(parsedInfo.containsKey(0x11), "应该包含超速报警信息");
        @SuppressWarnings("unchecked")
        Map<String, Object> overspeedInfo = (Map<String, Object>) parsedInfo.get(0x11);
        assertEquals(1, overspeedInfo.get("locationType"), "位置类型应该正确");
        assertEquals(0x12345678L, overspeedInfo.get("areaId"), "区域ID应该正确");
    }
    
    @Test
    void testAreaRouteAlarmInfo() {
        // 测试进出区域/路线报警附加信息 (ID: 0x12)
        Buffer additionalInfo = Buffer.buffer();
        
        additionalInfo.appendByte((byte) 0x12);
        additionalInfo.appendByte((byte) 0x06); // 长度: 6字节
        additionalInfo.appendByte((byte) 0x02); // 位置类型: 矩形区域
        additionalInfo.appendInt(0x87654321); // 区域或路段ID
        additionalInfo.appendByte((byte) 0x01); // 方向: 进入
        
        report.setAdditionalInfo(additionalInfo);
        
        Map<Integer, Object> parsedInfo = report.getParsedAdditionalInfo();
        
        assertTrue(parsedInfo.containsKey(0x12), "应该包含进出区域报警信息");
        @SuppressWarnings("unchecked")
        Map<String, Object> areaInfo = (Map<String, Object>) parsedInfo.get(0x12);
        assertEquals(2, areaInfo.get("locationType"), "位置类型应该正确");
        assertEquals(1, areaInfo.get("direction"), "方向应该正确");
        assertEquals(0x87654321L, areaInfo.get("areaId"), "区域ID应该正确");
    }
    
    @Test
    void testRoadSectionTimeAlarmInfo() {
        // 测试路段行驶时间不足/过长报警附加信息 (ID: 0x13)
        Buffer additionalInfo = Buffer.buffer();
        
        additionalInfo.appendByte((byte) 0x13);
        additionalInfo.appendByte((byte) 0x07); // 长度: 7字节
        additionalInfo.appendInt(0xABCDEF12); // 路段ID
        additionalInfo.appendShort((short) 1800); // 路段行驶时间 (秒)
        additionalInfo.appendByte((byte) 0x01); // 结果: 不足
        
        report.setAdditionalInfo(additionalInfo);
        
        Map<Integer, Object> parsedInfo = report.getParsedAdditionalInfo();
        
        assertTrue(parsedInfo.containsKey(0x13), "应该包含路段行驶时间报警信息");
        @SuppressWarnings("unchecked")
        Map<String, Object> roadInfo = (Map<String, Object>) parsedInfo.get(0x13);
        assertEquals(0xABCDEF12L, roadInfo.get("routeId"), "路段ID应该正确");
        assertEquals(1800, roadInfo.get("driveTime"), "行驶时间应该正确");
        assertEquals(1, roadInfo.get("result"), "结果应该正确");
    }
    
    @Test
    void testExtendedVehicleSignalStatus() {
        // 测试扩展车辆信号状态位 (ID: 0x25)
        Buffer additionalInfo = Buffer.buffer();
        
        additionalInfo.appendByte((byte) 0x25);
        additionalInfo.appendByte((byte) 0x04); // 长度: 4字节
        
        // 设置车辆信号状态位
        int vehicleSignal = 0x00000001 | 0x00000002 | 0x00000008 | 0x00000004;
        // 近光灯 + 远光灯 + 左转向灯 + 右转向灯
        additionalInfo.appendInt(vehicleSignal);
        
        report.setAdditionalInfo(additionalInfo);
        
        Map<Integer, Object> parsedInfo = report.getParsedAdditionalInfo();
        
        assertNotNull(parsedInfo, "解析后的附加信息不应该为null");
        assertTrue(parsedInfo.containsKey(0x25), "应该包含扩展车辆信号状态位");
        @SuppressWarnings("unchecked")
        Map<String, Boolean> vehicleSignalMap = (Map<String, Boolean>) parsedInfo.get(0x25);
        
        assertNotNull(vehicleSignalMap, "车辆信号状态位映射不应该为null");
        assertTrue(vehicleSignalMap.get("lowBeam"), "近光灯应该开启");
        assertTrue(vehicleSignalMap.get("highBeam"), "远光灯应该开启");
        assertTrue(vehicleSignalMap.get("leftTurnSignal"), "左转向灯应该开启");
        assertTrue(vehicleSignalMap.get("rightTurnSignal"), "右转向灯应该开启");
        assertFalse(vehicleSignalMap.get("brake"), "制动应该关闭");
        assertFalse(vehicleSignalMap.get("reverse"), "倒车应该关闭");
        assertFalse(vehicleSignalMap.get("fogLight"), "雾灯应该关闭");
        assertFalse(vehicleSignalMap.get("positionLight"), "示廓灯应该关闭");
        assertFalse(vehicleSignalMap.get("horn"), "喇叭应该关闭");
        assertFalse(vehicleSignalMap.get("airConditioner"), "空调应该关闭");
    }
    
    @Test
    void testIOStatus() {
        // 测试IO状态位 (ID: 0x2A)
        Buffer additionalInfo = Buffer.buffer();
        
        additionalInfo.appendByte((byte) 0x2A);
        additionalInfo.appendByte((byte) 0x02); // 长度: 2字节
        
        // 设置IO状态位
        short ioStatus = (short) (0x0001 | 0x0100); // 深度休眠状态 + AD0高电平
        additionalInfo.appendShort(ioStatus);
        
        report.setAdditionalInfo(additionalInfo);
        
        Map<Integer, Object> parsedInfo = report.getParsedAdditionalInfo();
        
        assertNotNull(parsedInfo, "解析后的附加信息不应该为null");
        assertTrue(parsedInfo.containsKey(0x2A), "应该包含IO状态位");
        @SuppressWarnings("unchecked")
        Map<String, Boolean> ioStatusMap = (Map<String, Boolean>) parsedInfo.get(0x2A);
        
        assertNotNull(ioStatusMap, "IO状态位映射不应该为null");
        assertTrue(ioStatusMap.get("deepSleep"), "深度休眠状态应该为true");
        assertFalse(ioStatusMap.get("sleep"), "休眠状态应该为false");
    }
    
    @Test
    void testAnalogValue() {
        // 测试模拟量 (ID: 0x2B)
        Buffer additionalInfo = Buffer.buffer();
        
        additionalInfo.appendByte((byte) 0x2B);
        additionalInfo.appendByte((byte) 0x04); // 长度: 4字节
        additionalInfo.appendInt(0x12345678); // AD0=0x1234, AD1=0x5678
        
        report.setAdditionalInfo(additionalInfo);
        
        Map<Integer, Object> parsedInfo = report.getParsedAdditionalInfo();
        
        assertTrue(parsedInfo.containsKey(0x2B), "应该包含模拟量");
        @SuppressWarnings("unchecked")
        Map<String, Integer> analogMap = (Map<String, Integer>) parsedInfo.get(0x2B);
        
        assertEquals(0x1234, (int) analogMap.get("AD1"), "AD1值应该正确");
        assertEquals(0x5678, (int) analogMap.get("AD0"), "AD0值应该正确");
    }
    
    @Test
    void testSignalStrengthAndSatelliteCount() {
        // 测试信号强度和卫星数
        Buffer additionalInfo = Buffer.buffer();
        
        // 添加无线通信网络信号强度 (ID: 0x30)
        additionalInfo.appendByte((byte) 0x30);
        additionalInfo.appendByte((byte) 0x01);
        additionalInfo.appendByte((byte) 85); // 信号强度
        
        // 添加GNSS定位卫星数 (ID: 0x31)
        additionalInfo.appendByte((byte) 0x31);
        additionalInfo.appendByte((byte) 0x01);
        additionalInfo.appendByte((byte) 12); // 卫星数
        
        report.setAdditionalInfo(additionalInfo);
        
        Map<Integer, Object> parsedInfo = report.getParsedAdditionalInfo();
        
        // 验证信号强度
        assertTrue(parsedInfo.containsKey(0x30), "应该包含信号强度");
        assertEquals(85, parsedInfo.get(0x30), "信号强度应该正确");
        
        // 验证卫星数
        assertTrue(parsedInfo.containsKey(0x31), "应该包含卫星数");
        assertEquals(12, parsedInfo.get(0x31), "卫星数应该正确");
    }
    
    @Test
    void testMultipleAdditionalInfo() {
        // 测试多个附加信息项的组合
        Buffer additionalInfo = Buffer.buffer();
        
        // 里程
        additionalInfo.appendByte((byte) 0x01);
        additionalInfo.appendByte((byte) 0x04);
        additionalInfo.appendInt(54321);
        
        // 油量
        additionalInfo.appendByte((byte) 0x02);
        additionalInfo.appendByte((byte) 0x02);
        additionalInfo.appendShort((short) 750);
        
        // 行驶记录速度
        additionalInfo.appendByte((byte) 0x03);
        additionalInfo.appendByte((byte) 0x02);
        additionalInfo.appendShort((short) 650);
        
        // 扩展车辆信号状态位
        additionalInfo.appendByte((byte) 0x25);
        additionalInfo.appendByte((byte) 0x04);
        additionalInfo.appendInt(0x00000013); // 近光灯 + 远光灯 + 制动
        
        // 信号强度
        additionalInfo.appendByte((byte) 0x30);
        additionalInfo.appendByte((byte) 0x01);
        additionalInfo.appendByte((byte) 95);
        
        // 卫星数
        additionalInfo.appendByte((byte) 0x31);
        additionalInfo.appendByte((byte) 0x01);
        additionalInfo.appendByte((byte) 15);
        
        report.setAdditionalInfo(additionalInfo);
        
        Map<Integer, Object> parsedInfo = report.getParsedAdditionalInfo();
        
        assertNotNull(parsedInfo, "解析后的附加信息不应该为null");
        assertEquals(6, parsedInfo.size(), "应该解析出6个附加信息项");
        
        // 验证所有项目
        assertEquals(5432.1, parsedInfo.get(0x01), "里程应该正确");
        assertEquals(75.0, parsedInfo.get(0x02), "油量应该正确");
        assertEquals(65.0, parsedInfo.get(0x03), "速度应该正确");
        assertEquals(95, parsedInfo.get(0x30), "信号强度应该正确");
        assertEquals(15, parsedInfo.get(0x31), "卫星数应该正确");
        
        @SuppressWarnings("unchecked")
        Map<String, Boolean> vehicleSignalMap = (Map<String, Boolean>) parsedInfo.get(0x25);
        assertNotNull(vehicleSignalMap, "车辆信号状态位映射不应该为null");
        assertTrue(vehicleSignalMap.get("lowBeam"), "近光灯应该开启");
        assertTrue(vehicleSignalMap.get("highBeam"), "远光灯应该开启");
        assertTrue(vehicleSignalMap.get("brake"), "制动应该开启");
        assertFalse(vehicleSignalMap.get("leftTurnSignal"), "左转向灯应该关闭");
    }
    
    @Test
    void testEmptyAdditionalInfo() {
        // 测试空附加信息
        report.setAdditionalInfo(Buffer.buffer());
        
        Map<Integer, Object> parsedInfo = report.getParsedAdditionalInfo();
        assertNull(parsedInfo, "空附加信息应该返回null");
    }
    
    @Test
    void testInvalidAdditionalInfo() {
        // 测试无效附加信息
        Buffer invalidInfo = Buffer.buffer();
        
        // 情况1: 长度不足
        invalidInfo.appendByte((byte) 0x01); // ID
        invalidInfo.appendByte((byte) 0x04); // 声明长度为4
        invalidInfo.appendByte((byte) 0x12); // 但只有1个字节数据
        
        report.setAdditionalInfo(invalidInfo);
        
        assertDoesNotThrow(() -> {
            Map<Integer, Object> parsedInfo = report.getParsedAdditionalInfo();
            // 无效数据应该被忽略，可能返回null或空Map
            if (parsedInfo != null) {
                assertFalse(parsedInfo.containsKey(0x01), "无效数据不应该被解析");
            }
        });
        
        // 情况2: 只有ID没有长度
        Buffer invalidInfo2 = Buffer.buffer();
        invalidInfo2.appendByte((byte) 0x02); // 只有ID
        
        report.setAdditionalInfo(invalidInfo2);
        
        assertDoesNotThrow(() -> {
            Map<Integer, Object> parsedInfo = report.getParsedAdditionalInfo();
            // 不完整数据应该被忽略，可能返回null或空Map
            if (parsedInfo != null) {
                assertTrue(parsedInfo.isEmpty(), "不完整数据应该被忽略");
            }
        });
    }
    
    @Test
    void testUnknownAdditionalInfoId() {
        // 测试未知的附加信息ID
        Buffer additionalInfo = Buffer.buffer();
        
        // 添加未知ID
        additionalInfo.appendByte((byte) 0xFF); // 未知ID
        additionalInfo.appendByte((byte) 0x04);
        additionalInfo.appendInt(0x12345678);
        
        report.setAdditionalInfo(additionalInfo);
        
        Map<Integer, Object> parsedInfo = report.getParsedAdditionalInfo();
        
        // 未知ID应该被解析为原始字节数组
        assertTrue(parsedInfo.containsKey(0xFF), "未知ID应该被保存");
        assertNotNull(parsedInfo.get(0xFF), "未知ID的值不应为null");
    }
    
    @Test
    void testAdditionalInfoToString() {
        // 测试附加信息在toString中的显示
        Buffer additionalInfo = Buffer.buffer();
        
        // 添加里程
        additionalInfo.appendByte((byte) 0x01);
        additionalInfo.appendByte((byte) 0x04);
        additionalInfo.appendInt(12345);
        
        // 添加油量
        additionalInfo.appendByte((byte) 0x02);
        additionalInfo.appendByte((byte) 0x02);
        additionalInfo.appendShort((short) 500);
        
        // 添加扩展车辆信号状态位
        additionalInfo.appendByte((byte) 0x25);
        additionalInfo.appendByte((byte) 0x04);
        additionalInfo.appendInt(0x00000003); // 近光灯 + 远光灯
        
        report.setAdditionalInfo(additionalInfo);
        
        // 确保附加信息被正确解析
        Map<Integer, Object> parsedInfo = report.getParsedAdditionalInfo();
        assertNotNull(parsedInfo, "解析后的附加信息不应该为null");
        
        String toStringResult = report.toString();
        assertNotNull(toStringResult, "toString结果不应该为null");
        
        // 验证toString包含格式化的附加信息
        assertTrue(toStringResult.contains("里程"), "应该包含里程描述");
        assertTrue(toStringResult.contains("1234.5 km"), "应该包含格式化的里程值");
        assertTrue(toStringResult.contains("油量"), "应该包含油量描述");
        assertTrue(toStringResult.contains("50.0 L"), "应该包含格式化的油量值");
        assertTrue(toStringResult.contains("扩展车辆信号状态位"), "应该包含扩展车辆信号状态位描述");

        logger.info("附加信息toString测试:");
        logger.info(toStringResult);
    }
    
    @Test
    void testAdditionalInfoDescriptions() {
        // 测试附加信息描述获取
        
        // 测试已知ID的描述
        String mileageDesc = report.getAdditionalInfoDescription(0x01);
        assertEquals("里程", mileageDesc, "里程描述应该正确");
        
        String fuelDesc = report.getAdditionalInfoDescription(0x02);
        assertEquals("油量", fuelDesc, "油量描述应该正确");
        
        String speedDesc = report.getAdditionalInfoDescription(0x03);
        assertEquals("行驶记录速度", speedDesc, "行驶记录速度描述应该正确");
        
        String alarmEventDesc = report.getAdditionalInfoDescription(0x04);
        assertEquals("人工确认报警事件ID", alarmEventDesc, "人工确认报警事件ID描述应该正确");
        
        String overspeedDesc = report.getAdditionalInfoDescription(0x11);
        assertEquals("超速报警附加信息", overspeedDesc, "超速报警描述应该正确");
        
        String areaDesc = report.getAdditionalInfoDescription(0x12);
        assertEquals("进出区域/路线报警附加信息", areaDesc, "进出区域报警描述应该正确");
        
        String roadTimeDesc = report.getAdditionalInfoDescription(0x13);
        assertEquals("路段行驶时间报警附加信息", roadTimeDesc, "路段行驶时间报警描述应该正确");
        
        String vehicleSignalDesc = report.getAdditionalInfoDescription(0x25);
        assertEquals("扩展车辆信号状态位", vehicleSignalDesc, "扩展车辆信号状态位描述应该正确");
        
        String ioStatusDesc = report.getAdditionalInfoDescription(0x2A);
        assertEquals("IO状态位", ioStatusDesc, "IO状态位描述应该正确");
        
        String analogDesc = report.getAdditionalInfoDescription(0x2B);
        assertEquals("模拟量", analogDesc, "模拟量描述应该正确");
        
        String signalDesc = report.getAdditionalInfoDescription(0x30);
        assertEquals("无线通信网络信号强度", signalDesc, "信号强度描述应该正确");
        
        String satelliteDesc = report.getAdditionalInfoDescription(0x31);
        assertEquals("GNSS定位卫星数", satelliteDesc, "卫星数描述应该正确");
        
        // 测试未知ID的描述
        String unknownDesc = report.getAdditionalInfoDescription(0xFF);
        assertEquals("自定义信息", unknownDesc, "未知ID描述应该正确");
    }
}