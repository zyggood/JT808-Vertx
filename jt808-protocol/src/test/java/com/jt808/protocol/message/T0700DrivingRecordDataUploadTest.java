package com.jt808.protocol.message;

import io.vertx.core.buffer.Buffer;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * T0700DrivingRecordDataUpload 测试类
 */
class T0700DrivingRecordDataUploadTest {

    @Test
    void testMessageId() {
        T0700DrivingRecordDataUpload message = new T0700DrivingRecordDataUpload();
        assertEquals(0x0700, message.getMessageId());
    }

    @Test
    void testDefaultConstructor() {
        T0700DrivingRecordDataUpload message = new T0700DrivingRecordDataUpload();
        assertEquals(0, message.getResponseSerialNumber());
        assertEquals(0, message.getCommandWord());
        assertNotNull(message.getDataBlock());
        assertEquals(0, message.getDataBlock().length);
        assertFalse(message.hasDataBlock());
    }

    @Test
    void testConstructorWithoutDataBlock() {
        int responseSerialNumber = 12345;
        byte commandWord = (byte) 0x08;
        
        T0700DrivingRecordDataUpload message = new T0700DrivingRecordDataUpload(responseSerialNumber, commandWord);
        
        assertEquals(responseSerialNumber, message.getResponseSerialNumber());
        assertEquals(commandWord, message.getCommandWord());
        assertNotNull(message.getDataBlock());
        assertEquals(0, message.getDataBlock().length);
        assertFalse(message.hasDataBlock());
    }

    @Test
    void testConstructorWithDataBlock() {
        int responseSerialNumber = 54321;
        byte commandWord = (byte) 0x09;
        byte[] dataBlock = {0x01, 0x02, 0x03, 0x04};
        
        T0700DrivingRecordDataUpload message = new T0700DrivingRecordDataUpload(responseSerialNumber, commandWord, dataBlock);
        
        assertEquals(responseSerialNumber, message.getResponseSerialNumber());
        assertEquals(commandWord, message.getCommandWord());
        assertArrayEquals(dataBlock, message.getDataBlock());
        assertTrue(message.hasDataBlock());
        assertEquals(4, message.getDataBlockLength());
    }

    @Test
    void testConstructorWithNullDataBlock() {
        int responseSerialNumber = 11111;
        byte commandWord = (byte) 0x0A;
        
        T0700DrivingRecordDataUpload message = new T0700DrivingRecordDataUpload(responseSerialNumber, commandWord, null);
        
        assertEquals(responseSerialNumber, message.getResponseSerialNumber());
        assertEquals(commandWord, message.getCommandWord());
        assertNotNull(message.getDataBlock());
        assertEquals(0, message.getDataBlock().length);
        assertFalse(message.hasDataBlock());
    }

    @Test
    void testCreateWithoutDataBlock() {
        int responseSerialNumber = 98765;
        byte commandWord = (byte) 0x0B;
        
        T0700DrivingRecordDataUpload message = T0700DrivingRecordDataUpload.create(responseSerialNumber, commandWord);
        
        assertEquals(responseSerialNumber, message.getResponseSerialNumber());
        assertEquals(commandWord, message.getCommandWord());
        assertFalse(message.hasDataBlock());
    }

    @Test
    void testCreateWithDataBlock() {
        int responseSerialNumber = 13579;
        byte commandWord = (byte) 0x0C;
        byte[] dataBlock = {(byte) 0xAA, (byte) 0xBB, (byte) 0xCC};
        
        T0700DrivingRecordDataUpload message = T0700DrivingRecordDataUpload.create(responseSerialNumber, commandWord, dataBlock);
        
        assertEquals(responseSerialNumber, message.getResponseSerialNumber());
        assertEquals(commandWord, message.getCommandWord());
        assertArrayEquals(dataBlock, message.getDataBlock());
        assertTrue(message.hasDataBlock());
    }

    @Test
    void testCreateDriverIdentityRecord() {
        int responseSerialNumber = 24680;
        byte[] dataBlock = {0x11, 0x22, 0x33};
        
        T0700DrivingRecordDataUpload message = T0700DrivingRecordDataUpload.createDriverIdentityRecord(responseSerialNumber, dataBlock);
        
        assertEquals(responseSerialNumber, message.getResponseSerialNumber());
        assertEquals((byte) 0x08, message.getCommandWord());
        assertArrayEquals(dataBlock, message.getDataBlock());
        assertEquals("驾驶员身份记录", message.getCommandDescription());
    }

