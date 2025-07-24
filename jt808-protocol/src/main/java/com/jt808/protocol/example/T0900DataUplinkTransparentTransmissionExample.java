package com.jt808.protocol.example;

import com.jt808.protocol.constants.MessageTypes;
import com.jt808.protocol.factory.JT808MessageFactory;
import com.jt808.protocol.message.JT808Message;
import com.jt808.protocol.message.T0900DataUplinkTransparentTransmission;
import io.vertx.core.buffer.Buffer;

import java.nio.charset.StandardCharsets;

/**
 * T0900数据上行透传示例程序
 * 演示如何创建、编码、解码和使用0x0900数据上行透传消息
 */
public class T0900DataUplinkTransparentTransmissionExample {
    
    public static void main(String[] args) {
        System.out.println("=== T0900数据上行透传示例程序 ===");
        
        // 基本用法示例
        basicUsageExample();
        
        // 不同透传消息类型示例
        differentMessageTypesExample();
        
        // 编码解码过程分析
        encodeDecodeAnalysisExample();
        
        // 工厂模式使用示例
        factoryUsageExample();
        
        // 实际应用场景模拟
        realWorldScenarioExample();
    }
    
    /**
     * 基本用法示例
     */
    private static void basicUsageExample() {
        System.out.println("\n--- 基本用法示例 ---");
        
        // 创建GNSS模块详细定位数据透传消息
        String gnssData = "经度:116.397128,纬度:39.916527,高度:50.5,速度:60.0";
        byte[] gnssBytes = gnssData.getBytes(StandardCharsets.UTF_8);
        
        T0900DataUplinkTransparentTransmission gnssMessage = new T0900DataUplinkTransparentTransmission(
            T0900DataUplinkTransparentTransmission.TransparentMessageType.GNSS_MODULE_DETAILED_POSITIONING_DATA,
            gnssBytes
        );
        
        System.out.println("GNSS透传消息:");
        System.out.println("  消息ID: 0x" + Integer.toHexString(gnssMessage.getMessageId()).toUpperCase());
        System.out.println("  透传类型: " + gnssMessage.getMessageType());
        System.out.println("  透传内容: " + new String(gnssMessage.getMessageContent(), StandardCharsets.UTF_8));
        System.out.println("  编码后长度: " + gnssMessage.encode().length() + " 字节");
        
        // 创建道路运输证IC卡信息透传消息
        byte[] icCardData = new byte[64]; // 上传消息为64字节
        for (int i = 0; i < icCardData.length; i++) {
            icCardData[i] = (byte) (0x10 + (i % 16));
        }
        
        T0900DataUplinkTransparentTransmission icCardMessage = new T0900DataUplinkTransparentTransmission(
            T0900DataUplinkTransparentTransmission.TransparentMessageType.ROAD_TRANSPORT_CERTIFICATE_IC_CARD_INFO,
            icCardData
        );
        
        System.out.println("\nIC卡透传消息:");
        System.out.println("  透传类型: " + icCardMessage.getMessageType());
        System.out.println("  数据长度: " + icCardMessage.getMessageContent().length + " 字节");
        System.out.println("  数据内容: " + bytesToHex(icCardMessage.getMessageContent()));
    }
    
