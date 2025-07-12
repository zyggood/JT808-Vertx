package com.jt808.protocol.message;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 状态标志位解析测试
 * 测试T0200LocationReport中的32位状态标志位解析功能
 */
class StatusFlagTest {

    private static final Logger logger = LoggerFactory.getLogger(StatusFlagTest.class);

    private T0200LocationReport report;

    @BeforeEach
    void setUp() {
        report = new T0200LocationReport();
    }

    @Test
    void testBasicStatusFlags() {
        // 测试ACC开关状态（第0位）
        report.setStatusFlag(0x00000001);
        assertTrue(report.isACCOn(), "ACC应该为开启状态");

        report.setStatusFlag(0x00000000);
        assertFalse(report.isACCOn(), "ACC应该为关闭状态");

        // 测试定位状态（第1位）
        report.setStatusFlag(0x00000002);
        assertTrue(report.isPositioned(), "应该为已定位状态");

        report.setStatusFlag(0x00000000);
        assertFalse(report.isPositioned(), "应该为未定位状态");

        // 测试南北纬状态（第2位）
        report.setStatusFlag(0x00000004);
        assertTrue(report.isSouthLatitude(), "应该为南纬");

        report.setStatusFlag(0x00000000);
        assertFalse(report.isSouthLatitude(), "应该为北纬");

        // 测试东西经状态（第3位）
        report.setStatusFlag(0x00000008);
        assertTrue(report.isWestLongitude(), "应该为西经");

        report.setStatusFlag(0x00000000);
        assertFalse(report.isWestLongitude(), "应该为东经");

        // 测试运营状态（第4位）
        report.setStatusFlag(0x00000010);
        assertTrue(report.isOutOfService(), "应该为停运状态");

        report.setStatusFlag(0x00000000);
        assertFalse(report.isOutOfService(), "应该为运营状态");

        // 测试经纬度加密状态（第5位）
        report.setStatusFlag(0x00000020);
        assertTrue(report.isCoordinateEncrypted(), "经纬度应该为加密状态");

        report.setStatusFlag(0x00000000);
        assertFalse(report.isCoordinateEncrypted(), "经纬度应该为未加密状态");

        // 测试组合状态
        report.setStatusFlag(0x0000003F); // 前6位全部置1
        assertTrue(report.isACCOn(), "ACC应该开启");
        assertTrue(report.isPositioned(), "应该已定位");
        assertTrue(report.isSouthLatitude(), "应该为南纬");
        assertTrue(report.isWestLongitude(), "应该为西经");
        assertTrue(report.isOutOfService(), "应该为停运");
        assertTrue(report.isCoordinateEncrypted(), "经纬度应该加密");
    }

    @Test
    void testLoadStatus() {
        // 测试载重状态（第8-9位）
        report.setStatusFlag(0x00000300); // 满载 (第8-9位都为1)
        assertEquals(3, report.getLoadStatus(), "应该为满载状态");

        report.setStatusFlag(0x00000000); // 空车
        assertEquals(0, report.getLoadStatus(), "应该为空车状态");
    }

    @Test
    void testVehicleCircuitStatus() {
        // 测试车辆电路状态（第10-11位）

        // 测试油路状态（第10位）
        report.setStatusFlag(0x00000400);
        assertTrue(report.isOilCircuitDisconnected(), "油路应该为断开状态");

        report.setStatusFlag(0x00000000);
        assertFalse(report.isOilCircuitDisconnected(), "油路应该为正常状态");

        // 测试电路状态（第11位）
        report.setStatusFlag(0x00000800);
        assertTrue(report.isElectricCircuitDisconnected(), "电路应该为断开状态");

        report.setStatusFlag(0x00000000);
        assertFalse(report.isElectricCircuitDisconnected(), "电路应该为正常状态");

        // 测试组合状态
        report.setStatusFlag(0x00000C00); // 油路断开 + 电路断开
        assertTrue(report.isOilCircuitDisconnected(), "油路应该为断开状态");
        assertTrue(report.isElectricCircuitDisconnected(), "电路应该为断开状态");
    }

    @Test
    void testDoorStatus() {
        // 测试车门状态（第12-17位）

        // 测试车门加锁状态（第12位）
        report.setStatusFlag(0x00001000);
        assertTrue(report.isDoorLocked(), "车门应该为加锁状态");

        report.setStatusFlag(0x00000000);
        assertFalse(report.isDoorLocked(), "车门应该为解锁状态");

        // 测试前门状态（第13位）
        report.setStatusFlag(0x00002000);
        assertTrue(report.isDoor1Open(), "前门应该为开启状态");

        // 测试中门状态（第14位）
        report.setStatusFlag(0x00004000);
        assertTrue(report.isDoor2Open(), "中门应该为开启状态");

        // 测试后门状态（第15位）
        report.setStatusFlag(0x00008000);
        assertTrue(report.isDoor3Open(), "后门应该为开启状态");

        // 测试驾驶席门状态（第16位）
        report.setStatusFlag(0x00010000);
        assertTrue(report.isDoor4Open(), "驾驶席门应该为开启状态");

        // 测试自定义门状态（第17位）
        report.setStatusFlag(0x00020000);
        assertTrue(report.isDoor5Open(), "自定义门应该为开启状态");

        // 测试所有门都开启
        report.setStatusFlag(0x0003E000); // 第13-17位全部置1
        assertTrue(report.isDoor1Open(), "前门应该开启");
        assertTrue(report.isDoor2Open(), "中门应该开启");
        assertTrue(report.isDoor3Open(), "后门应该开启");
        assertTrue(report.isDoor4Open(), "驾驶席门应该开启");
        assertTrue(report.isDoor5Open(), "自定义门应该开启");
        assertFalse(report.isDoorLocked(), "车门不应该加锁");
    }

