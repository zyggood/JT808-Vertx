package com.jt808.protocol.message;

import io.vertx.core.buffer.Buffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * T8603 删除矩形区域消息
 * 消息ID: 0x8603
 * 消息方向: 平台 → 终端
 * 
 * 消息体结构:
 * - 区域数(BYTE): 本条消息中包含的区域数，不超过125个，0为删除所有矩形区域
 * - 区域ID列表(DWORD[]): 要删除的区域ID列表
 * 
 * @author JT808-Vertx
 * @since 1.0.0
 */
public class T8603DeleteRectangularArea extends JT808Message {
    
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
    public T8603DeleteRectangularArea() {
        super();
        this.areaIds = new ArrayList<>();
    }
    
    /**
     * 带参数的构造函数
     * 
     * @param areaIds 要删除的区域ID列表
     */
    public T8603DeleteRectangularArea(List<Long> areaIds) {
        super();
        this.areaIds = new ArrayList<>(areaIds != null ? areaIds : new ArrayList<>());
        this.areaCount = (byte) this.areaIds.size();
    }
    
    /**
     * 创建删除所有矩形区域的消息
     * 
     * @return 删除所有矩形区域的消息实例
     */
    public static T8603DeleteRectangularArea createDeleteAll() {
        T8603DeleteRectangularArea message = new T8603DeleteRectangularArea();
        message.areaCount = DELETE_ALL;
        return message;
    }
    
    /**
     * 创建删除指定矩形区域的消息
     * 
     * @param areaIds 要删除的区域ID列表
     * @return 删除指定矩形区域的消息实例
     * @throws IllegalArgumentException 如果区域ID列表为空或超过最大数量
     */
    public static T8603DeleteRectangularArea createDeleteSpecific(List<Long> areaIds) {
        if (areaIds == null || areaIds.isEmpty()) {
            throw new IllegalArgumentException("区域ID列表不能为空");
        }
        if (areaIds.size() > MAX_AREA_COUNT) {
            throw new IllegalArgumentException("区域数量不能超过" + MAX_AREA_COUNT + "个");
        }
        return new T8603DeleteRectangularArea(areaIds);
    }
    
    /**
     * 创建删除单个矩形区域的消息
     * 
     * @param areaId 要删除的区域ID
     * @return 删除单个矩形区域的消息实例
     */
    public static T8603DeleteRectangularArea createDeleteSingle(long areaId) {
        return new T8603DeleteRectangularArea(Arrays.asList(areaId));
    }
    
    @Override
    public int getMessageId() {
        return 0x8603;
    }
    
    @Override
    public Buffer encodeBody() {
        Buffer buffer = Buffer.buffer();
        buffer.appendByte(areaCount);
        
        // 如果不是删除所有区域，则写入区域ID列表
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
        
        // 读取区域数量
        areaCount = body.getByte(0);
        
        // 清空现有区域ID列表
        areaIds.clear();
        
        // 如果不是删除所有区域，则读取区域ID列表
        if (areaCount != DELETE_ALL) {
            int expectedLength = 1 + areaCount * 4; // 1字节区域数 + 每个区域ID 4字节
            if (body.length() != expectedLength) {
                throw new IllegalArgumentException(
                    String.format("消息体长度不正确，期望%d字节，实际%d字节", expectedLength, body.length()));
            }
            
            // 读取区域ID列表
            for (int i = 0; i < areaCount; i++) {
                int offset = 1 + i * 4;
                long areaId = body.getUnsignedInt(offset);
                areaIds.add(areaId);
            }
        } else {
            // 删除所有区域时，消息体只有1字节
            if (body.length() != 1) {
                throw new IllegalArgumentException(
                    String.format("删除所有区域时消息体长度应为1字节，实际%d字节", body.length()));
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
     * @param areaId 要移除的区域ID
     * @return 如果移除成功返回true，否则返回false
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
        areaCount = 0;
    }
    
    /**
     * 判断是否为删除所有区域
     * 
     * @return 如果是删除所有区域返回true，否则返回false
     */
    public boolean isDeleteAll() {
        return areaCount == DELETE_ALL;
    }
    
    /**
     * 判断是否为删除指定区域
     * 
     * @return 如果是删除指定区域返回true，否则返回false
     */
    public boolean isDeleteSpecific() {
        return areaCount != DELETE_ALL && !areaIds.isEmpty();
    }
    
    /**
     * 判断是否包含指定区域ID
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
        if (isDeleteAll()) {
            return "删除所有矩形区域";
        } else if (isDeleteSpecific()) {
            return "删除指定矩形区域";
        } else {
            return "无效的删除操作";
        }
    }
    
    /**
     * 获取消息描述
     * 
     * @return 消息描述
     */
    public String getDescription() {
        return String.format("删除矩形区域消息 - %s，区域数量: %d", 
            getDeleteTypeDescription(), getUnsignedAreaCount());
    }
    
    @Override
    public String toString() {
        return String.format("T8603DeleteRectangularArea{areaCount=%d, areaIds=%s}", 
            getUnsignedAreaCount(), areaIds);
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        T8603DeleteRectangularArea that = (T8603DeleteRectangularArea) o;
        return areaCount == that.areaCount && Objects.equals(areaIds, that.areaIds);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(areaCount, areaIds);
    }
}