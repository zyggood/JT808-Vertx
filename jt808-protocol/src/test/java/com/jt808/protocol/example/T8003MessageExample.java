package com.jt808.protocol.example;

import com.jt808.common.exception.ProtocolException;
import com.jt808.protocol.factory.JT808MessageFactory;
import com.jt808.protocol.message.JT808Header;
import com.jt808.protocol.message.JT808Message;
import com.jt808.protocol.message.T8003ResendSubpackageRequest;
import com.jt808.protocol.util.ChecksumUtils;
import com.jt808.protocol.util.EscapeUtils;
import io.vertx.core.buffer.Buffer;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

/**
 * T8003补传分包请求消息使用示例
 */
public class T8003MessageExample {
    private static final Logger logger = LoggerFactory.getLogger(T8003MessageExample.class);

    @Test
    public void demonstrateT8003Usage() throws ProtocolException {
        logger.info("=== T8003补传分包请求消息使用示例 ===");

        // 1. 使用工厂创建消息
        JT808MessageFactory factory = JT808MessageFactory.getInstance();
        JT808Message message = factory.createMessage(0x8003);

        logger.info("1. 工厂创建的消息类型: {}", message.getClass().getSimpleName());
        logger.info("   消息ID: 0x{}", Integer.toHexString(message.getMessageId()).toUpperCase());

        // 2. 直接创建T8003消息
        T8003ResendSubpackageRequest request = new T8003ResendSubpackageRequest();
        request.setOriginalSerialNumber(12345);
        request.addRetransmitPackageId(1);
        request.addRetransmitPackageId(3);
        request.addRetransmitPackageId(5);
        request.addRetransmitPackageId(7);

        logger.info("\n2. 直接创建的T8003消息:");
        logger.info("   原始消息流水号: {}", request.getOriginalSerialNumber());
        logger.info("   重传包数量: {}", request.getRetransmitPackageCount());
        logger.info("   重传包序号: {}", request.getRetransmitPackageIds());

        // 3. 使用静态方法创建
        T8003ResendSubpackageRequest staticRequest = T8003ResendSubpackageRequest.create(54321, 2, 4, 6, 8, 10);

        logger.info("\n3. 静态方法创建的消息:");
        logger.info("   {}", staticRequest.toString());

        // 4. 设置消息头并编码
        JT808Header header = new JT808Header(0x8003, "13800138000", 100);
        request.setHeader(header);

        Buffer encodedBody = request.encodeBody();
        logger.info("\n4. 编码后的消息体:");
        logger.info("   长度: {} 字节", encodedBody.length());
        StringBuilder hexContent = new StringBuilder();
        for (int i = 0; i < encodedBody.length(); i++) {
            hexContent.append(String.format("%02X ", encodedBody.getByte(i) & 0xFF));
        }
        logger.info("   内容: {}", hexContent.toString());

        // 5. 解码验证
        T8003ResendSubpackageRequest decoded = new T8003ResendSubpackageRequest();
        decoded.decodeBody(encodedBody);

        logger.info("\n5. 解码验证:");
        logger.info("   原始流水号: {}", decoded.getOriginalSerialNumber());
        logger.info("   重传包数量: {}", decoded.getRetransmitPackageCount());
        logger.info("   重传包序号: {}", decoded.getRetransmitPackageIds());
        logger.info("   编码解码一致性: {}",
                (request.getOriginalSerialNumber() == decoded.getOriginalSerialNumber() &&
                        request.getRetransmitPackageIds().equals(decoded.getRetransmitPackageIds())));

        // 6. 使用工厂编码完整消息
        Buffer completeMessage = factory.encodeMessage(request);

        logger.info("\n6. 完整消息编码:");
        logger.info("   完整消息长度: {} 字节", completeMessage.length());
        StringBuilder completeHex = new StringBuilder();
        for (int i = 0; i < Math.min(completeMessage.length(), 20); i++) {
            completeHex.append(String.format("%02X ", completeMessage.getByte(i) & 0xFF));
        }
        if (completeMessage.length() > 20) {
            completeHex.append("...");
        }
        logger.info("   完整消息内容: {}", completeHex.toString());

        // 7. 校验码验证
        ChecksumUtils.ChecksumResult checksumResult = ChecksumUtils.verifyCompleteMessage(completeMessage);
        logger.info("\n7. 校验码验证: {}", checksumResult.getMessage());

        // 8. 转义处理检查
        boolean needsEscape = EscapeUtils.needsEscape(completeMessage);
        logger.info("\n8. 转义处理:");
        logger.info("   是否需要转义: {}", needsEscape);

        if (needsEscape) {
            Buffer escaped = EscapeUtils.escape(completeMessage);
            logger.info("   转义后长度: {} 字节", escaped.length());

            Buffer unescaped = EscapeUtils.unescape(escaped);
            boolean escapeSymmetry = completeMessage.equals(unescaped);
            logger.info("   转义对称性: {}", escapeSymmetry);
        }

        // 9. 工厂解析消息
        JT808Message parsed = factory.parseMessage(completeMessage);
        logger.info("\n9. 工厂解析结果:");
        logger.info("   解析的消息类型: {}", parsed.getClass().getSimpleName());
        logger.info("   消息ID匹配: {}", (parsed.getMessageId() == 0x8003));

        if (parsed instanceof T8003ResendSubpackageRequest) {
            T8003ResendSubpackageRequest parsedRequest = (T8003ResendSubpackageRequest) parsed;
            logger.info("   解析的流水号: {}", parsedRequest.getOriginalSerialNumber());
            logger.info("   解析的包数量: {}", parsedRequest.getRetransmitPackageCount());
        }

        // 10. 实际应用场景示例
        logger.info("\n10. 实际应用场景:");
        demonstrateRealWorldUsage();
    }

