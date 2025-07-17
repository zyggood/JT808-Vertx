package com.jt808.protocol.message;

import com.jt808.protocol.factory.JT808MessageFactory;
import io.vertx.core.buffer.Buffer;
import org.junit.jupiter.api.Test;
import java.util.Arrays;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

/**
 * T8601DeleteCircularArea 删除圆形区域消息测试类
 * 
 * @author JT808-Vertx
 * @since 1.0.0
 */
class T8601DeleteCircularAreaTest {
    
    @Test
    void testMessageId() {
        T8601DeleteCircularArea message = new T8601DeleteCircularArea();
        assertEquals(0x8601, message.getMessageId());
    }
    
    @Test
    void testDefaultConstructor() {
        T8601DeleteCircularArea message = new T8601DeleteCircularArea();
        assertNotNull(message);
        assertEquals(0, message.getUnsignedAreaCount());
        assertTrue(message.getAreaIds().isEmpty());
        assertTrue(message.isDeleteAll());
        assertFalse(message.isDeleteSpecific());
    }
    
    @Test
    void testConstructorWithNullAreaIds() {
        T8601DeleteCircularArea message = new T8601DeleteCircularArea(null);
        assertNotNull(message);
        assertEquals(0, message.getUnsignedAreaCount());
        assertTrue(message.getAreaIds().isEmpty());
        assertTrue(message.isDeleteAll());
    }
    
    @Test
    void testConstructorWithAreaIds() {
        List<Long> areaIds = Arrays.asList(1001L, 1002L, 1003L);
        T8601DeleteCircularArea message = new T8601DeleteCircularArea(areaIds);
        assertNotNull(message);
        assertEquals(3, message.getUnsignedAreaCount());
        assertEquals(areaIds, message.getAreaIds());
        assertFalse(message.isDeleteAll());
        assertTrue(message.isDeleteSpecific());
    }
    
    @Test
    void testCreateDeleteAll() {
        T8601DeleteCircularArea message = T8601DeleteCircularArea.createDeleteAll();
        assertNotNull(message);
        assertEquals(0, message.getUnsignedAreaCount());
        assertTrue(message.getAreaIds().isEmpty());
        assertTrue(message.isDeleteAll());
        assertFalse(message.isDeleteSpecific());
        assertEquals("删除所有区域", message.getDeleteTypeDescription());
    }
    
    @Test
    void testCreateDeleteSpecific() {
        List<Long> areaIds = Arrays.asList(1001L, 1002L);
        T8601DeleteCircularArea message = T8601DeleteCircularArea.createDeleteSpecific(areaIds);
        assertNotNull(message);
        assertEquals(2, message.getUnsignedAreaCount());
        assertEquals(areaIds, message.getAreaIds());
        assertFalse(message.isDeleteAll());
        assertTrue(message.isDeleteSpecific());
        assertEquals("删除指定区域", message.getDeleteTypeDescription());
    }
    
    @Test
    void testCreateDeleteSpecificWithEmptyList() {
        assertThrows(IllegalArgumentException.class, () -> {
            T8601DeleteCircularArea.createDeleteSpecific(null);
        });
        
        assertThrows(IllegalArgumentException.class, () -> {
            T8601DeleteCircularArea.createDeleteSpecific(Arrays.asList());
        });
    }
    
    @Test
    void testCreateDeleteSpecificWithTooManyAreas() {
        // 创建超过125个区域的列表
        List<Long> tooManyAreas = Arrays.asList(new Long[126]);
        for (int i = 0; i < 126; i++) {
            tooManyAreas.set(i, (long) (i + 1));
        }
        
        assertThrows(IllegalArgumentException.class, () -> {
            T8601DeleteCircularArea.createDeleteSpecific(tooManyAreas);
        });
    }
    
    @Test
    void testCreateDeleteSingle() {
        long areaId = 1001L;
        T8601DeleteCircularArea message = T8601DeleteCircularArea.createDeleteSingle(areaId);
        assertNotNull(message);
        assertEquals(1, message.getUnsignedAreaCount());
        assertEquals(Arrays.asList(areaId), message.getAreaIds());
        assertTrue(message.containsAreaId(areaId));
        assertFalse(message.isDeleteAll());
        assertTrue(message.isDeleteSpecific());
    }
    
