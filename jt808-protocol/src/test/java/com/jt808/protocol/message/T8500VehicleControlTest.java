package com.jt808.protocol.message;

import com.jt808.protocol.factory.JT808MessageFactory;
import io.vertx.core.buffer.Buffer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * T8500车辆控制消息测试类
 * 
 * @author JT808 Protocol Team
 * @version 1.0
 * @since 1.0
 */
class T8500VehicleControlTest {

    @Test
    void testMessageId() {
        T8500VehicleControl message = new T8500VehicleControl();
        assertEquals(0x8500, message.getMessageId());
    }

    @Test
    void testDefaultConstructor() {
        T8500VehicleControl message = new T8500VehicleControl();
        assertEquals(0, message.getControlFlag());
        assertEquals(0, message.getControlFlagUnsigned());
        assertTrue(message.isDoorUnlock());
        assertFalse(message.isDoorLock());
    }

    @Test
    void testParameterConstructor() {
        T8500VehicleControl message = new T8500VehicleControl((byte) 0x01);
        assertEquals(1, message.getControlFlag());
        assertEquals(1, message.getControlFlagUnsigned());
        assertFalse(message.isDoorUnlock());
        assertTrue(message.isDoorLock());
    }

    @Test
    void testCreateDoorUnlock() {
        T8500VehicleControl message = T8500VehicleControl.createDoorUnlock();
        assertEquals(T8500VehicleControl.ControlFlag.DOOR_UNLOCK, message.getControlFlag());
        assertTrue(message.isDoorUnlock());
        assertFalse(message.isDoorLock());
        assertEquals("车门解锁", message.getControlDescription());
    }

    @Test
    void testCreateDoorLock() {
        T8500VehicleControl message = T8500VehicleControl.createDoorLock();
        assertEquals(T8500VehicleControl.ControlFlag.DOOR_LOCK, message.getControlFlag());
        assertFalse(message.isDoorUnlock());
        assertTrue(message.isDoorLock());
        assertEquals("车门加锁", message.getControlDescription());
    }

    @Test
    void testEncodeBody() {
        // 测试车门解锁
        T8500VehicleControl unlockMessage = T8500VehicleControl.createDoorUnlock();
        Buffer unlockBuffer = unlockMessage.encodeBody();
        assertEquals(1, unlockBuffer.length());
        assertEquals(0x00, unlockBuffer.getByte(0));

        // 测试车门加锁
        T8500VehicleControl lockMessage = T8500VehicleControl.createDoorLock();
        Buffer lockBuffer = lockMessage.encodeBody();
        assertEquals(1, lockBuffer.length());
        assertEquals(0x01, lockBuffer.getByte(0));
    }

    @Test
    void testDecodeBody() {
        // 测试车门解锁解码
        T8500VehicleControl unlockMessage = new T8500VehicleControl();
        Buffer unlockBuffer = Buffer.buffer().appendByte((byte) 0x00);
        unlockMessage.decodeBody(unlockBuffer);
        assertEquals(0x00, unlockMessage.getControlFlag());
        assertTrue(unlockMessage.isDoorUnlock());
        assertFalse(unlockMessage.isDoorLock());

        // 测试车门加锁解码
        T8500VehicleControl lockMessage = new T8500VehicleControl();
        Buffer lockBuffer = Buffer.buffer().appendByte((byte) 0x01);
        lockMessage.decodeBody(lockBuffer);
        assertEquals(0x01, lockMessage.getControlFlag());
        assertFalse(lockMessage.isDoorUnlock());
        assertTrue(lockMessage.isDoorLock());
    }

    @Test
    void testEncodeDecodeConsistency() {
        // 测试车门解锁的编解码一致性
        T8500VehicleControl original = T8500VehicleControl.createDoorUnlock();
        Buffer encoded = original.encodeBody();
        
        T8500VehicleControl decoded = new T8500VehicleControl();
        decoded.decodeBody(encoded);
        
        assertEquals(original.getControlFlag(), decoded.getControlFlag());
        assertEquals(original.isDoorUnlock(), decoded.isDoorUnlock());
        assertEquals(original.isDoorLock(), decoded.isDoorLock());
        assertEquals(original.getControlDescription(), decoded.getControlDescription());

        // 测试车门加锁的编解码一致性
        T8500VehicleControl original2 = T8500VehicleControl.createDoorLock();
        Buffer encoded2 = original2.encodeBody();
        
        T8500VehicleControl decoded2 = new T8500VehicleControl();
        decoded2.decodeBody(encoded2);
        
        assertEquals(original2.getControlFlag(), decoded2.getControlFlag());
        assertEquals(original2.isDoorUnlock(), decoded2.isDoorUnlock());
        assertEquals(original2.isDoorLock(), decoded2.isDoorLock());
        assertEquals(original2.getControlDescription(), decoded2.getControlDescription());
    }

