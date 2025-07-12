package com.jt808.protocol.example;

import com.jt808.protocol.factory.JT808MessageFactory;
import com.jt808.protocol.message.JT808Header;
import com.jt808.protocol.message.JT808Message;
import com.jt808.protocol.message.T8100TerminalRegisterResponse;
import com.jt808.protocol.util.ChecksumUtils;
import com.jt808.protocol.util.EscapeUtils;
import io.vertx.core.buffer.Buffer;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.*;

/**
 * T8100终端注册应答消息使用示例
 * 演示0x8100消息的完整功能和实际应用场景
 */
public class T8100TerminalRegisterResponseExample {

    private static final Logger logger = LoggerFactory.getLogger(T8100TerminalRegisterResponseExample.class);

    @Test
    public void demonstrateT8100Usage() {
        logger.info("=== T8100终端注册应答消息功能演示 ===");

        // 1. 使用工厂创建消息
        logger.info("\n1. 使用工厂创建T8100消息");
        JT808MessageFactory factory = JT808MessageFactory.getInstance();
        JT808Message factoryMessage = factory.createMessage(0x8100);
        assertInstanceOf(T8100TerminalRegisterResponse.class, factoryMessage);
        logger.info("工厂创建成功: {}", factoryMessage.getClass().getSimpleName());

        // 2. 直接创建和设置消息数据
        logger.info("\n2. 创建成功的注册应答消息");
        T8100TerminalRegisterResponse successResponse = T8100TerminalRegisterResponse.createSuccessResponse(
                12345, "AUTH20241230001");

        logger.info("应答流水号: {}", successResponse.getResponseSerialNumber());
        logger.info("结果: {} ({})", successResponse.getResult(), successResponse.getResultDescription());
        logger.info("鉴权码: {}", successResponse.getAuthCode());
        logger.info("是否成功: {}", successResponse.isSuccess());

        // 3. 设置消息头并编码
        logger.info("\n3. 设置消息头并编码消息体");
        JT808Header header = new JT808Header();
        header.setMessageId(0x8100);
        header.setPhoneNumber("013912345678");
        header.setSerialNumber(1001);
        successResponse.setHeader(header);

        Buffer bodyBuffer = successResponse.encodeBody();
        logger.info("消息体编码结果: {} 字节", bodyBuffer.length());
        logger.info("消息体十六进制: {}", bytesToHex(bodyBuffer.getBytes()));

        // 4. 解析消息体字段
        logger.info("\n4. 解析消息体字段");
        logger.info("应答流水号 (前2字节): {}", bodyBuffer.getUnsignedShort(0));
        logger.info("结果 (第3字节): {}", bodyBuffer.getByte(2) & 0xFF);
        if (bodyBuffer.length() > 3) {
            String authCode = new String(bodyBuffer.getBytes(3, bodyBuffer.length()));
            logger.info("鉴权码 (剩余字节): {}", authCode);
        }

        // 5. 解码验证
        logger.info("\n5. 解码验证");
        T8100TerminalRegisterResponse decodedResponse = new T8100TerminalRegisterResponse();
        decodedResponse.decodeBody(bodyBuffer);

        assertEquals(successResponse.getResponseSerialNumber(), decodedResponse.getResponseSerialNumber());
        assertEquals(successResponse.getResult(), decodedResponse.getResult());
        assertEquals(successResponse.getAuthCode(), decodedResponse.getAuthCode());
        logger.info("解码验证成功: {}", decodedResponse);

        // 6. 完整消息编码
        logger.info("\n6. 完整消息编码");
        Buffer fullMessage = Buffer.buffer();
        fullMessage.appendByte((byte) 0x7E); // 起始符

        // 消息头 (简化处理，实际应用中使用编码器)
        Buffer headerBuffer = Buffer.buffer();
        headerBuffer.appendUnsignedShort(header.getMessageId());
        headerBuffer.appendUnsignedShort(bodyBuffer.length());
        headerBuffer.appendBytes(header.getPhoneNumber().getBytes());
        headerBuffer.appendUnsignedShort(header.getSerialNumber());
        fullMessage.appendBuffer(headerBuffer);

        // 消息体
        fullMessage.appendBuffer(bodyBuffer);

        // 校验码
        Buffer dataForChecksum = Buffer.buffer();
        dataForChecksum.appendBuffer(headerBuffer);
        dataForChecksum.appendBuffer(bodyBuffer);
        byte checksum = ChecksumUtils.calculateChecksum(dataForChecksum.getBytes());
        fullMessage.appendByte(checksum);

        fullMessage.appendByte((byte) 0x7E); // 结束符

        logger.info("完整消息长度: {} 字节", fullMessage.length());
        logger.info("完整消息十六进制: {}", bytesToHex(fullMessage.getBytes()));

        // 7. 校验码验证
        logger.info("\n7. 校验码验证");
        Buffer dataForVerification = Buffer.buffer();
        dataForVerification.appendBuffer(headerBuffer);
        dataForVerification.appendBuffer(bodyBuffer);
        byte calculatedChecksum = ChecksumUtils.calculateChecksum(dataForVerification.getBytes());
        logger.info("计算的校验码: 0x{}", String.format("%02X", calculatedChecksum & 0xFF));
        assertEquals(checksum, calculatedChecksum);
        logger.info("校验码验证通过");

        // 8. 转义处理检查
        logger.info("\n8. 转义处理检查");
        Buffer unescapedData = fullMessage.getBuffer(1, fullMessage.length() - 1); // 去掉起始和结束符
        Buffer escapedData = EscapeUtils.escape(unescapedData);
        Buffer unescapedAgain = EscapeUtils.unescape(escapedData);

        assertEquals(unescapedData.length(), unescapedAgain.length());
        logger.info("转义处理验证通过");

        // 9. 工厂解析消息
        logger.info("\n9. 工厂解析消息");
        JT808Message parsedMessage = factory.createMessage(0x8100);
        parsedMessage.setHeader(header);
        parsedMessage.decodeBody(bodyBuffer);

        assertInstanceOf(T8100TerminalRegisterResponse.class, parsedMessage);
        T8100TerminalRegisterResponse parsedResponse = (T8100TerminalRegisterResponse) parsedMessage;
        assertEquals(successResponse.getResponseSerialNumber(), parsedResponse.getResponseSerialNumber());
        assertEquals(successResponse.getResult(), parsedResponse.getResult());
        assertEquals(successResponse.getAuthCode(), parsedResponse.getAuthCode());
        logger.info("工厂解析验证成功: {}", parsedResponse);

        logger.info("\n=== T8100消息功能演示完成 ===");
    }

