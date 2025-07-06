package com.jt808.protocol.example;

import com.jt808.protocol.message.T8203ManualAlarmConfirmation;
import io.vertx.core.buffer.Buffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * T8203人工确认报警消息使用示例
 * 
 * 本示例演示了如何使用T8203人工确认报警消息，包括：
 * 1. 基本消息创建和使用
 * 2. 工厂方法创建消息
 * 3. 消息的编解码过程
 * 4. 报警类型的处理
 * 5. 实际应用场景
 * 6. 异常处理
 * 
 * @author JT808 Protocol Team
 * @version 1.0
 */
public class T8203ManualAlarmConfirmationExample {
    
    private static final Logger logger = LoggerFactory.getLogger(T8203ManualAlarmConfirmationExample.class);
    
    public static void main(String[] args) {
        logger.info("=== T8203人工确认报警消息功能演示 ===");
        
        try {
            // 1. 基本消息创建和使用
            demonstrateBasicUsage();
            
            // 2. 工厂方法创建消息
            demonstrateFactoryMethods();
            
            // 3. 消息编解码过程
            demonstrateEncodeDecode();
            
            // 4. 报警类型处理
            demonstrateAlarmTypes();
            
            // 5. 实际应用场景
            demonstrateRealWorldScenarios();
            
            // 6. 异常处理
            demonstrateExceptionHandling();
            
        } catch (Exception e) {
            logger.error("示例执行过程中发生异常", e);
        }
        
        logger.info("=== T8203人工确认报警消息演示完成 ===");
    }
    
    /**
     * 演示基本消息创建和使用
     */
    private static void demonstrateBasicUsage() {
        logger.info("\n--- 1. 基本消息创建和使用 ---");
        
        // 创建消息实例
        T8203ManualAlarmConfirmation message = new T8203ManualAlarmConfirmation();
        logger.info("消息ID: 0x{}", Integer.toHexString(message.getMessageId()).toUpperCase());
        logger.info("消息描述: {}", message.getMessageDescription());
        
        // 设置消息参数
        message.setAlarmSequenceNumber(12345);
        message.setConfirmationAlarmType(T8203ManualAlarmConfirmation.AlarmConfirmationType.EMERGENCY_ALARM);
        
        logger.info("报警消息流水号: {}", message.getAlarmSequenceNumber());
        logger.info("确认报警类型: 0x{}", Long.toHexString(message.getConfirmationAlarmType()).toUpperCase());
        logger.info("确认状态描述: {}", message.getConfirmationDescription());
        logger.info("是否确认所有报警: {}", message.isConfirmAllAlarms());
    }
    
    /**
     * 演示工厂方法创建消息
     */
    private static void demonstrateFactoryMethods() {
        logger.info("\n--- 2. 工厂方法创建消息 ---");
        
        // 创建确认指定流水号报警的消息
        T8203ManualAlarmConfirmation specificMessage = T8203ManualAlarmConfirmation.createConfirmSpecificAlarm(
            9999, T8203ManualAlarmConfirmation.AlarmConfirmationType.DANGER_WARNING);
        logger.info("确认指定报警: {}", specificMessage.getConfirmationDescription());
        
        // 创建确认所有指定类型报警的消息
        T8203ManualAlarmConfirmation allMessage = T8203ManualAlarmConfirmation.createConfirmAllAlarms(
            T8203ManualAlarmConfirmation.AlarmConfirmationType.AREA_ALARM);
        logger.info("确认所有报警: {}", allMessage.getConfirmationDescription());
        logger.info("是否确认所有报警: {}", allMessage.isConfirmAllAlarms());
    }
    
    /**
     * 演示消息编解码过程
     */
    private static void demonstrateEncodeDecode() {
        logger.info("\n--- 3. 消息编解码过程 ---");
        
        // 创建原始消息
        T8203ManualAlarmConfirmation originalMessage = new T8203ManualAlarmConfirmation(
            0x5678, T8203ManualAlarmConfirmation.AlarmConfirmationType.ILLEGAL_IGNITION_ALARM);
        
        logger.info("原始消息: {}", originalMessage.toString());
        
        // 编码消息体
        Buffer encodedBody = originalMessage.encodeBody();
        logger.info("编码后消息体长度: {} 字节", encodedBody.length());
        logger.info("编码后消息体内容: {}", bytesToHex(encodedBody.getBytes()));
        
        // 解码消息体
        T8203ManualAlarmConfirmation decodedMessage = new T8203ManualAlarmConfirmation();
        decodedMessage.decodeBody(encodedBody);
        
        logger.info("解码后消息: {}", decodedMessage.toString());
        
        // 验证编解码一致性
        boolean isConsistent = originalMessage.equals(decodedMessage);
        logger.info("编解码一致性验证: {}", isConsistent ? "通过" : "失败");
    }
    
