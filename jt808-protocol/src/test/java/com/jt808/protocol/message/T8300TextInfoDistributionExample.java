package com.jt808.protocol.message;

import io.vertx.core.buffer.Buffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * T8300文本信息下发消息功能演示
 * 
 * 演示内容：
 * 1. 基本消息创建和使用
 * 2. 工厂方法的使用
 * 3. 消息编解码
 * 4. 标志位处理
 * 5. 实际应用场景
 * 6. 异常处理
 */
public class T8300TextInfoDistributionExample {
    
    private static final Logger logger = LoggerFactory.getLogger(T8300TextInfoDistributionExample.class);
    
    public static void main(String[] args) {
        T8300TextInfoDistributionExample example = new T8300TextInfoDistributionExample();
        
        try {
            example.demonstrateBasicUsage();
            example.demonstrateFactoryMethods();
            example.demonstrateEncodeDecode();
            example.demonstrateFlagHandling();
            example.demonstrateRealWorldScenarios();
            example.demonstrateExceptionHandling();
            
            logger.info("T8300文本信息下发消息演示完成");
        } catch (Exception e) {
            logger.error("演示过程中发生错误", e);
        }
    }
    
    /**
     * 演示基本使用方法
     */
    private void demonstrateBasicUsage() {
        logger.info("=== 基本使用演示 ===");
        
        // 1. 使用默认构造函数
        T8300TextInfoDistribution message1 = new T8300TextInfoDistribution();
        message1.setTextFlag(T8300TextInfoDistribution.TextFlag.EMERGENCY);
        message1.setTextInfo("紧急通知：前方道路施工，请减速慢行");
        
        logger.info("消息1: " + message1);
        logger.info("是否紧急: " + message1.isEmergency());
        logger.info("标志描述: " + message1.getTextFlagDescription());
        
        // 2. 使用参数构造函数
        byte flag = (byte) (T8300TextInfoDistribution.TextFlag.TERMINAL_DISPLAY | 
                           T8300TextInfoDistribution.TextFlag.TERMINAL_TTS);
        T8300TextInfoDistribution message2 = new T8300TextInfoDistribution(flag, "请系好安全带");
        
        logger.info("消息2: " + message2);
        logger.info("终端显示: " + message2.isTerminalDisplay());
        logger.info("终端TTS: " + message2.isTerminalTTS());
    }
    
    /**
     * 演示工厂方法的使用
     */
    private void demonstrateFactoryMethods() {
        logger.info("=== 工厂方法演示 ===");
        
        // 1. 创建紧急文本消息
        T8300TextInfoDistribution emergencyMsg = T8300TextInfoDistribution.createEmergencyText(
            "紧急停车！发现车辆故障", true, true);
        logger.info("紧急消息: " + emergencyMsg);
        logger.info("标志描述: " + emergencyMsg.getTextFlagDescription());
        
        // 2. 创建普通文本消息
        T8300TextInfoDistribution normalMsg = T8300TextInfoDistribution.createNormalText(
            "欢迎使用车载导航系统", true, false, true);
        logger.info("普通消息: " + normalMsg);
        logger.info("标志描述: " + normalMsg.getTextFlagDescription());
        
        // 3. 创建CAN故障码信息
        T8300TextInfoDistribution canMsg = T8300TextInfoDistribution.createCANFaultInfo(
            "发动机故障码: P0171 - 系统过稀(燃油修正)", true);
        logger.info("CAN故障消息: " + canMsg);
        logger.info("标志描述: " + canMsg.getTextFlagDescription());
    }
    
    /**
     * 演示消息编解码
     */
    private void demonstrateEncodeDecode() {
        logger.info("=== 编解码演示 ===");
        
        // 创建原始消息
        byte flag = (byte) (T8300TextInfoDistribution.TextFlag.EMERGENCY | 
                           T8300TextInfoDistribution.TextFlag.TERMINAL_DISPLAY | 
                           T8300TextInfoDistribution.TextFlag.ADVERTISEMENT_DISPLAY);
        T8300TextInfoDistribution original = new T8300TextInfoDistribution(flag, "测试编解码功能");
        
        logger.info("原始消息: " + original);
        
        // 编码消息体
        Buffer encoded = original.encodeBody();
        logger.info("编码后长度: " + encoded.length() + " 字节");
        logger.info("编码数据: " + bytesToHex(encoded.getBytes()));
        
        // 解码消息体
        T8300TextInfoDistribution decoded = new T8300TextInfoDistribution();
        decoded.decodeBody(encoded);
        
        logger.info("解码后消息: " + decoded);
        logger.info("编解码一致性: " + original.equals(decoded));
    }
    
    /**
     * 演示标志位处理
     */
    private void demonstrateFlagHandling() {
        logger.info("=== 标志位处理演示 ===");
        
        // 演示各种标志位组合
        byte[] flagCombinations = {
            T8300TextInfoDistribution.TextFlag.EMERGENCY,
            T8300TextInfoDistribution.TextFlag.TERMINAL_DISPLAY,
            T8300TextInfoDistribution.TextFlag.TERMINAL_TTS,
            T8300TextInfoDistribution.TextFlag.ADVERTISEMENT_DISPLAY,
            T8300TextInfoDistribution.TextFlag.CAN_FAULT_INFO,
            (byte) (T8300TextInfoDistribution.TextFlag.EMERGENCY | T8300TextInfoDistribution.TextFlag.TERMINAL_DISPLAY),
            (byte) (T8300TextInfoDistribution.TextFlag.TERMINAL_TTS | T8300TextInfoDistribution.TextFlag.ADVERTISEMENT_DISPLAY),
            (byte) 0x1F // 所有标志位
        };
        
        for (int i = 0; i < flagCombinations.length; i++) {
            T8300TextInfoDistribution msg = new T8300TextInfoDistribution();
            msg.setTextFlag(flagCombinations[i]);
            msg.setTextInfo("标志位组合测试 " + (i + 1));
            
            logger.info("组合 " + (i + 1) + ": 0x" + String.format("%02X", flagCombinations[i] & 0xFF));
            logger.info("  紧急: " + msg.isEmergency());
            logger.info("  终端显示: " + msg.isTerminalDisplay());
            logger.info("  终端TTS: " + msg.isTerminalTTS());
            logger.info("  广告屏显示: " + msg.isAdvertisementDisplay());
            logger.info("  CAN故障码: " + msg.isCANFaultInfo());
            logger.info("  描述: " + msg.getTextFlagDescription());
        }
    }
    
