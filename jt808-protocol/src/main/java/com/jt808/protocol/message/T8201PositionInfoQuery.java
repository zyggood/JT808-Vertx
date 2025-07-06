package com.jt808.protocol.message;

import io.vertx.core.buffer.Buffer;

/**
 * T8201位置信息查询消息
 * 
 * 消息ID: 0x8201
 * 消息体: 无
 * 
 * 该消息用于平台向终端查询位置信息，消息体为空。
 * 终端收到此消息后，应立即上报当前位置信息。
 * 
 * @author JT808 Protocol Team
 * @version 1.0
 * @since 1.0
 */
public class T8201PositionInfoQuery extends JT808Message {
    
    /**
     * 默认构造函数
     */
    public T8201PositionInfoQuery() {
        super();
    }
    
    /**
     * 获取消息ID
     * 
     * @return 消息ID 0x8201
     */
    @Override
    public int getMessageId() {
        return 0x8201;
    }
    
    /**
     * 编码消息体
     * 
     * 位置信息查询消息体为空，返回空的Buffer
     * 
     * @return 空的Buffer
     */
    @Override
    public Buffer encodeBody() {
        return Buffer.buffer();
    }
    
    /**
     * 解码消息体
     * 
     * 位置信息查询消息体为空，无需解码任何数据
     * 
     * @param body 消息体数据（应为空）
     * @throws IllegalArgumentException 如果消息体不为空
     */
    @Override
    public void decodeBody(Buffer body) {
        if (body != null && body.length() > 0) {
            throw new IllegalArgumentException("位置信息查询消息体应为空，但接收到 " + body.length() + " 字节数据");
        }
        // 消息体为空，无需解码任何数据
    }
    
    /**
     * 创建位置信息查询消息的静态工厂方法
     * 
     * @return T8201PositionInfoQuery实例
     */
    public static T8201PositionInfoQuery create() {
        return new T8201PositionInfoQuery();
    }
    
    /**
     * 获取消息描述
     * 
     * @return 消息描述
     */
    public String getMessageDescription() {
        return "位置信息查询";
    }
    
    /**
     * 返回字符串表示
     * 
     * @return 字符串表示
     */
    @Override
    public String toString() {
        return "T8201PositionInfoQuery{" +
                "messageId=0x" + Integer.toHexString(getMessageId()).toUpperCase() +
                ", description='" + getMessageDescription() + "'" +
                ", bodyLength=0" +
                ", header=" + getHeader() +
                "}";
    }
    
    /**
     * 检查两个T8201PositionInfoQuery对象是否相等
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
        // 由于消息体为空，只要是同一类型的消息就认为相等
        return true;
    }
    
    /**
     * 计算哈希码
     * 
     * @return 哈希码
     */
    @Override
    public int hashCode() {
        return getMessageId();
    }
}