package com.jt808.protocol.message;

import io.vertx.core.buffer.Buffer;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * T8701DrivingRecordParameterTransmission 测试类
 */
class T8701DrivingRecordParameterTransmissionTest {

    @Test
    void testMessageId() {
        T8701DrivingRecordParameterTransmission message = new T8701DrivingRecordParameterTransmission();
        assertEquals(0x8701, message.getMessageId());
    }

    @Test
    void testDefaultConstructor() {
        T8701DrivingRecordParameterTransmission message = new T8701DrivingRecordParameterTransmission();
        assertEquals(0, message.getCommandWord());
        assertNull(message.getDataBlock());
        assertFalse(message.hasDataBlock());
        assertEquals(0, message.getDataBlockLength());
    }

    @Test
    void testConstructorWithParameters() {
        byte commandWord = (byte) 0x82;
        Buffer dataBlock = Buffer.buffer(new byte[]{0x01, 0x02, 0x03});
        
        T8701DrivingRecordParameterTransmission message = new T8701DrivingRecordParameterTransmission(commandWord, dataBlock);
        
        assertEquals(commandWord, message.getCommandWord());
        assertEquals(dataBlock, message.getDataBlock());
        assertTrue(message.hasDataBlock());
        assertEquals(3, message.getDataBlockLength());
    }

    @Test
    void testConstructorWithNullDataBlock() {
        byte commandWord = (byte) 0x83;
        
        T8701DrivingRecordParameterTransmission message = new T8701DrivingRecordParameterTransmission(commandWord, null);
        
        assertEquals(commandWord, message.getCommandWord());
        assertNull(message.getDataBlock());
        assertFalse(message.hasDataBlock());
        assertEquals(0, message.getDataBlockLength());
    }

    @Test
    void testCreateCommandOnly() {
        byte commandWord = (byte) 0x84;
        
        T8701DrivingRecordParameterTransmission message = T8701DrivingRecordParameterTransmission.createCommandOnly(commandWord);
        
        assertEquals(commandWord, message.getCommandWord());
        assertNull(message.getDataBlock());
        assertFalse(message.hasDataBlock());
    }

    @Test
    void testCreateWithDataBlockBuffer() {
        byte commandWord = (byte) 0x85;
        Buffer dataBlock = Buffer.buffer(new byte[]{(byte) 0xAA, (byte) 0xBB, (byte) 0xCC});
        
        T8701DrivingRecordParameterTransmission message = T8701DrivingRecordParameterTransmission.createWithDataBlock(commandWord, dataBlock);
        
        assertEquals(commandWord, message.getCommandWord());
        assertEquals(dataBlock, message.getDataBlock());
        assertTrue(message.hasDataBlock());
    }

    @Test
    void testCreateWithDataBlockBytes() {
        byte commandWord = (byte) 0x86;
        byte[] dataBytes = {0x11, 0x22, 0x33, 0x44};
        
        T8701DrivingRecordParameterTransmission message = T8701DrivingRecordParameterTransmission.createWithDataBlock(commandWord, dataBytes);
        
        assertEquals(commandWord, message.getCommandWord());
        assertArrayEquals(dataBytes, message.getDataBlockBytes());
        assertTrue(message.hasDataBlock());
        assertEquals(4, message.getDataBlockLength());
    }

    @Test
    void testCreateWithDataBlockEmptyBytes() {
        byte commandWord = (byte) 0x87;
        byte[] dataBytes = new byte[0];
        
        T8701DrivingRecordParameterTransmission message = T8701DrivingRecordParameterTransmission.createWithDataBlock(commandWord, dataBytes);
        
        assertEquals(commandWord, message.getCommandWord());
        assertNull(message.getDataBlock());
        assertFalse(message.hasDataBlock());
    }

    @Test
    void testCreateWithDataBlockNullBytes() {
        byte commandWord = (byte) 0x88;
        
        T8701DrivingRecordParameterTransmission message = T8701DrivingRecordParameterTransmission.createWithDataBlock(commandWord, (byte[]) null);
        
        assertEquals(commandWord, message.getCommandWord());
        assertNull(message.getDataBlock());
        assertFalse(message.hasDataBlock());
    }

