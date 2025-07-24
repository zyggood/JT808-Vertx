package com.jt808.protocol.example;

import com.jt808.protocol.constants.MessageTypes;
import com.jt808.protocol.factory.JT808MessageFactory;
import com.jt808.protocol.message.JT808Message;
import com.jt808.protocol.message.T8804RecordingStartCommand;
import io.vertx.core.buffer.Buffer;

/**
 * T8804录音开始命令示例程序
 * 演示如何创建、编码、解码和使用T8804消息
 */
public class T8804RecordingStartCommandExample {
    
    public static void main(String[] args) {
        System.out.println("=== T8804录音开始命令示例 ===");
        
        // 1. 基本用法示例
        basicUsageExample();
        
        // 2. 不同录音配置示例
        differentConfigurationsExample();
        
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
        
        T8804RecordingStartCommand command = new T8804RecordingStartCommand(
            T8804RecordingStartCommand.RecordingCommand.START,
            300, // 录音5分钟
            T8804RecordingStartCommand.SaveFlag.SAVE,
            T8804RecordingStartCommand.AudioSampleRate.RATE_8K
        );
        
        System.out.println("消息ID: 0x" + Integer.toHexString(command.getMessageId()).toUpperCase());
        System.out.println("录音命令: " + command.getRecordingCommand().getDescription());
        System.out.println("录音时间: " + command.getRecordingTime() + " 秒 (" + (command.getRecordingTime() / 60) + " 分钟)");
        System.out.println("保存标志: " + command.getSaveFlag().getDescription());
        System.out.println("音频采样率: " + command.getAudioSampleRate().getDescription());
    }
    
    /**
     * 不同录音配置示例
     */
    private static void differentConfigurationsExample() {
        System.out.println("\n2. 不同录音配置示例:");
        
        // 开始录音 - 实时上传
        T8804RecordingStartCommand startRealTime = new T8804RecordingStartCommand(
            T8804RecordingStartCommand.RecordingCommand.START,
            600, // 10分钟
            T8804RecordingStartCommand.SaveFlag.REAL_TIME_UPLOAD,
            T8804RecordingStartCommand.AudioSampleRate.RATE_11K
        );
        
        // 开始录音 - 保存到本地
        T8804RecordingStartCommand startSave = new T8804RecordingStartCommand(
            T8804RecordingStartCommand.RecordingCommand.START,
            1800, // 30分钟
            T8804RecordingStartCommand.SaveFlag.SAVE,
            T8804RecordingStartCommand.AudioSampleRate.RATE_23K
        );
        
        // 连续录音（时间为0表示一直录音）
        T8804RecordingStartCommand continuousRecording = new T8804RecordingStartCommand(
            T8804RecordingStartCommand.RecordingCommand.START,
            0, // 一直录音
            T8804RecordingStartCommand.SaveFlag.REAL_TIME_UPLOAD,
            T8804RecordingStartCommand.AudioSampleRate.RATE_32K
        );
        
        // 停止录音
        T8804RecordingStartCommand stopRecording = new T8804RecordingStartCommand(
            T8804RecordingStartCommand.RecordingCommand.STOP,
            0, // 停止录音时时间参数无意义
            T8804RecordingStartCommand.SaveFlag.SAVE,
            T8804RecordingStartCommand.AudioSampleRate.RATE_8K
        );
        
        System.out.println("实时上传录音: " + startRealTime);
        System.out.println("本地保存录音: " + startSave);
        System.out.println("连续录音: " + continuousRecording);
        System.out.println("停止录音: " + stopRecording);
    }
    
    /**
     * 编码解码过程演示
     */
    private static void encodeDecodeExample() {
        System.out.println("\n3. 编码解码过程演示:");
        
        T8804RecordingStartCommand original = new T8804RecordingStartCommand(
            T8804RecordingStartCommand.RecordingCommand.START,
            900, // 15分钟
            T8804RecordingStartCommand.SaveFlag.REAL_TIME_UPLOAD,
            T8804RecordingStartCommand.AudioSampleRate.RATE_23K
        );
        
        System.out.println("原始消息: " + original);
        
        // 编码
        Buffer encoded = original.encode();
        System.out.println("编码后长度: " + encoded.length() + " 字节");
        System.out.println("编码后数据: " + bytesToHex(encoded.getBytes()));
        
        // 解码
        T8804RecordingStartCommand decoded = T8804RecordingStartCommand.decode(encoded);
        System.out.println("解码后消息: " + decoded);
        
        // 验证编码解码一致性
        boolean isEqual = original.equals(decoded);
        System.out.println("编码解码一致性验证: " + (isEqual ? "通过" : "失败"));
    }
    
