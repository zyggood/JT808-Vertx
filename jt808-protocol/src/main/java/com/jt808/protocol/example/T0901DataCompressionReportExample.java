package com.jt808.protocol.example;

import com.jt808.protocol.constants.MessageTypes;
import com.jt808.protocol.factory.JT808MessageFactory;
import com.jt808.protocol.message.JT808Message;
import com.jt808.protocol.message.T0901DataCompressionReport;
import io.vertx.core.buffer.Buffer;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * T0901数据压缩上报示例程序
 * 演示如何创建、编码、解码和使用0x0901数据压缩上报消息
 */
public class T0901DataCompressionReportExample {
    
    public static void main(String[] args) {
        System.out.println("=== T0901数据压缩上报示例程序 ===");
        
        try {
            // 基本用法示例
            basicUsageExample();
            
            // 压缩效果对比示例
            compressionComparisonExample();
            
            // 编码解码过程分析
            encodeDecodeAnalysisExample();
            
            // 工厂模式使用示例
            factoryUsageExample();
            
            // 实际应用场景模拟
            realWorldScenarioExample();
            
        } catch (IOException e) {
            System.err.println("示例程序执行出错: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 基本用法示例
     */
    private static void basicUsageExample() throws IOException {
        System.out.println("\n--- 基本用法示例 ---");
        
        // 方式1: 直接使用已压缩的数据
        String originalText = "这是一个需要压缩的测试字符串，包含重复内容重复内容重复内容";
        byte[] originalData = originalText.getBytes(StandardCharsets.UTF_8);
        byte[] compressedData = T0901DataCompressionReport.compressData(originalData);
        
        T0901DataCompressionReport message1 = new T0901DataCompressionReport(compressedData);
        
        System.out.println("方式1 - 使用已压缩数据:");
        System.out.println("  消息ID: 0x" + Integer.toHexString(message1.getMessageId()).toUpperCase());
        System.out.println("  原始数据长度: " + originalData.length + " 字节");
        System.out.println("  压缩后长度: " + message1.getCompressedMessageLength() + " 字节");
        System.out.println("  压缩比: " + String.format("%.1f%%", message1.getCompressionRatio(originalData.length)));
        
        // 方式2: 自动压缩原始数据
        T0901DataCompressionReport message2 = new T0901DataCompressionReport(originalData, true);
        
        System.out.println("\n方式2 - 自动压缩原始数据:");
        System.out.println("  压缩后长度: " + message2.getCompressedMessageLength() + " 字节");
        System.out.println("  压缩比: " + String.format("%.1f%%", message2.getCompressionRatio(originalData.length)));
        
        // 验证解压缩
        byte[] decompressed = message2.decompressMessageBody();
        String decompressedText = new String(decompressed, StandardCharsets.UTF_8);
        System.out.println("  解压缩验证: " + (originalText.equals(decompressedText) ? "成功" : "失败"));
        
        // 方式3: 不压缩直接存储
        T0901DataCompressionReport message3 = new T0901DataCompressionReport(originalData, false);
        
        System.out.println("\n方式3 - 不压缩直接存储:");
        System.out.println("  存储长度: " + message3.getCompressedMessageLength() + " 字节");
        System.out.println("  与原始数据相同: " + (message3.getCompressedMessageLength() == originalData.length));
    }
    
    /**
     * 压缩效果对比示例
     */
    private static void compressionComparisonExample() throws IOException {
        System.out.println("\n--- 压缩效果对比示例 ---");
        
        // 测试不同类型数据的压缩效果
        
        // 1. 重复性高的数据
        byte[] repetitiveData = new byte[1000];
        for (int i = 0; i < repetitiveData.length; i++) {
            repetitiveData[i] = (byte) (i % 10); // 重复模式
        }
        
        T0901DataCompressionReport repetitiveMessage = new T0901DataCompressionReport(repetitiveData, true);
        
        System.out.println("重复性数据压缩:");
        System.out.println("  原始长度: " + repetitiveData.length + " 字节");
        System.out.println("  压缩后长度: " + repetitiveMessage.getCompressedMessageLength() + " 字节");
        System.out.println("  压缩比: " + String.format("%.1f%%", repetitiveMessage.getCompressionRatio(repetitiveData.length)));
        
        // 2. 文本数据
        String textData = "";
        for (int i = 0; i < 50; i++) {
            textData += "这是第" + i + "行文本数据，包含中文字符和数字。\n";
        }
        byte[] textBytes = textData.getBytes(StandardCharsets.UTF_8);
        
        T0901DataCompressionReport textMessage = new T0901DataCompressionReport(textBytes, true);
        
        System.out.println("\n文本数据压缩:");
        System.out.println("  原始长度: " + textBytes.length + " 字节");
        System.out.println("  压缩后长度: " + textMessage.getCompressedMessageLength() + " 字节");
        System.out.println("  压缩比: " + String.format("%.1f%%", textMessage.getCompressionRatio(textBytes.length)));
        
        // 3. 随机数据（压缩效果差）
        byte[] randomData = new byte[1000];
        for (int i = 0; i < randomData.length; i++) {
            randomData[i] = (byte) (Math.random() * 256);
        }
        
        T0901DataCompressionReport randomMessage = new T0901DataCompressionReport(randomData, true);
        
        System.out.println("\n随机数据压缩:");
        System.out.println("  原始长度: " + randomData.length + " 字节");
        System.out.println("  压缩后长度: " + randomMessage.getCompressedMessageLength() + " 字节");
        System.out.println("  压缩比: " + String.format("%.1f%%", randomMessage.getCompressionRatio(randomData.length)));
        
        // 4. 空数据
        T0901DataCompressionReport emptyMessage = new T0901DataCompressionReport(new byte[0], true);
        
        System.out.println("\n空数据压缩:");
        System.out.println("  原始长度: 0 字节");
        System.out.println("  压缩后长度: " + emptyMessage.getCompressedMessageLength() + " 字节");
    }
    
    /**
     * 编码解码过程分析
     */
    private static void encodeDecodeAnalysisExample() throws IOException {
        System.out.println("\n--- 编码解码过程分析 ---");
        
        // 创建包含JSON格式的数据
        String jsonData = "{\"timestamp\":\"" + LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) + 
                         "\",\"vehicle_id\":\"京A12345\",\"location\":{\"lat\":39.9042,\"lng\":116.4074},\"speed\":60.5,\"status\":\"normal\"}";
        byte[] jsonBytes = jsonData.getBytes(StandardCharsets.UTF_8);
        
        T0901DataCompressionReport originalMessage = new T0901DataCompressionReport(jsonBytes, true);
        
        System.out.println("原始JSON数据:");
        System.out.println("  内容: " + jsonData);
        System.out.println("  原始长度: " + jsonBytes.length + " 字节");
        System.out.println("  压缩后长度: " + originalMessage.getCompressedMessageLength() + " 字节");
        System.out.println("  压缩比: " + String.format("%.1f%%", originalMessage.getCompressionRatio(jsonBytes.length)));
        
        // 编码过程
        Buffer encodedBuffer = originalMessage.encode();
        System.out.println("\n编码过程:");
        System.out.println("  编码后总长度: " + encodedBuffer.length() + " 字节");
        System.out.println("  编码后数据: " + bytesToHex(encodedBuffer.getBytes()));
        
        // 分析编码结构
        System.out.println("\n编码结构分析:");
        long compressedLength = encodedBuffer.getUnsignedInt(0);
        System.out.println("  字节0-3 (压缩消息长度): " + compressedLength + " (0x" + 
                          Long.toHexString(compressedLength).toUpperCase() + ")");
        System.out.println("  字节4-" + (encodedBuffer.length() - 1) + " (压缩消息体): " + 
                          bytesToHex(encodedBuffer.getBytes(4, encodedBuffer.length())));
        
        // 解码过程
        T0901DataCompressionReport decodedMessage = T0901DataCompressionReport.decode(encodedBuffer);
        
        System.out.println("\n解码结果:");
        System.out.println("  压缩消息长度: " + decodedMessage.getCompressedMessageLength() + " 字节");
        System.out.println("  压缩消息体长度: " + decodedMessage.getCompressedMessageBody().length + " 字节");
        
        // 解压缩验证
        byte[] decompressed = decodedMessage.decompressMessageBody();
        String decompressedJson = new String(decompressed, StandardCharsets.UTF_8);
        System.out.println("  解压缩后内容: " + decompressedJson);
        
        // 验证编码解码的一致性
        boolean isConsistent = originalMessage.equals(decodedMessage) && jsonData.equals(decompressedJson);
        System.out.println("\n编码解码一致性: " + (isConsistent ? "通过" : "失败"));
    }
    
    /**
     * 工厂模式使用示例
     */
    private static void factoryUsageExample() throws IOException {
        System.out.println("\n--- 工厂模式使用示例 ---");
        
        // 使用工厂创建消息
        JT808MessageFactory factory = JT808MessageFactory.getInstance();
        JT808Message message = factory.createMessage(MessageTypes.Terminal.DATA_COMPRESSION_REPORT);
        
        if (message instanceof T0901DataCompressionReport) {
            T0901DataCompressionReport compressionMessage = (T0901DataCompressionReport) message;
            
            // 设置要压缩的数据
            String factoryData = "通过工厂创建的压缩消息，包含需要压缩的数据内容";
            compressionMessage.setOriginalDataAndCompress(factoryData.getBytes(StandardCharsets.UTF_8));
            
            System.out.println("工厂创建的消息:");
            System.out.println("  消息ID: 0x" + Integer.toHexString(compressionMessage.getMessageId()).toUpperCase());
            System.out.println("  原始数据长度: " + factoryData.getBytes(StandardCharsets.UTF_8).length + " 字节");
            System.out.println("  压缩后长度: " + compressionMessage.getCompressedMessageLength() + " 字节");
            System.out.println("  压缩比: " + String.format("%.1f%%", 
                              compressionMessage.getCompressionRatio(factoryData.getBytes(StandardCharsets.UTF_8).length)));
            
            // 验证解压缩
            byte[] decompressed = compressionMessage.decompressMessageBody();
            String decompressedText = new String(decompressed, StandardCharsets.UTF_8);
            System.out.println("  解压缩验证: " + (factoryData.equals(decompressedText) ? "成功" : "失败"));
        }
    }
    
    /**
     * 实际应用场景模拟
     */
    private static void realWorldScenarioExample() throws IOException {
        System.out.println("\n--- 实际应用场景模拟 ---");
        
        // 场景1: 批量位置数据压缩上报
        simulateBatchLocationDataCompression();
        
        // 场景2: 日志文件压缩上报
        simulateLogFileCompression();
        
        // 场景3: 配置文件压缩上报
        simulateConfigFileCompression();
        
        // 场景4: 图片数据压缩上报
        simulateImageDataCompression();
        
        // 场景5: 大量传感器数据压缩
        simulateSensorDataCompression();
        
        // 场景6: 压缩性能测试
        performanceTest();
    }
    
    private static void simulateBatchLocationDataCompression() throws IOException {
        System.out.println("\n场景1: 批量位置数据压缩上报");
        
        // 模拟100个位置点的数据
        StringBuilder locationData = new StringBuilder();
        locationData.append("[\n");
        
        for (int i = 0; i < 100; i++) {
            double lat = 39.9042 + (Math.random() - 0.5) * 0.01;
            double lng = 116.4074 + (Math.random() - 0.5) * 0.01;
            int speed = (int) (Math.random() * 120);
            
            locationData.append(String.format(
                "  {\"timestamp\":\"%s\",\"lat\":%.6f,\"lng\":%.6f,\"speed\":%d}%s\n",
                LocalDateTime.now().plusMinutes(i).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                lat, lng, speed,
                i < 99 ? "," : ""
            ));
        }
        locationData.append("]");
        
        byte[] originalData = locationData.toString().getBytes(StandardCharsets.UTF_8);
        T0901DataCompressionReport locationMessage = new T0901DataCompressionReport(originalData, true);
        
        System.out.println("  位置数据点数: 100个");
        System.out.println("  原始数据大小: " + originalData.length + " 字节");
        System.out.println("  压缩后大小: " + locationMessage.getCompressedMessageLength() + " 字节");
        System.out.println("  压缩比: " + String.format("%.1f%%", locationMessage.getCompressionRatio(originalData.length)));
        System.out.println("  节省空间: " + (originalData.length - locationMessage.getCompressedMessageLength()) + " 字节");
    }
    
    private static void simulateLogFileCompression() throws IOException {
        System.out.println("\n场景2: 日志文件压缩上报");
        
        // 模拟系统日志
        StringBuilder logData = new StringBuilder();
        String[] logLevels = {"INFO", "WARN", "ERROR", "DEBUG"};
        String[] components = {"GPS", "ENGINE", "BRAKE", "TRANSMISSION", "FUEL"};
        
        for (int i = 0; i < 200; i++) {
            String timestamp = LocalDateTime.now().minusMinutes(200 - i).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            String level = logLevels[(int) (Math.random() * logLevels.length)];
            String component = components[(int) (Math.random() * components.length)];
            
            logData.append(String.format("[%s] %s %s - 系统运行正常，状态码: %d\n",
                                       timestamp, level, component, (int) (Math.random() * 1000)));
        }
        
        byte[] originalLogData = logData.toString().getBytes(StandardCharsets.UTF_8);
        T0901DataCompressionReport logMessage = new T0901DataCompressionReport(originalLogData, true);
        
        System.out.println("  日志条数: 200条");
        System.out.println("  原始日志大小: " + originalLogData.length + " 字节");
        System.out.println("  压缩后大小: " + logMessage.getCompressedMessageLength() + " 字节");
        System.out.println("  压缩比: " + String.format("%.1f%%", logMessage.getCompressionRatio(originalLogData.length)));
    }
    
    private static void simulateConfigFileCompression() throws IOException {
        System.out.println("\n场景3: 配置文件压缩上报");
        
        // 模拟设备配置文件（XML格式）
        StringBuilder configData = new StringBuilder();
        configData.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        configData.append("<device_config>\n");
        configData.append("  <general>\n");
        configData.append("    <device_id>JT808_DEVICE_001</device_id>\n");
        configData.append("    <firmware_version>2.1.5</firmware_version>\n");
        configData.append("    <protocol_version>2019</protocol_version>\n");
        configData.append("  </general>\n");
        configData.append("  <communication>\n");
        configData.append("    <server_ip>192.168.1.100</server_ip>\n");
        configData.append("    <server_port>8080</server_port>\n");
        configData.append("    <heartbeat_interval>30</heartbeat_interval>\n");
        configData.append("  </communication>\n");
        configData.append("  <sensors>\n");
        
        for (int i = 1; i <= 10; i++) {
            configData.append(String.format("    <sensor id=\"%d\" type=\"temperature\" enabled=\"true\" threshold=\"%.1f\"/>\n",
                                           i, 20.0 + Math.random() * 40));
        }
        
        configData.append("  </sensors>\n");
        configData.append("</device_config>\n");
        
        byte[] originalConfigData = configData.toString().getBytes(StandardCharsets.UTF_8);
        T0901DataCompressionReport configMessage = new T0901DataCompressionReport(originalConfigData, true);
        
        System.out.println("  配置文件类型: XML");
        System.out.println("  原始配置大小: " + originalConfigData.length + " 字节");
        System.out.println("  压缩后大小: " + configMessage.getCompressedMessageLength() + " 字节");
        System.out.println("  压缩比: " + String.format("%.1f%%", configMessage.getCompressionRatio(originalConfigData.length)));
    }
    
    private static void simulateImageDataCompression() throws IOException {
        System.out.println("\n场景4: 图片数据压缩上报");
        
        // 模拟简单的位图数据（实际应用中可能是摄像头捕获的图片）
        byte[] imageData = new byte[10240]; // 10KB的模拟图片数据
        
        // 创建一个简单的模式（模拟图片的重复像素）
        for (int i = 0; i < imageData.length; i++) {
            if (i % 100 < 50) {
                imageData[i] = (byte) 0xFF; // 白色像素
            } else {
                imageData[i] = (byte) 0x00; // 黑色像素
            }
        }
        
        T0901DataCompressionReport imageMessage = new T0901DataCompressionReport(imageData, true);
        
        System.out.println("  图片数据类型: 模拟位图");
        System.out.println("  原始图片大小: " + imageData.length + " 字节");
        System.out.println("  压缩后大小: " + imageMessage.getCompressedMessageLength() + " 字节");
        System.out.println("  压缩比: " + String.format("%.1f%%", imageMessage.getCompressionRatio(imageData.length)));
        
        // 验证图片数据的完整性
        byte[] decompressedImage = imageMessage.decompressMessageBody();
        boolean imageIntegrity = java.util.Arrays.equals(imageData, decompressedImage);
        System.out.println("  图片数据完整性: " + (imageIntegrity ? "完整" : "损坏"));
    }
    
    private static void simulateSensorDataCompression() throws IOException {
        System.out.println("\n场景5: 大量传感器数据压缩");
        
        // 模拟24小时的传感器数据（每分钟一次采样）
        StringBuilder sensorData = new StringBuilder();
        sensorData.append("{\"sensor_readings\":[\n");
        
        for (int i = 0; i < 1440; i++) { // 24小时 * 60分钟
            LocalDateTime timestamp = LocalDateTime.now().minusMinutes(1440 - i);
            
            sensorData.append(String.format(
                "  {\"time\":\"%s\",\"temp\":%.1f,\"humidity\":%.1f,\"pressure\":%.1f,\"vibration\":%.2f}%s\n",
                timestamp.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                20.0 + Math.sin(i * Math.PI / 720) * 10, // 温度变化模式
                50.0 + Math.cos(i * Math.PI / 360) * 20, // 湿度变化模式
                1013.25 + Math.sin(i * Math.PI / 180) * 5, // 气压变化模式
                Math.random() * 0.1, // 随机振动
                i < 1439 ? "," : ""
            ));
        }
        sensorData.append("]}");
        
        byte[] originalSensorData = sensorData.toString().getBytes(StandardCharsets.UTF_8);
        T0901DataCompressionReport sensorMessage = new T0901DataCompressionReport(originalSensorData, true);
        
        System.out.println("  传感器数据点数: 1440个 (24小时)");
        System.out.println("  原始数据大小: " + originalSensorData.length + " 字节");
        System.out.println("  压缩后大小: " + sensorMessage.getCompressedMessageLength() + " 字节");
        System.out.println("  压缩比: " + String.format("%.1f%%", sensorMessage.getCompressionRatio(originalSensorData.length)));
        System.out.println("  节省流量: " + (originalSensorData.length - sensorMessage.getCompressedMessageLength()) + " 字节");
    }
    
    private static void performanceTest() throws IOException {
        System.out.println("\n场景6: 压缩性能测试");
        
        // 创建不同大小的测试数据
        int[] dataSizes = {1024, 5120, 10240, 51200}; // 1KB, 5KB, 10KB, 50KB
        
        for (int size : dataSizes) {
            // 创建测试数据（重复模式，便于压缩）
            byte[] testData = new byte[size];
            for (int i = 0; i < testData.length; i++) {
                testData[i] = (byte) (i % 256);
            }
            
            // 测试压缩性能
            long startTime = System.currentTimeMillis();
            T0901DataCompressionReport message = new T0901DataCompressionReport(testData, true);
            long compressionTime = System.currentTimeMillis() - startTime;
            
            // 测试编码性能
            startTime = System.currentTimeMillis();
            Buffer encoded = message.encode();
            long encodeTime = System.currentTimeMillis() - startTime;
            
            // 测试解码性能
            startTime = System.currentTimeMillis();
            T0901DataCompressionReport decoded = T0901DataCompressionReport.decode(encoded);
            long decodeTime = System.currentTimeMillis() - startTime;
            
            // 测试解压缩性能
            startTime = System.currentTimeMillis();
            byte[] decompressed = decoded.decompressMessageBody();
            long decompressionTime = System.currentTimeMillis() - startTime;
            
            System.out.println(String.format("\n  数据大小: %d 字节", size));
            System.out.println(String.format("    压缩耗时: %d ms", compressionTime));
            System.out.println(String.format("    编码耗时: %d ms", encodeTime));
            System.out.println(String.format("    解码耗时: %d ms", decodeTime));
            System.out.println(String.format("    解压缩耗时: %d ms", decompressionTime));
            System.out.println(String.format("    压缩比: %.1f%%", message.getCompressionRatio(testData.length)));
            System.out.println(String.format("    数据完整性: %s", 
                              java.util.Arrays.equals(testData, decompressed) ? "完整" : "损坏"));
        }
    }
    
    /**
     * 字节数组转十六进制字符串
     */
    private static String bytesToHex(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return "";
        }
        
        StringBuilder hex = new StringBuilder();
        for (int i = 0; i < Math.min(bytes.length, 20); i++) { // 只显示前20个字节
            hex.append(String.format("%02X ", bytes[i] & 0xFF));
        }
        if (bytes.length > 20) {
            hex.append("...");
        }
        return hex.toString().trim();
    }
}