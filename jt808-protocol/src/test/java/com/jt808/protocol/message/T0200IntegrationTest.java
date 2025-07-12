package com.jt808.protocol.message;

import io.vertx.core.buffer.Buffer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * T0200位置信息汇报集成测试
 * 测试完整的消息处理流程和数据一致性
 */
class T0200IntegrationTest {

    private static final Logger logger = LoggerFactory.getLogger(T0200IntegrationTest.class);

    private T0200LocationReport report;

    @BeforeEach
    void setUp() {
        report = new T0200LocationReport();
    }

    @Test
    void testCompleteLocationReportScenario() {
        // 测试完整的位置汇报场景

        // 设置基本位置信息
        report.setAlarmFlag(0x00000003); // 紧急报警 + 超速报警
        report.setStatusFlag(0x00040013); // ACC开 + 已定位 + 运营中 + GPS定位
        report.setLatitude(39908692); // 北京天安门纬度
        report.setLongitude(116397477); // 北京天安门经度
        report.setAltitude(50); // 海拔50米
        report.setSpeed(60); // 时速60km/h
        report.setDirection(90); // 正东方向
        report.setDateTime(LocalDateTime.of(2024, 1, 15, 14, 30, 25));

        // 构建复杂的附加信息
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

        // 报警事件ID
        additionalInfo.appendByte((byte) 0x04);
        additionalInfo.appendByte((byte) 0x02);
        additionalInfo.appendShort((short) 2001);

        // 超速报警附加信息
        additionalInfo.appendByte((byte) 0x11);
        additionalInfo.appendByte((byte) 0x05);
        additionalInfo.appendByte((byte) 0x01); // 圆形区域
        additionalInfo.appendInt(0x12345678); // 区域ID

        // 扩展车辆信号状态位
        additionalInfo.appendByte((byte) 0x25);
        additionalInfo.appendByte((byte) 0x04);
        int vehicleSignal = 0x00000001 | 0x00000002 | 0x00000008 | 0x00000010;
        // 近光灯(bit0) + 远光灯(bit1) + 左转向灯(bit3) + 制动(bit4)
        additionalInfo.appendInt(vehicleSignal);

        // IO状态位
        additionalInfo.appendByte((byte) 0x2A);
        additionalInfo.appendByte((byte) 0x02);
        additionalInfo.appendShort((short) 0x0101); // 深度休眠 + AD0高电平

        // 模拟量
        additionalInfo.appendByte((byte) 0x2B);
        additionalInfo.appendByte((byte) 0x04);
        additionalInfo.appendInt(0xABCD1234); // AD0=0xABCD, AD1=0x1234

        // 信号强度
        additionalInfo.appendByte((byte) 0x30);
        additionalInfo.appendByte((byte) 0x01);
        additionalInfo.appendByte((byte) 92);

        // 卫星数
        additionalInfo.appendByte((byte) 0x31);
        additionalInfo.appendByte((byte) 0x01);
        additionalInfo.appendByte((byte) 14);

        report.setAdditionalInfo(additionalInfo);

        // 验证基本信息
        assertEquals(0x00000003, report.getAlarmFlag());
        assertEquals(0x00040013, report.getStatusFlag());
        assertEquals(39908692, report.getLatitude(), 0.000001);
        assertEquals(116397477, report.getLongitude(), 0.000001);
        assertEquals(50, report.getAltitude());
        assertEquals(60, report.getSpeed());
        assertEquals(90, report.getDirection());

        // 验证报警状态
        assertTrue(report.hasEmergencyAlarm(), "应该有紧急报警");
        assertTrue(report.hasSpeedingAlarm(), "应该有超速报警");
        assertFalse(report.hasFatigueAlarm(), "不应该有疲劳驾驶报警");

        List<String> alarms = report.getActiveAlarmDescriptions();
        assertEquals(2, alarms.size(), "应该有2个激活的报警");
        assertTrue(alarms.contains("紧急报警"));
        assertTrue(alarms.contains("超速报警"));

        // 验证状态位
        assertTrue(report.isACCOn(), "ACC应该开启");
        assertTrue(report.isPositioned(), "应该已定位");
        assertTrue(report.isOutOfService(), "应该为运营状态");
        assertTrue(report.isGPSPositioning(), "应该使用GPS定位");
        assertFalse(report.isWestLongitude(), "不应该为西经");
        assertFalse(report.isSouthLatitude(), "不应该为南纬");

        // 验证附加信息解析
        Map<Integer, Object> parsedInfo = report.getParsedAdditionalInfo();
        assertEquals(10, parsedInfo.size(), "应该解析出10个附加信息项");

        // 验证具体附加信息
        assertEquals(12345.6, parsedInfo.get(0x01), "里程应该正确");
        assertEquals(80.0, parsedInfo.get(0x02), "油量应该正确");
        assertEquals(60.5, parsedInfo.get(0x03), "行驶记录速度应该正确");
        assertEquals(2001, parsedInfo.get(0x04), "报警事件ID应该正确");
        assertEquals(92, parsedInfo.get(0x30), "信号强度应该正确");
        assertEquals(14, parsedInfo.get(0x31), "卫星数应该正确");

        // 验证超速报警附加信息
        @SuppressWarnings("unchecked")
        Map<String, Object> overspeedInfo = (Map<String, Object>) parsedInfo.get(0x11);
        assertEquals(1, overspeedInfo.get("locationType"));
        assertEquals(0x12345678L, overspeedInfo.get("areaId"));

        // 验证扩展车辆信号状态位
        @SuppressWarnings("unchecked")
        Map<String, Boolean> vehicleSignalMap = (Map<String, Boolean>) parsedInfo.get(0x25);
        assertTrue(vehicleSignalMap.get("lowBeam"));
        assertTrue(vehicleSignalMap.get("highBeam"));
        assertTrue(vehicleSignalMap.get("leftTurnSignal"));
        assertTrue(vehicleSignalMap.get("brake"));
        assertFalse(vehicleSignalMap.get("rightTurnSignal"));

        // 验证IO状态位
        @SuppressWarnings("unchecked")
        Map<String, Boolean> ioStatusMap = (Map<String, Boolean>) parsedInfo.get(0x2A);
        assertTrue(ioStatusMap.get("deepSleep"));
        assertFalse(ioStatusMap.get("sleep")); // bit1 (0x0002) 在 0x0101 中为 false

        // 验证模拟量
        @SuppressWarnings("unchecked")
        Map<String, Integer> analogMap = (Map<String, Integer>) parsedInfo.get(0x2B);
        assertEquals(0x1234, (int) analogMap.get("AD0")); // bit0-15
        assertEquals(0xABCD, (int) analogMap.get("AD1")); // bit16-31

        // 验证toString输出
        String toStringResult = report.toString();
        assertNotNull(toStringResult);
        assertFalse(toStringResult.isEmpty());

        // 验证toString包含基本信息
        assertTrue(toStringResult.contains("T0200LocationReport"));
        assertTrue(toStringResult.contains("报警标志位"));
        assertTrue(toStringResult.contains("状态标志位"));
        assertTrue(toStringResult.contains("紧急报警"));
        assertTrue(toStringResult.contains("超速报警"));

        logger.info("完整场景测试通过");
        logger.info("toString输出:");
        logger.info(toStringResult);
    }

