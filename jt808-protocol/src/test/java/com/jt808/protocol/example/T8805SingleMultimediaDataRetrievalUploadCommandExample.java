package com.jt808.protocol.example;

import com.jt808.protocol.constants.MessageTypes;
import com.jt808.protocol.factory.JT808MessageFactory;
import com.jt808.protocol.message.JT808Message;
import com.jt808.protocol.message.T8805SingleMultimediaDataRetrievalUploadCommand;
import io.vertx.core.buffer.Buffer;

/**
 * T8805单条存储多媒体数据检索上传命令示例程序
 * 演示如何创建、编码、解码和使用T8805消息
 */
public class T8805SingleMultimediaDataRetrievalUploadCommandExample {
    
    public static void main(String[] args) {
        System.out.println("=== T8805单条存储多媒体数据检索上传命令示例 ===");
        
        // 1. 基本用法示例
        basicUsageExample();
        
        // 2. 不同删除标志示例
        differentDeleteFlagsExample();
        
        // 3. 编码解码过程演示
        encodeDecodeExample();
        
        // 4. 工厂模式使用示例
        factoryExample();
        
        // 5. 实际应用场景模拟
        realWorldScenarioExample();
    }
    
    /**
     * 基本用法示例
     */
    private static void basicUsageExample() {
        System.out.println("\n1. 基本用法示例:");
        
        T8805SingleMultimediaDataRetrievalUploadCommand command = new T8805SingleMultimediaDataRetrievalUploadCommand(
            12345L, T8805SingleMultimediaDataRetrievalUploadCommand.DeleteFlag.KEEP);
        
        System.out.println("消息ID: 0x" + Integer.toHexString(command.getMessageId()).toUpperCase());
        System.out.println("多媒体ID: " + command.getMultimediaId());
        System.out.println("删除标志: " + command.getDeleteFlag().getDescription());
        System.out.println("完整消息: " + command);
    }
    
    /**
     * 不同删除标志示例
     */
    private static void differentDeleteFlagsExample() {
        System.out.println("\n2. 不同删除标志示例:");
        
        // 保留多媒体数据
        T8805SingleMultimediaDataRetrievalUploadCommand keepCommand = new T8805SingleMultimediaDataRetrievalUploadCommand(
            98765L, T8805SingleMultimediaDataRetrievalUploadCommand.DeleteFlag.KEEP);
        
        // 删除多媒体数据
        T8805SingleMultimediaDataRetrievalUploadCommand deleteCommand = new T8805SingleMultimediaDataRetrievalUploadCommand(
            98765L, T8805SingleMultimediaDataRetrievalUploadCommand.DeleteFlag.DELETE);
        
        System.out.println("保留数据命令: " + keepCommand);
        System.out.println("删除数据命令: " + deleteCommand);
        
        // 显示删除标志的详细信息
        System.out.println("\n删除标志枚举信息:");
        for (T8805SingleMultimediaDataRetrievalUploadCommand.DeleteFlag flag : 
             T8805SingleMultimediaDataRetrievalUploadCommand.DeleteFlag.values()) {
            System.out.println("  " + flag.name() + " (值: " + flag.getValue() + ", 描述: " + flag.getDescription() + ")");
        }
    }
    
    /**
     * 编码解码过程演示
     */
    private static void encodeDecodeExample() {
        System.out.println("\n3. 编码解码过程演示:");
        
        T8805SingleMultimediaDataRetrievalUploadCommand original = new T8805SingleMultimediaDataRetrievalUploadCommand(
            0xABCDEF12L, T8805SingleMultimediaDataRetrievalUploadCommand.DeleteFlag.DELETE);
        
        System.out.println("原始消息: " + original);
        
        // 编码
        Buffer encoded = original.encode();
        System.out.println("编码后长度: " + encoded.length() + " 字节");
        System.out.println("编码后数据: " + bytesToHex(encoded.getBytes()));
        
        // 详细分析编码内容
        System.out.println("\n编码内容分析:");
        System.out.println("  多媒体ID (4字节): 0x" + Long.toHexString(encoded.getUnsignedInt(0)).toUpperCase());
        System.out.println("  删除标志 (1字节): 0x" + Integer.toHexString(encoded.getByte(4) & 0xFF).toUpperCase() + 
                          " (" + T8805SingleMultimediaDataRetrievalUploadCommand.DeleteFlag.fromValue(encoded.getByte(4) & 0xFF).getDescription() + ")");
        
        // 解码
        T8805SingleMultimediaDataRetrievalUploadCommand decoded = T8805SingleMultimediaDataRetrievalUploadCommand.decode(encoded);
        System.out.println("\n解码后消息: " + decoded);
        
        // 验证编码解码一致性
        boolean isEqual = original.equals(decoded);
        System.out.println("编码解码一致性验证: " + (isEqual ? "通过" : "失败"));
        
        if (isEqual) {
            System.out.println("  多媒体ID匹配: " + (original.getMultimediaId() == decoded.getMultimediaId()));
            System.out.println("  删除标志匹配: " + (original.getDeleteFlag() == decoded.getDeleteFlag()));
        }
    }
    
