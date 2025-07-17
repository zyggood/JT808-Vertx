package com.jt808.protocol.message;

import io.vertx.core.buffer.Buffer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * T8606SetRoute 测试类
 * 
 * @author JT808-Vertx
 */
class T8606SetRouteTest {
    
    private T8606SetRoute message;
    
    @BeforeEach
    void setUp() {
        message = new T8606SetRoute();
    }
    
    @Test
    void testMessageId() {
        assertEquals(0x8606, T8606SetRoute.MESSAGE_ID);
        assertEquals(0x8606, message.getMessageId());
    }
    
    @Test
    void testDefaultConstructor() {
        assertNotNull(message.getWaypoints());
        assertTrue(message.getWaypoints().isEmpty());
        assertEquals(0, message.getWaypointCount());
    }
    
    @Test
    void testParameterizedConstructor() {
        long routeId = 12345L;
        int routeAttribute = T8606SetRoute.ATTR_TIME_BASED | T8606SetRoute.ATTR_ENTER_ALARM_DRIVER;
        LocalDateTime startTime = LocalDateTime.of(2024, 1, 15, 10, 30, 0);
        LocalDateTime endTime = LocalDateTime.of(2024, 1, 15, 18, 30, 0);
        
        List<T8606SetRoute.RouteWaypoint> waypoints = new ArrayList<>();
        waypoints.add(new T8606SetRoute.RouteWaypoint(1L, 101L, 39906000L, 116407000L, 10, 0x03));
        
        T8606SetRoute msg = new T8606SetRoute(routeId, routeAttribute, startTime, endTime, waypoints);
        
        assertEquals(routeId, msg.getRouteId());
        assertEquals(routeAttribute, msg.getRouteAttribute());
        assertEquals(startTime, msg.getStartTime());
        assertEquals(endTime, msg.getEndTime());
        assertEquals(1, msg.getWaypointCount());
        assertEquals(1, msg.getWaypoints().size());
    }
    
    @Test
    void testAddWaypoint() {
        T8606SetRoute.RouteWaypoint waypoint1 = new T8606SetRoute.RouteWaypoint(1L, 101L, 39906000L, 116407000L, 10, 0x01);
        T8606SetRoute.RouteWaypoint waypoint2 = new T8606SetRoute.RouteWaypoint(2L, 102L, 39907000L, 116408000L, 15, 0x02);
        
        message.addWaypoint(waypoint1);
        assertEquals(1, message.getWaypointCount());
        assertEquals(1, message.getWaypoints().size());
        
        message.addWaypoint(waypoint2);
        assertEquals(2, message.getWaypointCount());
        assertEquals(2, message.getWaypoints().size());
        
        // 测试添加null
        message.addWaypoint(null);
        assertEquals(2, message.getWaypointCount());
    }
    
    @Test
    void testRouteAttributes() {
        message.setRouteAttribute(T8606SetRoute.ATTR_TIME_BASED | T8606SetRoute.ATTR_ENTER_ALARM_DRIVER);
        
        assertTrue(message.hasTimeAttribute());
        assertTrue(message.hasAttribute(T8606SetRoute.ATTR_TIME_BASED));
        assertTrue(message.hasAttribute(T8606SetRoute.ATTR_ENTER_ALARM_DRIVER));
        assertFalse(message.hasAttribute(T8606SetRoute.ATTR_EXIT_ALARM_PLATFORM));
        
        String description = message.getRouteAttributeDescription();
        assertTrue(description.contains("根据时间"));
        assertTrue(description.contains("进路线报警给驾驶员"));
    }
    
