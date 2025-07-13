package com.jt808.protocol.message;

import io.vertx.core.buffer.Buffer;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 信息点播菜单设置消息 (0x8303)
 * 平台设置终端信息点播菜单的消息
 */
public class T8303InfoMenuSetting extends JT808Message {

    /**
     * 设置类型
     */
    private byte settingType;

    /**
     * 信息项总数
     */
    private byte infoItemCount;

    /**
     * 信息项列表
     */
    private List<InfoItem> infoItems;

    public T8303InfoMenuSetting() {
        super();
        this.infoItems = new ArrayList<>();
    }

    public T8303InfoMenuSetting(JT808Header header) {
        super(header);
        this.infoItems = new ArrayList<>();
    }

    public T8303InfoMenuSetting(byte settingType) {
        super();
        this.settingType = settingType;
        this.infoItems = new ArrayList<>();
    }

    public T8303InfoMenuSetting(byte settingType, List<InfoItem> infoItems) {
        super();
        this.settingType = settingType;
        this.infoItems = infoItems != null ? new ArrayList<>(infoItems) : new ArrayList<>();
        this.infoItemCount = (byte) this.infoItems.size();
    }

    /**
     * 创建删除终端全部信息项的消息
     *
     * @return 信息点播菜单设置消息
     */
    public static T8303InfoMenuSetting createDeleteAll() {
        return new T8303InfoMenuSetting(SettingType.DELETE_ALL);
    }

    /**
     * 创建更新菜单的消息
     *
     * @param infoItems 信息项列表
     * @return 信息点播菜单设置消息
     */
    public static T8303InfoMenuSetting createUpdate(List<InfoItem> infoItems) {
        return new T8303InfoMenuSetting(SettingType.UPDATE, infoItems);
    }

    /**
     * 创建追加菜单的消息
     *
     * @param infoItems 信息项列表
     * @return 信息点播菜单设置消息
     */
    public static T8303InfoMenuSetting createAppend(List<InfoItem> infoItems) {
        return new T8303InfoMenuSetting(SettingType.APPEND, infoItems);
    }

    /**
     * 创建修改菜单的消息
     *
     * @param infoItems 信息项列表
     * @return 信息点播菜单设置消息
     */
    public static T8303InfoMenuSetting createModify(List<InfoItem> infoItems) {
        return new T8303InfoMenuSetting(SettingType.MODIFY, infoItems);
    }

    @Override
    public int getMessageId() {
        return 0x8303;
    }

    @Override
    public Buffer encodeBody() {
        Buffer buffer = Buffer.buffer();

        // 设置类型 (1字节)
        buffer.appendByte(settingType);

        // 如果是删除全部信息项，则不需要后续字节
        if (settingType == SettingType.DELETE_ALL) {
            return buffer;
        }

        // 信息项总数 (1字节)
        buffer.appendByte((byte) infoItems.size());

        // 信息项列表
        for (InfoItem item : infoItems) {
            // 信息类型 (1字节)
            buffer.appendByte(item.getInfoType());

            // 信息名称长度 (2字节，WORD) + 信息名称 (STRING，GBK编码)
            if (item.getInfoName() != null && !item.getInfoName().isEmpty()) {
                byte[] nameBytes = item.getInfoName().getBytes(Charset.forName("GBK"));
                if (nameBytes.length > 65535) {
                    throw new IllegalArgumentException("信息名称长度不能超过65535字节，当前长度: " + nameBytes.length + " 字节");
                }
                buffer.appendUnsignedShort(nameBytes.length);
                buffer.appendBytes(nameBytes);
            } else {
                buffer.appendUnsignedShort(0);
            }
        }

        return buffer;
    }

    @Override
    public void decodeBody(Buffer body) {
        if (body == null || body.length() < 1) {
            throw new IllegalArgumentException("信息点播菜单设置消息体长度至少为1字节，实际长度: " + (body != null ? body.length() : 0) + " 字节");
        }

        int index = 0;
        infoItems.clear();

        // 设置类型 (1字节)
        settingType = body.getByte(index);
        index += 1;

        // 如果是删除全部信息项，则没有后续字节
        if (settingType == SettingType.DELETE_ALL) {
            infoItemCount = 0;
            return;
        }

        if (index >= body.length()) {
            throw new IllegalArgumentException("消息体长度不足，缺少信息项总数字段");
        }

        // 信息项总数 (1字节)
        infoItemCount = body.getByte(index);
        index += 1;

        // 解析信息项列表
        for (int i = 0; i < (infoItemCount & 0xFF); i++) {
            if (index >= body.length()) {
                throw new IllegalArgumentException("消息体长度不足，无法解析信息项 " + (i + 1));
            }

            // 信息类型 (1字节)
            byte infoType = body.getByte(index);
            index += 1;

            if (index + 2 > body.length()) {
                throw new IllegalArgumentException("消息体长度不足，无法解析信息名称长度");
            }

            // 信息名称长度 (2字节，WORD)
            int nameLength = body.getUnsignedShort(index);
            index += 2;

            // 信息名称 (STRING，GBK编码)
            String infoName = "";
            if (nameLength > 0) {
                if (index + nameLength > body.length()) {
                    throw new IllegalArgumentException("消息体长度不足，无法解析信息名称内容");
                }
                byte[] nameBytes = body.getBytes(index, index + nameLength);
                infoName = new String(nameBytes, Charset.forName("GBK"));
                index += nameLength;
            }

            // 创建信息项
            InfoItem item = new InfoItem(infoType, infoName);
            infoItems.add(item);
        }
    }

    /**
     * 添加信息项
     *
     * @param item 信息项
     */
    public void addInfoItem(InfoItem item) {
        if (item != null) {
            infoItems.add(item);
            infoItemCount = (byte) infoItems.size();
        }
    }

