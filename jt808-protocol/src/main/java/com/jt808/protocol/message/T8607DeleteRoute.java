package com.jt808.protocol.message;

import io.vertx.core.buffer.Buffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * T8607 删除路线消息
 * 消息ID: 0x8607
 * 消息方向: 平台 → 终端
 * 
 * 消息体结构:
 * - 路线数(BYTE): 本条消息中包含的路线数，不超过125个，0为删除所有路线
 * - 路线ID列表(DWORD[]): 要删除的路线ID列表
 * 
 * @author JT808-Vertx
 * @since 1.0.0
 */
public class T8607DeleteRoute extends JT808Message {
    
    /** 最大路线数量 */
    public static final int MAX_ROUTE_COUNT = 125;
    
    /** 删除所有路线的标识 */
    public static final byte DELETE_ALL = 0;
    
    /** 路线数量 */
    private byte routeCount;
    
    /** 路线ID列表 */
    private List<Long> routeIds;
    
    /**
     * 默认构造函数
     */
    public T8607DeleteRoute() {
        super();
        this.routeIds = new ArrayList<>();
    }
    
    /**
     * 带参数的构造函数
     * 
     * @param routeIds 要删除的路线ID列表
     */
    public T8607DeleteRoute(List<Long> routeIds) {
        super();
        this.routeIds = new ArrayList<>(routeIds != null ? routeIds : new ArrayList<>());
        this.routeCount = (byte) this.routeIds.size();
    }
    
    /**
     * 创建删除所有路线的消息
     * 
     * @return 删除所有路线的消息实例
     */
    public static T8607DeleteRoute createDeleteAll() {
        T8607DeleteRoute message = new T8607DeleteRoute();
        message.routeCount = DELETE_ALL;
        return message;
    }
    
    /**
     * 创建删除指定路线的消息
     * 
     * @param routeIds 要删除的路线ID列表
     * @return 删除指定路线的消息实例
     * @throws IllegalArgumentException 如果路线ID列表为空或超过最大数量
     */
    public static T8607DeleteRoute createDeleteSpecific(List<Long> routeIds) {
        if (routeIds == null || routeIds.isEmpty()) {
            throw new IllegalArgumentException("路线ID列表不能为空");
        }
        if (routeIds.size() > MAX_ROUTE_COUNT) {
            throw new IllegalArgumentException("路线数量不能超过" + MAX_ROUTE_COUNT + "个");
        }
        return new T8607DeleteRoute(routeIds);
    }
    
    /**
     * 创建删除单个路线的消息
     * 
     * @param routeId 要删除的路线ID
     * @return 删除单个路线的消息实例
     */
    public static T8607DeleteRoute createDeleteSingle(long routeId) {
        return new T8607DeleteRoute(Arrays.asList(routeId));
    }
    
    @Override
    public int getMessageId() {
        return 0x8607;
    }
    
    @Override
    public Buffer encodeBody() {
        Buffer buffer = Buffer.buffer();
        
        // 路线数 (BYTE)
        buffer.appendByte(routeCount);
        
        // 路线ID列表 (DWORD[])
        if (routeCount > 0) {
            for (Long routeId : routeIds) {
                buffer.appendUnsignedInt(routeId);
            }
        }
        
        return buffer;
    }
    
    @Override
    public void decodeBody(Buffer body) {
        if (body.length() < 1) {
            throw new IllegalArgumentException("消息体长度不足，至少需要1字节");
        }
        
        int index = 0;
        
        // 路线数 (BYTE)
        this.routeCount = body.getByte(index);
        index += 1;
        
        // 路线ID列表 (DWORD[])
        this.routeIds.clear();
        if (routeCount > 0) {
            int expectedLength = 1 + routeCount * 4; // 1字节路线数 + 路线数*4字节路线ID
            if (body.length() < expectedLength) {
                throw new IllegalArgumentException(
                    String.format("消息体长度不足，期望%d字节，实际%d字节", expectedLength, body.length()));
            }
            
            for (int i = 0; i < routeCount; i++) {
                if (index + 4 > body.length()) {
                    throw new IllegalArgumentException(
                        String.format("消息体长度不足，无法读取第%d个路线ID", i + 1));
                }
                long routeId = body.getUnsignedInt(index);
                this.routeIds.add(routeId);
                index += 4;
            }
        }
    }
    
