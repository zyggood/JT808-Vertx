package com.jt808.protocol.example;

import com.jt808.protocol.factory.JT808MessageFactory;
import com.jt808.protocol.message.T8802StoredMultimediaDataRetrieval;
import io.vertx.core.buffer.Buffer;

import java.time.LocalDateTime;

/**
 * T8802存储多媒体数据检索消息示例
 * 演示如何创建、编码、解码和使用存储多媒体数据检索消息
 */
public class T8802StoredMultimediaDataRetrievalExample {

    public static void main(String[] args) {
        System.out.println("=== T8802存储多媒体数据检索消息示例 ===");
        
        // 示例1：检索所有图像数据（不按时间范围）
        demonstrateFullTimeRangeRetrieval();
        
        System.out.println();
        
        // 示例2：检索指定时间范围内的视频数据
        demonstrateTimeRangeRetrieval();
        
        System.out.println();
        
        // 示例3：使用LocalDateTime创建检索消息
        demonstrateLocalDateTimeRetrieval();
        
        System.out.println();
        
        // 示例4：演示不同类型的检索消息
        demonstrateDifferentRetrievalTypes();
        
        System.out.println();
        
        // 示例5：演示消息的编码和解码
        demonstrateEncodingAndDecoding();
        
        System.out.println();
        
        // 示例6：演示工厂模式创建消息
        demonstrateFactoryUsage();
    }

    /**
     * 演示检索所有数据（不按时间范围）
     */
    private static void demonstrateFullTimeRangeRetrieval() {
        System.out.println("--- 示例1：检索所有图像数据（不按时间范围） ---");
        
        // 创建检索所有通道的图像数据消息
        T8802StoredMultimediaDataRetrieval message = T8802StoredMultimediaDataRetrieval.createRetrievalMessage(
                T8802StoredMultimediaDataRetrieval.MultimediaType.IMAGE,
                0, // 0表示所有通道
                T8802StoredMultimediaDataRetrieval.EventCode.PLATFORM_COMMAND
        );
        
        System.out.println("消息ID: 0x" + Integer.toHexString(message.getMessageId()).toUpperCase());
        System.out.println("多媒体类型: " + message.getMultimediaTypeDescription());
        System.out.println("通道: " + (message.isAllChannels() ? "所有通道" : "通道" + message.getChannelId()));
        System.out.println("事件: " + message.getEventCodeDescription());
        System.out.println("时间范围: " + (message.isFullTimeRange() ? "全部" : message.getStartTime() + "~" + message.getEndTime()));
        System.out.println("消息描述: " + message.getMessageDescription());
        System.out.println("消息详情: " + message);
    }

    /**
     * 演示指定时间范围的检索
     */
    private static void demonstrateTimeRangeRetrieval() {
        System.out.println("--- 示例2：检索指定时间范围内的视频数据 ---");
        
        // 创建检索指定时间范围内通道1的视频数据消息
        T8802StoredMultimediaDataRetrieval message = T8802StoredMultimediaDataRetrieval.createRetrievalMessage(
                T8802StoredMultimediaDataRetrieval.MultimediaType.VIDEO,
                1, // 通道1
                T8802StoredMultimediaDataRetrieval.EventCode.ROBBERY_ALARM,
                "23-12-25-08-00-00", // 起始时间：2023年12月25日08:00:00
                "23-12-25-18-00-00"  // 结束时间：2023年12月25日18:00:00
        );
        
        System.out.println("消息ID: 0x" + Integer.toHexString(message.getMessageId()).toUpperCase());
        System.out.println("多媒体类型: " + message.getMultimediaTypeDescription());
        System.out.println("通道: " + (message.isAllChannels() ? "所有通道" : "通道" + message.getChannelId()));
        System.out.println("事件: " + message.getEventCodeDescription());
        System.out.println("起始时间: " + message.getStartTime());
        System.out.println("结束时间: " + message.getEndTime());
        System.out.println("时间范围: " + (message.isFullTimeRange() ? "全部" : "指定范围"));
        System.out.println("消息描述: " + message.getMessageDescription());
    }

