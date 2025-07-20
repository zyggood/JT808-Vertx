package com.jt808.protocol.message;

import io.vertx.core.buffer.Buffer;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 0x8602 设置矩形区域消息
 * 平台→终端
 * 
 * 消息体包含：
 * - 设置属性 (BYTE): 0-更新区域, 1-追加区域, 2-修改区域
 * - 区域总数 (BYTE)
 * - 区域项列表 (矩形区域项)
 * 
 * @author JT808-Vertx
 */
public class T8602SetRectangularArea extends JT808Message {
    
    /** 消息ID */
    public static final int MESSAGE_ID = 0x8602;
    
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
    
    /** 区域项列表 */
    private List<RectangularAreaItem> areaItems;
    
    /**
     * 默认构造函数
     */
    public T8602SetRectangularArea() {
        this.areaItems = new ArrayList<>();
    }
    
    /**
     * 构造函数
     * 
     * @param settingAttribute 设置属性
     * @param areaItems 区域项列表
     */
    public T8602SetRectangularArea(byte settingAttribute, List<RectangularAreaItem> areaItems) {
        this.settingAttribute = settingAttribute;
        this.areaItems = areaItems != null ? new ArrayList<>(areaItems) : new ArrayList<>();
        this.areaCount = (byte) this.areaItems.size();
    }
    
    /**
     * 创建更新区域消息
     * 
     * @param areaItems 区域项列表
     * @return 设置矩形区域消息
     */
    public static T8602SetRectangularArea createUpdate(List<RectangularAreaItem> areaItems) {
        return new T8602SetRectangularArea(SETTING_UPDATE, areaItems);
    }
    
    /**
     * 创建追加区域消息
     * 
     * @param areaItems 区域项列表
     * @return 设置矩形区域消息
     */
    public static T8602SetRectangularArea createAppend(List<RectangularAreaItem> areaItems) {
        return new T8602SetRectangularArea(SETTING_APPEND, areaItems);
    }
    
    /**
     * 创建修改区域消息
     * 
     * @param areaItems 区域项列表
     * @return 设置矩形区域消息
     */
    public static T8602SetRectangularArea createModify(List<RectangularAreaItem> areaItems) {
        return new T8602SetRectangularArea(SETTING_MODIFY, areaItems);
    }
    
    @Override
    public int getMessageId() {
        return MESSAGE_ID;
    }
    
    @Override
    public Buffer encodeBody() {
        Buffer buffer = Buffer.buffer();
        
        // 写入设置属性
        buffer.appendByte(settingAttribute);
        
        // 写入区域总数
        buffer.appendByte(areaCount);
        
        // 写入区域项列表
        for (RectangularAreaItem item : areaItems) {
            buffer.appendBuffer(item.encode());
        }
        
        return buffer;
    }
    
    @Override
    public void decodeBody(Buffer buffer) {
        if (buffer.length() < 2) {
            throw new IllegalArgumentException("消息体长度不足");
        }
        
        int index = 0;
        
        // 读取设置属性
        this.settingAttribute = buffer.getByte(index++);
        
        // 读取区域总数
        this.areaCount = buffer.getByte(index++);
        
        // 读取区域项列表
        this.areaItems = new ArrayList<>();
        for (int i = 0; i < Byte.toUnsignedInt(areaCount); i++) {
            RectangularAreaItem item = new RectangularAreaItem();
            index = item.decode(buffer, index);
            this.areaItems.add(item);
        }
    }
    
    /**
     * 添加区域项
     * 
     * @param item 区域项
     */
    public void addAreaItem(RectangularAreaItem item) {
        if (item != null) {
            this.areaItems.add(item);
            this.areaCount = (byte) this.areaItems.size();
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
                return "未知(" + settingAttribute + ")";
        }
    }
    
    /**
     * 获取无符号区域总数
     * 
     * @return 无符号区域总数
     */
    public int getUnsignedAreaCount() {
        return Byte.toUnsignedInt(areaCount);
    }
    
    /**
     * 获取消息描述
     * 
     * @return 消息描述
     */
    public String getDescription() {
        return String.format("设置矩形区域[%s, 区域数量: %d]", 
                getSettingAttributeDescription(), getUnsignedAreaCount());
    }
    