    @Test
    void testEncodeDecodeBasic() {
        // 设置基本数据
        message.setRouteId(12345L);
        message.setRouteAttribute(T8606SetRoute.ATTR_TIME_BASED);
        message.setStartTime(LocalDateTime.of(2024, 1, 15, 10, 30, 0));
        message.setEndTime(LocalDateTime.of(2024, 1, 15, 18, 30, 0));
        
        // 添加一个简单拐点（无可选字段）
        T8606SetRoute.RouteWaypoint waypoint = new T8606SetRoute.RouteWaypoint(
            1L, 101L, 39906000L, 116407000L, 10, 0x00);
        message.addWaypoint(waypoint);
        
        // 编码
        Buffer encoded = message.encodeBody();
        assertNotNull(encoded);
        assertTrue(encoded.length() >= 20); // 基本长度 + 拐点长度
        
        // 解码
        T8606SetRoute decoded = new T8606SetRoute();
        decoded.decodeBody(encoded);
        
        assertEquals(message.getRouteId(), decoded.getRouteId());
        assertEquals(message.getRouteAttribute(), decoded.getRouteAttribute());
        assertEquals(message.getStartTime(), decoded.getStartTime());
        assertEquals(message.getEndTime(), decoded.getEndTime());
        assertEquals(message.getWaypointCount(), decoded.getWaypointCount());
        assertEquals(1, decoded.getWaypoints().size());
        
        T8606SetRoute.RouteWaypoint decodedWaypoint = decoded.getWaypoints().get(0);
        assertEquals(waypoint.getWaypointId(), decodedWaypoint.getWaypointId());
        assertEquals(waypoint.getSegmentId(), decodedWaypoint.getSegmentId());
        assertEquals(waypoint.getLatitude(), decodedWaypoint.getLatitude());
        assertEquals(waypoint.getLongitude(), decodedWaypoint.getLongitude());
    }
    
    @Test
    void testEncodeDecodeWithOptionalFields() {
        // 设置基本数据
        message.setRouteId(54321L);
        message.setRouteAttribute(T8606SetRoute.ATTR_ENTER_ALARM_PLATFORM);
        
        // 添加带可选字段的拐点
        T8606SetRoute.RouteWaypoint waypoint = new T8606SetRoute.RouteWaypoint(
            2L, 202L, 31230000L, 121470000L, 20, 
            T8606SetRoute.RouteWaypoint.SEGMENT_ATTR_DRIVING_TIME | T8606SetRoute.RouteWaypoint.SEGMENT_ATTR_SPEED_LIMIT);
        waypoint.setDrivingOverThreshold(300);
        waypoint.setDrivingUnderThreshold(60);
        waypoint.setMaxSpeed(80);
        waypoint.setOverspeedDuration(10);
        
        message.addWaypoint(waypoint);
        
        // 编码
        Buffer encoded = message.encodeBody();
        assertNotNull(encoded);
        
        // 解码
        T8606SetRoute decoded = new T8606SetRoute();
        decoded.decodeBody(encoded);
        
        assertEquals(message.getRouteId(), decoded.getRouteId());
        assertEquals(1, decoded.getWaypoints().size());
        
        T8606SetRoute.RouteWaypoint decodedWaypoint = decoded.getWaypoints().get(0);
        assertTrue(decodedWaypoint.hasDrivingTimeAttribute());
        assertTrue(decodedWaypoint.hasSpeedLimitAttribute());
        assertEquals(waypoint.getDrivingOverThreshold(), decodedWaypoint.getDrivingOverThreshold());
        assertEquals(waypoint.getDrivingUnderThreshold(), decodedWaypoint.getDrivingUnderThreshold());
        assertEquals(waypoint.getMaxSpeed(), decodedWaypoint.getMaxSpeed());
        assertEquals(waypoint.getOverspeedDuration(), decodedWaypoint.getOverspeedDuration());
    }
    
    @Test
    void testEncodeDecodeMultipleWaypoints() {
        message.setRouteId(99999L);
        message.setRouteAttribute(T8606SetRoute.ATTR_EXIT_ALARM_DRIVER);
        
        // 添加多个拐点
        for (int i = 1; i <= 3; i++) {
            T8606SetRoute.RouteWaypoint waypoint = new T8606SetRoute.RouteWaypoint(
                i, 100 + i, 39900000L + i * 1000, 116400000L + i * 1000, 10 + i, 0x00);
            message.addWaypoint(waypoint);
        }
        
        // 编码解码
        Buffer encoded = message.encodeBody();
        T8606SetRoute decoded = new T8606SetRoute();
        decoded.decodeBody(encoded);
        
        assertEquals(3, decoded.getWaypointCount());
        assertEquals(3, decoded.getWaypoints().size());
        
        for (int i = 0; i < 3; i++) {
            T8606SetRoute.RouteWaypoint waypoint = decoded.getWaypoints().get(i);
            assertEquals(i + 1, waypoint.getWaypointId());
            assertEquals(101 + i, waypoint.getSegmentId());
        }
    }
    
