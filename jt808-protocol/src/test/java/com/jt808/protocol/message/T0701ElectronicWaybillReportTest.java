package com.jt808.protocol.message;

import io.vertx.core.buffer.Buffer;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * T0701ElectronicWaybillReport 测试类
 */
class T0701ElectronicWaybillReportTest {

    @Test
    void testMessageId() {
        T0701ElectronicWaybillReport message = new T0701ElectronicWaybillReport();
        assertEquals(0x0701, message.getMessageId());
    }

    @Test
    void testDefaultConstructor() {
        T0701ElectronicWaybillReport message = new T0701ElectronicWaybillReport();
        assertEquals(0, message.getWaybillLength());
        assertNotNull(message.getWaybillContent());
        assertEquals(0, message.getWaybillContent().length);
        assertFalse(message.hasWaybillContent());
        assertTrue(message.isLengthConsistent());
    }

    @Test
    void testConstructorWithLengthAndContent() {
        byte[] content = "Test waybill content".getBytes();
        T0701ElectronicWaybillReport message = new T0701ElectronicWaybillReport(content.length, content);
        
        assertEquals(content.length, message.getWaybillLength());
        assertArrayEquals(content, message.getWaybillContent());
        assertTrue(message.hasWaybillContent());
        assertTrue(message.isLengthConsistent());
    }

    @Test
    void testConstructorWithContentOnly() {
        byte[] content = "Auto length calculation".getBytes();
        T0701ElectronicWaybillReport message = new T0701ElectronicWaybillReport(content);
        
        assertEquals(content.length, message.getWaybillLength());
        assertArrayEquals(content, message.getWaybillContent());
        assertTrue(message.hasWaybillContent());
        assertTrue(message.isLengthConsistent());
    }

    @Test
    void testConstructorWithNullContent() {
        T0701ElectronicWaybillReport message = new T0701ElectronicWaybillReport(10, null);
        
        assertEquals(10, message.getWaybillLength());
        assertNotNull(message.getWaybillContent());
        assertEquals(0, message.getWaybillContent().length);
        assertFalse(message.hasWaybillContent());
        assertFalse(message.isLengthConsistent());
    }

    @Test
    void testCreateWithContent() {
        byte[] content = "Factory method test".getBytes();
        T0701ElectronicWaybillReport message = T0701ElectronicWaybillReport.create(content);
        
        assertEquals(content.length, message.getWaybillLength());
        assertArrayEquals(content, message.getWaybillContent());
        assertTrue(message.hasWaybillContent());
        assertTrue(message.isLengthConsistent());
    }

    @Test
    void testCreateWithLengthAndContent() {
        byte[] content = "Factory method with length".getBytes();
        T0701ElectronicWaybillReport message = T0701ElectronicWaybillReport.create(content.length, content);
        
        assertEquals(content.length, message.getWaybillLength());
        assertArrayEquals(content, message.getWaybillContent());
        assertTrue(message.hasWaybillContent());
        assertTrue(message.isLengthConsistent());
    }

    @Test
    void testCreateEmpty() {
        T0701ElectronicWaybillReport message = T0701ElectronicWaybillReport.createEmpty();
        
        assertEquals(0, message.getWaybillLength());
        assertNotNull(message.getWaybillContent());
        assertEquals(0, message.getWaybillContent().length);
        assertFalse(message.hasWaybillContent());
        assertTrue(message.isLengthConsistent());
    }

    @Test
    void testFromBytesWithValidData() {
        byte[] content = "From bytes test".getBytes();
        Buffer buffer = Buffer.buffer();
        buffer.appendUnsignedInt(content.length);
        buffer.appendBytes(content);
        
        T0701ElectronicWaybillReport message = T0701ElectronicWaybillReport.fromBytes(buffer.getBytes());
        
        assertEquals(content.length, message.getWaybillLength());
        assertArrayEquals(content, message.getWaybillContent());
        assertTrue(message.hasWaybillContent());
        assertTrue(message.isLengthConsistent());
    }

    @Test
    void testFromBytesWithInvalidData() {
        byte[] invalidData = {0x01, 0x02}; // 少于4字节
        T0701ElectronicWaybillReport message = T0701ElectronicWaybillReport.fromBytes(invalidData);
        
        assertEquals(0, message.getWaybillLength());
        assertEquals(0, message.getWaybillContent().length);
        assertFalse(message.hasWaybillContent());
    }

