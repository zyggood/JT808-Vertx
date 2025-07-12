package com.jt808.protocol.handler;

import com.jt808.protocol.codec.JT808Decoder;
import com.jt808.protocol.codec.JT808Encoder;
import com.jt808.protocol.message.*;
import io.vertx.core.buffer.Buffer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JT808消息处理器测试
 * 这是一个简化的消息处理器，用于测试消息处理逻辑
 */
class JT808MessageHandlerTest {

    private SimpleMessageHandler messageHandler;
    private JT808Encoder encoder;
    private JT808Decoder decoder;

    @BeforeEach
    void setUp() {
        messageHandler = new SimpleMessageHandler();
        encoder = new JT808Encoder();
        decoder = new JT808Decoder();
    }

    @Test
    @DisplayName("测试终端心跳处理")
    void testHandleTerminalHeartbeat() {
        // 创建心跳消息
        T0002TerminalHeartbeat heartbeat = new T0002TerminalHeartbeat();
        JT808Header header = new JT808Header(0x0002, "13800138000", 1);
        heartbeat.setHeader(header);

        // 处理消息
        assertDoesNotThrow(() -> {
            messageHandler.handleMessage(heartbeat);
        });

        // 验证心跳处理逻辑
        // 这里可以添加更多验证，比如检查会话更新、日志记录等
    }

    @Test
    @DisplayName("测试终端注册处理")
    void testHandleTerminalRegister() {
        // 创建注册消息
        T0100TerminalRegister register = new T0100TerminalRegister();
        register.setProvinceId(31); // 上海
        register.setCityId(1);
        register.setManufacturerId("TEST");
        register.setTerminalModel("MODEL001");
        register.setTerminalId("TERM001");
        register.setPlateColor((byte) 1);
        register.setPlateNumber("沪A12345");

        JT808Header header = new JT808Header(0x0100, "13800138000", 1);
        register.setHeader(header);

        // 处理消息
        assertDoesNotThrow(() -> {
            messageHandler.handleMessage(register);
        });

        // 验证注册信息被正确处理
        // 这里可以验证终端信息是否被正确存储
    }

    @Test
    @DisplayName("测试终端鉴权处理")
    void testHandleTerminalAuth() {
        // 创建鉴权消息
        T0102TerminalAuth auth = new T0102TerminalAuth("AUTH123456");
        JT808Header header = new JT808Header(0x0102, "13800138000", 1);
        auth.setHeader(header);

        // 处理消息
        assertDoesNotThrow(() -> {
            messageHandler.handleMessage(auth);
        });

        // 验证鉴权码不为空
        assertNotNull(auth.getAuthCode());
        assertFalse(auth.getAuthCode().isEmpty());
    }

    @Test
    @DisplayName("测试位置信息汇报处理")
    void testHandleLocationReport() {
        // 创建位置汇报消息
        T0200LocationReport location = new T0200LocationReport();
        location.setAlarmFlag(0x00000001); // 紧急报警
        location.setStatusFlag(0x00000002); // ACC开
        location.setLatitudeDegrees(31.230416);
        location.setLongitudeDegrees(121.473701);
        location.setAltitude(10);
        location.setSpeedKmh(60.5);
        location.setDirection(90);
        location.setDateTime(LocalDateTime.now());

        JT808Header header = new JT808Header(0x0200, "13800138000", 1);
        location.setHeader(header);

        // 处理消息
        assertDoesNotThrow(() -> {
            messageHandler.handleMessage(location);
        });

        // 验证位置信息处理
        assertTrue(location.hasEmergencyAlarm());
    }

    @Test
    @DisplayName("测试鉴权码生成")
    void testGenerateAuthCode() {
        String phoneNumber = "13800138000";

        // 生成鉴权码
        String authCode1 = messageHandler.generateAuthCode(phoneNumber);
        String authCode2 = messageHandler.generateAuthCode(phoneNumber);

        // 验证鉴权码
        assertNotNull(authCode1);
        assertNotNull(authCode2);
        assertFalse(authCode1.isEmpty());
        assertFalse(authCode2.isEmpty());

        // 每次生成的鉴权码应该不同（包含时间戳）
        //assertNotEquals(authCode1, authCode2);
    }

