package com.jt808.protocol.example;

import com.jt808.protocol.message.T8202TemporaryLocationTrackingControl;
import com.jt808.protocol.factory.JT808MessageFactory;
import io.vertx.core.buffer.Buffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * T8202临时位置跟踪控制消息示例
 * 
 * 本示例展示如何使用T8202TemporaryLocationTrackingControl类：
 * 1. 创建开始跟踪和停止跟踪的消息
 * 2. 消息的编解码过程
 * 3. 工厂模式创建消息
 * 4. 完整的消息处理流程
 * 
 * @author JT808 Protocol Team
 * @version 1.0
 * @since 1.0
 */
public class T8202TemporaryLocationTrackingControlExample {
    
    private static final Logger logger = LoggerFactory.getLogger(T8202TemporaryLocationTrackingControlExample.class);
    
    public static void main(String[] args) {
        logger.info("=== T8202临时位置跟踪控制消息示例 ===");
        
        try {
            // 1. 基本消息创建和使用
            demonstrateBasicUsage();
            
            // 2. 工厂方法创建消息
            demonstrateFactoryMethods();
            
            // 3. 消息编解码过程
            demonstrateEncodeDecodeProcess();
            
            // 4. 使用JT808MessageFactory
            demonstrateMessageFactory();
            
            // 5. 实际应用场景
            demonstrateRealWorldScenarios();
            
            // 6. 异常处理示例
            demonstrateExceptionHandling();
            
        } catch (Exception e) {
            logger.error("示例执行过程中发生异常", e);
            logger.error("示例执行失败: {}", e.getMessage());
        }
    }
    
    /**
     * 演示基本使用方法
     */
    private static void demonstrateBasicUsage() {
        logger.info("\n--- 基本使用示例 ---");
        
        // 创建开始跟踪的消息
        T8202TemporaryLocationTrackingControl startMessage = 
            new T8202TemporaryLocationTrackingControl(30, 3600);
        
        logger.info("开始跟踪消息: {}", startMessage);
        logger.info("消息ID: 0x{}", Integer.toHexString(startMessage.getMessageId()).toUpperCase());
        logger.info("时间间隔: {} 秒", startMessage.getTimeInterval());
        logger.info("有效期: {} 秒", startMessage.getValidityPeriod());
        logger.info("跟踪状态: {}", startMessage.getTrackingStatusDescription());
        logger.info("是否停止跟踪: {}", startMessage.isStopTracking());
        
        // 创建停止跟踪的消息
        T8202TemporaryLocationTrackingControl stopMessage = 
            new T8202TemporaryLocationTrackingControl(0, 0);
        
        logger.info("\n停止跟踪消息: {}", stopMessage);
        logger.info("跟踪状态: {}", stopMessage.getTrackingStatusDescription());
        logger.info("是否停止跟踪: {}", stopMessage.isStopTracking());
    }
    
    /**
     * 演示工厂方法
     */
    private static void demonstrateFactoryMethods() {
        logger.info("\n--- 工厂方法示例 ---");
        
        // 使用工厂方法创建开始跟踪消息
        T8202TemporaryLocationTrackingControl startTracking = 
            T8202TemporaryLocationTrackingControl.createStartTracking(60, 7200);
        
        logger.info("工厂创建的开始跟踪消息: {}", startTracking.getTrackingStatusDescription());
        
        // 使用工厂方法创建停止跟踪消息
        T8202TemporaryLocationTrackingControl stopTracking = 
            T8202TemporaryLocationTrackingControl.createStopTracking();
        
        logger.info("工厂创建的停止跟踪消息: {}", stopTracking.getTrackingStatusDescription());
        
        // 展示不同的跟踪间隔
        logger.info("\n不同跟踪间隔示例:");
        int[] intervals = {10, 30, 60, 120, 300};
        long validityPeriod = 3600; // 1小时
        
        for (int interval : intervals) {
            T8202TemporaryLocationTrackingControl msg = 
                T8202TemporaryLocationTrackingControl.createStartTracking(interval, validityPeriod);
            logger.info("  间隔 {} 秒: {}", interval, msg.getTrackingStatusDescription());
        }
    }
    
    /**
     * 演示编解码过程
     */
    private static void demonstrateEncodeDecodeProcess() {
        logger.info("\n--- 编解码过程示例 ---");
        
        // 创建原始消息
        T8202TemporaryLocationTrackingControl originalMessage = 
            T8202TemporaryLocationTrackingControl.createStartTracking(45, 1800);
        
        logger.info("原始消息: {}", originalMessage);
        
        // 编码消息体
        Buffer encodedBuffer = originalMessage.encodeBody();
        logger.info("编码后的数据长度: {} 字节", encodedBuffer.length());
        logger.info("编码后的数据 (十六进制): {}", bytesToHex(encodedBuffer.getBytes()));
        
        // 解码消息体
        T8202TemporaryLocationTrackingControl decodedMessage = 
            new T8202TemporaryLocationTrackingControl();
        decodedMessage.decodeBody(encodedBuffer);
        
        logger.info("解码后的消息: {}", decodedMessage);
        
        // 验证编解码一致性
        boolean isConsistent = originalMessage.equals(decodedMessage);
        logger.info("编解码一致性验证: {}", (isConsistent ? "通过" : "失败"));
        
        if (isConsistent) {
            logger.info("✓ 时间间隔一致: {} == {}", originalMessage.getTimeInterval(), decodedMessage.getTimeInterval());
            logger.info("✓ 有效期一致: {} == {}", originalMessage.getValidityPeriod(), decodedMessage.getValidityPeriod());
        }
    }
    
