package com.jt808.protocol.message;

import io.vertx.core.buffer.Buffer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * T8203人工确认报警消息测试类
 * 
 * @author JT808 Protocol Team
 * @version 1.0
 */
class T8203ManualAlarmConfirmationTest {
    
    @Test
    @DisplayName("测试消息ID")
    void testMessageId() {
        T8203ManualAlarmConfirmation message = new T8203ManualAlarmConfirmation();
        assertEquals(0x8203, message.getMessageId());
    }
    
    @Test
    @DisplayName("测试默认构造函数")
    void testDefaultConstructor() {
        T8203ManualAlarmConfirmation message = new T8203ManualAlarmConfirmation();
        assertEquals(0, message.getAlarmSequenceNumber());
        assertEquals(0L, message.getConfirmationAlarmType());
    }
    
    @Test
    @DisplayName("测试参数化构造函数")
    void testParameterizedConstructor() {
        T8203ManualAlarmConfirmation message = new T8203ManualAlarmConfirmation(123, 0x00000001L);
        assertEquals(123, message.getAlarmSequenceNumber());
        assertEquals(0x00000001L, message.getConfirmationAlarmType());
    }
    
    @Test
    @DisplayName("测试消息体编码 - 确认指定流水号报警")
    void testEncodeBodySpecificAlarm() {
        T8203ManualAlarmConfirmation message = new T8203ManualAlarmConfirmation(0x1234, 0x00000001L);
        Buffer encoded = message.encodeBody();
        
        assertEquals(6, encoded.length());
        
        // 验证报警消息流水号 (WORD, 大端序)
        assertEquals(0x12, encoded.getByte(0) & 0xFF);
        assertEquals(0x34, encoded.getByte(1) & 0xFF);
        
        // 验证人工确认报警类型 (DWORD, 大端序)
        assertEquals(0x00, encoded.getByte(2) & 0xFF);
        assertEquals(0x00, encoded.getByte(3) & 0xFF);
        assertEquals(0x00, encoded.getByte(4) & 0xFF);
        assertEquals(0x01, encoded.getByte(5) & 0xFF);
    }
    
    @Test
    @DisplayName("测试消息体编码 - 确认所有报警")
    void testEncodeBodyAllAlarms() {
        T8203ManualAlarmConfirmation message = new T8203ManualAlarmConfirmation(0, 0x08000000L);
        Buffer encoded = message.encodeBody();
        
        assertEquals(6, encoded.length());
        
        // 验证报警消息流水号为0
        assertEquals(0x00, encoded.getByte(0) & 0xFF);
        assertEquals(0x00, encoded.getByte(1) & 0xFF);
        
        // 验证人工确认报警类型
        assertEquals(0x08, encoded.getByte(2) & 0xFF);
        assertEquals(0x00, encoded.getByte(3) & 0xFF);
        assertEquals(0x00, encoded.getByte(4) & 0xFF);
        assertEquals(0x00, encoded.getByte(5) & 0xFF);
    }
    
    @Test
    @DisplayName("测试消息体编码 - 边界值")
    void testEncodeBodyBoundaryValues() {
        // 测试最大值
        T8203ManualAlarmConfirmation message = new T8203ManualAlarmConfirmation(0xFFFF, 0xFFFFFFFFL);
        Buffer encoded = message.encodeBody();
        
        assertEquals(6, encoded.length());
        
        // 验证最大WORD值
        assertEquals(0xFF, encoded.getByte(0) & 0xFF);
        assertEquals(0xFF, encoded.getByte(1) & 0xFF);
        
        // 验证最大DWORD值
        assertEquals(0xFF, encoded.getByte(2) & 0xFF);
        assertEquals(0xFF, encoded.getByte(3) & 0xFF);
        assertEquals(0xFF, encoded.getByte(4) & 0xFF);
        assertEquals(0xFF, encoded.getByte(5) & 0xFF);
    }
    
    @Test
    @DisplayName("测试消息体解码 - 确认指定流水号报警")
    void testDecodeBodySpecificAlarm() {
        Buffer buffer = Buffer.buffer();
        buffer.appendUnsignedShort(0x1234); // 报警消息流水号
        buffer.appendUnsignedInt(0x00000001L); // 人工确认报警类型
        
        T8203ManualAlarmConfirmation message = new T8203ManualAlarmConfirmation();
        message.decodeBody(buffer);
        
        assertEquals(0x1234, message.getAlarmSequenceNumber());
        assertEquals(0x00000001L, message.getConfirmationAlarmType());
    }
    
