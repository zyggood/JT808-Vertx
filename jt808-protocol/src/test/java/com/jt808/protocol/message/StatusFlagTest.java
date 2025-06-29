package com.jt808.protocol.message;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

/**
 * 状态标志位解析测试
 * 测试T0200LocationReport中的32位状态标志位解析功能
 */
class StatusFlagTest {
    
    private T0200LocationReport report;
    
    @BeforeEach
    void setUp() {
        report = new T0200LocationReport();
    }
    
    @Test
    void testBasicStatusFlags() {
        // 测试基本状态位
        
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
        
        // 测试经纬度方向（第2-3位）
        report.setStatusFlag(0x00000004); // 西经
        assertTrue(report.isWestLongitude(), "应该为西经");
        assertFalse(report.isSouthLatitude(), "不应该为南纬");
        
        report.setStatusFlag(0x00000008); // 南纬
        assertFalse(report.isWestLongitude(), "不应该为西经");
        assertTrue(report.isSouthLatitude(), "应该为南纬");
        
        report.setStatusFlag(0x0000000C); // 西经 + 南纬
        assertTrue(report.isWestLongitude(), "应该为西经");
        assertTrue(report.isSouthLatitude(), "应该为南纬");
        
        // 测试运营状态（第4位）
        report.setStatusFlag(0x00000010);
        assertFalse(report.isOutOfService(), "应该为运营状态");
        
        report.setStatusFlag(0x00000000);
        assertTrue(report.isOutOfService(), "应该为停运状态");
    }
    
    @Test
    void testLoadStatus() {
        // 测试载重状态（第5位）
        report.setStatusFlag(0x00000020); // 满载
        assertEquals(3, report.getLoadStatus(), "应该为满载状态");
        
        report.setStatusFlag(0x00000000); // 空车
        assertEquals(0, report.getLoadStatus(), "应该为空车状态");
    }
    
    @Test
    void testVehicleCircuitStatus() {
        // 测试车辆电路状态（第6-7位）
        
        // 测试油路状态（第6位）
        report.setStatusFlag(0x00000040);
        assertTrue(report.isOilCircuitDisconnected(), "油路应该为断开状态");
        
        report.setStatusFlag(0x00000000);
        assertFalse(report.isOilCircuitDisconnected(), "油路应该为正常状态");
        
        // 测试电路状态（第7位）
        report.setStatusFlag(0x00000080);
        assertTrue(report.isElectricCircuitDisconnected(), "电路应该为断开状态");
        
        report.setStatusFlag(0x00000000);
        assertFalse(report.isElectricCircuitDisconnected(), "电路应该为正常状态");
        
        // 测试组合状态
        report.setStatusFlag(0x000000C0); // 油路断开 + 电路断开
        assertTrue(report.isOilCircuitDisconnected(), "油路应该为断开状态");
        assertTrue(report.isElectricCircuitDisconnected(), "电路应该为断开状态");
    }
    
    @Test
    void testDoorStatus() {
        // 测试车门状态（第8-13位）
        
        // 测试车门加锁状态（第8位）
        report.setStatusFlag(0x00000100);
        assertTrue(report.isDoorLocked(), "车门应该为加锁状态");
        
        report.setStatusFlag(0x00000000);
        assertFalse(report.isDoorLocked(), "车门应该为解锁状态");
        
        // 测试前门状态（第9位）
        report.setStatusFlag(0x00000200);
        assertTrue(report.isDoor1Open(), "前门应该为开启状态");
        
        // 测试中门状态（第10位）
        report.setStatusFlag(0x00000400);
        assertTrue(report.isDoor2Open(), "中门应该为开启状态");
        
        // 测试后门状态（第11位）
        report.setStatusFlag(0x00000800);
        assertTrue(report.isDoor3Open(), "后门应该为开启状态");
        
        // 测试驾驶席门状态（第12位）
        report.setStatusFlag(0x00001000);
        assertTrue(report.isDoor4Open(), "驾驶席门应该为开启状态");
        
        // 测试自定义门状态（第13位）
        report.setStatusFlag(0x00002000);
        assertTrue(report.isDoor5Open(), "自定义门应该为开启状态");
        
        // 测试所有门都开启
        report.setStatusFlag(0x00003E00); // 第9-13位全部置1
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
        // 测试复杂的状态位组合
        
        // 组合1: ACC开 + 已定位 + 运营中 + 满载 + GPS定位
        int status1 = 0x00000001 | 0x00000002 | 0x00000010 | 0x00000020 | 0x00040000;
        report.setStatusFlag(status1);
        
        assertTrue(report.isACCOn(), "ACC应该开启");
        assertTrue(report.isPositioned(), "应该已定位");
        assertFalse(report.isOutOfService(), "应该为运营状态");
        assertEquals(3, report.getLoadStatus(), "应该为满载状态");
        assertTrue(report.isGPSPositioning(), "应该使用GPS定位");
        
        assertFalse(report.isWestLongitude(), "不应该为西经");
        assertFalse(report.isSouthLatitude(), "不应该为南纬");
        assertFalse(report.isOilCircuitDisconnected(), "油路不应该断开");
        assertFalse(report.isElectricCircuitDisconnected(), "电路不应该断开");
        
        // 组合2: 西经南纬 + 所有门开启 + 多系统定位
        int status2 = 0x0000000C | 0x00003E00 | 0x003C0000;
        report.setStatusFlag(status2);
        
        assertTrue(report.isWestLongitude(), "应该为西经");
        assertTrue(report.isSouthLatitude(), "应该为南纬");
        assertTrue(report.isDoor1Open(), "前门应该开启");
        assertTrue(report.isDoor2Open(), "中门应该开启");
        assertTrue(report.isDoor3Open(), "后门应该开启");
        assertTrue(report.isDoor4Open(), "驾驶席门应该开启");
        assertTrue(report.isDoor5Open(), "自定义门应该开启");
        assertTrue(report.isGPSPositioning(), "应该使用GPS定位");
        assertTrue(report.isBeidouPositioning(), "应该使用北斗定位");
        assertTrue(report.isGLONASSPositioning(), "应该使用GLONASS定位");
        assertTrue(report.isGalileoPositioning(), "应该使用Galileo定位");
        
        assertFalse(report.isACCOn(), "ACC不应该开启");
        assertFalse(report.isPositioned(), "不应该已定位");
        assertFalse(report.isDoorLocked(), "车门不应该加锁");
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
        assertTrue(toStringResult.contains("ACC状态: 开启"), "应该包含ACC状态");
        assertTrue(toStringResult.contains("定位状态: 已定位"), "应该包含定位状态");
        assertTrue(toStringResult.contains("运营状态: 运营中"), "应该包含运营状态");
        assertTrue(toStringResult.contains("载重状态: 空车"), "应该包含载重状态");
        assertTrue(toStringResult.contains("GPS: 是"), "应该包含GPS状态");
        
        System.out.println("状态位toString测试:");
        System.out.println(toStringResult);
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