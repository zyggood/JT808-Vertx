package com.jt808.protocol.example;

import com.jt808.protocol.factory.JT808MessageFactory;
import com.jt808.protocol.message.JT808Header;
import com.jt808.protocol.message.JT808Message;
import com.jt808.protocol.message.T0805CameraImmediateShootingCommandResponse;
import com.jt808.protocol.message.T0805CameraImmediateShootingCommandResponse.Result;
import io.vertx.core.buffer.Buffer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * T0805 摄像头立即拍摄命令应答消息示例
 * 演示如何使用T0805消息进行摄像头拍摄结果应答
 * 
 * @author JT808 Protocol
 * @version 1.0
 */
public class T0805CameraImmediateShootingCommandResponseExample {

    public static void main(String[] args) {
        System.out.println("=== T0805 摄像头立即拍摄命令应答消息示例 ===");
        
        // 演示成功应答
        demonstrateSuccessResponse();
        
        // 演示失败应答
        demonstrateFailureResponse();
        
        // 演示消息编码解码
        demonstrateEncodeDecodeMessage();
        
        // 演示消息操作
        demonstrateMessageOperations();
        
        // 演示批量处理
        demonstrateBatchProcessing();
        
        // 演示结果枚举使用
        demonstrateResultEnum();
        
        // 演示错误处理
        demonstrateErrorHandling();
        
        System.out.println("\n=== T0805 示例演示完成 ===");
    }

    /**
     * 演示成功应答的创建和使用
     */
    private static void demonstrateSuccessResponse() {
        System.out.println("\n--- 演示成功应答 ---");
        
        // 创建成功应答（使用List）
        List<Long> multimediaIds = Arrays.asList(1001L, 1002L, 1003L);
        T0805CameraImmediateShootingCommandResponse successResponse = 
                T0805CameraImmediateShootingCommandResponse.createSuccessResponse(12345, multimediaIds);
        
        System.out.println("成功应答: " + successResponse.getMessageDescription());
        System.out.println("是否成功: " + successResponse.isSuccess());
        System.out.println("多媒体ID个数: " + successResponse.getMultimediaIdCount());
        System.out.println("多媒体ID列表: " + successResponse.getMultimediaIds());
        
        // 创建成功应答（使用可变参数）
        T0805CameraImmediateShootingCommandResponse successResponse2 = 
                T0805CameraImmediateShootingCommandResponse.createSuccessResponse(12346, 2001L, 2002L);
        
        System.out.println("\n成功应答2: " + successResponse2.getMessageDescription());
    }

    /**
     * 演示失败应答的创建和使用
     */
    private static void demonstrateFailureResponse() {
        System.out.println("\n--- 演示失败应答 ---");
        
        // 创建失败应答
        T0805CameraImmediateShootingCommandResponse failureResponse = 
                T0805CameraImmediateShootingCommandResponse.createFailureResponse(12347, Result.FAILURE);
        
        System.out.println("失败应答: " + failureResponse.getMessageDescription());
        System.out.println("是否失败: " + failureResponse.isFailure());
        System.out.println("结果描述: " + failureResponse.getResultDescription());
        
        // 创建通道不支持应答
        T0805CameraImmediateShootingCommandResponse channelNotSupportedResponse = 
                T0805CameraImmediateShootingCommandResponse.createFailureResponse(12348, Result.CHANNEL_NOT_SUPPORTED);
        
        System.out.println("\n通道不支持应答: " + channelNotSupportedResponse.getMessageDescription());
        System.out.println("结果枚举: " + channelNotSupportedResponse.getResultEnum());
    }

    /**
     * 演示消息的编码和解码
     */
    private static void demonstrateEncodeDecodeMessage() {
        System.out.println("\n--- 演示消息编码解码 ---");
        
        // 创建原始消息
        T0805CameraImmediateShootingCommandResponse originalMessage = 
                T0805CameraImmediateShootingCommandResponse.createSuccessResponse(9999, 3001L, 3002L, 3003L);
        
        System.out.println("原始消息: " + originalMessage);
        
        // 编码消息体
        Buffer encodedBody = originalMessage.encodeBody();
        System.out.println("编码后的消息体长度: " + encodedBody.length() + " 字节");
        
        // 打印编码后的字节数据
        StringBuilder hexString = new StringBuilder();
        for (int i = 0; i < encodedBody.length(); i++) {
            hexString.append(String.format("%02X ", encodedBody.getByte(i) & 0xFF));
        }
        System.out.println("编码后的数据: " + hexString.toString().trim());
        
        // 解码消息体
        T0805CameraImmediateShootingCommandResponse decodedMessage = new T0805CameraImmediateShootingCommandResponse();
        decodedMessage.decodeBody(encodedBody);
        
        System.out.println("解码后的消息: " + decodedMessage);
        
        // 验证编码解码的一致性
        boolean isEqual = originalMessage.equals(decodedMessage);
        System.out.println("编码解码一致性验证: " + (isEqual ? "通过" : "失败"));
    }