    /**
     * 演示使用LocalDateTime创建检索消息
     */
    private static void demonstrateLocalDateTimeRetrieval() {
        System.out.println("--- 示例3：使用LocalDateTime创建检索消息 ---");
        
        LocalDateTime startTime = LocalDateTime.of(2023, 12, 25, 10, 30, 45);
        LocalDateTime endTime = LocalDateTime.of(2023, 12, 25, 16, 30, 45);
        
        T8802StoredMultimediaDataRetrieval message = T8802StoredMultimediaDataRetrieval.createRetrievalMessage(
                T8802StoredMultimediaDataRetrieval.MultimediaType.AUDIO,
                2, // 通道2
                T8802StoredMultimediaDataRetrieval.EventCode.COLLISION_ROLLOVER_ALARM,
                startTime,
                endTime
        );
        
        System.out.println("消息ID: 0x" + Integer.toHexString(message.getMessageId()).toUpperCase());
        System.out.println("多媒体类型: " + message.getMultimediaTypeDescription());
        System.out.println("通道: 通道" + message.getChannelId());
        System.out.println("事件: " + message.getEventCodeDescription());
        System.out.println("起始时间: " + message.getStartTime());
        System.out.println("结束时间: " + message.getEndTime());
        
        // 演示时间解析
        LocalDateTime parsedStartTime = message.parseDateTime(message.getStartTime());
        LocalDateTime parsedEndTime = message.parseDateTime(message.getEndTime());
        System.out.println("解析的起始时间: " + parsedStartTime);
        System.out.println("解析的结束时间: " + parsedEndTime);
    }

    /**
     * 演示不同类型的检索消息
     */
    private static void demonstrateDifferentRetrievalTypes() {
        System.out.println("--- 示例4：演示不同类型的检索消息 ---");
        
        // 图像检索
        T8802StoredMultimediaDataRetrieval imageMessage = T8802StoredMultimediaDataRetrieval.createRetrievalMessage(
                T8802StoredMultimediaDataRetrieval.MultimediaType.IMAGE,
                0,
                T8802StoredMultimediaDataRetrieval.EventCode.PLATFORM_COMMAND
        );
        System.out.println("图像检索: " + imageMessage.getMessageDescription());
        
        // 音频检索
        T8802StoredMultimediaDataRetrieval audioMessage = T8802StoredMultimediaDataRetrieval.createRetrievalMessage(
                T8802StoredMultimediaDataRetrieval.MultimediaType.AUDIO,
                1,
                T8802StoredMultimediaDataRetrieval.EventCode.TIMED_ACTION
        );
        System.out.println("音频检索: " + audioMessage.getMessageDescription());
        
        // 视频检索
        T8802StoredMultimediaDataRetrieval videoMessage = T8802StoredMultimediaDataRetrieval.createRetrievalMessage(
                T8802StoredMultimediaDataRetrieval.MultimediaType.VIDEO,
                2,
                T8802StoredMultimediaDataRetrieval.EventCode.ROBBERY_ALARM,
                "23-12-25-09-00-00",
                "23-12-25-17-00-00"
        );
        System.out.println("视频检索: " + videoMessage.getMessageDescription());
        
        // 碰撞侧翻报警触发的检索
        T8802StoredMultimediaDataRetrieval alarmMessage = T8802StoredMultimediaDataRetrieval.createRetrievalMessage(
                T8802StoredMultimediaDataRetrieval.MultimediaType.IMAGE,
                3,
                T8802StoredMultimediaDataRetrieval.EventCode.COLLISION_ROLLOVER_ALARM,
                "23-12-25-12-00-00",
                "23-12-25-14-00-00"
        );
        System.out.println("报警检索: " + alarmMessage.getMessageDescription());
    }

