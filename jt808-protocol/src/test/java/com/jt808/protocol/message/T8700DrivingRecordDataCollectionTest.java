package com.jt808.protocol.message;

import io.vertx.core.buffer.Buffer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * T8700 行驶记录数据采集命令消息测试类
 */
@DisplayName("T8700 行驶记录数据采集命令消息测试")
class T8700DrivingRecordDataCollectionTest {

    @Test
    @DisplayName("测试消息ID")
    void testMessageId() {
        T8700DrivingRecordDataCollection message = new T8700DrivingRecordDataCollection();
        assertEquals(0x8700, message.getMessageId());
    }

    @Test
    @DisplayName("测试默认构造函数")
    void testDefaultConstructor() {
        T8700DrivingRecordDataCollection message = new T8700DrivingRecordDataCollection();
        assertNotNull(message);
        assertEquals(0x8700, message.getMessageId());
        assertEquals(0, message.getCommandWord());
        assertNull(message.getDataBlock());
        assertFalse(message.hasDataBlock());
        assertEquals(0, message.getDataBlockLength());
    }

    @Test
    @DisplayName("测试带参数构造函数")
    void testParameterizedConstructor() {
        byte commandWord = (byte) 0x01;
        Buffer dataBlock = Buffer.buffer(new byte[]{0x12, 0x34, 0x56});
        
        T8700DrivingRecordDataCollection message = new T8700DrivingRecordDataCollection(commandWord, dataBlock);
        
        assertEquals(commandWord, message.getCommandWord());
        assertEquals(dataBlock, message.getDataBlock());
        assertTrue(message.hasDataBlock());
        assertEquals(3, message.getDataBlockLength());
    }

    @Test
    @DisplayName("测试创建只有命令字的消息")
    void testCreateCommandOnly() {
        byte commandWord = T8700DrivingRecordDataCollection.CommandWords.READ_BASIC_INFO;
        
        T8700DrivingRecordDataCollection message = T8700DrivingRecordDataCollection.createCommandOnly(commandWord);
        
        assertEquals(commandWord, message.getCommandWord());
        assertNull(message.getDataBlock());
        assertFalse(message.hasDataBlock());
        assertEquals(0, message.getDataBlockLength());
    }

    @Test
    @DisplayName("测试创建带数据块的消息")
    void testCreateWithDataBlock() {
        byte commandWord = T8700DrivingRecordDataCollection.CommandWords.SET_TIME;
        Buffer dataBlock = Buffer.buffer(new byte[]{0x20, 0x23, 0x12, 0x25, 0x10, 0x30, 0x00});
        
        T8700DrivingRecordDataCollection message = T8700DrivingRecordDataCollection.createWithDataBlock(commandWord, dataBlock);
        
        assertEquals(commandWord, message.getCommandWord());
        assertEquals(dataBlock, message.getDataBlock());
        assertTrue(message.hasDataBlock());
        assertEquals(7, message.getDataBlockLength());
    }

    @Test
    @DisplayName("测试创建带数据块的消息（字节数组形式）")
    void testCreateWithDataBlockBytes() {
        byte commandWord = T8700DrivingRecordDataCollection.CommandWords.READ_VEHICLE_INFO;
        byte[] dataBytes = {0x01, 0x02, 0x03, 0x04, 0x05};
        
        T8700DrivingRecordDataCollection message = T8700DrivingRecordDataCollection.createWithDataBlock(commandWord, dataBytes);
        
        assertEquals(commandWord, message.getCommandWord());
        assertArrayEquals(dataBytes, message.getDataBlockBytes());
        assertTrue(message.hasDataBlock());
        assertEquals(5, message.getDataBlockLength());
    }

