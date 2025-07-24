package com.jt808.protocol.message;

import com.jt808.protocol.constants.MessageTypes;
import com.jt808.protocol.factory.JT808MessageFactory;
import io.vertx.core.buffer.Buffer;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

/**
 * T0901数据压缩上报测试类
 */
class T0901DataCompressionReportTest {
    
    @Test
    void testMessageId() {
        T0901DataCompressionReport message = new T0901DataCompressionReport();
        assertEquals(0x0901, message.getMessageId());
    }
    
    @Test
    void testDefaultConstructor() {
        T0901DataCompressionReport message = new T0901DataCompressionReport();
        assertEquals(0L, message.getCompressedMessageLength());
        assertArrayEquals(new byte[0], message.getCompressedMessageBody());
    }
    
    @Test
    void testConstructorWithCompressedData() {
        byte[] compressedData = {0x1F, (byte) 0x8B, 0x08, 0x00}; // GZIP头部
        T0901DataCompressionReport message = new T0901DataCompressionReport(compressedData);
        
        assertEquals(compressedData.length, message.getCompressedMessageLength());
        assertArrayEquals(compressedData, message.getCompressedMessageBody());
    }
    
    @Test
    void testConstructorWithAutoCompress() throws IOException {
        String originalData = "这是需要压缩的测试数据，包含中文字符和英文字符ABC123";
        byte[] originalBytes = originalData.getBytes(StandardCharsets.UTF_8);
        
        // 测试自动压缩
        T0901DataCompressionReport message = new T0901DataCompressionReport(originalBytes, true);
        
        assertTrue(message.getCompressedMessageLength() > 0);
        // 注意：对于短字符串，GZIP压缩可能因为头部开销而增加大小
        // assertTrue(message.getCompressedMessageLength() < originalBytes.length); // 压缩后应该更小
        
        // 验证可以解压缩回原始数据
        byte[] decompressed = message.decompressMessageBody();
        assertArrayEquals(originalBytes, decompressed);
        assertEquals(originalData, new String(decompressed, StandardCharsets.UTF_8));
    }
    
    @Test
    void testConstructorWithoutAutoCompress() {
        byte[] originalData = {0x01, 0x02, 0x03, 0x04};
        T0901DataCompressionReport message = new T0901DataCompressionReport(originalData, false);
        
        assertEquals(originalData.length, message.getCompressedMessageLength());
        assertArrayEquals(originalData, message.getCompressedMessageBody());
    }
    
    @Test
    void testGettersAndSetters() {
        T0901DataCompressionReport message = new T0901DataCompressionReport();
        
        // 测试压缩消息长度
        message.setCompressedMessageLength(100L);
        assertEquals(100L, message.getCompressedMessageLength());
        
        // 测试负数长度
        assertThrows(IllegalArgumentException.class, () -> {
            message.setCompressedMessageLength(-1L);
        });
        
        // 测试压缩消息体
        byte[] data = {0x10, 0x20, 0x30, 0x40};
        message.setCompressedMessageBody(data);
        assertArrayEquals(data, message.getCompressedMessageBody());
        assertEquals(data.length, message.getCompressedMessageLength()); // 长度应该自动更新
    }
    
    @Test
    void testEncodeAndDecode() {
        byte[] compressedData = {0x1F, (byte) 0x8B, 0x08, 0x00, 0x01, 0x02, 0x03, 0x04};
        T0901DataCompressionReport original = new T0901DataCompressionReport(compressedData);
        
        // 编码
        Buffer encoded = original.encode();
        assertNotNull(encoded);
        assertEquals(4 + compressedData.length, encoded.length()); // 4字节长度 + 压缩数据
        
        // 验证编码格式
        assertEquals(compressedData.length, encoded.getInt(0)); // 压缩消息长度
        
        // 解码
        T0901DataCompressionReport decoded = T0901DataCompressionReport.decode(encoded);
        assertNotNull(decoded);
        
        // 验证解码结果
        assertEquals(original.getCompressedMessageLength(), decoded.getCompressedMessageLength());
        assertArrayEquals(original.getCompressedMessageBody(), decoded.getCompressedMessageBody());
    }
    
    @Test
    void testCompressAndDecompressData() throws IOException {
        String originalText = "Hello World! 这是一个测试字符串，包含中英文字符。";
        byte[] originalData = originalText.getBytes(StandardCharsets.UTF_8);
        
        // 压缩数据
        byte[] compressed = T0901DataCompressionReport.compressData(originalData);
        assertNotNull(compressed);
        assertTrue(compressed.length > 0);
        
        // 解压缩数据
        byte[] decompressed = T0901DataCompressionReport.decompressData(compressed);
        assertArrayEquals(originalData, decompressed);
        assertEquals(originalText, new String(decompressed, StandardCharsets.UTF_8));
    }
    