    /**
     * 演示消息的编码和解码
     */
    private static void demonstrateEncodingAndDecoding() {
        System.out.println("--- 示例5：演示消息的编码和解码 ---");
        
        // 创建原始消息
        T8802StoredMultimediaDataRetrieval originalMessage = T8802StoredMultimediaDataRetrieval.createRetrievalMessage(
                T8802StoredMultimediaDataRetrieval.MultimediaType.VIDEO,
                1,
                T8802StoredMultimediaDataRetrieval.EventCode.TIMED_ACTION,
                "23-12-25-10-30-45",
                "23-12-25-18-30-45"
        );
        
        System.out.println("原始消息: " + originalMessage.getMessageDescription());
        
        // 编码消息体
        Buffer encodedBody = originalMessage.encodeBody();
        System.out.println("编码后的消息体长度: " + encodedBody.length() + " 字节");
        System.out.println("编码后的消息体(十六进制): " + bytesToHex(encodedBody.getBytes()));
        
        // 解析编码内容
        System.out.println("\n编码内容解析:");
        System.out.println("  多媒体类型: " + encodedBody.getUnsignedByte(0) + " (" + 
                T8802StoredMultimediaDataRetrieval.MultimediaType.fromValue(encodedBody.getUnsignedByte(0)).getDescription() + ")");
        System.out.println("  通道ID: " + encodedBody.getUnsignedByte(1));
        System.out.println("  事件项编码: " + encodedBody.getUnsignedByte(2) + " (" + 
                T8802StoredMultimediaDataRetrieval.EventCode.fromValue(encodedBody.getUnsignedByte(2)).getDescription() + ")");
        
        System.out.println("  起始时间BCD: " + 
                String.format("%02X-%02X-%02X-%02X-%02X-%02X",
                        encodedBody.getUnsignedByte(3), encodedBody.getUnsignedByte(4), encodedBody.getUnsignedByte(5),
                        encodedBody.getUnsignedByte(6), encodedBody.getUnsignedByte(7), encodedBody.getUnsignedByte(8)));
        
        System.out.println("  结束时间BCD: " + 
                String.format("%02X-%02X-%02X-%02X-%02X-%02X",
                        encodedBody.getUnsignedByte(9), encodedBody.getUnsignedByte(10), encodedBody.getUnsignedByte(11),
                        encodedBody.getUnsignedByte(12), encodedBody.getUnsignedByte(13), encodedBody.getUnsignedByte(14)));
        
        // 解码消息
        T8802StoredMultimediaDataRetrieval decodedMessage = new T8802StoredMultimediaDataRetrieval();
        decodedMessage.decodeBody(encodedBody);
        
        System.out.println("\n解码后的消息: " + decodedMessage.getMessageDescription());
        System.out.println("编码解码一致性: " + originalMessage.equals(decodedMessage));
    }