    @Test
    void testCreateRealTimeData() {
        int responseSerialNumber = 13579;
        byte[] dataBlock = {0x44, 0x55, 0x66};
        
        T0700DrivingRecordDataUpload message = T0700DrivingRecordDataUpload.createRealTimeData(responseSerialNumber, dataBlock);
        
        assertEquals(responseSerialNumber, message.getResponseSerialNumber());
        assertEquals((byte) 0x09, message.getCommandWord());
        assertArrayEquals(dataBlock, message.getDataBlock());
        assertEquals("实时时间", message.getCommandDescription());
    }

    @Test
    void testCreateMileageData() {
        int responseSerialNumber = 97531;
        byte[] dataBlock = {0x77, (byte) 0x88, (byte) 0x99};
        
        T0700DrivingRecordDataUpload message = T0700DrivingRecordDataUpload.createMileageData(responseSerialNumber, dataBlock);
        
        assertEquals(responseSerialNumber, message.getResponseSerialNumber());
        assertEquals((byte) 0x0A, message.getCommandWord());
        assertArrayEquals(dataBlock, message.getDataBlock());
        assertEquals("累计行驶里程", message.getCommandDescription());
    }

    @Test
    void testCreatePulseCoefficientData() {
        int responseSerialNumber = 86420;
        byte[] dataBlock = {(byte) 0xAA, (byte) 0xBB};
        
        T0700DrivingRecordDataUpload message = T0700DrivingRecordDataUpload.createPulseCoefficientData(responseSerialNumber, dataBlock);
        
        assertEquals(responseSerialNumber, message.getResponseSerialNumber());
        assertEquals((byte) 0x0B, message.getCommandWord());
        assertArrayEquals(dataBlock, message.getDataBlock());
        assertEquals("脉冲系数", message.getCommandDescription());
    }

    @Test
    void testCreateVehicleInfoData() {
        int responseSerialNumber = 19283;
        byte[] dataBlock = {(byte) 0xCC, (byte) 0xDD, (byte) 0xEE, (byte) 0xFF};
        
        T0700DrivingRecordDataUpload message = T0700DrivingRecordDataUpload.createVehicleInfoData(responseSerialNumber, dataBlock);
        
        assertEquals(responseSerialNumber, message.getResponseSerialNumber());
        assertEquals((byte) 0x0C, message.getCommandWord());
        assertArrayEquals(dataBlock, message.getDataBlock());
        assertEquals("车辆信息", message.getCommandDescription());
    }

    @Test
    void testEncodeBodyWithoutDataBlock() {
        int responseSerialNumber = 0x1234;
        byte commandWord = (byte) 0x08;
        
        T0700DrivingRecordDataUpload message = new T0700DrivingRecordDataUpload(responseSerialNumber, commandWord);
        Buffer buffer = message.encodeBody();
        
        assertEquals(3, buffer.length());
        assertEquals(0x1234, buffer.getUnsignedShort(0));
        assertEquals(commandWord, buffer.getByte(2));
    }

    @Test
    void testEncodeBodyWithDataBlock() {
        int responseSerialNumber = 0x5678;
        byte commandWord = (byte) 0x09;
        byte[] dataBlock = {(byte) 0xAA, (byte) 0xBB, (byte) 0xCC, (byte) 0xDD};
        
        T0700DrivingRecordDataUpload message = new T0700DrivingRecordDataUpload(responseSerialNumber, commandWord, dataBlock);
        Buffer buffer = message.encodeBody();
        
        assertEquals(7, buffer.length());
        assertEquals(0x5678, buffer.getUnsignedShort(0));
        assertEquals(commandWord, buffer.getByte(2));
        assertEquals((byte) 0xAA, buffer.getByte(3));
        assertEquals((byte) 0xBB, buffer.getByte(4));
        assertEquals((byte) 0xCC, buffer.getByte(5));
        assertEquals((byte) 0xDD, buffer.getByte(6));
    }

