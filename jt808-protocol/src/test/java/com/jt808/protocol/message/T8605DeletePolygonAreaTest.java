package com.jt808.protocol.message;

import com.jt808.protocol.factory.JT808MessageFactory;
import io.vertx.core.buffer.Buffer;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * T8605DeletePolygonArea 删除多边形区域消息测试类
 * 
 * @author JT808-Vertx
 * @since 1.0.0
 */
class T8605DeletePolygonAreaTest {
    
    @Test
    void testMessageId() {
        T8605DeletePolygonArea message = new T8605DeletePolygonArea();
        assertEquals(0x8605, message.getMessageId());
    }
    
    @Test
    void testDefaultConstructor() {
        T8605DeletePolygonArea message = new T8605DeletePolygonArea();
        assertNotNull(message);
        assertEquals(0, message.getAreaCount());
        assertEquals(0, message.getUnsignedAreaCount());
        assertTrue(message.getAreaIds().isEmpty());
    }
    
    @Test
    void testConstructorWithAreaIds() {
        List<Long> areaIds = Arrays.asList(1001L, 1002L, 1003L);
        T8605DeletePolygonArea message = new T8605DeletePolygonArea(areaIds);
        
        assertEquals(3, message.getAreaCount());
        assertEquals(3, message.getUnsignedAreaCount());
        assertEquals(areaIds, message.getAreaIds());
    }
    
    @Test
    void testConstructorWithNullAreaIds() {
        T8605DeletePolygonArea message = new T8605DeletePolygonArea(null);
        
        assertEquals(0, message.getAreaCount());
        assertTrue(message.getAreaIds().isEmpty());
    }
    
    @Test
    void testCreateDeleteAll() {
        T8605DeletePolygonArea message = T8605DeletePolygonArea.createDeleteAll();
        
        assertEquals(T8605DeletePolygonArea.DELETE_ALL, message.getAreaCount());
        assertTrue(message.isDeleteAll());
        assertFalse(message.isDeleteSpecific());
        assertEquals("删除所有多边形区域", message.getDeleteTypeDescription());
    }
    
    @Test
    void testCreateDeleteSpecific() {
        List<Long> areaIds = Arrays.asList(1001L, 1002L);
        T8605DeletePolygonArea message = T8605DeletePolygonArea.createDeleteSpecific(areaIds);
        
        assertEquals(2, message.getAreaCount());
        assertEquals(areaIds, message.getAreaIds());
        assertFalse(message.isDeleteAll());
        assertTrue(message.isDeleteSpecific());
        assertEquals("删除指定的2个多边形区域", message.getDeleteTypeDescription());
    }
    
    @Test
    void testCreateDeleteSpecificWithEmptyList() {
        assertThrows(IllegalArgumentException.class, () -> {
            T8605DeletePolygonArea.createDeleteSpecific(Collections.emptyList());
        });
    }
    
    @Test
    void testCreateDeleteSpecificWithNullList() {
        assertThrows(IllegalArgumentException.class, () -> {
            T8605DeletePolygonArea.createDeleteSpecific(null);
        });
    }
    
    @Test
    void testCreateDeleteSpecificWithTooManyAreas() {
        // 创建超过最大数量的区域ID列表
        List<Long> tooManyAreaIds = Arrays.asList(new Long[T8605DeletePolygonArea.MAX_AREA_COUNT + 1]);
        for (int i = 0; i < tooManyAreaIds.size(); i++) {
            tooManyAreaIds.set(i, (long) (i + 1));
        }
        
        assertThrows(IllegalArgumentException.class, () -> {
            T8605DeletePolygonArea.createDeleteSpecific(tooManyAreaIds);
        });
    }
    
    @Test
    void testCreateDeleteSingle() {
        T8605DeletePolygonArea message = T8605DeletePolygonArea.createDeleteSingle(1001L);
        
        assertEquals(1, message.getAreaCount());
        assertEquals(Arrays.asList(1001L), message.getAreaIds());
        assertTrue(message.containsAreaId(1001L));
        assertFalse(message.containsAreaId(1002L));
    }
    
    @Test
    void testAddAreaId() {
        T8605DeletePolygonArea message = new T8605DeletePolygonArea();
        
        message.addAreaId(1001L);
        assertEquals(1, message.getAreaCount());
        assertTrue(message.containsAreaId(1001L));
        
        message.addAreaId(1002L);
        assertEquals(2, message.getAreaCount());
        assertTrue(message.containsAreaId(1002L));
    }
    
    @Test
    void testAddAreaIdExceedsMaxCount() {
        T8605DeletePolygonArea message = new T8605DeletePolygonArea();
        
        // 添加最大数量的区域ID
        for (int i = 0; i < T8605DeletePolygonArea.MAX_AREA_COUNT; i++) {
            message.addAreaId(i + 1L);
        }
        
        // 尝试添加超过最大数量的区域ID
        assertThrows(IllegalStateException.class, () -> {
            message.addAreaId(T8605DeletePolygonArea.MAX_AREA_COUNT + 1L);
        });
    }
    