    /**
     * 演示实际应用场景
     */
    private void demonstrateRealWorldScenarios() {
        logger.info("=== 实际应用场景演示 ===");
        
        // 场景1: 交通事故紧急通知
        T8300TextInfoDistribution accidentAlert = T8300TextInfoDistribution.createEmergencyText(
            "前方3公里处发生交通事故，请选择其他路线", true, true);
        logger.info("交通事故通知: " + accidentAlert.getTextInfo());
        logger.info("处理方式: " + accidentAlert.getTextFlagDescription());
        
        // 场景2: 车辆保养提醒
        T8300TextInfoDistribution maintenanceReminder = T8300TextInfoDistribution.createNormalText(
            "您的车辆已行驶15000公里，建议进行保养", true, false, false);
        logger.info("保养提醒: " + maintenanceReminder.getTextInfo());
        logger.info("处理方式: " + maintenanceReminder.getTextFlagDescription());
        
        // 场景3: 发动机故障诊断
        T8300TextInfoDistribution engineFault = T8300TextInfoDistribution.createCANFaultInfo(
            "发动机故障码: P0300 - 随机/多缸失火检测", true);
        logger.info("发动机故障: " + engineFault.getTextInfo());
        logger.info("处理方式: " + engineFault.getTextFlagDescription());
        
        // 场景4: 广告信息推送
        T8300TextInfoDistribution advertisement = T8300TextInfoDistribution.createNormalText(
            "附近加油站优惠活动：95号汽油每升优惠0.5元", false, false, true);
        logger.info("广告推送: " + advertisement.getTextInfo());
        logger.info("处理方式: " + advertisement.getTextFlagDescription());
        
        // 场景5: 导航语音提示
        T8300TextInfoDistribution navigationVoice = T8300TextInfoDistribution.createNormalText(
            "前方500米右转进入主干道", false, true, false);
        logger.info("导航语音: " + navigationVoice.getTextInfo());
        logger.info("处理方式: " + navigationVoice.getTextFlagDescription());
    }
    
    /**
     * 演示异常处理
     */
    private void demonstrateExceptionHandling() {
        logger.info("=== 异常处理演示 ===");
        
        // 1. 文本长度超限异常
        try {
            StringBuilder longText = new StringBuilder();
            for (int i = 0; i < 1025; i++) {
                longText.append("a");
            }
            
            T8300TextInfoDistribution msg = new T8300TextInfoDistribution();
            msg.setTextInfo(longText.toString());
            logger.error("应该抛出异常但没有抛出");
        } catch (IllegalArgumentException e) {
            logger.info("正确捕获文本长度超限异常: " + e.getMessage());
        }
        
        // 2. 中文文本长度超限异常
        try {
            StringBuilder longChineseText = new StringBuilder();
            for (int i = 0; i < 513; i++) { // 513 * 2 = 1026 > 1024
                longChineseText.append("中");
            }
            
            T8300TextInfoDistribution msg = new T8300TextInfoDistribution();
            msg.setTextInfo(longChineseText.toString());
            logger.error("应该抛出异常但没有抛出");
        } catch (IllegalArgumentException e) {
            logger.info("正确捕获中文文本长度超限异常: " + e.getMessage());
        }
        
        // 3. 解码空缓冲区异常
        try {
            T8300TextInfoDistribution msg = new T8300TextInfoDistribution();
            msg.decodeBody(null);
            logger.error("应该抛出异常但没有抛出");
        } catch (IllegalArgumentException e) {
            logger.info("正确捕获空缓冲区异常: " + e.getMessage());
        }
        
        // 4. 解码长度不足异常
        try {
            T8300TextInfoDistribution msg = new T8300TextInfoDistribution();
            Buffer emptyBuffer = Buffer.buffer();
            msg.decodeBody(emptyBuffer);
            logger.error("应该抛出异常但没有抛出");
        } catch (IllegalArgumentException e) {
            logger.info("正确捕获长度不足异常: " + e.getMessage());
        }
        
        // 5. 正常的边界情况
        try {
            // 最大长度文本（1024字节）
            StringBuilder maxText = new StringBuilder();
            for (int i = 0; i < 1024; i++) {
                maxText.append("a");
            }
            
            T8300TextInfoDistribution msg = new T8300TextInfoDistribution((byte) 0x01, maxText.toString());
            Buffer encoded = msg.encodeBody();
            
            T8300TextInfoDistribution decoded = new T8300TextInfoDistribution();
            decoded.decodeBody(encoded);
            
            logger.info("最大长度文本处理成功，长度: " + decoded.getTextInfo().length());
        } catch (Exception e) {
            logger.error("最大长度文本处理失败", e);
        }
    }
    
    /**
     * 字节数组转十六进制字符串
     */
    private String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02X ", b));
        }
        return result.toString().trim();
    }
}