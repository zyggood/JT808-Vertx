package com.jt808.protocol.message;

import io.vertx.core.buffer.Buffer;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 0x8600 设置圆形区域消息
 * 平台→终端
 * 
 * 消息体包含：
 * - 设置属性 (BYTE): 0-更新区域, 1-追加区域, 2-修改区域
 * - 区域总数 (BYTE)
 * - 区域项列表 (圆形区域项)
 * 
 * @author JT808-Vertx
 */
public class T8600SetCircularArea extends JT808Message {
    
    /** 消息ID */
    public static final int MESSAGE_ID = 0x8600;
    
    // 设置属性常量
    /** 更新区域 */
    public static final byte SETTING_UPDATE = 0x00;
    /** 追加区域 */
    public static final byte SETTING_APPEND = 0x01;
    /** 修改区域 */
    public static final byte SETTING_MODIFY = 0x02;
    
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
    /** 进区域关闭通信模块 */
    public static final int ATTR_ENTER_CLOSE_COMM = 0x4000;
    /** 进区域采集GNSS详细定位数据 */
    public static final int ATTR_ENTER_COLLECT_GNSS = 0x8000;
    
    /** 设置属性 */
    private byte settingAttribute;
    
    /** 区域总数 */
    private byte areaCount;
    
    /** 圆形区域项列表 */
    private List<CircularAreaItem> areaItems;
    
    /**
     * 默认构造函数
     */
    public T8600SetCircularArea() {
        this.areaItems = new ArrayList<>();
    }
    
    /**
     * 构造函数
     * 
     * @param settingAttribute 设置属性
     * @param areaItems 区域项列表
     */
    public T8600SetCircularArea(byte settingAttribute, List<CircularAreaItem> areaItems) {
        this.settingAttribute = settingAttribute;
        this.areaItems = areaItems != null ? new ArrayList<>(areaItems) : new ArrayList<>();
        this.areaCount = (byte) this.areaItems.size();
    }
    
    /**
     * 创建更新区域消息
     * 
     * @param areaItems 区域项列表
     * @return 设置圆形区域消息
     */
    public static T8600SetCircularArea createUpdate(List<CircularAreaItem> areaItems) {
        return new T8600SetCircularArea(SETTING_UPDATE, areaItems);
    }
    
    /**
     * 创建追加区域消息
     * 
     * @param areaItems 区域项列表
     * @return 设置圆形区域消息
     */
    public static T8600SetCircularArea createAppend(List<CircularAreaItem> areaItems) {
        return new T8600SetCircularArea(SETTING_APPEND, areaItems);
    }
    
    /**
     * 创建修改区域消息
     * 
     * @param areaItems 区域项列表
     * @return 设置圆形区域消息
     */
    public static T8600SetCircularArea createModify(List<CircularAreaItem> areaItems) {
        return new T8600SetCircularArea(SETTING_MODIFY, areaItems);
    }
    
    @Override
    public int getMessageId() {
        return MESSAGE_ID;
    }
    
    @Override
    public Buffer encodeBody() {
        Buffer buffer = Buffer.buffer();
        
        // 设置属性 (1字节)
        buffer.appendByte(settingAttribute);
        
        // 区域总数 (1字节)
        buffer.appendByte(areaCount);
        
        // 区域项列表
        for (CircularAreaItem item : areaItems) {
            buffer.appendBuffer(item.encode());
        }
        
        return buffer;
    }
    
    @Override
    public void decodeBody(Buffer buffer) {
        if (buffer == null || buffer.length() < 2) {
            throw new IllegalArgumentException("消息体长度不足");
        }
        
        int index = 0;
        
        // 设置属性 (1字节)
        settingAttribute = buffer.getByte(index++);
        
        // 区域总数 (1字节)
        areaCount = buffer.getByte(index++);
        
        // 区域项列表
        areaItems = new ArrayList<>();
        for (int i = 0; i < (areaCount & 0xFF); i++) {
            CircularAreaItem item = new CircularAreaItem();
            index += item.decode(buffer, index);
            areaItems.add(item);
        }
    }
    
    /**
     * 添加区域项
     * 
     * @param item 区域项
     */
    public void addAreaItem(CircularAreaItem item) {
        if (item != null) {
            areaItems.add(item);
            areaCount = (byte) areaItems.size();
        }
    }
    