    @Test
    void testRemoveAreaId() {
        List<Long> areaIds = Arrays.asList(1001L, 1002L, 1003L);
        T8605DeletePolygonArea message = new T8605DeletePolygonArea(areaIds);
        
        assertTrue(message.removeAreaId(1002L));
        assertEquals(2, message.getAreaCount());
        assertFalse(message.containsAreaId(1002L));
        
        assertFalse(message.removeAreaId(1004L)); // 不存在的区域ID
        assertEquals(2, message.getAreaCount());
    }
    
    @Test
    void testClearAreaIds() {
        List<Long> areaIds = Arrays.asList(1001L, 1002L, 1003L);
        T8605DeletePolygonArea message = new T8605DeletePolygonArea(areaIds);
        
        message.clearAreaIds();
        assertEquals(0, message.getAreaCount());
        assertTrue(message.getAreaIds().isEmpty());
    }
    
    @Test
    void testSetAreaIds() {
        T8605DeletePolygonArea message = new T8605DeletePolygonArea();
        List<Long> areaIds = Arrays.asList(1001L, 1002L);
        
        message.setAreaIds(areaIds);
        assertEquals(2, message.getAreaCount());
        assertEquals(areaIds, message.getAreaIds());
        
        // 测试设置null
        message.setAreaIds(null);
        assertEquals(0, message.getAreaCount());
        assertTrue(message.getAreaIds().isEmpty());
    }
    
    @Test
    void testEncodeBodyDeleteAll() {
        T8605DeletePolygonArea message = T8605DeletePolygonArea.createDeleteAll();
        Buffer encoded = message.encodeBody();
        
        assertEquals(1, encoded.length()); // 只有1字节的区域数
        assertEquals(T8605DeletePolygonArea.DELETE_ALL, encoded.getByte(0));
    }
    
    @Test
    void testEncodeBodyDeleteSpecific() {
        List<Long> areaIds = Arrays.asList(1001L, 1002L, 1003L);
        T8605DeletePolygonArea message = new T8605DeletePolygonArea(areaIds);
        Buffer encoded = message.encodeBody();
        
        assertEquals(13, encoded.length()); // 1字节区域数 + 3*4字节区域ID
        assertEquals(3, encoded.getByte(0)); // 区域数
        assertEquals(1001L, encoded.getUnsignedInt(1)); // 第一个区域ID
        assertEquals(1002L, encoded.getUnsignedInt(5)); // 第二个区域ID
        assertEquals(1003L, encoded.getUnsignedInt(9)); // 第三个区域ID
    }
    
    @Test
    void testDecodeBodyDeleteAll() {
        Buffer buffer = Buffer.buffer();
        buffer.appendByte(T8605DeletePolygonArea.DELETE_ALL);
        
        T8605DeletePolygonArea message = new T8605DeletePolygonArea();
        message.decodeBody(buffer);
        
        assertEquals(T8605DeletePolygonArea.DELETE_ALL, message.getAreaCount());
        assertTrue(message.isDeleteAll());
        assertTrue(message.getAreaIds().isEmpty());
    }
    
    @Test
    void testDecodeBodyDeleteSpecific() {
        Buffer buffer = Buffer.buffer();
        buffer.appendByte((byte) 3); // 区域数
        buffer.appendUnsignedInt(1001L); // 区域ID1
        buffer.appendUnsignedInt(1002L); // 区域ID2
        buffer.appendUnsignedInt(1003L); // 区域ID3
        
        T8605DeletePolygonArea message = new T8605DeletePolygonArea();
        message.decodeBody(buffer);
        
        assertEquals(3, message.getAreaCount());
        assertEquals(3, message.getUnsignedAreaCount());
        assertTrue(message.isDeleteSpecific());
        assertEquals(Arrays.asList(1001L, 1002L, 1003L), message.getAreaIds());
    }
    
    @Test
    void testDecodeBodyInvalidLength() {
        // 测试消息体长度不足
        Buffer emptyBuffer = Buffer.buffer();
        T8605DeletePolygonArea message = new T8605DeletePolygonArea();
        
        assertThrows(IllegalArgumentException.class, () -> {
            message.decodeBody(emptyBuffer);
        });
    }
    
    @Test
    void testDecodeBodyInsufficientDataForAreaIds() {
        // 声明有2个区域但只提供1个区域ID的数据
        Buffer buffer = Buffer.buffer();
        buffer.appendByte((byte) 2); // 声明有2个区域
        buffer.appendUnsignedInt(1001L); // 只提供1个区域ID
        
        T8605DeletePolygonArea message = new T8605DeletePolygonArea();
        assertThrows(IllegalArgumentException.class, () -> {
            message.decodeBody(buffer);
        });
    }
    
