package com.jt808.protocol.example;

import com.jt808.protocol.constants.MessageTypes;
import com.jt808.protocol.factory.JT808MessageFactory;
import com.jt808.protocol.message.JT808Message;
import com.jt808.protocol.message.T0802StoredMultimediaDataRetrievalResponse;
import io.vertx.core.buffer.Buffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * T0802存储多媒体数据检索应答消息示例
 * 演示如何创建、编码、解码和使用T0802消息
 */
public class T0802StoredMultimediaDataRetrievalResponseExample {
    
    private static final Logger logger = LoggerFactory.getLogger(T0802StoredMultimediaDataRetrievalResponseExample.class);
    
    public static void main(String[] args) {
        logger.info("=== T0802存储多媒体数据检索应答消息示例 ===");
        
        try {
            // 1. 基本消息创建和使用
            demonstrateBasicUsage();
            
            // 2. 不同类型的检索应答
            demonstrateDifferentRetrievalTypes();
            
            // 3. 编码和解码示例
            demonstrateEncodeAndDecode();
            
            // 4. 工厂模式使用
            demonstrateMessageFactory();
            
            // 5. 实际应用场景
            demonstrateRealWorldScenario();
            
        } catch (Exception e) {
            logger.error("示例执行出错", e);
        }
    }
    
    /**
     * 演示基本使用方法
     */
    private static void demonstrateBasicUsage() {
        logger.info("\n--- 基本使用示例 ---");
        
        // 创建空的应答消息
        T0802StoredMultimediaDataRetrievalResponse response = new T0802StoredMultimediaDataRetrievalResponse();
        response.setResponseSerialNumber(12345);
        response.setTotalCount(0);
        
        logger.info("创建空应答消息: {}", response);
        logger.info("消息ID: 0x{}", Integer.toHexString(response.getMessageId()).toUpperCase());
        logger.info("应答流水号: {}", response.getResponseSerialNumber());
        logger.info("总项数: {}", response.getTotalCount());
        
        // 创建带参数的应答消息
        T0802StoredMultimediaDataRetrievalResponse response2 = new T0802StoredMultimediaDataRetrievalResponse(54321, 5);
        logger.info("创建带参数应答消息: 流水号={}, 总项数={}", 
                   response2.getResponseSerialNumber(), response2.getTotalCount());
    }
    
    /**
     * 演示不同类型的检索应答
     */
    private static void demonstrateDifferentRetrievalTypes() {
        logger.info("\n--- 不同类型检索应答示例 ---");
        
        // 1. 图像检索应答
        T0802StoredMultimediaDataRetrievalResponse imageResponse = createImageRetrievalResponse();
        logger.info("图像检索应答: {}", imageResponse);
        
        // 2. 音频检索应答
        T0802StoredMultimediaDataRetrievalResponse audioResponse = createAudioRetrievalResponse();
        logger.info("音频检索应答: {}", audioResponse);
        
        // 3. 视频检索应答
        T0802StoredMultimediaDataRetrievalResponse videoResponse = createVideoRetrievalResponse();
        logger.info("视频检索应答: {}", videoResponse);
        
        // 4. 混合类型检索应答
        T0802StoredMultimediaDataRetrievalResponse mixedResponse = createMixedRetrievalResponse();
        logger.info("混合类型检索应答: {}", mixedResponse);
    }
    
    /**
     * 创建图像检索应答
     */
    private static T0802StoredMultimediaDataRetrievalResponse createImageRetrievalResponse() {
        T0802StoredMultimediaDataRetrievalResponse response = new T0802StoredMultimediaDataRetrievalResponse(10001, 3);
        
        // 添加图像检索项
        for (int i = 1; i <= 3; i++) {
            byte[] locationInfo = createMockLocationInfo(i);
            T0802StoredMultimediaDataRetrievalResponse.MultimediaRetrievalItem item = 
                new T0802StoredMultimediaDataRetrievalResponse.MultimediaRetrievalItem(
                    100000L + i,
                    T0802StoredMultimediaDataRetrievalResponse.MultimediaType.IMAGE,
                    i,
                    T0802StoredMultimediaDataRetrievalResponse.EventCode.PLATFORM_COMMAND,
                    locationInfo
                );
            response.addRetrievalItem(item);
        }
        
        return response;
    }
    
