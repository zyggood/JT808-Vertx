package com.jt808.protocol.util;

import com.jt808.protocol.util.EscapeUtils.EscapeValidationResult;
import io.vertx.core.buffer.Buffer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 转义工具类测试
 */
class EscapeUtilsTest {

    @Test
    @DisplayName("测试常量定义")
    void testConstants() {
        assertEquals((byte) 0x7E, EscapeUtils.PROTOCOL_FLAG);
        assertEquals((byte) 0x7D, EscapeUtils.ESCAPE_FLAG);
        assertEquals((byte) 0x02, EscapeUtils.ESCAPE_7E);
        assertEquals((byte) 0x01, EscapeUtils.ESCAPE_7D);
    }

    @Test
    @DisplayName("测试Buffer转义处理")
    void testEscapeBuffer() {
        // 测试包含需要转义字符的数据
        Buffer original = Buffer.buffer(new byte[]{0x7E, 0x01, 0x7D, 0x02});
        Buffer escaped = EscapeUtils.escape(original);

        byte[] expected = {0x7D, 0x02, 0x01, 0x7D, 0x01, 0x02};
        assertArrayEquals(expected, escaped.getBytes());
    }

    @Test
    @DisplayName("测试字节数组转义处理")
    void testEscapeByteArray() {
        // 测试字节数组转义
        byte[] original = {0x7E, 0x01, 0x7D, 0x02};
        Buffer escaped = EscapeUtils.escape(original);

        byte[] expected = {0x7D, 0x02, 0x01, 0x7D, 0x01, 0x02};
        assertArrayEquals(expected, escaped.getBytes());
    }

    @Test
    @DisplayName("测试Buffer反转义处理")
    void testUnescapeBuffer() {
        // 测试反转义处理
        Buffer escaped = Buffer.buffer(new byte[]{0x7D, 0x02, 0x01, 0x7D, 0x01, 0x02});
        Buffer unescaped = EscapeUtils.unescape(escaped);

        byte[] expected = {0x7E, 0x01, 0x7D, 0x02};
        assertArrayEquals(expected, unescaped.getBytes());
    }

    @Test
    @DisplayName("测试字节数组反转义处理")
    void testUnescapeByteArray() {
        // 测试字节数组反转义
        byte[] escaped = {0x7D, 0x02, 0x01, 0x7D, 0x01, 0x02};
        Buffer unescaped = EscapeUtils.unescape(escaped);

        byte[] expected = {0x7E, 0x01, 0x7D, 0x02};
        assertArrayEquals(expected, unescaped.getBytes());
    }

    @Test
    @DisplayName("测试转义和反转义的对称性")
    void testEscapeUnescapeSymmetry() {
        // 测试转义和反转义的对称性
        Buffer original = Buffer.buffer(new byte[]{0x7E, 0x7D, 0x01, 0x02, 0x7E, 0x7D});
        Buffer escaped = EscapeUtils.escape(original);
        Buffer unescaped = EscapeUtils.unescape(escaped);

        assertArrayEquals(original.getBytes(), unescaped.getBytes());
    }

    @Test
    @DisplayName("测试空数据处理")
    void testEmptyData() {
        // 测试null数据
        Buffer nullEscaped = EscapeUtils.escape((Buffer) null);
        assertNotNull(nullEscaped);
        assertEquals(0, nullEscaped.length());

        Buffer nullUnescaped = EscapeUtils.unescape((Buffer) null);
        assertNotNull(nullUnescaped);
        assertEquals(0, nullUnescaped.length());

        // 测试空Buffer
        Buffer emptyBuffer = Buffer.buffer();
        Buffer escapedEmpty = EscapeUtils.escape(emptyBuffer);
        assertEquals(0, escapedEmpty.length());

        Buffer unescapedEmpty = EscapeUtils.unescape(emptyBuffer);
        assertEquals(0, unescapedEmpty.length());

        // 测试空字节数组
        byte[] emptyArray = new byte[0];
        Buffer escapedEmptyArray = EscapeUtils.escape(emptyArray);
        assertEquals(0, escapedEmptyArray.length());

        Buffer unescapedEmptyArray = EscapeUtils.unescape(emptyArray);
        assertEquals(0, unescapedEmptyArray.length());

        // 测试null字节数组
        Buffer escapedNullArray = EscapeUtils.escape((byte[]) null);
        assertNotNull(escapedNullArray);
        assertEquals(0, escapedNullArray.length());

        Buffer unescapedNullArray = EscapeUtils.unescape((byte[]) null);
        assertNotNull(unescapedNullArray);
        assertEquals(0, unescapedNullArray.length());
    }