    @Test
    void testCreateSetVehicleInfo() {
        byte[] dataBlock = {0x01, 0x02, 0x03, 0x04, 0x05};
        
        T8701DrivingRecordParameterTransmission message = T8701DrivingRecordParameterTransmission.createSetVehicleInfo(dataBlock);
        
        assertEquals((byte) 0x82, message.getCommandWord());
        assertArrayEquals(dataBlock, message.getDataBlockBytes());
        assertEquals("设置车辆信息", message.getCommandDescription());
    }

    @Test
    void testCreateSetInitialMileage() {
        byte[] dataBlock = {0x10, 0x20, 0x30};
        
        T8701DrivingRecordParameterTransmission message = T8701DrivingRecordParameterTransmission.createSetInitialMileage(dataBlock);
        
        assertEquals((byte) 0x83, message.getCommandWord());
        assertArrayEquals(dataBlock, message.getDataBlockBytes());
        assertEquals("设置初始里程", message.getCommandDescription());
    }

    @Test
    void testCreateSetTime() {
        byte[] dataBlock = {0x20, 0x23, 0x12, 0x25, 0x10, 0x30, 0x00};
        
        T8701DrivingRecordParameterTransmission message = T8701DrivingRecordParameterTransmission.createSetTime(dataBlock);
        
        assertEquals((byte) 0x84, message.getCommandWord());
        assertArrayEquals(dataBlock, message.getDataBlockBytes());
        assertEquals("设置时间", message.getCommandDescription());
    }

    @Test
    void testCreateSetPulseCoefficient() {
        byte[] dataBlock = {(byte) 0xFF, (byte) 0xEE};
        
        T8701DrivingRecordParameterTransmission message = T8701DrivingRecordParameterTransmission.createSetPulseCoefficient(dataBlock);
        
        assertEquals((byte) 0x85, message.getCommandWord());
        assertArrayEquals(dataBlock, message.getDataBlockBytes());
        assertEquals("设置脉冲系数", message.getCommandDescription());
    }

    @Test
    void testEncodeBodyWithoutDataBlock() {
        byte commandWord = (byte) 0x82;
        
        T8701DrivingRecordParameterTransmission message = new T8701DrivingRecordParameterTransmission(commandWord, null);
        Buffer buffer = message.encodeBody();
        
        assertEquals(1, buffer.length());
        assertEquals(commandWord, buffer.getByte(0));
    }

    @Test
    void testEncodeBodyWithDataBlock() {
        byte commandWord = (byte) 0x83;
        byte[] dataBytes = {0x11, 0x22, 0x33};
        Buffer dataBlock = Buffer.buffer(dataBytes);
        
        T8701DrivingRecordParameterTransmission message = new T8701DrivingRecordParameterTransmission(commandWord, dataBlock);
        Buffer buffer = message.encodeBody();
        
        assertEquals(4, buffer.length());
        assertEquals(commandWord, buffer.getByte(0));
        assertEquals(0x11, buffer.getByte(1));
        assertEquals(0x22, buffer.getByte(2));
        assertEquals(0x33, buffer.getByte(3));
    }

    @Test
    void testDecodeBodyWithoutDataBlock() {
        Buffer buffer = Buffer.buffer();
        buffer.appendByte((byte) 0x84);
        
        T8701DrivingRecordParameterTransmission message = new T8701DrivingRecordParameterTransmission();
        message.decodeBody(buffer);
        
        assertEquals((byte) 0x84, message.getCommandWord());
        assertNull(message.getDataBlock());
        assertFalse(message.hasDataBlock());
    }

    @Test
    void testDecodeBodyWithDataBlock() {
        Buffer buffer = Buffer.buffer();
        buffer.appendByte((byte) 0x85);
        buffer.appendBytes(new byte[]{(byte) 0xAA, (byte) 0xBB, (byte) 0xCC, (byte) 0xDD});
        
        T8701DrivingRecordParameterTransmission message = new T8701DrivingRecordParameterTransmission();
        message.decodeBody(buffer);
        
        assertEquals((byte) 0x85, message.getCommandWord());
        assertTrue(message.hasDataBlock());
        assertEquals(4, message.getDataBlockLength());
        assertArrayEquals(new byte[]{(byte) 0xAA, (byte) 0xBB, (byte) 0xCC, (byte) 0xDD}, message.getDataBlockBytes());
    }