    /**
     * 创建音频检索应答
     */
    private static T0802StoredMultimediaDataRetrievalResponse createAudioRetrievalResponse() {
        T0802StoredMultimediaDataRetrievalResponse response = new T0802StoredMultimediaDataRetrievalResponse(10002, 2);
        
        // 添加音频检索项
        for (int i = 1; i <= 2; i++) {
            byte[] locationInfo = createMockLocationInfo(i + 10);
            T0802StoredMultimediaDataRetrievalResponse.MultimediaRetrievalItem item = 
                new T0802StoredMultimediaDataRetrievalResponse.MultimediaRetrievalItem(
                    200000L + i,
                    T0802StoredMultimediaDataRetrievalResponse.MultimediaType.AUDIO,
                    i,
                    T0802StoredMultimediaDataRetrievalResponse.EventCode.TIMED_ACTION,
                    locationInfo
                );
            response.addRetrievalItem(item);
        }
        
        return response;
    }
    
    /**
     * 创建视频检索应答
     */
    private static T0802StoredMultimediaDataRetrievalResponse createVideoRetrievalResponse() {
        T0802StoredMultimediaDataRetrievalResponse response = new T0802StoredMultimediaDataRetrievalResponse(10003, 1);
        
        // 添加视频检索项
        byte[] locationInfo = createMockLocationInfo(20);
        T0802StoredMultimediaDataRetrievalResponse.MultimediaRetrievalItem item = 
            new T0802StoredMultimediaDataRetrievalResponse.MultimediaRetrievalItem(
                300001L,
                T0802StoredMultimediaDataRetrievalResponse.MultimediaType.VIDEO,
                1,
                T0802StoredMultimediaDataRetrievalResponse.EventCode.ROBBERY_ALARM,
                locationInfo
            );
        response.addRetrievalItem(item);
        
        return response;
    }
    
    /**
     * 创建混合类型检索应答
     */
    private static T0802StoredMultimediaDataRetrievalResponse createMixedRetrievalResponse() {
        T0802StoredMultimediaDataRetrievalResponse response = new T0802StoredMultimediaDataRetrievalResponse(10004, 4);
        
        // 图像项
        byte[] locationInfo1 = createMockLocationInfo(30);
        T0802StoredMultimediaDataRetrievalResponse.MultimediaRetrievalItem imageItem = 
            new T0802StoredMultimediaDataRetrievalResponse.MultimediaRetrievalItem(
                400001L,
                T0802StoredMultimediaDataRetrievalResponse.MultimediaType.IMAGE,
                1,
                T0802StoredMultimediaDataRetrievalResponse.EventCode.PLATFORM_COMMAND,
                locationInfo1
            );
        response.addRetrievalItem(imageItem);
        
        // 音频项
        byte[] locationInfo2 = createMockLocationInfo(31);
        T0802StoredMultimediaDataRetrievalResponse.MultimediaRetrievalItem audioItem = 
            new T0802StoredMultimediaDataRetrievalResponse.MultimediaRetrievalItem(
                400002L,
                T0802StoredMultimediaDataRetrievalResponse.MultimediaType.AUDIO,
                2,
                T0802StoredMultimediaDataRetrievalResponse.EventCode.TIMED_ACTION,
                locationInfo2
            );
        response.addRetrievalItem(audioItem);
        
        // 视频项
        byte[] locationInfo3 = createMockLocationInfo(32);
        T0802StoredMultimediaDataRetrievalResponse.MultimediaRetrievalItem videoItem = 
            new T0802StoredMultimediaDataRetrievalResponse.MultimediaRetrievalItem(
                400003L,
                T0802StoredMultimediaDataRetrievalResponse.MultimediaType.VIDEO,
                3,
                T0802StoredMultimediaDataRetrievalResponse.EventCode.COLLISION_ROLLOVER_ALARM,
                locationInfo3
            );
        response.addRetrievalItem(videoItem);
        
        // 另一个图像项
        byte[] locationInfo4 = createMockLocationInfo(33);
        T0802StoredMultimediaDataRetrievalResponse.MultimediaRetrievalItem imageItem2 = 
            new T0802StoredMultimediaDataRetrievalResponse.MultimediaRetrievalItem(
                400004L,
                T0802StoredMultimediaDataRetrievalResponse.MultimediaType.IMAGE,
                4,
                T0802StoredMultimediaDataRetrievalResponse.EventCode.ROBBERY_ALARM,
                locationInfo4
            );
        response.addRetrievalItem(imageItem2);
        
        return response;
    }
    
