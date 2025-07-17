package com.jt808.protocol.message;

import io.vertx.core.buffer.Buffer;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 0x8604 设置多边形区域消息
 * 平台→终端
 * 
 * 消息体包含：
 * - 区域ID (DWORD)
 * - 区域属性 (WORD)
 * - 起始时间 (BCD[6])
 * - 结束时间 (BCD[6])
 * - 最高速度 (WORD) - 可选，当区域属性1位为0时不存在
 * - 超速持续时间 (BYTE) - 可选，当区域属性1位为0时不存在
 * - 区域总顶点数 (WORD)
 * - 顶点项列表 (多边形顶点项)
 * 
 * @author JT808-Vertx
 */
public class T8604SetPolygonArea extends JT808Message {
    
    /** 消息ID */
    public static final int MESSAGE_ID = 0x8604;
    
    // 区域属性位标志常量
    /** 根据时间 */
    public static final int ATTR_TIME_BASED = 0x0001;
    /** 限速 */
    public static final int ATTR_SPEED_LIMIT = 0x0002;
    /** 进区域报警给驾驶员 */
    public static final int ATTR_ENTER_ALARM_DRIVER = 0x0004;
    /** 进区域报警给平台 */
    public static final int ATTR_ENTER_ALARM_PLATFORM = 0x0008;
    /** 出区域报警给驾驶员 */
    public static final int ATTR_EXIT_ALARM_DRIVER = 0x0010;
    /** 出区域报警给平台 */
    public static final int ATTR_EXIT_ALARM_PLATFORM = 0x0020;
    /** 南纬 */
    public static final int ATTR_SOUTH_LATITUDE = 0x0040;
    /** 西经 */
    public static final int ATTR_WEST_LONGITUDE = 0x0080;
    /** 禁止开门 */
    public static final int ATTR_DOOR_FORBIDDEN = 0x0100;
    /** 进区域时关闭通信模块 */
    public static final int ATTR_ENTER_CLOSE_COMM = 0x4000;
    /** 进区域时采集GNSS详细定位数据 */
    public static final int ATTR_ENTER_COLLECT_GNSS = 0x8000;
    
    /** 区域ID */
    private long areaId;
    
    /** 区域属性 */
    private int areaAttribute;
    
    /** 起始时间 */
    private LocalDateTime startTime;
    
    /** 结束时间 */
    private LocalDateTime endTime;
    
    /** 最高速度 (km/h) - 可选字段 */
    private Integer maxSpeed;
    
    /** 超速持续时间 (秒) - 可选字段 */
    private Integer overspeedDuration;
    
    /** 区域总顶点数 */
    private int vertexCount;
    
    /** 顶点项列表 */
    private List<PolygonVertex> vertices;
    
    /**
     * 默认构造函数
     */
    public T8604SetPolygonArea() {
        this.vertices = new ArrayList<>();
    }
    
    /**
     * 带参数的构造函数
     * 
     * @param areaId 区域ID
     * @param areaAttribute 区域属性
     * @param startTime 起始时间
     * @param endTime 结束时间
     * @param vertices 顶点列表
     */
    public T8604SetPolygonArea(long areaId, int areaAttribute, LocalDateTime startTime, 
                              LocalDateTime endTime, List<PolygonVertex> vertices) {
        this.areaId = areaId;
        this.areaAttribute = areaAttribute;
        this.startTime = startTime;
        this.endTime = endTime;
        this.vertices = vertices != null ? new ArrayList<>(vertices) : new ArrayList<>();
        this.vertexCount = this.vertices.size();
    }
    
    @Override
    public int getMessageId() {
        return MESSAGE_ID;
    }
    
    @Override
    public Buffer encodeBody() {
        Buffer buffer = Buffer.buffer();
        
        // 区域ID (DWORD)
        buffer.appendUnsignedInt(areaId);
        
        // 区域属性 (WORD)
        buffer.appendUnsignedShort(areaAttribute);
        
        // 起始时间 (BCD[6])
        buffer.appendBuffer(encodeBcdTime(startTime));
        
        // 结束时间 (BCD[6])
        buffer.appendBuffer(encodeBcdTime(endTime));
        
        // 如果区域属性1位为1，则包含最高速度和超速持续时间
        if (hasSpeedLimitAttribute()) {
            // 最高速度 (WORD)
            buffer.appendUnsignedShort(maxSpeed != null ? maxSpeed : 0);
            
            // 超速持续时间 (BYTE)
            buffer.appendByte((byte) (overspeedDuration != null ? overspeedDuration : 0));
        }
        
        // 区域总顶点数 (WORD)
        buffer.appendUnsignedShort(vertexCount);
        
        // 顶点项列表
        for (PolygonVertex vertex : vertices) {
            buffer.appendBuffer(vertex.encode());
        }
        
        return buffer;
    }
    
