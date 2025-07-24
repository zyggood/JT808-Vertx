package com.jt808.protocol.message;

import com.jt808.protocol.constants.MessageTypes;
import io.vertx.core.buffer.Buffer;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/**
 * T8802 存储多媒体数据检索消息 (0x8802)
 * 
 * 消息体数据格式：
 * 起始字节 字段 数据类型 描述及要求
 * 0 多媒体类型 BYTE 0：图像；1：音频；2：视频
 * 1 通道ID BYTE 0表示检索该媒体类型的所有通道
 * 2 事件项编码 BYTE 0：平台下发指令；1：定时动作；2：抢劫报警触发；3：碰撞侧翻报警触发；其他保留
 * 3 起始时间 BCD[6] YY-MM-DD-hh-mm-ss
 * 9 结束时间 BCD[6] YY-MM-DD-hh-mm-ss
 * 
 * @author JT808 Protocol
 * @version 1.0
 */
public class T8802StoredMultimediaDataRetrieval extends JT808Message {

    /**
     * 多媒体类型 (BYTE)
     * 0：图像；1：音频；2：视频
     */
    private int multimediaType;

    /**
     * 通道ID (BYTE)
     * 0表示检索该媒体类型的所有通道
     */
    private int channelId;

    /**
     * 事件项编码 (BYTE)
     * 0：平台下发指令；1：定时动作；2：抢劫报警触发；3：碰撞侧翻报警触发；其他保留
     */
    private int eventCode;

    /**
     * 起始时间 (BCD[6])
     * YY-MM-DD-hh-mm-ss
     */
    private String startTime;

    /**
     * 结束时间 (BCD[6])
     * YY-MM-DD-hh-mm-ss
     */
    private String endTime;

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
     * 默认构造函数
     */
    public T8802StoredMultimediaDataRetrieval() {
        super();
        this.startTime = "00-00-00-00-00-00";
        this.endTime = "00-00-00-00-00-00";
    }

    /**
     * 带消息头的构造函数
     *
     * @param header 消息头
     */
    public T8802StoredMultimediaDataRetrieval(JT808Header header) {
        super(header);
        this.startTime = "00-00-00-00-00-00";
        this.endTime = "00-00-00-00-00-00";
    }

    @Override
    public int getMessageId() {
        return MessageTypes.Platform.STORED_MULTIMEDIA_DATA_RETRIEVAL;
    }

    /**
     * 创建检索所有类型多媒体数据的消息
     *
     * @param multimediaType 多媒体类型
     * @param channelId 通道ID（0表示所有通道）
     * @param eventCode 事件项编码
     * @return 检索消息实例
     */
    public static T8802StoredMultimediaDataRetrieval createRetrievalMessage(
            MultimediaType multimediaType, int channelId, EventCode eventCode) {
        T8802StoredMultimediaDataRetrieval message = new T8802StoredMultimediaDataRetrieval();
        message.setMultimediaType(multimediaType.getValue());
        message.setChannelId(channelId);
        message.setEventCode(eventCode.getValue());
        return message;
    }

    /**
     * 创建带时间范围的检索消息
     *
     * @param multimediaType 多媒体类型
     * @param channelId 通道ID（0表示所有通道）
     * @param eventCode 事件项编码
     * @param startTime 起始时间（YY-MM-DD-hh-mm-ss格式）
     * @param endTime 结束时间（YY-MM-DD-hh-mm-ss格式）
     * @return 检索消息实例
     */
    public static T8802StoredMultimediaDataRetrieval createRetrievalMessage(
            MultimediaType multimediaType, int channelId, EventCode eventCode,
            String startTime, String endTime) {
        T8802StoredMultimediaDataRetrieval message = new T8802StoredMultimediaDataRetrieval();
        message.setMultimediaType(multimediaType.getValue());
        message.setChannelId(channelId);
        message.setEventCode(eventCode.getValue());
        message.setStartTime(startTime);
        message.setEndTime(endTime);
        return message;
    }