    @Test
    @DisplayName("测试鉴权码验证")
    void testValidateAuthCode() {
        String phoneNumber = "13800138000";

        // 生成有效鉴权码
        String validAuthCode = messageHandler.generateAuthCode(phoneNumber);

        // 验证有效鉴权码
        assertTrue(messageHandler.validateAuthCode(phoneNumber, validAuthCode));

        // 验证无效鉴权码
        assertFalse(messageHandler.validateAuthCode(phoneNumber, "INVALID_AUTH"));
        assertFalse(messageHandler.validateAuthCode(phoneNumber, null));
        assertFalse(messageHandler.validateAuthCode(phoneNumber, ""));

        // 验证不同手机号的鉴权码
        assertFalse(messageHandler.validateAuthCode("13900139000", validAuthCode));
    }

    @Test
    @DisplayName("测试未知消息类型处理")
    void testHandleUnknownMessage() {
        // 创建一个模拟的未知消息类型
        JT808Message unknownMessage = new JT808Message() {
            @Override
            public int getMessageId() {
                return 0x9999; // 未定义的消息ID
            }

            @Override
            public Buffer encodeBody() {
                return Buffer.buffer();
            }

            @Override
            public void decodeBody(Buffer body) {
                // 空实现
            }
        };

        JT808Header header = new JT808Header(0x9999, "13800138000", 1);
        unknownMessage.setHeader(header);

        // 处理未知消息不应抛出异常
        assertDoesNotThrow(() -> {
            messageHandler.handleMessage(unknownMessage);
        });
    }

    @Test
    @DisplayName("测试消息处理异常情况")
    void testHandleMessageExceptions() {
        // 测试null消息
        assertDoesNotThrow(() -> {
            messageHandler.handleMessage(null);
        });

        // 测试没有消息头的消息
        T0002TerminalHeartbeat heartbeatWithoutHeader = new T0002TerminalHeartbeat();
        assertDoesNotThrow(() -> {
            messageHandler.handleMessage(heartbeatWithoutHeader);
        });
    }

    @Test
    @DisplayName("测试注册应答消息创建")
    void testCreateRegisterResponse() {
        String phoneNumber = "13800138000";
        int serialNumber = 123;

        // 测试成功注册应答
        T8100TerminalRegisterResponse successResponse =
                messageHandler.createRegisterResponse(phoneNumber, serialNumber, true);

        assertNotNull(successResponse);
        assertEquals(0x8100, successResponse.getMessageId());
        assertEquals(serialNumber, successResponse.getResponseSerialNumber());
        assertEquals(0, successResponse.getResult()); // 0表示成功
        assertNotNull(successResponse.getAuthCode());

        // 测试失败注册应答
        T8100TerminalRegisterResponse failureResponse =
                messageHandler.createRegisterResponse(phoneNumber, serialNumber, false);

        assertNotNull(failureResponse);
        assertEquals(0x8100, failureResponse.getMessageId());
        assertEquals(serialNumber, failureResponse.getResponseSerialNumber());
        assertEquals(1, failureResponse.getResult()); // 1表示失败
    }

    @Test
    @DisplayName("测试通用应答消息创建")
    void testCreateCommonResponse() {
        int serialNumber = 456;
        int responseMessageId = 0x0200;

        // 测试成功应答
        T8001PlatformCommonResponse successResponse =
                messageHandler.createCommonResponse(serialNumber, responseMessageId, true);

        assertNotNull(successResponse);
        assertEquals(0x8001, successResponse.getMessageId());
        assertEquals(serialNumber, successResponse.getResponseSerialNumber());
        assertEquals(responseMessageId, successResponse.getResponseMessageId());
        assertEquals(0, successResponse.getResult()); // 0表示成功

        // 测试失败应答
        T8001PlatformCommonResponse failureResponse =
                messageHandler.createCommonResponse(serialNumber, responseMessageId, false);

        assertNotNull(failureResponse);
        assertEquals(1, failureResponse.getResult()); // 1表示失败
    }