    /**
     * 添加信息项
     *
     * @param infoType 信息类型
     * @param infoName 信息名称
     */
    public void addInfoItem(byte infoType, String infoName) {
        addInfoItem(new InfoItem(infoType, infoName));
    }

    /**
     * 获取信息项
     *
     * @param infoType 信息类型
     * @return 信息项，如果不存在则返回null
     */
    public InfoItem getInfoItem(byte infoType) {
        return infoItems.stream()
                .filter(item -> item.getInfoType() == infoType)
                .findFirst()
                .orElse(null);
    }

    /**
     * 移除信息项
     *
     * @param infoType 信息类型
     * @return 是否成功移除
     */
    public boolean removeInfoItem(byte infoType) {
        boolean removed = infoItems.removeIf(item -> item.getInfoType() == infoType);
        if (removed) {
            infoItemCount = (byte) infoItems.size();
        }
        return removed;
    }

    /**
     * 清空所有信息项
     */
    public void clearInfoItems() {
        infoItems.clear();
        infoItemCount = 0;
    }

    /**
     * 检查设置类型
     */
    public boolean isDeleteAll() {
        return settingType == SettingType.DELETE_ALL;
    }

    public boolean isUpdate() {
        return settingType == SettingType.UPDATE;
    }

    public boolean isAppend() {
        return settingType == SettingType.APPEND;
    }

    public boolean isModify() {
        return settingType == SettingType.MODIFY;
    }

    /**
     * 获取设置类型描述
     *
     * @return 设置类型描述
     */
    public String getSettingTypeDescription() {
        switch (settingType) {
            case SettingType.DELETE_ALL:
                return "删除终端全部信息项";
            case SettingType.UPDATE:
                return "更新菜单";
            case SettingType.APPEND:
                return "追加菜单";
            case SettingType.MODIFY:
                return "修改菜单";
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

    public byte getInfoItemCount() {
        return infoItemCount;
    }

    public void setInfoItemCount(byte infoItemCount) {
        this.infoItemCount = infoItemCount;
    }

    public List<InfoItem> getInfoItems() {
        return new ArrayList<>(infoItems);
    }

    public void setInfoItems(List<InfoItem> infoItems) {
        this.infoItems = infoItems != null ? new ArrayList<>(infoItems) : new ArrayList<>();
        this.infoItemCount = (byte) this.infoItems.size();
    }

    /**
     * 获取信息项总数（无符号值）
     *
     * @return 信息项总数
     */
    public int getInfoItemCountUnsigned() {
        return infoItemCount & 0xFF;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("T8303InfoMenuSetting{");
        sb.append("settingType=").append(getSettingTypeDescription());
        sb.append(", infoItemCount=").append(getInfoItemCountUnsigned());
        if (!infoItems.isEmpty()) {
            sb.append(", infoItems=[");
            for (int i = 0; i < infoItems.size(); i++) {
                if (i > 0) sb.append(", ");
                sb.append(infoItems.get(i));
            }
            sb.append("]");
        }
        sb.append(", header=").append(getHeader());
        sb.append('}');
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        T8303InfoMenuSetting that = (T8303InfoMenuSetting) o;
        return settingType == that.settingType &&
                infoItemCount == that.infoItemCount &&
                Objects.equals(infoItems, that.infoItems);
    }

    @Override
    public int hashCode() {
        return Objects.hash(settingType, infoItemCount, infoItems);
    }

    /**
     * 设置类型常量
     */
    public static class SettingType {
        /**
         * 删除终端全部信息项
         */
        public static final byte DELETE_ALL = 0;
        /**
         * 更新菜单
         */
        public static final byte UPDATE = 1;
        /**
         * 追加菜单
         */
        public static final byte APPEND = 2;
        /**
         * 修改菜单
         */
        public static final byte MODIFY = 3;
    }

    /**
     * 信息点播信息项内部类
     */
    public static class InfoItem {
        /**
         * 信息类型
         */
        private byte infoType;

        /**
         * 信息名称
         */
        private String infoName;

        public InfoItem() {
        }

        public InfoItem(byte infoType, String infoName) {
            this.infoType = infoType;
            setInfoName(infoName);
        }

        public byte getInfoType() {
            return infoType;
        }

        public void setInfoType(byte infoType) {
            this.infoType = infoType;
        }

        public String getInfoName() {
            return infoName;
        }

        public void setInfoName(String infoName) {
            if (infoName != null) {
                byte[] nameBytes = infoName.getBytes(Charset.forName("GBK"));
                if (nameBytes.length > 65535) {
                    throw new IllegalArgumentException("信息名称长度不能超过65535字节，当前长度: " + nameBytes.length + " 字节");
                }
            }
            this.infoName = infoName;
        }

        /**
         * 获取信息类型的无符号值
         *
         * @return 无符号信息类型 (0-255)
         */
        public int getInfoTypeUnsigned() {
            return infoType & 0xFF;
        }

        /**
         * 获取信息名称的字节长度（GBK编码）
         *
         * @return 字节长度
         */
        public int getInfoNameByteLength() {
            if (infoName == null || infoName.isEmpty()) {
                return 0;
            }
            return infoName.getBytes(Charset.forName("GBK")).length;
        }

        @Override
        public String toString() {
            return "InfoItem{" +
                    "infoType=" + getInfoTypeUnsigned() +
                    ", infoName='" + infoName + '\'' +
                    ", nameByteLength=" + getInfoNameByteLength() +
                    '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            InfoItem infoItem = (InfoItem) o;
            return infoType == infoItem.infoType && Objects.equals(infoName, infoItem.infoName);
        }

        @Override
        public int hashCode() {
            return Objects.hash(infoType, infoName);
        }
    }
}