    /**
     * 不同透传消息类型示例
     */
    private static void differentMessageTypesExample() {
        System.out.println("\n--- 不同透传消息类型示例 ---");
        
        // 1. 串口1透传
        String serialPort1Data = "串口1接收到的数据: AT+CGMI\r\n";
        T0900DataUplinkTransparentTransmission serialPort1Message = new T0900DataUplinkTransparentTransmission(
            T0900DataUplinkTransparentTransmission.TransparentMessageType.SERIAL_PORT_1_TRANSPARENT,
            serialPort1Data.getBytes(StandardCharsets.UTF_8)
        );
        
        System.out.println("串口1透传消息:");
        System.out.println("  类型值: 0x" + Integer.toHexString(serialPort1Message.getMessageType().getValue()).toUpperCase());
        System.out.println("  内容: " + new String(serialPort1Message.getMessageContent(), StandardCharsets.UTF_8));
        
        // 2. 串口2透传
        byte[] serialPort2Data = {0x7E, 0x01, 0x02, 0x03, 0x04, 0x7E}; // 模拟协议数据
        T0900DataUplinkTransparentTransmission serialPort2Message = new T0900DataUplinkTransparentTransmission(
            T0900DataUplinkTransparentTransmission.TransparentMessageType.SERIAL_PORT_2_TRANSPARENT,
            serialPort2Data
        );
        
        System.out.println("\n串口2透传消息:");
        System.out.println("  类型值: 0x" + Integer.toHexString(serialPort2Message.getMessageType().getValue()).toUpperCase());
        System.out.println("  内容: " + bytesToHex(serialPort2Message.getMessageContent()));
        
        // 3. 用户自定义透传（0xF0-0xFF）
        for (int i = 0xF0; i <= 0xFF; i++) {
            try {
                T0900DataUplinkTransparentTransmission.TransparentMessageType customType = 
                    T0900DataUplinkTransparentTransmission.TransparentMessageType.fromValue(i);
                
                String customData = "自定义类型" + Integer.toHexString(i).toUpperCase() + "的数据";
                T0900DataUplinkTransparentTransmission customMessage = new T0900DataUplinkTransparentTransmission(
                    customType, customData.getBytes(StandardCharsets.UTF_8)
                );
                
                if (i == 0xF0 || i == 0xFF) { // 只显示第一个和最后一个
                    System.out.println("\n用户自定义透传消息 (0x" + Integer.toHexString(i).toUpperCase() + "):");
                    System.out.println("  内容: " + new String(customMessage.getMessageContent(), StandardCharsets.UTF_8));
                }
            } catch (IllegalArgumentException e) {
                // 某些值可能没有定义，跳过
            }
        }
    }
    
    /**
     * 编码解码过程分析
     */
    private static void encodeDecodeAnalysisExample() {
        System.out.println("\n--- 编码解码过程分析 ---");
        
        // 创建一个包含中文的透传消息
        String chineseContent = "中文透传数据：车辆位置信息";
        byte[] chineseBytes = chineseContent.getBytes(StandardCharsets.UTF_8);
        
        T0900DataUplinkTransparentTransmission originalMessage = new T0900DataUplinkTransparentTransmission(
            T0900DataUplinkTransparentTransmission.TransparentMessageType.USER_DEFINED_TRANSPARENT_F0,
            chineseBytes
        );
        
        System.out.println("原始消息:");
        System.out.println("  透传类型: " + originalMessage.getMessageType());
        System.out.println("  内容长度: " + originalMessage.getMessageContent().length + " 字节");
        System.out.println("  内容: " + new String(originalMessage.getMessageContent(), StandardCharsets.UTF_8));
        
        // 编码过程
        Buffer encodedBuffer = originalMessage.encode();
        System.out.println("\n编码过程:");
        System.out.println("  编码后总长度: " + encodedBuffer.length() + " 字节");
        System.out.println("  编码后数据: " + bytesToHex(encodedBuffer.getBytes()));
        
        // 分析编码结构
        System.out.println("\n编码结构分析:");
        System.out.println("  字节0 (透传类型): 0x" + Integer.toHexString(encodedBuffer.getByte(0) & 0xFF).toUpperCase());
        System.out.println("  字节1-" + (encodedBuffer.length() - 1) + " (透传内容): " + 
                          bytesToHex(encodedBuffer.getBytes(1, encodedBuffer.length())));
        
        // 解码过程
        T0900DataUplinkTransparentTransmission decodedMessage = T0900DataUplinkTransparentTransmission.decode(encodedBuffer);
        
        System.out.println("\n解码结果:");
        System.out.println("  透传类型: " + decodedMessage.getMessageType());
        System.out.println("  内容长度: " + decodedMessage.getMessageContent().length + " 字节");
        System.out.println("  内容: " + new String(decodedMessage.getMessageContent(), StandardCharsets.UTF_8));
        
        // 验证编码解码的一致性
        boolean isConsistent = originalMessage.equals(decodedMessage);
        System.out.println("\n编码解码一致性: " + (isConsistent ? "通过" : "失败"));
    }
    
    /**
     * 工厂模式使用示例
     */
    private static void factoryUsageExample() {
        System.out.println("\n--- 工厂模式使用示例 ---");
        
        // 使用工厂创建消息
        JT808MessageFactory factory = JT808MessageFactory.getInstance();
        JT808Message message = factory.createMessage(MessageTypes.Terminal.DATA_UPLINK_TRANSPARENT_TRANSMISSION);
        
        if (message instanceof T0900DataUplinkTransparentTransmission) {
            T0900DataUplinkTransparentTransmission transparentMessage = (T0900DataUplinkTransparentTransmission) message;
            
            // 设置透传数据
            String factoryData = "通过工厂创建的透传消息";
            transparentMessage.setMessageType(
                T0900DataUplinkTransparentTransmission.TransparentMessageType.USER_DEFINED_TRANSPARENT_F1
            );
            transparentMessage.setMessageContent(factoryData.getBytes(StandardCharsets.UTF_8));
            
            System.out.println("工厂创建的消息:");
            System.out.println("  消息ID: 0x" + Integer.toHexString(transparentMessage.getMessageId()).toUpperCase());
            System.out.println("  透传类型: " + transparentMessage.getMessageType());
            System.out.println("  内容: " + new String(transparentMessage.getMessageContent(), StandardCharsets.UTF_8));
        }
    }
    