    @Test
    void testGettersAndSetters() {
        T8601DeleteCircularArea message = new T8601DeleteCircularArea();
        
        // 测试区域数量
        message.setAreaCount((byte) 3);
        assertEquals(3, message.getUnsignedAreaCount());
        assertEquals((byte) 3, message.getAreaCount());
        
        // 测试区域ID列表
        List<Long> areaIds = Arrays.asList(1001L, 1002L, 1003L);
        message.setAreaIds(areaIds);
        assertEquals(areaIds, message.getAreaIds());
        assertEquals(3, message.getUnsignedAreaCount());
    }
    
    @Test
    void testAddAreaId() {
        T8601DeleteCircularArea message = new T8601DeleteCircularArea();
        
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
        T8601DeleteCircularArea message = new T8601DeleteCircularArea(areaIds);
        
        assertTrue(message.removeAreaId(1002L));
        assertEquals(2, message.getUnsignedAreaCount());
        assertFalse(message.containsAreaId(1002L));
        
        assertFalse(message.removeAreaId(9999L)); // 不存在的区域ID
        assertEquals(2, message.getUnsignedAreaCount());
    }
    
    @Test
    void testClearAreaIds() {
        List<Long> areaIds = Arrays.asList(1001L, 1002L, 1003L);
        T8601DeleteCircularArea message = new T8601DeleteCircularArea(areaIds);
        
        message.clearAreaIds();
        assertEquals(0, message.getUnsignedAreaCount());
        assertTrue(message.getAreaIds().isEmpty());
        assertTrue(message.isDeleteAll());
    }
    
    @Test
    void testEncodeBodyDeleteAll() {
        T8601DeleteCircularArea message = T8601DeleteCircularArea.createDeleteAll();
        
        Buffer buffer = message.encodeBody();
        
        assertEquals(1, buffer.length()); // 只有1字节的区域数量
        assertEquals(0, buffer.getByte(0)); // 区域数量为0
    }
    
    @Test
    void testEncodeBodyDeleteSpecific() {
        List<Long> areaIds = Arrays.asList(1001L, 1002L);
        T8601DeleteCircularArea message = T8601DeleteCircularArea.createDeleteSpecific(areaIds);
        
        Buffer buffer = message.encodeBody();
        
        assertEquals(9, buffer.length()); // 1字节区域数量 + 2个4字节区域ID
        assertEquals(2, Byte.toUnsignedInt(buffer.getByte(0))); // 区域数量为2
        assertEquals(1001L, buffer.getUnsignedInt(1)); // 第一个区域ID
        assertEquals(1002L, buffer.getUnsignedInt(5)); // 第二个区域ID
    }
    
    @Test
    void testDecodeBodyDeleteAll() {
        Buffer buffer = Buffer.buffer();
        buffer.appendByte((byte) 0); // 区域数量为0
        
        T8601DeleteCircularArea message = new T8601DeleteCircularArea();
        message.decodeBody(buffer);
        
        assertEquals(0, message.getUnsignedAreaCount());
        assertTrue(message.getAreaIds().isEmpty());
        assertTrue(message.isDeleteAll());
    }
    
    @Test
    void testDecodeBodyDeleteSpecific() {
        Buffer buffer = Buffer.buffer();
        buffer.appendByte((byte) 2); // 区域数量为2
        buffer.appendUnsignedInt(1001L); // 第一个区域ID
        buffer.appendUnsignedInt(1002L); // 第二个区域ID
        
        T8601DeleteCircularArea message = new T8601DeleteCircularArea();
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
        T8601DeleteCircularArea message = new T8601DeleteCircularArea();
        
        assertThrows(IllegalArgumentException.class, () -> {
            message.decodeBody(emptyBuffer);
        });
        
        // 测试区域ID数据不足
        Buffer insufficientBuffer = Buffer.buffer();
        insufficientBuffer.appendByte((byte) 2); // 声明有2个区域
        insufficientBuffer.appendUnsignedInt(1001L); // 只提供1个区域ID
        
        T8601DeleteCircularArea message2 = new T8601DeleteCircularArea();
        assertThrows(IllegalArgumentException.class, () -> {
            message2.decodeBody(insufficientBuffer);
        });
    }
    
