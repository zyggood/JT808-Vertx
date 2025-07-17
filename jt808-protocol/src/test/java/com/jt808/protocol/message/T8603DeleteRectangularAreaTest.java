package com.jt808.protocol.message;

import com.jt808.protocol.factory.JT808MessageFactory;
import io.vertx.core.buffer.Buffer;
import org.junit.jupiter.api.Test;
import java.util.Arrays;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

/**
 * T8603DeleteRectangularArea 删除矩形区域消息测试类
 * 
 * @author JT808-Vertx
 * @since 1.0.0
 */
class T8603DeleteRectangularAreaTest {
    
    @Test
    void testMessageId() {
        T8603DeleteRectangularArea message = new T8603DeleteRectangularArea();
        assertEquals(0x8603, message.getMessageId());
    }
    
    @Test
    void testDefaultConstructor() {
        T8603DeleteRectangularArea message = new T8603DeleteRectangularArea();
        assertNotNull(message);
        assertEquals(0, message.getUnsignedAreaCount());
        assertTrue(message.getAreaIds().isEmpty());
        assertTrue(message.isDeleteAll());
        assertFalse(message.isDeleteSpecific());
    }
    
    @Test
    void testConstructorWithNullAreaIds() {
        T8603DeleteRectangularArea message = new T8603DeleteRectangularArea(null);
        assertNotNull(message);
        assertEquals(0, message.getUnsignedAreaCount());
        assertTrue(message.getAreaIds().isEmpty());
        assertTrue(message.isDeleteAll());
    }
    
    @Test
    void testConstructorWithAreaIds() {
        List<Long> areaIds = Arrays.asList(1001L, 1002L, 1003L);
        T8603DeleteRectangularArea message = new T8603DeleteRectangularArea(areaIds);
        assertNotNull(message);
        assertEquals(3, message.getUnsignedAreaCount());
        assertEquals(areaIds, message.getAreaIds());
        assertFalse(message.isDeleteAll());
        assertTrue(message.isDeleteSpecific());
    }
    
    @Test
    void testCreateDeleteAll() {
        T8603DeleteRectangularArea message = T8603DeleteRectangularArea.createDeleteAll();
        assertNotNull(message);
        assertEquals(0, message.getUnsignedAreaCount());
        assertTrue(message.getAreaIds().isEmpty());
        assertTrue(message.isDeleteAll());
        assertFalse(message.isDeleteSpecific());
        assertEquals("删除所有矩形区域", message.getDeleteTypeDescription());
    }
    
    @Test
    void testCreateDeleteSpecific() {
        List<Long> areaIds = Arrays.asList(1001L, 1002L);
        T8603DeleteRectangularArea message = T8603DeleteRectangularArea.createDeleteSpecific(areaIds);
        assertNotNull(message);
        assertEquals(2, message.getUnsignedAreaCount());
        assertEquals(areaIds, message.getAreaIds());
        assertFalse(message.isDeleteAll());
        assertTrue(message.isDeleteSpecific());
        assertEquals("删除指定矩形区域", message.getDeleteTypeDescription());
    }
    
    @Test
    void testCreateDeleteSpecificWithEmptyList() {
        List<Long> emptyList = Arrays.asList();
        assertThrows(IllegalArgumentException.class, () -> {
            T8603DeleteRectangularArea.createDeleteSpecific(emptyList);
        });
        
        assertThrows(IllegalArgumentException.class, () -> {
            T8603DeleteRectangularArea.createDeleteSpecific(null);
        });
    }
    
    @Test
    void testCreateDeleteSpecificWithTooManyAreas() {
        // 创建超过125个区域的列表
        Long[] areaArray = new Long[126];
        for (int i = 0; i < 126; i++) {
            areaArray[i] = (long) (i + 1);
        }
        List<Long> tooManyAreas = Arrays.asList(areaArray);
        
        assertThrows(IllegalArgumentException.class, () -> {
            T8603DeleteRectangularArea.createDeleteSpecific(tooManyAreas);
        });
    }
    
    @Test
    void testCreateDeleteSingle() {
        long areaId = 1001L;
        T8603DeleteRectangularArea message = T8603DeleteRectangularArea.createDeleteSingle(areaId);
        assertNotNull(message);
        assertEquals(1, message.getUnsignedAreaCount());
        assertEquals(Arrays.asList(areaId), message.getAreaIds());
        assertFalse(message.isDeleteAll());
        assertTrue(message.isDeleteSpecific());
        assertTrue(message.containsAreaId(areaId));
    }
    