    /**
     * 创建模拟位置信息
     */
    private static byte[] createMockLocationInfo(int seed) {
        byte[] locationInfo = new byte[28];
        // 填充一些模拟数据
        for (int i = 0; i < 28; i++) {
            locationInfo[i] = (byte) ((seed + i) % 256);
        }
        return locationInfo;
    }
    
    /**
     * 演示编码和解码
     */
    private static void demonstrateEncodeAndDecode() {
        logger.info("\n--- 编码解码示例 ---");
        
        // 创建原始消息
        T0802StoredMultimediaDataRetrievalResponse original = createMixedRetrievalResponse();
        logger.info("原始消息: {}", original);
        logger.info("检索项数量: {}", original.getRetrievalItems().size());
        
        // 编码
        Buffer encoded = original.encode();
        logger.info("编码后长度: {} 字节", encoded.length());
        logger.info("编码数据: {}", bytesToHex(encoded.getBytes()));
        
        // 解码
        T0802StoredMultimediaDataRetrievalResponse decoded = T0802StoredMultimediaDataRetrievalResponse.decode(encoded);
        logger.info("解码后消息: {}", decoded);
        
        // 验证一致性
        boolean isConsistent = verifyConsistency(original, decoded);
        logger.info("编码解码一致性验证: {}", isConsistent ? "通过" : "失败");
        
        // 详细比较检索项
        compareRetrievalItems(original.getRetrievalItems(), decoded.getRetrievalItems());
    }
    
    /**
     * 验证编码解码一致性
     */
    private static boolean verifyConsistency(T0802StoredMultimediaDataRetrievalResponse original, 
                                           T0802StoredMultimediaDataRetrievalResponse decoded) {
        if (original.getResponseSerialNumber() != decoded.getResponseSerialNumber()) {
            logger.warn("应答流水号不一致: {} vs {}", original.getResponseSerialNumber(), decoded.getResponseSerialNumber());
            return false;
        }
        
        if (original.getTotalCount() != decoded.getTotalCount()) {
            logger.warn("总项数不一致: {} vs {}", original.getTotalCount(), decoded.getTotalCount());
            return false;
        }
        
        if (original.getRetrievalItems().size() != decoded.getRetrievalItems().size()) {
            logger.warn("检索项数量不一致: {} vs {}", original.getRetrievalItems().size(), decoded.getRetrievalItems().size());
            return false;
        }
        
        return true;
    }
    
    /**
     * 比较检索项
     */
    private static void compareRetrievalItems(List<T0802StoredMultimediaDataRetrievalResponse.MultimediaRetrievalItem> original,
                                            List<T0802StoredMultimediaDataRetrievalResponse.MultimediaRetrievalItem> decoded) {
        logger.info("\n检索项详细比较:");
        for (int i = 0; i < original.size(); i++) {
            T0802StoredMultimediaDataRetrievalResponse.MultimediaRetrievalItem originalItem = original.get(i);
            T0802StoredMultimediaDataRetrievalResponse.MultimediaRetrievalItem decodedItem = decoded.get(i);
            
            logger.info("检索项 {}: ", i + 1);
            logger.info("  多媒体ID: {} -> {}", originalItem.getMultimediaId(), decodedItem.getMultimediaId());
            logger.info("  多媒体类型: {} -> {}", originalItem.getMultimediaType(), decodedItem.getMultimediaType());
            logger.info("  通道ID: {} -> {}", originalItem.getChannelId(), decodedItem.getChannelId());
            logger.info("  事件编码: {} -> {}", originalItem.getEventCode(), decodedItem.getEventCode());
            logger.info("  位置信息一致: {}", Arrays.equals(originalItem.getLocationInfo(), decodedItem.getLocationInfo()));
        }
    }
    
