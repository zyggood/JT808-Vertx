package com.jt808.protocol.message;

import com.jt808.protocol.factory.JT808MessageFactory;
import io.vertx.core.buffer.Buffer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * T8400PhoneCallback 电话回拨消息测试
 */
class T8400PhoneCallbackTest {

    @Test
    void testMessageId() {
        T8400PhoneCallback message = new T8400PhoneCallback();
        assertEquals(0x8400, message.getMessageId());
        assertEquals(0x8400, T8400PhoneCallback.MESSAGE_ID);
    }

    @Test
    void testDefaultConstructor() {
        T8400PhoneCallback message = new T8400PhoneCallback();
        assertNotNull(message);
        assertEquals(0x8400, message.getMessageId());
    }

    @Test
    void testHeaderConstructor() {
        JT808Header header = new JT808Header();
        T8400PhoneCallback message = new T8400PhoneCallback(header);
        assertNotNull(message);
        assertEquals(header, message.getHeader());
        assertEquals(0x8400, message.getMessageId());
    }

    @Test
    void testParameterConstructor() {
        byte flag = T8400PhoneCallback.CallFlag.NORMAL_CALL;
        String phoneNumber = "13800138000";
        
        T8400PhoneCallback message = new T8400PhoneCallback(flag, phoneNumber);
        assertEquals(flag, message.getFlag());
        assertEquals(phoneNumber, message.getPhoneNumber());
    }

    @Test
    void testCreateNormalCall() {
        String phoneNumber = "13800138000";
        T8400PhoneCallback message = T8400PhoneCallback.createNormalCall(phoneNumber);
        
        assertNotNull(message);
        assertEquals(T8400PhoneCallback.CallFlag.NORMAL_CALL, message.getFlag());
        assertEquals(phoneNumber, message.getPhoneNumber());
        assertTrue(message.isNormalCall());
        assertFalse(message.isMonitor());
    }

    @Test
    void testCreateMonitorCall() {
        String phoneNumber = "13800138000";
        T8400PhoneCallback message = T8400PhoneCallback.createMonitorCall(phoneNumber);
        
        assertNotNull(message);
        assertEquals(T8400PhoneCallback.CallFlag.MONITOR, message.getFlag());
        assertEquals(phoneNumber, message.getPhoneNumber());
        assertFalse(message.isNormalCall());
        assertTrue(message.isMonitor());
    }

    @Test
    void testEncodeDecodeNormalCall() {
        String phoneNumber = "13800138000";
        T8400PhoneCallback original = T8400PhoneCallback.createNormalCall(phoneNumber);
        
        // 编码
        Buffer encoded = original.encodeBody();
        assertNotNull(encoded);
        assertTrue(encoded.length() > 0);
        
        // 解码
        T8400PhoneCallback decoded = new T8400PhoneCallback();
        decoded.decodeBody(encoded);
        
        // 验证
        assertEquals(original.getFlag(), decoded.getFlag());
        assertEquals(original.getPhoneNumber(), decoded.getPhoneNumber());
        assertTrue(decoded.isNormalCall());
        assertFalse(decoded.isMonitor());
    }

    @Test
    void testEncodeDecodeMonitorCall() {
        String phoneNumber = "400-800-8000";
        T8400PhoneCallback original = T8400PhoneCallback.createMonitorCall(phoneNumber);
        
        // 编码
        Buffer encoded = original.encodeBody();
        assertNotNull(encoded);
        assertTrue(encoded.length() > 0);
        
        // 解码
        T8400PhoneCallback decoded = new T8400PhoneCallback();
        decoded.decodeBody(encoded);
        
        // 验证
        assertEquals(original.getFlag(), decoded.getFlag());
        assertEquals(original.getPhoneNumber(), decoded.getPhoneNumber());
        assertFalse(decoded.isNormalCall());
        assertTrue(decoded.isMonitor());
    }

    @Test
    void testEncodeDecodeConsistency() {
        // 测试多种电话号码格式
        String[] phoneNumbers = {
            "13800138000",
            "400-800-8000",
            "021-12345678",
            "95588",
            "10086",
            "12345678901234567890" // 20字节边界测试
        };
        
        byte[] flags = {
            T8400PhoneCallback.CallFlag.NORMAL_CALL,
            T8400PhoneCallback.CallFlag.MONITOR
        };
        
        for (String phoneNumber : phoneNumbers) {
            for (byte flag : flags) {
                T8400PhoneCallback original = new T8400PhoneCallback(flag, phoneNumber);
                
                // 编码
                Buffer encoded = original.encodeBody();
                
                // 解码
                T8400PhoneCallback decoded = new T8400PhoneCallback();
                decoded.decodeBody(encoded);
                
                // 验证
                assertEquals(original.getFlag(), decoded.getFlag(), 
                    "标志不匹配，电话号码: " + phoneNumber + ", 标志: " + flag);
                assertEquals(original.getPhoneNumber(), decoded.getPhoneNumber(), 
                    "电话号码不匹配，原始: " + phoneNumber + ", 标志: " + flag);
            }
        }
    }