    @Test
    void testDecodeBodyInvalidLength() {
        Buffer buffer = Buffer.buffer();
        
        T8701DrivingRecordParameterTransmission message = new T8701DrivingRecordParameterTransmission();
        
        assertThrows(IllegalArgumentException.class, () -> {
            message.decodeBody(buffer);
        });
    }

    @Test
    void testDecodeBodyNullBuffer() {
        T8701DrivingRecordParameterTransmission message = new T8701DrivingRecordParameterTransmission();
        
        assertThrows(IllegalArgumentException.class, () -> {
            message.decodeBody(null);
        });
    }

    @Test
    void testGettersAndSetters() {
        T8701DrivingRecordParameterTransmission message = new T8701DrivingRecordParameterTransmission();
        
        // 测试命令字
        message.setCommandWord((byte) 0x86);
        assertEquals((byte) 0x86, message.getCommandWord());
        
        // 测试数据块
        Buffer dataBlock = Buffer.buffer(new byte[]{0x01, 0x02, 0x03});
        message.setDataBlock(dataBlock);
        assertEquals(dataBlock, message.getDataBlock());
        
        // 测试数据块字节数组
        byte[] dataBytes = {0x04, 0x05, 0x06};
        message.setDataBlockBytes(dataBytes);
        assertArrayEquals(dataBytes, message.getDataBlockBytes());
        
        // 测试设置null数据块字节数组
        message.setDataBlockBytes(null);
        assertNull(message.getDataBlock());
        
        // 测试设置空数据块字节数组
        message.setDataBlockBytes(new byte[0]);
        assertNull(message.getDataBlock());
    }

    @Test
    void testCommandWordConstants() {
        assertEquals((byte) 0x82, T8701DrivingRecordParameterTransmission.CommandWord.SET_VEHICLE_INFO);
        assertEquals((byte) 0x83, T8701DrivingRecordParameterTransmission.CommandWord.SET_INITIAL_MILEAGE);
        assertEquals((byte) 0x84, T8701DrivingRecordParameterTransmission.CommandWord.SET_TIME);
        assertEquals((byte) 0x85, T8701DrivingRecordParameterTransmission.CommandWord.SET_PULSE_COEFFICIENT);
        assertEquals((byte) 0x86, T8701DrivingRecordParameterTransmission.CommandWord.SET_VEHICLE_FEATURE_COEFFICIENT);
        assertEquals((byte) 0x87, T8701DrivingRecordParameterTransmission.CommandWord.SET_LICENSE_PLATE);
        assertEquals((byte) 0x88, T8701DrivingRecordParameterTransmission.CommandWord.SET_VEHICLE_TYPE);
    }

    @Test
    void testGetCommandWordUnsigned() {
        T8701DrivingRecordParameterTransmission message = new T8701DrivingRecordParameterTransmission();
        message.setCommandWord((byte) 0xFF);
        assertEquals(255, message.getCommandWordUnsigned());
    }

    @Test
    void testGetMessageDescription() {
        T8701DrivingRecordParameterTransmission message = new T8701DrivingRecordParameterTransmission();
        assertEquals("行驶记录参数下传命令", message.getMessageDescription());
    }

    @Test
    void testGetCommandDescription() {
        T8701DrivingRecordParameterTransmission message = new T8701DrivingRecordParameterTransmission();
        
        message.setCommandWord((byte) 0x82);
        assertEquals("设置车辆信息", message.getCommandDescription());
        
        message.setCommandWord((byte) 0x83);
        assertEquals("设置初始里程", message.getCommandDescription());
        
        message.setCommandWord((byte) 0x84);
        assertEquals("设置时间", message.getCommandDescription());
        
        message.setCommandWord((byte) 0x85);
        assertEquals("设置脉冲系数", message.getCommandDescription());
        
        message.setCommandWord((byte) 0x86);
        assertEquals("设置车辆特征系数", message.getCommandDescription());
        
        message.setCommandWord((byte) 0x87);
        assertEquals("设置车牌号", message.getCommandDescription());
        
        message.setCommandWord((byte) 0x88);
        assertEquals("设置车辆类型", message.getCommandDescription());
        
        message.setCommandWord((byte) 0xFF);
        assertEquals("未知命令字: 0xFF", message.getCommandDescription());
    }

