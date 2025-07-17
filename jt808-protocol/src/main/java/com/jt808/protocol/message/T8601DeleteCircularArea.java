package com.jt808.protocol.message;

import io.vertx.core.buffer.Buffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * T8601 删除圆形区域消息
 * 消息ID: 0x8601
 * 消息方向: 平台 → 终端
 * 
 * 消息体结构:
 * - 区域数(BYTE): 本条消息中包含的区域数，不超过125个，0为删除所有圆形区域
 * - 区域ID列表(DWORD[]): 要删除的区域ID列表
 * 
 * @author JT808-Vertx
 * @since 1.0.0
 */
public class T8601DeleteCircularArea extends JT808Message {
    
    /** 最大区域数量 */
    public static final int MAX_AREA_COUNT = 125;
    
    /** 删除所有区域的标识 */
    public static final byte DELETE_ALL = 0;
    
    /** 区域数量 */
    private byte areaCount;
    
    /** 区域ID列表 */
    private List<Long> areaIds;
    
    /**
     * 默认构造函数
     */
    public T8601DeleteCircularArea() {
        super();
        this.areaIds = new ArrayList<>();
    }
    

    
    /**
     * 带参数的构造函数
     * 
     * @param areaIds 要删除的区域ID列表
     */
    public T8601DeleteCircularArea(List<Long> areaIds) {
        super();
        this.areaIds = new ArrayList<>(areaIds != null ? areaIds : new ArrayList<>());
        this.areaCount = (byte) this.areaIds.size();
    }
    
    /**
     * 创建删除所有圆形区域的消息
     * 
     * @return 删除所有区域的消息实例
     */
    public static T8601DeleteCircularArea createDeleteAll() {
        T8601DeleteCircularArea message = new T8601DeleteCircularArea();
        message.areaCount = DELETE_ALL;
        message.areaIds.clear();
        return message;
    }
    
    /**
     * 创建删除指定圆形区域的消息
     * 
     * @param areaIds 要删除的区域ID列表
     * @return 删除指定区域的消息实例
     * @throws IllegalArgumentException 如果区域ID列表为空或超过最大数量
     */
    public static T8601DeleteCircularArea createDeleteSpecific(List<Long> areaIds) {
        if (areaIds == null || areaIds.isEmpty()) {
            throw new IllegalArgumentException("区域ID列表不能为空");
        }
        if (areaIds.size() > MAX_AREA_COUNT) {
            throw new IllegalArgumentException("区域数量不能超过" + MAX_AREA_COUNT + "个");
        }
        return new T8601DeleteCircularArea(areaIds);
    }
    
    /**
     * 创建删除单个圆形区域的消息
     * 
     * @param areaId 要删除的区域ID
     * @return 删除单个区域的消息实例
     */
    public static T8601DeleteCircularArea createDeleteSingle(long areaId) {
        return createDeleteSpecific(Arrays.asList(areaId));
    }
    
    @Override
    public int getMessageId() {
        return 0x8601;
    }
    
    @Override
    public Buffer encodeBody() {
        Buffer buffer = Buffer.buffer();
        
        // 写入区域数量
        buffer.appendByte(areaCount);
        
        // 如果不是删除所有区域，写入区域ID列表
        if (areaCount != DELETE_ALL) {
            for (Long areaId : areaIds) {
                buffer.appendUnsignedInt(areaId);
            }
        }
        
        return buffer;
    }
    
    @Override
    public void decodeBody(Buffer body) {
        if (body.length() < 1) {
            throw new IllegalArgumentException("消息体长度不足");
        }
        
        int readerIndex = 0;
        
        // 读取区域数量
        areaCount = body.getByte(readerIndex++);
        
        // 验证区域数量
        if (areaCount < 0) {
            throw new IllegalArgumentException("区域数量不能为负数");
        }
        if (areaCount > MAX_AREA_COUNT) {
            throw new IllegalArgumentException("区域数量不能超过" + MAX_AREA_COUNT + "个");
        }
        
        // 清空现有区域ID列表
        areaIds.clear();
        
        // 如果不是删除所有区域，读取区域ID列表
        if (areaCount != DELETE_ALL) {
            // 验证消息体长度
            int expectedLength = 1 + areaCount * 4; // 1字节区域数 + N个4字节区域ID
            if (body.length() < expectedLength) {
                throw new IllegalArgumentException("消息体长度不足，期望" + expectedLength + "字节，实际" + body.length() + "字节");
            }
            
            // 读取区域ID列表
            for (int i = 0; i < getUnsignedAreaCount(); i++) {
                long areaId = body.getUnsignedInt(readerIndex);
                areaIds.add(areaId);
                readerIndex += 4;
            }
        }
    }
    