    @Test
    void testEmptyPhoneNumber() {
        T8400PhoneCallback message = T8400PhoneCallback.createNormalCall("");
        
        // 编码
        Buffer encoded = message.encodeBody();
        assertEquals(1, encoded.length()); // 只有标志字节
        
        // 解码
        T8400PhoneCallback decoded = new T8400PhoneCallback();
        decoded.decodeBody(encoded);
        
        assertEquals(T8400PhoneCallback.CallFlag.NORMAL_CALL, decoded.getFlag());
        assertEquals("", decoded.getPhoneNumber());
    }

    @Test
    void testNullPhoneNumber() {
        T8400PhoneCallback message = new T8400PhoneCallback(T8400PhoneCallback.CallFlag.MONITOR, null);
        
        // 编码
        Buffer encoded = message.encodeBody();
        assertEquals(1, encoded.length()); // 只有标志字节
        
        // 解码
        T8400PhoneCallback decoded = new T8400PhoneCallback();
        decoded.decodeBody(encoded);
        
        assertEquals(T8400PhoneCallback.CallFlag.MONITOR, decoded.getFlag());
        assertEquals("", decoded.getPhoneNumber());
    }

    @Test
    void testMaxPhoneNumberLength() {
        // 测试20字节边界
        String phoneNumber = "12345678901234567890"; // 正好20字节
        T8400PhoneCallback message = T8400PhoneCallback.createNormalCall(phoneNumber);
        
        // 应该能正常编码
        Buffer encoded = message.encodeBody();
        assertEquals(21, encoded.length()); // 1字节标志 + 20字节电话号码
        
        // 解码验证
        T8400PhoneCallback decoded = new T8400PhoneCallback();
        decoded.decodeBody(encoded);
        assertEquals(phoneNumber, decoded.getPhoneNumber());
    }

