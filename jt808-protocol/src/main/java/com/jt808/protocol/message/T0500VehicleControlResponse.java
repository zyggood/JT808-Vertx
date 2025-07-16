package com.jt808.protocol.message;

import io.vertx.core.buffer.Buffer;

/**
 * 0x0500 车辆控制应答
 * 终端收到车辆控制消息后，执行相应的控制动作，并回复车辆控制应答消息
 */
public class T0500VehicleControlResponse extends JT808Message {

    /**
     * 应答流水号 - 对应的车辆控制消息的流水号
     */
    private int responseSerialNumber;

    /**
     * 位置信息汇报消息体
     */
    private T0200LocationReport locationReport;

    public T0500VehicleControlResponse() {
    }

    public T0500VehicleControlResponse(int responseSerialNumber, T0200LocationReport locationReport) {
        this.responseSerialNumber = responseSerialNumber;
        this.locationReport = locationReport;
    }

    public int getResponseSerialNumber() {
        return responseSerialNumber;
    }

    public void setResponseSerialNumber(int responseSerialNumber) {
        this.responseSerialNumber = responseSerialNumber;
    }

    public T0200LocationReport getLocationReport() {
        return locationReport;
    }

    public void setLocationReport(T0200LocationReport locationReport) {
        this.locationReport = locationReport;
    }

    /**
     * 创建车辆控制应答消息
     * 
     * @param responseSerialNumber 应答流水号
     * @param locationReport 位置信息汇报
     * @return 车辆控制应答消息
     */
    public static T0500VehicleControlResponse create(int responseSerialNumber, T0200LocationReport locationReport) {
        return new T0500VehicleControlResponse(responseSerialNumber, locationReport);
    }

    /**
     * 创建车辆控制应答消息（简化版）
     * 
     * @param responseSerialNumber 应答流水号
     * @return 车辆控制应答消息（不包含位置信息）
     */
    public static T0500VehicleControlResponse create(int responseSerialNumber) {
        return new T0500VehicleControlResponse(responseSerialNumber, null);
    }

    /**
     * 检查是否包含位置信息
     * 
     * @return true-包含位置信息，false-不包含位置信息
     */
    public boolean hasLocationReport() {
        return locationReport != null;
    }

    /**
     * 获取应答流水号的无符号值
     * 
     * @return 应答流水号的无符号值
     */
    public long getResponseSerialNumberUnsigned() {
        return Integer.toUnsignedLong(responseSerialNumber);
    }

    /**
     * 获取消息描述
     * 
     * @return 消息描述
     */
    public String getMessageDescription() {
        return "车辆控制应答";
    }

    /**
     * 获取应答状态描述
     * 
     * @return 应答状态描述
     */
    public String getResponseDescription() {
        if (hasLocationReport()) {
            return "控制成功，包含位置信息";
        } else {
            return "控制应答，无位置信息";
        }
    }

    @Override
    public int getMessageId() {
        return 0x0500;
    }

    @Override
    public Buffer encodeBody() {
        Buffer buffer = Buffer.buffer();
        
        // 写入应答流水号（2字节）
        buffer.appendUnsignedShort(responseSerialNumber);
        
        // 写入位置信息汇报消息体（如果存在）
        if (locationReport != null) {
            Buffer locationData = locationReport.encodeBody();
            buffer.appendBuffer(locationData);
        }
        
        return buffer;
    }

    @Override
    public void decodeBody(Buffer buffer) {
        if (buffer == null || buffer.length() < 2) {
            throw new IllegalArgumentException("车辆控制应答消息体长度不能少于2字节");
        }
        
        // 读取应答流水号（2字节）
        this.responseSerialNumber = buffer.getUnsignedShort(0);
        
        // 读取位置信息汇报消息体（如果存在）
        if (buffer.length() > 2) {
            Buffer locationData = buffer.getBuffer(2, buffer.length());
            
            this.locationReport = new T0200LocationReport();
            this.locationReport.decodeBody(locationData);
        }
    }

    @Override
    public String toString() {
        return "T0500VehicleControlResponse{" +
                "responseSerialNumber=" + responseSerialNumber +
                ", hasLocationReport=" + hasLocationReport() +
                ", locationReport=" + (locationReport != null ? locationReport.toString() : "null") +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        
        T0500VehicleControlResponse that = (T0500VehicleControlResponse) o;
        
        if (responseSerialNumber != that.responseSerialNumber) return false;
        return locationReport != null ? locationReport.equals(that.locationReport) : that.locationReport == null;
    }

    @Override
    public int hashCode() {
        int result = responseSerialNumber;
        result = 31 * result + (locationReport != null ? locationReport.hashCode() : 0);
        return result;
    }

    /**
     * 应答状态常量
     */
    public static class ResponseStatus {
        /** 控制成功 */
        public static final String SUCCESS = "控制成功";
        /** 控制失败 */
        public static final String FAILURE = "控制失败";
        /** 控制超时 */
        public static final String TIMEOUT = "控制超时";
        /** 不支持该控制 */
        public static final String UNSUPPORTED = "不支持该控制";
    }
}