    @Test
    void testWaypointCoordinateConversion() {
        T8606SetRoute.RouteWaypoint waypoint = new T8606SetRoute.RouteWaypoint();
        
        // 测试纬度转换
        waypoint.setLatitudeDegrees(39.906);
        assertEquals(39906000L, waypoint.getLatitude());
        assertEquals(39.906, waypoint.getLatitudeDegrees(), 0.000001);
        
        // 测试经度转换
        waypoint.setLongitudeDegrees(116.407);
        assertEquals(116407000L, waypoint.getLongitude());
        assertEquals(116.407, waypoint.getLongitudeDegrees(), 0.000001);
    }
    
    @Test
    void testWaypointAttributes() {
        T8606SetRoute.RouteWaypoint waypoint = new T8606SetRoute.RouteWaypoint();
        
        // 测试行驶时间属性
        waypoint.setSegmentAttribute(T8606SetRoute.RouteWaypoint.SEGMENT_ATTR_DRIVING_TIME);
        assertTrue(waypoint.hasDrivingTimeAttribute());
        assertFalse(waypoint.hasSpeedLimitAttribute());
        
        // 测试限速属性
        waypoint.setSegmentAttribute(T8606SetRoute.RouteWaypoint.SEGMENT_ATTR_SPEED_LIMIT);
        assertFalse(waypoint.hasDrivingTimeAttribute());
        assertTrue(waypoint.hasSpeedLimitAttribute());
        
        // 测试组合属性
        waypoint.setSegmentAttribute(
            T8606SetRoute.RouteWaypoint.SEGMENT_ATTR_DRIVING_TIME | 
            T8606SetRoute.RouteWaypoint.SEGMENT_ATTR_SPEED_LIMIT |
            T8606SetRoute.RouteWaypoint.SEGMENT_ATTR_SOUTH_LATITUDE);
        assertTrue(waypoint.hasDrivingTimeAttribute());
        assertTrue(waypoint.hasSpeedLimitAttribute());
        assertTrue(waypoint.hasAttribute(T8606SetRoute.RouteWaypoint.SEGMENT_ATTR_SOUTH_LATITUDE));
        
        String description = waypoint.getSegmentAttributeDescription();
        assertTrue(description.contains("行驶时间"));
        assertTrue(description.contains("限速"));
        assertTrue(description.contains("南纬"));
    }
    
    @Test
    void testConstants() {
        // 测试路线属性常量
        assertEquals(0x0001, T8606SetRoute.ATTR_TIME_BASED);
        assertEquals(0x0004, T8606SetRoute.ATTR_ENTER_ALARM_DRIVER);
        assertEquals(0x0008, T8606SetRoute.ATTR_ENTER_ALARM_PLATFORM);
        assertEquals(0x0010, T8606SetRoute.ATTR_EXIT_ALARM_DRIVER);
        assertEquals(0x0020, T8606SetRoute.ATTR_EXIT_ALARM_PLATFORM);
        
        // 测试路段属性常量
        assertEquals(0x01, T8606SetRoute.RouteWaypoint.SEGMENT_ATTR_DRIVING_TIME);
        assertEquals(0x02, T8606SetRoute.RouteWaypoint.SEGMENT_ATTR_SPEED_LIMIT);
        assertEquals(0x04, T8606SetRoute.RouteWaypoint.SEGMENT_ATTR_SOUTH_LATITUDE);
        assertEquals(0x08, T8606SetRoute.RouteWaypoint.SEGMENT_ATTR_WEST_LONGITUDE);
    }
    
    @Test
    void testBoundaryValues() {
        // 测试边界值
        message.setRouteId(0xFFFFFFFFL); // 最大DWORD值
        message.setRouteAttribute(0xFFFF); // 最大WORD值
        
        T8606SetRoute.RouteWaypoint waypoint = new T8606SetRoute.RouteWaypoint();
        waypoint.setWaypointId(0xFFFFFFFFL);
        waypoint.setSegmentId(0xFFFFFFFFL);
        waypoint.setLatitude(0xFFFFFFFFL);
        waypoint.setLongitude(0xFFFFFFFFL);
        waypoint.setSegmentWidth(255); // 最大BYTE值
        waypoint.setSegmentAttribute(255);
        
        message.addWaypoint(waypoint);
        
        // 编码解码测试
        Buffer encoded = message.encodeBody();
        T8606SetRoute decoded = new T8606SetRoute();
        decoded.decodeBody(encoded);
        
        assertEquals(message.getRouteId(), decoded.getRouteId());
        assertEquals(message.getRouteAttribute(), decoded.getRouteAttribute());
        
        T8606SetRoute.RouteWaypoint decodedWaypoint = decoded.getWaypoints().get(0);
        assertEquals(waypoint.getWaypointId(), decodedWaypoint.getWaypointId());
        assertEquals(waypoint.getSegmentWidth(), decodedWaypoint.getSegmentWidth());
    }
    