    /**
     * 实际应用场景模拟
     */
    private static void realWorldScenarioExample() {
        System.out.println("\n--- 实际应用场景模拟 ---");
        
        // 场景1: GNSS定位数据上传
        simulateGnssDataUpload();
        
        // 场景2: IC卡认证数据上传
        simulateIcCardAuthentication();
        
        // 场景3: 串口设备数据透传
        simulateSerialPortTransparent();
        
        // 场景4: 自定义协议数据上传
        simulateCustomProtocolUpload();
        
        // 场景5: 空内容透传
        simulateEmptyContentTransparent();
        
        // 场景6: 大数据量透传
        simulateLargeDataTransparent();
    }
    
    private static void simulateGnssDataUpload() {
        System.out.println("\n场景1: GNSS定位数据上传");
        
        // 模拟GNSS模块返回的详细定位数据
        StringBuilder gnssData = new StringBuilder();
        gnssData.append("$GPGGA,123519,4807.038,N,01131.000,E,1,08,0.9,545.4,M,46.9,M,,*47\r\n");
        gnssData.append("$GPRMC,123519,A,4807.038,N,01131.000,E,022.4,084.4,230394,003.1,W*6A\r\n");
        
        T0900DataUplinkTransparentTransmission gnssMessage = new T0900DataUplinkTransparentTransmission(
            T0900DataUplinkTransparentTransmission.TransparentMessageType.GNSS_MODULE_DETAILED_POSITIONING_DATA,
            gnssData.toString().getBytes(StandardCharsets.UTF_8)
        );
        
        System.out.println("  GNSS数据长度: " + gnssMessage.getMessageContent().length + " 字节");
        System.out.println("  编码后长度: " + gnssMessage.encode().length() + " 字节");
        System.out.println("  数据预览: " + gnssData.toString().substring(0, Math.min(50, gnssData.length())) + "...");
    }
    
    private static void simulateIcCardAuthentication() {
        System.out.println("\n场景2: IC卡认证数据上传");
        
        // 模拟64字节的IC卡信息
        byte[] icCardData = new byte[64];
        // 卡号 (8字节)
        System.arraycopy("12345678".getBytes(), 0, icCardData, 0, 8);
        // 驾驶员姓名 (20字节)
        byte[] driverName = "张三".getBytes(StandardCharsets.UTF_8);
        System.arraycopy(driverName, 0, icCardData, 8, Math.min(driverName.length, 20));
        // 证件号码 (20字节)
        System.arraycopy("110101199001011234".getBytes(), 0, icCardData, 28, 18);
        // 其他信息填充
        for (int i = 46; i < 64; i++) {
            icCardData[i] = (byte) (0x00);
        }
        
        T0900DataUplinkTransparentTransmission icCardMessage = new T0900DataUplinkTransparentTransmission(
            T0900DataUplinkTransparentTransmission.TransparentMessageType.ROAD_TRANSPORT_CERTIFICATE_IC_CARD_INFO,
            icCardData
        );
        
        System.out.println("  IC卡数据长度: " + icCardMessage.getMessageContent().length + " 字节");
        System.out.println("  卡号: " + new String(icCardData, 0, 8));
        System.out.println("  驾驶员: " + new String(icCardData, 8, 20, StandardCharsets.UTF_8).trim());
    }
    
