package com.jt808.protocol.message;

import io.vertx.core.buffer.Buffer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * T8100终端注册应答消息测试类
 */
@DisplayName("T8100终端注册应答消息测试")
class T8100TerminalRegisterResponseTest {

    private T8100TerminalRegisterResponse message;

    @BeforeEach
    void setUp() {
        message = new T8100TerminalRegisterResponse();
    }

    @Test
    @DisplayName("测试消息ID")
    void testMessageId() {
        assertEquals(0x8100, message.getMessageId());
    }

    @Test
    @DisplayName("测试构造函数")
    void testConstructors() {
        // 测试默认构造函数
        T8100TerminalRegisterResponse msg1 = new T8100TerminalRegisterResponse();
        assertNotNull(msg1);
        assertEquals(0x8100, msg1.getMessageId());

        // 测试带Header的构造函数
        JT808Header header = new JT808Header();
        T8100TerminalRegisterResponse msg2 = new T8100TerminalRegisterResponse(header);
        assertNotNull(msg2);
        assertEquals(header, msg2.getHeader());

        // 测试带参数的构造函数
        T8100TerminalRegisterResponse msg3 = new T8100TerminalRegisterResponse(12345, (byte) 0x00, "AUTH123456");
        assertEquals(12345, msg3.getResponseSerialNumber());
        assertEquals((byte) 0x00, msg3.getResult());
        assertEquals("AUTH123456", msg3.getAuthCode());
    }

    @Test
    @DisplayName("测试字段设置和获取")
    void testFieldsSettersAndGetters() {
        // 测试应答流水号
        message.setResponseSerialNumber(54321);
        assertEquals(54321, message.getResponseSerialNumber());

        // 测试结果
        message.setResult(T8100TerminalRegisterResponse.RESULT_SUCCESS);
        assertEquals(T8100TerminalRegisterResponse.RESULT_SUCCESS, message.getResult());

        // 测试鉴权码
        message.setAuthCode("TESTAUTH");
        assertEquals("TESTAUTH", message.getAuthCode());
    }

    @Test
    @DisplayName("测试成功注册应答的编码和解码")
    void testSuccessResponseEncodingDecoding() {
        // 创建成功的注册应答
        T8100TerminalRegisterResponse original = new T8100TerminalRegisterResponse(
                12345, T8100TerminalRegisterResponse.RESULT_SUCCESS, "AUTH123456");

        // 编码
        Buffer encoded = original.encodeBody();
        assertNotNull(encoded);
        assertTrue(encoded.length() > 3); // 至少包含流水号(2字节) + 结果(1字节) + 鉴权码

        // 解码
        T8100TerminalRegisterResponse decoded = new T8100TerminalRegisterResponse();
        decoded.decodeBody(encoded);

        // 验证解码结果
        assertEquals(original.getResponseSerialNumber(), decoded.getResponseSerialNumber());
        assertEquals(original.getResult(), decoded.getResult());
        assertEquals(original.getAuthCode(), decoded.getAuthCode());
    }

    @Test
    @DisplayName("测试失败注册应答的编码和解码")
    void testFailureResponseEncodingDecoding() {
        // 创建失败的注册应答
        T8100TerminalRegisterResponse original = new T8100TerminalRegisterResponse(
                54321, T8100TerminalRegisterResponse.RESULT_VEHICLE_REGISTERED, null);

        // 编码
        Buffer encoded = original.encodeBody();
        assertNotNull(encoded);
        assertEquals(3, encoded.length()); // 流水号(2字节) + 结果(1字节)

        // 解码
        T8100TerminalRegisterResponse decoded = new T8100TerminalRegisterResponse();
        decoded.decodeBody(encoded);

        // 验证解码结果
        assertEquals(original.getResponseSerialNumber(), decoded.getResponseSerialNumber());
        assertEquals(original.getResult(), decoded.getResult());
        assertNull(decoded.getAuthCode());
    }

    @Test
    @DisplayName("测试所有结果类型的编码解码")
    void testAllResultTypesEncodingDecoding() {
        byte[] results = {
                T8100TerminalRegisterResponse.RESULT_SUCCESS,
                T8100TerminalRegisterResponse.RESULT_VEHICLE_REGISTERED,
                T8100TerminalRegisterResponse.RESULT_VEHICLE_NOT_IN_DATABASE,
                T8100TerminalRegisterResponse.RESULT_TERMINAL_REGISTERED,
                T8100TerminalRegisterResponse.RESULT_TERMINAL_NOT_IN_DATABASE
        };

        for (byte result : results) {
            T8100TerminalRegisterResponse original = new T8100TerminalRegisterResponse(
                    1000, result, result == T8100TerminalRegisterResponse.RESULT_SUCCESS ? "AUTH" : null);

            Buffer encoded = original.encodeBody();
            T8100TerminalRegisterResponse decoded = new T8100TerminalRegisterResponse();
            decoded.decodeBody(encoded);

            assertEquals(original.getResponseSerialNumber(), decoded.getResponseSerialNumber());
            assertEquals(original.getResult(), decoded.getResult());
            assertEquals(original.getAuthCode(), decoded.getAuthCode());
        }
    }