    @Test
    void testDecodeBodyTooManyAreas() {
        // 测试区域数量超过最大限制
        Buffer buffer = Buffer.buffer();
        buffer.appendByte((byte) (T8605DeletePolygonArea.MAX_AREA_COUNT + 1)); // 超过最大数量
        
        T8605DeletePolygonArea message = new T8605DeletePolygonArea();
        assertThrows(IllegalArgumentException.class, () -> {
            message.decodeBody(buffer);
        });
    }
    
    @Test
    void testEncodeDecodeConsistency() {
        List<Long> originalAreaIds = Arrays.asList(1001L, 1002L, 1003L, 1004L, 1005L);
        T8605DeletePolygonArea original = new T8605DeletePolygonArea(originalAreaIds);
        
        // 编码
        Buffer encoded = original.encodeBody();
        
        // 解码
        T8605DeletePolygonArea decoded = new T8605DeletePolygonArea();
        decoded.decodeBody(encoded);
        
        // 验证一致性
        assertEquals(original.getAreaCount(), decoded.getAreaCount());
        assertEquals(original.getUnsignedAreaCount(), decoded.getUnsignedAreaCount());
        assertEquals(original.getAreaIds(), decoded.getAreaIds());
        assertEquals(original.isDeleteAll(), decoded.isDeleteAll());
        assertEquals(original.isDeleteSpecific(), decoded.isDeleteSpecific());
        assertEquals(original, decoded);
    }
    
    @Test
    void testEncodeDecodeConsistencyDeleteAll() {
        T8605DeletePolygonArea original = T8605DeletePolygonArea.createDeleteAll();
        
        // 编码
        Buffer encoded = original.encodeBody();
        
        // 解码
        T8605DeletePolygonArea decoded = new T8605DeletePolygonArea();
        decoded.decodeBody(encoded);
        
        // 验证一致性
        assertEquals(original.getAreaCount(), decoded.getAreaCount());
        assertEquals(original.isDeleteAll(), decoded.isDeleteAll());
        assertEquals(original.getAreaIds(), decoded.getAreaIds());
        assertEquals(original, decoded);
    }
    
    @Test
    void testGetDescription() {
        // 测试删除所有区域的描述
        T8605DeletePolygonArea deleteAllMessage = T8605DeletePolygonArea.createDeleteAll();
        String deleteAllDescription = deleteAllMessage.getDescription();
        assertTrue(deleteAllDescription.contains("删除多边形区域消息"));
        assertTrue(deleteAllDescription.contains("删除所有多边形区域"));
        
        // 测试删除指定区域的描述
        T8605DeletePolygonArea deleteSpecificMessage = T8605DeletePolygonArea.createDeleteSpecific(Arrays.asList(1001L, 1002L));
        String deleteSpecificDescription = deleteSpecificMessage.getDescription();
        assertTrue(deleteSpecificDescription.contains("删除多边形区域消息"));
        assertTrue(deleteSpecificDescription.contains("删除指定的2个多边形区域"));
    }
    
    @Test
    void testToString() {
        List<Long> areaIds = Arrays.asList(1001L, 1002L);
        T8605DeletePolygonArea message = new T8605DeletePolygonArea(areaIds);
        String str = message.toString();
        
        assertTrue(str.contains("T8605DeletePolygonArea"));
        assertTrue(str.contains("areaCount=2"));
        assertTrue(str.contains("[1001, 1002]"));
    }
    
    @Test
    void testEquals() {
        List<Long> areaIds1 = Arrays.asList(1001L, 1002L);
        List<Long> areaIds2 = Arrays.asList(1001L, 1002L);
        List<Long> areaIds3 = Arrays.asList(1003L, 1004L);
        
        T8605DeletePolygonArea message1 = new T8605DeletePolygonArea(areaIds1);
        T8605DeletePolygonArea message2 = new T8605DeletePolygonArea(areaIds2);
        T8605DeletePolygonArea message3 = new T8605DeletePolygonArea(areaIds3);
        
        // 测试相等性
        assertEquals(message1, message2);
        assertNotEquals(message1, message3);
        assertNotEquals(message1, null);
        assertNotEquals(message1, "not a message");
        
        // 测试自反性
        assertEquals(message1, message1);
        
        // 测试删除所有区域的相等性
        T8605DeletePolygonArea deleteAll1 = T8605DeletePolygonArea.createDeleteAll();
        T8605DeletePolygonArea deleteAll2 = T8605DeletePolygonArea.createDeleteAll();
        assertEquals(deleteAll1, deleteAll2);
    }
    
