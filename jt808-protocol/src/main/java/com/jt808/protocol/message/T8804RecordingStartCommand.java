package com.jt808.protocol.message;

import com.jt808.protocol.constants.MessageTypes;
import io.vertx.core.buffer.Buffer;

import java.util.Objects;

/**
 * 录音开始命令 (0x8804)
 * 平台向终端下发录音开始命令
 */
public class T8804RecordingStartCommand extends JT808Message {
    
    /**
     * 录音命令枚举
     */
    public enum RecordingCommand {
        STOP(0, "停止录音"),
        START(1, "开始录音");
        
        private final int value;
        private final String description;
        
        RecordingCommand(int value, String description) {
            this.value = value;
            this.description = description;
        }
        
        public int getValue() {
            return value;
        }
        
        public String getDescription() {
            return description;
        }
        
        public static RecordingCommand fromValue(int value) {
            for (RecordingCommand command : values()) {
                if (command.value == value) {
                    return command;
                }
            }
            throw new IllegalArgumentException("未知的录音命令: " + value);
        }
    }
    
    /**
     * 保存标志枚举
     */
    public enum SaveFlag {
        REAL_TIME_UPLOAD(0, "实时上传"),
        SAVE(1, "保存");
        
        private final int value;
        private final String description;
        
        SaveFlag(int value, String description) {
            this.value = value;
            this.description = description;
        }
        
        public int getValue() {
            return value;
        }
        
        public String getDescription() {
            return description;
        }
        
        public static SaveFlag fromValue(int value) {
            for (SaveFlag flag : values()) {
                if (flag.value == value) {
                    return flag;
                }
            }
            throw new IllegalArgumentException("未知的保存标志: " + value);
        }
    }
    
    /**
     * 音频采样率枚举
     */
    public enum AudioSampleRate {
        RATE_8K(0, "8K"),
        RATE_11K(1, "11K"),
        RATE_23K(2, "23K"),
        RATE_32K(3, "32K");
        
        private final int value;
        private final String description;
        
        AudioSampleRate(int value, String description) {
            this.value = value;
            this.description = description;
        }
        
        public int getValue() {
            return value;
        }
        
        public String getDescription() {
            return description;
        }
        
        public static AudioSampleRate fromValue(int value) {
            for (AudioSampleRate rate : values()) {
                if (rate.value == value) {
                    return rate;
                }
            }
            throw new IllegalArgumentException("未知的音频采样率: " + value);
        }
    }
    
    // 消息体字段
    private RecordingCommand recordingCommand;  // 录音命令 (BYTE)
    private int recordingTime;                  // 录音时间 (WORD) 单位为秒，0表示一直录音
    private SaveFlag saveFlag;                  // 保存标志 (BYTE)
    private AudioSampleRate audioSampleRate;   // 音频采样率 (BYTE)
    
    /**
     * 默认构造函数
     */
    public T8804RecordingStartCommand() {
    }
    
    /**
     * 构造函数
     */
    public T8804RecordingStartCommand(RecordingCommand recordingCommand, int recordingTime, 
                                      SaveFlag saveFlag, AudioSampleRate audioSampleRate) {
        this.recordingCommand = recordingCommand;
        this.recordingTime = recordingTime;
        this.saveFlag = saveFlag;
        this.audioSampleRate = audioSampleRate;
    }
    
    /**
     * 编码消息体
     */
    @Override
    public Buffer encodeBody() {
        Buffer buffer = Buffer.buffer();
        
        // 录音命令 (BYTE)
        buffer.appendByte((byte) recordingCommand.getValue());
        
        // 录音时间 (WORD) - 大端序
        buffer.appendUnsignedShort(recordingTime);
        
        // 保存标志 (BYTE)
        buffer.appendByte((byte) saveFlag.getValue());
        
        // 音频采样率 (BYTE)
        buffer.appendByte((byte) audioSampleRate.getValue());
        
        return buffer;
    }
    
    /**
     * 解码消息体
     */
    @Override
    public void decodeBody(Buffer body) {
        T8804RecordingStartCommand decoded = decode(body);
        this.recordingCommand = decoded.recordingCommand;
        this.recordingTime = decoded.recordingTime;
        this.saveFlag = decoded.saveFlag;
        this.audioSampleRate = decoded.audioSampleRate;
    }
    
    /**
     * 静态解码方法
     */
    public static T8804RecordingStartCommand decode(Buffer buffer) {
        if (buffer.length() < 5) {
            throw new IllegalArgumentException("消息体长度不足，期望至少5字节，实际: " + buffer.length());
        }
        
        T8804RecordingStartCommand message = new T8804RecordingStartCommand();
        int offset = 0;
        
        // 录音命令 (BYTE)
        message.recordingCommand = RecordingCommand.fromValue(buffer.getByte(offset++) & 0xFF);
        
        // 录音时间 (WORD) - 大端序
        message.recordingTime = buffer.getUnsignedShort(offset);
        offset += 2;
        
        // 保存标志 (BYTE)
        message.saveFlag = SaveFlag.fromValue(buffer.getByte(offset++) & 0xFF);
        
        // 音频采样率 (BYTE)
        message.audioSampleRate = AudioSampleRate.fromValue(buffer.getByte(offset) & 0xFF);
        
        return message;
    }
    
    /**
     * 获取消息ID
     */
    @Override
    public int getMessageId() {
        return MessageTypes.Platform.RECORDING_START_COMMAND;
    }
    
    /**
     * 编码消息体（兼容方法）
     */
    public Buffer encode() {
        return encodeBody();
    }
    
    // Getters and Setters
    public RecordingCommand getRecordingCommand() {
        return recordingCommand;
    }
    
    public void setRecordingCommand(RecordingCommand recordingCommand) {
        this.recordingCommand = recordingCommand;
    }
    
    public int getRecordingTime() {
        return recordingTime;
    }
    
    public void setRecordingTime(int recordingTime) {
        this.recordingTime = recordingTime;
    }
    
    public SaveFlag getSaveFlag() {
        return saveFlag;
    }
    
    public void setSaveFlag(SaveFlag saveFlag) {
        this.saveFlag = saveFlag;
    }
    
    public AudioSampleRate getAudioSampleRate() {
        return audioSampleRate;
    }
    
    public void setAudioSampleRate(AudioSampleRate audioSampleRate) {
        this.audioSampleRate = audioSampleRate;
    }
    
    @Override
    public String toString() {
        return "T8804RecordingStartCommand{" +
                "recordingCommand=" + recordingCommand +
                ", recordingTime=" + recordingTime +
                ", saveFlag=" + saveFlag +
                ", audioSampleRate=" + audioSampleRate +
                '}';
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        T8804RecordingStartCommand that = (T8804RecordingStartCommand) o;
        return recordingTime == that.recordingTime &&
                recordingCommand == that.recordingCommand &&
                saveFlag == that.saveFlag &&
                audioSampleRate == that.audioSampleRate;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(recordingCommand, recordingTime, saveFlag, audioSampleRate);
    }
}