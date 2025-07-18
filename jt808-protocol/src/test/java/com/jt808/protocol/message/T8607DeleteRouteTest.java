package com.jt808.protocol.message;

import io.vertx.core.buffer.Buffer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * T8607DeleteRoute 测试类
 * 
 * @author JT808-Vertx
 */
class T8607DeleteRouteTest {
    
    private T8607DeleteRoute message;
    
    @BeforeEach
    void setUp() {
        message = new T8607DeleteRoute();
    }
    
    @Test
    void testMessageId() {
        assertEquals(0x8607, message.getMessageId());
    }
    
    @Test
    void testDefaultConstructor() {
        assertNotNull(message.getRouteIds());
        assertTrue(message.getRouteIds().isEmpty());
        assertEquals(0, message.getRouteCount());
        assertEquals(0, message.getUnsignedRouteCount());
    }
    
    @Test
    void testParameterizedConstructor() {
        List<Long> routeIds = Arrays.asList(1001L, 1002L, 1003L);
        T8607DeleteRoute msg = new T8607DeleteRoute(routeIds);
        
        assertEquals(3, msg.getRouteCount());
        assertEquals(3, msg.getUnsignedRouteCount());
        assertEquals(3, msg.getRouteIds().size());
        assertTrue(msg.getRouteIds().containsAll(routeIds));
    }
    
    @Test
    void testParameterizedConstructorWithNull() {
        T8607DeleteRoute msg = new T8607DeleteRoute(null);
        
        assertEquals(0, msg.getRouteCount());
        assertTrue(msg.getRouteIds().isEmpty());
    }
    
    @Test
    void testCreateDeleteAll() {
        T8607DeleteRoute msg = T8607DeleteRoute.createDeleteAll();
        
        assertTrue(msg.isDeleteAll());
        assertFalse(msg.isDeleteSpecific());
        assertEquals(T8607DeleteRoute.DELETE_ALL, msg.getRouteCount());
        assertTrue(msg.getRouteIds().isEmpty());
    }
    
    @Test
    void testCreateDeleteSpecific() {
        List<Long> routeIds = Arrays.asList(2001L, 2002L);
        T8607DeleteRoute msg = T8607DeleteRoute.createDeleteSpecific(routeIds);
        
        assertFalse(msg.isDeleteAll());
        assertTrue(msg.isDeleteSpecific());
        assertEquals(2, msg.getRouteCount());
        assertEquals(routeIds, msg.getRouteIds());
    }
    
    @Test
    void testCreateDeleteSpecificWithEmptyList() {
        assertThrows(IllegalArgumentException.class, () -> {
            T8607DeleteRoute.createDeleteSpecific(new ArrayList<>());
        });
        
        assertThrows(IllegalArgumentException.class, () -> {
            T8607DeleteRoute.createDeleteSpecific(null);
        });
    }
    
    @Test
    void testCreateDeleteSpecificWithTooManyRoutes() {
        List<Long> tooManyRoutes = new ArrayList<>();
        for (int i = 0; i < T8607DeleteRoute.MAX_ROUTE_COUNT + 1; i++) {
            tooManyRoutes.add((long) i);
        }
        
        assertThrows(IllegalArgumentException.class, () -> {
            T8607DeleteRoute.createDeleteSpecific(tooManyRoutes);
        });
    }
    
    @Test
    void testCreateDeleteSingle() {
        long routeId = 3001L;
        T8607DeleteRoute msg = T8607DeleteRoute.createDeleteSingle(routeId);
        
        assertFalse(msg.isDeleteAll());
        assertTrue(msg.isDeleteSpecific());
        assertEquals(1, msg.getRouteCount());
        assertEquals(1, msg.getRouteIds().size());
        assertTrue(msg.containsRouteId(routeId));
    }
    
    @Test
    void testAddRouteId() {
        message.addRouteId(4001L);
        assertEquals(1, message.getRouteCount());
        assertTrue(message.containsRouteId(4001L));
        
        message.addRouteId(4002L);
        assertEquals(2, message.getRouteCount());
        assertTrue(message.containsRouteId(4002L));
    }
    