    @Test
    void testDecodeBodyInvalidAreaCount() {
        // 测试负数区域数量
        Buffer buffer = Buffer.buffer();
        buffer.appendByte((byte) -1);
        
        T8601DeleteCircularArea message = new T8601DeleteCircularArea();
        assertThrows(IllegalArgumentException.class, () -> {
            message.decodeBody(buffer);
        });
        
        // 测试超过最大区域数量
        Buffer buffer2 = Buffer.buffer();
        buffer2.appendByte((byte) 126); // 超过125的限制
        
        T8601DeleteCircularArea message2 = new T8601DeleteCircularArea();
        assertThrows(IllegalArgumentException.class, () -> {
            message2.decodeBody(buffer2);
        });
    }
    
    @Test
    void testEncodeDecodeConsistency() {
        // 测试删除所有区域的编解码一致性
        T8601DeleteCircularArea original1 = T8601DeleteCircularArea.createDeleteAll();
        Buffer buffer1 = original1.encodeBody();
        
        T8601DeleteCircularArea decoded1 = new T8601DeleteCircularArea();
        decoded1.decodeBody(buffer1);
        
        assertEquals(original1.getUnsignedAreaCount(), decoded1.getUnsignedAreaCount());
        assertEquals(original1.getAreaIds(), decoded1.getAreaIds());
        assertEquals(original1.isDeleteAll(), decoded1.isDeleteAll());
        
        // 测试删除指定区域的编解码一致性
        List<Long> areaIds = Arrays.asList(1001L, 1002L, 1003L);
        T8601DeleteCircularArea original2 = T8601DeleteCircularArea.createDeleteSpecific(areaIds);
        Buffer buffer2 = original2.encodeBody();
        
        T8601DeleteCircularArea decoded2 = new T8601DeleteCircularArea();
        decoded2.decodeBody(buffer2);
        
        assertEquals(original2.getUnsignedAreaCount(), decoded2.getUnsignedAreaCount());
        assertEquals(original2.getAreaIds(), decoded2.getAreaIds());
        assertEquals(original2.isDeleteSpecific(), decoded2.isDeleteSpecific());
    }
    
    @Test
    void testContainsAreaId() {
        List<Long> areaIds = Arrays.asList(1001L, 1002L, 1003L);
        T8601DeleteCircularArea message = new T8601DeleteCircularArea(areaIds);
        
        assertTrue(message.containsAreaId(1001L));
        assertTrue(message.containsAreaId(1002L));
        assertTrue(message.containsAreaId(1003L));
        assertFalse(message.containsAreaId(9999L));
    }
    
    @Test
    void testGetDescription() {
        // 测试删除所有区域的描述
        T8601DeleteCircularArea deleteAll = T8601DeleteCircularArea.createDeleteAll();
        assertEquals("删除圆形区域[删除所有区域]", deleteAll.getDescription());
        
        // 测试删除指定区域的描述
        List<Long> areaIds = Arrays.asList(1001L, 1002L);
        T8601DeleteCircularArea deleteSpecific = T8601DeleteCircularArea.createDeleteSpecific(areaIds);
        assertEquals("删除圆形区域[删除指定区域, 区域数量: 2]", deleteSpecific.getDescription());
    }
    
    @Test
    void testToString() {
        // 测试删除所有区域的toString
        T8601DeleteCircularArea deleteAll = T8601DeleteCircularArea.createDeleteAll();
        String toStringAll = deleteAll.toString();
        assertTrue(toStringAll.contains("T8601DeleteCircularArea"));
        assertTrue(toStringAll.contains("0x8601"));
        assertTrue(toStringAll.contains("areaCount=0"));
        assertTrue(toStringAll.contains("deleteType=删除所有区域"));
        assertTrue(toStringAll.contains("areaIds=[]"));
        
        // 测试删除指定区域的toString
        List<Long> areaIds = Arrays.asList(1001L, 1002L);
        T8601DeleteCircularArea deleteSpecific = T8601DeleteCircularArea.createDeleteSpecific(areaIds);
        String toStringSpecific = deleteSpecific.toString();
        assertTrue(toStringSpecific.contains("areaCount=2"));
        assertTrue(toStringSpecific.contains("deleteType=删除指定区域"));
        assertTrue(toStringSpecific.contains("1001"));
        assertTrue(toStringSpecific.contains("1002"));
    }
    