    @Test
    @DisplayName("测试消息体解码 - 确认所有报警")
    void testDecodeBodyAllAlarms() {
        Buffer buffer = Buffer.buffer();
        buffer.appendUnsignedShort(0); // 报警消息流水号为0
        buffer.appendUnsignedInt(0x08000000L); // 人工确认报警类型
        
        T8203ManualAlarmConfirmation message = new T8203ManualAlarmConfirmation();
        message.decodeBody(buffer);
        
        assertEquals(0, message.getAlarmSequenceNumber());
        assertEquals(0x08000000L, message.getConfirmationAlarmType());
        assertTrue(message.isConfirmAllAlarms());
    }
    
    @Test
    @DisplayName("测试消息体解码 - 边界值")
    void testDecodeBodyBoundaryValues() {
        Buffer buffer = Buffer.buffer();
        buffer.appendUnsignedShort(0xFFFF); // 最大WORD值
        buffer.appendUnsignedInt(0xFFFFFFFFL); // 最大DWORD值
        
        T8203ManualAlarmConfirmation message = new T8203ManualAlarmConfirmation();
        message.decodeBody(buffer);
        
        assertEquals(0xFFFF, message.getAlarmSequenceNumber());
        assertEquals(0xFFFFFFFFL, message.getConfirmationAlarmType());
    }
    