    /**
     * 工厂模式使用示例
     */
    private static void factoryExample() {
        System.out.println("\n4. 工厂模式使用示例:");
        
        JT808MessageFactory factory = JT808MessageFactory.getInstance();
        
        // 通过工厂创建消息
        JT808Message message = factory.createMessage(MessageTypes.Platform.SINGLE_MULTIMEDIA_DATA_RETRIEVAL_UPLOAD_COMMAND);
        
        if (message instanceof T8805SingleMultimediaDataRetrievalUploadCommand) {
            T8805SingleMultimediaDataRetrievalUploadCommand command = (T8805SingleMultimediaDataRetrievalUploadCommand) message;
            
            // 设置消息内容
            command.setMultimediaId(555666L);
            command.setDeleteFlag(T8805SingleMultimediaDataRetrievalUploadCommand.DeleteFlag.KEEP);
            
            System.out.println("通过工厂创建的消息: " + command);
            System.out.println("消息ID: 0x" + Integer.toHexString(command.getMessageId()).toUpperCase());
            System.out.println("消息类型验证: " + (command.getMessageId() == 0x8805 ? "正确" : "错误"));
        }
    }
    
    /**
     * 实际应用场景模拟
     */
    private static void realWorldScenarioExample() {
        System.out.println("\n5. 实际应用场景模拟:");
        
        // 场景1: 查看多媒体数据但保留
        System.out.println("\n场景1: 查看多媒体数据（保留原文件）");
        T8805SingleMultimediaDataRetrievalUploadCommand viewCommand = new T8805SingleMultimediaDataRetrievalUploadCommand(
            20231201001L, T8805SingleMultimediaDataRetrievalUploadCommand.DeleteFlag.KEEP);
        System.out.println("查看命令: " + viewCommand);
        System.out.println("操作说明: 平台请求上传指定多媒体文件，终端上传后保留本地文件");
        
        // 场景2: 获取多媒体数据并删除
        System.out.println("\n场景2: 获取多媒体数据（删除本地文件）");
        T8805SingleMultimediaDataRetrievalUploadCommand retrieveCommand = new T8805SingleMultimediaDataRetrievalUploadCommand(
            20231201002L, T8805SingleMultimediaDataRetrievalUploadCommand.DeleteFlag.DELETE);
        System.out.println("获取命令: " + retrieveCommand);
        System.out.println("操作说明: 平台请求上传指定多媒体文件，终端上传后删除本地文件以节省存储空间");
        
        // 场景3: 批量处理不同多媒体ID
        System.out.println("\n场景3: 批量处理多个多媒体文件");
        long[] multimediaIds = {100001L, 100002L, 100003L, 100004L, 100005L};
        T8805SingleMultimediaDataRetrievalUploadCommand.DeleteFlag[] flags = {
            T8805SingleMultimediaDataRetrievalUploadCommand.DeleteFlag.KEEP,
            T8805SingleMultimediaDataRetrievalUploadCommand.DeleteFlag.DELETE,
            T8805SingleMultimediaDataRetrievalUploadCommand.DeleteFlag.KEEP,
            T8805SingleMultimediaDataRetrievalUploadCommand.DeleteFlag.DELETE,
            T8805SingleMultimediaDataRetrievalUploadCommand.DeleteFlag.KEEP
        };
        
        for (int i = 0; i < multimediaIds.length; i++) {
            T8805SingleMultimediaDataRetrievalUploadCommand command = new T8805SingleMultimediaDataRetrievalUploadCommand(
                multimediaIds[i], flags[i]);
            System.out.println("  文件" + (i + 1) + ": " + command);
        }
        
        // 场景4: 最大多媒体ID处理
        System.out.println("\n场景4: 处理最大多媒体ID");
        long maxId = 0xFFFFFFFFL; // DWORD最大值
        T8805SingleMultimediaDataRetrievalUploadCommand maxIdCommand = new T8805SingleMultimediaDataRetrievalUploadCommand(
            maxId, T8805SingleMultimediaDataRetrievalUploadCommand.DeleteFlag.DELETE);
        System.out.println("最大ID命令: " + maxIdCommand);
        System.out.println("最大ID值: " + maxId + " (0x" + Long.toHexString(maxId).toUpperCase() + ")");
        
        // 编码所有命令并显示大小
        System.out.println("\n各命令编码后大小:");
        System.out.println("查看命令: " + viewCommand.encode().length() + " 字节");
        System.out.println("获取命令: " + retrieveCommand.encode().length() + " 字节");
        System.out.println("最大ID命令: " + maxIdCommand.encode().length() + " 字节");
        
        // 场景5: 错误处理演示
        System.out.println("\n场景5: 错误处理演示");
        try {
            // 尝试创建无效的多媒体ID
            new T8805SingleMultimediaDataRetrievalUploadCommand(0L, T8805SingleMultimediaDataRetrievalUploadCommand.DeleteFlag.KEEP);
        } catch (IllegalArgumentException e) {
            System.out.println("捕获到预期错误: " + e.getMessage());
        }
        
        try {
            // 尝试解码无效的缓冲区
            Buffer invalidBuffer = Buffer.buffer(new byte[]{0x01, 0x02}); // 长度不足
            T8805SingleMultimediaDataRetrievalUploadCommand.decode(invalidBuffer);
        } catch (IllegalArgumentException e) {
            System.out.println("捕获到预期错误: " + e.getMessage());
        }
        
        System.out.println("\n=== 示例程序结束 ===");
    }
    
    /**
     * 字节数组转十六进制字符串
     */
    private static String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02X ", b));
        }
        return result.toString().trim();
    }
}