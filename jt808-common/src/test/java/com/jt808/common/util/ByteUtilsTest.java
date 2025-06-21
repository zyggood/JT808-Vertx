package com.jt808.common.util;

import io.vertx.core.buffer.Buffer;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * ByteUtils工具类测试
 */
class ByteUtilsTest {
    
    @Test
    void testCalculateChecksumWithByteArray() {
        // 测试字节数组校验码计算
        byte[] data = {0x01, 0x02, 0x03, 0x04};
        byte checksum = ByteUtils.calculateChecksum(data, 0, data.length);
        assertEquals((byte) 0x04, checksum); // 1^2^3^4 = 4
        
        // 测试部分数据校验码计算
        byte partialChecksum = ByteUtils.calculateChecksum(data, 1, 2);
        assertEquals((byte) 0x01, partialChecksum); // 2^3 = 1
    }
    
    @Test
    void testCalculateChecksumWithBuffer() {
        // 测试Buffer校验码计算
        Buffer buffer = Buffer.buffer(new byte[]{0x01, 0x02, 0x03, 0x04});
        byte checksum = ByteUtils.calculateChecksum(buffer, 0, buffer.length());
        assertEquals((byte) 0x04, checksum);
        
        // 测试部分Buffer校验码计算
        byte partialChecksum = ByteUtils.calculateChecksum(buffer, 1, 2);
        assertEquals((byte) 0x01, partialChecksum);
    }
    
    @Test
    void testEscape() {
        // 测试转义处理
        Buffer original = Buffer.buffer(new byte[]{0x7E, 0x01, 0x7D, 0x02});
        Buffer escaped = ByteUtils.escape(original);
        
        byte[] expected = {0x7D, 0x02, 0x01, 0x7D, 0x01, 0x02};
        assertArrayEquals(expected, escaped.getBytes());
    }
    
    @Test
    void testUnescape() {
        // 测试反转义处理
        Buffer escaped = Buffer.buffer(new byte[]{0x7D, 0x02, 0x01, 0x7D, 0x01, 0x02});
        Buffer unescaped = ByteUtils.unescape(escaped);
        
        byte[] expected = {0x7E, 0x01, 0x7D, 0x02};
        assertArrayEquals(expected, unescaped.getBytes());
    }
    
    @Test
    void testEscapeAndUnescape() {
        // 测试转义和反转义的对称性
        Buffer original = Buffer.buffer(new byte[]{0x7E, 0x7D, 0x01, 0x02, 0x7E, 0x7D});
        Buffer escaped = ByteUtils.escape(original);
        Buffer unescaped = ByteUtils.unescape(escaped);
        
        assertArrayEquals(original.getBytes(), unescaped.getBytes());
    }
    
    @Test
    void testBytesToHex() {
        // 测试字节数组转十六进制字符串
        byte[] bytes = {0x01, 0x23, (byte) 0xAB, (byte) 0xFF};
        String hex = ByteUtils.bytesToHex(bytes);
        assertEquals("0123ABFF", hex);
        
        // 测试空数组
        String emptyHex = ByteUtils.bytesToHex(new byte[0]);
        assertEquals("", emptyHex);
    }
    
    @Test
    void testHexToBytes() {
        // 测试十六进制字符串转字节数组
        String hex = "0123ABFF";
        byte[] bytes = ByteUtils.hexToBytes(hex);
        byte[] expected = {0x01, 0x23, (byte) 0xAB, (byte) 0xFF};
        assertArrayEquals(expected, bytes);
        
        // 测试空字符串
        byte[] emptyBytes = ByteUtils.hexToBytes("");
        assertEquals(0, emptyBytes.length);
    }
    
    @Test
    void testHexAndBytesConversion() {
        // 测试十六进制和字节数组转换的对称性
        byte[] original = {0x12, 0x34, (byte) 0x56, (byte) 0x78, (byte) 0x9A, (byte) 0xBC, (byte) 0xDE, (byte) 0xF0};
        String hex = ByteUtils.bytesToHex(original);
        byte[] converted = ByteUtils.hexToBytes(hex);
        assertArrayEquals(original, converted);
    }
    
    @Test
    void testToBCD() {
        // 测试BCD编码
        byte[] bcd = ByteUtils.toBCD(123456, 3);
        byte[] expected = {0x12, 0x34, 0x56};
        assertArrayEquals(expected, bcd);
        
        // 测试带前导零的BCD编码
        byte[] bcdWithZero = ByteUtils.toBCD(123, 3);
        byte[] expectedWithZero = {0x00, 0x01, 0x23};
        assertArrayEquals(expectedWithZero, bcdWithZero);
    }
    
    @Test
    void testFromBCD() {
        // 测试BCD解码
        byte[] bcd = {0x12, 0x34, 0x56};
        long value = ByteUtils.fromBCD(bcd);
        assertEquals(123456L, value);
        
        // 测试带前导零的BCD解码
        byte[] bcdWithZero = {0x00, 0x01, 0x23};
        long valueWithZero = ByteUtils.fromBCD(bcdWithZero);
        assertEquals(123L, valueWithZero);
    }
    
    @Test
    void testBCDConversion() {
        // 测试BCD编码和解码的对称性
        long original = 987654321L;
        byte[] bcd = ByteUtils.toBCD(original, 5);
        long converted = ByteUtils.fromBCD(bcd);
        assertEquals(original, converted);
    }
    
    @Test
    void testEdgeCases() {
        // 测试边界情况
        
        // 空Buffer的转义处理
        Buffer emptyBuffer = Buffer.buffer();
        Buffer escapedEmpty = ByteUtils.escape(emptyBuffer);
        assertEquals(0, escapedEmpty.length());
        
        Buffer unescapedEmpty = ByteUtils.unescape(emptyBuffer);
        assertEquals(0, unescapedEmpty.length());
        
        // 单字节校验码
        byte[] singleByte = {0x42};
        byte singleChecksum = ByteUtils.calculateChecksum(singleByte, 0, 1);
        assertEquals((byte) 0x42, singleChecksum);
        
        // BCD编码零值
        byte[] zeroBcd = ByteUtils.toBCD(0, 2);
        byte[] expectedZero = {0x00, 0x00};
        assertArrayEquals(expectedZero, zeroBcd);
        
        long zeroValue = ByteUtils.fromBCD(zeroBcd);
        assertEquals(0L, zeroValue);
    }
}