    // Getters and Setters
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
    
    public List<RectangularAreaItem> getAreaItems() {
        return new ArrayList<>(areaItems);
    }
    
    public void setAreaItems(List<RectangularAreaItem> areaItems) {
        this.areaItems = areaItems != null ? new ArrayList<>(areaItems) : new ArrayList<>();
        this.areaCount = (byte) this.areaItems.size();
    }
    
    @Override
    public String toString() {
        return String.format("T8602SetRectangularArea{messageId=0x%04X, settingAttribute=%s, areaCount=%d, areaItems=%s}",
                getMessageId(), getSettingAttributeDescription(), getUnsignedAreaCount(), areaItems);
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        T8602SetRectangularArea that = (T8602SetRectangularArea) o;
        return settingAttribute == that.settingAttribute && 
               areaCount == that.areaCount && 
               Objects.equals(areaItems, that.areaItems);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(settingAttribute, areaCount, areaItems);
    }
    
    /**
     * 矩形区域项
     */
    public static class RectangularAreaItem {
        
        /** 区域ID */
        private int areaId;
        
        /** 区域属性 */
        private int areaAttribute;
        
        /** 左上点纬度 */
        private int topLeftLatitude;
        
        /** 左上点经度 */
        private int topLeftLongitude;
        
        /** 右下点纬度 */
        private int bottomRightLatitude;
        
        /** 右下点经度 */
        private int bottomRightLongitude;
        
        /** 起始时间 */
        private LocalDateTime startTime;
        
        /** 结束时间 */
        private LocalDateTime endTime;
        
        /** 最高速度 */
        private Integer maxSpeed;
        
        /** 超速持续时间 */
        private Integer overspeedDuration;
        
        /**
         * 默认构造函数
         */
        public RectangularAreaItem() {}
        
        /**
         * 构造函数
         * 
         * @param areaId 区域ID
         * @param areaAttribute 区域属性
         * @param topLeftLatitude 左上点纬度
         * @param topLeftLongitude 左上点经度
         * @param bottomRightLatitude 右下点纬度
         * @param bottomRightLongitude 右下点经度
         */
        public RectangularAreaItem(int areaId, int areaAttribute, 
                                 int topLeftLatitude, int topLeftLongitude,
                                 int bottomRightLatitude, int bottomRightLongitude) {
            this.areaId = areaId;
            this.areaAttribute = areaAttribute;
            this.topLeftLatitude = topLeftLatitude;
            this.topLeftLongitude = topLeftLongitude;
            this.bottomRightLatitude = bottomRightLatitude;
            this.bottomRightLongitude = bottomRightLongitude;
        }
        
        /**
         * 编码区域项
         * 
         * @return 编码后的数据
         */
        public Buffer encode() {
            Buffer buffer = Buffer.buffer();
            
            // 区域ID (DWORD)
            buffer.appendUnsignedInt(areaId);
            
            // 区域属性 (WORD)
            buffer.appendUnsignedShort(areaAttribute);
            
            // 左上点纬度 (DWORD)
            buffer.appendUnsignedInt(topLeftLatitude);
            
            // 左上点经度 (DWORD)
            buffer.appendUnsignedInt(topLeftLongitude);
            
            // 右下点纬度 (DWORD)
            buffer.appendUnsignedInt(bottomRightLatitude);
            
            // 右下点经度 (DWORD)
            buffer.appendUnsignedInt(bottomRightLongitude);
            
            // 起始时间 (BCD[6]) - 仅当区域属性bit0为1时存在
            if (hasTimeAttribute()) {
                if (startTime != null) {
                    buffer.appendBuffer(encodeBcdTime(startTime));
                } else {
                    buffer.appendBytes(new byte[6]); // 填充0
                }
            }
            
            // 结束时间 (BCD[6]) - 仅当区域属性bit0为1时存在
            if (hasTimeAttribute()) {
                if (endTime != null) {
                    buffer.appendBuffer(encodeBcdTime(endTime));
                } else {
                    buffer.appendBytes(new byte[6]); // 填充0
                }
            }
            
            // 最高速度 (WORD) - 仅当区域属性bit1为1时存在
            if (hasSpeedLimitAttribute()) {
                buffer.appendUnsignedShort(maxSpeed != null ? maxSpeed : 0);
            }
            
            // 超速持续时间 (BYTE) - 仅当区域属性bit1为1时存在
            if (hasSpeedLimitAttribute()) {
                buffer.appendByte(overspeedDuration != null ? overspeedDuration.byteValue() : 0);
            }
            
            return buffer;
        }
        
