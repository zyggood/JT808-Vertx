package com.jt808.protocol.message;

import io.vertx.core.buffer.Buffer;

import java.util.ArrayList;
import java.util.List;

/**
 * 事件设置消息 (0x8301)
 * 平台设置终端事件的消息
 */
public class T8301EventSetting extends JT808Message {

    /**
     * 设置类型
     */
    private byte settingType;

    /**
     * 事件项列表
     */
    private List<EventItem> eventItems;

    public T8301EventSetting() {
        super();
        this.eventItems = new ArrayList<>();
    }

    public T8301EventSetting(JT808Header header) {
        super(header);
        this.eventItems = new ArrayList<>();
    }

    public T8301EventSetting(byte settingType) {
        super();
        this.settingType = settingType;
        this.eventItems = new ArrayList<>();
    }

    /**
     * 创建删除所有事件的消息
     */
    public static T8301EventSetting createDeleteAll() {
        return new T8301EventSetting(SettingType.DELETE_ALL);
    }

    /**
     * 创建更新事件的消息
     */
    public static T8301EventSetting createUpdate() {
        return new T8301EventSetting(SettingType.UPDATE);
    }

    /**
     * 创建追加事件的消息
     */
    public static T8301EventSetting createAppend() {
        return new T8301EventSetting(SettingType.APPEND);
    }

    /**
     * 创建修改事件的消息
     */
    public static T8301EventSetting createModify() {
        return new T8301EventSetting(SettingType.MODIFY);
    }

    /**
     * 创建删除特定事件的消息
     */
    public static T8301EventSetting createDeleteSpecific() {
        return new T8301EventSetting(SettingType.DELETE_SPECIFIC);
    }

    @Override
    public int getMessageId() {
        return 0x8301;
    }

    @Override
    public Buffer encodeBody() {
        Buffer buffer = Buffer.buffer();

        // 设置类型 (1字节)
        buffer.appendByte(settingType);

        // 如果是删除所有事件，则不需要后续字节
        if (settingType == SettingType.DELETE_ALL) {
            return buffer;
        }

        // 设置总数 (1字节)
        buffer.appendByte((byte) eventItems.size());

        // 事件项列表
        for (EventItem item : eventItems) {
            // 事件ID (1字节)
            buffer.appendByte(item.getEventId());

            // 如果是删除特定事件，则不需要事件内容
            if (settingType == SettingType.DELETE_SPECIFIC) {
                continue;
            }

            // 事件内容字节数组
            byte[] contentBytes = item.getContentBytes();

            // 事件内容长度 (1字节)
            buffer.appendByte((byte) contentBytes.length);

            // 事件内容
            buffer.appendBytes(contentBytes);
        }

        return buffer;
    }

    @Override
    public void decodeBody(Buffer body) {
        if (body == null || body.length() < 1) {
            throw new IllegalArgumentException("事件设置消息体不能为空");
        }

        int index = 0;
        eventItems.clear();

        // 设置类型 (1字节)
        settingType = body.getByte(index);
        index += 1;

        // 如果是删除所有事件，则没有后续字节
        if (settingType == SettingType.DELETE_ALL) {
            return;
        }

        if (index >= body.length()) {
            throw new IllegalArgumentException("消息体长度不足，缺少设置总数字段");
        }

        // 设置总数 (1字节)
        int eventCount = body.getUnsignedByte(index);
        index += 1;

        // 解析事件项列表
        for (int i = 0; i < eventCount; i++) {
            if (index >= body.length()) {
                throw new IllegalArgumentException("消息体长度不足，无法解析事件项 " + (i + 1));
            }

            // 事件ID (1字节)
            byte eventId = body.getByte(index);
            index += 1;

            // 如果是删除特定事件，则没有事件内容
            if (settingType == SettingType.DELETE_SPECIFIC) {
                EventItem item = new EventItem(eventId, new byte[0]);
                eventItems.add(item);
                continue;
            }

            if (index >= body.length()) {
                throw new IllegalArgumentException("消息体长度不足，无法解析事件内容长度");
            }

            // 事件内容长度 (1字节)
            int contentLength = body.getUnsignedByte(index);
            index += 1;

            if (index + contentLength > body.length()) {
                throw new IllegalArgumentException("消息体长度不足，无法解析事件内容");
            }

            // 事件内容
            byte[] contentBytes = body.getBytes(index, index + contentLength);
            index += contentLength;

            // 创建事件项
            EventItem item = new EventItem(eventId, contentBytes);
            eventItems.add(item);
        }
    }

    /**
     * 添加事件项
     */
    public void addEventItem(EventItem item) {
        if (item != null) {
            eventItems.add(item);
        }
    }

    /**
     * 添加事件项（使用字符串内容）
     */
    public void addEventItem(byte eventId, String content) {
        if (content == null) {
            content = "";
        }
        try {
            byte[] contentBytes = content.getBytes("GBK");
            if (contentBytes.length > 255) {
                throw new IllegalArgumentException("事件内容长度不能超过255字节");
            }
            eventItems.add(new EventItem(eventId, contentBytes));
        } catch (Exception e) {
            throw new RuntimeException("事件内容GBK编码失败：" + e.getMessage(), e);
        }
    }

    /**
     * 添加事件项（仅事件ID，用于删除操作）
     */
    public void addEventId(byte eventId) {
        eventItems.add(new EventItem(eventId, new byte[0]));
    }

    /**
     * 获取事件项
     */
    public EventItem getEventItem(byte eventId) {
        return eventItems.stream()
                .filter(item -> item.getEventId() == eventId)
                .findFirst()
                .orElse(null);
    }

    /**
     * 移除事件项
     */
    public boolean removeEventItem(byte eventId) {
        return eventItems.removeIf(item -> item.getEventId() == eventId);
    }