    @Test
    @DisplayName("测试创建带空数据块的消息")
    void testCreateWithEmptyDataBlock() {
        byte commandWord = T8700DrivingRecordDataCollection.CommandWords.READ_TIME;
        
        // 测试null数据块
        T8700DrivingRecordDataCollection message1 = T8700DrivingRecordDataCollection.createWithDataBlock(commandWord, (Buffer) null);
        assertEquals(commandWord, message1.getCommandWord());
        assertNull(message1.getDataBlock());
        assertFalse(message1.hasDataBlock());
        
        // 测试空字节数组
        T8700DrivingRecordDataCollection message2 = T8700DrivingRecordDataCollection.createWithDataBlock(commandWord, new byte[0]);
        assertEquals(commandWord, message2.getCommandWord());
        assertNull(message2.getDataBlock());
        assertFalse(message2.hasDataBlock());
        
        // 测试null字节数组
        T8700DrivingRecordDataCollection message3 = T8700DrivingRecordDataCollection.createWithDataBlock(commandWord, (byte[]) null);
        assertEquals(commandWord, message3.getCommandWord());
        assertNull(message3.getDataBlock());
        assertFalse(message3.hasDataBlock());
    }

    @Test
    @DisplayName("测试消息体编码 - 只有命令字")
    void testEncodeBodyCommandOnly() {
        byte commandWord = (byte) 0x05;
        T8700DrivingRecordDataCollection message = T8700DrivingRecordDataCollection.createCommandOnly(commandWord);
        
        Buffer encoded = message.encodeBody();
        
        assertEquals(1, encoded.length());
        assertEquals(commandWord, encoded.getByte(0));
    }

    @Test
    @DisplayName("测试消息体编码 - 带数据块")
    void testEncodeBodyWithDataBlock() {
        byte commandWord = (byte) 0x07;
        byte[] dataBytes = {0x11, 0x22, 0x33, 0x44};
        T8700DrivingRecordDataCollection message = T8700DrivingRecordDataCollection.createWithDataBlock(commandWord, dataBytes);
        
        Buffer encoded = message.encodeBody();
        
        assertEquals(5, encoded.length());
        assertEquals(commandWord, encoded.getByte(0));
        assertArrayEquals(dataBytes, encoded.getBytes(1, 5));
    }

    @Test
    @DisplayName("测试消息体解码 - 只有命令字")
    void testDecodeBodyCommandOnly() {
        byte commandWord = (byte) 0x03;
        Buffer body = Buffer.buffer().appendByte(commandWord);
        
        T8700DrivingRecordDataCollection message = new T8700DrivingRecordDataCollection();
        message.decodeBody(body);
        
        assertEquals(commandWord, message.getCommandWord());
        assertNull(message.getDataBlock());
        assertFalse(message.hasDataBlock());
    }

    @Test
    @DisplayName("测试消息体解码 - 带数据块")
    void testDecodeBodyWithDataBlock() {
        byte commandWord = (byte) 0x08;
        byte[] dataBytes = {0x55, 0x66, 0x77, (byte) 0x88, (byte) 0x99};
        Buffer body = Buffer.buffer().appendByte(commandWord).appendBytes(dataBytes);
        
        T8700DrivingRecordDataCollection message = new T8700DrivingRecordDataCollection();
        message.decodeBody(body);
        
        assertEquals(commandWord, message.getCommandWord());
        assertArrayEquals(dataBytes, message.getDataBlockBytes());
        assertTrue(message.hasDataBlock());
        assertEquals(5, message.getDataBlockLength());
    }

    @Test
    @DisplayName("测试消息体解码 - 空消息体")
    void testDecodeBodyEmpty() {
        Buffer body = Buffer.buffer();
        
        T8700DrivingRecordDataCollection message = new T8700DrivingRecordDataCollection();
        message.decodeBody(body);
        
        assertEquals(0, message.getCommandWord());
        assertNull(message.getDataBlock());
        assertFalse(message.hasDataBlock());
    }

    @Test
    @DisplayName("测试编码解码往返")
    void testEncodeDecodeRoundTrip() {
        byte commandWord = T8700DrivingRecordDataCollection.CommandWords.READ_DRIVING_STATUS;
        byte[] originalData = {0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08};
        
        // 创建原始消息
        T8700DrivingRecordDataCollection originalMessage = T8700DrivingRecordDataCollection.createWithDataBlock(commandWord, originalData);
        
        // 编码
        Buffer encoded = originalMessage.encodeBody();
        
        // 解码
        T8700DrivingRecordDataCollection decodedMessage = new T8700DrivingRecordDataCollection();
        decodedMessage.decodeBody(encoded);
        
        // 验证
        assertEquals(originalMessage.getCommandWord(), decodedMessage.getCommandWord());
        assertArrayEquals(originalMessage.getDataBlockBytes(), decodedMessage.getDataBlockBytes());
        assertEquals(originalMessage.hasDataBlock(), decodedMessage.hasDataBlock());
        assertEquals(originalMessage.getDataBlockLength(), decodedMessage.getDataBlockLength());
    }