    @Test
    void testDecodeBodyWithoutDataBlock() {
        Buffer buffer = Buffer.buffer();
        buffer.appendUnsignedShort(0x9ABC);
        buffer.appendByte((byte) 0x0A);
        
        T0700DrivingRecordDataUpload message = new T0700DrivingRecordDataUpload();
        message.decodeBody(buffer);
        
        assertEquals(0x9ABC, message.getResponseSerialNumber());
        assertEquals((byte) 0x0A, message.getCommandWord());
        assertFalse(message.hasDataBlock());
        assertEquals(0, message.getDataBlockLength());
    }

    @Test
    void testDecodeBodyWithDataBlock() {
        Buffer buffer = Buffer.buffer();
        buffer.appendUnsignedShort(0xDEF0);
        buffer.appendByte((byte) 0x0B);
        buffer.appendBytes(new byte[]{0x11, 0x22, 0x33, 0x44, 0x55});
        
        T0700DrivingRecordDataUpload message = new T0700DrivingRecordDataUpload();
        message.decodeBody(buffer);
        
        assertEquals(0xDEF0, message.getResponseSerialNumber());
        assertEquals((byte) 0x0B, message.getCommandWord());
        assertTrue(message.hasDataBlock());
        assertEquals(5, message.getDataBlockLength());
        assertArrayEquals(new byte[]{0x11, 0x22, 0x33, 0x44, 0x55}, message.getDataBlock());
    }

    @Test
    void testDecodeBodyInvalidLength() {
        Buffer buffer = Buffer.buffer();
        buffer.appendByte((byte) 0x01);
        buffer.appendByte((byte) 0x02);
        
        T0700DrivingRecordDataUpload message = new T0700DrivingRecordDataUpload();
        
        assertThrows(IllegalArgumentException.class, () -> {
            message.decodeBody(buffer);
        });
    }

    @Test
    void testDecodeBodyNullBuffer() {
        T0700DrivingRecordDataUpload message = new T0700DrivingRecordDataUpload();
        
        assertThrows(IllegalArgumentException.class, () -> {
            message.decodeBody(null);
        });
    }

    @Test
    void testGettersAndSetters() {
        T0700DrivingRecordDataUpload message = new T0700DrivingRecordDataUpload();
        
        // 测试应答流水号
        message.setResponseSerialNumber(12345);
        assertEquals(12345, message.getResponseSerialNumber());
        
        // 测试命令字
        message.setCommandWord((byte) 0x0C);
        assertEquals((byte) 0x0C, message.getCommandWord());
        
        // 测试数据块
        byte[] dataBlock = {0x01, 0x02, 0x03};
        message.setDataBlock(dataBlock);
        assertArrayEquals(dataBlock, message.getDataBlock());
        
        // 测试设置null数据块
        message.setDataBlock(null);
        assertNotNull(message.getDataBlock());
        assertEquals(0, message.getDataBlock().length);
    }

    @Test
    void testCommandWordConstants() {
        assertEquals((byte) 0x08, T0700DrivingRecordDataUpload.CommandWord.DRIVER_IDENTITY_RECORD);
        assertEquals((byte) 0x09, T0700DrivingRecordDataUpload.CommandWord.REAL_TIME_DATA);
        assertEquals((byte) 0x0A, T0700DrivingRecordDataUpload.CommandWord.MILEAGE_DATA);
        assertEquals((byte) 0x0B, T0700DrivingRecordDataUpload.CommandWord.PULSE_COEFFICIENT_DATA);
        assertEquals((byte) 0x0C, T0700DrivingRecordDataUpload.CommandWord.VEHICLE_INFO_DATA);
    }

    @Test
    void testGetResponseSerialNumberUnsigned() {
        T0700DrivingRecordDataUpload message = new T0700DrivingRecordDataUpload();
        message.setResponseSerialNumber(-1);
        assertEquals(4294967295L, message.getResponseSerialNumberUnsigned());
    }

    @Test
    void testGetCommandWordUnsigned() {
        T0700DrivingRecordDataUpload message = new T0700DrivingRecordDataUpload();
        message.setCommandWord((byte) 0xFF);
        assertEquals(255, message.getCommandWordUnsigned());
    }

    @Test
    void testGetMessageDescription() {
        T0700DrivingRecordDataUpload message = new T0700DrivingRecordDataUpload();
        assertEquals("行驶记录数据上传", message.getMessageDescription());
    }