    /**
     * 获取无符号路线数量
     * 
     * @return 无符号路线数量
     */
    public int getUnsignedRouteCount() {
        return Byte.toUnsignedInt(routeCount);
    }
    
    /**
     * 获取路线数量
     * 
     * @return 路线数量
     */
    public byte getRouteCount() {
        return routeCount;
    }
    
    /**
     * 设置路线数量
     * 
     * @param routeCount 路线数量
     */
    public void setRouteCount(byte routeCount) {
        this.routeCount = routeCount;
    }
    
    /**
     * 获取路线ID列表
     * 
     * @return 路线ID列表的副本
     */
    public List<Long> getRouteIds() {
        return new ArrayList<>(routeIds);
    }
    
    /**
     * 设置路线ID列表
     * 
     * @param routeIds 路线ID列表
     */
    public void setRouteIds(List<Long> routeIds) {
        this.routeIds = new ArrayList<>(routeIds != null ? routeIds : new ArrayList<>());
        this.routeCount = (byte) this.routeIds.size();
    }
    
    /**
     * 添加路线ID
     * 
     * @param routeId 路线ID
     * @throws IllegalStateException 如果路线数量已达到最大值
     */
    public void addRouteId(long routeId) {
        if (routeIds.size() >= MAX_ROUTE_COUNT) {
            throw new IllegalStateException("路线数量不能超过" + MAX_ROUTE_COUNT + "个");
        }
        this.routeIds.add(routeId);
        this.routeCount = (byte) this.routeIds.size();
    }
    
    /**
     * 移除路线ID
     * 
     * @param routeId 要移除的路线ID
     * @return 如果成功移除返回true，否则返回false
     */
    public boolean removeRouteId(long routeId) {
        boolean removed = this.routeIds.remove(routeId);
        if (removed) {
            this.routeCount = (byte) this.routeIds.size();
        }
        return removed;
    }
    
    /**
     * 清空所有路线ID
     */
    public void clearRouteIds() {
        this.routeIds.clear();
        this.routeCount = 0;
    }
    
    /**
     * 判断是否为删除所有路线
     * 
     * @return 如果是删除所有路线返回true，否则返回false
     */
    public boolean isDeleteAll() {
        return routeCount == DELETE_ALL;
    }
    
    /**
     * 判断是否为删除指定路线
     * 
     * @return 如果是删除指定路线返回true，否则返回false
     */
    public boolean isDeleteSpecific() {
        return routeCount > DELETE_ALL && !routeIds.isEmpty();
    }
    
    /**
     * 判断是否包含指定路线ID
     * 
     * @param routeId 路线ID
     * @return 如果包含指定路线ID返回true，否则返回false
     */
    public boolean containsRouteId(long routeId) {
        return routeIds.contains(routeId);
    }
    
    /**
     * 获取删除类型描述
     * 
     * @return 删除类型描述
     */
    public String getDeleteTypeDescription() {
        if (isDeleteAll()) {
            return "删除所有路线";
        } else if (isDeleteSpecific()) {
            return String.format("删除指定路线（共%d条）", getUnsignedRouteCount());
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
        return String.format("删除路线消息 - %s", getDeleteTypeDescription());
    }
    
    @Override
    public String toString() {
        return String.format("T8607DeleteRoute{routeCount=%d, routeIds=%s}", 
                           getUnsignedRouteCount(), routeIds);
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        T8607DeleteRoute that = (T8607DeleteRoute) o;
        return routeCount == that.routeCount && Objects.equals(routeIds, that.routeIds);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(routeCount, routeIds);
    }
}