    private static void simulateSerialPortTransparent() {
        System.out.println("\n场景3: 串口设备数据透传");
        
        // 串口1: AT指令响应
        String atResponse = "AT+CSQ\r\n+CSQ: 25,99\r\n\r\nOK\r\n";
        T0900DataUplinkTransparentTransmission serialPort1Message = new T0900DataUplinkTransparentTransmission(
            T0900DataUplinkTransparentTransmission.TransparentMessageType.SERIAL_PORT_1_TRANSPARENT,
            atResponse.getBytes(StandardCharsets.UTF_8)
        );
        
        System.out.println("  串口1数据: " + atResponse.replace("\r\n", "\\r\\n"));
        
        // 串口2: 二进制协议数据
        byte[] binaryProtocol = {0x7E, 0x02, 0x00, 0x10, 0x01, 0x23, 0x45, 0x67, (byte) 0x89, (byte) 0xAB, (byte) 0xCD, (byte) 0xEF, 0x12, 0x34, 0x56, 0x78, (byte) 0x9A, (byte) 0xBC, 0x7E};
        T0900DataUplinkTransparentTransmission serialPort2Message = new T0900DataUplinkTransparentTransmission(
            T0900DataUplinkTransparentTransmission.TransparentMessageType.SERIAL_PORT_2_TRANSPARENT,
            binaryProtocol
        );
        
        System.out.println("  串口2数据: " + bytesToHex(serialPort2Message.getMessageContent()));
    }
    
    private static void simulateCustomProtocolUpload() {
        System.out.println("\n场景4: 自定义协议数据上传");
        
        // 自定义协议: 温度传感器数据
        String temperatureData = "{\"sensors\":[{\"id\":1,\"temp\":25.6},{\"id\":2,\"temp\":26.1}]}";
        T0900DataUplinkTransparentTransmission customMessage = new T0900DataUplinkTransparentTransmission(
            T0900DataUplinkTransparentTransmission.TransparentMessageType.USER_DEFINED_TRANSPARENT_F2,
            temperatureData.getBytes(StandardCharsets.UTF_8)
        );
        
        System.out.println("  自定义协议类型: 0x" + 
                          Integer.toHexString(customMessage.getMessageType().getValue()).toUpperCase());
        System.out.println("  JSON数据: " + new String(customMessage.getMessageContent(), StandardCharsets.UTF_8));
    }
    
    private static void simulateEmptyContentTransparent() {
        System.out.println("\n场景5: 空内容透传");
        
        T0900DataUplinkTransparentTransmission emptyMessage = new T0900DataUplinkTransparentTransmission(
            T0900DataUplinkTransparentTransmission.TransparentMessageType.USER_DEFINED_TRANSPARENT_F3,
            new byte[0]
        );
        
        System.out.println("  空内容消息长度: " + emptyMessage.getMessageContent().length + " 字节");
        System.out.println("  编码后长度: " + emptyMessage.encode().length() + " 字节");
        
        // 验证空内容的编码解码
        Buffer encoded = emptyMessage.encode();
        T0900DataUplinkTransparentTransmission decoded = T0900DataUplinkTransparentTransmission.decode(encoded);
        System.out.println("  解码后内容长度: " + decoded.getMessageContent().length + " 字节");
    }
    
    private static void simulateLargeDataTransparent() {
        System.out.println("\n场景6: 大数据量透传");
        
        // 创建大量数据（模拟日志文件内容）
        StringBuilder largeData = new StringBuilder();
        for (int i = 0; i < 100; i++) {
            largeData.append(String.format("[%03d] %s - 车辆状态正常，位置更新\n", 
                                         i, java.time.LocalDateTime.now().toString()));
        }
        
        T0900DataUplinkTransparentTransmission largeMessage = new T0900DataUplinkTransparentTransmission(
            T0900DataUplinkTransparentTransmission.TransparentMessageType.USER_DEFINED_TRANSPARENT_F4,
            largeData.toString().getBytes(StandardCharsets.UTF_8)
        );
        
        System.out.println("  大数据内容长度: " + largeMessage.getMessageContent().length + " 字节");
        System.out.println("  编码后长度: " + largeMessage.encode().length() + " 字节");
        
        // 验证大数据的编码解码性能
        long startTime = System.currentTimeMillis();
        Buffer encoded = largeMessage.encode();
        long encodeTime = System.currentTimeMillis() - startTime;
        
        startTime = System.currentTimeMillis();
        T0900DataUplinkTransparentTransmission decoded = T0900DataUplinkTransparentTransmission.decode(encoded);
        long decodeTime = System.currentTimeMillis() - startTime;
        
        System.out.println("  编码耗时: " + encodeTime + " ms");
        System.out.println("  解码耗时: " + decodeTime + " ms");
        System.out.println("  数据一致性: " + (largeMessage.equals(decoded) ? "通过" : "失败"));
    }
    
    /**
     * 字节数组转十六进制字符串
     */
    private static String bytesToHex(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return "";
        }
        
        StringBuilder hex = new StringBuilder();
        for (byte b : bytes) {
            hex.append(String.format("%02X ", b & 0xFF));
        }
        return hex.toString().trim();
    }
}