    /**
     * 演示工厂模式使用
     */
    private static void demonstrateMessageFactory() {
        logger.info("\n--- JT808MessageFactory示例 ---");
        
        JT808MessageFactory factory = JT808MessageFactory.getInstance();
        
        // 检查是否支持T0802消息
        boolean isSupported = factory.isSupported(MessageTypes.Terminal.STORED_MULTIMEDIA_DATA_RETRIEVAL_RESPONSE);
        logger.info("工厂是否支持0x0802消息: {}", isSupported);
        
        if (isSupported) {
            // 使用工厂创建消息
            JT808Message message = factory.createMessage(MessageTypes.Terminal.STORED_MULTIMEDIA_DATA_RETRIEVAL_RESPONSE);
            logger.info("工厂创建的消息类型: {}", message.getClass().getSimpleName());
            logger.info("消息ID: 0x{}", Integer.toHexString(message.getMessageId()).toUpperCase());
            
            // 转换为具体类型并使用
            if (message instanceof T0802StoredMultimediaDataRetrievalResponse response) {
                response.setResponseSerialNumber(99999);
                response.setTotalCount(10);
                logger.info("设置消息参数后: {}", response);
            }
        } else {
            logger.warn("工厂不支持T0802消息类型");
        }
    }
    
    /**
     * 演示实际应用场景
     */
    private static void demonstrateRealWorldScenario() {
        logger.info("\n--- 实际应用场景示例 ---");
        
        // 场景1: 平台查询车辆某时间段的图像数据
        logger.info("场景1: 查询车辆图像数据应答");
        T0802StoredMultimediaDataRetrievalResponse imageQueryResponse = simulateImageQueryResponse();
        logger.info("图像查询应答: 找到{}个图像文件", imageQueryResponse.getRetrievalItems().size());
        
        // 场景2: 紧急情况下查询报警相关的多媒体数据
        logger.info("\n场景2: 紧急报警多媒体数据查询应答");
        T0802StoredMultimediaDataRetrievalResponse alarmQueryResponse = simulateAlarmQueryResponse();
        logger.info("报警查询应答: 找到{}个相关文件", alarmQueryResponse.getRetrievalItems().size());
        
        // 场景3: 无匹配数据的应答
        logger.info("\n场景3: 无匹配数据的应答");
        T0802StoredMultimediaDataRetrievalResponse emptyResponse = simulateEmptyQueryResponse();
        logger.info("空查询应答: {}", emptyResponse);
        
        // 场景4: 大量数据的分页应答
        logger.info("\n场景4: 大量数据分页应答");
        T0802StoredMultimediaDataRetrievalResponse pageResponse = simulatePagedQueryResponse();
        logger.info("分页应答: 总共{}项，当前返回{}项", 
                   pageResponse.getTotalCount(), pageResponse.getRetrievalItems().size());
    }
    
    /**
     * 模拟图像查询应答
     */
    private static T0802StoredMultimediaDataRetrievalResponse simulateImageQueryResponse() {
        T0802StoredMultimediaDataRetrievalResponse response = new T0802StoredMultimediaDataRetrievalResponse(20001, 5);
        
        // 模拟5个图像文件
        for (int i = 1; i <= 5; i++) {
            byte[] locationInfo = createMockLocationInfo(100 + i);
            T0802StoredMultimediaDataRetrievalResponse.MultimediaRetrievalItem item = 
                new T0802StoredMultimediaDataRetrievalResponse.MultimediaRetrievalItem(
                    500000L + i,
                    T0802StoredMultimediaDataRetrievalResponse.MultimediaType.IMAGE,
                    1, // 主摄像头
                    T0802StoredMultimediaDataRetrievalResponse.EventCode.PLATFORM_COMMAND,
                    locationInfo
                );
            response.addRetrievalItem(item);
        }
        
        return response;
    }
    
