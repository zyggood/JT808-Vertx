package com.jt808.protocol.example;

import com.jt808.protocol.factory.JT808MessageFactory;
import com.jt808.protocol.message.T0200LocationReport;
import com.jt808.protocol.message.T0801MultimediaDataUpload;
import io.vertx.core.buffer.Buffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;

/**
 * T0801多媒体数据上传消息使用示例
 */
public class T0801MultimediaDataUploadExample {

    private static final Logger logger = LoggerFactory.getLogger(T0801MultimediaDataUploadExample.class);

    public static void main(String[] args) {
        T0801MultimediaDataUploadExample example = new T0801MultimediaDataUploadExample();
        
        logger.info("=== T0801多媒体数据上传消息示例 ===");
        
        // 基本使用示例
        example.demonstrateBasicUsage();
        
        // 编解码示例
        example.demonstrateEncodeDecode();
        
        // 工厂模式示例
        example.demonstrateMessageFactory();
        
        // 不同类型多媒体示例
        example.demonstrateMultimediaTypes();
    }

    /**
     * 基本使用示例
     */
    private void demonstrateBasicUsage() {
        logger.info("\n--- 基本使用示例 ---");
        
        // 创建多媒体数据上传消息
        T0801MultimediaDataUpload message = new T0801MultimediaDataUpload();
        
        // 设置多媒体信息
        message.setMultimediaId(123456789L);
        message.setMultimediaType(0); // 图像
        message.setFormatCode(0); // JPEG
        message.setEventCode(1); // 定时动作
        message.setChannelId(1);
        
        // 创建位置信息
        T0200LocationReport locationInfo = new T0200LocationReport();
        locationInfo.setAlarmFlag((int)0x00000000L);
        locationInfo.setStatusFlag((int)0x00000002L); // ACC开
        locationInfo.setLatitude((int)(39.908722 * 1000000)); // 北京天安门纬度
        locationInfo.setLongitude((int)(116.397496 * 1000000)); // 北京天安门经度
        locationInfo.setAltitude(50);
        locationInfo.setSpeed(60);
        locationInfo.setDirection(90); // 正东方向
        locationInfo.setDateTime(LocalDateTime.now()); // 当前时间
        message.setLocationInfo(locationInfo);
        
        // 模拟JPEG图像数据
        byte[] jpegHeader = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0}; // JPEG文件头
        Buffer imageData = Buffer.buffer(jpegHeader);
        imageData.appendString("...JPEG图像数据...");
        message.setMultimediaData(imageData);
        