    @Test
    void testGetCommandDescription() {
        T0700DrivingRecordDataUpload message = new T0700DrivingRecordDataUpload();
        
        message.setCommandWord((byte) 0x08);
        assertEquals("驾驶员身份记录", message.getCommandDescription());
        
        message.setCommandWord((byte) 0x09);
        assertEquals("实时时间", message.getCommandDescription());
        
        message.setCommandWord((byte) 0x0A);
        assertEquals("累计行驶里程", message.getCommandDescription());
        
        message.setCommandWord((byte) 0x0B);
        assertEquals("脉冲系数", message.getCommandDescription());
        
        message.setCommandWord((byte) 0x0C);
        assertEquals("车辆信息", message.getCommandDescription());
        
        message.setCommandWord((byte) 0xFF);
        assertEquals("未知命令字: 0xFF", message.getCommandDescription());
    }

    @Test
    void testBoundaryValues() {
        // 测试最大应答流水号
        T0700DrivingRecordDataUpload message = new T0700DrivingRecordDataUpload();
        message.setResponseSerialNumber(65535);
        assertEquals(65535, message.getResponseSerialNumber());
        
        // 测试最大命令字
        message.setCommandWord((byte) 0xFF);
        assertEquals((byte) 0xFF, message.getCommandWord());
        
        // 测试空数据块
        message.setDataBlock(new byte[0]);
        assertEquals(0, message.getDataBlockLength());
        assertFalse(message.hasDataBlock());
    }

    @Test
    void testRealScenario() {
        // 模拟真实场景：驾驶员身份记录数据上传
        int responseSerialNumber = 1001;
        byte[] driverData = {
            0x01, 0x23, 0x45, 0x67, (byte) 0x89, (byte) 0xAB, (byte) 0xCD, (byte) 0xEF,  // 驾驶员身份证号
            0x20, 0x23, 0x12, 0x25, 0x10, 0x30, 0x00,  // 时间戳
            0x01  // 状态
        };
        
        T0700DrivingRecordDataUpload message = T0700DrivingRecordDataUpload.createDriverIdentityRecord(responseSerialNumber, driverData);
        
        // 编码
        Buffer encoded = message.encodeBody();
        
        // 解码
        T0700DrivingRecordDataUpload decoded = new T0700DrivingRecordDataUpload();
        decoded.decodeBody(encoded);
        
        // 验证
        assertEquals(message.getResponseSerialNumber(), decoded.getResponseSerialNumber());
        assertEquals(message.getCommandWord(), decoded.getCommandWord());
        assertArrayEquals(message.getDataBlock(), decoded.getDataBlock());
        assertEquals("驾驶员身份记录", decoded.getCommandDescription());
    }

    @Test
    void testToString() {
        T0700DrivingRecordDataUpload message = new T0700DrivingRecordDataUpload(12345, (byte) 0x08, new byte[]{0x01, 0x02});
        String result = message.toString();
        
        assertTrue(result.contains("responseSerialNumber=12345"));
        assertTrue(result.contains("commandWord=0x08"));
        assertTrue(result.contains("dataBlockLength=2"));
        assertTrue(result.contains("commandDescription='驾驶员身份记录'"));
    }

    @Test
    void testEquals() {
        T0700DrivingRecordDataUpload message1 = new T0700DrivingRecordDataUpload(123, (byte) 0x08, new byte[]{0x01, 0x02});
        T0700DrivingRecordDataUpload message2 = new T0700DrivingRecordDataUpload(123, (byte) 0x08, new byte[]{0x01, 0x02});
        T0700DrivingRecordDataUpload message3 = new T0700DrivingRecordDataUpload(456, (byte) 0x09, new byte[]{0x03, 0x04});
        
        assertEquals(message1, message2);
        assertNotEquals(message1, message3);
        assertNotEquals(message1, null);
        assertNotEquals(message1, "string");
    }

    @Test
    void testHashCode() {
        T0700DrivingRecordDataUpload message1 = new T0700DrivingRecordDataUpload(123, (byte) 0x08, new byte[]{0x01, 0x02});
        T0700DrivingRecordDataUpload message2 = new T0700DrivingRecordDataUpload(123, (byte) 0x08, new byte[]{0x01, 0x02});
        
        assertEquals(message1.hashCode(), message2.hashCode());
    }
}