    @Test
    @DisplayName("测试Getter和Setter方法")
    void testGettersAndSetters() {
        T8700DrivingRecordDataCollection message = new T8700DrivingRecordDataCollection();
        
        // 测试命令字
        byte commandWord = (byte) 0x0A;
        message.setCommandWord(commandWord);
        assertEquals(commandWord, message.getCommandWord());
        
        // 测试数据块
        Buffer dataBlock = Buffer.buffer(new byte[]{0x11, 0x22, 0x33});
        message.setDataBlock(dataBlock);
        assertEquals(dataBlock, message.getDataBlock());
        assertTrue(message.hasDataBlock());
        assertEquals(3, message.getDataBlockLength());
        
        // 测试数据块字节数组
        byte[] dataBytes = {0x44, 0x55, 0x66, 0x77};
        message.setDataBlockBytes(dataBytes);
        assertArrayEquals(dataBytes, message.getDataBlockBytes());
        assertEquals(4, message.getDataBlockLength());
        
        // 测试设置null数据块
        message.setDataBlock(null);
        assertNull(message.getDataBlock());
        assertFalse(message.hasDataBlock());
        assertEquals(0, message.getDataBlockLength());
        
        // 测试设置空字节数组
        message.setDataBlockBytes(new byte[0]);
        assertNull(message.getDataBlock());
        assertFalse(message.hasDataBlock());
    }

    @Test
    @DisplayName("测试常量命令字")
    void testCommandWordConstants() {
        assertEquals((byte) 0x01, T8700DrivingRecordDataCollection.CommandWords.READ_BASIC_INFO);
        assertEquals((byte) 0x02, T8700DrivingRecordDataCollection.CommandWords.READ_PARAMETERS);
        assertEquals((byte) 0x03, T8700DrivingRecordDataCollection.CommandWords.READ_TIME);
        assertEquals((byte) 0x04, T8700DrivingRecordDataCollection.CommandWords.SET_TIME);
        assertEquals((byte) 0x05, T8700DrivingRecordDataCollection.CommandWords.READ_VEHICLE_INFO);
        assertEquals((byte) 0x06, T8700DrivingRecordDataCollection.CommandWords.READ_DRIVER_INFO);
        assertEquals((byte) 0x07, T8700DrivingRecordDataCollection.CommandWords.READ_DRIVING_STATUS);
        assertEquals((byte) 0x08, T8700DrivingRecordDataCollection.CommandWords.READ_ACCIDENT_RECORD);
        assertEquals((byte) 0x09, T8700DrivingRecordDataCollection.CommandWords.READ_OVERTIME_DRIVING);
        assertEquals((byte) 0x0A, T8700DrivingRecordDataCollection.CommandWords.READ_SPEED_STATUS_LOG);
    }

    @Test
    @DisplayName("测试边界值")
    void testBoundaryValues() {
        T8700DrivingRecordDataCollection message = new T8700DrivingRecordDataCollection();
        
        // 测试最小命令字值
        message.setCommandWord((byte) 0x00);
        assertEquals((byte) 0x00, message.getCommandWord());
        
        // 测试最大命令字值
        message.setCommandWord((byte) 0xFF);
        assertEquals((byte) 0xFF, message.getCommandWord());
        
        // 测试大数据块
        byte[] largeData = new byte[1024];
        for (int i = 0; i < largeData.length; i++) {
            largeData[i] = (byte) (i % 256);
        }
        message.setDataBlockBytes(largeData);
        assertEquals(1024, message.getDataBlockLength());
        assertArrayEquals(largeData, message.getDataBlockBytes());
    }

