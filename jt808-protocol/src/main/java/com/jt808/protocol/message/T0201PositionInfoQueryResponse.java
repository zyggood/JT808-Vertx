package com.jt808.protocol.message;

import io.vertx.core.buffer.Buffer;

/**
 * T0201位置信息查询应答消息
 * 
 * 消息ID: 0x0201
 * 消息体结构:
 * - 应答流水号 (WORD, 2字节): 对应的位置信息查询消息的流水号
 * - 位置信息汇报: 完整的位置信息汇报数据
 * 
 * 该消息是终端对平台位置信息查询(0x8201)的应答，包含当前位置信息。
 * 
 * @author JT808 Protocol Team
 * @version 1.0
 * @since 1.0
 */
public class T0201PositionInfoQueryResponse extends JT808Message {
    
    /** 应答流水号 - 对应的位置信息查询消息的流水号 */
    private int responseSerialNumber;
    
    /** 位置信息汇报 */
    private T0200LocationReport locationReport;
    
    /**
     * 默认构造函数
     */
    public T0201PositionInfoQueryResponse() {
        super();
        this.locationReport = new T0200LocationReport();
    }
    
    /**
     * 带参数构造函数
     * 
     * @param responseSerialNumber 应答流水号
     * @param locationReport 位置信息汇报
     */
    public T0201PositionInfoQueryResponse(int responseSerialNumber, T0200LocationReport locationReport) {
        super();
        this.responseSerialNumber = responseSerialNumber;
        this.locationReport = locationReport != null ? locationReport : new T0200LocationReport();
    }
    
    /**
     * 获取消息ID
     * 
     * @return 消息ID 0x0201
     */
    @Override
    public int getMessageId() {
        return 0x0201;
    }
    
    /**
     * 编码消息体
     * 
     * @return 编码后的消息体
     */
    @Override
    public Buffer encodeBody() {
        Buffer buffer = Buffer.buffer();
        
        // 应答流水号 (2字节)
        buffer.appendUnsignedShort(responseSerialNumber);
        
        // 位置信息汇报数据
        if (locationReport != null) {
            Buffer locationData = locationReport.encodeBody();
            buffer.appendBuffer(locationData);
        }
        
        return buffer;
    }
    
    /**
     * 解码消息体
     * 
     * @param body 消息体数据
     * @throws IllegalArgumentException 如果消息体长度不足
     */
    @Override
    public void decodeBody(Buffer body) {
        if (body == null || body.length() < 2) {
            throw new IllegalArgumentException("位置信息查询应答消息体长度不足，至少需要2字节，实际长度: " + 
                (body != null ? body.length() : 0));
        }
        
        int index = 0;
        
        // 应答流水号 (2字节)
        responseSerialNumber = body.getUnsignedShort(index);
        index += 2;
        
        // 位置信息汇报数据
        if (index < body.length()) {
            Buffer locationData = body.getBuffer(index, body.length());
            if (locationReport == null) {
                locationReport = new T0200LocationReport();
            }
            locationReport.decodeBody(locationData);
        } else {
            throw new IllegalArgumentException("位置信息查询应答消息体缺少位置信息汇报数据");
        }
    }
    
    /**
     * 获取应答流水号
     * 
     * @return 应答流水号
     */
    public int getResponseSerialNumber() {
        return responseSerialNumber;
    }
    
    /**
     * 设置应答流水号
     * 
     * @param responseSerialNumber 应答流水号
     */
    public void setResponseSerialNumber(int responseSerialNumber) {
        this.responseSerialNumber = responseSerialNumber;
    }
    
    /**
     * 获取位置信息汇报
     * 
     * @return 位置信息汇报
     */
    public T0200LocationReport getLocationReport() {
        return locationReport;
    }
    
    /**
     * 设置位置信息汇报
     * 
     * @param locationReport 位置信息汇报
     */
    public void setLocationReport(T0200LocationReport locationReport) {
        this.locationReport = locationReport;
    }
    
    /**
     * 创建位置信息查询应答消息的静态工厂方法
     * 
     * @param responseSerialNumber 应答流水号
     * @param locationReport 位置信息汇报
     * @return T0201PositionInfoQueryResponse实例
     */
    public static T0201PositionInfoQueryResponse create(int responseSerialNumber, T0200LocationReport locationReport) {
        return new T0201PositionInfoQueryResponse(responseSerialNumber, locationReport);
    }
    
    /**
     * 创建带有当前位置信息的查询应答消息
     * 
     * @param responseSerialNumber 应答流水号
     * @param latitude 纬度（度）
     * @param longitude 经度（度）
     * @param altitude 高程（米）
     * @param speed 速度（1/10km/h）
     * @param direction 方向（0-359度）
     * @return T0201PositionInfoQueryResponse实例
     */
    public static T0201PositionInfoQueryResponse createWithPosition(int responseSerialNumber, 
            double latitude, double longitude, int altitude, int speed, int direction) {
        T0200LocationReport locationReport = new T0200LocationReport();
        locationReport.setLatitudeDegrees(latitude);
        locationReport.setLongitudeDegrees(longitude);
        locationReport.setAltitude(altitude);
        locationReport.setSpeed(speed);
        locationReport.setDirection(direction);
        locationReport.setDateTime(java.time.LocalDateTime.now());
        
        return new T0201PositionInfoQueryResponse(responseSerialNumber, locationReport);
    }
    
    /**
     * 获取消息描述
     * 
     * @return 消息描述
     */
    public String getMessageDescription() {
        return "位置信息查询应答";
    }
    
    /**
     * 返回字符串表示
     * 
     * @return 字符串表示
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("T0201PositionInfoQueryResponse{");
        sb.append("messageId=0x").append(Integer.toHexString(getMessageId()).toUpperCase());
        sb.append(", description='").append(getMessageDescription()).append("'");
        sb.append(", responseSerialNumber=").append(responseSerialNumber);
        
        if (locationReport != null) {
            sb.append(", latitude=").append(locationReport.getLatitudeDegrees());
            sb.append(", longitude=").append(locationReport.getLongitudeDegrees());
            sb.append(", altitude=").append(locationReport.getAltitude());
            sb.append(", speed=").append(locationReport.getSpeed());
            sb.append(", direction=").append(locationReport.getDirection());
            sb.append(", dateTime=").append(locationReport.getDateTime());
        } else {
            sb.append(", locationReport=null");
        }
        
        sb.append(", header=").append(getHeader());
        sb.append("}");
        
        return sb.toString();
    }
    
    /**
     * 检查两个T0201PositionInfoQueryResponse对象是否相等
     * 
     * @param obj 要比较的对象
     * @return 如果相等返回true，否则返回false
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        
        T0201PositionInfoQueryResponse that = (T0201PositionInfoQueryResponse) obj;
        
        if (responseSerialNumber != that.responseSerialNumber) {
            return false;
        }
        
        // 处理locationReport的比较
        if (locationReport == null && that.locationReport == null) {
            return true;
        }
        if (locationReport == null || that.locationReport == null) {
            return false;
        }
        
        return locationReport.equals(that.locationReport);
    }
    
    /**
     * 计算哈希码
     * 
     * @return 哈希码
     */
    @Override
    public int hashCode() {
        int result = getMessageId();
        result = 31 * result + responseSerialNumber;
        result = 31 * result + (locationReport != null ? locationReport.hashCode() : 0);
        return result;
    }
}