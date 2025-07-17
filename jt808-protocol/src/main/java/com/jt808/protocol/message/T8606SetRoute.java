package com.jt808.protocol.message;

import io.vertx.core.buffer.Buffer;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 0x8606 设置路线消息
 * 平台→终端
 * 
 * 消息体包含：
 * - 路线ID (DWORD)
 * - 路线属性 (WORD)
 * - 起始时间 (BCD[6])
 * - 结束时间 (BCD[6])
 * - 路线总拐点数 (WORD)
 * - 拐点项列表 (路线拐点项)
 * 
 * @author JT808-Vertx
 */
public class T8606SetRoute extends JT808Message {
    
    /** 消息ID */
    public static final int MESSAGE_ID = 0x8606;
    
    // 路线属性位标志常量
    /** 根据时间 */
    public static final int ATTR_TIME_BASED = 0x0001;
    /** 进路线报警给驾驶员 */
    public static final int ATTR_ENTER_ALARM_DRIVER = 0x0004;
    /** 进路线报警给平台 */
    public static final int ATTR_ENTER_ALARM_PLATFORM = 0x0008;
    /** 出路线报警给驾驶员 */
    public static final int ATTR_EXIT_ALARM_DRIVER = 0x0010;
    /** 出路线报警给平台 */
    public static final int ATTR_EXIT_ALARM_PLATFORM = 0x0020;
    
    /** 路线ID */
    private long routeId;
    
    /** 路线属性 */
    private int routeAttribute;
    
    /** 起始时间 */
    private LocalDateTime startTime;
    
    /** 结束时间 */
    private LocalDateTime endTime;
    
    /** 路线总拐点数 */
    private int waypointCount;
    
    /** 拐点项列表 */
    private List<RouteWaypoint> waypoints;
    
    /**
     * 默认构造函数
     */
    public T8606SetRoute() {
        this.waypoints = new ArrayList<>();
    }
    
    /**
     * 带参数的构造函数
     * 
     * @param routeId 路线ID
     * @param routeAttribute 路线属性
     * @param startTime 起始时间
     * @param endTime 结束时间
     * @param waypoints 拐点列表
     */
    public T8606SetRoute(long routeId, int routeAttribute, LocalDateTime startTime, 
                        LocalDateTime endTime, List<RouteWaypoint> waypoints) {
        this.routeId = routeId;
        this.routeAttribute = routeAttribute;
        this.startTime = startTime;
        this.endTime = endTime;
        this.waypoints = waypoints != null ? new ArrayList<>(waypoints) : new ArrayList<>();
        this.waypointCount = this.waypoints.size();
    }
    
    @Override
    public int getMessageId() {
        return MESSAGE_ID;
    }
    
    @Override
    public Buffer encodeBody() {
        Buffer buffer = Buffer.buffer();
        
        // 路线ID (DWORD)
        buffer.appendUnsignedInt(routeId);
        
        // 路线属性 (WORD)
        buffer.appendUnsignedShort(routeAttribute);
        
        // 起始时间 (BCD[6])
        if (startTime != null) {
            buffer.appendBuffer(encodeBcdTime(startTime));
        } else {
            buffer.appendBytes(new byte[6]); // 填充0
        }
        
        // 结束时间 (BCD[6])
        if (endTime != null) {
            buffer.appendBuffer(encodeBcdTime(endTime));
        } else {
            buffer.appendBytes(new byte[6]); // 填充0
        }
        
        // 路线总拐点数 (WORD)
        buffer.appendUnsignedShort(waypointCount);
        
        // 拐点项列表
        for (RouteWaypoint waypoint : waypoints) {
            buffer.appendBuffer(waypoint.encode());
        }
        
        return buffer;
    }
    
