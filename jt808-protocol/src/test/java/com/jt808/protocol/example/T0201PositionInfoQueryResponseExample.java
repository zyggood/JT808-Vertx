    package com.jt808.protocol.example;

import com.jt808.protocol.message.T0201PositionInfoQueryResponse;
import com.jt808.protocol.message.T0200LocationReport;
import com.jt808.protocol.factory.JT808MessageFactory;
import io.vertx.core.buffer.Buffer;

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
    
    public static void main(String[] args) {
        System.out.println("=== T0201位置信息查询应答消息示例 ===");
        
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
        
        System.out.println("\n=== 示例程序执行完成 ===");
    }
    
    /**
     * 演示构造函数的使用
     */
    private static void demonstrateConstructors() {
        System.out.println("\n--- 构造函数示例 ---");
        
        // 默认构造函数
        T0201PositionInfoQueryResponse message1 = new T0201PositionInfoQueryResponse();
        System.out.println("默认构造: " + message1.getMessageDescription());
        System.out.println("消息ID: 0x" + Integer.toHexString(message1.getMessageId()).toUpperCase());
        
        // 创建位置信息汇报
        T0200LocationReport locationReport = createSampleLocationReport();
        
        // 带参数构造函数
        T0201PositionInfoQueryResponse message2 = new T0201PositionInfoQueryResponse(12345, locationReport);
        System.out.println("带参数构造: 应答流水号=" + message2.getResponseSerialNumber());
        System.out.println("位置信息: 纬度=" + message2.getLocationReport().getLatitudeDegrees() + 
                          "°, 经度=" + message2.getLocationReport().getLongitudeDegrees() + "°");
    }
    
    /**
     * 演示静态工厂方法的使用
     */
    private static void demonstrateStaticFactoryMethods() {
        System.out.println("\n--- 静态工厂方法示例 ---");
        
        T0200LocationReport locationReport = createSampleLocationReport();
        
        // 使用create方法
        T0201PositionInfoQueryResponse message1 = T0201PositionInfoQueryResponse.create(54321, locationReport);
        System.out.println("create方法: 应答流水号=" + message1.getResponseSerialNumber());
        
        // 使用createWithPosition方法
        T0201PositionInfoQueryResponse message2 = T0201PositionInfoQueryResponse.createWithPosition(
            98765, 39.908692, 116.397477, 50, 60, 90);
        System.out.println("createWithPosition方法: 应答流水号=" + message2.getResponseSerialNumber());
        System.out.println("位置信息: 纬度=" + message2.getLocationReport().getLatitudeDegrees() + 
                          "°, 经度=" + message2.getLocationReport().getLongitudeDegrees() + 
                          "°, 高程=" + message2.getLocationReport().getAltitude() + "m");
    }
    
    /**
     * 演示编码和解码过程
     */
    private static void demonstrateEncodingDecoding() {
        System.out.println("\n--- 编码解码示例 ---");
        
        // 创建原始消息
        T0200LocationReport locationReport = createSampleLocationReport();
        T0201PositionInfoQueryResponse originalMessage = new T0201PositionInfoQueryResponse(11111, locationReport);
        
        System.out.println("原始消息: " + originalMessage.toString());
        
        // 编码消息
        Buffer encoded = originalMessage.encodeBody();
        System.out.println("编码后长度: " + encoded.length() + " 字节");
        System.out.println("编码数据: " + bytesToHex(encoded.getBytes()));
        
        // 解码消息
        T0201PositionInfoQueryResponse decodedMessage = new T0201PositionInfoQueryResponse();
        decodedMessage.decodeBody(encoded);
        
        System.out.println("解码后消息: " + decodedMessage.toString());
        
        // 验证编解码一致性
        boolean isConsistent = originalMessage.getResponseSerialNumber() == decodedMessage.getResponseSerialNumber() &&
                              Math.abs(originalMessage.getLocationReport().getLatitudeDegrees() - 
                                     decodedMessage.getLocationReport().getLatitudeDegrees()) < 0.000001 &&
                              Math.abs(originalMessage.getLocationReport().getLongitudeDegrees() - 
                                     decodedMessage.getLocationReport().getLongitudeDegrees()) < 0.000001;
        
        System.out.println("编解码一致性检查: " + (isConsistent ? "通过" : "失败"));
    }
    
    /**
     * 演示消息属性和方法
     */
    private static void demonstrateMessageProperties() {
        System.out.println("\n--- 消息属性和方法示例 ---");
        
        T0200LocationReport locationReport = createSampleLocationReport();
        T0201PositionInfoQueryResponse message = new T0201PositionInfoQueryResponse(22222, locationReport);
        
        // 基本属性
        System.out.println("消息ID: 0x" + Integer.toHexString(message.getMessageId()).toUpperCase());
        System.out.println("消息描述: " + message.getMessageDescription());
        System.out.println("应答流水号: " + message.getResponseSerialNumber());
        
        // 位置信息详情
        T0200LocationReport location = message.getLocationReport();
        if (location != null) {
            System.out.println("位置详情:");
            System.out.println("  纬度: " + location.getLatitudeDegrees() + "°");
            System.out.println("  经度: " + location.getLongitudeDegrees() + "°");
            System.out.println("  高程: " + location.getAltitude() + "m");
            System.out.println("  速度: " + location.getSpeed() + " (1/10km/h)");
            System.out.println("  方向: " + location.getDirection() + "°");
            System.out.println("  时间: " + location.getDateTime());
        }
        
        // 修改属性
        message.setResponseSerialNumber(33333);
        System.out.println("修改后应答流水号: " + message.getResponseSerialNumber());
        
        // toString方法
        System.out.println("toString输出: " + message.toString());
        
        // equals和hashCode
        T0201PositionInfoQueryResponse message2 = new T0201PositionInfoQueryResponse(33333, locationReport);
        System.out.println("消息相等性: " + message.equals(message2));
        System.out.println("哈希码相同: " + (message.hashCode() == message2.hashCode()));
    }
    
    /**
     * 演示工厂创建
     */
    private static void demonstrateFactoryCreation() {
        System.out.println("\n--- 工厂创建示例 ---");
        
        try {
            JT808MessageFactory factory = JT808MessageFactory.getInstance();
            T0201PositionInfoQueryResponse message = 
                (T0201PositionInfoQueryResponse) factory.createMessage(0x0201);
            
            System.out.println("工厂创建成功: " + message.getClass().getSimpleName());
            System.out.println("消息ID: 0x" + Integer.toHexString(message.getMessageId()).toUpperCase());
            System.out.println("消息描述: " + message.getMessageDescription());
        } catch (Exception e) {
            System.err.println("工厂创建失败: " + e.getMessage());
        }
    }
    
    /**
     * 演示异常处理
     */
    private static void demonstrateExceptionHandling() {
        System.out.println("\n--- 异常处理示例 ---");
        
        T0201PositionInfoQueryResponse message = new T0201PositionInfoQueryResponse();
        
        // 测试空消息体解码
        try {
            message.decodeBody(null);
        } catch (IllegalArgumentException e) {
            System.out.println("空消息体异常: " + e.getMessage());
        }
        
        // 测试长度不足的消息体解码
        try {
            Buffer shortBuffer = Buffer.buffer().appendByte((byte) 0x01);
            message.decodeBody(shortBuffer);
        } catch (IllegalArgumentException e) {
            System.out.println("长度不足异常: " + e.getMessage());
        }
        
        // 测试只有应答流水号的消息体解码
        try {
            Buffer onlySerialBuffer = Buffer.buffer().appendUnsignedShort(12345);
            message.decodeBody(onlySerialBuffer);
        } catch (IllegalArgumentException e) {
            System.out.println("缺少位置数据异常: " + e.getMessage());
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