    /**
     * 获取设置属性描述
     * 
     * @return 设置属性描述
     */
    public String getSettingAttributeDescription() {
        switch (settingAttribute) {
            case SETTING_UPDATE:
                return "更新区域";
            case SETTING_APPEND:
                return "追加区域";
            case SETTING_MODIFY:
                return "修改区域";
            default:
                return "未知设置属性(" + (settingAttribute & 0xFF) + ")";
        }
    }
    
    /**
     * 获取无符号区域总数
     * 
     * @return 无符号区域总数
     */
    public int getUnsignedAreaCount() {
        return areaCount & 0xFF;
    }
    
    /**
     * 获取消息描述
     * 
     * @return 消息描述
     */
    public String getDescription() {
        return String.format("设置圆形区域[%s, 区域数量: %d]", 
                getSettingAttributeDescription(), getUnsignedAreaCount());
    }
    
    // Getter和Setter方法
    public byte getSettingAttribute() {
        return settingAttribute;
    }
    
    public void setSettingAttribute(byte settingAttribute) {
        this.settingAttribute = settingAttribute;
    }
    
    public byte getAreaCount() {
        return areaCount;
    }
    
    public void setAreaCount(byte areaCount) {
        this.areaCount = areaCount;
    }
    
    public List<CircularAreaItem> getAreaItems() {
        return new ArrayList<>(areaItems);
    }
    
    public void setAreaItems(List<CircularAreaItem> areaItems) {
        this.areaItems = areaItems != null ? new ArrayList<>(areaItems) : new ArrayList<>();
        this.areaCount = (byte) this.areaItems.size();
    }
    
    @Override
    public String toString() {
        return String.format("T8600SetCircularArea{settingAttribute=%d(%s), areaCount=%d, areaItems=%s}",
                settingAttribute & 0xFF, getSettingAttributeDescription(), 
                areaCount & 0xFF, areaItems);
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        T8600SetCircularArea that = (T8600SetCircularArea) o;
        return settingAttribute == that.settingAttribute &&
                areaCount == that.areaCount &&
                Objects.equals(areaItems, that.areaItems);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(settingAttribute, areaCount, areaItems);
    }
    
    /**
     * 圆形区域项
     */
    public static class CircularAreaItem {
        
        /** 区域ID */
        private int areaId;
        
        /** 区域属性 */
        private int areaAttribute;
        
        /** 中心点纬度 (度*10^6) */
        private int centerLatitude;
        
        /** 中心点经度 (度*10^6) */
        private int centerLongitude;
        
        /** 半径 (米) */
        private int radius;
        
        /** 起始时间 (可选) */
        private LocalDateTime startTime;
        
        /** 结束时间 (可选) */
        private LocalDateTime endTime;
        
        /** 最高速度 (km/h, 可选) */
        private Integer maxSpeed;
        
        /** 超速持续时间 (秒, 可选) */
        private Integer overspeedDuration;
        
        /**
         * 默认构造函数
         */
        public CircularAreaItem() {}
        
        /**
         * 构造函数
         * 
         * @param areaId 区域ID
         * @param areaAttribute 区域属性
         * @param centerLatitude 中心点纬度
         * @param centerLongitude 中心点经度
         * @param radius 半径
         */
        public CircularAreaItem(int areaId, int areaAttribute, int centerLatitude, 
                               int centerLongitude, int radius) {
            this.areaId = areaId;
            this.areaAttribute = areaAttribute;
            this.centerLatitude = centerLatitude;
            this.centerLongitude = centerLongitude;
            this.radius = radius;
        }
        