    @Test
    void testGNSSPositioningStatus() {
        // 测试GNSS定位系统状态（第18-21位）

        // 测试GPS定位（第18位）
        report.setStatusFlag(0x00040000);
        assertTrue(report.isGPSPositioning(), "应该使用GPS定位");
        assertFalse(report.isBeidouPositioning(), "不应该使用北斗定位");
        assertFalse(report.isGLONASSPositioning(), "不应该使用GLONASS定位");
        assertFalse(report.isGalileoPositioning(), "不应该使用Galileo定位");

        // 测试北斗定位（第19位）
        report.setStatusFlag(0x00080000);
        assertFalse(report.isGPSPositioning(), "不应该使用GPS定位");
        assertTrue(report.isBeidouPositioning(), "应该使用北斗定位");
        assertFalse(report.isGLONASSPositioning(), "不应该使用GLONASS定位");
        assertFalse(report.isGalileoPositioning(), "不应该使用Galileo定位");

        // 测试GLONASS定位（第20位）
        report.setStatusFlag(0x00100000);
        assertFalse(report.isGPSPositioning(), "不应该使用GPS定位");
        assertFalse(report.isBeidouPositioning(), "不应该使用北斗定位");
        assertTrue(report.isGLONASSPositioning(), "应该使用GLONASS定位");
        assertFalse(report.isGalileoPositioning(), "不应该使用Galileo定位");

        // 测试Galileo定位（第21位）
        report.setStatusFlag(0x00200000);
        assertFalse(report.isGPSPositioning(), "不应该使用GPS定位");
        assertFalse(report.isBeidouPositioning(), "不应该使用北斗定位");
        assertFalse(report.isGLONASSPositioning(), "不应该使用GLONASS定位");
        assertTrue(report.isGalileoPositioning(), "应该使用Galileo定位");

        // 测试多系统组合定位
        report.setStatusFlag(0x000C0000); // GPS + 北斗
        assertTrue(report.isGPSPositioning(), "应该使用GPS定位");
        assertTrue(report.isBeidouPositioning(), "应该使用北斗定位");
        assertFalse(report.isGLONASSPositioning(), "不应该使用GLONASS定位");
        assertFalse(report.isGalileoPositioning(), "不应该使用Galileo定位");

        // 测试所有系统组合
        report.setStatusFlag(0x003C0000); // GPS + 北斗 + GLONASS + Galileo
        assertTrue(report.isGPSPositioning(), "应该使用GPS定位");
        assertTrue(report.isBeidouPositioning(), "应该使用北斗定位");
        assertTrue(report.isGLONASSPositioning(), "应该使用GLONASS定位");
        assertTrue(report.isGalileoPositioning(), "应该使用Galileo定位");
    }

    @Test
    void testComplexStatusCombinations() {
        // 测试复杂状态组合

        // 场景1：车辆正常行驶状态
        // ACC开启 + 已定位 + 北纬 + 东经 + 运营中 + 未加密 + 空车 + 油路正常 + 电路正常 + 车门解锁
        report.setStatusFlag(0x00000003); // ACC开启 + 已定位
        assertTrue(report.isACCOn(), "ACC应该开启");
        assertTrue(report.isPositioned(), "应该已定位");
        assertFalse(report.isSouthLatitude(), "应该为北纬");
        assertFalse(report.isWestLongitude(), "应该为东经");
        assertFalse(report.isOutOfService(), "应该为运营状态");
        assertFalse(report.isCoordinateEncrypted(), "经纬度不应该加密");
        assertEquals(0, report.getLoadStatus(), "应该为空车");
        assertFalse(report.isOilCircuitDisconnected(), "油路应该正常");
        assertFalse(report.isElectricCircuitDisconnected(), "电路应该正常");
        assertFalse(report.isDoorLocked(), "车门不应该加锁");

        // 场景2：车辆停车状态
        // ACC关闭 + 已定位 + 南纬 + 西经 + 停运 + 加密 + 满载 + 油路断开 + 电路断开 + 车门加锁 + 所有门关闭
        report.setStatusFlag(0x00001F3E); // 复杂组合状态
        assertFalse(report.isACCOn(), "ACC应该关闭");
        assertTrue(report.isPositioned(), "应该已定位");
        assertTrue(report.isSouthLatitude(), "应该为南纬");
        assertTrue(report.isWestLongitude(), "应该为西经");
        assertTrue(report.isOutOfService(), "应该为停运状态");
        assertTrue(report.isCoordinateEncrypted(), "经纬度应该加密");
        assertEquals(3, report.getLoadStatus(), "应该为满载");
        assertTrue(report.isOilCircuitDisconnected(), "油路应该断开");
        assertTrue(report.isElectricCircuitDisconnected(), "电路应该断开");
        assertTrue(report.isDoorLocked(), "车门应该加锁");

        // 场景3：紧急状态
        // ACC开启 + 未定位 + 所有门开启
        report.setStatusFlag(0x0003E001); // ACC开启 + 所有门开启
        assertTrue(report.isACCOn(), "ACC应该开启");
        assertFalse(report.isPositioned(), "应该未定位");
        assertTrue(report.isDoor1Open(), "前门应该开启");
        assertTrue(report.isDoor2Open(), "中门应该开启");
        assertTrue(report.isDoor3Open(), "后门应该开启");
        assertTrue(report.isDoor4Open(), "驾驶席门应该开启");
        assertTrue(report.isDoor5Open(), "自定义门应该开启");
    }

