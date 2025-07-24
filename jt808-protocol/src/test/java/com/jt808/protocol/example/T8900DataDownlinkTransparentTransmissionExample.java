package com.jt808.protocol.example;

import com.jt808.protocol.constants.MessageTypes;
import com.jt808.protocol.factory.JT808MessageFactory;
import com.jt808.protocol.message.JT808Message;
import com.jt808.protocol.message.T8900DataDownlinkTransparentTransmission;
import io.vertx.core.buffer.Buffer;

import java.nio.charset.StandardCharsets;

/**
 * T8900数据下行透传示例程序
 * 演示如何创建、编码、解码和使用T8900消息
 */
public class T8900DataDownlinkTransparentTransmissionExample {
    
    public static void main(String[] args) {
        System.out.println("=== T8900数据下行透传示例 ===");
        
        // 1. 基本用法示例
        basicUsageExample();
        
        // 2. 不同透传消息类型示例
        differentMessageTypesExample();
        
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
        
        String content = "Hello JT808 Transparent Transmission";
        T8900DataDownlinkTransparentTransmission message = new T8900DataDownlinkTransparentTransmission(
            T8900DataDownlinkTransparentTransmission.TransparentMessageType.CUSTOM_TRANSPARENT_DATA, content);
        
        System.out.println("消息ID: 0x" + Integer.toHexString(message.getMessageId()).toUpperCase());
        System.out.println("透传消息类型: " + message.getMessageType().getDescription());
        System.out.println("透传消息内容: " + message.getMessageContentAsString());
        System.out.println("内容长度: " + message.getMessageContentLength() + " 字节");
        System.out.println("完整消息: " + message);
    }
    
    /**
     * 不同透传消息类型示例
     */
    private static void differentMessageTypesExample() {
        System.out.println("\n2. 不同透传消息类型示例:");
        
        // GNSS模块详细定位数据
        byte[] gnssData = {0x01, 0x02, 0x03, 0x04, 0x05};
        T8900DataDownlinkTransparentTransmission gnssMessage = new T8900DataDownlinkTransparentTransmission(
            T8900DataDownlinkTransparentTransmission.TransparentMessageType.GNSS_MODULE_DETAILED_POSITIONING_DATA, gnssData);
        
        // 道路运输证IC卡信息
        String cardInfo = "道路运输证:京A12345";
        T8900DataDownlinkTransparentTransmission cardMessage = new T8900DataDownlinkTransparentTransmission(
            T8900DataDownlinkTransparentTransmission.TransparentMessageType.ROAD_TRANSPORT_CERTIFICATE_IC_CARD_INFO, cardInfo);
        
        // 驾驶员身份IC卡信息
        String driverInfo = "驾驶员:张三,证件号:110101199001011234";
        T8900DataDownlinkTransparentTransmission driverMessage = new T8900DataDownlinkTransparentTransmission(
            T8900DataDownlinkTransparentTransmission.TransparentMessageType.DRIVER_IDENTITY_IC_CARD_INFO, driverInfo);
        
        // GNSS模块详细定位数据(扩展)
        byte[] gnssExtData = {(byte) 0xF0, (byte) 0xF1, (byte) 0xF2, (byte) 0xF3};
        T8900DataDownlinkTransparentTransmission gnssExtMessage = new T8900DataDownlinkTransparentTransmission(
            T8900DataDownlinkTransparentTransmission.TransparentMessageType.GNSS_MODULE_DETAILED_POSITIONING_DATA_WITH_EXTENSION, gnssExtData);
        
        // 自定义透传数据
        String customData = "自定义透传数据内容";
        T8900DataDownlinkTransparentTransmission customMessage = new T8900DataDownlinkTransparentTransmission(
            T8900DataDownlinkTransparentTransmission.TransparentMessageType.CUSTOM_TRANSPARENT_DATA, customData);
        
        System.out.println("GNSS定位数据: " + gnssMessage);
        System.out.println("道路运输证信息: " + cardMessage);
        System.out.println("驾驶员身份信息: " + driverMessage);
        System.out.println("GNSS扩展数据: " + gnssExtMessage);
        System.out.println("自定义数据: " + customMessage);
        
        // 显示所有透传消息类型的详细信息
        System.out.println("\n透传消息类型枚举信息:");
        for (T8900DataDownlinkTransparentTransmission.TransparentMessageType type : 
             T8900DataDownlinkTransparentTransmission.TransparentMessageType.values()) {
            System.out.println("  " + type.name() + " (值: 0x" + Integer.toHexString(type.getValue()).toUpperCase() + 
                              ", 描述: " + type.getDescription() + ")");
        }
    }
    