    /**
     * 创建带LocalDateTime时间范围的检索消息
     *
     * @param multimediaType 多媒体类型
     * @param channelId 通道ID（0表示所有通道）
     * @param eventCode 事件项编码
     * @param startDateTime 起始时间
     * @param endDateTime 结束时间
     * @return 检索消息实例
     */
    public static T8802StoredMultimediaDataRetrieval createRetrievalMessage(
            MultimediaType multimediaType, int channelId, EventCode eventCode,
            LocalDateTime startDateTime, LocalDateTime endDateTime) {
        T8802StoredMultimediaDataRetrieval message = new T8802StoredMultimediaDataRetrieval();
        message.setMultimediaType(multimediaType.getValue());
        message.setChannelId(channelId);
        message.setEventCode(eventCode.getValue());
        message.setStartTime(formatDateTime(startDateTime));
        message.setEndTime(formatDateTime(endDateTime));
        return message;
    }

    @Override
    public Buffer encodeBody() {
        Buffer buffer = Buffer.buffer();
        
        // 多媒体类型 (1字节)
        buffer.appendUnsignedByte((short) multimediaType);
        
        // 通道ID (1字节)
        buffer.appendUnsignedByte((short) channelId);
        
        // 事件项编码 (1字节)
        buffer.appendUnsignedByte((short) eventCode);
        
        // 起始时间 (BCD[6])
        buffer.appendBuffer(encodeBcdTime(startTime));
        
        // 结束时间 (BCD[6])
        buffer.appendBuffer(encodeBcdTime(endTime));
        
        return buffer;
    }

    @Override
    public void decodeBody(Buffer buffer) {
        if (buffer.length() < 15) {
            throw new IllegalArgumentException("消息体长度不足，期望至少15字节，实际: " + buffer.length());
        }
        
        int index = 0;
        
        // 多媒体类型 (1字节)
        this.multimediaType = buffer.getUnsignedByte(index++);
        
        // 通道ID (1字节)
        this.channelId = buffer.getUnsignedByte(index++);
        
        // 事件项编码 (1字节)
        this.eventCode = buffer.getUnsignedByte(index++);
        
        // 起始时间 (BCD[6])
        this.startTime = decodeBcdTime(buffer.getBuffer(index, index + 6));
        index += 6;
        
        // 结束时间 (BCD[6])
        this.endTime = decodeBcdTime(buffer.getBuffer(index, index + 6));
    }

    /**
     * 编码BCD时间
     *
     * @param timeStr 时间字符串（YY-MM-DD-hh-mm-ss格式）
     * @return BCD编码的时间数据
     */
    private Buffer encodeBcdTime(String timeStr) {
        Buffer buffer = Buffer.buffer();
        String[] parts = timeStr.split("-");
        
        if (parts.length != 6) {
            throw new IllegalArgumentException("时间格式错误，期望YY-MM-DD-hh-mm-ss格式: " + timeStr);
        }
        
        for (String part : parts) {
            int value = Integer.parseInt(part);
            int bcd = ((value / 10) << 4) | (value % 10);
            buffer.appendUnsignedByte((short) bcd);
        }
        
        return buffer;
    }

    /**
     * 解码BCD时间
     *
     * @param buffer BCD编码的时间数据
     * @return 时间字符串（YY-MM-DD-hh-mm-ss格式）
     */
    private String decodeBcdTime(Buffer buffer) {
        StringBuilder sb = new StringBuilder();
        
        for (int i = 0; i < buffer.length(); i++) {
            if (i > 0) {
                sb.append("-");
            }
            int bcd = buffer.getUnsignedByte(i);
            int high = (bcd >> 4) & 0x0F;
            int low = bcd & 0x0F;
            sb.append(String.format("%d%d", high, low));
        }
        
        return sb.toString();
    }