    @Test
    void testBoundaryValues() {
        // 测试最大命令字
        T8701DrivingRecordParameterTransmission message = new T8701DrivingRecordParameterTransmission();
        message.setCommandWord((byte) 0xFF);
        assertEquals((byte) 0xFF, message.getCommandWord());
        
        // 测试空数据块
        message.setDataBlock(Buffer.buffer());
        assertEquals(0, message.getDataBlockLength());
        assertFalse(message.hasDataBlock());
    }

    @Test
    void testRealScenario() {
        // 模拟真实场景：设置车辆信息参数
        byte[] vehicleInfoData = {
            0x01, 0x23, 0x45, 0x67, (byte) 0x89, (byte) 0xAB, (byte) 0xCD, (byte) 0xEF,  // 车辆识别号
            0x20, 0x23, 0x12, 0x25,  // 车牌号
            0x01,  // 车辆类型
            0x02   // 其他参数
        };
        
        T8701DrivingRecordParameterTransmission message = T8701DrivingRecordParameterTransmission.createSetVehicleInfo(vehicleInfoData);
        
        // 编码
        Buffer encoded = message.encodeBody();
        
        // 解码
        T8701DrivingRecordParameterTransmission decoded = new T8701DrivingRecordParameterTransmission();
        decoded.decodeBody(encoded);
        
        // 验证
        assertEquals(message.getCommandWord(), decoded.getCommandWord());
        assertArrayEquals(message.getDataBlockBytes(), decoded.getDataBlockBytes());
        assertEquals("设置车辆信息", decoded.getCommandDescription());
    }

    @Test
    void testToString() {
        T8701DrivingRecordParameterTransmission message = new T8701DrivingRecordParameterTransmission((byte) 0x82, Buffer.buffer(new byte[]{0x01, 0x02}));
        String result = message.toString();
        
        assertTrue(result.contains("commandWord=0x82"));
        assertTrue(result.contains("dataBlockLength=2"));
        assertTrue(result.contains("hasDataBlock=true"));
        assertTrue(result.contains("commandDescription='设置车辆信息'"));
    }

    @Test
    void testEquals() {
        T8701DrivingRecordParameterTransmission message1 = new T8701DrivingRecordParameterTransmission((byte) 0x82, Buffer.buffer(new byte[]{0x01, 0x02}));
        T8701DrivingRecordParameterTransmission message2 = new T8701DrivingRecordParameterTransmission((byte) 0x82, Buffer.buffer(new byte[]{0x01, 0x02}));
        T8701DrivingRecordParameterTransmission message3 = new T8701DrivingRecordParameterTransmission((byte) 0x83, Buffer.buffer(new byte[]{0x03, 0x04}));
        
        assertEquals(message1, message2);
        assertNotEquals(message1, message3);
        assertNotEquals(message1, null);
        assertNotEquals(message1, "string");
    }

    @Test
    void testHashCode() {
        T8701DrivingRecordParameterTransmission message1 = new T8701DrivingRecordParameterTransmission((byte) 0x82, Buffer.buffer(new byte[]{0x01, 0x02}));
        T8701DrivingRecordParameterTransmission message2 = new T8701DrivingRecordParameterTransmission((byte) 0x82, Buffer.buffer(new byte[]{0x01, 0x02}));
        
        assertEquals(message1.hashCode(), message2.hashCode());
    }

    @Test
    void testEncodeDecodeRoundTrip() {
        // 测试编码解码往返
        byte commandWord = (byte) 0x84;
        byte[] originalData = {0x20, 0x23, 0x12, 0x25, 0x10, 0x30, 0x00};
        
        T8701DrivingRecordParameterTransmission original = T8701DrivingRecordParameterTransmission.createWithDataBlock(commandWord, originalData);
        
        // 编码
        Buffer encoded = original.encodeBody();
        
        // 解码
        T8701DrivingRecordParameterTransmission decoded = new T8701DrivingRecordParameterTransmission();
        decoded.decodeBody(encoded);
        
        // 验证
        assertEquals(original.getCommandWord(), decoded.getCommandWord());
        assertArrayEquals(original.getDataBlockBytes(), decoded.getDataBlockBytes());
        assertEquals(original.hasDataBlock(), decoded.hasDataBlock());
        assertEquals(original.getDataBlockLength(), decoded.getDataBlockLength());
    }
}