    /**
     * 演示使用JT808MessageFactory
     */
    private static void demonstrateMessageFactory() {
        logger.info("\n--- JT808MessageFactory示例 ---");
        
        JT808MessageFactory factory = JT808MessageFactory.getInstance();
        
        // 检查是否支持T8202消息
        boolean isSupported = factory.isSupported(0x8202);
        logger.info("工厂是否支持0x8202消息: {}", isSupported);
        
        if (!isSupported) {
            // 注册T8202消息类型
            factory.registerMessage(0x8202, T8202TemporaryLocationTrackingControl::new);
            logger.info("已注册T8202消息类型到工厂");
        }
        
        // 使用工厂创建消息
        T8202TemporaryLocationTrackingControl factoryMessage = 
            (T8202TemporaryLocationTrackingControl) factory.createMessage(0x8202);
        
        factoryMessage.setTimeInterval(90);
        factoryMessage.setValidityPeriod(5400);
        
        logger.info("工厂创建的消息: {}", factoryMessage);
        
        // 注意：工厂编码需要完整的消息头，这里只演示消息体编码
        Buffer messageBody = factoryMessage.encodeBody();
        logger.info("工厂创建消息的消息体长度: {} 字节", messageBody.length());
        logger.info("消息体数据 (十六进制): {}", bytesToHex(messageBody.getBytes()));
    }
    
    /**
     * 演示实际应用场景
     */
    private static void demonstrateRealWorldScenarios() {
        logger.info("\n--- 实际应用场景示例 ---");
        
        // 场景1: 紧急情况下的高频跟踪
        logger.info("场景1: 紧急情况高频跟踪");
        T8202TemporaryLocationTrackingControl emergencyTracking = 
            T8202TemporaryLocationTrackingControl.createStartTracking(5, 600); // 5秒间隔，10分钟有效期
        logger.info("  {}", emergencyTracking.getTrackingStatusDescription());
        
        // 场景2: 日常监控
        logger.info("\n场景2: 日常监控");
        T8202TemporaryLocationTrackingControl dailyMonitoring = 
            T8202TemporaryLocationTrackingControl.createStartTracking(300, 28800); // 5分钟间隔，8小时有效期
        logger.info("  {}", dailyMonitoring.getTrackingStatusDescription());
        
        // 场景3: 长途运输监控
        logger.info("\n场景3: 长途运输监控");
        T8202TemporaryLocationTrackingControl longDistanceTracking = 
            T8202TemporaryLocationTrackingControl.createStartTracking(120, 86400); // 2分钟间隔，24小时有效期
        logger.info("  {}", longDistanceTracking.getTrackingStatusDescription());
        
        // 场景4: 停止所有跟踪
        logger.info("\n场景4: 停止跟踪");
        T8202TemporaryLocationTrackingControl stopAllTracking = 
            T8202TemporaryLocationTrackingControl.createStopTracking();
        logger.info("  {}", stopAllTracking.getTrackingStatusDescription());
        
        // 展示时间转换
        logger.info("\n时间转换示例:");
        logger.info("  600秒 = {} 分钟", (600 / 60));
        logger.info("  28800秒 = {} 小时", (28800 / 3600));
        logger.info("  86400秒 = {} 天", (86400 / 86400));
    }
    
    /**
     * 演示异常处理
     */
    private static void demonstrateExceptionHandling() {
        logger.info("\n--- 异常处理示例 ---");
        
        // 测试无效参数异常
        try {
            T8202TemporaryLocationTrackingControl.createStartTracking(0, 3600);
        } catch (IllegalArgumentException e) {
            logger.info("捕获到预期异常 - 无效时间间隔: {}", e.getMessage());
        }
        
        try {
            T8202TemporaryLocationTrackingControl.createStartTracking(30, 0);
        } catch (IllegalArgumentException e) {
            logger.info("捕获到预期异常 - 无效有效期: {}", e.getMessage());
        }
        
        // 测试解码异常
        try {
            T8202TemporaryLocationTrackingControl message = new T8202TemporaryLocationTrackingControl();
            message.decodeBody(null);
        } catch (IllegalArgumentException e) {
            logger.info("捕获到预期异常 - 空消息体: {}", e.getMessage());
        }
        
        try {
            T8202TemporaryLocationTrackingControl message = new T8202TemporaryLocationTrackingControl();
            Buffer shortBuffer = Buffer.buffer();
            shortBuffer.appendUnsignedShort(30); // 只有2字节
            message.decodeBody(shortBuffer);
        } catch (IllegalArgumentException e) {
            logger.info("捕获到预期异常 - 消息体长度不足: {}", e.getMessage());
        }
        
        // 测试Setter异常
        try {
            T8202TemporaryLocationTrackingControl message = new T8202TemporaryLocationTrackingControl();
            message.setTimeInterval(-1);
        } catch (IllegalArgumentException e) {
            logger.info("捕获到预期异常 - 负数时间间隔: {}", e.getMessage());
        }
        
        try {
            T8202TemporaryLocationTrackingControl message = new T8202TemporaryLocationTrackingControl();
            message.setValidityPeriod(-1);
        } catch (IllegalArgumentException e) {
            logger.info("捕获到预期异常 - 负数有效期: {}", e.getMessage());
        }
    }
    
    /**
     * 将字节数组转换为十六进制字符串
     * 
     * @param bytes 字节数组
     * @return 十六进制字符串
     */
    private static String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02X ", b));
        }
        return result.toString().trim();
    }
}