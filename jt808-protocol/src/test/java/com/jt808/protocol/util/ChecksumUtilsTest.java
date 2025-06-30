package com.jt808.protocol.util;

import com.jt808.protocol.util.ChecksumUtils.ChecksumResult;
import io.vertx.core.buffer.Buffer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 校验码工具类测试
 */
class ChecksumUtilsTest {
    
    @Test
    @DisplayName("测试字节数组校验码计算")
    void testCalculateChecksumWithByteArray() {
        // 测试基本校验码计算
        byte[] data = {0x01, 0x02, 0x03, 0x04};
        byte checksum = ChecksumUtils.calculateChecksum(data);
        assertEquals((byte) 0x04, checksum); // 1^2^3^4 = 4
        
        // 测试部分数据校验码计算
        byte partialChecksum = ChecksumUtils.calculateChecksum(data, 1, 2);
        assertEquals((byte) 0x01, partialChecksum); // 2^3 = 1
    }
    
    @Test
    @DisplayName("测试Buffer校验码计算")
    void testCalculateChecksumWithBuffer() {
        // 测试Buffer校验码计算
        Buffer buffer = Buffer.buffer(new byte[]{0x01, 0x02, 0x03, 0x04});
        byte checksum = ChecksumUtils.calculateChecksum(buffer);
        assertEquals((byte) 0x04, checksum);
        
        // 测试部分Buffer校验码计算
        byte partialChecksum = ChecksumUtils.calculateChecksum(buffer, 1, 2);
        assertEquals((byte) 0x01, partialChecksum);
    }
    
    @Test
    @DisplayName("测试空数据校验码计算")
    void testCalculateChecksumWithEmptyData() {
        // 测试空字节数组
        byte[] emptyArray = new byte[0];
        assertEquals((byte) 0, ChecksumUtils.calculateChecksum(emptyArray));
        
        // 测试null数组
        assertEquals((byte) 0, ChecksumUtils.calculateChecksum((byte[]) null));
        
        // 测试空Buffer
        Buffer emptyBuffer = Buffer.buffer();
        assertEquals((byte) 0, ChecksumUtils.calculateChecksum(emptyBuffer));
        
        // 测试null Buffer
        assertEquals((byte) 0, ChecksumUtils.calculateChecksum((Buffer) null));
    }
    
    @Test
    @DisplayName("测试无效参数的校验码计算")
    void testCalculateChecksumWithInvalidParams() {
        byte[] data = {0x01, 0x02, 0x03, 0x04};
        
        // 测试无效起始位置
        assertEquals((byte) 0, ChecksumUtils.calculateChecksum(data, -1, 2));
        assertEquals((byte) 0, ChecksumUtils.calculateChecksum(data, 5, 2));
        
        // 测试无效长度
        assertEquals((byte) 0, ChecksumUtils.calculateChecksum(data, 0, 0));
        assertEquals((byte) 0, ChecksumUtils.calculateChecksum(data, 0, -1));
        assertEquals((byte) 0, ChecksumUtils.calculateChecksum(data, 2, 5));
        
        Buffer buffer = Buffer.buffer(data);
        
        // 测试Buffer的无效参数
        assertEquals((byte) 0, ChecksumUtils.calculateChecksum(buffer, -1, 2));
        assertEquals((byte) 0, ChecksumUtils.calculateChecksum(buffer, 5, 2));
        assertEquals((byte) 0, ChecksumUtils.calculateChecksum(buffer, 0, 0));
        assertEquals((byte) 0, ChecksumUtils.calculateChecksum(buffer, 0, -1));
        assertEquals((byte) 0, ChecksumUtils.calculateChecksum(buffer, 2, 5));
    }
    
    @Test
    @DisplayName("测试校验码验证")
    void testVerifyChecksum() {
        byte[] data = {0x01, 0x02, 0x03, 0x04};
        byte correctChecksum = (byte) 0x04;
        byte wrongChecksum = (byte) 0x05;
        
        // 测试正确的校验码
        assertTrue(ChecksumUtils.verifyChecksum(data, correctChecksum));
        
        // 测试错误的校验码
        assertFalse(ChecksumUtils.verifyChecksum(data, wrongChecksum));
        
        // 测试部分数据校验码验证
        assertTrue(ChecksumUtils.verifyChecksum(data, 1, 2, (byte) 0x01));
        assertFalse(ChecksumUtils.verifyChecksum(data, 1, 2, (byte) 0x02));
        
        // 测试Buffer校验码验证
        Buffer buffer = Buffer.buffer(data);
        assertTrue(ChecksumUtils.verifyChecksum(buffer, correctChecksum));
        assertFalse(ChecksumUtils.verifyChecksum(buffer, wrongChecksum));
        
        assertTrue(ChecksumUtils.verifyChecksum(buffer, 1, 2, (byte) 0x01));
        assertFalse(ChecksumUtils.verifyChecksum(buffer, 1, 2, (byte) 0x02));
    }
    