    @Test
    void testEquals() {
        List<Long> areaIds1 = Arrays.asList(1001L, 1002L);
        List<Long> areaIds2 = Arrays.asList(1001L, 1002L);
        List<Long> areaIds3 = Arrays.asList(1003L, 1004L);
        
        T8601DeleteCircularArea message1 = new T8601DeleteCircularArea(areaIds1);
        T8601DeleteCircularArea message2 = new T8601DeleteCircularArea(areaIds2);
        T8601DeleteCircularArea message3 = new T8601DeleteCircularArea(areaIds3);
        T8601DeleteCircularArea deleteAll = T8601DeleteCircularArea.createDeleteAll();
        
        // 测试相等性
        assertEquals(message1, message2);
        assertNotEquals(message1, message3);
        assertNotEquals(message1, deleteAll);
        assertNotEquals(message1, null);
        assertNotEquals(message1, "not a message");
        
        // 测试自反性
        assertEquals(message1, message1);
    }
    
    @Test
    void testHashCode() {
        List<Long> areaIds1 = Arrays.asList(1001L, 1002L);
        List<Long> areaIds2 = Arrays.asList(1001L, 1002L);
        
        T8601DeleteCircularArea message1 = new T8601DeleteCircularArea(areaIds1);
        T8601DeleteCircularArea message2 = new T8601DeleteCircularArea(areaIds2);
        
        assertEquals(message1.hashCode(), message2.hashCode());
    }
    
    @Test
    void testMaxAreaCountConstant() {
        assertEquals(125, T8601DeleteCircularArea.MAX_AREA_COUNT);
    }
    
    @Test
    void testDeleteAllConstant() {
        assertEquals(0, T8601DeleteCircularArea.DELETE_ALL);
    }
    
    @Test
    void testMessageFactoryIntegration() {
        JT808MessageFactory factory = JT808MessageFactory.getInstance();
        
        // 测试工厂创建消息
        JT808Message message = factory.createMessage(0x8601);
        assertNotNull(message);
        assertTrue(message instanceof T8601DeleteCircularArea);
        assertEquals(0x8601, message.getMessageId());
    }
    
    @Test
    void testBoundaryValues() {
        // 测试最大区域数量边界
        T8601DeleteCircularArea message = new T8601DeleteCircularArea();
        
        // 添加最大数量的区域
        for (int i = 1; i <= T8601DeleteCircularArea.MAX_AREA_COUNT; i++) {
            message.addAreaId(i);
        }
        assertEquals(T8601DeleteCircularArea.MAX_AREA_COUNT, message.getUnsignedAreaCount());
        
        // 尝试添加超过最大数量的区域
        assertThrows(IllegalStateException.class, () -> {
            message.addAreaId(126L);
        });
    }
    
    @Test
    void testRealWorldScenario() {
        // 模拟真实场景：删除学校周边的圆形区域
        List<Long> schoolAreaIds = Arrays.asList(2001L, 2002L, 2003L); // 学校区域ID
        T8601DeleteCircularArea deleteSchoolAreas = T8601DeleteCircularArea.createDeleteSpecific(schoolAreaIds);
        
        // 验证消息内容
        assertEquals(3, deleteSchoolAreas.getUnsignedAreaCount());
        assertTrue(deleteSchoolAreas.containsAreaId(2001L));
        assertTrue(deleteSchoolAreas.containsAreaId(2002L));
        assertTrue(deleteSchoolAreas.containsAreaId(2003L));
        assertFalse(deleteSchoolAreas.isDeleteAll());
        
        // 编码消息
        Buffer buffer = deleteSchoolAreas.encodeBody();
        
        // 解码验证
        T8601DeleteCircularArea decoded = new T8601DeleteCircularArea();
        decoded.decodeBody(buffer);
        
        assertEquals(deleteSchoolAreas.getAreaIds(), decoded.getAreaIds());
        assertEquals(deleteSchoolAreas.getUnsignedAreaCount(), decoded.getUnsignedAreaCount());
        
        // 模拟删除所有区域的场景
        T8601DeleteCircularArea deleteAllAreas = T8601DeleteCircularArea.createDeleteAll();
        assertTrue(deleteAllAreas.isDeleteAll());
        assertEquals("删除圆形区域[删除所有区域]", deleteAllAreas.getDescription());
    }
}