    /**
     * 编码解码过程演示
     */
    private static void encodeDecodeExample() {
        System.out.println("\n3. 编码解码过程演示:");
        
        String content = "透传数据编码解码测试";
        T8900DataDownlinkTransparentTransmission original = new T8900DataDownlinkTransparentTransmission(
            T8900DataDownlinkTransparentTransmission.TransparentMessageType.DRIVER_IDENTITY_IC_CARD_INFO, content);
        
        System.out.println("原始消息: " + original);
        
        // 编码
        Buffer encoded = original.encode();
        System.out.println("编码后长度: " + encoded.length() + " 字节");
        System.out.println("编码后数据: " + bytesToHex(encoded.getBytes()));
        
        // 详细分析编码内容
        System.out.println("\n编码内容分析:");
        System.out.println("  透传消息类型 (1字节): 0x" + Integer.toHexString(encoded.getByte(0) & 0xFF).toUpperCase() + 
                          " (" + T8900DataDownlinkTransparentTransmission.TransparentMessageType.fromValue(encoded.getByte(0) & 0xFF).getDescription() + ")");
        
        if (encoded.length() > 1) {
            byte[] contentBytes = new byte[encoded.length() - 1];
            encoded.getBytes(1, encoded.length(), contentBytes);
            System.out.println("  透传消息内容 (" + contentBytes.length + "字节): " + new String(contentBytes, StandardCharsets.UTF_8));
        }
        
        // 解码
        T8900DataDownlinkTransparentTransmission decoded = T8900DataDownlinkTransparentTransmission.decode(encoded);
        System.out.println("\n解码后消息: " + decoded);
        
        // 验证编码解码一致性
        boolean isEqual = original.equals(decoded);
        System.out.println("编码解码一致性验证: " + (isEqual ? "通过" : "失败"));
        
        if (isEqual) {
            System.out.println("  消息类型匹配: " + (original.getMessageType() == decoded.getMessageType()));
            System.out.println("  消息内容匹配: " + original.getMessageContentAsString().equals(decoded.getMessageContentAsString()));
        }
    }
    
    /**
     * 工厂模式使用示例
     */
    private static void factoryExample() {
        System.out.println("\n4. 工厂模式使用示例:");
        
        JT808MessageFactory factory = JT808MessageFactory.getInstance();
        
        // 通过工厂创建消息
        JT808Message message = factory.createMessage(MessageTypes.Platform.DATA_DOWNLINK_TRANSPARENT_TRANSMISSION);
        
        if (message instanceof T8900DataDownlinkTransparentTransmission) {
            T8900DataDownlinkTransparentTransmission transparentMessage = (T8900DataDownlinkTransparentTransmission) message;
            
            // 设置消息内容
            transparentMessage.setMessageType(T8900DataDownlinkTransparentTransmission.TransparentMessageType.GNSS_MODULE_DETAILED_POSITIONING_DATA);
            transparentMessage.setMessageContent("工厂创建的透传消息");
            
            System.out.println("通过工厂创建的消息: " + transparentMessage);
            System.out.println("消息ID: 0x" + Integer.toHexString(transparentMessage.getMessageId()).toUpperCase());
            System.out.println("消息类型验证: " + (transparentMessage.getMessageId() == 0x8900 ? "正确" : "错误"));
        }
    }
    