    /**
     * 工厂模式使用示例
     */
    private static void factoryExample() {
        System.out.println("\n4. 工厂模式使用示例:");
        
        JT808MessageFactory factory = JT808MessageFactory.getInstance();
        
        // 通过工厂创建消息
        JT808Message message = factory.createMessage(MessageTypes.Platform.RECORDING_START_COMMAND);
        
        if (message instanceof T8804RecordingStartCommand) {
            T8804RecordingStartCommand command = (T8804RecordingStartCommand) message;
            
            // 设置消息内容
            command.setRecordingCommand(T8804RecordingStartCommand.RecordingCommand.START);
            command.setRecordingTime(1200); // 20分钟
            command.setSaveFlag(T8804RecordingStartCommand.SaveFlag.SAVE);
            command.setAudioSampleRate(T8804RecordingStartCommand.AudioSampleRate.RATE_32K);
            
            System.out.println("通过工厂创建的消息: " + command);
            System.out.println("消息ID: 0x" + Integer.toHexString(command.getMessageId()).toUpperCase());
        }
    }
    
    /**
     * 实际应用场景模拟
     */
    private static void realWorldScenarioExample() {
        System.out.println("\n5. 实际应用场景模拟:");
        
        // 场景1: 紧急情况录音
        System.out.println("\n场景1: 紧急情况录音（高质量实时上传）");
        T8804RecordingStartCommand emergencyRecording = new T8804RecordingStartCommand(
            T8804RecordingStartCommand.RecordingCommand.START,
            300, // 5分钟紧急录音
            T8804RecordingStartCommand.SaveFlag.REAL_TIME_UPLOAD, // 实时上传
            T8804RecordingStartCommand.AudioSampleRate.RATE_32K // 高质量音频
        );
        System.out.println(emergencyRecording);
        
        // 场景2: 日常监控录音
        System.out.println("\n场景2: 日常监控录音（本地保存）");
        T8804RecordingStartCommand dailyMonitoring = new T8804RecordingStartCommand(
            T8804RecordingStartCommand.RecordingCommand.START,
            3600, // 1小时录音
            T8804RecordingStartCommand.SaveFlag.SAVE, // 本地保存
            T8804RecordingStartCommand.AudioSampleRate.RATE_8K // 标准质量
        );
        System.out.println(dailyMonitoring);
        
        // 场景3: 连续监听模式
        System.out.println("\n场景3: 连续监听模式");
        T8804RecordingStartCommand continuousMonitoring = new T8804RecordingStartCommand(
            T8804RecordingStartCommand.RecordingCommand.START,
            0, // 连续录音
            T8804RecordingStartCommand.SaveFlag.REAL_TIME_UPLOAD,
            T8804RecordingStartCommand.AudioSampleRate.RATE_11K
        );
        System.out.println(continuousMonitoring);
        
        // 场景4: 停止所有录音
        System.out.println("\n场景4: 停止所有录音");
        T8804RecordingStartCommand stopAllRecording = new T8804RecordingStartCommand(
            T8804RecordingStartCommand.RecordingCommand.STOP,
            0,
            T8804RecordingStartCommand.SaveFlag.SAVE,
            T8804RecordingStartCommand.AudioSampleRate.RATE_8K
        );
        System.out.println(stopAllRecording);
        
        // 场景5: 不同采样率对比
        System.out.println("\n场景5: 不同采样率录音命令对比");
        T8804RecordingStartCommand.AudioSampleRate[] rates = {
            T8804RecordingStartCommand.AudioSampleRate.RATE_8K,
            T8804RecordingStartCommand.AudioSampleRate.RATE_11K,
            T8804RecordingStartCommand.AudioSampleRate.RATE_23K,
            T8804RecordingStartCommand.AudioSampleRate.RATE_32K
        };
        
        for (T8804RecordingStartCommand.AudioSampleRate rate : rates) {
            T8804RecordingStartCommand command = new T8804RecordingStartCommand(
                T8804RecordingStartCommand.RecordingCommand.START,
                600, // 10分钟
                T8804RecordingStartCommand.SaveFlag.SAVE,
                rate
            );
            System.out.println(rate.getDescription() + " 采样率命令: " + command);
        }
        
        // 编码所有命令并显示大小
        System.out.println("\n各命令编码后大小:");
        System.out.println("紧急录音命令: " + emergencyRecording.encode().length() + " 字节");
        System.out.println("日常监控命令: " + dailyMonitoring.encode().length() + " 字节");
        System.out.println("连续监听命令: " + continuousMonitoring.encode().length() + " 字节");
        System.out.println("停止录音命令: " + stopAllRecording.encode().length() + " 字节");
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