    @Test
    void testEmergencyScenario() {
        // 测试紧急情况场景

        // 设置紧急报警组合
        int emergencyAlarms = 0x00000001 | 0x00000008 | 0x40000000 | 0x20000000;
        // 紧急报警 + 危险预警 + 侧翻预警 + 碰撞预警
        report.setAlarmFlag(emergencyAlarms);

        // 设置紧急状态
        int emergencyStatus = 0x00000002 | 0x00000C00 | 0x0003E000;
        // 已定位 + 油路电路断开 + 所有门开启
        report.setStatusFlag(emergencyStatus);

        // 设置位置信息
        report.setLatitude(40123456);
        report.setLongitude(116654321);
        report.setSpeed(0); // 停车状态
        report.setDateTime(LocalDateTime.now());

        // 验证紧急报警
        assertTrue(report.hasEmergencyAlarm(), "应该有紧急报警");
        assertTrue(report.hasDangerWarning(), "应该有危险预警");
        assertTrue(report.hasRolloverWarning(), "应该有侧翻预警");
        assertTrue(report.hasCollisionWarning(), "应该有碰撞预警");

        List<String> alarms = report.getActiveAlarmDescriptions();
        assertEquals(4, alarms.size(), "应该有4个激活的报警");
        assertTrue(alarms.contains("紧急报警"));
        assertTrue(alarms.contains("危险预警"));
        assertTrue(alarms.contains("侧翻预警"));
        assertTrue(alarms.contains("碰撞预警"));

        // 验证紧急状态
        assertTrue(report.isPositioned(), "应该已定位");
        assertTrue(report.isOilCircuitDisconnected(), "油路应该断开");
        assertTrue(report.isElectricCircuitDisconnected(), "电路应该断开");
        assertTrue(report.isDoor1Open(), "前门应该开启");
        assertTrue(report.isDoor2Open(), "中门应该开启");
        assertTrue(report.isDoor3Open(), "后门应该开启");
        assertTrue(report.isDoor4Open(), "驾驶席门应该开启");
        assertTrue(report.isDoor5Open(), "自定义门应该开启");

        assertFalse(report.isACCOn(), "ACC应该关闭");
        assertFalse(report.isDoorLocked(), "车门不应该加锁");

        logger.info("紧急场景测试通过");
        logger.info("紧急报警: {}", String.join(", ", alarms));
    }

