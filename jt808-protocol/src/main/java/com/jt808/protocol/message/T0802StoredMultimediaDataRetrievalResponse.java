package com.jt808.protocol.message;

import com.jt808.protocol.constants.MessageTypes;
import io.vertx.core.buffer.Buffer;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 存储多媒体数据检索应答 (0x0802)
 * 终端应答平台的存储多媒体数据检索请求
 */
public class T0802StoredMultimediaDataRetrievalResponse extends JT808Message {
    
    /**
     * 应答流水号 - 对应的多媒体数据检索消息的流水号
     */
    private int responseSerialNumber;
    
    /**
     * 多媒体数据总项数 - 满足检索条件的多媒体数据总项数
     */
    private int totalCount;
    
    /**
     * 检索项列表
     */
    private List<MultimediaRetrievalItem> retrievalItems;
    
    public T0802StoredMultimediaDataRetrievalResponse() {
        this.retrievalItems = new ArrayList<>();
    }
    
    public T0802StoredMultimediaDataRetrievalResponse(int responseSerialNumber, int totalCount) {
        this.responseSerialNumber = responseSerialNumber;
        this.totalCount = totalCount;
        this.retrievalItems = new ArrayList<>();
    }
    
    public T0802StoredMultimediaDataRetrievalResponse(int responseSerialNumber, int totalCount, List<MultimediaRetrievalItem> retrievalItems) {
        this.responseSerialNumber = responseSerialNumber;
        this.totalCount = totalCount;
        this.retrievalItems = retrievalItems != null ? new ArrayList<>(retrievalItems) : new ArrayList<>();
    }
    
    /**
     * 编码消息体
     */
    @Override
    public Buffer encodeBody() {
        Buffer buffer = Buffer.buffer();
        
        // 应答流水号 (WORD)
        buffer.appendUnsignedShort(responseSerialNumber);
        
        // 多媒体数据总项数 (WORD)
        buffer.appendUnsignedShort(totalCount);
        
        // 检索项
        if (retrievalItems != null) {
            for (MultimediaRetrievalItem item : retrievalItems) {
                buffer.appendBuffer(item.encode());
            }
        }
        
        return buffer;
    }
    
    /**
     * 解码消息体
     */
    public static T0802StoredMultimediaDataRetrievalResponse decode(Buffer buffer) {
        if (buffer.length() < 4) {
            throw new IllegalArgumentException("Buffer too short for T0802 message");
        }
        
        int offset = 0;
        
        // 应答流水号 (WORD)
        int responseSerialNumber = buffer.getUnsignedShort(offset);
        offset += 2;
        
        // 多媒体数据总项数 (WORD)
        int totalCount = buffer.getUnsignedShort(offset);
        offset += 2;
        
        T0802StoredMultimediaDataRetrievalResponse response = new T0802StoredMultimediaDataRetrievalResponse(responseSerialNumber, totalCount);
        
        // 解码检索项
        while (offset < buffer.length()) {
            if (offset + 35 > buffer.length()) { // 最小检索项长度：4+1+1+1+28=35字节
                break;
            }
            
            MultimediaRetrievalItem item = MultimediaRetrievalItem.decode(buffer.getBuffer(offset, buffer.length()));
            response.addRetrievalItem(item);
            offset += 35; // 每个检索项固定35字节
        }
        
        return response;
    }
    
    /**
     * 添加检索项
     */
    public void addRetrievalItem(MultimediaRetrievalItem item) {
        if (item != null) {
            this.retrievalItems.add(item);
        }
    }
    
    /**
     * 获取消息ID
     */
    @Override
    public int getMessageId() {
        return MessageTypes.Terminal.STORED_MULTIMEDIA_DATA_RETRIEVAL_RESPONSE;
    }
    
    /**
     * 解码消息体
     */
    @Override
    public void decodeBody(Buffer body) {
        T0802StoredMultimediaDataRetrievalResponse decoded = decode(body);
        this.responseSerialNumber = decoded.responseSerialNumber;
        this.totalCount = decoded.totalCount;
        this.retrievalItems = decoded.retrievalItems;
    }
    
    /**
     * 编码消息体（兼容方法）
     */
    public Buffer encode() {
        return encodeBody();
    }
    
    // Getters and Setters
    public int getResponseSerialNumber() {
        return responseSerialNumber;
    }
    
    public void setResponseSerialNumber(int responseSerialNumber) {
        this.responseSerialNumber = responseSerialNumber;
    }
    