        /**
         * 编码区域项
         * 
         * @return 编码后的Buffer
         */
        public Buffer encode() {
            Buffer buffer = Buffer.buffer();
            
            // 区域ID (4字节)
            buffer.appendInt(areaId);
            
            // 区域属性 (2字节)
            buffer.appendShort((short) areaAttribute);
            
            // 中心点纬度 (4字节)
            buffer.appendInt(centerLatitude);
            
            // 中心点经度 (4字节)
            buffer.appendInt(centerLongitude);
            
            // 半径 (4字节)
            buffer.appendInt(radius);
            
            // 起始时间 (6字节BCD, 可选)
            if (hasTimeAttribute() && startTime != null) {
                buffer.appendBuffer(encodeBcdTime(startTime));
            }
            
            // 结束时间 (6字节BCD, 可选)
            if (hasTimeAttribute() && endTime != null) {
                buffer.appendBuffer(encodeBcdTime(endTime));
            }
            
            // 最高速度 (2字节, 可选)
            if (hasSpeedLimitAttribute() && maxSpeed != null) {
                buffer.appendShort(maxSpeed.shortValue());
            }
            
            // 超速持续时间 (1字节, 可选)
            if (hasSpeedLimitAttribute() && overspeedDuration != null) {
                buffer.appendByte(overspeedDuration.byteValue());
            }
            
            return buffer;
        }
        
        /**
         * 解码区域项
         * 
         * @param buffer 数据缓冲区
         * @param startIndex 起始索引
         * @return 解码的字节数
         */
        public int decode(Buffer buffer, int startIndex) {
            int index = startIndex;
            
            // 区域ID (4字节)
            areaId = buffer.getInt(index);
            index += 4;
            
            // 区域属性 (2字节)
            areaAttribute = buffer.getShort(index) & 0xFFFF;
            index += 2;
            
            // 中心点纬度 (4字节)
            centerLatitude = buffer.getInt(index);
            index += 4;
            
            // 中心点经度 (4字节)
            centerLongitude = buffer.getInt(index);
            index += 4;
            
            // 半径 (4字节)
            radius = buffer.getInt(index);
            index += 4;
            
            // 起始时间 (6字节BCD, 可选)
            if (hasTimeAttribute()) {
                startTime = decodeBcdTime(buffer, index);
                index += 6;
            }
            
            // 结束时间 (6字节BCD, 可选)
            if (hasTimeAttribute()) {
                endTime = decodeBcdTime(buffer, index);
                index += 6;
            }
            
            // 最高速度 (2字节, 可选)
            if (hasSpeedLimitAttribute()) {
                maxSpeed = buffer.getShort(index) & 0xFFFF;
                index += 2;
            }
            
            // 超速持续时间 (1字节, 可选)
            if (hasSpeedLimitAttribute()) {
                overspeedDuration = buffer.getByte(index) & 0xFF;
                index += 1;
            }
            
            return index - startIndex;
        }
        
        /**
         * 检查是否有时间属性
         * 
         * @return 是否有时间属性
         */
        public boolean hasTimeAttribute() {
            return (areaAttribute & ATTR_TIME_BASED) != 0;
        }
        
        /**
         * 检查是否有限速属性
         * 
         * @return 是否有限速属性
         */
        public boolean hasSpeedLimitAttribute() {
            return (areaAttribute & ATTR_SPEED_LIMIT) != 0;
        }
        
        /**
         * 检查是否有指定的区域属性
         * 
         * @param attribute 属性标志
         * @return 是否有指定属性
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
            StringBuilder sb = new StringBuilder();
            if (hasAttribute(ATTR_TIME_BASED)) sb.append("时间限制,");
            if (hasAttribute(ATTR_SPEED_LIMIT)) sb.append("限速,");
            if (hasAttribute(ATTR_ENTER_ALARM_DRIVER)) sb.append("进区域报警驾驶员,");
            if (hasAttribute(ATTR_ENTER_ALARM_PLATFORM)) sb.append("进区域报警平台,");
            if (hasAttribute(ATTR_EXIT_ALARM_DRIVER)) sb.append("出区域报警驾驶员,");
            if (hasAttribute(ATTR_EXIT_ALARM_PLATFORM)) sb.append("出区域报警平台,");
            if (hasAttribute(ATTR_SOUTH_LATITUDE)) sb.append("南纬,");
            if (hasAttribute(ATTR_WEST_LONGITUDE)) sb.append("西经,");
            if (hasAttribute(ATTR_DOOR_FORBIDDEN)) sb.append("禁止开门,");
            if (hasAttribute(ATTR_ENTER_CLOSE_COMM)) sb.append("进区域关闭通信,");
            if (hasAttribute(ATTR_ENTER_COLLECT_GNSS)) sb.append("进区域采集GNSS,");
            
            String result = sb.toString();
            return result.isEmpty() ? "无" : result.substring(0, result.length() - 1);
        }
        
        /**
         * 编码BCD时间
         * 
         * @param time 时间
         * @return BCD编码的时间
         */
        private Buffer encodeBcdTime(LocalDateTime time) {
            Buffer buffer = Buffer.buffer();
            
            int year = time.getYear() % 100; // 取年份后两位
            int month = time.getMonthValue();
            int day = time.getDayOfMonth();
            int hour = time.getHour();
            int minute = time.getMinute();
            int second = time.getSecond();
            
            buffer.appendByte((byte) ((year / 10 << 4) | (year % 10)));
            buffer.appendByte((byte) ((month / 10 << 4) | (month % 10)));
            buffer.appendByte((byte) ((day / 10 << 4) | (day % 10)));
            buffer.appendByte((byte) ((hour / 10 << 4) | (hour % 10)));
            buffer.appendByte((byte) ((minute / 10 << 4) | (minute % 10)));
            buffer.appendByte((byte) ((second / 10 << 4) | (second % 10)));
            
            return buffer;
        }
        