    @Test
    void testAddRouteIdExceedsMaximum() {
        // 添加最大数量的路线
        for (int i = 0; i < T8607DeleteRoute.MAX_ROUTE_COUNT; i++) {
            message.addRouteId(i);
        }
        
        // 尝试添加超过最大数量的路线
        assertThrows(IllegalStateException.class, () -> {
            message.addRouteId(T8607DeleteRoute.MAX_ROUTE_COUNT);
        });
    }
    
    @Test
    void testRemoveRouteId() {
        message.addRouteId(5001L);
        message.addRouteId(5002L);
        assertEquals(2, message.getRouteCount());
        
        assertTrue(message.removeRouteId(5001L));
        assertEquals(1, message.getRouteCount());
        assertFalse(message.containsRouteId(5001L));
        assertTrue(message.containsRouteId(5002L));
        
        assertFalse(message.removeRouteId(5001L)); // 已经不存在
        assertEquals(1, message.getRouteCount());
    }
    
    @Test
    void testClearRouteIds() {
        message.addRouteId(6001L);
        message.addRouteId(6002L);
        assertEquals(2, message.getRouteCount());
        
        message.clearRouteIds();
        assertEquals(0, message.getRouteCount());
        assertTrue(message.getRouteIds().isEmpty());
    }
    
    @Test
    void testSetRouteIds() {
        List<Long> routeIds = Arrays.asList(7001L, 7002L, 7003L);
        message.setRouteIds(routeIds);
        
        assertEquals(3, message.getRouteCount());
        assertEquals(routeIds, message.getRouteIds());
        
        // 测试设置null
        message.setRouteIds(null);
        assertEquals(0, message.getRouteCount());
        assertTrue(message.getRouteIds().isEmpty());
    }
    
    @Test
    void testEncodeDecodeDeleteAll() {
        T8607DeleteRoute msg = T8607DeleteRoute.createDeleteAll();
        
        // 编码
        Buffer encoded = msg.encodeBody();
        assertNotNull(encoded);
        assertEquals(1, encoded.length()); // 只有1字节的路线数
        assertEquals(T8607DeleteRoute.DELETE_ALL, encoded.getByte(0));
        
        // 解码
        T8607DeleteRoute decoded = new T8607DeleteRoute();
        decoded.decodeBody(encoded);
        
        assertTrue(decoded.isDeleteAll());
        assertEquals(T8607DeleteRoute.DELETE_ALL, decoded.getRouteCount());
        assertTrue(decoded.getRouteIds().isEmpty());
    }
    
    @Test
    void testEncodeDecodeDeleteSpecific() {
        List<Long> routeIds = Arrays.asList(8001L, 8002L, 8003L);
        T8607DeleteRoute msg = T8607DeleteRoute.createDeleteSpecific(routeIds);
        
        // 编码
        Buffer encoded = msg.encodeBody();
        assertNotNull(encoded);
        assertEquals(1 + 3 * 4, encoded.length()); // 1字节路线数 + 3个路线ID(每个4字节)
        
        // 解码
        T8607DeleteRoute decoded = new T8607DeleteRoute();
        decoded.decodeBody(encoded);
        
        assertFalse(decoded.isDeleteAll());
        assertTrue(decoded.isDeleteSpecific());
        assertEquals(3, decoded.getRouteCount());
        assertEquals(routeIds, decoded.getRouteIds());
    }
    
    @Test
    void testEncodeDecodeSingleRoute() {
        long routeId = 9001L;
        T8607DeleteRoute msg = T8607DeleteRoute.createDeleteSingle(routeId);
        
        // 编码
        Buffer encoded = msg.encodeBody();
        assertNotNull(encoded);
        assertEquals(5, encoded.length()); // 1字节路线数 + 1个路线ID(4字节)
        
        // 解码
        T8607DeleteRoute decoded = new T8607DeleteRoute();
        decoded.decodeBody(encoded);
        
        assertEquals(1, decoded.getRouteCount());
        assertTrue(decoded.containsRouteId(routeId));
    }
    
