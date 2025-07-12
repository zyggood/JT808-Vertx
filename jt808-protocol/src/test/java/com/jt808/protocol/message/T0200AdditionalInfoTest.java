package com.jt808.protocol.message;

import com.jt808.protocol.message.additional.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * T0200LocationReport 附加信息测试类
 * 测试新的附加信息架构的功能
 */
public class T0200AdditionalInfoTest {

    private T0200LocationReport locationReport;

    @BeforeEach
    void setUp() {
        locationReport = new T0200LocationReport();
    }

    @Test
    void testMileageInfo() {
        // 创建里程信息
        MileageInfo mileageInfo = new MileageInfo(1234.5); // 1234.5 km
        locationReport.addAdditionalInfo(mileageInfo);

        // 验证获取里程信息
        assertEquals(1234.5, locationReport.getMileage(), 0.1);
        assertTrue(locationReport.hasAdditionalInfo(MileageInfo.class));

        // 验证通过ID获取
        AdditionalInfo info = locationReport.getAdditionalInfoById(0x01);
        assertNotNull(info);
        assertTrue(info instanceof MileageInfo);
        assertEquals(1234.5, ((MileageInfo) info).getMileageKm(), 0.1);
    }

    @Test
    void testFuelInfo() {
        // 创建油量信息
        FuelInfo fuelInfo = new FuelInfo(56.7); // 56.7 L
        locationReport.addAdditionalInfo(fuelInfo);

        // 验证获取油量信息
        assertEquals(56.7, locationReport.getFuelLevel(), 0.1);
        assertTrue(locationReport.hasAdditionalInfo(FuelInfo.class));
    }

    @Test
    void testRecordSpeedInfo() {
        // 创建行驶记录速度信息
        RecordSpeedInfo speedInfo = new RecordSpeedInfo(80.0); // 80.0 km/h
        locationReport.addAdditionalInfo(speedInfo);

        // 验证获取速度信息
        assertEquals(80.0, locationReport.getRecordSpeed(), 0.1);
        assertTrue(locationReport.hasAdditionalInfo(RecordSpeedInfo.class));
    }

    @Test
    void testManualAlarmEventInfo() {
        // 创建人工确认报警事件信息
        ManualAlarmEventInfo alarmInfo = new ManualAlarmEventInfo(123);
        locationReport.addAdditionalInfo(alarmInfo);

        // 验证获取报警事件ID
        assertEquals(123, locationReport.getManualAlarmEventId());
        assertTrue(locationReport.hasAdditionalInfo(ManualAlarmEventInfo.class));
    }

    @Test
    void testOverspeedAlarmInfo() {
        // 创建超速报警信息
        OverspeedAlarmInfo overspeedInfo = new OverspeedAlarmInfo((byte) 1, 456);
        locationReport.addAdditionalInfo(overspeedInfo);

        // 验证获取超速报警信息
        OverspeedAlarmInfo retrieved = locationReport.getOverspeedAlarmInfo();
        assertNotNull(retrieved);
        assertEquals(1, retrieved.getLocationType());
        assertEquals(456, retrieved.getAreaId());
    }

    @Test
    void testAreaRouteAlarmInfo() {
        // 创建进出区域/路线报警信息
        AreaRouteAlarmInfo areaInfo = new AreaRouteAlarmInfo((byte) 2, 789, (byte) 1);
        locationReport.addAdditionalInfo(areaInfo);

        // 验证获取进出区域/路线报警信息
        AreaRouteAlarmInfo retrieved = locationReport.getAreaRouteAlarmInfo();
        assertNotNull(retrieved);
        assertEquals(2, retrieved.getLocationType());
        assertEquals(789, retrieved.getAreaId());
        assertEquals(1, retrieved.getDirection());
    }

    @Test
    void testRouteTimeAlarmInfo() {
        // 创建路段行驶时间报警信息
        RouteTimeAlarmInfo routeInfo = new RouteTimeAlarmInfo(1001, 3600, (byte) 0);
        locationReport.addAdditionalInfo(routeInfo);

        // 验证获取路段行驶时间报警信息
        RouteTimeAlarmInfo retrieved = locationReport.getRouteTimeAlarmInfo();
        assertNotNull(retrieved);
        assertEquals(1001, retrieved.getRouteId());
        assertEquals(3600, retrieved.getDriveTime());
        assertEquals(0, retrieved.getResult());
    }

