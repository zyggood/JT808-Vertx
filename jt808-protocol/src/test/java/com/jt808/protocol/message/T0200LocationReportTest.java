package com.jt808.protocol.message;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;
import io.vertx.core.buffer.Buffer;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * T0200位置信息汇报消息测试
 * 测试报警标志位、状态位、附加信息解析等功能
 */
class T0200LocationReportTest {
    
    private T0200LocationReport report;
    
    @BeforeEach
    void setUp() {
        report = new T0200LocationReport();
    }
    
    @Test
    void testBasicLocationData() {
        // 测试基本位置数据设置和获取
        report.setAlarmFlag(0x00000001);
        report.setStatusFlag(0x00000002);
        report.setLatitude(39908692);
        report.setLongitude(116397477);
        report.setAltitude(50);
        report.setSpeed(60);
        report.setDirection(90);
        report.setDateTime(LocalDateTime.now());
        
        assertEquals(0x00000001, report.getAlarmFlag());
        assertEquals(0x00000002, report.getStatusFlag());
        assertEquals(39908692, report.getLatitude(), 0.000001);
        assertEquals(116397477, report.getLongitude(), 0.000001);
        assertEquals(50, report.getAltitude());
        assertEquals(60, report.getSpeed());
        assertEquals(90, report.getDirection());
        assertNotNull(report.getDateTime());
    }
    
    @Test
    void testStatusFlagParsing() {
        // 测试状态位解析功能
        
        // 测试ACC开关状态
        report.setStatusFlag(0x00000001); // ACC开
        assertTrue(report.isACCOn(), "ACC应该为开启状态");
        
        report.setStatusFlag(0x00000000); // ACC关
        assertFalse(report.isACCOn(), "ACC应该为关闭状态");
        
        // 测试定位状态
        report.setStatusFlag(0x00000002); // 已定位
        assertTrue(report.isPositioned(), "应该为已定位状态");
        
        report.setStatusFlag(0x00000000); // 未定位
        assertFalse(report.isPositioned(), "应该为未定位状态");
        
        // 测试经纬度方向
        report.setStatusFlag(0x00000004); // 南纬
        assertTrue(report.isSouthLatitude(), "应该为南纬");

        report.setStatusFlag(0x00000008); // 西经
        assertTrue(report.isWestLongitude(), "应该为西经");

        // 测试运营状态
        report.setStatusFlag(0x00000010); // 运营状态-停运
        assertTrue(report.isOutOfService(), "应该为运营状态-停运");
        
        // 测试载重状态
        report.setStatusFlag(0x00000000); // 空车
        assertEquals(0, report.getLoadStatus(), "应该为空车状态");
        
        report.setStatusFlag(0x00000300); // 满载
        assertEquals(3, report.getLoadStatus(), "应该为满载状态");
        
        // 测试车辆状态
        report.setStatusFlag(0x00000400); // 车辆油路断开
        assertTrue(report.isOilCircuitDisconnected(), "油路应该为断开状态");
        
        report.setStatusFlag(0x00000800); // 车辆电路断开
        assertTrue(report.isElectricCircuitDisconnected(), "电路应该为断开状态");
        
        // 测试车门状态
        report.setStatusFlag(0x00001000); // 车门加锁
        assertTrue(report.isDoorLocked(), "车门应该为加锁状态");
        
        report.setStatusFlag(0x00002000); // 前门开
        assertTrue(report.isDoor1Open(), "前门应该为开启状态");
        
        report.setStatusFlag(0x00004000); // 中门开
        assertTrue(report.isDoor2Open(), "中门应该为开启状态");
        
        report.setStatusFlag(0x00008000); // 后门开
        assertTrue(report.isDoor3Open(), "后门应该为开启状态");
        
        report.setStatusFlag(0x00010000); // 驾驶席门开
        assertTrue(report.isDoor4Open(), "驾驶席门应该为开启状态");
        
        report.setStatusFlag(0x00020000); // 自定义门开
        assertTrue(report.isDoor5Open(), "自定义门应该为开启状态");
        
        // 测试定位系统状态
        report.setStatusFlag(0x00040000); // 使用GPS定位
        assertTrue(report.isGPSPositioning(), "应该使用GPS定位");
        
        report.setStatusFlag(0x00080000); // 使用北斗定位
        assertTrue(report.isBeidouPositioning(), "应该使用北斗定位");
        
        report.setStatusFlag(0x00100000); // 使用GLONASS定位
        assertTrue(report.isGLONASSPositioning(), "应该使用GLONASS定位");
        
        report.setStatusFlag(0x00200000); // 使用Galileo定位
        assertTrue(report.isGalileoPositioning(), "应该使用Galileo定位");
    }
    