        /**
         * 解码区域项
         * 
         * @param buffer 数据缓冲区
         * @param startIndex 起始索引
         * @return 解码后的索引位置
         */
        public int decode(Buffer buffer, int startIndex) {
            int index = startIndex;
            
            // 区域ID (DWORD)
            this.areaId = (int) buffer.getUnsignedInt(index);
            index += 4;
            
            // 区域属性 (WORD)
            this.areaAttribute = buffer.getUnsignedShort(index);
            index += 2;
            
            // 左上点纬度 (DWORD)
            this.topLeftLatitude = (int) buffer.getUnsignedInt(index);
            index += 4;
            
            // 左上点经度 (DWORD)
            this.topLeftLongitude = (int) buffer.getUnsignedInt(index);
            index += 4;
            
            // 右下点纬度 (DWORD)
            this.bottomRightLatitude = (int) buffer.getUnsignedInt(index);
            index += 4;
            
            // 右下点经度 (DWORD)
            this.bottomRightLongitude = (int) buffer.getUnsignedInt(index);
            index += 4;
            
            // 起始时间 (BCD[6]) - 仅当区域属性bit0为1时存在
            if (hasTimeAttribute()) {
                this.startTime = decodeBcdTime(buffer, index);
                index += 6;
            }
            
            // 结束时间 (BCD[6]) - 仅当区域属性bit0为1时存在
            if (hasTimeAttribute()) {
                this.endTime = decodeBcdTime(buffer, index);
                index += 6;
            }
            
            // 最高速度 (WORD) - 仅当区域属性bit1为1时存在
            if (hasSpeedLimitAttribute()) {
                this.maxSpeed = buffer.getUnsignedShort(index);
                index += 2;
            }
            
            // 超速持续时间 (BYTE) - 仅当区域属性bit1为1时存在
            if (hasSpeedLimitAttribute()) {
                this.overspeedDuration = (int) buffer.getUnsignedByte(index);
                index += 1;
            }
            
            return index;
        }
        
        /**
         * 是否有时间属性
         * 
         * @return true如果有时间属性
         */
        public boolean hasTimeAttribute() {
            return (areaAttribute & ATTR_TIME_BASED) != 0;
        }
        
        /**
         * 是否有限速属性
         * 
         * @return true如果有限速属性
         */
        public boolean hasSpeedLimitAttribute() {
            return (areaAttribute & ATTR_SPEED_LIMIT) != 0;
        }
        