    @Override
    public void decodeBody(Buffer buffer) {
        if (buffer.length() < 18) { // 最小长度：4+2+6+6 = 18字节
            throw new IllegalArgumentException("消息体长度不足");
        }
        
        int index = 0;
        
        // 区域ID (DWORD)
        areaId = buffer.getUnsignedInt(index);
        index += 4;
        
        // 区域属性 (WORD)
        areaAttribute = buffer.getUnsignedShort(index);
        index += 2;
        
        // 起始时间 (BCD[6])
        startTime = decodeBcdTime(buffer, index);
        index += 6;
        
        // 结束时间 (BCD[6])
        endTime = decodeBcdTime(buffer, index);
        index += 6;
        
        // 如果区域属性1位为1，则读取最高速度和超速持续时间
        if (hasSpeedLimitAttribute()) {
            if (buffer.length() < index + 3) {
                throw new IllegalArgumentException("消息体长度不足，缺少速度限制字段");
            }
            
            // 最高速度 (WORD)
            maxSpeed = buffer.getUnsignedShort(index);
            index += 2;
            
            // 超速持续时间 (BYTE)
            overspeedDuration = Byte.toUnsignedInt(buffer.getByte(index));
            index += 1;
        }
        
        // 区域总顶点数 (WORD)
        if (buffer.length() < index + 2) {
            throw new IllegalArgumentException("消息体长度不足，缺少顶点数字段");
        }
        vertexCount = buffer.getUnsignedShort(index);
        index += 2;
        
        // 验证剩余长度是否足够包含所有顶点
        int expectedVertexDataLength = vertexCount * 8; // 每个顶点8字节
        if (buffer.length() < index + expectedVertexDataLength) {
            throw new IllegalArgumentException(
                String.format("消息体长度不足，期望顶点数据%d字节，实际剩余%d字节", 
                    expectedVertexDataLength, buffer.length() - index));
        }
        
        // 顶点项列表
        vertices.clear();
        for (int i = 0; i < vertexCount; i++) {
            PolygonVertex vertex = new PolygonVertex();
            index = vertex.decode(buffer, index);
            vertices.add(vertex);
        }
    }
    
    /**
     * 添加顶点
     * 
     * @param vertex 顶点
     */
    public void addVertex(PolygonVertex vertex) {
        if (vertex != null) {
            vertices.add(vertex);
            vertexCount = vertices.size();
        }
    }
    
    /**
     * 判断是否有速度限制属性
     * 
     * @return 如果有速度限制属性返回true，否则返回false
     */
    public boolean hasSpeedLimitAttribute() {
        return (areaAttribute & ATTR_SPEED_LIMIT) != 0;
    }
    
    /**
     * 判断是否有时间属性
     * 
     * @return 如果有时间属性返回true，否则返回false
     */
    public boolean hasTimeAttribute() {
        return (areaAttribute & ATTR_TIME_BASED) != 0;
    }
    
    /**
     * 判断是否有指定属性
     * 
     * @param attribute 属性标志
     * @return 如果有指定属性返回true，否则返回false
     */
    public boolean hasAttribute(int attribute) {
        return (areaAttribute & attribute) != 0;
    }
    
    /**
     * 获取区域属性描述
     * 
     * @return 区域属性描述
     */
    public String getAreaAttributeDescription() {
        List<String> attributes = new ArrayList<>();
        if (hasAttribute(ATTR_TIME_BASED)) attributes.add("根据时间");
        if (hasAttribute(ATTR_SPEED_LIMIT)) attributes.add("限速");
        if (hasAttribute(ATTR_ENTER_ALARM_DRIVER)) attributes.add("进区域报警给驾驶员");
        if (hasAttribute(ATTR_ENTER_ALARM_PLATFORM)) attributes.add("进区域报警给平台");
        if (hasAttribute(ATTR_EXIT_ALARM_DRIVER)) attributes.add("出区域报警给驾驶员");
        if (hasAttribute(ATTR_EXIT_ALARM_PLATFORM)) attributes.add("出区域报警给平台");
        if (hasAttribute(ATTR_SOUTH_LATITUDE)) attributes.add("南纬");
        if (hasAttribute(ATTR_WEST_LONGITUDE)) attributes.add("西经");
        if (hasAttribute(ATTR_DOOR_FORBIDDEN)) attributes.add("禁止开门");
        if (hasAttribute(ATTR_ENTER_CLOSE_COMM)) attributes.add("进区域时关闭通信模块");
        if (hasAttribute(ATTR_ENTER_COLLECT_GNSS)) attributes.add("进区域时采集GNSS详细定位数据");
        return String.join(", ", attributes);
    }
    