    @Test
    @DisplayName("测试结果描述")
    void testResultDescription() {
        message.setResult(T8100TerminalRegisterResponse.RESULT_SUCCESS);
        assertEquals("成功", message.getResultDescription());

        message.setResult(T8100TerminalRegisterResponse.RESULT_VEHICLE_REGISTERED);
        assertEquals("车辆已被注册", message.getResultDescription());

        message.setResult(T8100TerminalRegisterResponse.RESULT_VEHICLE_NOT_IN_DATABASE);
        assertEquals("数据库中无该车辆", message.getResultDescription());

        message.setResult(T8100TerminalRegisterResponse.RESULT_TERMINAL_REGISTERED);
        assertEquals("终端已被注册", message.getResultDescription());

        message.setResult(T8100TerminalRegisterResponse.RESULT_TERMINAL_NOT_IN_DATABASE);
        assertEquals("数据库中无该终端", message.getResultDescription());

        // 测试未知结果
        message.setResult((byte) 0xFF);
        assertEquals("未知结果(255)", message.getResultDescription());
    }

    @Test
    @DisplayName("测试成功判断")
    void testIsSuccess() {
        message.setResult(T8100TerminalRegisterResponse.RESULT_SUCCESS);
        assertTrue(message.isSuccess());

        message.setResult(T8100TerminalRegisterResponse.RESULT_VEHICLE_REGISTERED);
        assertFalse(message.isSuccess());

        message.setResult(T8100TerminalRegisterResponse.RESULT_TERMINAL_NOT_IN_DATABASE);
        assertFalse(message.isSuccess());
    }

    @Test
    @DisplayName("测试静态工厂方法")
    void testStaticFactoryMethods() {
        // 测试创建成功应答
        T8100TerminalRegisterResponse successResponse = 
                T8100TerminalRegisterResponse.createSuccessResponse(12345, "AUTH123");
        assertNotNull(successResponse);
        assertEquals(12345, successResponse.getResponseSerialNumber());
        assertEquals(T8100TerminalRegisterResponse.RESULT_SUCCESS, successResponse.getResult());
        assertEquals("AUTH123", successResponse.getAuthCode());
        assertTrue(successResponse.isSuccess());

        // 测试创建失败应答
        T8100TerminalRegisterResponse failureResponse = 
                T8100TerminalRegisterResponse.createFailureResponse(54321, T8100TerminalRegisterResponse.RESULT_VEHICLE_REGISTERED);
        assertNotNull(failureResponse);
        assertEquals(54321, failureResponse.getResponseSerialNumber());
        assertEquals(T8100TerminalRegisterResponse.RESULT_VEHICLE_REGISTERED, failureResponse.getResult());
        assertNull(failureResponse.getAuthCode());
        assertFalse(failureResponse.isSuccess());
    }

    @Test
    @DisplayName("测试空鉴权码处理")
    void testEmptyAuthCodeHandling() {
        // 测试空字符串鉴权码
        T8100TerminalRegisterResponse msg1 = new T8100TerminalRegisterResponse(
                1000, T8100TerminalRegisterResponse.RESULT_SUCCESS, "");
        Buffer encoded1 = msg1.encodeBody();
        assertEquals(3, encoded1.length()); // 不应包含鉴权码

        // 测试null鉴权码
        T8100TerminalRegisterResponse msg2 = new T8100TerminalRegisterResponse(
                1000, T8100TerminalRegisterResponse.RESULT_SUCCESS, null);
        Buffer encoded2 = msg2.encodeBody();
        assertEquals(3, encoded2.length()); // 不应包含鉴权码
    }

    @Test
    @DisplayName("测试边界值")
    void testBoundaryValues() {
        // 测试最大流水号
        message.setResponseSerialNumber(65535);
        assertEquals(65535, message.getResponseSerialNumber());

        // 测试最小流水号
        message.setResponseSerialNumber(0);
        assertEquals(0, message.getResponseSerialNumber());

        // 测试长鉴权码
        String longAuthCode = "A".repeat(100);
        message.setAuthCode(longAuthCode);
        assertEquals(longAuthCode, message.getAuthCode());
    }

    @Test
    @DisplayName("测试toString方法")
    void testToString() {
        message.setResponseSerialNumber(12345);
        message.setResult(T8100TerminalRegisterResponse.RESULT_SUCCESS);
        message.setAuthCode("AUTH123");

        String result = message.toString();
        assertNotNull(result);
        assertTrue(result.contains("T8100TerminalRegisterResponse"));
        assertTrue(result.contains("12345"));
        assertTrue(result.contains("成功"));
        assertTrue(result.contains("AUTH123"));
    }

    @Test
    @DisplayName("测试消息体长度计算")
    void testMessageBodyLength() {
        // 成功应答（包含鉴权码）
        T8100TerminalRegisterResponse successMsg = new T8100TerminalRegisterResponse(
                1000, T8100TerminalRegisterResponse.RESULT_SUCCESS, "AUTH123456");
        Buffer successEncoded = successMsg.encodeBody();
        assertEquals(3 + "AUTH123456".length(), successEncoded.length());

        // 失败应答（不包含鉴权码）
        T8100TerminalRegisterResponse failureMsg = new T8100TerminalRegisterResponse(
                1000, T8100TerminalRegisterResponse.RESULT_VEHICLE_REGISTERED, null);
        Buffer failureEncoded = failureMsg.encodeBody();
        assertEquals(3, failureEncoded.length());
    }

    @Test
    @DisplayName("测试中文鉴权码")
    void testChineseAuthCode() {
        T8100TerminalRegisterResponse original = new T8100TerminalRegisterResponse(
                1000, T8100TerminalRegisterResponse.RESULT_SUCCESS, "测试鉴权码");

        Buffer encoded = original.encodeBody();
        T8100TerminalRegisterResponse decoded = new T8100TerminalRegisterResponse();
        decoded.decodeBody(encoded);

        assertEquals(original.getResponseSerialNumber(), decoded.getResponseSerialNumber());
        assertEquals(original.getResult(), decoded.getResult());
        assertEquals(original.getAuthCode(), decoded.getAuthCode());
    }
}