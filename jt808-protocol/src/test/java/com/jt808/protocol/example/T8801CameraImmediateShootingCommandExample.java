package com.jt808.protocol.example;

import com.jt808.protocol.message.T8801CameraImmediateShootingCommand;
import io.vertx.core.buffer.Buffer;

/**
 * T8801 摄像头立即拍摄命令消息示例
 * 
 * 演示如何创建、编码和解码摄像头立即拍摄命令消息
 * 
 * 消息体数据格式：
 * - 通道ID (1字节): >0
 * - 拍摄命令 (2字节): 0=停止拍摄, 0xFFFF=录像, 其它=拍照张数
 * - 拍照间隔/录像时间 (2字节): 秒，0=按最小间隔拍照或一直录像
 * - 保存标志 (1字节): 1=保存, 0=实时上传
 * - 分辨率 (1字节): 0x01~0x08对应不同分辨率
 * - 图像/视频质量 (1字节): 1-10，1=质量损失最小，10=压缩比最大
 * - 亮度 (1字节): 0-255
 * - 对比度 (1字节): 0-127
 * - 饱和度 (1字节): 0-127
 * - 色度 (1字节): 0-255
 * 
 * @author JT808 Protocol
 * @version 1.0
 */
public class T8801CameraImmediateShootingCommandExample {

    public static void main(String[] args) {
        System.out.println("=== T8801 摄像头立即拍摄命令消息示例 ===");
        System.out.println();
        
        // 示例1：停止拍摄命令
        demonstrateStopCommand();
        System.out.println();
        
        // 示例2：拍照命令
        demonstratePhotoCommand();
        System.out.println();
        
        // 示例3：录像命令
        demonstrateVideoCommand();
        System.out.println();
        
        // 示例4：自定义参数命令
        demonstrateCustomCommand();
        System.out.println();
        
        // 示例5：编码解码演示
        demonstrateEncodeDecode();
        System.out.println();
        
        // 示例6：批量处理演示
        demonstrateBatchProcessing();
    }

    /**
     * 演示停止拍摄命令
     */
    private static void demonstrateStopCommand() {
        System.out.println("--- 示例1：停止拍摄命令 ---");
        
        int channelId = 1;
        
        // 创建停止拍摄命令
        T8801CameraImmediateShootingCommand command = T8801CameraImmediateShootingCommand.createStopCommand(channelId);
        
        System.out.println("停止拍摄命令创建成功:");
        System.out.println("  通道ID: " + command.getChannelId());
        System.out.println("  拍摄命令: " + command.getShootingCommand() + " (停止拍摄)");
        System.out.println("  命令类型: " + (command.isStopCommand() ? "停止拍摄" : "其他"));
        System.out.println("  分辨率: " + command.getResolutionDescription());
        System.out.println("  保存标志: " + command.getSaveFlagDescription());
        
        // 编码
        Buffer encoded = command.encodeBody();
        System.out.println("  编码后长度: " + encoded.length() + " 字节");
        System.out.println("  编码数据: " + bytesToHex(encoded.getBytes()));
    }

    /**
     * 演示拍照命令
     */
    private static void demonstratePhotoCommand() {
        System.out.println("--- 示例2：拍照命令 ---");
        
        int channelId = 2;
        int photoCount = 5; // 拍5张照片
        int interval = 3; // 间隔3秒
        int saveFlag = 1; // 保存到本地
        T8801CameraImmediateShootingCommand.Resolution resolution = T8801CameraImmediateShootingCommand.Resolution.VGA_640_480;
        
        // 创建拍照命令
        T8801CameraImmediateShootingCommand command = T8801CameraImmediateShootingCommand
                .createPhotoCommand(channelId, photoCount, interval, saveFlag, resolution);
        
        System.out.println("拍照命令创建成功:");
        System.out.println("  通道ID: " + command.getChannelId());
        System.out.println("  拍照张数: " + command.getPhotoCount() + " 张");
        System.out.println("  拍照间隔: " + command.getIntervalOrDuration() + " 秒");
        System.out.println("  命令类型: " + (command.isPhotoCommand() ? "拍照" : "其他"));
        System.out.println("  分辨率: " + command.getResolutionDescription());
        System.out.println("  保存标志: " + command.getSaveFlagDescription());
        System.out.println("  图像质量: " + command.getQuality() + "/10");
        
        // 编码
        Buffer encoded = command.encodeBody();
        System.out.println("  编码后长度: " + encoded.length() + " 字节");
        System.out.println("  编码数据: " + bytesToHex(encoded.getBytes()));
    }

    /**
     * 演示录像命令
     */
    private static void demonstrateVideoCommand() {
        System.out.println("--- 示例3：录像命令 ---");
        
        int channelId = 3;
        int duration = 60; // 录像60秒
        int saveFlag = 0; // 实时上传
        T8801CameraImmediateShootingCommand.Resolution resolution = T8801CameraImmediateShootingCommand.Resolution.D1_704_576;
        
        // 创建录像命令
        T8801CameraImmediateShootingCommand command = T8801CameraImmediateShootingCommand
                .createVideoCommand(channelId, duration, saveFlag, resolution);
        
        System.out.println("录像命令创建成功:");
        System.out.println("  通道ID: " + command.getChannelId());
        System.out.println("  拍摄命令: 0x" + Integer.toHexString(command.getShootingCommand()).toUpperCase() + " (录像)");
        System.out.println("  录像时长: " + command.getIntervalOrDuration() + " 秒");
        System.out.println("  命令类型: " + (command.isVideoCommand() ? "录像" : "其他"));
        System.out.println("  分辨率: " + command.getResolutionDescription());
        System.out.println("  保存标志: " + command.getSaveFlagDescription());
        System.out.println("  视频质量: " + command.getQuality() + "/10");
        
        // 编码
        Buffer encoded = command.encodeBody();
        System.out.println("  编码后长度: " + encoded.length() + " 字节");
        System.out.println("  编码数据: " + bytesToHex(encoded.getBytes()));
    }

