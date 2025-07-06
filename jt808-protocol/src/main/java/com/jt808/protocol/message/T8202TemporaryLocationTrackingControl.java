package com.jt808.protocol.message;

import io.vertx.core.buffer.Buffer;

/**
 * T8202临时位置跟踪控制消息
 * 
 * 消息ID: 0x8202
 * 消息体长度: 6字节
 * 
 * 该消息用于平台向终端发送临时位置跟踪控制指令。
 * 终端收到此消息后，在有效期内按指定时间间隔发送位置汇报。
 * 
 * 消息体结构:
 * - 时间间隔 (WORD, 2字节): 单位为秒，0则停止跟踪
 * - 位置跟踪有效期 (DWORD, 4字节): 单位为秒，跟踪的有效期
 * 
 * @author JT808 Protocol Team
 * @version 1.0
 * @since 1.0
 */
public class T8202TemporaryLocationTrackingControl extends JT808Message {
    
    /** 时间间隔，单位：秒，0则停止跟踪 */
    private int timeInterval;
    
    /** 位置跟踪有效期，单位：秒 */
    private long validityPeriod;
    
    /**
     * 默认构造函数
     */
    public T8202TemporaryLocationTrackingControl() {
        super();
    }
    
    /**
     * 构造临时位置跟踪控制消息
     * 
     * @param timeInterval 时间间隔（秒），0则停止跟踪
     * @param validityPeriod 位置跟踪有效期（秒）
     */
    public T8202TemporaryLocationTrackingControl(int timeInterval, long validityPeriod) {
        this();
        this.timeInterval = timeInterval;
        this.validityPeriod = validityPeriod;
    }
    
    /**
     * 获取消息ID
     * 
     * @return 消息ID 0x8202
     */
    @Override
    public int getMessageId() {
        return 0x8202;
    }
    
    /**
     * 编码消息体
     * 
     * @return 编码后的消息体
     */
    @Override
    public Buffer encodeBody() {
        Buffer buffer = Buffer.buffer();
        
        // 时间间隔 (WORD, 2字节，大端序)
        buffer.appendUnsignedShort(timeInterval & 0xFFFF);
        
        // 位置跟踪有效期 (DWORD, 4字节，大端序)
        buffer.appendUnsignedInt(validityPeriod & 0xFFFFFFFFL);
        
        return buffer;
    }
    
    /**
     * 解码消息体
     * 
     * @param body 消息体数据
     * @throws IllegalArgumentException 如果消息体长度不正确
     */
    @Override
    public void decodeBody(Buffer body) {
        if (body == null) {
            throw new IllegalArgumentException("消息体不能为空");
        }
        
        // 检查消息体长度
        if (body.length() < 6) {
            throw new IllegalArgumentException(
                "临时位置跟踪控制消息体长度应为6字节，实际长度: " + body.length() + " 字节");
        }
        
        int index = 0;
        
        // 时间间隔 (WORD, 2字节，大端序)
        timeInterval = body.getUnsignedShort(index);
        index += 2;
        
        // 位置跟踪有效期 (DWORD, 4字节，大端序)
        validityPeriod = body.getUnsignedInt(index);
    }
    
    /**
     * 创建开始跟踪的消息
     * 
     * @param intervalSeconds 时间间隔（秒）
     * @param validitySeconds 有效期（秒）
     * @return T8202TemporaryLocationTrackingControl实例
     */
    public static T8202TemporaryLocationTrackingControl createStartTracking(int intervalSeconds, long validitySeconds) {
        if (intervalSeconds <= 0) {
            throw new IllegalArgumentException("时间间隔必须大于0秒");
        }
        if (validitySeconds <= 0) {
            throw new IllegalArgumentException("有效期必须大于0秒");
        }
        return new T8202TemporaryLocationTrackingControl(intervalSeconds, validitySeconds);
    }
    
    /**
     * 创建停止跟踪的消息
     * 
     * @return T8202TemporaryLocationTrackingControl实例
     */
    public static T8202TemporaryLocationTrackingControl createStopTracking() {
        return new T8202TemporaryLocationTrackingControl(0, 0);
    }
    
    /**
     * 检查是否为停止跟踪命令
     * 
     * @return 如果时间间隔为0则返回true
     */
    public boolean isStopTracking() {
        return timeInterval == 0;
    }
    
    /**
     * 获取消息描述
     * 
     * @return 消息描述
     */
    public String getMessageDescription() {
        return "临时位置跟踪控制";
    }
    
    /**
     * 获取跟踪状态描述
     * 
     * @return 跟踪状态描述
     */
    public String getTrackingStatusDescription() {
        if (isStopTracking()) {
            return "停止跟踪";
        } else {
            return String.format("开始跟踪 - 间隔:%d秒, 有效期:%d秒", timeInterval, validityPeriod);
        }
    }
    
    // Getters and Setters
    
    /**
     * 获取时间间隔
     * 
     * @return 时间间隔（秒）
     */
    public int getTimeInterval() {
        return timeInterval;
    }
    
    /**
     * 设置时间间隔
     * 
     * @param timeInterval 时间间隔（秒），0则停止跟踪
     */
    public void setTimeInterval(int timeInterval) {
        if (timeInterval < 0) {
            throw new IllegalArgumentException("时间间隔不能为负数");
        }
        this.timeInterval = timeInterval;
    }
    
    /**
     * 获取位置跟踪有效期
     * 
     * @return 位置跟踪有效期（秒）
     */
    public long getValidityPeriod() {
        return validityPeriod;
    }
    
    /**
     * 设置位置跟踪有效期
     * 
     * @param validityPeriod 位置跟踪有效期（秒）
     */
    public void setValidityPeriod(long validityPeriod) {
        if (validityPeriod < 0) {
            throw new IllegalArgumentException("有效期不能为负数");
        }
        this.validityPeriod = validityPeriod;
    }
    
    /**
     * 返回字符串表示
     * 
     * @return 字符串表示
     */
    @Override
    public String toString() {
        return "T8202TemporaryLocationTrackingControl{" +
                "messageId=0x" + Integer.toHexString(getMessageId()).toUpperCase() +
                ", description='" + getMessageDescription() + "'" +
                ", timeInterval=" + timeInterval +
                ", validityPeriod=" + validityPeriod +
                ", status='" + getTrackingStatusDescription() + "'" +
                ", header=" + getHeader() +
                "}";
    }
    
    /**
     * 检查两个T8202TemporaryLocationTrackingControl对象是否相等
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
        
        T8202TemporaryLocationTrackingControl that = (T8202TemporaryLocationTrackingControl) obj;
        return timeInterval == that.timeInterval && validityPeriod == that.validityPeriod;
    }
    
    /**
     * 计算哈希码
     * 
     * @return 哈希码
     */
    @Override
    public int hashCode() {
        int result = getMessageId();
        result = 31 * result + timeInterval;
        result = 31 * result + Long.hashCode(validityPeriod);
        return result;
    }
}