        /**
         * 解码BCD时间
         * 
         * @param buffer 数据缓冲区
         * @param index 起始索引
         * @return 解码的时间
         */
        private LocalDateTime decodeBcdTime(Buffer buffer, int index) {
            int year = ((buffer.getByte(index) & 0xF0) >> 4) * 10 + (buffer.getByte(index) & 0x0F);
            int month = ((buffer.getByte(index + 1) & 0xF0) >> 4) * 10 + (buffer.getByte(index + 1) & 0x0F);
            int day = ((buffer.getByte(index + 2) & 0xF0) >> 4) * 10 + (buffer.getByte(index + 2) & 0x0F);
            int hour = ((buffer.getByte(index + 3) & 0xF0) >> 4) * 10 + (buffer.getByte(index + 3) & 0x0F);
            int minute = ((buffer.getByte(index + 4) & 0xF0) >> 4) * 10 + (buffer.getByte(index + 4) & 0x0F);
            int second = ((buffer.getByte(index + 5) & 0xF0) >> 4) * 10 + (buffer.getByte(index + 5) & 0x0F);
            
            // 假设年份为20xx
            year += 2000;
            
            return LocalDateTime.of(year, month, day, hour, minute, second);
        }
        
        // Getter和Setter方法
        public int getAreaId() {
            return areaId;
        }
        
        public void setAreaId(int areaId) {
            this.areaId = areaId;
        }
        
        public int getAreaAttribute() {
            return areaAttribute;
        }
        
        public void setAreaAttribute(int areaAttribute) {
            this.areaAttribute = areaAttribute;
        }
        
        public int getCenterLatitude() {
            return centerLatitude;
        }
        
        public void setCenterLatitude(int centerLatitude) {
            this.centerLatitude = centerLatitude;
        }
        
        public int getCenterLongitude() {
            return centerLongitude;
        }
        
        public void setCenterLongitude(int centerLongitude) {
            this.centerLongitude = centerLongitude;
        }
        
        public int getRadius() {
            return radius;
        }
        
        public void setRadius(int radius) {
            this.radius = radius;
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
        
        @Override
        public String toString() {
            return String.format("CircularAreaItem{areaId=%d, areaAttribute=0x%04X(%s), " +
                            "centerLatitude=%d, centerLongitude=%d, radius=%d, " +
                            "startTime=%s, endTime=%s, maxSpeed=%s, overspeedDuration=%s}",
                    areaId, areaAttribute, getAreaAttributeDescription(),
                    centerLatitude, centerLongitude, radius,
                    startTime, endTime, maxSpeed, overspeedDuration);
        }
        
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            CircularAreaItem that = (CircularAreaItem) o;
            return areaId == that.areaId &&
                    areaAttribute == that.areaAttribute &&
                    centerLatitude == that.centerLatitude &&
                    centerLongitude == that.centerLongitude &&
                    radius == that.radius &&
                    Objects.equals(startTime, that.startTime) &&
                    Objects.equals(endTime, that.endTime) &&
                    Objects.equals(maxSpeed, that.maxSpeed) &&
                    Objects.equals(overspeedDuration, that.overspeedDuration);
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(areaId, areaAttribute, centerLatitude, centerLongitude, 
                    radius, startTime, endTime, maxSpeed, overspeedDuration);
        }
    }
}