    @Test
    void testControlFlagOperations() {
        T8500VehicleControl message = new T8500VehicleControl();
        
        // 测试设置车门解锁
        message.setControlFlag(T8500VehicleControl.ControlFlag.DOOR_UNLOCK);
        assertTrue(message.isDoorUnlock());
        assertFalse(message.isDoorLock());
        assertEquals("车门解锁", message.getControlDescription());
        
        // 测试设置车门加锁
        message.setControlFlag(T8500VehicleControl.ControlFlag.DOOR_LOCK);
        assertFalse(message.isDoorUnlock());
        assertTrue(message.isDoorLock());
        assertEquals("车门加锁", message.getControlDescription());
    }

    @Test
    void testUnsignedValue() {
        // 测试正数
        T8500VehicleControl message1 = new T8500VehicleControl((byte) 0x01);
        assertEquals(1, message1.getControlFlagUnsigned());
        
        // 测试负数（作为无符号值）
        T8500VehicleControl message2 = new T8500VehicleControl((byte) 0xFF);
        assertEquals(255, message2.getControlFlagUnsigned());
        
        // 测试零
        T8500VehicleControl message3 = new T8500VehicleControl((byte) 0x00);
        assertEquals(0, message3.getControlFlagUnsigned());
    }

    @Test
    void testMessageDescription() {
        T8500VehicleControl message = new T8500VehicleControl();
        assertEquals("车辆控制消息", message.getMessageDescription());
    }

    @Test
    void testControlDescription() {
        // 测试车门解锁描述
        T8500VehicleControl unlockMessage = T8500VehicleControl.createDoorUnlock();
        assertEquals("车门解锁", unlockMessage.getControlDescription());
        
        // 测试车门加锁描述
        T8500VehicleControl lockMessage = T8500VehicleControl.createDoorLock();
        assertEquals("车门加锁", lockMessage.getControlDescription());
        
        // 测试未知控制操作（保留位设置）
        T8500VehicleControl unknownMessage = new T8500VehicleControl((byte) 0x02);
        assertEquals("车门解锁", unknownMessage.getControlDescription()); // 位0=0，所以是解锁
        
        unknownMessage.setControlFlag((byte) 0x03);
        assertEquals("车门加锁", unknownMessage.getControlDescription()); // 位0=1，所以是加锁
    }

    @Test
    void testDecodeBodyExceptions() {
        T8500VehicleControl message = new T8500VehicleControl();
        
        // 测试空消息体
        assertThrows(IllegalArgumentException.class, () -> {
            message.decodeBody(null);
        });
        
        // 测试消息体长度不足
        assertThrows(IllegalArgumentException.class, () -> {
            message.decodeBody(Buffer.buffer());
        });
        
        // 测试消息体长度过长
        assertThrows(IllegalArgumentException.class, () -> {
            Buffer longBuffer = Buffer.buffer().appendByte((byte) 0x01).appendByte((byte) 0x02);
            message.decodeBody(longBuffer);
        });
    }

    @Test
    void testToString() {
        T8500VehicleControl unlockMessage = T8500VehicleControl.createDoorUnlock();
        String unlockStr = unlockMessage.toString();
        assertTrue(unlockStr.contains("T8500VehicleControl"));
        assertTrue(unlockStr.contains("controlFlag=0x00"));
        assertTrue(unlockStr.contains("车门解锁"));
        
        T8500VehicleControl lockMessage = T8500VehicleControl.createDoorLock();
        String lockStr = lockMessage.toString();
        assertTrue(lockStr.contains("T8500VehicleControl"));
        assertTrue(lockStr.contains("controlFlag=0x01"));
        assertTrue(lockStr.contains("车门加锁"));
    }

    @Test
    void testEquals() {
        T8500VehicleControl message1 = T8500VehicleControl.createDoorUnlock();
        T8500VehicleControl message2 = T8500VehicleControl.createDoorUnlock();
        T8500VehicleControl message3 = T8500VehicleControl.createDoorLock();
        
        // 测试相等
        assertEquals(message1, message2);
        assertEquals(message1, message1);
        
        // 测试不相等
        assertNotEquals(message1, message3);
        assertNotEquals(message1, null);
        assertNotEquals(message1, "not a message");
    }

    @Test
    void testHashCode() {
        T8500VehicleControl message1 = T8500VehicleControl.createDoorUnlock();
        T8500VehicleControl message2 = T8500VehicleControl.createDoorUnlock();
        
        assertEquals(message1.hashCode(), message2.hashCode());
    }

    @Test
    void testControlFlagConstants() {
        assertEquals(0x00, T8500VehicleControl.ControlFlag.DOOR_UNLOCK);
        assertEquals(0x01, T8500VehicleControl.ControlFlag.DOOR_LOCK);
    }

