package com.jt808.protocol.example;

import com.jt808.protocol.constants.MessageTypes;
import com.jt808.protocol.factory.JT808MessageFactory;
import com.jt808.protocol.message.JT808Message;
import com.jt808.protocol.message.T8803StoredMultimediaDataUploadCommand;
import io.vertx.core.buffer.Buffer;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * T8803存储多媒体数据上传命令示例程序
 * 演示如何创建、编码、解码和使用T8803消息
 */
public class T8803StoredMultimediaDataUploadCommandExample {
    
    public static void main(String[] args) {
        System.out.println("=== T8803存储多媒体数据上传命令示例 ===");
        
        // 1. 基本用法示例
        basicUsageExample();
        
        // 2. 不同类型的上传命令示例
        differentTypesExample();
        
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
        
        LocalDateTime startTime = LocalDateTime.of(2024, 7, 24, 10, 0, 0);
        LocalDateTime endTime = LocalDateTime.of(2024, 7, 24, 11, 0, 0);
        
        T8803StoredMultimediaDataUploadCommand command = new T8803StoredMultimediaDataUploadCommand(
            T8803StoredMultimediaDataUploadCommand.MultimediaType.IMAGE,
            1, // 通道1
            T8803StoredMultimediaDataUploadCommand.EventCode.PLATFORM_COMMAND,
            startTime,
            endTime,
            T8803StoredMultimediaDataUploadCommand.DeleteFlag.KEEP
        );
        
        System.out.println("消息ID: 0x" + Integer.toHexString(command.getMessageId()).toUpperCase());
        System.out.println("多媒体类型: " + command.getMultimediaType().getDescription());
        System.out.println("通道ID: " + command.getChannelId());
        System.out.println("事件编码: " + command.getEventCode().getDescription());
        System.out.println("起始时间: " + command.getStartTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        System.out.println("结束时间: " + command.getEndTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        System.out.println("删除标志: " + command.getDeleteFlag().getDescription());
    }
    
    /**
     * 不同类型的上传命令示例
     */
    private static void differentTypesExample() {
        System.out.println("\n2. 不同类型的上传命令示例:");
        
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime oneHourLater = now.plusHours(1);
        
        // 图像上传命令
        T8803StoredMultimediaDataUploadCommand imageCommand = new T8803StoredMultimediaDataUploadCommand(
            T8803StoredMultimediaDataUploadCommand.MultimediaType.IMAGE,
            1,
            T8803StoredMultimediaDataUploadCommand.EventCode.PLATFORM_COMMAND,
            now,
            oneHourLater,
            T8803StoredMultimediaDataUploadCommand.DeleteFlag.KEEP
        );
        
        // 音频上传命令
        T8803StoredMultimediaDataUploadCommand audioCommand = new T8803StoredMultimediaDataUploadCommand(
            T8803StoredMultimediaDataUploadCommand.MultimediaType.AUDIO,
            2,
            T8803StoredMultimediaDataUploadCommand.EventCode.TIMED_ACTION,
            now,
            oneHourLater,
            T8803StoredMultimediaDataUploadCommand.DeleteFlag.DELETE
        );
        
        // 视频上传命令
        T8803StoredMultimediaDataUploadCommand videoCommand = new T8803StoredMultimediaDataUploadCommand(
            T8803StoredMultimediaDataUploadCommand.MultimediaType.VIDEO,
            3,
            T8803StoredMultimediaDataUploadCommand.EventCode.ROBBERY_ALARM,
            now,
            oneHourLater,
            T8803StoredMultimediaDataUploadCommand.DeleteFlag.KEEP
        );
        
        System.out.println("图像上传命令: " + imageCommand);
        System.out.println("音频上传命令: " + audioCommand);
        System.out.println("视频上传命令: " + videoCommand);
    }
    
    /**
     * 编码解码过程演示
     */
    private static void encodeDecodeExample() {
        System.out.println("\n3. 编码解码过程演示:");
        
        LocalDateTime startTime = LocalDateTime.of(2024, 7, 24, 14, 30, 15);
        LocalDateTime endTime = LocalDateTime.of(2024, 7, 24, 15, 30, 15);
        
        T8803StoredMultimediaDataUploadCommand original = new T8803StoredMultimediaDataUploadCommand(
            T8803StoredMultimediaDataUploadCommand.MultimediaType.VIDEO,
            4,
            T8803StoredMultimediaDataUploadCommand.EventCode.COLLISION_ROLLOVER_ALARM,
            startTime,
            endTime,
            T8803StoredMultimediaDataUploadCommand.DeleteFlag.DELETE
        );
        
        System.out.println("原始消息: " + original);
        
        // 编码
        Buffer encoded = original.encode();
        System.out.println("编码后长度: " + encoded.length() + " 字节");
        System.out.println("编码后数据: " + bytesToHex(encoded.getBytes()));
        
        // 解码
        T8803StoredMultimediaDataUploadCommand decoded = T8803StoredMultimediaDataUploadCommand.decode(encoded);
        System.out.println("解码后消息: " + decoded);
        
        // 验证编码解码一致性
        boolean isEqual = original.equals(decoded);
        System.out.println("编码解码一致性验证: " + (isEqual ? "通过" : "失败"));
    }
    
    /**
     * 工厂模式使用示例
     */
    private static void factoryExample() {
        System.out.println("\n4. 工厂模式使用示例:");
        
        JT808MessageFactory factory = JT808MessageFactory.getInstance();
        
        // 通过工厂创建消息
        JT808Message message = factory.createMessage(MessageTypes.Platform.STORED_MULTIMEDIA_DATA_UPLOAD_COMMAND);
        
        if (message instanceof T8803StoredMultimediaDataUploadCommand) {
            T8803StoredMultimediaDataUploadCommand command = (T8803StoredMultimediaDataUploadCommand) message;
            
            // 设置消息内容
            command.setMultimediaType(T8803StoredMultimediaDataUploadCommand.MultimediaType.IMAGE);
            command.setChannelId(5);
            command.setEventCode(T8803StoredMultimediaDataUploadCommand.EventCode.PLATFORM_COMMAND);
            command.setStartTime(LocalDateTime.now());
            command.setEndTime(LocalDateTime.now().plusMinutes(30));
            command.setDeleteFlag(T8803StoredMultimediaDataUploadCommand.DeleteFlag.KEEP);
            
            System.out.println("通过工厂创建的消息: " + command);
            System.out.println("消息ID: 0x" + Integer.toHexString(command.getMessageId()).toUpperCase());
        }
    }
    
    /**
     * 实际应用场景模拟
     */
    private static void realWorldScenarioExample() {
        System.out.println("\n5. 实际应用场景模拟:");
        
        // 场景1: 平台要求上传报警触发时的图像
        System.out.println("\n场景1: 报警触发图像上传");
        LocalDateTime alarmTime = LocalDateTime.of(2024, 7, 24, 16, 45, 30);
        T8803StoredMultimediaDataUploadCommand alarmImageCommand = new T8803StoredMultimediaDataUploadCommand(
            T8803StoredMultimediaDataUploadCommand.MultimediaType.IMAGE,
            1,
            T8803StoredMultimediaDataUploadCommand.EventCode.ROBBERY_ALARM,
            alarmTime.minusMinutes(5), // 报警前5分钟
            alarmTime.plusMinutes(5),  // 报警后5分钟
            T8803StoredMultimediaDataUploadCommand.DeleteFlag.KEEP
        );
        System.out.println(alarmImageCommand);
        
        // 场景2: 定时上传行车记录视频
        System.out.println("\n场景2: 定时行车记录视频上传");
        LocalDateTime recordTime = LocalDateTime.of(2024, 7, 24, 8, 0, 0);
        T8803StoredMultimediaDataUploadCommand recordVideoCommand = new T8803StoredMultimediaDataUploadCommand(
            T8803StoredMultimediaDataUploadCommand.MultimediaType.VIDEO,
            2,
            T8803StoredMultimediaDataUploadCommand.EventCode.TIMED_ACTION,
            recordTime,
            recordTime.plusHours(2), // 2小时的行车记录
            T8803StoredMultimediaDataUploadCommand.DeleteFlag.DELETE // 上传后删除
        );
        System.out.println(recordVideoCommand);
        
        // 场景3: 碰撞报警音频上传
        System.out.println("\n场景3: 碰撞报警音频上传");
        LocalDateTime collisionTime = LocalDateTime.of(2024, 7, 24, 12, 30, 45);
        T8803StoredMultimediaDataUploadCommand collisionAudioCommand = new T8803StoredMultimediaDataUploadCommand(
            T8803StoredMultimediaDataUploadCommand.MultimediaType.AUDIO,
            3,
            T8803StoredMultimediaDataUploadCommand.EventCode.COLLISION_ROLLOVER_ALARM,
            collisionTime.minusMinutes(2), // 碰撞前2分钟
            collisionTime.plusMinutes(3),  // 碰撞后3分钟
            T8803StoredMultimediaDataUploadCommand.DeleteFlag.KEEP
        );
        System.out.println(collisionAudioCommand);
        
        // 编码所有命令并显示大小
        System.out.println("\n各命令编码后大小:");
        System.out.println("报警图像命令: " + alarmImageCommand.encode().length() + " 字节");
        System.out.println("行车记录视频命令: " + recordVideoCommand.encode().length() + " 字节");
        System.out.println("碰撞音频命令: " + collisionAudioCommand.encode().length() + " 字节");
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