    @Test
    void testStatusFlagBoundaryValues() {
        // 测试边界值

        // 测试全0
        report.setStatusFlag(0x00000000);
        assertFalse(report.isACCOn(), "所有状态应该为false");
        assertFalse(report.isPositioned(), "所有状态应该为false");
        assertFalse(report.isOutOfService(), "所有状态应该为false");
        assertEquals(0, report.getLoadStatus(), "所有状态应该为false");

        // 测试全1（在有效位范围内）
        report.setStatusFlag(0xFFFFFFFF);
        assertTrue(report.isACCOn(), "ACC应该开启");
        assertTrue(report.isPositioned(), "应该已定位");
        assertTrue(report.isWestLongitude(), "应该为西经");
        assertTrue(report.isSouthLatitude(), "应该为南纬");
        assertTrue(report.isOutOfService(), "应该为运营状态");
        assertEquals(3, report.getLoadStatus(), "应该为满载状态");
        assertTrue(report.isOilCircuitDisconnected(), "油路应该断开");
        assertTrue(report.isElectricCircuitDisconnected(), "电路应该断开");
        assertTrue(report.isDoorLocked(), "车门应该加锁");
        assertTrue(report.isDoor1Open(), "前门应该开启");
        assertTrue(report.isDoor2Open(), "中门应该开启");
        assertTrue(report.isDoor3Open(), "后门应该开启");
        assertTrue(report.isDoor4Open(), "驾驶席门应该开启");
        assertTrue(report.isDoor5Open(), "自定义门应该开启");
        assertTrue(report.isGPSPositioning(), "应该使用GPS定位");
        assertTrue(report.isBeidouPositioning(), "应该使用北斗定位");
        assertTrue(report.isGLONASSPositioning(), "应该使用GLONASS定位");
        assertTrue(report.isGalileoPositioning(), "应该使用Galileo定位");
    }

    @Test
    void testStatusFlagToString() {
        // 测试状态位在toString中的显示

        // 设置一些状态位
        int status = 0x00000001 | 0x00000002 | 0x00000010 | 0x00040000;
        // ACC开 + 已定位 + 运营中 + GPS定位
        report.setStatusFlag(status);

        String toStringResult = report.toString();

        // 验证toString包含状态位信息
        assertTrue(toStringResult.contains("ACC开关: 开"), "应该包含ACC状态");
        assertTrue(toStringResult.contains("定位状态: 已定位"), "应该包含定位状态");
        assertTrue(toStringResult.contains("运营状态: 停运"), "应该包含运营状态");
        assertTrue(toStringResult.contains("载重状态: 空车"), "应该包含载重状态");
        assertTrue(toStringResult.contains("GPS定位: 使用"), "应该包含GPS状态");

        logger.info("状态位toString测试:");
        logger.info(toStringResult);
    }

    @Test
    void testIndividualBitOperations() {
        // 测试单个位的操作

        for (int bit = 0; bit < 32; bit++) {
            int statusFlag = 1 << bit;
            report.setStatusFlag(statusFlag);

            // 验证toString不会抛出异常
            assertDoesNotThrow(() -> {
                String result = report.toString();
                assertNotNull(result, "toString结果不应为null");
                assertFalse(result.isEmpty(), "toString结果不应为空");
            }, "第" + bit + "位状态设置时toString不应抛出异常");
        }
    }

    @Test
    void testStatusFlagConsistency() {
        // 测试状态位设置和获取的一致性

        int[] testValues = {
                0x00000000,
                0x00000001,
                0x00000002,
                0x00000004,
                0x00000008,
                0x00000010,
                0x00000020,
                0x00000040,
                0x00000080,
                0x00000100,
                0x00000200,
                0x00000400,
                0x00000800,
                0x00001000,
                0x00002000,
                0x00040000,
                0x00080000,
                0x00100000,
                0x00200000,
                0x12345678,
                0xFFFFFFFF
        };

        for (int testValue : testValues) {
            report.setStatusFlag(testValue);
            assertEquals(testValue, report.getStatusFlag(),
                    "状态位设置和获取应该一致: 0x" + Integer.toHexString(testValue));
        }
    }
}