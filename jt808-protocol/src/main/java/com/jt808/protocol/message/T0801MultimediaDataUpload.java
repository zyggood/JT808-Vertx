package com.jt808.protocol.message;

import com.jt808.protocol.constants.MessageTypes;
import io.vertx.core.buffer.Buffer;

/**
 * T0801 多媒体数据上传消息
 * 消息ID: 0x0801
 * 
 * 消息体数据格式:
 * - 多媒体ID (DWORD, 4字节): >0
 * - 多媒体类型 (BYTE, 1字节): 0-图像, 1-音频, 2-视频
 * - 多媒体格式编码 (BYTE, 1字节): 0-JPEG, 1-TIF, 2-MP3, 3-WAV, 4-WMV
 * - 事件项编码 (BYTE, 1字节): 0-平台下发指令, 1-定时动作, 2-抢劫报警触发, 3-碰撞侧翻报警触发
 * - 通道ID (BYTE, 1字节)
 * - 位置信息汇报消息体 (BYTE[28], 28字节): T0200消息体数据
 * - 多媒体数据包 (变长): 实际的多媒体数据
 */
public class T0801MultimediaDataUpload extends JT808Message {

    public static final int MESSAGE_ID = MessageTypes.Terminal.MULTIMEDIA_DATA_UPLOAD;

    /**
     * 多媒体ID (DWORD, 4字节)
     * 值域: >0
     */
    private long multimediaId;

    /**
     * 多媒体类型 (BYTE, 1字节)
     * 0: 图像
     * 1: 音频
     * 2: 视频
     */
    private int multimediaType;

    /**
     * 多媒体格式编码 (BYTE, 1字节)
     * 0: JPEG
     * 1: TIF
     * 2: MP3
     * 3: WAV
     * 4: WMV
     * 其他: 保留
     */
    private int formatCode;

    /**
     * 事件项编码 (BYTE, 1字节)
     * 0: 平台下发指令
     * 1: 定时动作
     * 2: 抢劫报警触发
     * 3: 碰撞侧翻报警触发
     * 其他: 保留
     */
    private int eventCode;

    /**
     * 通道ID (BYTE, 1字节)
     */
    private int channelId;

    /**
     * 位置信息汇报消息体 (T0200消息体数据, 28字节)
     */
    private T0200LocationReport locationInfo;

    /**
     * 多媒体数据包 (变长)
     */
    private Buffer multimediaData;

    /**
     * 默认构造函数
     */
    public T0801MultimediaDataUpload() {
        this.locationInfo = new T0200LocationReport();
    }

    /**
     * 带参数构造函数
     */
    public T0801MultimediaDataUpload(long multimediaId, int multimediaType, int formatCode, 
                                   int eventCode, int channelId, T0200LocationReport locationInfo, 
                                   Buffer multimediaData) {
        this.multimediaId = multimediaId;
        this.multimediaType = multimediaType;
        this.formatCode = formatCode;
        this.eventCode = eventCode;
        this.channelId = channelId;
        this.locationInfo = locationInfo != null ? locationInfo : new T0200LocationReport();
        this.multimediaData = multimediaData;
    }

    @Override
    public int getMessageId() {
        return MESSAGE_ID;
    }

    @Override
    public Buffer encodeBody() {
        Buffer buffer = Buffer.buffer();
        
        // 多媒体ID (DWORD, 4字节)
        buffer.appendUnsignedInt(multimediaId);
        
        // 多媒体类型 (BYTE, 1字节)
        buffer.appendUnsignedByte((short) multimediaType);
        
        // 多媒体格式编码 (BYTE, 1字节)
        buffer.appendUnsignedByte((short) formatCode);
        
        // 事件项编码 (BYTE, 1字节)
        buffer.appendUnsignedByte((short) eventCode);
        
        // 通道ID (BYTE, 1字节)
        buffer.appendUnsignedByte((short) channelId);
        
        // 位置信息汇报消息体 (28字节)
        if (locationInfo != null) {
            Buffer locationBuffer = locationInfo.encodeBody();
            // 确保位置信息正好是28字节
            if (locationBuffer.length() >= 28) {
                buffer.appendBuffer(locationBuffer, 0, 28);
            } else {
                buffer.appendBuffer(locationBuffer);
                // 如果不足28字节，用0填充
                for (int i = locationBuffer.length(); i < 28; i++) {
                    buffer.appendByte((byte) 0);
                }
            }
        } else {
            // 如果位置信息为空，填充28个0字节
            for (int i = 0; i < 28; i++) {
                buffer.appendByte((byte) 0);
            }
        }
        
        // 多媒体数据包 (变长)
        if (multimediaData != null && multimediaData.length() > 0) {
            buffer.appendBuffer(multimediaData);
        }
        
        return buffer;
    }