    /**
     * 模拟报警查询应答
     */
    private static T0802StoredMultimediaDataRetrievalResponse simulateAlarmQueryResponse() {
        T0802StoredMultimediaDataRetrievalResponse response = new T0802StoredMultimediaDataRetrievalResponse(20002, 3);
        
        // 抢劫报警触发的图像
        byte[] locationInfo1 = createMockLocationInfo(200);
        T0802StoredMultimediaDataRetrievalResponse.MultimediaRetrievalItem alarmImage = 
            new T0802StoredMultimediaDataRetrievalResponse.MultimediaRetrievalItem(
                600001L,
                T0802StoredMultimediaDataRetrievalResponse.MultimediaType.IMAGE,
                1,
                T0802StoredMultimediaDataRetrievalResponse.EventCode.ROBBERY_ALARM,
                locationInfo1
            );
        response.addRetrievalItem(alarmImage);
        
        // 抢劫报警触发的音频
        byte[] locationInfo2 = createMockLocationInfo(201);
        T0802StoredMultimediaDataRetrievalResponse.MultimediaRetrievalItem alarmAudio = 
            new T0802StoredMultimediaDataRetrievalResponse.MultimediaRetrievalItem(
                600002L,
                T0802StoredMultimediaDataRetrievalResponse.MultimediaType.AUDIO,
                1,
                T0802StoredMultimediaDataRetrievalResponse.EventCode.ROBBERY_ALARM,
                locationInfo2
            );
        response.addRetrievalItem(alarmAudio);
        
        // 碰撞报警触发的视频
        byte[] locationInfo3 = createMockLocationInfo(202);
        T0802StoredMultimediaDataRetrievalResponse.MultimediaRetrievalItem collisionVideo = 
            new T0802StoredMultimediaDataRetrievalResponse.MultimediaRetrievalItem(
                600003L,
                T0802StoredMultimediaDataRetrievalResponse.MultimediaType.VIDEO,
                2,
                T0802StoredMultimediaDataRetrievalResponse.EventCode.COLLISION_ROLLOVER_ALARM,
                locationInfo3
            );
        response.addRetrievalItem(collisionVideo);
        
        return response;
    }
    
    /**
     * 模拟空查询应答
     */
    private static T0802StoredMultimediaDataRetrievalResponse simulateEmptyQueryResponse() {
        return new T0802StoredMultimediaDataRetrievalResponse(20003, 0);
    }
    
    /**
     * 模拟分页查询应答
     */
    private static T0802StoredMultimediaDataRetrievalResponse simulatePagedQueryResponse() {
        // 总共有100项数据，但这次只返回前10项
        T0802StoredMultimediaDataRetrievalResponse response = new T0802StoredMultimediaDataRetrievalResponse(20004, 100);
        
        // 返回前10项
        for (int i = 1; i <= 10; i++) {
            byte[] locationInfo = createMockLocationInfo(300 + i);
            T0802StoredMultimediaDataRetrievalResponse.MultimediaType type = 
                i % 3 == 0 ? T0802StoredMultimediaDataRetrievalResponse.MultimediaType.VIDEO :
                i % 2 == 0 ? T0802StoredMultimediaDataRetrievalResponse.MultimediaType.AUDIO :
                T0802StoredMultimediaDataRetrievalResponse.MultimediaType.IMAGE;
            
            T0802StoredMultimediaDataRetrievalResponse.MultimediaRetrievalItem item = 
                new T0802StoredMultimediaDataRetrievalResponse.MultimediaRetrievalItem(
                    700000L + i,
                    type,
                    (i % 4) + 1, // 通道1-4
                    T0802StoredMultimediaDataRetrievalResponse.EventCode.TIMED_ACTION,
                    locationInfo
                );
            response.addRetrievalItem(item);
        }
        
        return response;
    }
    
    /**
     * 字节数组转十六进制字符串
     */
    private static String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < Math.min(bytes.length, 32); i++) { // 只显示前32字节
            result.append(String.format("%02X ", bytes[i]));
        }
        if (bytes.length > 32) {
            result.append("...");
        }
        return result.toString().trim();
    }
}