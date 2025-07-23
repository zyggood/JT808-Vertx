package com.jt808.protocol.example;

import com.jt808.protocol.message.T8801MultimediaDataUploadResponse;
import io.vertx.core.buffer.Buffer;

/**
 * T8801 多媒体数据上传应答消息使用示例
 * 
 * 该示例展示了如何创建和使用多媒体数据上传应答消息，包括：
 * 1. 完整接收应答（无需重传）
 * 2. 部分接收应答（需要重传指定分包）
 * 3. 消息的编码和解码
 * 
 * @author JT808 Protocol
 * @version 1.0
 */
public class T8801MultimediaDataUploadResponseExample {

    public static void main(String[] args) {
        System.out.println("=== T8801 多媒体数据上传应答消息示例 ===");
        
        // 示例1：完整接收应答
        demonstrateCompleteResponse();
        
        System.out.println();
        
        // 示例2：部分接收应答（需要重传）
        demonstrateRetransmissionResponse();
        
        System.out.println();
        
        // 示例3：编码和解码
        demonstrateEncodingDecoding();
        
        System.out.println();
        
        // 示例4：实际应用场景
        demonstrateRealWorldScenario();
    }

    /**
     * 示例1：完整接收应答
     * 当平台完整接收到所有多媒体数据包时的应答
     */
    private static void demonstrateCompleteResponse() {
        System.out.println("--- 示例1：完整接收应答 ---");
        
        // 多媒体ID
        long multimediaId = 0x12345678L;
        
        // 创建完整接收应答
        T8801MultimediaDataUploadResponse response = T8801MultimediaDataUploadResponse
                .createCompleteResponse(multimediaId);
        
        System.out.println("多媒体ID: 0x" + Long.toHexString(multimediaId).toUpperCase());
        System.out.println("重传包总数: " + response.getRetransmissionPacketCount());
        System.out.println("应答类型: 完整接收，无需重传");
        System.out.println("消息详情: " + response);
        
        // 编码消息
        Buffer encoded = response.encodeBody();
        System.out.println("编码后长度: " + encoded.length() + " 字节");
        System.out.println("编码数据: " + bytesToHex(encoded.getBytes()));
    }

    /**
     * 示例2：部分接收应答（需要重传）
     * 当平台只接收到部分多媒体数据包时的应答
     */
    private static void demonstrateRetransmissionResponse() {
        System.out.println("--- 示例2：部分接收应答（需要重传） ---");
        
        // 多媒体ID
        long multimediaId = 0xABCDEF01L;
        
        // 需要重传的分包ID列表
        int[] missingPacketIds = {2, 5, 8, 12};
        
        // 创建重传应答
        T8801MultimediaDataUploadResponse response = T8801MultimediaDataUploadResponse
                .createRetransmissionResponse(multimediaId, missingPacketIds);
        
        System.out.println("多媒体ID: 0x" + Long.toHexString(multimediaId).toUpperCase());
        System.out.println("重传包总数: " + response.getRetransmissionPacketCount());
        System.out.print("需要重传的分包ID: ");
        for (int i = 0; i < missingPacketIds.length; i++) {
            System.out.print(missingPacketIds[i]);
            if (i < missingPacketIds.length - 1) {
                System.out.print(", ");
            }
        }
        System.out.println();
        System.out.println("应答类型: 部分接收，需要重传指定分包");
        System.out.println("消息详情: " + response);
        
        // 编码消息
        Buffer encoded = response.encodeBody();
        System.out.println("编码后长度: " + encoded.length() + " 字节");
        System.out.println("编码数据: " + bytesToHex(encoded.getBytes()));
    }