    @Test
    @DisplayName("测试是否需要转义检查")
    void testNeedsEscape() {
        // 测试需要转义的数据
        Buffer needsEscapeBuffer = Buffer.buffer(new byte[]{0x01, 0x7E, 0x02});
        assertTrue(EscapeUtils.needsEscape(needsEscapeBuffer));

        Buffer needsEscapeBuffer2 = Buffer.buffer(new byte[]{0x01, 0x7D, 0x02});
        assertTrue(EscapeUtils.needsEscape(needsEscapeBuffer2));

        byte[] needsEscapeArray = {0x01, 0x7E, 0x02};
        assertTrue(EscapeUtils.needsEscape(needsEscapeArray));

        byte[] needsEscapeArray2 = {0x01, 0x7D, 0x02};
        assertTrue(EscapeUtils.needsEscape(needsEscapeArray2));

        // 测试不需要转义的数据
        Buffer noEscapeBuffer = Buffer.buffer(new byte[]{0x01, 0x02, 0x03});
        assertFalse(EscapeUtils.needsEscape(noEscapeBuffer));

        byte[] noEscapeArray = {0x01, 0x02, 0x03};
        assertFalse(EscapeUtils.needsEscape(noEscapeArray));

        // 测试空数据
        assertFalse(EscapeUtils.needsEscape((Buffer) null));
        assertFalse(EscapeUtils.needsEscape(Buffer.buffer()));
        assertFalse(EscapeUtils.needsEscape((byte[]) null));
        assertFalse(EscapeUtils.needsEscape(new byte[0]));
    }

    @Test
    @DisplayName("测试转义字节计数")
    void testCountEscapeBytes() {
        // 测试包含转义字符的数据
        Buffer buffer = Buffer.buffer(new byte[]{0x01, 0x7E, 0x02, 0x7D, 0x03});
        assertEquals(2, EscapeUtils.countEscapeBytes(buffer));

        byte[] array = {0x01, 0x7E, 0x02, 0x7D, 0x03};
        assertEquals(2, EscapeUtils.countEscapeBytes(array));

        // 测试不包含转义字符的数据
        Buffer noEscapeBuffer = Buffer.buffer(new byte[]{0x01, 0x02, 0x03});
        assertEquals(0, EscapeUtils.countEscapeBytes(noEscapeBuffer));

        byte[] noEscapeArray = {0x01, 0x02, 0x03};
        assertEquals(0, EscapeUtils.countEscapeBytes(noEscapeArray));

        // 测试空数据
        assertEquals(0, EscapeUtils.countEscapeBytes((Buffer) null));
        assertEquals(0, EscapeUtils.countEscapeBytes(Buffer.buffer()));
        assertEquals(0, EscapeUtils.countEscapeBytes((byte[]) null));
        assertEquals(0, EscapeUtils.countEscapeBytes(new byte[0]));
    }

    @Test
    @DisplayName("测试转义后长度计算")
    void testCalculateEscapedLength() {
        assertEquals(10, EscapeUtils.calculateEscapedLength(8, 2));
        assertEquals(5, EscapeUtils.calculateEscapedLength(5, 0));
        assertEquals(0, EscapeUtils.calculateEscapedLength(0, 0));
    }