    @Test
    void testEncodeDecodeMaximumRoutes() {
        List<Long> routeIds = new ArrayList<>();
        for (int i = 0; i < T8607DeleteRoute.MAX_ROUTE_COUNT; i++) {
            routeIds.add((long) (10000 + i));
        }
        
        T8607DeleteRoute msg = new T8607DeleteRoute(routeIds);
        
        // 编码
        Buffer encoded = msg.encodeBody();
        assertNotNull(encoded);
        assertEquals(1 + T8607DeleteRoute.MAX_ROUTE_COUNT * 4, encoded.length());
        
        // 解码
        T8607DeleteRoute decoded = new T8607DeleteRoute();
        decoded.decodeBody(encoded);
        
        assertEquals(T8607DeleteRoute.MAX_ROUTE_COUNT, decoded.getUnsignedRouteCount());
        assertEquals(routeIds, decoded.getRouteIds());
    }
    
    @Test
    void testDecodeInvalidData() {
        // 测试消息体长度不足
        Buffer emptyBuffer = Buffer.buffer();
        assertThrows(IllegalArgumentException.class, () -> {
            message.decodeBody(emptyBuffer);
        });
        
        // 测试路线数与实际数据不匹配
        Buffer invalidBuffer = Buffer.buffer();
        invalidBuffer.appendByte((byte) 2); // 声明有2个路线
        invalidBuffer.appendUnsignedInt(1001L); // 但只提供1个路线ID
        
        assertThrows(IllegalArgumentException.class, () -> {
            message.decodeBody(invalidBuffer);
        });
    }
    
    @Test
    void testConstants() {
        assertEquals(125, T8607DeleteRoute.MAX_ROUTE_COUNT);
        assertEquals(0, T8607DeleteRoute.DELETE_ALL);
    }
    
    @Test
    void testBoundaryValues() {
        // 测试最大路线ID值
        long maxRouteId = 0xFFFFFFFFL; // 最大DWORD值
        T8607DeleteRoute msg = T8607DeleteRoute.createDeleteSingle(maxRouteId);
        
        Buffer encoded = msg.encodeBody();
        T8607DeleteRoute decoded = new T8607DeleteRoute();
        decoded.decodeBody(encoded);
        
        assertTrue(decoded.containsRouteId(maxRouteId));
        
        // 测试最大路线数量
        assertEquals(125, T8607DeleteRoute.MAX_ROUTE_COUNT);
    }
    
    @Test
    void testRealWorldScenario() {
        // 模拟真实场景：删除多条路线
        List<Long> routesToDelete = Arrays.asList(
            888001L, // 北京-上海路线
            888002L, // 上海-广州路线
            888003L, // 广州-深圳路线
            888004L, // 深圳-香港路线
            888005L  // 香港-澳门路线
        );
        
        T8607DeleteRoute msg = T8607DeleteRoute.createDeleteSpecific(routesToDelete);
        
        // 验证消息属性
        assertTrue(msg.isDeleteSpecific());
        assertFalse(msg.isDeleteAll());
        assertEquals(5, msg.getRouteCount());
        assertEquals("删除指定路线（共5条）", msg.getDeleteTypeDescription());
        
        // 编码解码验证
        Buffer encoded = msg.encodeBody();
        T8607DeleteRoute decoded = new T8607DeleteRoute();
        decoded.decodeBody(encoded);
        
        assertEquals(msg.getRouteCount(), decoded.getRouteCount());
        assertEquals(msg.getRouteIds(), decoded.getRouteIds());
        
        // 验证每个路线ID都存在
        for (Long routeId : routesToDelete) {
            assertTrue(decoded.containsRouteId(routeId));
        }
    }
    