    @Test
    void testAnalogQuantityInfo() {
        // 创建模拟量信息
        AnalogQuantityInfo analogInfo = new AnalogQuantityInfo(1024, 2048);
        locationReport.addAdditionalInfo(analogInfo);

        // 验证获取模拟量信息
        AnalogQuantityInfo retrieved = locationReport.getAnalogQuantityInfo();
        assertNotNull(retrieved);
        assertEquals(1024, retrieved.getAD0());
        assertEquals(2048, retrieved.getAD1());
    }

    @Test
    void testSignalStrengthInfo() {
        // 创建信号强度信息
        SignalStrengthInfo signalInfo = new SignalStrengthInfo((byte) -75);
        locationReport.addAdditionalInfo(signalInfo);

        // 验证获取信号强度
        assertEquals(-75, locationReport.getSignalStrength());
        assertTrue(locationReport.hasAdditionalInfo(SignalStrengthInfo.class));
    }

    @Test
    void testSatelliteCountInfo() {
        // 创建卫星数信息
        SatelliteCountInfo satelliteInfo = new SatelliteCountInfo((byte) 12);
        locationReport.addAdditionalInfo(satelliteInfo);

        // 验证获取卫星数
        assertEquals(12, locationReport.getSatelliteCount());
        assertTrue(locationReport.hasAdditionalInfo(SatelliteCountInfo.class));
    }

    @Test
    void testMultipleAdditionalInfo() {
        // 添加多个附加信息
        locationReport.addAdditionalInfo(new MileageInfo(1234.5));
        locationReport.addAdditionalInfo(new FuelInfo(56.7));
        locationReport.addAdditionalInfo(new RecordSpeedInfo(80.0));
        locationReport.addAdditionalInfo(new SignalStrengthInfo((byte) -80));

        // 验证所有信息都存在
        assertEquals(4, locationReport.getAdditionalInfoList().size());
        assertNotNull(locationReport.getMileage());
        assertNotNull(locationReport.getFuelLevel());
        assertNotNull(locationReport.getRecordSpeed());
        assertNotNull(locationReport.getSignalStrength());
    }

    @Test
    void testRemoveAdditionalInfo() {
        // 添加附加信息
        locationReport.addAdditionalInfo(new MileageInfo(1234.5));
        locationReport.addAdditionalInfo(new FuelInfo(56.7));

        assertEquals(2, locationReport.getAdditionalInfoList().size());

        // 移除里程信息
        assertTrue(locationReport.removeAdditionalInfo(MileageInfo.class));
        assertEquals(1, locationReport.getAdditionalInfoList().size());
        assertNull(locationReport.getMileage());
        assertNotNull(locationReport.getFuelLevel());

        // 移除不存在的信息
        assertFalse(locationReport.removeAdditionalInfo(MileageInfo.class));
    }

    @Test
    void testRemoveAdditionalInfoById() {
        // 添加附加信息
        locationReport.addAdditionalInfo(new MileageInfo(1234.5));
        locationReport.addAdditionalInfo(new FuelInfo(56.7));

        assertEquals(2, locationReport.getAdditionalInfoList().size());

        // 通过ID移除里程信息
        assertTrue(locationReport.removeAdditionalInfoById(0x01));
        assertEquals(1, locationReport.getAdditionalInfoList().size());
        assertNull(locationReport.getMileage());
        assertNotNull(locationReport.getFuelLevel());
    }

    @Test
    void testClearAdditionalInfo() {
        // 添加附加信息
        locationReport.addAdditionalInfo(new MileageInfo(1234.5));
        locationReport.addAdditionalInfo(new FuelInfo(56.7));

        assertEquals(2, locationReport.getAdditionalInfoList().size());

        // 清空所有附加信息
        locationReport.clearAdditionalInfo();
        assertEquals(0, locationReport.getAdditionalInfoList().size());
        assertNull(locationReport.getMileage());
        assertNull(locationReport.getFuelLevel());
    }

    @Test
    void testToString() {
        // 添加一些附加信息
        locationReport.addAdditionalInfo(new MileageInfo(1234.5));
        locationReport.addAdditionalInfo(new FuelInfo(56.7));
        locationReport.addAdditionalInfo(new SignalStrengthInfo((byte) -75));

        // 验证toString包含附加信息
        String result = locationReport.toString();
        assertNotNull(result);
        assertTrue(result.contains("解析后的附加信息"));
        assertTrue(result.contains("里程"));
        assertTrue(result.contains("油量"));
        assertTrue(result.contains("信号强度"));
    }
}