    @Test
    void testNormalDrivingScenario() {
        // 测试正常行驶场景

        // 设置正常状态（无报警）
        report.setAlarmFlag(0x00000000);

        // 设置正常行驶状态
        int normalStatus = 0x00000001 | 0x00000002 | 0x00000010 | 0x00001000 | 0x000C0000;
        // ACC开 + 已定位 + 运营中 + 车门加锁 + GPS+北斗定位
        report.setStatusFlag(normalStatus);

        // 设置行驶信息
        report.setLatitude(39123456);
        report.setLongitude(116789012);
        report.setAltitude(100);
        report.setSpeed(80); // 正常行驶速度
        report.setDirection(45); // 东北方向
        report.setDateTime(LocalDateTime.now());

        // 添加正常的附加信息
        Buffer additionalInfo = Buffer.buffer();

        // 里程
        additionalInfo.appendByte((byte) 0x01);
        additionalInfo.appendByte((byte) 0x04);
        additionalInfo.appendInt(98765);

        // 油量
        additionalInfo.appendByte((byte) 0x02);
        additionalInfo.appendByte((byte) 0x02);
        additionalInfo.appendShort((short) 600); // 60L

        // 信号强度
        additionalInfo.appendByte((byte) 0x30);
        additionalInfo.appendByte((byte) 0x01);
        additionalInfo.appendByte((byte) 88);

        // 卫星数
        additionalInfo.appendByte((byte) 0x31);
        additionalInfo.appendByte((byte) 0x01);
        additionalInfo.appendByte((byte) 16);

        report.setAdditionalInfo(additionalInfo);

        // 验证无报警
        List<String> alarms = report.getActiveAlarmDescriptions();
        assertTrue(alarms.isEmpty(), "正常行驶时不应该有报警");

        assertFalse(report.hasEmergencyAlarm(), "不应该有紧急报警");
        assertFalse(report.hasSpeedingAlarm(), "不应该有超速报警");
        assertFalse(report.hasFatigueAlarm(), "不应该有疲劳驾驶报警");

        // 验证正常状态
        assertTrue(report.isACCOn(), "ACC应该开启");
        assertTrue(report.isPositioned(), "应该已定位");
        assertTrue(report.isOutOfService(), "应该为停运状态");
        assertTrue(report.isDoorLocked(), "车门应该加锁");
        assertTrue(report.isGPSPositioning(), "应该使用GPS定位");
        assertTrue(report.isBeidouPositioning(), "应该使用北斗定位");

        assertFalse(report.isOilCircuitDisconnected(), "油路不应该断开");
        assertFalse(report.isElectricCircuitDisconnected(), "电路不应该断开");
        assertFalse(report.isDoor1Open(), "前门不应该开启");

        // 验证附加信息
        Map<Integer, Object> parsedInfo = report.getParsedAdditionalInfo();
        assertEquals(4, parsedInfo.size(), "应该有4个附加信息项");
        assertEquals(9876.5, parsedInfo.get(0x01), "里程应该正确");
        assertEquals(60.0, parsedInfo.get(0x02), "油量应该正确");
        assertEquals(88, parsedInfo.get(0x30), "信号强度应该正确");
        assertEquals(16, parsedInfo.get(0x31), "卫星数应该正确");

        logger.info("正常行驶场景测试通过");
    }

    @Test
    void testParkingScenario() {
        // 测试停车场景

        // 设置停车相关报警
        int parkingAlarms = 0x00080000; // 超时停车
        report.setAlarmFlag(parkingAlarms);

        // 设置停车状态
        int parkingStatus = 0x00000002 | 0x00001000; // 已定位 + 车门加锁
        // 注意：ACC关闭，不在运营状态
        report.setStatusFlag(parkingStatus);

        // 设置停车位置
        report.setLatitude(39987654);
        report.setLongitude(116123456);
        report.setSpeed(0); // 停车状态
        report.setDirection(0);
        report.setDateTime(LocalDateTime.now());

        // 验证停车报警
        assertTrue(report.hasOvertimeParking(), "应该有超时停车报警");
        assertFalse(report.hasEmergencyAlarm(), "不应该有紧急报警");

        List<String> alarms = report.getActiveAlarmDescriptions();
        assertEquals(1, alarms.size(), "应该有1个报警");
        assertTrue(alarms.contains("超时停车"));

        // 验证停车状态
        assertFalse(report.isACCOn(), "ACC应该关闭");
        assertTrue(report.isPositioned(), "应该已定位");
        assertFalse(report.isOutOfService(), "不应该为停运状态");
        assertTrue(report.isDoorLocked(), "车门应该加锁");

        assertEquals(0, report.getSpeed(), "停车时速度应该为0");

        logger.info("停车场景测试通过");
    }

