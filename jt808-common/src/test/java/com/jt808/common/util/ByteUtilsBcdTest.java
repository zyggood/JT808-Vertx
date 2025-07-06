package com.jt808.common.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * ByteUtils BCD编解码方法测试
 */
class ByteUtilsBcdTest {
    
    @Test
    void testEncodeBcd_normalString() {
        // 测试正常的字符串编码
        byte[] result = ByteUtils.encodeBcd("123456", 3);
        assertArrayEquals(new byte[]{0x12, 0x34, 0x56}, result);
    }
    
    @Test
    void testEncodeBcd_oddLengthString() {
        // 测试奇数长度字符串（会自动补0）
        byte[] result = ByteUtils.encodeBcd("12345", 3);
        assertArrayEquals(new byte[]{0x01, 0x23, 0x45}, result);
    }
    
    @Test
    void testEncodeBcd_emptyString() {
        // 测试空字符串
        byte[] result = ByteUtils.encodeBcd("", 2);
        assertArrayEquals(new byte[]{0x00, 0x00}, result);
    }
    
    @Test
    void testEncodeBcd_nullString() {
        // 测试null字符串
        assertThrows(IllegalArgumentException.class, () -> {
            ByteUtils.encodeBcd(null, 2);
        });
    }
    
    @Test
    void testEncodeBcd_invalidLength() {
        // 测试无效长度
        assertThrows(IllegalArgumentException.class, () -> {
            ByteUtils.encodeBcd("123", 0);
        });
        
        assertThrows(IllegalArgumentException.class, () -> {
            ByteUtils.encodeBcd("123", -1);
        });
    }
    
    @Test
    void testEncodeBcd_invalidCharacter() {
        // 测试包含非数字字符
        assertThrows(IllegalArgumentException.class, () -> {
            ByteUtils.encodeBcd("12A3", 2);
        });
    }
    
    @Test
    void testDecodeBcd_normalBytes() {
        // 测试正常的字节数组解码
        byte[] input = {0x12, 0x34, 0x56};
        String result = ByteUtils.decodeBcd(input);
        assertEquals("123456", result);
    }
    
    @Test
    void testDecodeBcd_withLeadingZeros() {
        // 测试包含前导零的解码
        byte[] input = {0x00, 0x12, 0x34};
        String result = ByteUtils.decodeBcd(input);
        assertEquals("1234", result);
    }
    
    @Test
    void testDecodeBcd_allZeros() {
        // 测试全零字节数组
        byte[] input = {0x00, 0x00, 0x00};
        String result = ByteUtils.decodeBcd(input);
        assertEquals("0", result);
    }
    
    @Test
    void testDecodeBcd_nullBytes() {
        // 测试null字节数组
        assertThrows(IllegalArgumentException.class, () -> {
            ByteUtils.decodeBcd(null);
        });
    }
    
    @Test
    void testDecodeBcd_emptyBytes() {
        // 测试空字节数组
        byte[] input = {};
        String result = ByteUtils.decodeBcd(input);
        assertEquals("0", result);
    }
    
    @Test
    void testBcdRoundTrip() {
        // 测试编码解码的往返一致性
        String original = "12345678901234567890";
        byte[] encoded = ByteUtils.encodeBcd(original, 10);
        String decoded = ByteUtils.decodeBcd(encoded);
        assertEquals(original, decoded);
    }
    
    @Test
    void testBcdRoundTrip_withOddLength() {
        // 测试奇数长度字符串的往返一致性
        String original = "123456789";
        byte[] encoded = ByteUtils.encodeBcd(original, 5);
        String decoded = ByteUtils.decodeBcd(encoded);
        assertEquals(original, decoded); // decodeBcd会移除前导零，所以结果与原始字符串相同
    }
    
    @Test
    void testBcdRoundTrip_iccidExample() {
        // 测试ICCID的实际使用场景
        String iccid = "12345678901234567890";
        byte[] encoded = ByteUtils.encodeBcd(iccid, 10);
        String decoded = ByteUtils.decodeBcd(encoded);
        assertEquals(iccid, decoded);
        assertEquals(10, encoded.length);
    }
}