    @Test
    void testGettersAndSetters() {
        T8603DeleteRectangularArea message = new T8603DeleteRectangularArea();
        
        // 测试设置区域ID列表
        List<Long> areaIds = Arrays.asList(1001L, 1002L);
        message.setAreaIds(areaIds);
        assertEquals(2, message.getUnsignedAreaCount());
        assertEquals(areaIds, message.getAreaIds());
        
        // 测试设置区域数量
        message.setAreaCount((byte) 5);
        assertEquals(5, message.getUnsignedAreaCount());
        assertEquals((byte) 5, message.getAreaCount());
    }
    
    @Test
    void testAddAreaId() {
        T8603DeleteRectangularArea message = new T8603DeleteRectangularArea();
        
        message.addAreaId(1001L);
        assertEquals(1, message.getUnsignedAreaCount());
        assertTrue(message.containsAreaId(1001L));
        
        message.addAreaId(1002L);
        assertEquals(2, message.getUnsignedAreaCount());
        assertTrue(message.containsAreaId(1002L));
    }
    
    @Test
    void testRemoveAreaId() {
        List<Long> areaIds = Arrays.asList(1001L, 1002L, 1003L);
        T8603DeleteRectangularArea message = new T8603DeleteRectangularArea(areaIds);
        
        assertTrue(message.removeAreaId(1002L));
        assertEquals(2, message.getUnsignedAreaCount());
        assertFalse(message.containsAreaId(1002L));
        
        assertFalse(message.removeAreaId(9999L)); // 不存在的区域ID
    }
    
    @Test
    void testClearAreaIds() {
        List<Long> areaIds = Arrays.asList(1001L, 1002L, 1003L);
        T8603DeleteRectangularArea message = new T8603DeleteRectangularArea(areaIds);
        
        message.clearAreaIds();
        assertEquals(0, message.getUnsignedAreaCount());
        assertTrue(message.getAreaIds().isEmpty());
        assertTrue(message.isDeleteAll());
    }
    
    @Test
    void testEncodeBodyDeleteAll() {
        T8603DeleteRectangularArea message = T8603DeleteRectangularArea.createDeleteAll();
        Buffer encoded = message.encodeBody();
        
        assertEquals(1, encoded.length());
        assertEquals(0, encoded.getByte(0)); // DELETE_ALL = 0
    }
    
    @Test
    void testEncodeBodyDeleteSpecific() {
        List<Long> areaIds = Arrays.asList(1001L, 1002L);
        T8603DeleteRectangularArea message = T8603DeleteRectangularArea.createDeleteSpecific(areaIds);
        Buffer encoded = message.encodeBody();
        
        assertEquals(9, encoded.length()); // 1字节区域数 + 2个区域ID * 4字节
        assertEquals(2, encoded.getByte(0)); // 区域数量
        assertEquals(1001L, encoded.getUnsignedInt(1)); // 第一个区域ID
        assertEquals(1002L, encoded.getUnsignedInt(5)); // 第二个区域ID
    }
    
    @Test
    void testDecodeBodyDeleteAll() {
        Buffer buffer = Buffer.buffer();
        buffer.appendByte((byte) 0); // DELETE_ALL
        
        T8603DeleteRectangularArea message = new T8603DeleteRectangularArea();
        message.decodeBody(buffer);
        
        assertEquals(0, message.getUnsignedAreaCount());
        assertTrue(message.getAreaIds().isEmpty());
        assertTrue(message.isDeleteAll());
        assertFalse(message.isDeleteSpecific());
    }
    
    @Test
    void testDecodeBodyDeleteSpecific() {
        Buffer buffer = Buffer.buffer();
        buffer.appendByte((byte) 2); // 区域数量
        buffer.appendUnsignedInt(1001L); // 第一个区域ID
        buffer.appendUnsignedInt(1002L); // 第二个区域ID
        
        T8603DeleteRectangularArea message = new T8603DeleteRectangularArea();
        message.decodeBody(buffer);
        
        assertEquals(2, message.getUnsignedAreaCount());
        assertEquals(Arrays.asList(1001L, 1002L), message.getAreaIds());
        assertFalse(message.isDeleteAll());
        assertTrue(message.isDeleteSpecific());
    }
    
    @Test
    void testDecodeBodyInvalidLength() {
        // 测试消息体长度不足
        Buffer emptyBuffer = Buffer.buffer();
        T8603DeleteRectangularArea message1 = new T8603DeleteRectangularArea();
        assertThrows(IllegalArgumentException.class, () -> {
            message1.decodeBody(emptyBuffer);
        });
        
        // 测试删除指定区域时长度不正确
        Buffer invalidBuffer = Buffer.buffer();
        invalidBuffer.appendByte((byte) 2); // 声明有2个区域
        invalidBuffer.appendUnsignedInt(1001L); // 但只提供1个区域ID
        
        T8603DeleteRectangularArea message2 = new T8603DeleteRectangularArea();
        assertThrows(IllegalArgumentException.class, () -> {
            message2.decodeBody(invalidBuffer);
        });
        
        // 测试删除所有区域时长度不正确
        Buffer invalidDeleteAllBuffer = Buffer.buffer();
        invalidDeleteAllBuffer.appendByte((byte) 0); // DELETE_ALL
        invalidDeleteAllBuffer.appendByte((byte) 1); // 多余的字节
        
        T8603DeleteRectangularArea message3 = new T8603DeleteRectangularArea();
        assertThrows(IllegalArgumentException.class, () -> {
            message3.decodeBody(invalidDeleteAllBuffer);
        });
    }
    