    @Test
    @DisplayName("测试转义数据验证 - 有效数据")
    void testValidateEscapedDataValid() {
        // 测试有效的转义数据
        Buffer validEscaped = Buffer.buffer(new byte[]{0x01, 0x7D, 0x02, 0x03, 0x7D, 0x01, 0x04});
        EscapeValidationResult result = EscapeUtils.validateEscapedData(validEscaped);
        assertTrue(result.isValid());
        assertEquals("转义数据验证通过", result.getMessage());

        // 测试字节数组
        byte[] validEscapedArray = {0x01, 0x7D, 0x02, 0x03, 0x7D, 0x01, 0x04};
        EscapeValidationResult arrayResult = EscapeUtils.validateEscapedData(validEscapedArray);
        assertTrue(arrayResult.isValid());

        // 测试空数据
        EscapeValidationResult nullResult = EscapeUtils.validateEscapedData((Buffer) null);
        assertTrue(nullResult.isValid());
        assertEquals("数据为空", nullResult.getMessage());

        EscapeValidationResult emptyResult = EscapeUtils.validateEscapedData(new byte[0]);
        assertTrue(emptyResult.isValid());
        assertEquals("数据为空", emptyResult.getMessage());
    }

    @Test
    @DisplayName("测试转义数据验证 - 无效数据")
    void testValidateEscapedDataInvalid() {
        // 测试包含未转义标识位的数据
        Buffer invalidEscaped1 = Buffer.buffer(new byte[]{0x01, 0x7E, 0x02});
        EscapeValidationResult result1 = EscapeUtils.validateEscapedData(invalidEscaped1);
        assertFalse(result1.isValid());
        assertTrue(result1.getMessage().contains("未转义的标识位"));

        // 测试不完整的转义序列
        Buffer invalidEscaped2 = Buffer.buffer(new byte[]{0x01, 0x7D});
        EscapeValidationResult result2 = EscapeUtils.validateEscapedData(invalidEscaped2);
        assertFalse(result2.isValid());
        assertTrue(result2.getMessage().contains("转义序列不完整"));

        // 测试无效的转义序列
        Buffer invalidEscaped3 = Buffer.buffer(new byte[]{0x01, 0x7D, 0x03});
        EscapeValidationResult result3 = EscapeUtils.validateEscapedData(invalidEscaped3);
        assertFalse(result3.isValid());
        assertTrue(result3.getMessage().contains("转义序列无效"));
    }

    @Test
    @DisplayName("测试EscapeValidationResult的toString方法")
    void testEscapeValidationResultToString() {
        EscapeValidationResult validResult = new EscapeValidationResult(true, "验证成功");
        String validString = validResult.toString();
        assertTrue(validString.contains("valid=true"));
        assertTrue(validString.contains("验证成功"));

        EscapeValidationResult invalidResult = new EscapeValidationResult(false, "验证失败");
        String invalidString = invalidResult.toString();
        assertTrue(invalidString.contains("valid=false"));
        assertTrue(invalidString.contains("验证失败"));
    }

    @Test
    @DisplayName("测试复杂转义场景")
    void testComplexEscapeScenarios() {
        // 测试连续的转义字符
        Buffer original = Buffer.buffer(new byte[]{0x7E, 0x7E, 0x7D, 0x7D});
        Buffer escaped = EscapeUtils.escape(original);
        Buffer unescaped = EscapeUtils.unescape(escaped);
        assertArrayEquals(original.getBytes(), unescaped.getBytes());

        // 验证转义后的数据
        EscapeValidationResult validation = EscapeUtils.validateEscapedData(escaped);
        assertTrue(validation.isValid());

        // 测试转义字节计数
        assertEquals(4, EscapeUtils.countEscapeBytes(original));

        // 测试转义后长度
        int expectedLength = EscapeUtils.calculateEscapedLength(original.length(),
                EscapeUtils.countEscapeBytes(original));
        assertEquals(expectedLength, escaped.length());
    }
}