    /**
     * 演示消息操作
     */
    private static void demonstrateMessageOperations() {
        System.out.println("\n--- 演示消息操作 ---");
        
        // 创建空的成功应答
        T0805CameraImmediateShootingCommandResponse response = 
                T0805CameraImmediateShootingCommandResponse.createSuccessResponse(5555, new ArrayList<>());
        
        System.out.println("初始状态: " + response.getMessageDescription());
        
        // 添加多媒体ID
        response.addMultimediaId(4001L);
        response.addMultimediaId(4002L);
        System.out.println("添加多媒体ID后: " + response.getMessageDescription());
        
        // 清空多媒体ID
        response.clearMultimediaIds();
        System.out.println("清空多媒体ID后: " + response.getMessageDescription());
        
        // 设置新的多媒体ID列表
        List<Long> newIds = Arrays.asList(5001L, 5002L, 5003L, 5004L);
        response.setMultimediaIds(newIds);
        System.out.println("设置新列表后: " + response.getMessageDescription());
    }

    /**
     * 演示批量处理
     */
    private static void demonstrateBatchProcessing() {
        System.out.println("\n--- 演示批量处理 ---");
        
        List<T0805CameraImmediateShootingCommandResponse> responses = new ArrayList<>();
        
        // 创建多个应答消息
        responses.add(T0805CameraImmediateShootingCommandResponse.createSuccessResponse(1001, 6001L, 6002L));
        responses.add(T0805CameraImmediateShootingCommandResponse.createFailureResponse(1002, Result.FAILURE));
        responses.add(T0805CameraImmediateShootingCommandResponse.createSuccessResponse(1003, 6003L));
        responses.add(T0805CameraImmediateShootingCommandResponse.createFailureResponse(1004, Result.CHANNEL_NOT_SUPPORTED));
        
        // 统计处理结果
        int successCount = 0;
        int failureCount = 0;
        int totalMultimediaIds = 0;
        
        System.out.println("批量处理结果:");
        for (T0805CameraImmediateShootingCommandResponse response : responses) {
            System.out.println("  " + response.getMessageDescription());
            
            if (response.isSuccess()) {
                successCount++;
                totalMultimediaIds += response.getMultimediaIdCount();
            } else {
                failureCount++;
            }
        }
        
        System.out.println("\n统计结果:");
        System.out.println("  成功应答数: " + successCount);
        System.out.println("  失败应答数: " + failureCount);
        System.out.println("  总多媒体ID数: " + totalMultimediaIds);
    }

    /**
     * 演示结果枚举的使用
     */
    private static void demonstrateResultEnum() {
        System.out.println("\n--- 演示结果枚举 ---");
        
        System.out.println("所有结果类型:");
        for (Result result : Result.values()) {
            System.out.println("  " + result.name() + " (" + result.getValue() + "): " + result.getDescription());
        }
        
        // 根据值查找枚举
        System.out.println("\n根据值查找枚举:");
        for (int i = 0; i <= 3; i++) {
            Result result = Result.fromValue(i);
            if (result != null) {
                System.out.println("  值 " + i + " 对应: " + result.getDescription());
            } else {
                System.out.println("  值 " + i + " 无对应枚举");
            }
        }
    }

    /**
     * 演示错误处理
     */
    private static void demonstrateErrorHandling() {
        System.out.println("\n--- 演示错误处理 ---");
        
        try {
            // 尝试用SUCCESS结果创建失败应答（应该抛出异常）
            T0805CameraImmediateShootingCommandResponse.createFailureResponse(1234, Result.SUCCESS);
            System.out.println("错误：应该抛出异常但没有");
        } catch (IllegalArgumentException e) {
            System.out.println("正确捕获异常: " + e.getMessage());
        }
        
        try {
            // 尝试解码长度不足的数据
            T0805CameraImmediateShootingCommandResponse response = new T0805CameraImmediateShootingCommandResponse();
            Buffer invalidBuffer = Buffer.buffer(new byte[]{0x01}); // 只有1字节，不足3字节
            response.decodeBody(invalidBuffer);
            System.out.println("错误：应该抛出异常但没有");
        } catch (IllegalArgumentException e) {
            System.out.println("正确捕获解码异常: " + e.getMessage());
        }
        
        try {
            // 尝试解码成功应答但多媒体ID列表长度不足
            T0805CameraImmediateShootingCommandResponse response = new T0805CameraImmediateShootingCommandResponse();
            Buffer invalidBuffer = Buffer.buffer(new byte[]{0x12, 0x34, 0x00, 0x00, 0x02}); // 声明有2个多媒体ID但没有数据
            response.decodeBody(invalidBuffer);
            System.out.println("错误：应该抛出异常但没有");
        } catch (IllegalArgumentException e) {
            System.out.println("正确捕获多媒体ID列表长度异常: " + e.getMessage());
        }
    }

    /**
     * 演示使用JT808MessageFactory创建和解析消息
     */
    private static void demonstrateMessageFactory() {
        System.out.println("\n--- 演示消息工厂 ---");
        
        try {
            // 使用工厂创建消息
            JT808MessageFactory factory = JT808MessageFactory.getInstance();
            JT808Message message = factory.createMessage(0x0805);
            if (message instanceof T0805CameraImmediateShootingCommandResponse) {
                System.out.println("成功通过工厂创建T0805消息: " + message.getClass().getSimpleName());
            }
            
            // 创建完整的消息包（包含消息头）
            T0805CameraImmediateShootingCommandResponse response = 
                    T0805CameraImmediateShootingCommandResponse.createSuccessResponse(7777, 8001L, 8002L);
            
            // 设置消息头
            JT808Header header = new JT808Header();
            header.setMessageId(0x0805);
            header.setPhoneNumber("13800138000");
            header.setSerialNumber(1234);
            response.setHeader(header);
            
            System.out.println("完整消息: " + response);
            
        } catch (Exception e) {
            System.out.println("消息工厂演示出错: " + e.getMessage());
        }
    }
}