        /**
         * 检查是否有指定属性
         * 
         * @param attribute 属性标志
         * @return true如果有该属性
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
            if (hasAttribute(ATTR_TIME_BASED)) sb.append("根据时间,");
            if (hasAttribute(ATTR_SPEED_LIMIT)) sb.append("限速,");
            if (hasAttribute(ATTR_ENTER_ALARM_DRIVER)) sb.append("进区域报警给驾驶员,");
            if (hasAttribute(ATTR_ENTER_ALARM_PLATFORM)) sb.append("进区域报警给平台,");
            if (hasAttribute(ATTR_EXIT_ALARM_DRIVER)) sb.append("出区域报警给驾驶员,");
            if (hasAttribute(ATTR_EXIT_ALARM_PLATFORM)) sb.append("出区域报警给平台,");
            if (hasAttribute(ATTR_SOUTH_LATITUDE)) sb.append("南纬,");
            if (hasAttribute(ATTR_WEST_LONGITUDE)) sb.append("西经,");
            if (hasAttribute(ATTR_DOOR_FORBIDDEN)) sb.append("禁止开门,");
            if (hasAttribute(ATTR_ENTER_CLOSE_COMM)) sb.append("进区域关闭通信模块,");
            if (hasAttribute(ATTR_ENTER_COLLECT_GNSS)) sb.append("进区域采集GNSS详细定位数据,");
            return sb.length() > 0 ? sb.substring(0, sb.length() - 1) : "无";
        }
        
        /**
         * 编码BCD时间
         * 
         * @param time 时间
         * @return 编码后的BCD时间
         */
        private Buffer encodeBcdTime(LocalDateTime time) {
            Buffer buffer = Buffer.buffer();
            
            // 年月日时分秒，每个字段用BCD编码
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
         * @return 解码后的时间
         */
        private LocalDateTime decodeBcdTime(Buffer buffer, int index) {
            try {
                int year = 2000 + ((buffer.getByte(index) >> 4) & 0x0F) * 10 + (buffer.getByte(index) & 0x0F);
                int month = ((buffer.getByte(index + 1) >> 4) & 0x0F) * 10 + (buffer.getByte(index + 1) & 0x0F);
                int day = ((buffer.getByte(index + 2) >> 4) & 0x0F) * 10 + (buffer.getByte(index + 2) & 0x0F);
                int hour = ((buffer.getByte(index + 3) >> 4) & 0x0F) * 10 + (buffer.getByte(index + 3) & 0x0F);
                int minute = ((buffer.getByte(index + 4) >> 4) & 0x0F) * 10 + (buffer.getByte(index + 4) & 0x0F);
                int second = ((buffer.getByte(index + 5) >> 4) & 0x0F) * 10 + (buffer.getByte(index + 5) & 0x0F);
                
                return LocalDateTime.of(year, month, day, hour, minute, second);
            } catch (Exception e) {
                return null;
            }
        }
        
        // Getters and Setters
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
        
        public int getTopLeftLatitude() {
            return topLeftLatitude;
        }
        
        public void setTopLeftLatitude(int topLeftLatitude) {
            this.topLeftLatitude = topLeftLatitude;
        }
        
        public int getTopLeftLongitude() {
            return topLeftLongitude;
        }
        
        public void setTopLeftLongitude(int topLeftLongitude) {
            this.topLeftLongitude = topLeftLongitude;
        }
        
        public int getBottomRightLatitude() {
            return bottomRightLatitude;
        }
        
        public void setBottomRightLatitude(int bottomRightLatitude) {
            this.bottomRightLatitude = bottomRightLatitude;
        }
        
        public int getBottomRightLongitude() {
            return bottomRightLongitude;
        }
        
        public void setBottomRightLongitude(int bottomRightLongitude) {
            this.bottomRightLongitude = bottomRightLongitude;
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
            return String.format("RectangularAreaItem{areaId=%d, areaAttribute=0x%04X, topLeft=[%d,%d], bottomRight=[%d,%d], startTime=%s, endTime=%s, maxSpeed=%s, overspeedDuration=%s}",
                    areaId, areaAttribute, topLeftLatitude, topLeftLongitude, 
                    bottomRightLatitude, bottomRightLongitude,
                    startTime != null ? startTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : "null",
                    endTime != null ? endTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : "null",
                    maxSpeed, overspeedDuration);
        }
        
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            RectangularAreaItem that = (RectangularAreaItem) o;
            return areaId == that.areaId &&
                   areaAttribute == that.areaAttribute &&
                   topLeftLatitude == that.topLeftLatitude &&
                   topLeftLongitude == that.topLeftLongitude &&
                   bottomRightLatitude == that.bottomRightLatitude &&
                   bottomRightLongitude == that.bottomRightLongitude &&
                   Objects.equals(startTime, that.startTime) &&
                   Objects.equals(endTime, that.endTime) &&
                   Objects.equals(maxSpeed, that.maxSpeed) &&
                   Objects.equals(overspeedDuration, that.overspeedDuration);
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(areaId, areaAttribute, topLeftLatitude, topLeftLongitude,
                              bottomRightLatitude, bottomRightLongitude, startTime, endTime, 
                              maxSpeed, overspeedDuration);
        }
    }
}