    @Test
    void testDeleteAllScenario() {
        T8607DeleteRoute msg = T8607DeleteRoute.createDeleteAll();
        
        assertTrue(msg.isDeleteAll());
        assertFalse(msg.isDeleteSpecific());
        assertEquals("删除所有路线", msg.getDeleteTypeDescription());
        assertEquals("删除路线消息 - 删除所有路线", msg.getDescription());
        
        // 编码解码验证
        Buffer encoded = msg.encodeBody();
        assertEquals(1, encoded.length());
        
        T8607DeleteRoute decoded = new T8607DeleteRoute();
        decoded.decodeBody(encoded);
        
        assertTrue(decoded.isDeleteAll());
        assertEquals(T8607DeleteRoute.DELETE_ALL, decoded.getRouteCount());
    }
    
    @Test
    void testGettersAndSetters() {
        // 测试路线数量
        message.setRouteCount((byte) 5);
        assertEquals(5, message.getRouteCount());
        assertEquals(5, message.getUnsignedRouteCount());
        
        // 测试负数路线数量（作为无符号数处理）
        message.setRouteCount((byte) -1);
        assertEquals(-1, message.getRouteCount());
        assertEquals(255, message.getUnsignedRouteCount());
    }
    
    @Test
    void testContainsRouteId() {
        message.addRouteId(11001L);
        message.addRouteId(11002L);
        
        assertTrue(message.containsRouteId(11001L));
        assertTrue(message.containsRouteId(11002L));
        assertFalse(message.containsRouteId(11003L));
    }
    
    @Test
    void testGetDeleteTypeDescription() {
        // 删除所有路线
        T8607DeleteRoute deleteAll = T8607DeleteRoute.createDeleteAll();
        assertEquals("删除所有路线", deleteAll.getDeleteTypeDescription());
        
        // 删除指定路线
        T8607DeleteRoute deleteSpecific = T8607DeleteRoute.createDeleteSingle(12001L);
        assertEquals("删除指定路线（共1条）", deleteSpecific.getDeleteTypeDescription());
        
        // 无效状态
        T8607DeleteRoute invalid = new T8607DeleteRoute();
        invalid.setRouteCount((byte) 5); // 设置路线数但没有实际路线ID
        assertEquals("无效的删除操作", invalid.getDeleteTypeDescription());
    }
    
    @Test
    void testDescription() {
        T8607DeleteRoute msg = T8607DeleteRoute.createDeleteSingle(13001L);
        String description = msg.getDescription();
        assertTrue(description.contains("删除路线消息"));
        assertTrue(description.contains("删除指定路线"));
    }
    
    @Test
    void testToString() {
        List<Long> routeIds = Arrays.asList(14001L, 14002L);
        T8607DeleteRoute msg = new T8607DeleteRoute(routeIds);
        
        String str = msg.toString();
        assertTrue(str.contains("T8607DeleteRoute"));
        assertTrue(str.contains("routeCount=2"));
        assertTrue(str.contains("14001"));
        assertTrue(str.contains("14002"));
    }
    
    @Test
    void testEqualsAndHashCode() {
        List<Long> routeIds = Arrays.asList(15001L, 15002L);
        
        T8607DeleteRoute msg1 = new T8607DeleteRoute(routeIds);
        T8607DeleteRoute msg2 = new T8607DeleteRoute(routeIds);
        
        assertEquals(msg1, msg2);
        assertEquals(msg1.hashCode(), msg2.hashCode());
        
        // 修改一个消息
        msg2.addRouteId(15003L);
        assertNotEquals(msg1, msg2);
        assertNotEquals(msg1.hashCode(), msg2.hashCode());
        
        // 测试与null和不同类型的比较
        assertNotEquals(msg1, null);
        assertNotEquals(msg1, "not a message");
        
        // 测试相同对象
        assertEquals(msg1, msg1);
    }
    
    @Test
    void testGetRouteIdsReturnsDefensiveCopy() {
        message.addRouteId(16001L);
        List<Long> routeIds = message.getRouteIds();
        
        // 修改返回的列表不应影响原始消息
        routeIds.add(16002L);
        assertEquals(1, message.getRouteCount());
        assertFalse(message.containsRouteId(16002L));
    }
}