    public int getTotalCount() {
        return totalCount;
    }
    
    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }
    
    public List<MultimediaRetrievalItem> getRetrievalItems() {
        return new ArrayList<>(retrievalItems);
    }
    
    public void setRetrievalItems(List<MultimediaRetrievalItem> retrievalItems) {
        this.retrievalItems = retrievalItems != null ? new ArrayList<>(retrievalItems) : new ArrayList<>();
    }
    
    @Override
    public String toString() {
        return "T0802StoredMultimediaDataRetrievalResponse{" +
                "responseSerialNumber=" + responseSerialNumber +
                ", totalCount=" + totalCount +
                ", retrievalItems=" + retrievalItems +
                '}';
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        T0802StoredMultimediaDataRetrievalResponse that = (T0802StoredMultimediaDataRetrievalResponse) o;
        return responseSerialNumber == that.responseSerialNumber &&
                totalCount == that.totalCount &&
                Objects.equals(retrievalItems, that.retrievalItems);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(responseSerialNumber, totalCount, retrievalItems);
    }
    
    /**
     * 多媒体检索项数据格式
     */
    public static class MultimediaRetrievalItem {
        
        /**
         * 多媒体ID (DWORD) >0
         */
        private long multimediaId;
        
        /**
         * 多媒体类型 (BYTE)
         */
        private MultimediaType multimediaType;
        
        /**
         * 通道ID (BYTE)
         */
        private int channelId;
        
        /**
         * 事件项编码 (BYTE)
         */
        private EventCode eventCode;
        
        /**
         * 位置信息汇报(0x0200)消息体 (BYTE[28])
         * 表示拍摄或录制的起始时刻的位置基本信息数据
         */
        private byte[] locationInfo;
        
        public MultimediaRetrievalItem() {
            this.locationInfo = new byte[28];
        }
        
        public MultimediaRetrievalItem(long multimediaId, MultimediaType multimediaType, int channelId, EventCode eventCode, byte[] locationInfo) {
            this.multimediaId = multimediaId;
            this.multimediaType = multimediaType;
            this.channelId = channelId;
            this.eventCode = eventCode;
            this.locationInfo = locationInfo != null && locationInfo.length == 28 ? locationInfo.clone() : new byte[28];
        }
        
        /**
         * 编码检索项
         */
        public Buffer encode() {
            Buffer buffer = Buffer.buffer();
            
            // 多媒体ID (DWORD)
            buffer.appendUnsignedInt(multimediaId);
            
            // 多媒体类型 (BYTE)
            buffer.appendByte((byte) (multimediaType != null ? multimediaType.getValue() : 0));
            
            // 通道ID (BYTE)
            buffer.appendByte((byte) channelId);
            
            // 事件项编码 (BYTE)
            buffer.appendByte((byte) (eventCode != null ? eventCode.getValue() : 0));
            
            // 位置信息汇报消息体 (BYTE[28])
            buffer.appendBytes(locationInfo);
            
            return buffer;
        }
        
        /**
         * 解码检索项
         */
        public static MultimediaRetrievalItem decode(Buffer buffer) {
            if (buffer.length() < 35) {
                throw new IllegalArgumentException("Buffer too short for MultimediaRetrievalItem");
            }
            
            int offset = 0;
            
            // 多媒体ID (DWORD)
            long multimediaId = buffer.getUnsignedInt(offset);
            offset += 4;
            
            // 多媒体类型 (BYTE)
            MultimediaType multimediaType = MultimediaType.fromValue(buffer.getUnsignedByte(offset));
            offset += 1;
            
            // 通道ID (BYTE)
            int channelId = buffer.getUnsignedByte(offset);
            offset += 1;
            
            // 事件项编码 (BYTE)
            EventCode eventCode = EventCode.fromValue(buffer.getUnsignedByte(offset));
            offset += 1;
            
            // 位置信息汇报消息体 (BYTE[28])
            byte[] locationInfo = buffer.getBytes(offset, offset + 28);
            
            return new MultimediaRetrievalItem(multimediaId, multimediaType, channelId, eventCode, locationInfo);
        }
        
        // Getters and Setters
        public long getMultimediaId() {
            return multimediaId;
        }
        
        public void setMultimediaId(long multimediaId) {
            this.multimediaId = multimediaId;
        }
        
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
        
        public byte[] getLocationInfo() {
            return locationInfo != null ? locationInfo.clone() : new byte[28];
        }
        
        public void setLocationInfo(byte[] locationInfo) {
            this.locationInfo = locationInfo != null && locationInfo.length == 28 ? locationInfo.clone() : new byte[28];
        }
        
        @Override
        public String toString() {
            return "MultimediaRetrievalItem{" +
                    "multimediaId=" + multimediaId +
                    ", multimediaType=" + multimediaType +
                    ", channelId=" + channelId +
                    ", eventCode=" + eventCode +
                    ", locationInfoLength=" + (locationInfo != null ? locationInfo.length : 0) +
                    '}';
        }
        
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            MultimediaRetrievalItem that = (MultimediaRetrievalItem) o;
            return multimediaId == that.multimediaId &&
                    channelId == that.channelId &&
                    multimediaType == that.multimediaType &&
                    eventCode == that.eventCode &&
                    java.util.Arrays.equals(locationInfo, that.locationInfo);
        }
        
        @Override
        public int hashCode() {
            int result = Objects.hash(multimediaId, multimediaType, channelId, eventCode);
            result = 31 * result + java.util.Arrays.hashCode(locationInfo);
            return result;
        }
    }
    
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
            return IMAGE; // 默认返回图像类型
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
            return PLATFORM_COMMAND; // 默认返回平台下发指令
        }
    }
}