    /**
     * 格式化LocalDateTime为BCD时间字符串
     *
     * @param dateTime 时间对象
     * @return BCD时间字符串
     */
    private static String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "00-00-00-00-00-00";
        }
        return dateTime.format(DateTimeFormatter.ofPattern("yy-MM-dd-HH-mm-ss"));
    }

    /**
     * 解析BCD时间字符串为LocalDateTime
     *
     * @param timeStr BCD时间字符串
     * @return LocalDateTime对象
     */
    public LocalDateTime parseDateTime(String timeStr) {
        if ("00-00-00-00-00-00".equals(timeStr)) {
            return null;
        }
        return LocalDateTime.parse("20" + timeStr, DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss"));
    }

    /**
     * 检查是否为全时间范围检索（不按时间范围）
     *
     * @return 如果起始时间和结束时间都为00-00-00-00-00-00则返回true
     */
    public boolean isFullTimeRange() {
        return "00-00-00-00-00-00".equals(startTime) && "00-00-00-00-00-00".equals(endTime);
    }

    /**
     * 检查是否为所有通道检索
     *
     * @return 如果通道ID为0则返回true
     */
    public boolean isAllChannels() {
        return channelId == 0;
    }

    /**
     * 获取多媒体类型枚举
     *
     * @return 多媒体类型枚举
     */
    public MultimediaType getMultimediaTypeEnum() {
        return MultimediaType.fromValue(multimediaType);
    }

    /**
     * 获取事件项编码枚举
     *
     * @return 事件项编码枚举
     */
    public EventCode getEventCodeEnum() {
        return EventCode.fromValue(eventCode);
    }

    /**
     * 获取多媒体类型描述
     *
     * @return 多媒体类型描述
     */
    public String getMultimediaTypeDescription() {
        try {
            return getMultimediaTypeEnum().getDescription();
        } catch (IllegalArgumentException e) {
            return "未知类型(" + multimediaType + ")";
        }
    }

    /**
     * 获取事件项编码描述
     *
     * @return 事件项编码描述
     */
    public String getEventCodeDescription() {
        try {
            return getEventCodeEnum().getDescription();
        } catch (IllegalArgumentException e) {
            return "未知事件(" + eventCode + ")";
        }
    }

    /**
     * 获取消息描述
     *
     * @return 消息描述字符串
     */
    public String getMessageDescription() {
        StringBuilder sb = new StringBuilder();
        sb.append("存储多媒体数据检索: ");
        sb.append("类型=").append(getMultimediaTypeDescription());
        sb.append(", 通道=").append(isAllChannels() ? "所有通道" : "通道" + channelId);
        sb.append(", 事件=").append(getEventCodeDescription());
        if (isFullTimeRange()) {
            sb.append(", 时间范围=全部");
        } else {
            sb.append(", 时间范围=").append(startTime).append("~").append(endTime);
        }
        return sb.toString();
    }

    // Getter和Setter方法
    public int getMultimediaType() {
        return multimediaType;
    }

    public void setMultimediaType(int multimediaType) {
        this.multimediaType = multimediaType;
    }

    public int getChannelId() {
        return channelId;
    }

    public void setChannelId(int channelId) {
        this.channelId = channelId;
    }

    public int getEventCode() {
        return eventCode;
    }

    public void setEventCode(int eventCode) {
        this.eventCode = eventCode;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    @Override
    public String toString() {
        return "T8802StoredMultimediaDataRetrieval{" +
                "multimediaType=" + multimediaType +
                ", channelId=" + channelId +
                ", eventCode=" + eventCode +
                ", startTime='" + startTime + '\'' +
                ", endTime='" + endTime + '\'' +
                ", description='" + getMessageDescription() + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        T8802StoredMultimediaDataRetrieval that = (T8802StoredMultimediaDataRetrieval) o;
        return multimediaType == that.multimediaType &&
                channelId == that.channelId &&
                eventCode == that.eventCode &&
                Objects.equals(startTime, that.startTime) &&
                Objects.equals(endTime, that.endTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(multimediaType, channelId, eventCode, startTime, endTime);
    }
}