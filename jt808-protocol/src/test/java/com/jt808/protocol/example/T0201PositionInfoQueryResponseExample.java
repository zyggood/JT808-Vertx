package com.jt808.protocol.example;

import com.jt808.protocol.message.T0201PositionInfoQueryResponse;
import com.jt808.protocol.message.T0200LocationReport;
import com.jt808.protocol.factory.JT808MessageFactory;
import io.vertx.core.buffer.Buffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;

/**
 * T0201位置信息查询应答消息示例程序
 * 
 * 演示如何使用T0201PositionInfoQueryResponse类来创建、编码和解码位置信息查询应答消息。
 * 
 * @author JT808 Protocol Team
 * @version 1.0
 * @since 1.0
 */
public class T0201PositionInfoQueryResponseExample {
    
    private static final Logger logger = LoggerFactory.getLogger(T0201PositionInfoQueryResponseExample.class);
    
    public static void main(String[] args) {
        logger.info("=== T0201位置信息查询应答消息示例 ===");
        
        // 1. 使用构造函数创建消息
        demonstrateConstructors();
        
        // 2. 使用静态工厂方法创建消息
        demonstrateStaticFactoryMethods();
        
        // 3. 演示编码和解码过程
        demonstrateEncodingDecoding();
        
        // 4. 演示消息属性和方法
        demonstrateMessageProperties();
        
        // 5. 演示工厂创建
        demonstrateFactoryCreation();
        
        // 6. 演示异常处理
        demonstrateExceptionHandling();
        
        logger.info("\n=== 示例程序执行完成 ===");
    }
    
    /**
     * 演示构造函数的使用
     */
    private static void demonstrateConstructors() {
        logger.info("\n--- 构造函数示例 ---");
        
        // 默认构造函数
        T0201PositionInfoQueryResponse message1 = new T0201PositionInfoQueryResponse();
        logger.info("默认构造: {}", message1.getMessageDescription());
        logger.info("消息ID: 0x{}", Integer.toHexString(message1.getMessageId()).toUpperCase());
        
        // 创建位置信息汇报
        T0200LocationReport locationReport = createSampleLocationReport();
        
        // 带参数构造函数
        T0201PositionInfoQueryResponse message2 = new T0201PositionInfoQueryResponse(12345, locationReport);
        logger.info("带参数构造: 应答流水号={}", message2.getResponseSerialNumber());
        logger.info("位置信息: 纬度={}°, 经度={}°", message2.getLocationReport().getLatitudeDegrees(), 
                          message2.getLocationReport().getLongitudeDegrees());
    }
    
    /**
     * 演示静态工厂方法的使用
     */
    private static void demonstrateStaticFactoryMethods() {
        logger.info("\n--- 静态工厂方法示例 ---");
        
        T0200LocationReport locationReport = createSampleLocationReport();
        
        // 使用create方法
        T0201PositionInfoQueryResponse message1 = T0201PositionInfoQueryResponse.create(54321, locationReport);
        logger.info("create方法: 应答流水号={}", message1.getResponseSerialNumber());
        
        // 使用createWithPosition方法
        T0201PositionInfoQueryResponse message2 = T0201PositionInfoQueryResponse.createWithPosition(
            98765, 39.908692, 116.397477, 50, 60, 90);
        logger.info("createWithPosition方法: 应答流水号={}", message2.getResponseSerialNumber());
        logger.info("位置信息: 纬度={}°, 经度={}°, 高程={}m", message2.getLocationReport().getLatitudeDegrees(), 
                          message2.getLocationReport().getLongitudeDegrees(), 
                          message2.getLocationReport().getAltitude());
    }
    
    /**
     * 演示编码和解码过程
     */
    private static void demonstrateEncodingDecoding() {
        logger.info("\n--- 编码解码示例 ---");
        
        // 创建原始消息
        T0200LocationReport locationReport = createSampleLocationReport();
        T0201PositionInfoQueryResponse originalMessage = new T0201PositionInfoQueryResponse(11111, locationReport);
        
        logger.info("原始消息: {}", originalMessage.toString());
        
        // 编码消息
        Buffer encoded = originalMessage.encodeBody();
        logger.info("编码后长度: {} 字节", encoded.length());
        logger.info("编码数据: {}", bytesToHex(encoded.getBytes()));
        
        // 解码消息
        T0201PositionInfoQueryResponse decodedMessage = new T0201PositionInfoQueryResponse();
        decodedMessage.decodeBody(encoded);
        
        logger.info("解码后消息: {}", decodedMessage.toString());
        
        // 验证编解码一致性
        boolean isConsistent = originalMessage.getResponseSerialNumber() == decodedMessage.getResponseSerialNumber() &&
                              Math.abs(originalMessage.getLocationReport().getLatitudeDegrees() - 
                                     decodedMessage.getLocationReport().getLatitudeDegrees()) < 0.000001 &&
                              Math.abs(originalMessage.getLocationReport().getLongitudeDegrees() - 
                                     decodedMessage.getLocationReport().getLongitudeDegrees()) < 0.000001;
        
        logger.info("编解码一致性检查: {}", (isConsistent ? "通过" : "失败"));
    }
    