    @Test
    void testEncodeDecodeConsistency() {
        // 测试删除所有区域的编码解码一致性
        T8603DeleteRectangularArea originalDeleteAll = T8603DeleteRectangularArea.createDeleteAll();
        Buffer encodedDeleteAll = originalDeleteAll.encodeBody();
        
        T8603DeleteRectangularArea decodedDeleteAll = new T8603DeleteRectangularArea();
        decodedDeleteAll.decodeBody(encodedDeleteAll);
        
        assertEquals(originalDeleteAll.getUnsignedAreaCount(), decodedDeleteAll.getUnsignedAreaCount());
        assertEquals(originalDeleteAll.getAreaIds(), decodedDeleteAll.getAreaIds());
        assertEquals(originalDeleteAll.isDeleteAll(), decodedDeleteAll.isDeleteAll());
        
        // 测试删除指定区域的编码解码一致性
        List<Long> areaIds = Arrays.asList(1001L, 1002L, 1003L);
        T8603DeleteRectangularArea originalDeleteSpecific = T8603DeleteRectangularArea.createDeleteSpecific(areaIds);
        Buffer encodedDeleteSpecific = originalDeleteSpecific.encodeBody();
        
        T8603DeleteRectangularArea decodedDeleteSpecific = new T8603DeleteRectangularArea();
        decodedDeleteSpecific.decodeBody(encodedDeleteSpecific);
        
        assertEquals(originalDeleteSpecific.getUnsignedAreaCount(), decodedDeleteSpecific.getUnsignedAreaCount());
        assertEquals(originalDeleteSpecific.getAreaIds(), decodedDeleteSpecific.getAreaIds());
        assertEquals(originalDeleteSpecific.isDeleteSpecific(), decodedDeleteSpecific.isDeleteSpecific());
    }
    
    @Test
    void testContainsAreaId() {
        List<Long> areaIds = Arrays.asList(1001L, 1002L, 1003L);
        T8603DeleteRectangularArea message = new T8603DeleteRectangularArea(areaIds);
        
        assertTrue(message.containsAreaId(1001L));
        assertTrue(message.containsAreaId(1002L));
        assertFalse(message.containsAreaId(9999L));
    }
    
    @Test
    void testGetDescription() {
        T8603DeleteRectangularArea deleteAllMessage = T8603DeleteRectangularArea.createDeleteAll();
        assertTrue(deleteAllMessage.getDescription().contains("删除所有矩形区域"));
        
        List<Long> areaIds = Arrays.asList(1001L, 1002L);
        T8603DeleteRectangularArea deleteSpecificMessage = T8603DeleteRectangularArea.createDeleteSpecific(areaIds);
        assertTrue(deleteSpecificMessage.getDescription().contains("删除指定矩形区域"));
        assertTrue(deleteSpecificMessage.getDescription().contains("2"));
    }
    
    @Test
    void testToString() {
        // 测试删除所有区域的toString
        T8603DeleteRectangularArea deleteAllMessage = T8603DeleteRectangularArea.createDeleteAll();
        String deleteAllStr = deleteAllMessage.toString();
        assertTrue(deleteAllStr.contains("T8603DeleteRectangularArea"));
        assertTrue(deleteAllStr.contains("areaCount=0"));
        assertTrue(deleteAllStr.contains("areaIds=[]"));
        
        // 测试删除指定区域的toString
        List<Long> areaIds = Arrays.asList(1001L, 1002L);
        T8603DeleteRectangularArea deleteSpecificMessage = T8603DeleteRectangularArea.createDeleteSpecific(areaIds);
        String deleteSpecificStr = deleteSpecificMessage.toString();
        assertTrue(deleteSpecificStr.contains("T8603DeleteRectangularArea"));
        assertTrue(deleteSpecificStr.contains("areaCount=2"));
        assertTrue(deleteSpecificStr.contains("1001"));
        assertTrue(deleteSpecificStr.contains("1002"));
    }
    