    @Test
    void testStatusFlagCombinations() {
        // 测试状态位组合
        int combinedStatus = 0x00000001 | 0x00000002 | 0x00000010 | 0x00000300 | 0x00040000;
        // ACC开 + 已定位 + 运营状态 + 满载 + GPS定位
        
        report.setStatusFlag(combinedStatus);
        
        assertTrue(report.isACCOn(), "ACC应该开启");
        assertTrue(report.isPositioned(), "应该已定位");
        assertTrue(report.isOutOfService(), "应该为运营状态");
        assertEquals(3, report.getLoadStatus(), "应该为满载状态");
        assertTrue(report.isGPSPositioning(), "应该使用GPS定位");
        
        assertFalse(report.isWestLongitude(), "不应该为西经");
        assertFalse(report.isSouthLatitude(), "不应该为南纬");
        assertFalse(report.isOilCircuitDisconnected(), "油路不应该断开");
    }
    
    @Test
    void testAdditionalInfoParsing() {
        // 测试附加信息解析
        Buffer additionalInfo = Buffer.buffer();
        
        // 添加里程信息 (ID: 0x01, 长度: 4字节)
        additionalInfo.appendByte((byte) 0x01); // ID
        additionalInfo.appendByte((byte) 0x04); // 长度
        additionalInfo.appendInt(12345); // 里程值 (km)
        
        // 添加油量信息 (ID: 0x02, 长度: 2字节)
        additionalInfo.appendByte((byte) 0x02); // ID
        additionalInfo.appendByte((byte) 0x02); // 长度
        additionalInfo.appendShort((short) 500); // 油量值 (0.1L)
        
        // 添加行驶记录速度 (ID: 0x03, 长度: 2字节)
        additionalInfo.appendByte((byte) 0x03); // ID
        additionalInfo.appendByte((byte) 0x02); // 长度
        additionalInfo.appendShort((short) 800); // 速度值 (0.1km/h)
        
        // 添加报警事件ID (ID: 0x04, 长度: 2字节)
        additionalInfo.appendByte((byte) 0x04); // ID
        additionalInfo.appendByte((byte) 0x02); // 长度
        additionalInfo.appendShort((short) 1001); // 事件ID
        
        report.setAdditionalInfo(additionalInfo);
        
        // 验证附加信息解析
        Map<Integer, Object> parsedInfo = report.parseAdditionalInfo(additionalInfo.getBytes());
        assertNotNull(parsedInfo, "解析后的附加信息不应为空");
        
        // 验证里程信息
        assertTrue(parsedInfo.containsKey(0x01), "应该包含里程信息");
        assertEquals(1234.5, parsedInfo.get(0x01), "里程值应该正确");
        
        // 验证油量信息
        assertTrue(parsedInfo.containsKey(0x02), "应该包含油量信息");
        assertEquals(50.0, parsedInfo.get(0x02), "油量值应该正确");
        
        // 验证行驶记录速度
        assertTrue(parsedInfo.containsKey(0x03), "应该包含行驶记录速度");
        assertEquals(80.0, parsedInfo.get(0x03), "速度值应该正确");
        
        // 验证报警事件ID
        assertTrue(parsedInfo.containsKey(0x04), "应该包含报警事件ID");
        assertEquals(1001, parsedInfo.get(0x04), "事件ID应该正确");
    }
    