    @Test
    void testFromBytesWithNull() {
        T0701ElectronicWaybillReport message = T0701ElectronicWaybillReport.fromBytes(null);
        
        assertEquals(0, message.getWaybillLength());
        assertEquals(0, message.getWaybillContent().length);
        assertFalse(message.hasWaybillContent());
    }

    @Test
    void testEncodeBody() {
        byte[] content = "Encode test content".getBytes();
        T0701ElectronicWaybillReport message = new T0701ElectronicWaybillReport(content);
        
        Buffer encoded = message.encodeBody();
        
        assertEquals(4 + content.length, encoded.length());
        assertEquals(content.length, encoded.getUnsignedInt(0));
        
        byte[] decodedContent = new byte[content.length];
        encoded.getBytes(4, encoded.length(), decodedContent);
        assertArrayEquals(content, decodedContent);
    }

    @Test
    void testEncodeBodyWithEmptyContent() {
        T0701ElectronicWaybillReport message = new T0701ElectronicWaybillReport();
        
        Buffer encoded = message.encodeBody();
        
        assertEquals(4, encoded.length());
        assertEquals(0, encoded.getUnsignedInt(0));
    }

    @Test
    void testDecodeBody() {
        byte[] content = "Decode test content".getBytes();
        Buffer buffer = Buffer.buffer();
        buffer.appendUnsignedInt(content.length);
        buffer.appendBytes(content);
        
        T0701ElectronicWaybillReport message = new T0701ElectronicWaybillReport();
        message.decodeBody(buffer);
        
        assertEquals(content.length, message.getWaybillLength());
        assertArrayEquals(content, message.getWaybillContent());
        assertTrue(message.hasWaybillContent());
        assertTrue(message.isLengthConsistent());
    }

    @Test
    void testDecodeBodyWithOnlyLength() {
        Buffer buffer = Buffer.buffer();
        buffer.appendUnsignedInt(100); // 只有长度，没有内容
        
        T0701ElectronicWaybillReport message = new T0701ElectronicWaybillReport();
        message.decodeBody(buffer);
        
        assertEquals(100, message.getWaybillLength());
        assertEquals(0, message.getWaybillContent().length);
        assertFalse(message.hasWaybillContent());
        assertFalse(message.isLengthConsistent());
    }

    @Test
    void testDecodeBodyWithInvalidBuffer() {
        T0701ElectronicWaybillReport message = new T0701ElectronicWaybillReport();
        
        // 测试null buffer
        assertThrows(IllegalArgumentException.class, () -> message.decodeBody(null));
        
        // 测试长度不足的buffer
        Buffer shortBuffer = Buffer.buffer(new byte[]{0x01, 0x02});
        assertThrows(IllegalArgumentException.class, () -> message.decodeBody(shortBuffer));
    }

    @Test
    void testGettersAndSetters() {
        T0701ElectronicWaybillReport message = new T0701ElectronicWaybillReport();
        
        // 测试设置长度
        message.setWaybillLength(1000);
        assertEquals(1000, message.getWaybillLength());
        
        // 测试设置内容
        byte[] content = "Setter test".getBytes();
        message.setWaybillContent(content);
        assertArrayEquals(content, message.getWaybillContent());
        assertEquals(content.length, message.getWaybillLength()); // 应该自动更新长度
        
        // 测试设置null内容
        message.setWaybillContent(null);
        assertNotNull(message.getWaybillContent());
        assertEquals(0, message.getWaybillContent().length);
        assertEquals(0, message.getWaybillLength());
    }

    @Test
    void testUtilityMethods() {
        byte[] content = "Utility methods test".getBytes();
        T0701ElectronicWaybillReport message = new T0701ElectronicWaybillReport(content);
        
        assertTrue(message.hasWaybillContent());
        assertEquals(content.length, message.getActualWaybillLength());
        assertTrue(message.isLengthConsistent());
        assertEquals("电子运单上报", message.getMessageDescription());
        
        // 测试空内容
        T0701ElectronicWaybillReport emptyMessage = new T0701ElectronicWaybillReport();
        assertFalse(emptyMessage.hasWaybillContent());
        assertEquals(0, emptyMessage.getActualWaybillLength());
        assertTrue(emptyMessage.isLengthConsistent());
    }

    @Test
    void testLengthInconsistency() {
        byte[] content = "Length inconsistency test".getBytes();
        T0701ElectronicWaybillReport message = new T0701ElectronicWaybillReport(content.length + 10, content);
        
        assertEquals(content.length + 10, message.getWaybillLength());
        assertEquals(content.length, message.getActualWaybillLength());
        assertFalse(message.isLengthConsistent());
    }