    /**
     * 示例3：编码和解码
     * 演示消息的编码和解码过程
     */
    private static void demonstrateEncodingDecoding() {
        System.out.println("--- 示例3：编码和解码 ---");
        
        // 创建原始消息
        long originalMultimediaId = 0x87654321L;
        int[] originalPacketIds = {1, 3, 7, 15, 31};
        
        T8801MultimediaDataUploadResponse originalMessage = T8801MultimediaDataUploadResponse
                .createRetransmissionResponse(originalMultimediaId, originalPacketIds);
        
        System.out.println("原始消息: " + originalMessage);
        
        // 编码消息
        Buffer encoded = originalMessage.encodeBody();
        System.out.println("编码数据: " + bytesToHex(encoded.getBytes()));
        
        // 解码消息
        T8801MultimediaDataUploadResponse decodedMessage = new T8801MultimediaDataUploadResponse();
        decodedMessage.decodeBody(encoded);
        
        System.out.println("解码消息: " + decodedMessage);
        
        // 验证编码解码的一致性
        boolean isConsistent = originalMessage.getMultimediaId() == decodedMessage.getMultimediaId() &&
                originalMessage.getRetransmissionPacketCount() == decodedMessage.getRetransmissionPacketCount() &&
                originalMessage.getRetransmissionPacketIds().equals(decodedMessage.getRetransmissionPacketIds());
        
        System.out.println("编码解码一致性: " + (isConsistent ? "通过" : "失败"));
    }

    /**
     * 示例4：实际应用场景
     * 模拟平台处理多媒体数据上传的实际场景
     */
    private static void demonstrateRealWorldScenario() {
        System.out.println("--- 示例4：实际应用场景 ---");
        
        // 场景：终端上传了一个包含20个分包的多媒体文件
        long multimediaId = 0x20231201L; // 假设这是一个基于日期的多媒体ID
        int totalPackets = 20;
        
        System.out.println("场景：终端上传多媒体文件");
        System.out.println("多媒体ID: 0x" + Long.toHexString(multimediaId).toUpperCase());
        System.out.println("总分包数: " + totalPackets);
        
        // 模拟平台接收情况：丢失了几个分包
        boolean[] receivedPackets = new boolean[totalPackets];
        // 假设接收到了大部分分包，但丢失了第3、7、11、18包
        for (int i = 0; i < totalPackets; i++) {
            receivedPackets[i] = !(i == 2 || i == 6 || i == 10 || i == 17); // 0-based索引
        }
        
        // 统计丢失的分包
        java.util.List<Short> missingPackets = new java.util.ArrayList<>();
        for (int i = 0; i < totalPackets; i++) {
            if (!receivedPackets[i]) {
                missingPackets.add((short) (i + 1)); // 转换为1-based分包序号
            }
        }
        
        System.out.print("接收状态: ");
        for (int i = 0; i < totalPackets; i++) {
            System.out.print(receivedPackets[i] ? "✓" : "✗");
        }
        System.out.println();
        
        T8801MultimediaDataUploadResponse response;
        
        if (missingPackets.isEmpty()) {
            // 完整接收
            response = T8801MultimediaDataUploadResponse.createCompleteResponse(multimediaId);
            System.out.println("处理结果: 完整接收所有分包");
        } else {
            // 需要重传
            int[] missingArray = missingPackets.stream().mapToInt(Short::intValue).toArray();
            
            response = T8801MultimediaDataUploadResponse.createRetransmissionResponse(multimediaId, missingArray);
            System.out.println("处理结果: 需要重传 " + missingPackets.size() + " 个分包");
            System.out.println("丢失分包: " + missingPackets);
        }
        
        System.out.println("应答消息: " + response);
        
        // 编码准备发送
        Buffer responseData = response.encodeBody();
        System.out.println("应答数据长度: " + responseData.length() + " 字节");
        System.out.println("应答数据: " + bytesToHex(responseData.getBytes()));
        
        System.out.println("\n注意：终端收到此应答后，应使用0x0801消息重发丢失的分包");
    }

    /**
     * 将字节数组转换为十六进制字符串
     */
    private static String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02X ", b));
        }
        return result.toString().trim();
    }
}