    /**
     * 获取消息描述
     * 
     * @return 消息描述
     */
    public String getDescription() {
        return String.format("设置多边形区域消息 - 区域ID: %d，顶点数: %d", areaId, vertexCount);
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
            // 如果时间为null，填充6个0字节
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
        StringBuilder timeStr = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            byte b = buffer.getByte(index + i);
            int high = (b >> 4) & 0x0F;
            int low = b & 0x0F;
            timeStr.append(high).append(low);
        }
        
        try {
            return LocalDateTime.parse("20" + timeStr.toString(), DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        } catch (Exception e) {
            return null;
        }
    }
    
    // Getters and Setters
    public long getAreaId() {
        return areaId;
    }
    
    public void setAreaId(long areaId) {
        this.areaId = areaId;
    }
    
    public int getAreaAttribute() {
        return areaAttribute;
    }
    
    public void setAreaAttribute(int areaAttribute) {
        this.areaAttribute = areaAttribute;
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
    
    public int getVertexCount() {
        return vertexCount;
    }
    
    public void setVertexCount(int vertexCount) {
        this.vertexCount = vertexCount;
    }
    
    public List<PolygonVertex> getVertices() {
        return new ArrayList<>(vertices);
    }
    
    public void setVertices(List<PolygonVertex> vertices) {
        this.vertices = vertices != null ? new ArrayList<>(vertices) : new ArrayList<>();
        this.vertexCount = this.vertices.size();
    }
    
    @Override
    public String toString() {
        return String.format("T8604SetPolygonArea{areaId=%d, areaAttribute=0x%04X, vertexCount=%d, vertices=%s}", 
            areaId, areaAttribute, vertexCount, vertices);
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        T8604SetPolygonArea that = (T8604SetPolygonArea) o;
        return areaId == that.areaId && 
               areaAttribute == that.areaAttribute && 
               vertexCount == that.vertexCount &&
               Objects.equals(startTime, that.startTime) &&
               Objects.equals(endTime, that.endTime) &&
               Objects.equals(maxSpeed, that.maxSpeed) &&
               Objects.equals(overspeedDuration, that.overspeedDuration) &&
               Objects.equals(vertices, that.vertices);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(areaId, areaAttribute, startTime, endTime, maxSpeed, overspeedDuration, vertexCount, vertices);
    }
    
    /**
     * 多边形顶点项
     */
    public static class PolygonVertex {
        
        /** 顶点纬度 */
        private long latitude;
        
        /** 顶点经度 */
        private long longitude;
        
        /**
         * 默认构造函数
         */
        public PolygonVertex() {}
        
        /**
         * 带参数的构造函数
         * 
         * @param latitude 纬度（乘以10^6）
         * @param longitude 经度（乘以10^6）
         */
        public PolygonVertex(long latitude, long longitude) {
            this.latitude = latitude;
            this.longitude = longitude;
        }
        
        /**
         * 编码顶点数据
         * 
         * @return 编码后的Buffer
         */
        public Buffer encode() {
            Buffer buffer = Buffer.buffer();
            
            // 顶点纬度 (DWORD)
            buffer.appendUnsignedInt(latitude);
            
            // 顶点经度 (DWORD)
            buffer.appendUnsignedInt(longitude);
            
            return buffer;
        }
        
        /**
         * 解码顶点数据
         * 
         * @param buffer 数据缓冲区
         * @param startIndex 起始索引
         * @return 解码后的下一个索引位置
         */
        public int decode(Buffer buffer, int startIndex) {
            int index = startIndex;
            
            // 顶点纬度 (DWORD)
            latitude = buffer.getUnsignedInt(index);
            index += 4;
            
            // 顶点经度 (DWORD)
            longitude = buffer.getUnsignedInt(index);
            index += 4;
            
            return index;
        }
        
        /**
         * 获取纬度（度）
         * 
         * @return 纬度值
         */
        public double getLatitudeDegrees() {
            return latitude / 1000000.0;
        }
        
        /**
         * 设置纬度（度）
         * 
         * @param degrees 纬度值
         */
        public void setLatitudeDegrees(double degrees) {
            this.latitude = Math.round(degrees * 1000000);
        }
        
        /**
         * 获取经度（度）
         * 
         * @return 经度值
         */
        public double getLongitudeDegrees() {
            return longitude / 1000000.0;
        }
        
        /**
         * 设置经度（度）
         * 
         * @param degrees 经度值
         */
        public void setLongitudeDegrees(double degrees) {
            this.longitude = Math.round(degrees * 1000000);
        }
        
        // Getters and Setters
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
        
        @Override
        public String toString() {
            return String.format("PolygonVertex{latitude=%d(%.6f°), longitude=%d(%.6f°)}", 
                latitude, getLatitudeDegrees(), longitude, getLongitudeDegrees());
        }
        
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            PolygonVertex that = (PolygonVertex) o;
            return latitude == that.latitude && longitude == that.longitude;
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(latitude, longitude);
        }
    }
}