    /**
     * 实际应用场景模拟
     */
    private static void realWorldScenarioExample() {
        System.out.println("\n5. 实际应用场景模拟:");
        
        // 场景1: GNSS定位数据透传
        System.out.println("\n场景1: GNSS定位数据透传");
        byte[] gnssRawData = {
            (byte) 0x24, (byte) 0x47, (byte) 0x50, (byte) 0x47, (byte) 0x47, (byte) 0x41, // $GPGGA
            (byte) 0x2C, (byte) 0x31, (byte) 0x32, (byte) 0x33, (byte) 0x34, (byte) 0x35, // ,12345
            (byte) 0x36, (byte) 0x2E, (byte) 0x30, (byte) 0x30, (byte) 0x2C, (byte) 0x4E  // 6.00,N
        };
        T8900DataDownlinkTransparentTransmission gnssMessage = new T8900DataDownlinkTransparentTransmission(
            T8900DataDownlinkTransparentTransmission.TransparentMessageType.GNSS_MODULE_DETAILED_POSITIONING_DATA, gnssRawData);
        System.out.println("GNSS数据透传: " + gnssMessage);
        System.out.println("原始GNSS数据: " + bytesToHex(gnssRawData));
        
        // 场景2: IC卡信息透传
        System.out.println("\n场景2: IC卡信息透传");
        String cardData = "{\"cardType\":\"道路运输证\",\"cardNo\":\"京A12345\",\"validDate\":\"2025-12-31\",\"company\":\"北京运输公司\"}";
        T8900DataDownlinkTransparentTransmission cardMessage = new T8900DataDownlinkTransparentTransmission(
            T8900DataDownlinkTransparentTransmission.TransparentMessageType.ROAD_TRANSPORT_CERTIFICATE_IC_CARD_INFO, cardData);
        System.out.println("IC卡信息透传: " + cardMessage);
        System.out.println("卡片信息: " + cardMessage.getMessageContentAsString());
        
        // 场景3: 驾驶员身份信息透传
        System.out.println("\n场景3: 驾驶员身份信息透传");
        String driverData = "{\"name\":\"张三\",\"idCard\":\"110101199001011234\",\"licenseNo\":\"京A123456789\",\"licenseType\":\"A1\"}";
        T8900DataDownlinkTransparentTransmission driverMessage = new T8900DataDownlinkTransparentTransmission(
            T8900DataDownlinkTransparentTransmission.TransparentMessageType.DRIVER_IDENTITY_IC_CARD_INFO, driverData);
        System.out.println("驾驶员信息透传: " + driverMessage);
        System.out.println("驾驶员信息: " + driverMessage.getMessageContentAsString());
        
        // 场景4: 自定义协议透传
        System.out.println("\n场景4: 自定义协议透传");
        byte[] customProtocolData = {
            (byte) 0xAA, (byte) 0xBB, // 协议头
            (byte) 0x01, (byte) 0x02, // 命令类型
            (byte) 0x00, (byte) 0x08, // 数据长度
            (byte) 0x12, (byte) 0x34, (byte) 0x56, (byte) 0x78, // 数据内容
            (byte) 0x9A, (byte) 0xBC, (byte) 0xDE, (byte) 0xF0,
            (byte) 0xCC, (byte) 0xDD  // 校验码
        };
        T8900DataDownlinkTransparentTransmission customMessage = new T8900DataDownlinkTransparentTransmission(
            T8900DataDownlinkTransparentTransmission.TransparentMessageType.CUSTOM_TRANSPARENT_DATA, customProtocolData);
        System.out.println("自定义协议透传: " + customMessage);
        System.out.println("自定义协议数据: " + bytesToHex(customProtocolData));
        
        // 场景5: 空内容透传
        System.out.println("\n场景5: 空内容透传（心跳或状态查询）");
        T8900DataDownlinkTransparentTransmission emptyMessage = new T8900DataDownlinkTransparentTransmission(
            T8900DataDownlinkTransparentTransmission.TransparentMessageType.GNSS_MODULE_DETAILED_POSITIONING_DATA, new byte[0]);
        System.out.println("空内容透传: " + emptyMessage);
        System.out.println("用途: 可用于心跳检测或状态查询");
        
        // 场景6: 中文内容透传
        System.out.println("\n场景6: 中文内容透传");
        String chineseContent = "中文透传数据测试：车辆位置信息，驾驶员：李四，当前位置：北京市朝阳区";
        T8900DataDownlinkTransparentTransmission chineseMessage = new T8900DataDownlinkTransparentTransmission(
            T8900DataDownlinkTransparentTransmission.TransparentMessageType.CUSTOM_TRANSPARENT_DATA, chineseContent);
        System.out.println("中文透传: " + chineseMessage);
        System.out.println("中文内容: " + chineseMessage.getMessageContentAsString());
        System.out.println("UTF-8编码长度: " + chineseContent.getBytes(StandardCharsets.UTF_8).length + " 字节");
        
        // 编码所有消息并显示大小
        System.out.println("\n各消息编码后大小:");
        System.out.println("GNSS数据透传: " + gnssMessage.encode().length() + " 字节");
        System.out.println("IC卡信息透传: " + cardMessage.encode().length() + " 字节");
        System.out.println("驾驶员信息透传: " + driverMessage.encode().length() + " 字节");
        System.out.println("自定义协议透传: " + customMessage.encode().length() + " 字节");
        System.out.println("空内容透传: " + emptyMessage.encode().length() + " 字节");
        System.out.println("中文内容透传: " + chineseMessage.encode().length() + " 字节");
        
        // 场景7: 大数据量透传
        System.out.println("\n场景7: 大数据量透传测试");
        byte[] largeData = new byte[1024]; // 1KB数据
        for (int i = 0; i < largeData.length; i++) {
            largeData[i] = (byte) (i % 256);
        }
        T8900DataDownlinkTransparentTransmission largeMessage = new T8900DataDownlinkTransparentTransmission(
            T8900DataDownlinkTransparentTransmission.TransparentMessageType.GNSS_MODULE_DETAILED_POSITIONING_DATA_WITH_EXTENSION, largeData);
        System.out.println("大数据透传: " + largeMessage);
        System.out.println("数据大小: " + largeMessage.getMessageContentLength() + " 字节");
        System.out.println("编码后总大小: " + largeMessage.encode().length() + " 字节");
        
        // 场景8: 未知消息类型处理
        System.out.println("\n场景8: 未知消息类型处理");
        Buffer unknownTypeBuffer = Buffer.buffer();
        unknownTypeBuffer.appendByte((byte) 0x99); // 未知类型
        unknownTypeBuffer.appendBytes("未知类型数据".getBytes());
        
        T8900DataDownlinkTransparentTransmission unknownMessage = T8900DataDownlinkTransparentTransmission.decode(unknownTypeBuffer);
        System.out.println("未知类型消息: " + unknownMessage);
        System.out.println("处理方式: 自动映射为自定义透传数据类型");
        
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