    @Test
    void testCompressEmptyData() throws IOException {
        byte[] emptyData = new byte[0];
        byte[] compressed = T0901DataCompressionReport.compressData(emptyData);
        assertArrayEquals(new byte[0], compressed);
        
        byte[] decompressed = T0901DataCompressionReport.decompressData(compressed);
        assertArrayEquals(new byte[0], decompressed);
    }
    
    @Test
    void testCompressNullData() throws IOException {
        byte[] compressed = T0901DataCompressionReport.compressData(null);
        assertArrayEquals(new byte[0], compressed);
        
        byte[] decompressed = T0901DataCompressionReport.decompressData(null);
        assertArrayEquals(new byte[0], decompressed);
    }
    
    @Test
    void testCompressionRatio() {
        byte[] originalData = new byte[1000];
        for (int i = 0; i < originalData.length; i++) {
            originalData[i] = (byte) (i % 10); // 重复数据，压缩效果好
        }
        
        T0901DataCompressionReport message = new T0901DataCompressionReport(originalData, true);
        
        double ratio = message.getCompressionRatio(originalData.length);
        assertTrue(ratio > 0); // 应该有压缩效果
        assertTrue(ratio < 100); // 压缩比应该小于100%
        
        // 测试原始长度为0的情况
        assertEquals(0.0, message.getCompressionRatio(0));
    }
    
    @Test
    void testSetOriginalDataAndCompress() throws IOException {
        T0901DataCompressionReport message = new T0901DataCompressionReport();
        
        String originalText = "需要压缩的原始数据";
        byte[] originalData = originalText.getBytes(StandardCharsets.UTF_8);
        
        message.setOriginalDataAndCompress(originalData);
        
        assertTrue(message.getCompressedMessageLength() > 0);
        
        // 验证可以解压缩回原始数据
        byte[] decompressed = message.decompressMessageBody();
        assertArrayEquals(originalData, decompressed);
        assertEquals(originalText, new String(decompressed, StandardCharsets.UTF_8));
    }
    
    @Test
    void testToString() {
        T0901DataCompressionReport message = new T0901DataCompressionReport(new byte[]{0x01, 0x02, 0x03});
        
        String toString = message.toString();
        assertNotNull(toString);
        assertTrue(toString.contains("T0901DataCompressionReport"));
        assertTrue(toString.contains("compressedMessageLength=3"));
        assertTrue(toString.contains("compressedMessageBodyLength=3"));
    }
    
    @Test
    void testEqualsAndHashCode() {
        byte[] data = {0x01, 0x02, 0x03};
        T0901DataCompressionReport message1 = new T0901DataCompressionReport(data);
        T0901DataCompressionReport message2 = new T0901DataCompressionReport(data);
        T0901DataCompressionReport message3 = new T0901DataCompressionReport(new byte[]{0x04, 0x05, 0x06});
        
        // 测试equals
        assertEquals(message1, message2);
        assertNotEquals(message1, message3);
        assertNotEquals(message1, null);
        assertNotEquals(message1, "string");
        
        // 测试hashCode
        assertEquals(message1.hashCode(), message2.hashCode());
    }
    
    @Test
    void testInvalidDecodeBuffer() {
        // 测试空缓冲区
        assertThrows(IllegalArgumentException.class, () -> {
            T0901DataCompressionReport.decode(null);
        });
        
        // 测试长度不足的缓冲区
        assertThrows(IllegalArgumentException.class, () -> {
            T0901DataCompressionReport.decode(Buffer.buffer().appendByte((byte) 0x01));
        });
    }
    
    @Test
    void testLengthMismatch() {
        // 创建一个长度不匹配的缓冲区
        Buffer buffer = Buffer.buffer();
        buffer.appendInt(10); // 声明长度为10
        buffer.appendBytes(new byte[]{0x01, 0x02, 0x03}); // 实际只有3字节
        
        assertThrows(IllegalArgumentException.class, () -> {
            T0901DataCompressionReport.decode(buffer);
        });
    }
    