    /**
     * 演示自定义参数命令
     */
    private static void demonstrateCustomCommand() {
        System.out.println("--- 示例4：自定义参数命令 ---");
        
        // 创建自定义命令
        T8801CameraImmediateShootingCommand command = new T8801CameraImmediateShootingCommand();
        
        // 设置自定义参数
        command.setChannelId(4);
        command.setShootingCommand(10); // 拍10张照片
        command.setIntervalOrDuration(2); // 间隔2秒
        command.setSaveFlag(1); // 保存
        command.setResolution(T8801CameraImmediateShootingCommand.Resolution.XGA_1024_768.getValue());
        command.setQuality(8); // 高质量
        command.setBrightness(180); // 高亮度
        command.setContrast(90); // 高对比度
        command.setSaturation(100); // 高饱和度
        command.setChromaticity(200); // 高色度
        
        System.out.println("自定义命令创建成功:");
        System.out.println("  通道ID: " + command.getChannelId());
        System.out.println("  拍照张数: " + command.getPhotoCount() + " 张");
        System.out.println("  拍照间隔: " + command.getIntervalOrDuration() + " 秒");
        System.out.println("  分辨率: " + command.getResolutionDescription());
        System.out.println("  保存标志: " + command.getSaveFlagDescription());
        System.out.println("  图像质量: " + command.getQuality() + "/10");
        System.out.println("  亮度: " + command.getBrightness() + "/255");
        System.out.println("  对比度: " + command.getContrast() + "/127");
        System.out.println("  饱和度: " + command.getSaturation() + "/127");
        System.out.println("  色度: " + command.getChromaticity() + "/255");
        
        // 编码
        Buffer encoded = command.encodeBody();
        System.out.println("  编码后长度: " + encoded.length() + " 字节");
        System.out.println("  编码数据: " + bytesToHex(encoded.getBytes()));
    }

    /**
     * 演示编码解码过程
     */
    private static void demonstrateEncodeDecode() {
        System.out.println("--- 示例5：编码解码演示 ---");
        
        // 创建原始命令
        T8801CameraImmediateShootingCommand originalCommand = T8801CameraImmediateShootingCommand
                .createPhotoCommand(5, 3, 5, 1, T8801CameraImmediateShootingCommand.Resolution.SVGA_800_600);
        
        System.out.println("原始命令:");
        System.out.println("  " + originalCommand.toString());
        
        // 编码
        Buffer encoded = originalCommand.encodeBody();
        System.out.println("\n编码结果:");
        System.out.println("  长度: " + encoded.length() + " 字节");
        System.out.println("  数据: " + bytesToHex(encoded.getBytes()));
        
        // 解码
        T8801CameraImmediateShootingCommand decodedCommand = new T8801CameraImmediateShootingCommand();
        decodedCommand.decodeBody(encoded);
        
        System.out.println("\n解码结果:");
        System.out.println("  " + decodedCommand.toString());
        
        // 验证一致性
        boolean isEqual = originalCommand.equals(decodedCommand);
        System.out.println("\n编码解码一致性验证: " + (isEqual ? "通过" : "失败"));
        
        if (isEqual) {
            System.out.println("  所有字段都正确编码和解码");
        } else {
            System.out.println("  编码解码过程中出现数据不一致");
        }
    }

    /**
     * 演示批量处理
     */
    private static void demonstrateBatchProcessing() {
        System.out.println("--- 示例6：批量处理示例 ---");
        
        // 模拟多通道摄像头控制
        int[] channelIds = {1, 2, 3, 4};
        String[] operations = {"停止", "拍照", "录像", "拍照"};
        
        System.out.println("批量处理多通道摄像头控制:");
        
        for (int i = 0; i < channelIds.length; i++) {
            int channelId = channelIds[i];
            String operation = operations[i];
            
            T8801CameraImmediateShootingCommand command;
            
            switch (operation) {
                case "停止":
                    command = T8801CameraImmediateShootingCommand.createStopCommand(channelId);
                    break;
                case "拍照":
                    command = T8801CameraImmediateShootingCommand.createPhotoCommand(
                            channelId, 3, 2, 1, T8801CameraImmediateShootingCommand.Resolution.VGA_640_480);
                    break;
                case "录像":
                    command = T8801CameraImmediateShootingCommand.createVideoCommand(
                            channelId, 30, 0, T8801CameraImmediateShootingCommand.Resolution.D1_704_576);
                    break;
                default:
                    continue;
            }
            
            Buffer encoded = command.encodeBody();
            
            System.out.println("  通道" + channelId + " - " + operation + ":");
            System.out.println("    命令类型: " + getCommandTypeDescription(command));
            System.out.println("    编码长度: " + encoded.length() + " 字节");
            System.out.println("    编码数据: " + bytesToHex(encoded.getBytes()));
        }
        
        System.out.println("\n批量处理完成，共处理 " + channelIds.length + " 个通道");
    }

    /**
     * 获取命令类型描述
     */
    private static String getCommandTypeDescription(T8801CameraImmediateShootingCommand command) {
        if (command.isStopCommand()) {
            return "停止拍摄";
        } else if (command.isVideoCommand()) {
            return "录像 " + command.getIntervalOrDuration() + " 秒";
        } else if (command.isPhotoCommand()) {
            return "拍照 " + command.getPhotoCount() + " 张，间隔 " + command.getIntervalOrDuration() + " 秒";
        } else {
            return "未知命令";
        }
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