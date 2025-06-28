package com.jt808.protocol.message;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import com.jt808.protocol.message.T0200LocationReport;
import java.util.List;

/**
 * 报警标志位解析测试
 */
class AlarmFlagTest {
    
    @Test
    void testAlarmFlagParsing() {
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
        report.setAlarmFlag(0x04000007); // 车辆被盗 + 危险预警 + 超速 + 紧急
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
        
        System.out.println("报警标志位解析测试通过");
    }
    
    @Test
    void testAllAlarmFlags() {
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
        
        System.out.println("所有报警标志位测试通过，共测试了 " + alarmBits.length + " 个报警位");
        System.out.println("激活所有报警时的描述: " + String.join(", ", allActiveAlarms));
    }
    
    @Test
    void testSpecificAlarmMethods() {
        T0200LocationReport report = new T0200LocationReport();
        
        // 测试新增的报警方法
        report.setAlarmFlag(0x00000200); // LCD故障
        assertTrue(report.hasLCDFault(), "应该检测到LCD故障");
        
        report.setAlarmFlag(0x00000400); // TTS故障
        assertTrue(report.hasTTSFault(), "应该检测到TTS故障");
        
        report.setAlarmFlag(0x00000800); // 摄像头故障
        assertTrue(report.hasCameraFault(), "应该检测到摄像头故障");
        
        report.setAlarmFlag(0x00040000); // 当天累计驾驶超时
        assertTrue(report.hasDailyDrivingTimeout(), "应该检测到当天累计驾驶超时");
        
        report.setAlarmFlag(0x00080000); // 超时停车
        assertTrue(report.hasOvertimeParking(), "应该检测到超时停车");
        
        report.setAlarmFlag(0x00100000); // 进出区域报警
        assertTrue(report.hasAreaInOutAlarm(), "应该检测到进出区域报警");
        
        report.setAlarmFlag(0x00200000); // 进出路线报警
        assertTrue(report.hasRouteInOutAlarm(), "应该检测到进出路线报警");
        
        report.setAlarmFlag(0x00400000); // 路段行驶时间不足/过长
        assertTrue(report.hasRoadSectionTimeAlarm(), "应该检测到路段行驶时间报警");
        
        report.setAlarmFlag(0x00800000); // 路线偏离报警
        assertTrue(report.hasRouteDeviationAlarm(), "应该检测到路线偏离报警");
        
        report.setAlarmFlag(0x01000000); // 车辆VSS故障
        assertTrue(report.hasVSSFault(), "应该检测到VSS故障");
        
        report.setAlarmFlag(0x02000000); // 车辆油量异常
        assertTrue(report.hasFuelAbnormal(), "应该检测到油量异常");
        
        report.setAlarmFlag(0x08000000); // 车辆非法点火
        assertTrue(report.hasIllegalIgnition(), "应该检测到非法点火");
        
        report.setAlarmFlag(0x10000000); // 车辆非法位移
        assertTrue(report.hasIllegalDisplacement(), "应该检测到非法位移");
        
        report.setAlarmFlag(0x20000000); // 碰撞侧翻报警
        assertTrue(report.hasCollisionRolloverAlarm(), "应该检测到碰撞侧翻报警");
        
        report.setAlarmFlag(0x40000000); // 非法开门报警
        assertTrue(report.hasIllegalDoorOpenAlarm(), "应该检测到非法开门报警");
        
        System.out.println("特定报警方法测试通过");
    }
    
    @Test
    void testToStringWithAlarms() {
        T0200LocationReport report = new T0200LocationReport();
        
        // 测试无报警的toString
        report.setAlarmFlag(0x00000000);
        String noAlarmStr = report.toString();
        assertTrue(noAlarmStr.contains("无报警"), "无报警时toString应包含'无报警'");
        
        // 测试有报警的toString
        report.setAlarmFlag(0x00000003); // 紧急报警 + 超速报警
        String withAlarmStr = report.toString();
        assertTrue(withAlarmStr.contains("紧急报警"), "有报警时toString应包含报警描述");
        assertTrue(withAlarmStr.contains("超速报警"), "有报警时toString应包含报警描述");
        
        System.out.println("toString测试通过");
        System.out.println("无报警: " + noAlarmStr);
        System.out.println("有报警: " + withAlarmStr);
    }
}