    @Test
    void testDataConsistency() {
        // 测试数据一致性

        // 设置初始数据
        int originalAlarm = 0x12345678;
        int originalStatus = 0x87654321;
        int originalLat = 391234567;
        int originalLng = 1169876543;
        int originalAltitude = 12345;
        int originalSpeed = 123;
        int originalDirection = 359;
        LocalDateTime originalTime = LocalDateTime.of(2024, 12, 25, 23, 59, 59);

        report.setAlarmFlag(originalAlarm);
        report.setStatusFlag(originalStatus);
        report.setLatitude(originalLat);
        report.setLongitude(originalLng);
        report.setAltitude(originalAltitude);
        report.setSpeed(originalSpeed);
        report.setDirection(originalDirection);
        report.setDateTime(originalTime);

        // 验证数据一致性
        assertEquals(originalAlarm, report.getAlarmFlag(), "报警标志位应该一致");
        assertEquals(originalStatus, report.getStatusFlag(), "状态标志位应该一致");
        assertEquals(originalLat, report.getLatitude(), 0.000000001, "纬度应该一致");
        assertEquals(originalLng, report.getLongitude(), 0.000000001, "经度应该一致");
        assertEquals(originalAltitude, report.getAltitude(), "高程应该一致");
        assertEquals(originalSpeed, report.getSpeed(), "速度应该一致");
        assertEquals(originalDirection, report.getDirection(), "方向应该一致");
        assertEquals(originalTime, report.getDateTime(), "时间应该一致");

        // 多次调用toString不应该改变数据
        String firstToString = report.toString();
        String secondToString = report.toString();
        assertEquals(firstToString, secondToString, "多次调用toString结果应该一致");

        // 验证数据没有被toString改变
        assertEquals(originalAlarm, report.getAlarmFlag(), "toString后报警标志位不应该改变");
        assertEquals(originalStatus, report.getStatusFlag(), "toString后状态标志位不应该改变");

        logger.info("数据一致性测试通过");
    }

    @Test
    void testBoundaryValues() {
        // 测试边界值

        // 测试最大值
        report.setAlarmFlag(0xFFFFFFFF);
        report.setStatusFlag(0xFFFFFFFF);
        report.setLatitude(90); // 最大纬度
        report.setLongitude(180); // 最大经度
        report.setAltitude(65535); // 最大高程
        report.setSpeed(65535); // 最大速度
        report.setDirection(359); // 最大方向

        assertDoesNotThrow(() -> {
            String result = report.toString();
            assertNotNull(result);
        }, "边界值不应该导致异常");

        // 测试最小值
        report.setAlarmFlag(0x00000000);
        report.setStatusFlag(0x00000000);
        report.setLatitude(-90); // 最小纬度
        report.setLongitude(-180); // 最小经度
        report.setAltitude(0); // 最小高程
        report.setSpeed(0); // 最小速度
        report.setDirection(0); // 最小方向

        assertDoesNotThrow(() -> {
            String result = report.toString();
            assertNotNull(result);
        }, "边界值不应该导致异常");

        logger.info("边界值测试通过");
    }

    @Test
    void testPerformance() {
        // 测试性能（简单的性能测试）

        // 设置测试数据
        report.setAlarmFlag(0x12345678);
        report.setStatusFlag(0x87654321);
        report.setLatitude(39123456);
        report.setLongitude(116987654);
        report.setAltitude(100);
        report.setSpeed(80);
        report.setDirection(90);
        report.setDateTime(LocalDateTime.now());

        // 添加附加信息
        Buffer additionalInfo = Buffer.buffer();
        for (int i = 1; i <= 10; i++) {
            additionalInfo.appendByte((byte) i);
            additionalInfo.appendByte((byte) 0x04);
            additionalInfo.appendInt(i * 1000);
        }
        report.setAdditionalInfo(additionalInfo);

        // 性能测试
        long startTime = System.currentTimeMillis();

        for (int i = 0; i < 1000; i++) {
            // 测试报警检查性能
            report.hasEmergencyAlarm();
            report.hasSpeedingAlarm();
            report.getActiveAlarmDescriptions();

            // 测试状态检查性能
            report.isACCOn();
            report.isPositioned();
            report.isOutOfService();

            // 测试附加信息解析性能
            report.getParsedAdditionalInfo();

            // 测试toString性能
            if (i % 100 == 0) { // 每100次调用一次toString
                report.toString();
            }
        }

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        assertTrue(duration < 5000, "1000次操作应该在5秒内完成，实际耗时: " + duration + "ms");

        logger.info("性能测试通过，1000次操作耗时: {}ms", duration);
    }
}