    @Override
    public void decodeBody(Buffer body) {
        if (body == null || body.length() < 36) { // 最小长度: 4+1+1+1+1+28 = 36字节
            throw new IllegalArgumentException("消息体长度不足，至少需要36字节");
        }
        
        int offset = 0;
        
        // 多媒体ID (DWORD, 4字节)
        this.multimediaId = body.getUnsignedInt(offset);
        offset += 4;
        
        // 多媒体类型 (BYTE, 1字节)
        this.multimediaType = body.getUnsignedByte(offset);
        offset += 1;
        
        // 多媒体格式编码 (BYTE, 1字节)
        this.formatCode = body.getUnsignedByte(offset);
        offset += 1;
        
        // 事件项编码 (BYTE, 1字节)
        this.eventCode = body.getUnsignedByte(offset);
        offset += 1;
        
        // 通道ID (BYTE, 1字节)
        this.channelId = body.getUnsignedByte(offset);
        offset += 1;
        
        // 位置信息汇报消息体 (28字节)
        Buffer locationBuffer = body.getBuffer(offset, offset + 28);
        if (this.locationInfo == null) {
            this.locationInfo = new T0200LocationReport();
        }
        this.locationInfo.decodeBody(locationBuffer);
        offset += 28;
        
        // 多媒体数据包 (剩余字节)
        if (offset < body.length()) {
            this.multimediaData = body.getBuffer(offset, body.length());
        } else {
            this.multimediaData = Buffer.buffer();
        }
    }

    // Getter和Setter方法
    
    public long getMultimediaId() {
        return multimediaId;
    }

    public void setMultimediaId(long multimediaId) {
        this.multimediaId = multimediaId;
    }

    public int getMultimediaType() {
        return multimediaType;
    }

    public void setMultimediaType(int multimediaType) {
        this.multimediaType = multimediaType;
    }

    public int getFormatCode() {
        return formatCode;
    }

    public void setFormatCode(int formatCode) {
        this.formatCode = formatCode;
    }

    public int getEventCode() {
        return eventCode;
    }

    public void setEventCode(int eventCode) {
        this.eventCode = eventCode;
    }

    public int getChannelId() {
        return channelId;
    }

    public void setChannelId(int channelId) {
        this.channelId = channelId;
    }

    public T0200LocationReport getLocationInfo() {
        return locationInfo;
    }

    public void setLocationInfo(T0200LocationReport locationInfo) {
        this.locationInfo = locationInfo;
    }

    public Buffer getMultimediaData() {
        return multimediaData;
    }

    public void setMultimediaData(Buffer multimediaData) {
        this.multimediaData = multimediaData;
    }

    /**
     * 获取多媒体类型描述
     */
    public String getMultimediaTypeDescription() {
        switch (multimediaType) {
            case 0: return "图像";
            case 1: return "音频";
            case 2: return "视频";
            default: return "未知类型(" + multimediaType + ")";
        }
    }

    /**
     * 获取格式编码描述
     */
    public String getFormatCodeDescription() {
        switch (formatCode) {
            case 0: return "JPEG";
            case 1: return "TIF";
            case 2: return "MP3";
            case 3: return "WAV";
            case 4: return "WMV";
            default: return "保留格式(" + formatCode + ")";
        }
    }

    /**
     * 获取事件项编码描述
     */
    public String getEventCodeDescription() {
        switch (eventCode) {
            case 0: return "平台下发指令";
            case 1: return "定时动作";
            case 2: return "抢劫报警触发";
            case 3: return "碰撞侧翻报警触发";
            default: return "保留事件(" + eventCode + ")";
        }
    }

    /**
     * 获取多媒体数据大小
     */
    public int getMultimediaDataSize() {
        return multimediaData != null ? multimediaData.length() : 0;
    }

    @Override
    public String toString() {
        return "T0801MultimediaDataUpload{" +
                "multimediaId=" + multimediaId +
                ", multimediaType=" + multimediaType + "(" + getMultimediaTypeDescription() + ")" +
                ", formatCode=" + formatCode + "(" + getFormatCodeDescription() + ")" +
                ", eventCode=" + eventCode + "(" + getEventCodeDescription() + ")" +
                ", channelId=" + channelId +
                ", locationInfo=" + locationInfo +
                ", multimediaDataSize=" + getMultimediaDataSize() +
                "}";
    }
}