    @Test
    @DisplayName("测试消息体解码 - 空指针异常")
    void testDecodeBodyNullException() {
        T8203ManualAlarmConfirmation message = new T8203ManualAlarmConfirmation();
        
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            message.decodeBody(null);
        });
        
        assertEquals("消息体不能为空", exception.getMessage());
    }
    
    @Test
    @DisplayName("测试消息体解码 - 长度不足异常")
    void testDecodeBodyInsufficientLength() {
        T8203ManualAlarmConfirmation message = new T8203ManualAlarmConfirmation();
        Buffer buffer = Buffer.buffer();
        buffer.appendByte((byte) 0x01); // 只有1字节，不足6字节
        
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            message.decodeBody(buffer);
        });
        
        assertTrue(exception.getMessage().contains("人工确认报警消息体长度应为6字节"));
    }
    
    @Test
    @DisplayName("测试编解码一致性")
    void testEncodeDecodeConsistency() {
        T8203ManualAlarmConfirmation original = new T8203ManualAlarmConfirmation(0x5678, 0x12345678L);
        
        // 编码
        Buffer encoded = original.encodeBody();
        
        // 解码
        T8203ManualAlarmConfirmation decoded = new T8203ManualAlarmConfirmation();
        decoded.decodeBody(encoded);
        
        // 验证一致性
        assertEquals(original.getAlarmSequenceNumber(), decoded.getAlarmSequenceNumber());
        assertEquals(original.getConfirmationAlarmType(), decoded.getConfirmationAlarmType());
    }
    
    @Test
    @DisplayName("测试工厂方法 - 创建确认指定报警")
    void testCreateConfirmSpecificAlarm() {
        T8203ManualAlarmConfirmation message = T8203ManualAlarmConfirmation.createConfirmSpecificAlarm(
            123, T8203ManualAlarmConfirmation.AlarmConfirmationType.EMERGENCY_ALARM);
        
        assertEquals(123, message.getAlarmSequenceNumber());
        assertEquals(T8203ManualAlarmConfirmation.AlarmConfirmationType.EMERGENCY_ALARM, 
                    message.getConfirmationAlarmType());
        assertFalse(message.isConfirmAllAlarms());
    }
    
    @Test
    @DisplayName("测试工厂方法 - 创建确认所有报警")
    void testCreateConfirmAllAlarms() {
        T8203ManualAlarmConfirmation message = T8203ManualAlarmConfirmation.createConfirmAllAlarms(
            T8203ManualAlarmConfirmation.AlarmConfirmationType.DANGER_WARNING);
        
        assertEquals(0, message.getAlarmSequenceNumber());
        assertEquals(T8203ManualAlarmConfirmation.AlarmConfirmationType.DANGER_WARNING, 
                    message.getConfirmationAlarmType());
        assertTrue(message.isConfirmAllAlarms());
    }
    
    @Test
    @DisplayName("测试工厂方法 - 流水号超出范围异常")
    void testCreateConfirmSpecificAlarmInvalidSequence() {
        // 测试负数
        IllegalArgumentException exception1 = assertThrows(IllegalArgumentException.class, () -> {
            T8203ManualAlarmConfirmation.createConfirmSpecificAlarm(-1, 0x00000001L);
        });
        assertEquals("报警消息流水号必须在0-65535范围内", exception1.getMessage());
        
        // 测试超出范围
        IllegalArgumentException exception2 = assertThrows(IllegalArgumentException.class, () -> {
            T8203ManualAlarmConfirmation.createConfirmSpecificAlarm(0x10000, 0x00000001L);
        });
        assertEquals("报警消息流水号必须在0-65535范围内", exception2.getMessage());
    }
    
    @Test
    @DisplayName("测试工厂方法 - 报警类型为负数异常")
    void testCreateConfirmNegativeAlarmType() {
        IllegalArgumentException exception1 = assertThrows(IllegalArgumentException.class, () -> {
            T8203ManualAlarmConfirmation.createConfirmSpecificAlarm(123, -1L);
        });
        assertEquals("报警类型不能为负数", exception1.getMessage());
        
        IllegalArgumentException exception2 = assertThrows(IllegalArgumentException.class, () -> {
            T8203ManualAlarmConfirmation.createConfirmAllAlarms(-1L);
        });
        assertEquals("报警类型不能为负数", exception2.getMessage());
    }
    
    @Test
    @DisplayName("测试Setter异常")
    void testSetterExceptions() {
        T8203ManualAlarmConfirmation message = new T8203ManualAlarmConfirmation();
        
        // 测试设置负数流水号
        IllegalArgumentException exception1 = assertThrows(IllegalArgumentException.class, () -> {
            message.setAlarmSequenceNumber(-1);
        });
        assertEquals("报警消息流水号必须在0-65535范围内", exception1.getMessage());
        
        // 测试设置超出范围流水号
        IllegalArgumentException exception2 = assertThrows(IllegalArgumentException.class, () -> {
            message.setAlarmSequenceNumber(0x10000);
        });
        assertEquals("报警消息流水号必须在0-65535范围内", exception2.getMessage());
        
        // 测试设置负数报警类型
        IllegalArgumentException exception3 = assertThrows(IllegalArgumentException.class, () -> {
            message.setConfirmationAlarmType(-1L);
        });
        assertEquals("报警类型不能为负数", exception3.getMessage());
    }
    
    @Test
    @DisplayName("测试equals方法")
    void testEquals() {
        T8203ManualAlarmConfirmation message1 = new T8203ManualAlarmConfirmation(123, 0x00000001L);
        T8203ManualAlarmConfirmation message2 = new T8203ManualAlarmConfirmation(123, 0x00000001L);
        T8203ManualAlarmConfirmation message3 = new T8203ManualAlarmConfirmation(456, 0x00000001L);
        T8203ManualAlarmConfirmation message4 = new T8203ManualAlarmConfirmation(123, 0x00000002L);
        
        assertEquals(message1, message2);
        assertNotEquals(message1, message3);
        assertNotEquals(message1, message4);
        assertNotEquals(message1, null);
        assertNotEquals(message1, "not a message");
        assertEquals(message1, message1);
    }
    
    @Test
    @DisplayName("测试hashCode方法")
    void testHashCode() {
        T8203ManualAlarmConfirmation message1 = new T8203ManualAlarmConfirmation(123, 0x00000001L);
        T8203ManualAlarmConfirmation message2 = new T8203ManualAlarmConfirmation(123, 0x00000001L);
        
        assertEquals(message1.hashCode(), message2.hashCode());
    }
    
    @Test
    @DisplayName("测试toString方法")
    void testToString() {
        T8203ManualAlarmConfirmation message = new T8203ManualAlarmConfirmation(123, 
            T8203ManualAlarmConfirmation.AlarmConfirmationType.EMERGENCY_ALARM);
        
        String result = message.toString();
        
        assertTrue(result.contains("T8203ManualAlarmConfirmation"));
        assertTrue(result.contains("alarmSequenceNumber=123"));
        assertTrue(result.contains("confirmationAlarmType=0x00000001"));
        assertTrue(result.contains("紧急报警"));
    }
    
    @Test
    @DisplayName("测试报警类型常量")
    void testAlarmTypeConstants() {
        assertEquals(0x00000001L, T8203ManualAlarmConfirmation.AlarmConfirmationType.EMERGENCY_ALARM);
        assertEquals(0x00000008L, T8203ManualAlarmConfirmation.AlarmConfirmationType.DANGER_WARNING);
        assertEquals(0x00100000L, T8203ManualAlarmConfirmation.AlarmConfirmationType.AREA_ALARM);
        assertEquals(0x00200000L, T8203ManualAlarmConfirmation.AlarmConfirmationType.ROUTE_ALARM);
        assertEquals(0x00400000L, T8203ManualAlarmConfirmation.AlarmConfirmationType.DRIVING_TIME_ALARM);
        assertEquals(0x08000000L, T8203ManualAlarmConfirmation.AlarmConfirmationType.ILLEGAL_IGNITION_ALARM);
        assertEquals(0x10000000L, T8203ManualAlarmConfirmation.AlarmConfirmationType.ILLEGAL_DISPLACEMENT_ALARM);
    }
    
    @Test
    @DisplayName("测试报警类型检查")
    void testHasAlarmType() {
        long combinedType = T8203ManualAlarmConfirmation.AlarmConfirmationType.EMERGENCY_ALARM |
                           T8203ManualAlarmConfirmation.AlarmConfirmationType.DANGER_WARNING;
        
        assertTrue(T8203ManualAlarmConfirmation.AlarmConfirmationType.hasAlarmType(
            combinedType, T8203ManualAlarmConfirmation.AlarmConfirmationType.EMERGENCY_ALARM));
        assertTrue(T8203ManualAlarmConfirmation.AlarmConfirmationType.hasAlarmType(
            combinedType, T8203ManualAlarmConfirmation.AlarmConfirmationType.DANGER_WARNING));
        assertFalse(T8203ManualAlarmConfirmation.AlarmConfirmationType.hasAlarmType(
            combinedType, T8203ManualAlarmConfirmation.AlarmConfirmationType.AREA_ALARM));
    }
    
    @Test
    @DisplayName("测试报警类型描述")
    void testAlarmTypeDescription() {
        // 测试单个报警类型
        String desc1 = T8203ManualAlarmConfirmation.AlarmConfirmationType.getAlarmTypeDescription(
            T8203ManualAlarmConfirmation.AlarmConfirmationType.EMERGENCY_ALARM);
        assertEquals("紧急报警", desc1);
        
        // 测试组合报警类型
        long combinedType = T8203ManualAlarmConfirmation.AlarmConfirmationType.EMERGENCY_ALARM |
                           T8203ManualAlarmConfirmation.AlarmConfirmationType.DANGER_WARNING;
        String desc2 = T8203ManualAlarmConfirmation.AlarmConfirmationType.getAlarmTypeDescription(combinedType);
        assertTrue(desc2.contains("紧急报警"));
        assertTrue(desc2.contains("危险预警"));
        
        // 测试未知报警类型
        String desc3 = T8203ManualAlarmConfirmation.AlarmConfirmationType.getAlarmTypeDescription(0L);
        assertEquals("未知报警类型", desc3);
    }
    
    @Test
    @DisplayName("测试确认状态描述")
    void testConfirmationDescription() {
        // 测试确认指定流水号报警
        T8203ManualAlarmConfirmation message1 = new T8203ManualAlarmConfirmation(123, 
            T8203ManualAlarmConfirmation.AlarmConfirmationType.EMERGENCY_ALARM);
        String desc1 = message1.getConfirmationDescription();
        assertTrue(desc1.contains("确认流水号[123]"));
        assertTrue(desc1.contains("紧急报警"));
        
        // 测试确认所有报警
        T8203ManualAlarmConfirmation message2 = new T8203ManualAlarmConfirmation(0, 
            T8203ManualAlarmConfirmation.AlarmConfirmationType.DANGER_WARNING);
        String desc2 = message2.getConfirmationDescription();
        assertTrue(desc2.contains("确认所有"));
        assertTrue(desc2.contains("危险预警"));
    }
    
    @Test
    @DisplayName("测试消息描述")
    void testMessageDescription() {
        T8203ManualAlarmConfirmation message = new T8203ManualAlarmConfirmation();
        assertEquals("人工确认报警", message.getMessageDescription());
    }
}