    @Test
    void testComplexAdditionalInfo() {
        // 测试复杂附加信息解析
        Buffer additionalInfo = Buffer.buffer();
        
        // 添加扩展车辆信号状态位 (ID: 0x25, 长度: 4字节)
        additionalInfo.appendByte((byte) 0x25); // ID
        additionalInfo.appendByte((byte) 0x04); // 长度
        int vehicleSignal = 0x00000001 | 0x00000002 | 0x00000010; // 近光灯 + 远光灯 + 左转向灯
        additionalInfo.appendInt(vehicleSignal);
        
        // 添加IO状态位 (ID: 0x2A, 长度: 2字节)
        additionalInfo.appendByte((byte) 0x2A); // ID
        additionalInfo.appendByte((byte) 0x02); // 长度
        short ioStatus = (short) 0x0101; // 深度休眠状态 + AD0高电平
        additionalInfo.appendShort(ioStatus);
        
        // 添加模拟量 (ID: 0x2B, 长度: 4字节)
        additionalInfo.appendByte((byte) 0x2B); // ID
        additionalInfo.appendByte((byte) 0x04); // 长度
        additionalInfo.appendInt(0x12345678); // 模拟量值
        
        // 添加无线通信网络信号强度 (ID: 0x30, 长度: 1字节)
        additionalInfo.appendByte((byte) 0x30); // ID
        additionalInfo.appendByte((byte) 0x01); // 长度
        additionalInfo.appendByte((byte) 85); // 信号强度
        
        // 添加GNSS定位卫星数 (ID: 0x31, 长度: 1字节)
        additionalInfo.appendByte((byte) 0x31); // ID
        additionalInfo.appendByte((byte) 0x01); // 长度
        additionalInfo.appendByte((byte) 12); // 卫星数

        Map<Integer, Object> parsedInfo = report.parseAdditionalInfo(additionalInfo.getBytes());


        // 验证扩展车辆信号状态位
        assertTrue(parsedInfo.containsKey(0x25), "应该包含扩展车辆信号状态位");
        @SuppressWarnings("unchecked")
        Map<String, Boolean> vehicleSignalMap = (Map<String, Boolean>) parsedInfo.get(0x25);
        assertTrue(vehicleSignalMap.get("近光灯"), "近光灯应该开启");
        assertTrue(vehicleSignalMap.get("远光灯"), "远光灯应该开启");
        assertTrue(vehicleSignalMap.get("左转向灯"), "左转向灯应该开启");
        assertFalse(vehicleSignalMap.get("右转向灯"), "右转向灯应该关闭");
        
        // 验证IO状态位
        assertTrue(parsedInfo.containsKey(0x2A), "应该包含IO状态位");
        @SuppressWarnings("unchecked")
        Map<String, Boolean> ioStatusMap = (Map<String, Boolean>) parsedInfo.get(0x2A);
        assertTrue(ioStatusMap.get("深度休眠状态"), "应该为深度休眠状态");
        assertTrue(ioStatusMap.get("AD0高电平"), "AD0应该为高电平");
        
        // 验证模拟量
        assertTrue(parsedInfo.containsKey(0x2B), "应该包含模拟量");
        @SuppressWarnings("unchecked")
        Map<String, Integer> analogMap = (Map<String, Integer>) parsedInfo.get(0x2B);
        assertEquals(0x1234, (int) analogMap.get("AD0"), "AD0值应该正确");
        assertEquals(0x5678, (int) analogMap.get("AD1"), "AD1值应该正确");
        
        // 验证信号强度
        assertTrue(parsedInfo.containsKey(0x30), "应该包含信号强度");
        assertEquals(85, parsedInfo.get(0x30), "信号强度应该正确");
        
        // 验证卫星数
        assertTrue(parsedInfo.containsKey(0x31), "应该包含卫星数");
        assertEquals(12, parsedInfo.get(0x31), "卫星数应该正确");
    }
    
