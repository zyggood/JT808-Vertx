package com.jt808.protocol.message;

import com.jt808.protocol.constants.MessageTypes;
import io.vertx.core.buffer.Buffer;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/**
 * 存储多媒体数据上传命令 (0x8803)
 * 平台向终端下发存储多媒体数据上传命令
 */
public class T8803StoredMultimediaDataUploadCommand extends JT808Message {
    
    /**
     * 多媒体类型枚举
     */
    public enum MultimediaType {
        IMAGE(0, "图像"),
        AUDIO(1, "音频"),
        VIDEO(2, "视频");
        
        private final int value;
        private final String description;
        
        MultimediaType(int value, String description) {
            this.value = value;
            this.description = description;
        }
        
        public int getValue() {
            return value;
        }
        
        public String getDescription() {
            return description;
        }
        
        public static MultimediaType fromValue(int value) {
            for (MultimediaType type : values()) {
                if (type.value == value) {
                    return type;
                }
            }
            throw new IllegalArgumentException("未知的多媒体类型: " + value);
        }
    }
    
    /**
     * 事件项编码枚举
     */
    public enum EventCode {
        PLATFORM_COMMAND(0, "平台下发指令"),
        TIMED_ACTION(1, "定时动作"),
        ROBBERY_ALARM(2, "抢劫报警触发"),
        COLLISION_ROLLOVER_ALARM(3, "碰撞侧翻报警触发");
        
        private final int value;
        private final String description;
        
        EventCode(int value, String description) {
            this.value = value;
            this.description = description;
        }
        
        public int getValue() {
            return value;
        }
        
        public String getDescription() {
            return description;
        }
        
        public static EventCode fromValue(int value) {
            for (EventCode code : values()) {
                if (code.value == value) {
                    return code;
                }
            }
            throw new IllegalArgumentException("未知的事件项编码: " + value);
        }
    }
    
    /**
     * 删除标志枚举
     */
    public enum DeleteFlag {
        KEEP(0, "保留"),
        DELETE(1, "删除");
        
        private final int value;
        private final String description;
        
        DeleteFlag(int value, String description) {
            this.value = value;
            this.description = description;
        }
        
        public int getValue() {
            return value;
        }
        
        public String getDescription() {
            return description;
        }
        
        public static DeleteFlag fromValue(int value) {
            for (DeleteFlag flag : values()) {
                if (flag.value == value) {
                    return flag;
                }
            }
            throw new IllegalArgumentException("未知的删除标志: " + value);
        }
    }
    
    // 消息体字段
    private MultimediaType multimediaType;  // 多媒体类型 (BYTE)
    private int channelId;                   // 通道ID (BYTE)
    private EventCode eventCode;             // 事件项编码 (BYTE)
    private LocalDateTime startTime;         // 起始时间 (BCD[6])
    private LocalDateTime endTime;           // 结束时间 (BCD[6])
    private DeleteFlag deleteFlag;           // 删除标志 (BYTE)
    
    /**
     * 默认构造函数
     */
    public T8803StoredMultimediaDataUploadCommand() {
    }
    
    /**
     * 构造函数
     */
    public T8803StoredMultimediaDataUploadCommand(MultimediaType multimediaType, int channelId, 
                                                  EventCode eventCode, LocalDateTime startTime, 
                                                  LocalDateTime endTime, DeleteFlag deleteFlag) {
        this.multimediaType = multimediaType;
        this.channelId = channelId;
        this.eventCode = eventCode;
        this.startTime = startTime;
        this.endTime = endTime;
        this.deleteFlag = deleteFlag;
    }
    
    /**
     * 编码消息体
     */
    @Override
    public Buffer encodeBody() {
        Buffer buffer = Buffer.buffer();
        
        // 多媒体类型 (BYTE)
        buffer.appendByte((byte) multimediaType.getValue());
        
        // 通道ID (BYTE)
        buffer.appendByte((byte) channelId);
        
        // 事件项编码 (BYTE)
        buffer.appendByte((byte) eventCode.getValue());
        
        // 起始时间 (BCD[6])
        buffer.appendBuffer(encodeBcdTime(startTime));
        
        // 结束时间 (BCD[6])
        buffer.appendBuffer(encodeBcdTime(endTime));
        
        // 删除标志 (BYTE)
        buffer.appendByte((byte) deleteFlag.getValue());
        
        return buffer;
    }
    
    /**
     * 解码消息体
     */
    @Override
    public void decodeBody(Buffer body) {
        T8803StoredMultimediaDataUploadCommand decoded = decode(body);
        this.multimediaType = decoded.multimediaType;
        this.channelId = decoded.channelId;
        this.eventCode = decoded.eventCode;
        this.startTime = decoded.startTime;
        this.endTime = decoded.endTime;
        this.deleteFlag = decoded.deleteFlag;
    }
    