    @Test
    public void demonstrateRealWorldScenarios() {
        logger.info("\n=== T8100实际应用场景演示 ===");

        // 场景1: 终端注册成功
        logger.info("\n场景1: 终端注册成功应答");
        T8100TerminalRegisterResponse successResponse = T8100TerminalRegisterResponse.createSuccessResponse(
                1001, "AUTH2024123001");

        JT808Header successHeader = new JT808Header();
        successHeader.setMessageId(0x8100);
        successHeader.setPhoneNumber("013912345678");
        successHeader.setSerialNumber(2001);
        successResponse.setHeader(successHeader);

        logger.info("平台应答: 终端{}注册成功，分配鉴权码: {}",
                successHeader.getPhoneNumber(), successResponse.getAuthCode());
        logger.info("应答详情: {}", successResponse);

        // 编码并模拟发送
        Buffer successMessage = successResponse.encodeBody();
        logger.info("发送给终端的消息体: {} 字节", successMessage.length());

        // 场景2: 车辆已被注册
        logger.info("\n场景2: 车辆已被注册");
        T8100TerminalRegisterResponse vehicleRegisteredResponse = T8100TerminalRegisterResponse.createFailureResponse(
                1002, T8100TerminalRegisterResponse.RESULT_VEHICLE_REGISTERED);

        JT808Header vehicleHeader = new JT808Header();
        vehicleHeader.setMessageId(0x8100);
        vehicleHeader.setPhoneNumber("013987654321");
        vehicleHeader.setSerialNumber(2002);
        vehicleRegisteredResponse.setHeader(vehicleHeader);

        logger.info("平台应答: 终端{}注册失败，原因: {}",
                vehicleHeader.getPhoneNumber(), vehicleRegisteredResponse.getResultDescription());
        logger.info("应答详情: {}", vehicleRegisteredResponse);

        assertFalse(vehicleRegisteredResponse.isSuccess());
        assertNull(vehicleRegisteredResponse.getAuthCode());

        // 场景3: 数据库中无该终端
        logger.info("\n场景3: 数据库中无该终端");
        T8100TerminalRegisterResponse terminalNotFoundResponse = T8100TerminalRegisterResponse.createFailureResponse(
                1003, T8100TerminalRegisterResponse.RESULT_TERMINAL_NOT_IN_DATABASE);

        JT808Header terminalHeader = new JT808Header();
        terminalHeader.setMessageId(0x8100);
        terminalHeader.setPhoneNumber("013555666777");
        terminalHeader.setSerialNumber(2003);
        terminalNotFoundResponse.setHeader(terminalHeader);

        logger.info("平台应答: 终端{}注册失败，原因: {}",
                terminalHeader.getPhoneNumber(), terminalNotFoundResponse.getResultDescription());
        logger.info("应答详情: {}", terminalNotFoundResponse);

        assertFalse(terminalNotFoundResponse.isSuccess());
        assertNull(terminalNotFoundResponse.getAuthCode());

        // 场景4: 批量处理注册应答
        logger.info("\n场景4: 批量处理注册应答");
        String[] phoneNumbers = {"013111111111", "013222222222", "013333333333"};
        byte[] results = {
                T8100TerminalRegisterResponse.RESULT_SUCCESS,
                T8100TerminalRegisterResponse.RESULT_VEHICLE_NOT_IN_DATABASE,
                T8100TerminalRegisterResponse.RESULT_SUCCESS
        };

        for (int i = 0; i < phoneNumbers.length; i++) {
            T8100TerminalRegisterResponse response;
            if (results[i] == T8100TerminalRegisterResponse.RESULT_SUCCESS) {
                response = T8100TerminalRegisterResponse.createSuccessResponse(
                        2000 + i, "AUTH202412300" + (i + 1));
            } else {
                response = T8100TerminalRegisterResponse.createFailureResponse(
                        2000 + i, results[i]);
            }

            JT808Header batchHeader = new JT808Header();
            batchHeader.setMessageId(0x8100);
            batchHeader.setPhoneNumber(phoneNumbers[i]);
            batchHeader.setSerialNumber(3000 + i);
            response.setHeader(batchHeader);

            logger.info("批量处理 {}: 终端{} - {}",
                    i + 1, phoneNumbers[i], response.getResultDescription());

            if (response.isSuccess()) {
                logger.info("  分配鉴权码: {}", response.getAuthCode());
            }
        }

        // 场景5: 消息完整性验证
        logger.info("\n场景5: 消息完整性验证");
        T8100TerminalRegisterResponse originalResponse = T8100TerminalRegisterResponse.createSuccessResponse(
                9999, "FINAL_AUTH_CODE");

        // 编码
        Buffer encoded = originalResponse.encodeBody();

        // 解码
        T8100TerminalRegisterResponse decodedResponse = new T8100TerminalRegisterResponse();
        decodedResponse.decodeBody(encoded);

        // 验证完整性
        assertEquals(originalResponse.getResponseSerialNumber(), decodedResponse.getResponseSerialNumber());
        assertEquals(originalResponse.getResult(), decodedResponse.getResult());
        assertEquals(originalResponse.getAuthCode(), decodedResponse.getAuthCode());
        assertEquals(originalResponse.isSuccess(), decodedResponse.isSuccess());

        logger.info("消息完整性验证通过");
        logger.info("原始消息: {}", originalResponse);
        logger.info("解码消息: {}", decodedResponse);

        logger.info("\n=== T8100实际应用场景演示完成 ===");
    }

    /**
     * 字节数组转十六进制字符串
     */
    private String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02X ", b & 0xFF));
        }
        return result.toString().trim();
    }
}