        // 显示消息信息
        logger.info("消息ID: 0x{}", Integer.toHexString(message.getMessageId()).toUpperCase());
        logger.info("多媒体ID: {}", message.getMultimediaId());
        logger.info("多媒体类型: {} ({})", message.getMultimediaType(), message.getMultimediaTypeDescription());
        logger.info("格式编码: {} ({})", message.getFormatCode(), message.getFormatCodeDescription());
        logger.info("事件编码: {} ({})", message.getEventCode(), message.getEventCodeDescription());
        logger.info("通道ID: {}", message.getChannelId());
        logger.info("多媒体数据大小: {} 字节", message.getMultimediaDataSize());
        logger.info("位置信息: 纬度={}, 经度={}, 高度={}m, 速度={}km/h, 方向={}°", 
                   locationInfo.getLatitude(), locationInfo.getLongitude(), 
                   locationInfo.getAltitude(), locationInfo.getSpeed(), locationInfo.getDirection());
    }

    /**
     * 编解码示例
     */
    private void demonstrateEncodeDecode() {
        logger.info("\n--- 编解码示例 ---");
        
        // 创建原始消息
        T0801MultimediaDataUpload originalMessage = new T0801MultimediaDataUpload();
        originalMessage.setMultimediaId(987654321L);
        originalMessage.setMultimediaType(1); // 音频
        originalMessage.setFormatCode(3); // WAV
        originalMessage.setEventCode(2); // 抢劫报警触发
        originalMessage.setChannelId(2);
        
        // 设置位置信息
        T0200LocationReport locationInfo = new T0200LocationReport();
        locationInfo.setAlarmFlag((int)0x00000001L); // 紧急报警
        locationInfo.setStatusFlag((int)0x00000003L); // ACC开+定位
        locationInfo.setLatitude((int)(31.230416 * 1000000)); // 上海外滩纬度
        locationInfo.setLongitude((int)(121.473701 * 1000000)); // 上海外滩经度
        locationInfo.setAltitude(10);
        locationInfo.setSpeed(0); // 静止状态
        locationInfo.setDirection(0);
        locationInfo.setDateTime(LocalDateTime.now());
        originalMessage.setLocationInfo(locationInfo);
        
        // 模拟WAV音频数据
        Buffer audioData = Buffer.buffer("RIFF"); // WAV文件头
        audioData.appendString("...WAV音频数据...");
        originalMessage.setMultimediaData(audioData);
        
        logger.info("原始消息: {}", originalMessage);
        
        // 编码
        Buffer encoded = originalMessage.encodeBody();
        logger.info("编码后长度: {} 字节", encoded.length());
        logger.info("编码数据: {}", bytesToHex(encoded.getBytes(), 0, Math.min(32, encoded.length())));
        
        // 解码
        T0801MultimediaDataUpload decodedMessage = new T0801MultimediaDataUpload();
        decodedMessage.decodeBody(encoded);
        
        logger.info("解码后消息: {}", decodedMessage);
        
        // 验证一致性
        boolean consistent = originalMessage.getMultimediaId() == decodedMessage.getMultimediaId() &&
                           originalMessage.getMultimediaType() == decodedMessage.getMultimediaType() &&
                           originalMessage.getFormatCode() == decodedMessage.getFormatCode() &&
                           originalMessage.getEventCode() == decodedMessage.getEventCode() &&
                           originalMessage.getChannelId() == decodedMessage.getChannelId() &&
                           originalMessage.getMultimediaDataSize() == decodedMessage.getMultimediaDataSize();
        
        logger.info("编解码一致性: {}", consistent ? "✓ 通过" : "✗ 失败");
    }

    /**
     * 工厂模式示例
     */
    private void demonstrateMessageFactory() {
        logger.info("\n--- JT808MessageFactory示例 ---");
        
        JT808MessageFactory factory = JT808MessageFactory.getInstance();
        
        // 检查是否支持T0801消息
        boolean isSupported = factory.isSupported(0x0801);
        logger.info("工厂是否支持0x0801消息: {}", isSupported);
        
        if (isSupported) {
            // 创建消息实例
            T0801MultimediaDataUpload message = (T0801MultimediaDataUpload) factory.createMessage(0x0801);
            logger.info("通过工厂创建的消息类型: {}", message.getClass().getSimpleName());
            logger.info("消息ID: 0x{}", Integer.toHexString(message.getMessageId()).toUpperCase());
        }
        
        // 显示所有支持的消息ID
        logger.info("工厂支持的消息ID数量: {}", factory.getSupportedMessageIds().size());
    }

    /**
     * 不同类型多媒体示例
     */
    private void demonstrateMultimediaTypes() {
        logger.info("\n--- 不同类型多媒体示例 ---");
        
        // 图像类型示例
        demonstrateImageType();
        
        // 音频类型示例
        demonstrateAudioType();
        
        // 视频类型示例
        demonstrateVideoType();
    }

    private void demonstrateImageType() {
        logger.info("\n1. 图像类型示例:");
        
        T0801MultimediaDataUpload imageMessage = new T0801MultimediaDataUpload();
        imageMessage.setMultimediaId(1001L);
        imageMessage.setMultimediaType(0); // 图像
        imageMessage.setFormatCode(0); // JPEG
        imageMessage.setEventCode(0); // 平台下发指令
        imageMessage.setChannelId(1);
        
        // 模拟JPEG图像数据
        Buffer jpegData = Buffer.buffer(new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0});
        jpegData.appendString("...JPEG图像数据内容...");
        imageMessage.setMultimediaData(jpegData);
        
        logger.info("  类型: {} | 格式: {} | 大小: {} 字节", 
                   imageMessage.getMultimediaTypeDescription(),
                   imageMessage.getFormatCodeDescription(),
                   imageMessage.getMultimediaDataSize());
    }

    private void demonstrateAudioType() {
        logger.info("\n2. 音频类型示例:");
        
        T0801MultimediaDataUpload audioMessage = new T0801MultimediaDataUpload();
        audioMessage.setMultimediaId(1002L);
        audioMessage.setMultimediaType(1); // 音频
        audioMessage.setFormatCode(2); // MP3
        audioMessage.setEventCode(1); // 定时动作
        audioMessage.setChannelId(2);
        
        // 模拟MP3音频数据
        Buffer mp3Data = Buffer.buffer("ID3"); // MP3文件头
        mp3Data.appendString("...MP3音频数据内容...");
        audioMessage.setMultimediaData(mp3Data);
        
        logger.info("  类型: {} | 格式: {} | 大小: {} 字节", 
                   audioMessage.getMultimediaTypeDescription(),
                   audioMessage.getFormatCodeDescription(),
                   audioMessage.getMultimediaDataSize());
    }

    private void demonstrateVideoType() {
        logger.info("\n3. 视频类型示例:");
        
        T0801MultimediaDataUpload videoMessage = new T0801MultimediaDataUpload();
        videoMessage.setMultimediaId(1003L);
        videoMessage.setMultimediaType(2); // 视频
        videoMessage.setFormatCode(4); // WMV
        videoMessage.setEventCode(3); // 碰撞侧翻报警触发
        videoMessage.setChannelId(3);
        
        // 模拟WMV视频数据
        Buffer wmvData = Buffer.buffer(new byte[]{0x30, 0x26, (byte) 0xB2, 0x75}); // WMV文件头
        wmvData.appendString("...WMV视频数据内容...");
        videoMessage.setMultimediaData(wmvData);
        
        logger.info("  类型: {} | 格式: {} | 大小: {} 字节", 
                   videoMessage.getMultimediaTypeDescription(),
                   videoMessage.getFormatCodeDescription(),
                   videoMessage.getMultimediaDataSize());
    }

    /**
     * 字节数组转十六进制字符串
     */
    private String bytesToHex(byte[] bytes, int offset, int length) {
        StringBuilder result = new StringBuilder();
        for (int i = offset; i < offset + length && i < bytes.length; i++) {
            result.append(String.format("%02X ", bytes[i]));
        }
        if (length < bytes.length - offset) {
            result.append("...");
        }
        return result.toString().trim();
    }
}