    @Test
    @DisplayName("测试真实场景 - 读取基本信息")
    void testRealScenarioReadBasicInfo() {
        // 创建读取基本信息命令（无数据块）
        T8700DrivingRecordDataCollection message = T8700DrivingRecordDataCollection.createCommandOnly(
                T8700DrivingRecordDataCollection.CommandWords.READ_BASIC_INFO);
        
        // 验证消息
        assertEquals(T8700DrivingRecordDataCollection.CommandWords.READ_BASIC_INFO, message.getCommandWord());
        assertFalse(message.hasDataBlock());
        
        // 编码测试
        Buffer encoded = message.encodeBody();
        assertEquals(1, encoded.length());
        assertEquals(T8700DrivingRecordDataCollection.CommandWords.READ_BASIC_INFO, encoded.getByte(0));
    }

    @Test
    @DisplayName("测试真实场景 - 设置时间")
    void testRealScenarioSetTime() {
        // 创建设置时间命令（带时间数据块）
        // 时间格式：年月日时分秒（BCD码）
        byte[] timeData = {0x23, 0x12, 0x25, 0x10, 0x30, 0x00}; // 2023年12月25日10时30分00秒
        
        T8700DrivingRecordDataCollection message = T8700DrivingRecordDataCollection.createWithDataBlock(
                T8700DrivingRecordDataCollection.CommandWords.SET_TIME, timeData);
        
        // 验证消息
        assertEquals(T8700DrivingRecordDataCollection.CommandWords.SET_TIME, message.getCommandWord());
        assertTrue(message.hasDataBlock());
        assertEquals(6, message.getDataBlockLength());
        assertArrayEquals(timeData, message.getDataBlockBytes());
        
        // 编码测试
        Buffer encoded = message.encodeBody();
        assertEquals(7, encoded.length());
        assertEquals(T8700DrivingRecordDataCollection.CommandWords.SET_TIME, encoded.getByte(0));
        assertArrayEquals(timeData, encoded.getBytes(1, 7));
    }

    @Test
    @DisplayName("测试toString方法")
    void testToString() {
        // 测试无数据块的消息
        T8700DrivingRecordDataCollection message1 = T8700DrivingRecordDataCollection.createCommandOnly((byte) 0x01);
        String str1 = message1.toString();
        assertTrue(str1.contains("commandWord=0x01"));
        assertTrue(str1.contains("dataBlockLength=0"));
        assertTrue(str1.contains("hasDataBlock=false"));
        
        // 测试有数据块的消息
        T8700DrivingRecordDataCollection message2 = T8700DrivingRecordDataCollection.createWithDataBlock(
                (byte) 0x05, new byte[]{0x11, 0x22, 0x33});
        String str2 = message2.toString();
        assertTrue(str2.contains("commandWord=0x05"));
        assertTrue(str2.contains("dataBlockLength=3"));
        assertTrue(str2.contains("hasDataBlock=true"));
    }

    @Test
    @DisplayName("测试equals和hashCode方法")
    void testEqualsAndHashCode() {
        byte commandWord = (byte) 0x07;
        byte[] dataBytes = {0x11, 0x22, 0x33};
        
        T8700DrivingRecordDataCollection message1 = T8700DrivingRecordDataCollection.createWithDataBlock(commandWord, dataBytes);
        T8700DrivingRecordDataCollection message2 = T8700DrivingRecordDataCollection.createWithDataBlock(commandWord, dataBytes);
        T8700DrivingRecordDataCollection message3 = T8700DrivingRecordDataCollection.createWithDataBlock((byte) 0x08, dataBytes);
        T8700DrivingRecordDataCollection message4 = T8700DrivingRecordDataCollection.createCommandOnly(commandWord);
        
        // 测试equals
        assertEquals(message1, message2);
        assertNotEquals(message1, message3);
        assertNotEquals(message1, message4);
        assertNotEquals(message1, null);
        assertNotEquals(message1, "not a message");
        assertEquals(message1, message1);
        
        // 测试hashCode
        assertEquals(message1.hashCode(), message2.hashCode());
        assertNotEquals(message1.hashCode(), message3.hashCode());
        assertNotEquals(message1.hashCode(), message4.hashCode());
    }
}