    @Test
    void testNullParameterHandling() {
        // 测试null压缩数据
        T0901DataCompressionReport message = new T0901DataCompressionReport(null);
        assertEquals(0L, message.getCompressedMessageLength());
        assertArrayEquals(new byte[0], message.getCompressedMessageBody());
        
        // 测试setter的null处理
        message.setCompressedMessageBody(null);
        assertEquals(0L, message.getCompressedMessageLength());
        assertArrayEquals(new byte[0], message.getCompressedMessageBody());
    }
    
    @Test
    void testFactoryCreation() {
        JT808MessageFactory factory = JT808MessageFactory.getInstance();
        JT808Message message = factory.createMessage(MessageTypes.Terminal.DATA_COMPRESSION_REPORT);
        
        assertNotNull(message);
        assertInstanceOf(T0901DataCompressionReport.class, message);
        assertEquals(0x0901, message.getMessageId());
    }
    
    @Test
    void testEmptyCompressedData() {
        T0901DataCompressionReport message = new T0901DataCompressionReport(new byte[0]);
        
        Buffer encoded = message.encode();
        assertEquals(4, encoded.length()); // 只有4字节的长度字段
        assertEquals(0, encoded.getInt(0));
        
        T0901DataCompressionReport decoded = T0901DataCompressionReport.decode(encoded);
        assertEquals(0L, decoded.getCompressedMessageLength());
        assertArrayEquals(new byte[0], decoded.getCompressedMessageBody());
    }
    
    @Test
    void testLargeCompressedData() throws IOException {
        // 创建大量重复数据，压缩效果好
        byte[] largeOriginalData = new byte[10000];
        for (int i = 0; i < largeOriginalData.length; i++) {
            largeOriginalData[i] = (byte) (i % 100);
        }
        
        T0901DataCompressionReport message = new T0901DataCompressionReport(largeOriginalData, true);
        
        // 验证压缩效果
        assertTrue(message.getCompressedMessageLength() < largeOriginalData.length);
        double compressionRatio = message.getCompressionRatio(largeOriginalData.length);
        assertTrue(compressionRatio > 50); // 应该有很好的压缩效果
        
        // 验证编码解码
        Buffer encoded = message.encode();
        T0901DataCompressionReport decoded = T0901DataCompressionReport.decode(encoded);
        
        assertEquals(message.getCompressedMessageLength(), decoded.getCompressedMessageLength());
        assertArrayEquals(message.getCompressedMessageBody(), decoded.getCompressedMessageBody());
        
        // 验证解压缩
        byte[] decompressed = decoded.decompressMessageBody();
        assertArrayEquals(largeOriginalData, decompressed);
    }
    
    @Test
    void testInvalidGzipData() {
        // 创建无效的GZIP数据
        byte[] invalidGzipData = {0x00, 0x01, 0x02, 0x03};
        T0901DataCompressionReport message = new T0901DataCompressionReport(invalidGzipData);
        
        // 尝试解压缩应该抛出异常
        assertThrows(IOException.class, () -> {
            message.decompressMessageBody();
        });
    }
    
    @Test
    void testMaxLengthValue() {
        T0901DataCompressionReport message = new T0901DataCompressionReport();
        
        // 测试最大长度值
        long maxLength = 0xFFFFFFFFL; // DWORD最大值
        message.setCompressedMessageLength(maxLength);
        assertEquals(maxLength, message.getCompressedMessageLength());
    }
    
    @Test
    void testCompressionWithDifferentDataTypes() throws IOException {
        // 测试不同类型的数据压缩效果
        
        // 1. 重复数据（压缩效果好）
        byte[] repetitiveData = new byte[1000];
        for (int i = 0; i < repetitiveData.length; i++) {
            repetitiveData[i] = (byte) 0xAA;
        }
        T0901DataCompressionReport repetitiveMessage = new T0901DataCompressionReport(repetitiveData, true);
        double repetitiveRatio = repetitiveMessage.getCompressionRatio(repetitiveData.length);
        
        // 2. 随机数据（压缩效果差）
        byte[] randomData = new byte[1000];
        for (int i = 0; i < randomData.length; i++) {
            randomData[i] = (byte) (Math.random() * 256);
        }
        T0901DataCompressionReport randomMessage = new T0901DataCompressionReport(randomData, true);
        double randomRatio = randomMessage.getCompressionRatio(randomData.length);
        
        // 重复数据的压缩比应该比随机数据好
        assertTrue(repetitiveRatio > randomRatio);
        
        // 验证都可以正确解压缩
        assertArrayEquals(repetitiveData, repetitiveMessage.decompressMessageBody());
        assertArrayEquals(randomData, randomMessage.decompressMessageBody());
    }
}