    /**
     * 清空所有事件项
     */
    public void clearEventItems() {
        eventItems.clear();
    }

    /**
     * 检查设置类型
     */
    public boolean isDeleteAll() {
        return settingType == SettingType.DELETE_ALL;
    }

    // 静态工厂方法

    public boolean isUpdate() {
        return settingType == SettingType.UPDATE;
    }

    public boolean isAppend() {
        return settingType == SettingType.APPEND;
    }

    public boolean isModify() {
        return settingType == SettingType.MODIFY;
    }

    public boolean isDeleteSpecific() {
        return settingType == SettingType.DELETE_SPECIFIC;
    }

    /**
     * 获取设置类型描述
     */
    public String getSettingTypeDescription() {
        switch (settingType) {
            case SettingType.DELETE_ALL:
                return "删除终端现有所有事件";
            case SettingType.UPDATE:
                return "更新事件";
            case SettingType.APPEND:
                return "追加事件";
            case SettingType.MODIFY:
                return "修改事件";
            case SettingType.DELETE_SPECIFIC:
                return "删除特定几项事件";
            default:
                return "未知设置类型(" + (settingType & 0xFF) + ")";
        }
    }

    // Getters and Setters

    public byte getSettingType() {
        return settingType;
    }

    public void setSettingType(byte settingType) {
        this.settingType = settingType;
    }

    public List<EventItem> getEventItems() {
        return new ArrayList<>(eventItems);
    }

    public void setEventItems(List<EventItem> eventItems) {
        this.eventItems = eventItems != null ? new ArrayList<>(eventItems) : new ArrayList<>();
    }

    /**
     * 获取事件总数
     */
    public int getEventCount() {
        return eventItems.size();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("T8301EventSetting{");
        sb.append("settingType=").append(getSettingTypeDescription());
        sb.append(", eventCount=").append(eventItems.size());
        if (!eventItems.isEmpty()) {
            sb.append(", events=[");
            for (int i = 0; i < eventItems.size(); i++) {
                if (i > 0) sb.append(", ");
                sb.append(eventItems.get(i));
            }
            sb.append("]");
        }
        sb.append(", header=").append(getHeader());
        sb.append('}');
        return sb.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        T8301EventSetting that = (T8301EventSetting) obj;
        return settingType == that.settingType &&
                eventItems.equals(that.eventItems);
    }

    @Override
    public int hashCode() {
        int result = settingType;
        result = 31 * result + eventItems.hashCode();
        return result;
    }

    /**
     * 设置类型常量
     */
    public static class SettingType {
        /**
         * 删除终端现有所有事件
         */
        public static final byte DELETE_ALL = 0;
        /**
         * 更新事件
         */
        public static final byte UPDATE = 1;
        /**
         * 追加事件
         */
        public static final byte APPEND = 2;
        /**
         * 修改事件
         */
        public static final byte MODIFY = 3;
        /**
         * 删除特定几项事件
         */
        public static final byte DELETE_SPECIFIC = 4;
    }

    /**
     * 事件项内部类
     */
    public static class EventItem {
        /**
         * 事件ID
         */
        private byte eventId;
        /**
         * 事件内容字节数组
         */
        private byte[] contentBytes;

        public EventItem(byte eventId, byte[] contentBytes) {
            this.eventId = eventId;
            this.contentBytes = contentBytes != null ? contentBytes.clone() : new byte[0];
        }

        public EventItem(byte eventId, String content) {
            this.eventId = eventId;
            if (content == null) {
                this.contentBytes = new byte[0];
            } else {
                try {
                    this.contentBytes = content.getBytes("GBK");
                    if (this.contentBytes.length > 255) {
                        throw new IllegalArgumentException("事件内容长度不能超过255字节");
                    }
                } catch (Exception e) {
                    throw new RuntimeException("事件内容GBK编码失败", e);
                }
            }
        }

        public byte getEventId() {
            return eventId;
        }

        public void setEventId(byte eventId) {
            this.eventId = eventId;
        }

        public byte[] getContentBytes() {
            return contentBytes.clone();
        }

        public void setContentBytes(byte[] contentBytes) {
            this.contentBytes = contentBytes != null ? contentBytes.clone() : new byte[0];
        }

        /**
         * 获取事件内容字符串（GBK解码）
         */
        public String getContentString() {
            if (contentBytes.length == 0) {
                return "";
            }
            try {
                return new String(contentBytes, "GBK");
            } catch (Exception e) {
                return "";
            }
        }

        /**
         * 设置事件内容字符串（GBK编码）
         */
        public void setContentString(String content) {
            if (content == null) {
                this.contentBytes = new byte[0];
            } else {
                try {
                    this.contentBytes = content.getBytes("GBK");
                    if (this.contentBytes.length > 255) {
                        throw new IllegalArgumentException("事件内容长度不能超过255字节");
                    }
                } catch (Exception e) {
                    throw new RuntimeException("事件内容GBK编码失败", e);
                }
            }
        }

        /**
         * 获取事件内容长度
         */
        public int getContentLength() {
            return contentBytes.length;
        }

        @Override
        public String toString() {
            return "EventItem{" +
                    "eventId=" + (eventId & 0xFF) +
                    ", contentLength=" + contentBytes.length +
                    ", content='" + getContentString() + "'" +
                    '}';
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;

            EventItem eventItem = (EventItem) obj;
            return eventId == eventItem.eventId &&
                    java.util.Arrays.equals(contentBytes, eventItem.contentBytes);
        }

        @Override
        public int hashCode() {
            int result = eventId;
            result = 31 * result + java.util.Arrays.hashCode(contentBytes);
            return result;
        }
    }
}