    @Override
    public void decodeBody(Buffer buffer) {
        if (buffer.length() < 20) { // 最小长度：4+2+6+6+2 = 20字节
            throw new IllegalArgumentException("消息体长度不足，至少需要20字节");
        }
        
        int index = 0;
        
        // 路线ID (DWORD)
        this.routeId = buffer.getUnsignedInt(index);
        index += 4;
        
        // 路线属性 (WORD)
        this.routeAttribute = buffer.getUnsignedShort(index);
        index += 2;
        
        // 起始时间 (BCD[6])
        this.startTime = decodeBcdTime(buffer, index);
        index += 6;
        
        // 结束时间 (BCD[6])
        this.endTime = decodeBcdTime(buffer, index);
        index += 6;
        
        // 路线总拐点数 (WORD)
        this.waypointCount = buffer.getUnsignedShort(index);
        index += 2;
        
        // 拐点项列表
        this.waypoints.clear();
        for (int i = 0; i < waypointCount; i++) {
            if (index >= buffer.length()) {
                throw new IllegalArgumentException(
                    String.format("消息体长度不足，无法读取第%d个拐点", i + 1));
            }
            
            RouteWaypoint waypoint = new RouteWaypoint();
            index = waypoint.decode(buffer, index);
            this.waypoints.add(waypoint);
        }
    }
    
    /**
     * 添加拐点
     * 
     * @param waypoint 拐点
     */
    public void addWaypoint(RouteWaypoint waypoint) {
        if (waypoint != null) {
            this.waypoints.add(waypoint);
            this.waypointCount = this.waypoints.size();
        }
    }
    
    /**
     * 判断是否有时间属性
     * 
     * @return 如果有时间属性返回true，否则返回false
     */
    public boolean hasTimeAttribute() {
        return (routeAttribute & ATTR_TIME_BASED) != 0;
    }
    
    /**
     * 判断是否有指定属性
     * 
     * @param attribute 属性标志
     * @return 如果有指定属性返回true，否则返回false
     */
    public boolean hasAttribute(int attribute) {
        return (routeAttribute & attribute) != 0;
    }
    
    /**
     * 获取路线属性描述
     * 
     * @return 路线属性描述
     */
    public String getRouteAttributeDescription() {
        StringBuilder sb = new StringBuilder();
        if (hasAttribute(ATTR_TIME_BASED)) sb.append("根据时间 ");
        if (hasAttribute(ATTR_ENTER_ALARM_DRIVER)) sb.append("进路线报警给驾驶员 ");
        if (hasAttribute(ATTR_ENTER_ALARM_PLATFORM)) sb.append("进路线报警给平台 ");
        if (hasAttribute(ATTR_EXIT_ALARM_DRIVER)) sb.append("出路线报警给驾驶员 ");
        if (hasAttribute(ATTR_EXIT_ALARM_PLATFORM)) sb.append("出路线报警给平台 ");
        return sb.toString().trim();
    }
    
    /**
     * 获取消息描述
     * 
     * @return 消息描述
     */
    public String getDescription() {
        return String.format("设置路线消息 - 路线ID: %d, 拐点数: %d", routeId, waypointCount);
    }
    
    /**
     * 编码BCD时间
     * 
     * @param time 时间
     * @return 编码后的Buffer
     */
    private Buffer encodeBcdTime(LocalDateTime time) {
        Buffer buffer = Buffer.buffer();
        if (time != null) {
            String timeStr = time.format(DateTimeFormatter.ofPattern("yyMMddHHmmss"));
            for (int i = 0; i < timeStr.length(); i += 2) {
                int high = Character.getNumericValue(timeStr.charAt(i));
                int low = Character.getNumericValue(timeStr.charAt(i + 1));
                buffer.appendByte((byte) ((high << 4) | low));
            }
        } else {
            buffer.appendBytes(new byte[6]);
        }
        return buffer;
    }
    
    /**
     * 解码BCD时间
     * 
     * @param buffer 数据缓冲区
     * @param index 起始索引
     * @return 解码后的时间
     */
    private LocalDateTime decodeBcdTime(Buffer buffer, int index) {
        try {
            StringBuilder timeStr = new StringBuilder();
            for (int i = 0; i < 6; i++) {
                byte b = buffer.getByte(index + i);
                int high = (b >> 4) & 0x0F;
                int low = b & 0x0F;
                timeStr.append(high).append(low);
            }
            return LocalDateTime.parse("20" + timeStr.toString(), DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        } catch (Exception e) {
            return null;
        }
    }
    
    // Getter和Setter方法
    public long getRouteId() {
        return routeId;
    }
    
    public void setRouteId(long routeId) {
        this.routeId = routeId;
    }
    
    public int getRouteAttribute() {
        return routeAttribute;
    }
    
    public void setRouteAttribute(int routeAttribute) {
        this.routeAttribute = routeAttribute;
    }
    
    public LocalDateTime getStartTime() {
        return startTime;
    }
    
    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }
    
