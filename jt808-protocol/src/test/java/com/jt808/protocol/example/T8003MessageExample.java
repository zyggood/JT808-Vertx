package com.jt808.protocol.example;

import com.jt808.protocol.factory.JT808MessageFactory;
import com.jt808.protocol.message.JT808Header;
import com.jt808.protocol.message.JT808Message;
import com.jt808.protocol.message.T8003ResendSubpackageRequest;
import com.jt808.protocol.util.ChecksumUtils;
import com.jt808.protocol.util.EscapeUtils;
import com.jt808.common.exception.ProtocolException;
import io.vertx.core.buffer.Buffer;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

/**
 * T8003补传分包请求消息使用示例
 */
public class T8003MessageExample {
    
    @Test
    public void demonstrateT8003Usage() throws ProtocolException {
        System.out.println("=== T8003补传分包请求消息使用示例 ===");
        
        // 1. 使用工厂创建消息
        JT808MessageFactory factory = JT808MessageFactory.getInstance();
        JT808Message message = factory.createMessage(0x8003);
        
        System.out.println("1. 工厂创建的消息类型: " + message.getClass().getSimpleName());
        System.out.println("   消息ID: 0x" + Integer.toHexString(message.getMessageId()).toUpperCase());
        
        // 2. 直接创建T8003消息
        T8003ResendSubpackageRequest request = new T8003ResendSubpackageRequest();
        request.setOriginalSerialNumber(12345);
        request.addRetransmitPackageId(1);
        request.addRetransmitPackageId(3);
        request.addRetransmitPackageId(5);
        request.addRetransmitPackageId(7);
        
        System.out.println("\n2. 直接创建的T8003消息:");
        System.out.println("   原始消息流水号: " + request.getOriginalSerialNumber());
        System.out.println("   重传包数量: " + request.getRetransmitPackageCount());
        System.out.println("   重传包序号: " + request.getRetransmitPackageIds());
        
        // 3. 使用静态方法创建
        T8003ResendSubpackageRequest staticRequest = T8003ResendSubpackageRequest.create(54321, 2, 4, 6, 8, 10);
        
        System.out.println("\n3. 静态方法创建的消息:");
        System.out.println("   " + staticRequest.toString());
        
        // 4. 设置消息头并编码
        JT808Header header = new JT808Header(0x8003, "13800138000", 100);
        request.setHeader(header);
        
        Buffer encodedBody = request.encodeBody();
        System.out.println("\n4. 编码后的消息体:");
        System.out.println("   长度: " + encodedBody.length() + " 字节");
        System.out.print("   内容: ");
        for (int i = 0; i < encodedBody.length(); i++) {
            System.out.printf("%02X ", encodedBody.getByte(i) & 0xFF);
        }
        System.out.println();
        
        // 5. 解码验证
        T8003ResendSubpackageRequest decoded = new T8003ResendSubpackageRequest();
        decoded.decodeBody(encodedBody);
        
        System.out.println("\n5. 解码验证:");
        System.out.println("   原始流水号: " + decoded.getOriginalSerialNumber());
        System.out.println("   重传包数量: " + decoded.getRetransmitPackageCount());
        System.out.println("   重传包序号: " + decoded.getRetransmitPackageIds());
        System.out.println("   编码解码一致性: " + 
            (request.getOriginalSerialNumber() == decoded.getOriginalSerialNumber() &&
             request.getRetransmitPackageIds().equals(decoded.getRetransmitPackageIds())));
        
        // 6. 使用工厂编码完整消息
        Buffer completeMessage = factory.encodeMessage(request);
        
        System.out.println("\n6. 完整消息编码:");
        System.out.println("   完整消息长度: " + completeMessage.length() + " 字节");
        System.out.print("   完整消息内容: ");
        for (int i = 0; i < Math.min(completeMessage.length(), 20); i++) {
            System.out.printf("%02X ", completeMessage.getByte(i) & 0xFF);
        }
        if (completeMessage.length() > 20) {
            System.out.print("...");
        }
        System.out.println();
        
        // 7. 校验码验证
        ChecksumUtils.ChecksumResult checksumResult = ChecksumUtils.verifyCompleteMessage(completeMessage);
        System.out.println("\n7. 校验码验证: " + checksumResult.getMessage());
        
        // 8. 转义处理检查
        boolean needsEscape = EscapeUtils.needsEscape(completeMessage);
        System.out.println("\n8. 转义处理:");
        System.out.println("   是否需要转义: " + needsEscape);
        
        if (needsEscape) {
            Buffer escaped = EscapeUtils.escape(completeMessage);
            System.out.println("   转义后长度: " + escaped.length() + " 字节");
            
            Buffer unescaped = EscapeUtils.unescape(escaped);
            boolean escapeSymmetry = completeMessage.equals(unescaped);
            System.out.println("   转义对称性: " + escapeSymmetry);
        }
        
        // 9. 工厂解析消息
        JT808Message parsed = factory.parseMessage(completeMessage);
        System.out.println("\n9. 工厂解析结果:");
        System.out.println("   解析的消息类型: " + parsed.getClass().getSimpleName());
        System.out.println("   消息ID匹配: " + (parsed.getMessageId() == 0x8003));
        
        if (parsed instanceof T8003ResendSubpackageRequest) {
            T8003ResendSubpackageRequest parsedRequest = (T8003ResendSubpackageRequest) parsed;
            System.out.println("   解析的流水号: " + parsedRequest.getOriginalSerialNumber());
            System.out.println("   解析的包数量: " + parsedRequest.getRetransmitPackageCount());
        }
        
        // 10. 实际应用场景示例
        System.out.println("\n10. 实际应用场景:");
        demonstrateRealWorldUsage();
    }
    
    /**
     * 演示实际应用场景
     */
    private void demonstrateRealWorldUsage() {
        System.out.println("    场景: 终端上传位置信息时分包传输，平台发现缺少部分分包");
        
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
        
        System.out.println("    补传请求详情:");
        System.out.println("    - 原始位置消息流水号: " + originalLocationMessageSerial);
        System.out.println("    - 缺失的分包序号: " + Arrays.toString(missingPackages));
        System.out.println("    - 平台消息流水号: " + platformHeader.getSerialNumber());
        System.out.println("    - 目标终端号码: " + platformHeader.getPhoneNumber());
        
        // 编码准备发送
        Buffer messageToSend = resendRequest.encodeBody();
        System.out.println("    - 编码后消息体长度: " + messageToSend.length() + " 字节");
        
        // 模拟终端收到请求后的处理
        System.out.println("\n    终端收到补传请求后的处理:");
        T8003ResendSubpackageRequest receivedRequest = new T8003ResendSubpackageRequest();
        receivedRequest.decodeBody(messageToSend);
        
        System.out.println("    - 需要重传的原始消息流水号: " + receivedRequest.getOriginalSerialNumber());
        System.out.println("    - 需要重传的分包数量: " + receivedRequest.getRetransmitPackageCount());
        System.out.println("    - 需要重传的分包序号: " + receivedRequest.getRetransmitPackageIds());
        
        // 验证包序号
        for (int packageId : missingPackages) {
            if (receivedRequest.containsPackageId(packageId)) {
                System.out.println("    - 确认需要重传分包 #" + packageId);
            }
        }
    }
}