    @Test
    void testEquals() {
        List<Long> areaIds1 = Arrays.asList(1001L, 1002L);
        List<Long> areaIds2 = Arrays.asList(1001L, 1002L);
        List<Long> areaIds3 = Arrays.asList(1003L, 1004L);
        
        T8603DeleteRectangularArea message1 = new T8603DeleteRectangularArea(areaIds1);
        T8603DeleteRectangularArea message2 = new T8603DeleteRectangularArea(areaIds2);
        T8603DeleteRectangularArea message3 = new T8603DeleteRectangularArea(areaIds3);
        
        // 测试相等性
        assertEquals(message1, message2);
        assertNotEquals(message1, message3);
        assertNotEquals(message1, null);
        assertNotEquals(message1, "not a message");
        
        // 测试自反性
        assertEquals(message1, message1);
        
        // 测试删除所有区域的相等性
        T8603DeleteRectangularArea deleteAll1 = T8603DeleteRectangularArea.createDeleteAll();
        T8603DeleteRectangularArea deleteAll2 = T8603DeleteRectangularArea.createDeleteAll();
        assertEquals(deleteAll1, deleteAll2);
    }
    
    @Test
    void testHashCode() {
        List<Long> areaIds1 = Arrays.asList(1001L, 1002L);
        List<Long> areaIds2 = Arrays.asList(1001L, 1002L);
        
        T8603DeleteRectangularArea message1 = new T8603DeleteRectangularArea(areaIds1);
        T8603DeleteRectangularArea message2 = new T8603DeleteRectangularArea(areaIds2);
        
        assertEquals(message1.hashCode(), message2.hashCode());
    }
    
    @Test
    void testMaxAreaCountConstant() {
        assertEquals(125, T8603DeleteRectangularArea.MAX_AREA_COUNT);
    }
    
    @Test
    void testDeleteAllConstant() {
        assertEquals(0, T8603DeleteRectangularArea.DELETE_ALL);
    }
    
    @Test
    void testMessageFactoryIntegration() {
        JT808MessageFactory factory = JT808MessageFactory.getInstance();
        
        // 测试工厂是否支持该消息类型
        assertTrue(factory.isSupported(0x8603));
        
        // 测试工厂创建消息实例
        JT808Message message = factory.createMessage(0x8603);
        assertNotNull(message);
        assertTrue(message instanceof T8603DeleteRectangularArea);
        assertEquals(0x8603, message.getMessageId());
    }
    
    @Test
    void testBoundaryValues() {
        // 测试最大区域数量边界值
        Long[] maxAreaArray = new Long[T8603DeleteRectangularArea.MAX_AREA_COUNT];
        for (int i = 0; i < T8603DeleteRectangularArea.MAX_AREA_COUNT; i++) {
            maxAreaArray[i] = (long) (i + 1);
        }
        List<Long> maxAreas = Arrays.asList(maxAreaArray);
        
        // 应该能够创建最大数量的区域
        assertDoesNotThrow(() -> {
            T8603DeleteRectangularArea.createDeleteSpecific(maxAreas);
        });
        
        // 测试区域ID的边界值
        T8603DeleteRectangularArea message = new T8603DeleteRectangularArea();
        message.addAreaId(0L); // 最小值
        message.addAreaId(0xFFFFFFFFL); // 最大值（DWORD）
        
        assertEquals(2, message.getUnsignedAreaCount());
        assertTrue(message.containsAreaId(0L));
        assertTrue(message.containsAreaId(0xFFFFFFFFL));
    }
    
    @Test
    void testRealWorldScenario() {
        // 模拟真实场景：删除多个矩形区域
        List<Long> areaIds = Arrays.asList(1001L, 1002L, 1003L, 1004L, 1005L);
        T8603DeleteRectangularArea message = T8603DeleteRectangularArea.createDeleteSpecific(areaIds);
        
        // 验证消息属性
        assertEquals(0x8603, message.getMessageId());
        assertEquals(5, message.getUnsignedAreaCount());
        assertEquals(areaIds, message.getAreaIds());
        assertTrue(message.isDeleteSpecific());
        assertFalse(message.isDeleteAll());
        
        // 编码消息
        Buffer encoded = message.encodeBody();
        assertEquals(21, encoded.length()); // 1字节区域数 + 5个区域ID * 4字节
        
        // 解码消息
        T8603DeleteRectangularArea decoded = new T8603DeleteRectangularArea();
        decoded.decodeBody(encoded);
        
        // 验证解码结果
        assertEquals(message.getUnsignedAreaCount(), decoded.getUnsignedAreaCount());
        assertEquals(message.getAreaIds(), decoded.getAreaIds());
        assertEquals(message.isDeleteSpecific(), decoded.isDeleteSpecific());
        assertEquals(message, decoded);
        
        // 验证描述信息
        String description = decoded.getDescription();
        assertTrue(description.contains("删除矩形区域消息"));
        assertTrue(description.contains("删除指定矩形区域"));
        assertTrue(description.contains("5"));
    }
}