    /**
     * 演示报警类型处理
     */
    private static void demonstrateAlarmTypes() {
        logger.info("\n--- 4. 报警类型处理 ---");
        
        // 演示各种报警类型
        long[] alarmTypes = {
            T8203ManualAlarmConfirmation.AlarmConfirmationType.EMERGENCY_ALARM,
            T8203ManualAlarmConfirmation.AlarmConfirmationType.DANGER_WARNING,
            T8203ManualAlarmConfirmation.AlarmConfirmationType.AREA_ALARM,
            T8203ManualAlarmConfirmation.AlarmConfirmationType.ROUTE_ALARM,
            T8203ManualAlarmConfirmation.AlarmConfirmationType.DRIVING_TIME_ALARM,
            T8203ManualAlarmConfirmation.AlarmConfirmationType.ILLEGAL_IGNITION_ALARM,
            T8203ManualAlarmConfirmation.AlarmConfirmationType.ILLEGAL_DISPLACEMENT_ALARM
        };
        
        for (long alarmType : alarmTypes) {
            String description = T8203ManualAlarmConfirmation.AlarmConfirmationType.getAlarmTypeDescription(alarmType);
            logger.info("报警类型 0x{}: {}", Long.toHexString(alarmType).toUpperCase(), description);
        }
        
        // 演示组合报警类型
        long combinedAlarmType = T8203ManualAlarmConfirmation.AlarmConfirmationType.EMERGENCY_ALARM |
                                T8203ManualAlarmConfirmation.AlarmConfirmationType.DANGER_WARNING |
                                T8203ManualAlarmConfirmation.AlarmConfirmationType.AREA_ALARM;
        
        logger.info("\n组合报警类型 0x{}: {}", 
            Long.toHexString(combinedAlarmType).toUpperCase(),
            T8203ManualAlarmConfirmation.AlarmConfirmationType.getAlarmTypeDescription(combinedAlarmType));
        
        // 检查特定报警类型是否存在
        boolean hasEmergency = T8203ManualAlarmConfirmation.AlarmConfirmationType.hasAlarmType(
            combinedAlarmType, T8203ManualAlarmConfirmation.AlarmConfirmationType.EMERGENCY_ALARM);
        boolean hasRoute = T8203ManualAlarmConfirmation.AlarmConfirmationType.hasAlarmType(
            combinedAlarmType, T8203ManualAlarmConfirmation.AlarmConfirmationType.ROUTE_ALARM);
        
        logger.info("包含紧急报警: {}", hasEmergency);
        logger.info("包含路线报警: {}", hasRoute);
    }
    
    /**
     * 演示实际应用场景
     */
    private static void demonstrateRealWorldScenarios() {
        logger.info("\n--- 5. 实际应用场景 ---");
        
        // 场景1: 监控中心确认紧急报警
        logger.info("\n场景1: 监控中心确认紧急报警");
        T8203ManualAlarmConfirmation emergencyConfirm = T8203ManualAlarmConfirmation.createConfirmSpecificAlarm(
            8888, T8203ManualAlarmConfirmation.AlarmConfirmationType.EMERGENCY_ALARM);
        logger.info("确认消息: {}", emergencyConfirm.getConfirmationDescription());
        
        // 场景2: 批量确认所有区域报警
        logger.info("\n场景2: 批量确认所有区域报警");
        T8203ManualAlarmConfirmation areaConfirmAll = T8203ManualAlarmConfirmation.createConfirmAllAlarms(
            T8203ManualAlarmConfirmation.AlarmConfirmationType.AREA_ALARM);
        logger.info("确认消息: {}", areaConfirmAll.getConfirmationDescription());
        
        // 场景3: 确认多种类型的报警
        logger.info("\n场景3: 确认多种类型的报警");
        long multipleAlarms = T8203ManualAlarmConfirmation.AlarmConfirmationType.ILLEGAL_IGNITION_ALARM |
                             T8203ManualAlarmConfirmation.AlarmConfirmationType.ILLEGAL_DISPLACEMENT_ALARM;
        T8203ManualAlarmConfirmation multiConfirm = T8203ManualAlarmConfirmation.createConfirmSpecificAlarm(
            7777, multipleAlarms);
        logger.info("确认消息: {}", multiConfirm.getConfirmationDescription());
        
        // 场景4: 时间转换示例
        logger.info("\n场景4: 消息处理时间记录");
        long currentTime = System.currentTimeMillis();
        logger.info("消息处理时间: {} ms", currentTime);
        logger.info("消息体大小: {} 字节", emergencyConfirm.encodeBody().length());
    }
    
    /**
     * 演示异常处理
     */
    private static void demonstrateExceptionHandling() {
        logger.info("\n--- 6. 异常处理 ---");
        
        // 测试流水号超出范围
        try {
            T8203ManualAlarmConfirmation.createConfirmSpecificAlarm(0x10000, 0x00000001L);
        } catch (IllegalArgumentException e) {
            logger.warn("流水号超出范围异常: {}", e.getMessage());
        }
        
        // 测试负数报警类型
        try {
            T8203ManualAlarmConfirmation.createConfirmAllAlarms(-1L);
        } catch (IllegalArgumentException e) {
            logger.warn("负数报警类型异常: {}", e.getMessage());
        }
        
        // 测试消息体长度不足
        try {
            T8203ManualAlarmConfirmation message = new T8203ManualAlarmConfirmation();
            Buffer shortBuffer = Buffer.buffer();
            shortBuffer.appendByte((byte) 0x01); // 只有1字节
            message.decodeBody(shortBuffer);
        } catch (IllegalArgumentException e) {
            logger.warn("消息体长度不足异常: {}", e.getMessage());
        }
        
        // 测试空消息体
        try {
            T8203ManualAlarmConfirmation message = new T8203ManualAlarmConfirmation();
            message.decodeBody(null);
        } catch (IllegalArgumentException e) {
            logger.warn("空消息体异常: {}", e.getMessage());
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