    @Test
    void testControlFlagHasControlFlag() {
        // 测试车门解锁标志检查
        assertFalse(T8500VehicleControl.ControlFlag.hasControlFlag(
                T8500VehicleControl.ControlFlag.DOOR_UNLOCK, 
                T8500VehicleControl.ControlFlag.DOOR_LOCK));
        
        // 测试车门加锁标志检查
        assertTrue(T8500VehicleControl.ControlFlag.hasControlFlag(
                T8500VehicleControl.ControlFlag.DOOR_LOCK, 
                T8500VehicleControl.ControlFlag.DOOR_LOCK));
        
        // 测试组合标志
        byte combinedFlag = (byte) (T8500VehicleControl.ControlFlag.DOOR_LOCK | 0x02);
        assertTrue(T8500VehicleControl.ControlFlag.hasControlFlag(
                combinedFlag, T8500VehicleControl.ControlFlag.DOOR_LOCK));
    }

    @Test
    void testControlFlagGetDescription() {
        assertEquals("车门解锁", T8500VehicleControl.ControlFlag.getControlFlagDescription(
                T8500VehicleControl.ControlFlag.DOOR_UNLOCK));
        assertEquals("车门加锁", T8500VehicleControl.ControlFlag.getControlFlagDescription(
                T8500VehicleControl.ControlFlag.DOOR_LOCK));
        
        // 测试带保留位的标志
        assertEquals("车门解锁", T8500VehicleControl.ControlFlag.getControlFlagDescription((byte) 0x02));
        assertEquals("车门加锁", T8500VehicleControl.ControlFlag.getControlFlagDescription((byte) 0x03));
    }

    @Test
    void testMessageFactoryIntegration() {
        JT808MessageFactory factory = JT808MessageFactory.getInstance();
        
        // 测试工厂是否支持T8500消息
        assertTrue(factory.isSupported(0x8500));
        
        // 测试工厂创建T8500消息
        JT808Message message = factory.createMessage(0x8500);
        assertNotNull(message);
        assertTrue(message instanceof T8500VehicleControl);
        assertEquals(0x8500, message.getMessageId());
    }

    @Test
    void testRealWorldScenarios() {
        // 场景1：远程解锁车门
        T8500VehicleControl remoteUnlock = T8500VehicleControl.createDoorUnlock();
        assertEquals("车门解锁", remoteUnlock.getControlDescription());
        assertTrue(remoteUnlock.isDoorUnlock());
        
        // 场景2：远程锁定车门
        T8500VehicleControl remoteLock = T8500VehicleControl.createDoorLock();
        assertEquals("车门加锁", remoteLock.getControlDescription());
        assertTrue(remoteLock.isDoorLock());
        
        // 场景3：编解码传输
        Buffer encoded = remoteUnlock.encodeBody();
        T8500VehicleControl received = new T8500VehicleControl();
        received.decodeBody(encoded);
        assertEquals(remoteUnlock.getControlFlag(), received.getControlFlag());
        assertEquals(remoteUnlock.getControlDescription(), received.getControlDescription());
    }

    @Test
    void testEdgeCases() {
        // 测试所有可能的字节值
        for (int i = 0; i <= 255; i++) {
            T8500VehicleControl message = new T8500VehicleControl((byte) i);
            
            // 验证位0的逻辑
            if ((i & 0x01) == 0) {
                assertTrue(message.isDoorUnlock());
                assertFalse(message.isDoorLock());
                assertEquals("车门解锁", message.getControlDescription());
            } else {
                assertFalse(message.isDoorUnlock());
                assertTrue(message.isDoorLock());
                assertEquals("车门加锁", message.getControlDescription());
            }
            
            // 验证无符号值转换
            assertEquals(i, message.getControlFlagUnsigned());
            
            // 验证编解码一致性
            Buffer encoded = message.encodeBody();
            T8500VehicleControl decoded = new T8500VehicleControl();
            decoded.decodeBody(encoded);
            assertEquals(message.getControlFlag(), decoded.getControlFlag());
        }
    }

    @Test
    void testGettersAndSetters() {
        T8500VehicleControl message = new T8500VehicleControl();
        
        // 测试初始值
        assertEquals(0, message.getControlFlag());
        assertEquals(0, message.getControlFlagUnsigned());
        
        // 测试设置车门加锁
        message.setControlFlag((byte) 0x01);
        assertEquals(1, message.getControlFlag());
        assertEquals(1, message.getControlFlagUnsigned());
        assertTrue(message.isDoorLock());
        
        // 测试设置车门解锁
        message.setControlFlag((byte) 0x00);
        assertEquals(0, message.getControlFlag());
        assertEquals(0, message.getControlFlagUnsigned());
        assertTrue(message.isDoorUnlock());
        
        // 测试设置负数（作为无符号值）
        message.setControlFlag((byte) 0xFF);
        assertEquals(-1, message.getControlFlag());
        assertEquals(255, message.getControlFlagUnsigned());
    }
}