    /**
     * 演示消息属性和方法
     */
    private static void demonstrateMessageProperties() {
        logger.info("\n--- 消息属性和方法示例 ---");
        
        T0200LocationReport locationReport = createSampleLocationReport();
        T0201PositionInfoQueryResponse message = new T0201PositionInfoQueryResponse(22222, locationReport);
        
        // 基本属性
        logger.info("消息ID: 0x{}", Integer.toHexString(message.getMessageId()).toUpperCase());
        logger.info("消息描述: {}", message.getMessageDescription());
        logger.info("应答流水号: {}", message.getResponseSerialNumber());
        
        // 位置信息详情
        T0200LocationReport location = message.getLocationReport();
        if (location != null) {
            logger.info("位置详情:");
            logger.info("  纬度: {}°", location.getLatitudeDegrees());
            logger.info("  经度: {}°", location.getLongitudeDegrees());
            logger.info("  高程: {}m", location.getAltitude());
            logger.info("  速度: {} (1/10km/h)", location.getSpeed());
            logger.info("  方向: {}°", location.getDirection());
            logger.info("  时间: {}", location.getDateTime());
        }
        
        // 修改属性
        message.setResponseSerialNumber(33333);
        logger.info("修改后应答流水号: {}", message.getResponseSerialNumber());
        
        // toString方法
        logger.info("toString输出: {}", message.toString());
        
        // equals和hashCode
        T0201PositionInfoQueryResponse message2 = new T0201PositionInfoQueryResponse(33333, locationReport);
        logger.info("消息相等性: {}", message.equals(message2));
        logger.info("哈希码相同: {}", (message.hashCode() == message2.hashCode()));
    }
    
    /**
     * 演示工厂创建
     */
    private static void demonstrateFactoryCreation() {
        logger.info("\n--- 工厂创建示例 ---");
        
        try {
            JT808MessageFactory factory = JT808MessageFactory.getInstance();
            T0201PositionInfoQueryResponse message = 
                (T0201PositionInfoQueryResponse) factory.createMessage(0x0201);
            
            logger.info("工厂创建成功: {}", message.getClass().getSimpleName());
            logger.info("消息ID: 0x{}", Integer.toHexString(message.getMessageId()).toUpperCase());
            logger.info("消息描述: {}", message.getMessageDescription());
        } catch (Exception e) {
            logger.error("工厂创建失败: {}", e.getMessage());
        }
    }
    
    /**
     * 演示异常处理
     */
    private static void demonstrateExceptionHandling() {
        logger.info("\n--- 异常处理示例 ---");
        
        T0201PositionInfoQueryResponse message = new T0201PositionInfoQueryResponse();
        
        // 测试空消息体解码
        try {
            message.decodeBody(null);
        } catch (IllegalArgumentException e) {
            logger.info("空消息体异常: {}", e.getMessage());
        }
        
        // 测试长度不足的消息体解码
        try {
            Buffer shortBuffer = Buffer.buffer().appendByte((byte) 0x01);
            message.decodeBody(shortBuffer);
        } catch (IllegalArgumentException e) {
            logger.info("长度不足异常: {}", e.getMessage());
        }
        
        // 测试只有应答流水号的消息体解码
        try {
            Buffer onlySerialBuffer = Buffer.buffer().appendUnsignedShort(12345);
            message.decodeBody(onlySerialBuffer);
        } catch (IllegalArgumentException e) {
            logger.info("缺少位置数据异常: {}", e.getMessage());
        }
    }
    
    /**
     * 创建示例位置信息汇报
     */
    private static T0200LocationReport createSampleLocationReport() {
        T0200LocationReport locationReport = new T0200LocationReport();
        locationReport.setLatitudeDegrees(39.908692);  // 北京天安门纬度
        locationReport.setLongitudeDegrees(116.397477); // 北京天安门经度
        locationReport.setAltitude(50);                 // 高程50米
        locationReport.setSpeed(60);                    // 速度6.0km/h
        locationReport.setDirection(90);                // 方向正东
        locationReport.setDateTime(LocalDateTime.of(2024, 1, 15, 14, 30, 45));
        return locationReport;
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