    /**
     * 获取区域数量（无符号值）
     * 
     * @return 区域数量
     */
    public int getUnsignedAreaCount() {
        return Byte.toUnsignedInt(areaCount);
    }
    
    /**
     * 获取区域数量
     * 
     * @return 区域数量
     */
    public byte getAreaCount() {
        return areaCount;
    }
    
    /**
     * 设置区域数量
     * 
     * @param areaCount 区域数量
     */
    public void setAreaCount(byte areaCount) {
        this.areaCount = areaCount;
    }
    
    /**
     * 获取区域ID列表
     * 
     * @return 区域ID列表
     */
    public List<Long> getAreaIds() {
        return new ArrayList<>(areaIds);
    }
    
    /**
     * 设置区域ID列表
     * 
     * @param areaIds 区域ID列表
     */
    public void setAreaIds(List<Long> areaIds) {
        this.areaIds = new ArrayList<>(areaIds != null ? areaIds : new ArrayList<>());
        this.areaCount = (byte) this.areaIds.size();
    }
    
    /**
     * 添加区域ID
     * 
     * @param areaId 区域ID
     * @throws IllegalStateException 如果区域数量已达到最大值
     */
    public void addAreaId(long areaId) {
        if (areaIds.size() >= MAX_AREA_COUNT) {
            throw new IllegalStateException("区域数量不能超过" + MAX_AREA_COUNT + "个");
        }
        areaIds.add(areaId);
        areaCount = (byte) areaIds.size();
    }
    
    /**
     * 移除区域ID
     * 
     * @param areaId 区域ID
     * @return 是否成功移除
     */
    public boolean removeAreaId(long areaId) {
        boolean removed = areaIds.remove(areaId);
        if (removed) {
            areaCount = (byte) areaIds.size();
        }
        return removed;
    }
    
    /**
     * 清空所有区域ID
     */
    public void clearAreaIds() {
        areaIds.clear();
        areaCount = DELETE_ALL;
    }
    
    /**
     * 检查是否为删除所有区域
     * 
     * @return 如果是删除所有区域返回true，否则返回false
     */
    public boolean isDeleteAll() {
        return areaCount == DELETE_ALL;
    }
    
    /**
     * 检查是否为删除指定区域
     * 
     * @return 如果是删除指定区域返回true，否则返回false
     */
    public boolean isDeleteSpecific() {
        return areaCount != DELETE_ALL;
    }
    
    /**
     * 检查是否包含指定区域ID
     * 
     * @param areaId 区域ID
     * @return 如果包含返回true，否则返回false
     */
    public boolean containsAreaId(long areaId) {
        return areaIds.contains(areaId);
    }
    
    /**
     * 获取删除类型描述
     * 
     * @return 删除类型描述
     */
    public String getDeleteTypeDescription() {
        return isDeleteAll() ? "删除所有区域" : "删除指定区域";
    }
    
    /**
     * 获取消息描述
     * 
     * @return 消息描述
     */
    public String getDescription() {
        if (isDeleteAll()) {
            return "删除圆形区域[删除所有区域]"; 
        } else {
            return String.format("删除圆形区域[删除指定区域, 区域数量: %d]", getUnsignedAreaCount());
        }
    }
    
    @Override
    public String toString() {
        return String.format("T8601DeleteCircularArea{messageId=0x%04X, areaCount=%d, deleteType=%s, areaIds=%s}",
                getMessageId(), getUnsignedAreaCount(), getDeleteTypeDescription(), 
                isDeleteAll() ? "[]" : areaIds.toString());
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        T8601DeleteCircularArea that = (T8601DeleteCircularArea) o;
        return areaCount == that.areaCount && Objects.equals(areaIds, that.areaIds);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(areaCount, areaIds);
    }
}