    @Test
    void testRealWorldScenario() {
        // 模拟真实场景：北京到上海的路线
        message.setRouteId(888888L);
        message.setRouteAttribute(
            T8606SetRoute.ATTR_TIME_BASED | 
            T8606SetRoute.ATTR_ENTER_ALARM_PLATFORM |
            T8606SetRoute.ATTR_EXIT_ALARM_PLATFORM);
        message.setStartTime(LocalDateTime.of(2024, 3, 15, 8, 0, 0));
        message.setEndTime(LocalDateTime.of(2024, 3, 15, 20, 0, 0));
        
        // 北京出发点
        T8606SetRoute.RouteWaypoint waypoint1 = new T8606SetRoute.RouteWaypoint(
            1001L, 2001L, 39906000L, 116407000L, 50, 
            T8606SetRoute.RouteWaypoint.SEGMENT_ATTR_SPEED_LIMIT);
        waypoint1.setMaxSpeed(120);
        waypoint1.setOverspeedDuration(5);
        message.addWaypoint(waypoint1);
        
        // 中途点（天津）
        T8606SetRoute.RouteWaypoint waypoint2 = new T8606SetRoute.RouteWaypoint(
            1002L, 2002L, 39084000L, 117200000L, 40,
            T8606SetRoute.RouteWaypoint.SEGMENT_ATTR_DRIVING_TIME | T8606SetRoute.RouteWaypoint.SEGMENT_ATTR_SPEED_LIMIT);
        waypoint2.setDrivingOverThreshold(7200); // 2小时
        waypoint2.setDrivingUnderThreshold(3600); // 1小时
        waypoint2.setMaxSpeed(100);
        waypoint2.setOverspeedDuration(3);
        message.addWaypoint(waypoint2);
        
        // 上海终点
        T8606SetRoute.RouteWaypoint waypoint3 = new T8606SetRoute.RouteWaypoint(
            1003L, 2003L, 31230000L, 121470000L, 30, 0x00);
        message.addWaypoint(waypoint3);
        
        // 验证设置
        assertEquals(3, message.getWaypointCount());
        assertTrue(message.hasTimeAttribute());
        assertTrue(message.hasAttribute(T8606SetRoute.ATTR_ENTER_ALARM_PLATFORM));
        
        // 编码解码验证
        Buffer encoded = message.encodeBody();
        T8606SetRoute decoded = new T8606SetRoute();
        decoded.decodeBody(encoded);
        
        assertEquals(message.getRouteId(), decoded.getRouteId());
        assertEquals(3, decoded.getWaypointCount());
        
        // 验证第二个拐点的可选字段
        T8606SetRoute.RouteWaypoint decodedWaypoint2 = decoded.getWaypoints().get(1);
        assertTrue(decodedWaypoint2.hasDrivingTimeAttribute());
        assertTrue(decodedWaypoint2.hasSpeedLimitAttribute());
        assertEquals(7200, decodedWaypoint2.getDrivingOverThreshold());
        assertEquals(100, decodedWaypoint2.getMaxSpeed());
    }
    
    @Test
    void testInvalidData() {
        // 测试消息体长度不足
        Buffer shortBuffer = Buffer.buffer(new byte[10]); // 少于最小20字节
        assertThrows(IllegalArgumentException.class, () -> {
            message.decodeBody(shortBuffer);
        });
        
        // 测试拐点数据不足
        Buffer buffer = Buffer.buffer();
        buffer.appendUnsignedInt(12345L); // 路线ID
        buffer.appendUnsignedShort(0x01); // 路线属性
        buffer.appendBytes(new byte[12]); // 时间字段
        buffer.appendUnsignedShort(1); // 拐点数
        buffer.appendBytes(new byte[10]); // 不足的拐点数据
        
        assertThrows(IllegalArgumentException.class, () -> {
            message.decodeBody(buffer);
        });
    }
    