    /**
     * 演示工厂模式创建消息
     */
    private static void demonstrateFactoryUsage() {
        System.out.println("--- 示例6：演示工厂模式创建消息 ---");
        
        JT808MessageFactory factory = JT808MessageFactory.getInstance();
        
        // 检查是否支持T8802消息
        boolean isSupported = factory.isSupported(0x8802);
        System.out.println("工厂是否支持0x8802消息: " + isSupported);
        
        if (isSupported) {
            // 使用工厂创建消息
            T8802StoredMultimediaDataRetrieval factoryMessage = 
                    (T8802StoredMultimediaDataRetrieval) factory.createMessage(0x8802);
            
            // 设置消息参数
            factoryMessage.setMultimediaType(T8802StoredMultimediaDataRetrieval.MultimediaType.IMAGE.getValue());
            factoryMessage.setChannelId(0);
            factoryMessage.setEventCode(T8802StoredMultimediaDataRetrieval.EventCode.PLATFORM_COMMAND.getValue());
            factoryMessage.setStartTime("23-12-25-08-00-00");
            factoryMessage.setEndTime("23-12-25-20-00-00");
            
            System.out.println("工厂创建的消息: " + factoryMessage.getMessageDescription());
            
            // 使用工厂编码消息
            Buffer encodedMessage = factory.encodeMessage(factoryMessage);
            System.out.println("工厂编码的完整消息长度: " + encodedMessage.length() + " 字节");
            
            try {
                // 使用工厂解码消息
                T8802StoredMultimediaDataRetrieval parsedMessage = 
                        (T8802StoredMultimediaDataRetrieval) factory.parseMessage(encodedMessage);
                System.out.println("工厂解码的消息: " + parsedMessage.getMessageDescription());
                System.out.println("工厂编码解码一致性: " + factoryMessage.equals(parsedMessage));
            } catch (Exception e) {
                System.out.println("工厂解码失败: " + e.getMessage());
            }
        }
        
        // 显示所有支持的消息ID
        System.out.println("\n工厂支持的消息ID数量: " + factory.getSupportedMessageIds().size());
        System.out.println("是否包含0x8802: " + factory.getSupportedMessageIds().contains(0x8802));
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

    /**
     * 演示枚举的使用
     */
    private static void demonstrateEnums() {
        System.out.println("\n=== 枚举使用演示 ===");
        
        System.out.println("多媒体类型枚举:");
        for (T8802StoredMultimediaDataRetrieval.MultimediaType type : 
                T8802StoredMultimediaDataRetrieval.MultimediaType.values()) {
            System.out.println("  " + type.getValue() + ": " + type.getDescription());
        }
        
        System.out.println("\n事件项编码枚举:");
        for (T8802StoredMultimediaDataRetrieval.EventCode code : 
                T8802StoredMultimediaDataRetrieval.EventCode.values()) {
            System.out.println("  " + code.getValue() + ": " + code.getDescription());
        }
    }

    /**
     * 演示实际应用场景
     */
    private static void demonstrateRealWorldScenarios() {
        System.out.println("\n=== 实际应用场景演示 ===");
        
        System.out.println("\n场景1：查询昨天的所有报警图像");
        LocalDateTime yesterday = LocalDateTime.now().minusDays(1);
        LocalDateTime startOfDay = yesterday.withHour(0).withMinute(0).withSecond(0);
        LocalDateTime endOfDay = yesterday.withHour(23).withMinute(59).withSecond(59);
        
        T8802StoredMultimediaDataRetrieval alarmImages = T8802StoredMultimediaDataRetrieval.createRetrievalMessage(
                T8802StoredMultimediaDataRetrieval.MultimediaType.IMAGE,
                0, // 所有通道
                T8802StoredMultimediaDataRetrieval.EventCode.ROBBERY_ALARM,
                startOfDay,
                endOfDay
        );
        System.out.println(alarmImages.getMessageDescription());
        
        System.out.println("\n场景2：查询指定通道的定时录像");
        T8802StoredMultimediaDataRetrieval timedVideos = T8802StoredMultimediaDataRetrieval.createRetrievalMessage(
                T8802StoredMultimediaDataRetrieval.MultimediaType.VIDEO,
                1, // 通道1
                T8802StoredMultimediaDataRetrieval.EventCode.TIMED_ACTION
        );
        System.out.println(timedVideos.getMessageDescription());
        
        System.out.println("\n场景3：查询碰撞事故相关的所有多媒体数据");
        T8802StoredMultimediaDataRetrieval crashData = T8802StoredMultimediaDataRetrieval.createRetrievalMessage(
                T8802StoredMultimediaDataRetrieval.MultimediaType.IMAGE,
                0, // 所有通道
                T8802StoredMultimediaDataRetrieval.EventCode.COLLISION_ROLLOVER_ALARM,
                "23-12-25-14-30-00", // 事故发生前30分钟
                "23-12-25-15-30-00"  // 事故发生后30分钟
        );
        System.out.println(crashData.getMessageDescription());
    }
}