    /**
     * 静态解码方法
     */
    public static T8803StoredMultimediaDataUploadCommand decode(Buffer buffer) {
        if (buffer.length() < 16) {
            throw new IllegalArgumentException("消息体长度不足，期望至少16字节，实际: " + buffer.length());
        }
        
        T8803StoredMultimediaDataUploadCommand message = new T8803StoredMultimediaDataUploadCommand();
        int offset = 0;
        
        // 多媒体类型 (BYTE)
        message.multimediaType = MultimediaType.fromValue(buffer.getByte(offset++) & 0xFF);
        
        // 通道ID (BYTE)
        message.channelId = buffer.getByte(offset++) & 0xFF;
        
        // 事件项编码 (BYTE)
        message.eventCode = EventCode.fromValue(buffer.getByte(offset++) & 0xFF);
        
        // 起始时间 (BCD[6])
        message.startTime = decodeBcdTime(buffer.getBuffer(offset, offset + 6));
        offset += 6;
        
        // 结束时间 (BCD[6])
        message.endTime = decodeBcdTime(buffer.getBuffer(offset, offset + 6));
        offset += 6;
        
        // 删除标志 (BYTE)
        message.deleteFlag = DeleteFlag.fromValue(buffer.getByte(offset) & 0xFF);
        
        return message;
    }
    
    /**
     * 获取消息ID
     */
    @Override
    public int getMessageId() {
        return MessageTypes.Platform.STORED_MULTIMEDIA_DATA_UPLOAD_COMMAND;
    }
    
    /**
     * 编码消息体（兼容方法）
     */
    public Buffer encode() {
        return encodeBody();
    }
    
    /**
     * 编码BCD时间
     */
    private Buffer encodeBcdTime(LocalDateTime dateTime) {
        Buffer buffer = Buffer.buffer();
        
        int year = dateTime.getYear() % 100;  // 取年份后两位
        int month = dateTime.getMonthValue();
        int day = dateTime.getDayOfMonth();
        int hour = dateTime.getHour();
        int minute = dateTime.getMinute();
        int second = dateTime.getSecond();
        
        buffer.appendByte((byte) ((year / 10 << 4) | (year % 10)));
        buffer.appendByte((byte) ((month / 10 << 4) | (month % 10)));
        buffer.appendByte((byte) ((day / 10 << 4) | (day % 10)));
        buffer.appendByte((byte) ((hour / 10 << 4) | (hour % 10)));
        buffer.appendByte((byte) ((minute / 10 << 4) | (minute % 10)));
        buffer.appendByte((byte) ((second / 10 << 4) | (second % 10)));
        
        return buffer;
    }
    
    /**
     * 解码BCD时间
     */
    private static LocalDateTime decodeBcdTime(Buffer buffer) {
        if (buffer.length() != 6) {
            throw new IllegalArgumentException("BCD时间长度必须为6字节");
        }
        
        int year = bcdToByte(buffer.getByte(0)) + 2000;
        int month = bcdToByte(buffer.getByte(1));
        int day = bcdToByte(buffer.getByte(2));
        int hour = bcdToByte(buffer.getByte(3));
        int minute = bcdToByte(buffer.getByte(4));
        int second = bcdToByte(buffer.getByte(5));
        
        return LocalDateTime.of(year, month, day, hour, minute, second);
    }
    
    /**
     * BCD转字节
     */
    private static int bcdToByte(byte bcd) {
        return ((bcd >> 4) & 0x0F) * 10 + (bcd & 0x0F);
    }
    
    // Getters and Setters
    public MultimediaType getMultimediaType() {
        return multimediaType;
    }
    
    public void setMultimediaType(MultimediaType multimediaType) {
        this.multimediaType = multimediaType;
    }
    
    public int getChannelId() {
        return channelId;
    }
    
    public void setChannelId(int channelId) {
        this.channelId = channelId;
    }
    
    public EventCode getEventCode() {
        return eventCode;
    }
    
    public void setEventCode(EventCode eventCode) {
        this.eventCode = eventCode;
    }
    
    public LocalDateTime getStartTime() {
        return startTime;
    }
    
    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }
    
    public LocalDateTime getEndTime() {
        return endTime;
    }
    
    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }
    
    public DeleteFlag getDeleteFlag() {
        return deleteFlag;
    }
    
    public void setDeleteFlag(DeleteFlag deleteFlag) {
        this.deleteFlag = deleteFlag;
    }
    
    @Override
    public String toString() {
        return "T8803StoredMultimediaDataUploadCommand{" +
                "multimediaType=" + multimediaType +
                ", channelId=" + channelId +
                ", eventCode=" + eventCode +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", deleteFlag=" + deleteFlag +
                '}';
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        T8803StoredMultimediaDataUploadCommand that = (T8803StoredMultimediaDataUploadCommand) o;
        return channelId == that.channelId &&
                multimediaType == that.multimediaType &&
                eventCode == that.eventCode &&
                Objects.equals(startTime, that.startTime) &&
                Objects.equals(endTime, that.endTime) &&
                deleteFlag == that.deleteFlag;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(multimediaType, channelId, eventCode, startTime, endTime, deleteFlag);
    }
}