    public LocalDateTime getEndTime() {
        return endTime;
    }
    
    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }
    
    public int getWaypointCount() {
        return waypointCount;
    }
    
    public void setWaypointCount(int waypointCount) {
        this.waypointCount = waypointCount;
    }
    
    public List<RouteWaypoint> getWaypoints() {
        return new ArrayList<>(waypoints);
    }
    
    public void setWaypoints(List<RouteWaypoint> waypoints) {
        this.waypoints = waypoints != null ? new ArrayList<>(waypoints) : new ArrayList<>();
        this.waypointCount = this.waypoints.size();
    }
    
    @Override
    public String toString() {
        return String.format("T8606SetRoute{routeId=%d, routeAttribute=0x%04X, waypointCount=%d}", 
                           routeId, routeAttribute, waypointCount);
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        T8606SetRoute that = (T8606SetRoute) o;
        return routeId == that.routeId &&
               routeAttribute == that.routeAttribute &&
               waypointCount == that.waypointCount &&
               Objects.equals(startTime, that.startTime) &&
               Objects.equals(endTime, that.endTime) &&
               Objects.equals(waypoints, that.waypoints);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(routeId, routeAttribute, startTime, endTime, waypointCount, waypoints);
    }
    
    /**
     * 路线拐点项
     */
    public static class RouteWaypoint {
        
        // 路段属性位标志常量
        /** 行驶时间 */
        public static final int SEGMENT_ATTR_DRIVING_TIME = 0x01;
        /** 限速 */
        public static final int SEGMENT_ATTR_SPEED_LIMIT = 0x02;
        /** 南纬 */
        public static final int SEGMENT_ATTR_SOUTH_LATITUDE = 0x04;
        /** 西经 */
        public static final int SEGMENT_ATTR_WEST_LONGITUDE = 0x08;
        
        /** 拐点ID */
        private long waypointId;
        
        /** 路段ID */
        private long segmentId;
        
        /** 拐点纬度 */
        private long latitude;
        
        /** 拐点经度 */
        private long longitude;
        
        /** 路段宽度 */
        private int segmentWidth;
        
        /** 路段属性 */
        private int segmentAttribute;
        
        /** 路段行驶过长阈值 */
        private Integer drivingOverThreshold;
        
        /** 路段行驶不足阈值 */
        private Integer drivingUnderThreshold;
        
        /** 路段最高速度 */
        private Integer maxSpeed;
        
        /** 路段超速持续时间 */
        private Integer overspeedDuration;
        
        /**
         * 默认构造函数
         */
        public RouteWaypoint() {}
        
        /**
         * 带参数的构造函数
         * 
         * @param waypointId 拐点ID
         * @param segmentId 路段ID
         * @param latitude 拐点纬度
         * @param longitude 拐点经度
         * @param segmentWidth 路段宽度
         * @param segmentAttribute 路段属性
         */
        public RouteWaypoint(long waypointId, long segmentId, long latitude, long longitude, 
                           int segmentWidth, int segmentAttribute) {
            this.waypointId = waypointId;
            this.segmentId = segmentId;
            this.latitude = latitude;
            this.longitude = longitude;
            this.segmentWidth = segmentWidth;
            this.segmentAttribute = segmentAttribute;
        }
        
        /**
         * 编码拐点数据
         * 
         * @return 编码后的Buffer
         */
        public Buffer encode() {
            Buffer buffer = Buffer.buffer();
            
            // 拐点ID (DWORD)
            buffer.appendUnsignedInt(waypointId);
            
            // 路段ID (DWORD)
            buffer.appendUnsignedInt(segmentId);
            
            // 拐点纬度 (DWORD)
            buffer.appendUnsignedInt(latitude);
            
            // 拐点经度 (DWORD)
            buffer.appendUnsignedInt(longitude);
            
            // 路段宽度 (BYTE)
            buffer.appendByte((byte) segmentWidth);
            
            // 路段属性 (BYTE)
            buffer.appendByte((byte) segmentAttribute);
            
            // 可选字段：行驶时间相关
            if (hasDrivingTimeAttribute()) {
                // 路段行驶过长阈值 (WORD)
                buffer.appendUnsignedShort(drivingOverThreshold != null ? drivingOverThreshold : 0);
                // 路段行驶不足阈值 (WORD)
                buffer.appendUnsignedShort(drivingUnderThreshold != null ? drivingUnderThreshold : 0);
            }
            
            // 可选字段：限速相关
            if (hasSpeedLimitAttribute()) {
                // 路段最高速度 (WORD)
                buffer.appendUnsignedShort(maxSpeed != null ? maxSpeed : 0);
                // 路段超速持续时间 (BYTE)
                buffer.appendByte((byte) (overspeedDuration != null ? overspeedDuration : 0));
            }
            
            return buffer;
        }
        
        /**
         * 解码拐点数据
         * 
         * @param buffer 数据缓冲区
         * @param startIndex 起始索引
         * @return 下一个索引位置
         */
        public int decode(Buffer buffer, int startIndex) {
            int index = startIndex;
            
            // 检查基本字段长度
            if (index + 18 > buffer.length()) {
                throw new IllegalArgumentException("拐点数据长度不足");
            }
            
            // 拐点ID (DWORD)
            this.waypointId = buffer.getUnsignedInt(index);
            index += 4;
            
            // 路段ID (DWORD)
            this.segmentId = buffer.getUnsignedInt(index);
            index += 4;
            
            // 拐点纬度 (DWORD)
            this.latitude = buffer.getUnsignedInt(index);
            index += 4;
            
            // 拐点经度 (DWORD)
            this.longitude = buffer.getUnsignedInt(index);
            index += 4;
            
            // 路段宽度 (BYTE)
            this.segmentWidth = Byte.toUnsignedInt(buffer.getByte(index));
            index += 1;
            
            // 路段属性 (BYTE)
            this.segmentAttribute = Byte.toUnsignedInt(buffer.getByte(index));
            index += 1;
            
            // 可选字段：行驶时间相关
            if (hasDrivingTimeAttribute()) {
                if (index + 4 > buffer.length()) {
                    throw new IllegalArgumentException("拐点行驶时间数据长度不足");
                }
                // 路段行驶过长阈值 (WORD)
                this.drivingOverThreshold = buffer.getUnsignedShort(index);
                index += 2;
                // 路段行驶不足阈值 (WORD)
                this.drivingUnderThreshold = buffer.getUnsignedShort(index);
                index += 2;
            }
            
            // 可选字段：限速相关
            if (hasSpeedLimitAttribute()) {
                if (index + 3 > buffer.length()) {
                    throw new IllegalArgumentException("拐点限速数据长度不足");
                }
                // 路段最高速度 (WORD)
                this.maxSpeed = buffer.getUnsignedShort(index);
                index += 2;
                // 路段超速持续时间 (BYTE)
                this.overspeedDuration = Byte.toUnsignedInt(buffer.getByte(index));
                index += 1;
            }
            
            return index;
        }
        
        /**
         * 判断是否有行驶时间属性
         * 
         * @return 如果有行驶时间属性返回true，否则返回false
         */
        public boolean hasDrivingTimeAttribute() {
            return (segmentAttribute & SEGMENT_ATTR_DRIVING_TIME) != 0;
        }
        
        /**
         * 判断是否有限速属性
         * 
         * @return 如果有限速属性返回true，否则返回false
         */
        public boolean hasSpeedLimitAttribute() {
            return (segmentAttribute & SEGMENT_ATTR_SPEED_LIMIT) != 0;
        }
        
        /**
         * 判断是否有指定属性
         * 
         * @param attribute 属性标志
         * @return 如果有指定属性返回true，否则返回false
         */
        public boolean hasAttribute(int attribute) {
            return (segmentAttribute & attribute) != 0;
        }
        
        /**
         * 获取纬度（度）
         * 
         * @return 纬度值（度）
         */
        public double getLatitudeDegrees() {
            return latitude / 1000000.0;
        }
        
        /**
         * 设置纬度（度）
         * 
         * @param degrees 纬度值（度）
         */
        public void setLatitudeDegrees(double degrees) {
            this.latitude = Math.round(degrees * 1000000);
        }
        
        /**
         * 获取经度（度）
         * 
         * @return 经度值（度）
         */
        public double getLongitudeDegrees() {
            return longitude / 1000000.0;
        }
        
        /**
         * 设置经度（度）
         * 
         * @param degrees 经度值（度）
         */
        public void setLongitudeDegrees(double degrees) {
            this.longitude = Math.round(degrees * 1000000);
        }
        
        /**
         * 获取路段属性描述
         * 
         * @return 路段属性描述
         */
        public String getSegmentAttributeDescription() {
            StringBuilder sb = new StringBuilder();
            if (hasAttribute(SEGMENT_ATTR_DRIVING_TIME)) sb.append("行驶时间 ");
            if (hasAttribute(SEGMENT_ATTR_SPEED_LIMIT)) sb.append("限速 ");
            if (hasAttribute(SEGMENT_ATTR_SOUTH_LATITUDE)) sb.append("南纬 ");
            if (hasAttribute(SEGMENT_ATTR_WEST_LONGITUDE)) sb.append("西经 ");
            return sb.toString().trim();
        }
        
        // Getter和Setter方法
        public long getWaypointId() {
            return waypointId;
        }
        
        public void setWaypointId(long waypointId) {
            this.waypointId = waypointId;
        }
        
        public long getSegmentId() {
            return segmentId;
        }
        
        public void setSegmentId(long segmentId) {
            this.segmentId = segmentId;
        }
        
        public long getLatitude() {
            return latitude;
        }
        
        public void setLatitude(long latitude) {
            this.latitude = latitude;
        }
        
        public long getLongitude() {
            return longitude;
        }
        
        public void setLongitude(long longitude) {
            this.longitude = longitude;
        }
        
        public int getSegmentWidth() {
            return segmentWidth;
        }
        
        public void setSegmentWidth(int segmentWidth) {
            this.segmentWidth = segmentWidth;
        }
        
        public int getSegmentAttribute() {
            return segmentAttribute;
        }
        
        public void setSegmentAttribute(int segmentAttribute) {
            this.segmentAttribute = segmentAttribute;
        }
        
        public Integer getDrivingOverThreshold() {
            return drivingOverThreshold;
        }
        
        public void setDrivingOverThreshold(Integer drivingOverThreshold) {
            this.drivingOverThreshold = drivingOverThreshold;
        }
        
        public Integer getDrivingUnderThreshold() {
            return drivingUnderThreshold;
        }
        
        public void setDrivingUnderThreshold(Integer drivingUnderThreshold) {
            this.drivingUnderThreshold = drivingUnderThreshold;
        }
        
        public Integer getMaxSpeed() {
            return maxSpeed;
        }
        
        public void setMaxSpeed(Integer maxSpeed) {
            this.maxSpeed = maxSpeed;
        }
        
        public Integer getOverspeedDuration() {
            return overspeedDuration;
        }
        
        public void setOverspeedDuration(Integer overspeedDuration) {
            this.overspeedDuration = overspeedDuration;
        }
        
        @Override
        public String toString() {
            return String.format("RouteWaypoint{waypointId=%d, segmentId=%d, lat=%.6f, lng=%.6f, width=%d, attr=0x%02X}", 
                               waypointId, segmentId, getLatitudeDegrees(), getLongitudeDegrees(), segmentWidth, segmentAttribute);
        }
        
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            RouteWaypoint that = (RouteWaypoint) o;
            return waypointId == that.waypointId &&
                   segmentId == that.segmentId &&
                   latitude == that.latitude &&
                   longitude == that.longitude &&
                   segmentWidth == that.segmentWidth &&
                   segmentAttribute == that.segmentAttribute &&
                   Objects.equals(drivingOverThreshold, that.drivingOverThreshold) &&
                   Objects.equals(drivingUnderThreshold, that.drivingUnderThreshold) &&
                   Objects.equals(maxSpeed, that.maxSpeed) &&
                   Objects.equals(overspeedDuration, that.overspeedDuration);
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(waypointId, segmentId, latitude, longitude, segmentWidth, segmentAttribute,
                              drivingOverThreshold, drivingUnderThreshold, maxSpeed, overspeedDuration);
        }
    }
}