    @Test
    @DisplayName("测试完整消息校验码验证")
    void testVerifyCompleteMessage() {
        // 创建一个有效的JT808消息
        Buffer message = Buffer.buffer();
        message.appendByte((byte) 0x7E); // 起始标识位
        message.appendBytes(new byte[]{0x01, 0x02, 0x03, 0x04}); // 消息头+消息体
        message.appendByte((byte) 0x04); // 校验码 (1^2^3^4 = 4)
        message.appendByte((byte) 0x7E); // 结束标识位
        
        ChecksumResult result = ChecksumUtils.verifyCompleteMessage(message);
        assertTrue(result.isValid());
        assertEquals((byte) 0x04, result.getActualChecksum());
        assertEquals((byte) 0x04, result.getExpectedChecksum());
        assertEquals("校验码验证成功", result.getMessage());
    }
    
    @Test
    @DisplayName("测试完整消息校验码验证失败")
    void testVerifyCompleteMessageFailed() {
        // 创建一个校验码错误的JT808消息
        Buffer message = Buffer.buffer();
        message.appendByte((byte) 0x7E); // 起始标识位
        message.appendBytes(new byte[]{0x01, 0x02, 0x03, 0x04}); // 消息头+消息体
        message.appendByte((byte) 0x05); // 错误的校验码
        message.appendByte((byte) 0x7E); // 结束标识位
        
        ChecksumResult result = ChecksumUtils.verifyCompleteMessage(message);
        assertFalse(result.isValid());
        assertEquals((byte) 0x04, result.getActualChecksum());
        assertEquals((byte) 0x05, result.getExpectedChecksum());
        assertEquals("校验码验证失败", result.getMessage());
    }
    
    @Test
    @DisplayName("测试无效完整消息")
    void testVerifyInvalidCompleteMessage() {
        // 测试消息长度不足
        Buffer shortMessage = Buffer.buffer(new byte[]{0x7E, 0x01, 0x7E});
        ChecksumResult result = ChecksumUtils.verifyCompleteMessage(shortMessage);
        assertFalse(result.isValid());
        assertEquals("消息长度不足", result.getMessage());
        
        // 测试null消息
        ChecksumResult nullResult = ChecksumUtils.verifyCompleteMessage(null);
        assertFalse(nullResult.isValid());
        assertEquals("消息长度不足", nullResult.getMessage());
        
        // 测试标识位错误
        Buffer invalidMessage = Buffer.buffer();
        invalidMessage.appendByte((byte) 0x7F); // 错误的起始标识位
        invalidMessage.appendBytes(new byte[]{0x01, 0x02, 0x03, 0x04});
        invalidMessage.appendByte((byte) 0x04);
        invalidMessage.appendByte((byte) 0x7E);
        
        ChecksumResult invalidResult = ChecksumUtils.verifyCompleteMessage(invalidMessage);
        assertFalse(invalidResult.isValid());
        assertEquals("消息标识位错误", invalidResult.getMessage());
    }
    
    @Test
    @DisplayName("测试ChecksumResult的toString方法")
    void testChecksumResultToString() {
        ChecksumResult validResult = new ChecksumResult(true, (byte) 0x04, (byte) 0x04, "成功");
        String validString = validResult.toString();
        assertTrue(validString.contains("valid=true"));
        assertTrue(validString.contains("actual=0x04"));
        assertTrue(validString.contains("expected=0x04"));
        assertTrue(validString.contains("成功"));
        
        ChecksumResult invalidResult = new ChecksumResult(false, (byte) 0x04, (byte) 0x05, "失败");
        String invalidString = invalidResult.toString();
        assertTrue(invalidString.contains("valid=false"));
        assertTrue(invalidString.contains("actual=0x04"));
        assertTrue(invalidString.contains("expected=0x05"));
        assertTrue(invalidString.contains("失败"));
    }
    
    @Test
    @DisplayName("测试复杂数据的校验码计算")
    void testComplexDataChecksum() {
        // 测试包含0x00和0xFF的数据
        byte[] complexData = {(byte) 0x00, (byte) 0xFF, 0x7E, 0x7D, (byte) 0x80};
        byte checksum = ChecksumUtils.calculateChecksum(complexData);
        
        // 手动计算：0x00 ^ 0xFF ^ 0x7E ^ 0x7D ^ 0x80 = 0x02
        assertEquals((byte) 0x02, checksum);
        
        // 验证校验码
        assertTrue(ChecksumUtils.verifyChecksum(complexData, (byte) 0x02));
        assertFalse(ChecksumUtils.verifyChecksum(complexData, (byte) 0x03));
    }
}