    @Test
    void testHashCode() {
        List<Long> areaIds1 = Arrays.asList(1001L, 1002L);
        List<Long> areaIds2 = Arrays.asList(1001L, 1002L);
        
        T8605DeletePolygonArea message1 = new T8605DeletePolygonArea(areaIds1);
        T8605DeletePolygonArea message2 = new T8605DeletePolygonArea(areaIds2);
        
        assertEquals(message1.hashCode(), message2.hashCode());
        
        // 测试删除所有区域的哈希码
        T8605DeletePolygonArea deleteAll1 = T8605DeletePolygonArea.createDeleteAll();
        T8605DeletePolygonArea deleteAll2 = T8605DeletePolygonArea.createDeleteAll();
        assertEquals(deleteAll1.hashCode(), deleteAll2.hashCode());
    }
    
    @Test
    void testMessageFactoryIntegration() {
        JT808MessageFactory factory = JT808MessageFactory.getInstance();
        
        // 测试工厂是否支持该消息类型
        assertTrue(factory.isSupported(0x8605));
        
        // 测试工厂创建消息实例
        JT808Message message = factory.createMessage(0x8605);
        assertNotNull(message);
        assertTrue(message instanceof T8605DeletePolygonArea);
        assertEquals(0x8605, message.getMessageId());
    }
    
    @Test
    void testConstants() {
        assertEquals(125, T8605DeletePolygonArea.MAX_AREA_COUNT);
        assertEquals(0, T8605DeletePolygonArea.DELETE_ALL);
    }
    
    @Test
    void testBoundaryValues() {
        T8605DeletePolygonArea message = new T8605DeletePolygonArea();
        
        // 测试最大区域数量
        for (int i = 0; i < T8605DeletePolygonArea.MAX_AREA_COUNT; i++) {
            message.addAreaId(i + 1L);
        }
        assertEquals(T8605DeletePolygonArea.MAX_AREA_COUNT, message.getUnsignedAreaCount());
        
        // 测试最大区域ID值
        message.clearAreaIds();
        message.addAreaId(0xFFFFFFFFL); // 最大32位无符号整数
        assertTrue(message.containsAreaId(0xFFFFFFFFL));
    }
    
    @Test
    void testRealWorldScenario() {
        // 模拟真实场景：删除指定的多边形电子围栏区域
        List<Long> areaIdsToDelete = Arrays.asList(2001L, 2002L, 2003L, 2004L, 2005L);
        
        T8605DeletePolygonArea message = T8605DeletePolygonArea.createDeleteSpecific(areaIdsToDelete);
        
        // 验证消息属性
        assertEquals(0x8605, message.getMessageId());
        assertEquals(5, message.getUnsignedAreaCount());
        assertTrue(message.isDeleteSpecific());
        assertFalse(message.isDeleteAll());
        
        // 验证包含所有要删除的区域ID
        for (Long areaId : areaIdsToDelete) {
            assertTrue(message.containsAreaId(areaId));
        }
        
        // 编码消息
        Buffer encoded = message.encodeBody();
        assertNotNull(encoded);
        assertEquals(21, encoded.length()); // 1字节区域数 + 5*4字节区域ID
        
        // 解码消息
        T8605DeletePolygonArea decoded = new T8605DeletePolygonArea();
        decoded.decodeBody(encoded);
        
        // 验证解码结果
        assertEquals(message.getAreaCount(), decoded.getAreaCount());
        assertEquals(message.getAreaIds(), decoded.getAreaIds());
        assertEquals(message.isDeleteSpecific(), decoded.isDeleteSpecific());
        assertEquals(message, decoded);
        
        // 验证描述信息
        String description = decoded.getDescription();
        assertTrue(description.contains("删除多边形区域消息"));
        assertTrue(description.contains("删除指定的5个多边形区域"));
        
        // 模拟删除所有多边形区域的场景
        T8605DeletePolygonArea deleteAllMessage = T8605DeletePolygonArea.createDeleteAll();
        
        // 验证删除所有区域的消息
        assertEquals(0x8605, deleteAllMessage.getMessageId());
        assertTrue(deleteAllMessage.isDeleteAll());
        assertFalse(deleteAllMessage.isDeleteSpecific());
        assertEquals(0, deleteAllMessage.getUnsignedAreaCount());
        assertTrue(deleteAllMessage.getAreaIds().isEmpty());
        
        // 编码和解码删除所有区域的消息
        Buffer deleteAllEncoded = deleteAllMessage.encodeBody();
        assertEquals(1, deleteAllEncoded.length());
        
        T8605DeletePolygonArea deleteAllDecoded = new T8605DeletePolygonArea();
        deleteAllDecoded.decodeBody(deleteAllEncoded);
        
        assertEquals(deleteAllMessage, deleteAllDecoded);
        assertTrue(deleteAllDecoded.getDescription().contains("删除所有多边形区域"));
    }
}