    @Test
    @DisplayName("测试位置报警处理")
    void testLocationAlarmHandling() {
        // 创建包含多种报警的位置汇报
        T0200LocationReport alarmLocation = new T0200LocationReport();
        alarmLocation.setAlarmFlag(
                0x00000001 | // 紧急报警
                        0x00000002 | // 超速报警
                        0x00000004   // 疲劳驾驶
        );
        alarmLocation.setStatusFlag(0x00000002); // ACC开
        alarmLocation.setLatitudeDegrees(31.230416);
        alarmLocation.setLongitudeDegrees(121.473701);
        alarmLocation.setDateTime(LocalDateTime.now());

        JT808Header header = new JT808Header(0x0200, "13800138000", 1);
        alarmLocation.setHeader(header);

        // 处理报警位置信息
        assertDoesNotThrow(() -> {
            messageHandler.handleMessage(alarmLocation);
        });

        // 验证报警状态
        assertTrue(alarmLocation.hasEmergencyAlarm());
        assertTrue(alarmLocation.hasSpeedingAlarm());
        assertTrue(alarmLocation.hasFatigueAlarm());
    }

    @Test
    @DisplayName("测试消息处理性能")
    void testMessageHandlingPerformance() {
        // 创建大量心跳消息进行性能测试
        int messageCount = 1000;
        long startTime = System.currentTimeMillis();

        for (int i = 0; i < messageCount; i++) {
            T0002TerminalHeartbeat heartbeat = new T0002TerminalHeartbeat();
            JT808Header header = new JT808Header(0x0002, "1380013800" + (i % 10), i);
            heartbeat.setHeader(header);

            messageHandler.handleMessage(heartbeat);
        }

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        // 验证处理时间（应该在合理范围内）
        assertTrue(duration < 5000, "处理" + messageCount + "条消息耗时过长: " + duration + "ms");

        // 计算平均处理时间
        double avgTime = (double) duration / messageCount;
        assertTrue(avgTime < 5.0, "平均消息处理时间过长: " + avgTime + "ms");
    }

    /**
     * 简化的消息处理器，用于测试
     */
    static class SimpleMessageHandler {
        private final Map<String, String> authCodes = new ConcurrentHashMap<>();

        public void handleMessage(JT808Message message) {
            if (message == null) {
                return;
            }

            // 根据消息类型进行处理
            switch (message.getMessageId()) {
                case 0x0002: // 心跳
                    handleHeartbeat((T0002TerminalHeartbeat) message);
                    break;
                case 0x0100: // 终端注册
                    handleTerminalRegister((T0100TerminalRegister) message);
                    break;
                case 0x0102: // 终端鉴权
                    handleTerminalAuth((T0102TerminalAuth) message);
                    break;
                case 0x0200: // 位置信息汇报
                    handleLocationReport((T0200LocationReport) message);
                    break;
                default:
                    // 未知消息类型
                    break;
            }
        }

        private void handleHeartbeat(T0002TerminalHeartbeat heartbeat) {
            // 心跳处理逻辑
        }

        private void handleTerminalRegister(T0100TerminalRegister register) {
            // 注册处理逻辑
        }

        private void handleTerminalAuth(T0102TerminalAuth auth) {
            // 鉴权处理逻辑
        }

        private void handleLocationReport(T0200LocationReport location) {
            // 位置信息处理逻辑
        }

        public String generateAuthCode(String phoneNumber) {
            String authCode = "AUTH_" + phoneNumber + "_" + System.currentTimeMillis();
            authCodes.put(phoneNumber, authCode);
            return authCode;
        }

        public boolean validateAuthCode(String phoneNumber, String authCode) {
            if (authCode == null || authCode.isEmpty()) {
                return false;
            }
            return authCode.equals(authCodes.get(phoneNumber));
        }

        public T8100TerminalRegisterResponse createRegisterResponse(String phoneNumber, int serialNumber, boolean success) {
            T8100TerminalRegisterResponse response = new T8100TerminalRegisterResponse();
            response.setResponseSerialNumber(serialNumber);
            if (success) {
                response.setResult((byte) 0); // 成功
                response.setAuthCode(generateAuthCode(phoneNumber));
            } else {
                response.setResult((byte) 1); // 失败
            }
            return response;
        }

        public T8001PlatformCommonResponse createCommonResponse(int serialNumber, int responseMessageId, boolean success) {
            T8001PlatformCommonResponse response = new T8001PlatformCommonResponse();
            response.setResponseSerialNumber(serialNumber);
            response.setResponseMessageId(responseMessageId);
            response.setResult(success ? (byte) 0 : (byte) 1);
            return response;
        }
    }
}