    @Test
    void testPhoneNumberTooLong() {
        // 测试超过20字节的电话号码
        String phoneNumber = "123456789012345678901"; // 21字节
        T8400PhoneCallback message = T8400PhoneCallback.createNormalCall(phoneNumber);
        
        // 应该抛出异常
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
            message::encodeBody);
        assertTrue(exception.getMessage().contains("电话号码长度不能超过20字节"));
    }

    @Test
    void testGBKEncoding() {
        // 测试包含中文字符的电话号码（虽然实际不太可能）
        String phoneNumber = "电话123"; // GBK编码会占用更多字节
        T8400PhoneCallback message = T8400PhoneCallback.createNormalCall(phoneNumber);
        
        // 编码
        Buffer encoded = message.encodeBody();
        
        // 解码
        T8400PhoneCallback decoded = new T8400PhoneCallback();
        decoded.decodeBody(encoded);
        
        assertEquals(phoneNumber, decoded.getPhoneNumber());
    }

    @Test
    void testCallFlagConstants() {
        assertEquals(0x00, T8400PhoneCallback.CallFlag.NORMAL_CALL);
        assertEquals(0x01, T8400PhoneCallback.CallFlag.MONITOR);
    }

    @Test
    void testFlagJudgmentMethods() {
        T8400PhoneCallback normalCall = T8400PhoneCallback.createNormalCall("13800138000");
        assertTrue(normalCall.isNormalCall());
        assertFalse(normalCall.isMonitor());
        
        T8400PhoneCallback monitorCall = T8400PhoneCallback.createMonitorCall("13800138000");
        assertFalse(monitorCall.isNormalCall());
        assertTrue(monitorCall.isMonitor());
    }

    @Test
    void testFlagDescription() {
        T8400PhoneCallback normalCall = T8400PhoneCallback.createNormalCall("13800138000");
        assertEquals("普通通话", normalCall.getFlagDescription());
        
        T8400PhoneCallback monitorCall = T8400PhoneCallback.createMonitorCall("13800138000");
        assertEquals("监听", monitorCall.getFlagDescription());
        
        // 测试未知标志
        T8400PhoneCallback unknownFlag = new T8400PhoneCallback((byte) 0x99, "13800138000");
        assertTrue(unknownFlag.getFlagDescription().contains("未知标志"));
        assertTrue(unknownFlag.getFlagDescription().contains("153"));
    }

    @Test
    void testGetFlagUnsigned() {
        T8400PhoneCallback normalCall = T8400PhoneCallback.createNormalCall("13800138000");
        assertEquals(0, normalCall.getFlagUnsigned());
        
        T8400PhoneCallback monitorCall = T8400PhoneCallback.createMonitorCall("13800138000");
        assertEquals(1, monitorCall.getFlagUnsigned());
        
        // 测试负数标志的无符号转换
        T8400PhoneCallback negativeFlag = new T8400PhoneCallback((byte) 0xFF, "13800138000");
        assertEquals(255, negativeFlag.getFlagUnsigned());
    }

    @Test
    void testToString() {
        T8400PhoneCallback message = T8400PhoneCallback.createNormalCall("13800138000");
        String str = message.toString();
        
        assertTrue(str.contains("T8400PhoneCallback"));
        assertTrue(str.contains("普通通话"));
        assertTrue(str.contains("13800138000"));
        assertTrue(str.contains("8400"));
    }

    @Test
    void testEquals() {
        T8400PhoneCallback message1 = T8400PhoneCallback.createNormalCall("13800138000");
        T8400PhoneCallback message2 = T8400PhoneCallback.createNormalCall("13800138000");
        T8400PhoneCallback message3 = T8400PhoneCallback.createMonitorCall("13800138000");
        T8400PhoneCallback message4 = T8400PhoneCallback.createNormalCall("13800138001");
        
        assertEquals(message1, message2);
        assertNotEquals(message1, message3);
        assertNotEquals(message1, message4);
        assertNotEquals(message1, null);
        assertNotEquals(message1, "not a message");
    }

    @Test
    void testHashCode() {
        T8400PhoneCallback message1 = T8400PhoneCallback.createNormalCall("13800138000");
        T8400PhoneCallback message2 = T8400PhoneCallback.createNormalCall("13800138000");
        
        assertEquals(message1.hashCode(), message2.hashCode());
    }

    @Test
    void testGettersAndSetters() {
        T8400PhoneCallback message = new T8400PhoneCallback();
        
        // 测试标志
        message.setFlag(T8400PhoneCallback.CallFlag.MONITOR);
        assertEquals(T8400PhoneCallback.CallFlag.MONITOR, message.getFlag());
        
        // 测试电话号码
        String phoneNumber = "13800138000";
        message.setPhoneNumber(phoneNumber);
        assertEquals(phoneNumber, message.getPhoneNumber());
    }

    @Test
    void testDecodeInvalidBody() {
        T8400PhoneCallback message = new T8400PhoneCallback();
        
        // 测试null body
        IllegalArgumentException exception1 = assertThrows(IllegalArgumentException.class, 
            () -> message.decodeBody(null));
        assertTrue(exception1.getMessage().contains("电话回拨消息体长度至少为1字节"));
        
        // 测试空body
        IllegalArgumentException exception2 = assertThrows(IllegalArgumentException.class, 
            () -> message.decodeBody(Buffer.buffer()));
        assertTrue(exception2.getMessage().contains("电话回拨消息体长度至少为1字节"));
    }

    @Test
    void testMessageFactoryCreation() {
        JT808MessageFactory factory = JT808MessageFactory.getInstance();
        
        // 测试工厂是否支持T8400消息
        assertTrue(factory.isSupported(0x8400));
        
        // 测试创建消息
        JT808Message message = factory.createMessage(0x8400);
        assertNotNull(message);
        assertInstanceOf(T8400PhoneCallback.class, message);
        assertEquals(0x8400, message.getMessageId());
    }

    @Test
    void testMessageFactorySupport() {
        JT808MessageFactory factory = JT808MessageFactory.getInstance();
        assertTrue(factory.getSupportedMessageIds().contains(0x8400));
    }

    @Test
    void testRealWorldScenarios() {
        // 场景1：紧急呼叫
        T8400PhoneCallback emergencyCall = T8400PhoneCallback.createNormalCall("110");
        Buffer encoded1 = emergencyCall.encodeBody();
        T8400PhoneCallback decoded1 = new T8400PhoneCallback();
        decoded1.decodeBody(encoded1);
        assertEquals("110", decoded1.getPhoneNumber());
        assertTrue(decoded1.isNormalCall());
        
        // 场景2：客服热线监听
        T8400PhoneCallback serviceMonitor = T8400PhoneCallback.createMonitorCall("400-800-8000");
        Buffer encoded2 = serviceMonitor.encodeBody();
        T8400PhoneCallback decoded2 = new T8400PhoneCallback();
        decoded2.decodeBody(encoded2);
        assertEquals("400-800-8000", decoded2.getPhoneNumber());
        assertTrue(decoded2.isMonitor());
        
        // 场景3：手机号码回拨
        T8400PhoneCallback mobileCallback = T8400PhoneCallback.createNormalCall("13800138000");
        Buffer encoded3 = mobileCallback.encodeBody();
        T8400PhoneCallback decoded3 = new T8400PhoneCallback();
        decoded3.decodeBody(encoded3);
        assertEquals("13800138000", decoded3.getPhoneNumber());
        assertTrue(decoded3.isNormalCall());
    }
}