    @Test
    void testNullTimeHandling() {
        message.setRouteId(12345L);
        message.setStartTime(null);
        message.setEndTime(null);
        
        // 编码时应该填充0
        Buffer encoded = message.encodeBody();
        assertNotNull(encoded);
        
        // 解码后时间应该为null或默认值
        T8606SetRoute decoded = new T8606SetRoute();
        decoded.decodeBody(encoded);
        // 时间解码可能返回null或默认时间，这取决于实现
    }
    
    @Test
    void testDescription() {
        message.setRouteId(12345L);
        message.addWaypoint(new T8606SetRoute.RouteWaypoint());
        message.addWaypoint(new T8606SetRoute.RouteWaypoint());
        
        String description = message.getDescription();
        assertTrue(description.contains("12345"));
        assertTrue(description.contains("2"));
    }
    
    @Test
    void testToString() {
        message.setRouteId(12345L);
        message.setRouteAttribute(0x0005);
        message.setWaypointCount(2);
        
        String str = message.toString();
        assertTrue(str.contains("T8606SetRoute"));
        assertTrue(str.contains("12345"));
        assertTrue(str.contains("0x0005"));
        assertTrue(str.contains("2"));
    }
    
    @Test
    void testWaypointToString() {
        T8606SetRoute.RouteWaypoint waypoint = new T8606SetRoute.RouteWaypoint(
            1L, 101L, 39906000L, 116407000L, 10, 0x03);
        
        String str = waypoint.toString();
        assertTrue(str.contains("RouteWaypoint"));
        assertTrue(str.contains("1"));
        assertTrue(str.contains("101"));
        assertTrue(str.contains("39.906"));
        assertTrue(str.contains("116.407"));
    }
    
    @Test
    void testEqualsAndHashCode() {
        T8606SetRoute msg1 = new T8606SetRoute();
        msg1.setRouteId(12345L);
        msg1.setRouteAttribute(0x01);
        msg1.setStartTime(LocalDateTime.of(2024, 1, 1, 10, 0, 0));
        
        T8606SetRoute msg2 = new T8606SetRoute();
        msg2.setRouteId(12345L);
        msg2.setRouteAttribute(0x01);
        msg2.setStartTime(LocalDateTime.of(2024, 1, 1, 10, 0, 0));
        
        assertEquals(msg1, msg2);
        assertEquals(msg1.hashCode(), msg2.hashCode());
        
        // 修改一个属性
        msg2.setRouteId(54321L);
        assertNotEquals(msg1, msg2);
    }
    
    @Test
    void testWaypointEqualsAndHashCode() {
        T8606SetRoute.RouteWaypoint wp1 = new T8606SetRoute.RouteWaypoint(
            1L, 101L, 39906000L, 116407000L, 10, 0x01);
        wp1.setMaxSpeed(80);
        
        T8606SetRoute.RouteWaypoint wp2 = new T8606SetRoute.RouteWaypoint(
            1L, 101L, 39906000L, 116407000L, 10, 0x01);
        wp2.setMaxSpeed(80);
        
        assertEquals(wp1, wp2);
        assertEquals(wp1.hashCode(), wp2.hashCode());
        
        // 修改一个属性
        wp2.setMaxSpeed(100);
        assertNotEquals(wp1, wp2);
    }
    
    @Test
    void testSetWaypoints() {
        List<T8606SetRoute.RouteWaypoint> waypoints = new ArrayList<>();
        waypoints.add(new T8606SetRoute.RouteWaypoint(1L, 101L, 39906000L, 116407000L, 10, 0x00));
        waypoints.add(new T8606SetRoute.RouteWaypoint(2L, 102L, 39907000L, 116408000L, 15, 0x00));
        
        message.setWaypoints(waypoints);
        assertEquals(2, message.getWaypointCount());
        assertEquals(2, message.getWaypoints().size());
        
        // 测试设置null
        message.setWaypoints(null);
        assertEquals(0, message.getWaypointCount());
        assertTrue(message.getWaypoints().isEmpty());
    }
}