    @Test
    void testToStringEnhanced() {
        // 测试增强的toString方法
        report.setAlarmFlag(0x00000003); // 紧急报警 + 超速报警
        report.setStatusFlag(0x00000013); // ACC开 + 已定位 + 运营状态
        report.setLatitude(39908692);
        report.setLongitude(116397477);
        report.setAltitude(50);
        report.setSpeed(60);
        report.setDirection(90);
        report.setDateTime(LocalDateTime.now());

        // 添加附加信息
        Buffer additionalInfo = Buffer.buffer();
        additionalInfo.appendByte((byte) 0x01); // 里程
        additionalInfo.appendByte((byte) 0x04);
        additionalInfo.appendInt(12345);

        additionalInfo.appendByte((byte) 0x02); // 油量
        additionalInfo.appendByte((byte) 0x02);
        additionalInfo.appendShort((short) 500);

        report.setAdditionalInfo(additionalInfo);

        String toStringResult = report.toString();

        // 验证toString包含基本信息
        assertTrue(toStringResult.contains("报警标志位"), "应该包含报警标志位信息");
        assertTrue(toStringResult.contains("状态标志位"), "应该包含状态标志位信息");
        assertTrue(toStringResult.contains("纬度"), "应该包含纬度信息");
        assertTrue(toStringResult.contains("经度"), "应该包含经度信息");
        assertTrue(toStringResult.contains("速度"), "应该包含速度信息");
        assertTrue(toStringResult.contains("方向"), "应该包含方向信息");

        // 验证toString包含状态位详细信息
        assertTrue(toStringResult.contains("ACC开关: 开"), "应该包含ACC状态");
        assertTrue(toStringResult.contains("定位状态: 已定位"), "应该包含定位状态");
        assertTrue(toStringResult.contains("运营状态: 停运"), "应该包含运营状态");

        // 验证toString包含报警信息
        assertTrue(toStringResult.contains("紧急报警"), "应该包含紧急报警");
        assertTrue(toStringResult.contains("超速报警"), "应该包含超速报警");

        // 验证toString包含附加信息
//        assertTrue(toStringResult.contains("里程"), "应该包含里程信息");
//        assertTrue(toStringResult.contains("12345 km"), "应该包含格式化的里程值");
//        assertTrue(toStringResult.contains("油量"), "应该包含油量信息");
//        assertTrue(toStringResult.contains("50.0 L"), "应该包含格式化的油量值");

        System.out.println("Enhanced toString output:");
        System.out.println(toStringResult);
    }
    
    @Test
    void testEmptyAdditionalInfo() {
        // 测试空附加信息

        Map<Integer, Object> parsedInfo = report.parseAdditionalInfo(Buffer.buffer().getBytes());
        assertTrue(parsedInfo.isEmpty(), "空附加信息应该返回空Map");
        
        String toStringResult = report.toString();
        assertTrue(toStringResult.contains("附加信息: 无"), "toString应该显示无附加信息");
    }
    
    @Test
    void testInvalidAdditionalInfo() {
        // 测试无效附加信息（长度不足）
        Buffer invalidInfo = Buffer.buffer();
        invalidInfo.appendByte((byte) 0x01); // ID
        invalidInfo.appendByte((byte) 0x04); // 声明长度为4
        invalidInfo.appendByte((byte) 0x12); // 但只有1个字节数据

        // 应该能够处理无效数据而不抛出异常
        assertDoesNotThrow(() -> {
            Map<Integer, Object> parsedInfo = report.parseAdditionalInfo(invalidInfo.getBytes());
            // 无效数据应该被忽略
            assertFalse(parsedInfo.containsKey(0x01), "无效数据不应该被解析");
        });
    }
    
    @Test
    void testAllAlarmAndStatusCombinations() {
        // 测试报警和状态位的各种组合
        int[] testAlarms = {0x00000001, 0x00000002, 0x00000004, 0x00000008};
        int[] testStatuses = {0x00000001, 0x00000002, 0x00000010, 0x00000020};
        
        for (int alarm : testAlarms) {
            for (int status : testStatuses) {
                report.setAlarmFlag(alarm);
                report.setStatusFlag(status);
                
                // 验证toString不会抛出异常
                assertDoesNotThrow(() -> {
                    String result = report.toString();
                    assertNotNull(result, "toString结果不应为null");
                    assertFalse(result.isEmpty(), "toString结果不应为空");
                });
                
                // 验证报警描述不会抛出异常
                assertDoesNotThrow(() -> {
                    List<String> alarms = report.getActiveAlarmDescriptions();
                    assertNotNull(alarms, "报警描述列表不应为null");
                });
            }
        }
    }
}