    @Test
    void testBoundaryValues() {
        // 测试最大DWORD值
        long maxDword = 0xFFFFFFFFL;
        T0701ElectronicWaybillReport message = new T0701ElectronicWaybillReport();
        message.setWaybillLength(maxDword);
        assertEquals(maxDword, message.getWaybillLength());
        
        // 测试编码解码最大值
        Buffer buffer = Buffer.buffer();
        buffer.appendUnsignedInt(maxDword);
        
        T0701ElectronicWaybillReport decodedMessage = new T0701ElectronicWaybillReport();
        decodedMessage.decodeBody(buffer);
        assertEquals(maxDword, decodedMessage.getWaybillLength());
    }

    @Test
    void testRealWorldScenario() {
        // 模拟真实的电子运单数据
        String waybillData = "{\"waybillNo\":\"WB202301010001\",\"sender\":\"张三\",\"receiver\":\"李四\",\"goods\":\"电子产品\",\"weight\":5.5}";
        byte[] content = waybillData.getBytes();
        
        T0701ElectronicWaybillReport message = T0701ElectronicWaybillReport.create(content);
        
        // 验证创建
        assertTrue(message.hasWaybillContent());
        assertTrue(message.isLengthConsistent());
        assertEquals(content.length, message.getWaybillLength());
        
        // 验证编码
        Buffer encoded = message.encodeBody();
        assertNotNull(encoded);
        assertTrue(encoded.length() > 4);
        
        // 验证解码
        T0701ElectronicWaybillReport decodedMessage = new T0701ElectronicWaybillReport();
        decodedMessage.decodeBody(encoded);
        
        assertEquals(message.getWaybillLength(), decodedMessage.getWaybillLength());
        assertArrayEquals(message.getWaybillContent(), decodedMessage.getWaybillContent());
        assertTrue(decodedMessage.isLengthConsistent());
    }

    @Test
    void testToString() {
        byte[] content = "ToString test".getBytes();
        T0701ElectronicWaybillReport message = new T0701ElectronicWaybillReport(content);
        
        String str = message.toString();
        assertNotNull(str);
        assertTrue(str.contains("T0701ElectronicWaybillReport"));
        assertTrue(str.contains(String.valueOf(content.length)));
        assertTrue(str.contains("true")); // hasContent
    }

    @Test
    void testEquals() {
        byte[] content1 = "Content 1".getBytes();
        byte[] content2 = "Content 2".getBytes();
        
        T0701ElectronicWaybillReport message1 = new T0701ElectronicWaybillReport(content1);
        T0701ElectronicWaybillReport message2 = new T0701ElectronicWaybillReport(content1);
        T0701ElectronicWaybillReport message3 = new T0701ElectronicWaybillReport(content2);
        
        assertEquals(message1, message2);
        assertNotEquals(message1, message3);
        assertNotEquals(message1, null);
        assertNotEquals(message1, "not a message");
        assertEquals(message1, message1); // 自反性
    }

    @Test
    void testHashCode() {
        byte[] content = "HashCode test".getBytes();
        T0701ElectronicWaybillReport message1 = new T0701ElectronicWaybillReport(content);
        T0701ElectronicWaybillReport message2 = new T0701ElectronicWaybillReport(content);
        
        assertEquals(message1.hashCode(), message2.hashCode());
        
        // 测试不同内容的hashCode
        T0701ElectronicWaybillReport message3 = new T0701ElectronicWaybillReport("Different content".getBytes());
        assertNotEquals(message1.hashCode(), message3.hashCode());
    }

    @Test
    void testLargeContent() {
        // 测试大容量内容
        byte[] largeContent = new byte[10000];
        for (int i = 0; i < largeContent.length; i++) {
            largeContent[i] = (byte) (i % 256);
        }
        
        T0701ElectronicWaybillReport message = T0701ElectronicWaybillReport.create(largeContent);
        
        assertTrue(message.hasWaybillContent());
        assertEquals(largeContent.length, message.getWaybillLength());
        assertTrue(message.isLengthConsistent());
        
        // 测试编码解码
        Buffer encoded = message.encodeBody();
        T0701ElectronicWaybillReport decodedMessage = new T0701ElectronicWaybillReport();
        decodedMessage.decodeBody(encoded);
        
        assertArrayEquals(largeContent, decodedMessage.getWaybillContent());
        assertEquals(largeContent.length, decodedMessage.getWaybillLength());
    }
}