    /**
     * 演示实际应用场景
     */
    private void demonstrateRealWorldUsage() {
        logger.info("    场景: 终端上传位置信息时分包传输，平台发现缺少部分分包");

        // 模拟场景：终端发送了10个分包的位置信息，平台只收到了1,2,4,6,7,9,10包
        // 平台需要请求重传第3,5,8包

        int originalLocationMessageSerial = 98765;
        int[] missingPackages = {3, 5, 8};

        // 创建补传请求
        T8003ResendSubpackageRequest resendRequest = T8003ResendSubpackageRequest.create(
                originalLocationMessageSerial, missingPackages);

        // 设置平台消息头
        JT808Header platformHeader = new JT808Header(0x8003, "13800138000", 201);
        resendRequest.setHeader(platformHeader);

        logger.info("    补传请求详情:");
        logger.info("    - 原始位置消息流水号: {}", originalLocationMessageSerial);
        logger.info("    - 缺失的分包序号: {}", Arrays.toString(missingPackages));
        logger.info("    - 平台消息流水号: {}", platformHeader.getSerialNumber());
        logger.info("    - 目标终端号码: {}", platformHeader.getPhoneNumber());

        // 编码准备发送
        Buffer messageToSend = resendRequest.encodeBody();
        logger.info("    - 编码后消息体长度: {} 字节", messageToSend.length());

        // 模拟终端收到请求后的处理
        logger.info("\n    终端收到补传请求后的处理:");
        T8003ResendSubpackageRequest receivedRequest = new T8003ResendSubpackageRequest();
        receivedRequest.decodeBody(messageToSend);

        logger.info("    - 需要重传的原始消息流水号: {}", receivedRequest.getOriginalSerialNumber());
        logger.info("    - 需要重传的分包数量: {}", receivedRequest.getRetransmitPackageCount());
        logger.info("    - 需要重传的分包序号: {}", receivedRequest.getRetransmitPackageIds());

        // 验证包序号
        for (int packageId : missingPackages) {
            if (receivedRequest.containsPackageId(packageId)) {
                logger.info("    - 确认需要重传分包 #{}", packageId);
            }
        }
    }
}