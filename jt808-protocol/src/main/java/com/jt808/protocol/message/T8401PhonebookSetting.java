package com.jt808.protocol.message;

import io.vertx.core.buffer.Buffer;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 设置电话本消息 (0x8401)
 * 平台设置终端电话本的消息
 */
public class T8401PhonebookSetting extends JT808Message {

    /**
     * 设置类型
     */
    private byte settingType;

    /**
     * 联系人总数
     */
    private byte contactCount;

    /**
     * 联系人项列表
     */
    private List<ContactItem> contactItems;

    public T8401PhonebookSetting() {
        super();
        this.contactItems = new ArrayList<>();
    }

    public T8401PhonebookSetting(JT808Header header) {
        super(header);
        this.contactItems = new ArrayList<>();
    }

    public T8401PhonebookSetting(byte settingType) {
        super();
        this.settingType = settingType;
        this.contactItems = new ArrayList<>();
    }

    public T8401PhonebookSetting(byte settingType, List<ContactItem> contactItems) {
        super();
        this.settingType = settingType;
        this.contactItems = contactItems != null ? new ArrayList<>(contactItems) : new ArrayList<>();
        this.contactCount = (byte) this.contactItems.size();
    }

    /**
     * 创建删除终端上所有存储的联系人的消息
     *
     * @return 设置电话本消息
     */
    public static T8401PhonebookSetting createDeleteAll() {
        return new T8401PhonebookSetting(SettingType.DELETE_ALL);
    }

    /**
     * 创建更新电话本的消息（删除终端中已有全部联系人并追加消息中的联系人）
     *
     * @param contactItems 联系人项列表
     * @return 设置电话本消息
     */
    public static T8401PhonebookSetting createUpdate(List<ContactItem> contactItems) {
        return new T8401PhonebookSetting(SettingType.UPDATE, contactItems);
    }

    /**
     * 创建追加电话本的消息
     *
     * @param contactItems 联系人项列表
     * @return 设置电话本消息
     */
    public static T8401PhonebookSetting createAppend(List<ContactItem> contactItems) {
        return new T8401PhonebookSetting(SettingType.APPEND, contactItems);
    }

    /**
     * 创建修改电话本的消息（以联系人为索引）
     *
     * @param contactItems 联系人项列表
     * @return 设置电话本消息
     */
    public static T8401PhonebookSetting createModify(List<ContactItem> contactItems) {
        return new T8401PhonebookSetting(SettingType.MODIFY, contactItems);
    }

    @Override
    public int getMessageId() {
        return 0x8401;
    }

    @Override
    public Buffer encodeBody() {
        Buffer buffer = Buffer.buffer();

        // 设置类型 (1字节)
        buffer.appendByte(settingType);

        // 如果是删除全部联系人，则不需要后续字节
        if (settingType == SettingType.DELETE_ALL) {
            return buffer;
        }

        // 联系人总数 (1字节)
        buffer.appendByte((byte) contactItems.size());

        // 联系人项列表
        for (ContactItem item : contactItems) {
            // 标志 (1字节)
            buffer.appendByte(item.getFlag());

            // 号码长度 (1字节) + 电话号码 (STRING)
            if (item.getPhoneNumber() != null && !item.getPhoneNumber().isEmpty()) {
                byte[] phoneBytes = item.getPhoneNumber().getBytes(Charset.forName("UTF-8"));
                if (phoneBytes.length > 255) {
                    throw new IllegalArgumentException("电话号码长度不能超过255字节，当前长度: " + phoneBytes.length + " 字节");
                }
                buffer.appendByte((byte) phoneBytes.length);
                buffer.appendBytes(phoneBytes);
            } else {
                buffer.appendByte((byte) 0);
            }

            // 联系人长度 (1字节) + 联系人 (STRING，GBK编码)
            if (item.getContactName() != null && !item.getContactName().isEmpty()) {
                byte[] nameBytes = item.getContactName().getBytes(Charset.forName("GBK"));
                if (nameBytes.length > 255) {
                    throw new IllegalArgumentException("联系人姓名长度不能超过255字节，当前长度: " + nameBytes.length + " 字节");
                }
                buffer.appendByte((byte) nameBytes.length);
                buffer.appendBytes(nameBytes);
            } else {
                buffer.appendByte((byte) 0);
            }
        }

        return buffer;
    }

    @Override
    public void decodeBody(Buffer body) {
        if (body == null || body.length() < 1) {
            throw new IllegalArgumentException("设置电话本消息体长度至少为1字节，实际长度: " + (body != null ? body.length() : 0) + " 字节");
        }

        int index = 0;
        contactItems.clear();

        // 设置类型 (1字节)
        settingType = body.getByte(index);
        index += 1;

        // 如果是删除全部联系人，则没有后续字节
        if (settingType == SettingType.DELETE_ALL) {
            contactCount = 0;
            return;
        }

        if (index >= body.length()) {
            throw new IllegalArgumentException("消息体长度不足，缺少联系人总数字段");
        }

        // 联系人总数 (1字节)
        contactCount = body.getByte(index);
        index += 1;

        // 解析联系人项列表
        for (int i = 0; i < (contactCount & 0xFF); i++) {
            if (index >= body.length()) {
                throw new IllegalArgumentException("消息体长度不足，无法解析联系人项 " + (i + 1));
            }

            // 标志 (1字节)
            byte flag = body.getByte(index);
            index += 1;

            if (index >= body.length()) {
                throw new IllegalArgumentException("消息体长度不足，无法解析号码长度");
            }

            // 号码长度 (1字节)
            int phoneLength = body.getByte(index) & 0xFF;
            index += 1;

            // 电话号码 (STRING)
            String phoneNumber = "";
            if (phoneLength > 0) {
                if (index + phoneLength > body.length()) {
                    throw new IllegalArgumentException("消息体长度不足，无法解析电话号码内容");
                }
                byte[] phoneBytes = body.getBytes(index, index + phoneLength);
                phoneNumber = new String(phoneBytes, Charset.forName("UTF-8"));
                index += phoneLength;
            }

            if (index >= body.length()) {
                throw new IllegalArgumentException("消息体长度不足，无法解析联系人长度");
            }

            // 联系人长度 (1字节)
            int nameLength = body.getByte(index) & 0xFF;
            index += 1;

            // 联系人 (STRING，GBK编码)
            String contactName = "";
            if (nameLength > 0) {
                if (index + nameLength > body.length()) {
                    throw new IllegalArgumentException("消息体长度不足，无法解析联系人姓名内容");
                }
                byte[] nameBytes = body.getBytes(index, index + nameLength);
                contactName = new String(nameBytes, Charset.forName("GBK"));
                index += nameLength;
            }

            // 创建联系人项
            ContactItem item = new ContactItem(flag, phoneNumber, contactName);
            contactItems.add(item);
        }
    }

    /**
     * 添加联系人项
     *
     * @param item 联系人项
     */
    public void addContactItem(ContactItem item) {
        if (item != null) {
            contactItems.add(item);
            contactCount = (byte) contactItems.size();
        }
    }

    /**
     * 添加联系人项
     *
     * @param flag 标志
     * @param phoneNumber 电话号码
     * @param contactName 联系人姓名
     */
    public void addContactItem(byte flag, String phoneNumber, String contactName) {
        addContactItem(new ContactItem(flag, phoneNumber, contactName));
    }

    /**
     * 根据电话号码查找联系人项
     *
     * @param phoneNumber 电话号码
     * @return 联系人项，如果未找到则返回null
     */
    public ContactItem getContactItem(String phoneNumber) {
        for (ContactItem item : contactItems) {
            if (Objects.equals(item.getPhoneNumber(), phoneNumber)) {
                return item;
            }
        }
        return null;
    }

    /**
     * 根据电话号码移除联系人项
     *
     * @param phoneNumber 电话号码
     * @return 是否成功移除
     */
    public boolean removeContactItem(String phoneNumber) {
        boolean removed = contactItems.removeIf(item -> Objects.equals(item.getPhoneNumber(), phoneNumber));
        if (removed) {
            contactCount = (byte) contactItems.size();
        }
        return removed;
    }

    /**
     * 清空所有联系人项
     */
    public void clearContactItems() {
        contactItems.clear();
        contactCount = 0;
    }

    /**
     * 检查是否为删除全部联系人
     *
     * @return true表示删除全部联系人
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
     * 获取设置类型的描述
     *
     * @return 设置类型描述
     */
    public String getSettingTypeDescription() {
        switch (settingType) {
            case SettingType.DELETE_ALL:
                return "删除终端上所有存储的联系人";
            case SettingType.UPDATE:
                return "更新电话本（删除终端中已有全部联系人并追加消息中的联系人）";
            case SettingType.APPEND:
                return "追加电话本";
            case SettingType.MODIFY:
                return "修改电话本（以联系人为索引）";
            default:
                return "未知设置类型: " + (settingType & 0xFF);
        }
    }

    // Getters and Setters
    public byte getSettingType() {
        return settingType;
    }

    public void setSettingType(byte settingType) {
        this.settingType = settingType;
    }

    public byte getContactCount() {
        return contactCount;
    }

    public void setContactCount(byte contactCount) {
        this.contactCount = contactCount;
    }

    public List<ContactItem> getContactItems() {
        return contactItems;
    }

    public void setContactItems(List<ContactItem> contactItems) {
        this.contactItems = contactItems != null ? new ArrayList<>(contactItems) : new ArrayList<>();
        this.contactCount = (byte) this.contactItems.size();
    }

    /**
     * 获取联系人总数的无符号值
     *
     * @return 无符号联系人总数 (0-255)
     */
    public int getContactCountUnsigned() {
        return contactCount & 0xFF;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("T8401PhonebookSetting{")
                .append("settingType=").append(settingType & 0xFF)
                .append(", settingTypeDesc='").append(getSettingTypeDescription()).append('\'')
                .append(", contactCount=").append(contactCount & 0xFF)
                .append(", contactItems=").append(contactItems)
                .append('}');
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        T8401PhonebookSetting that = (T8401PhonebookSetting) o;
        return settingType == that.settingType &&
                contactCount == that.contactCount &&
                Objects.equals(contactItems, that.contactItems);
    }

    @Override
    public int hashCode() {
        return Objects.hash(settingType, contactCount, contactItems);
    }

    /**
     * 设置类型常量
     */
    public static class SettingType {
        /**
         * 删除终端上所有存储的联系人
         */
        public static final byte DELETE_ALL = 0;
        /**
         * 表示更新电话本（删除终端中已有全部联系人并追加消息中的联系人）
         */
        public static final byte UPDATE = 1;
        /**
         * 表示追加电话本
         */
        public static final byte APPEND = 2;
        /**
         * 表示修改电话本（以联系人为索引）
         */
        public static final byte MODIFY = 3;
    }

    /**
     * 电话本联系人项内部类
     */
    public static class ContactItem {
        /**
         * 标志
         */
        private byte flag;

        /**
         * 电话号码
         */
        private String phoneNumber;

        /**
         * 联系人姓名
         */
        private String contactName;

        public ContactItem() {
        }

        public ContactItem(byte flag, String phoneNumber, String contactName) {
            this.flag = flag;
            setPhoneNumber(phoneNumber);
            setContactName(contactName);
        }

        public byte getFlag() {
            return flag;
        }

        public void setFlag(byte flag) {
            this.flag = flag;
        }

        public String getPhoneNumber() {
            return phoneNumber;
        }

        public void setPhoneNumber(String phoneNumber) {
            if (phoneNumber != null) {
                byte[] phoneBytes = phoneNumber.getBytes(Charset.forName("UTF-8"));
                if (phoneBytes.length > 255) {
                    throw new IllegalArgumentException("电话号码长度不能超过255字节，当前长度: " + phoneBytes.length + " 字节");
                }
            }
            this.phoneNumber = phoneNumber;
        }

        public String getContactName() {
            return contactName;
        }

        public void setContactName(String contactName) {
            if (contactName != null) {
                byte[] nameBytes = contactName.getBytes(Charset.forName("GBK"));
                if (nameBytes.length > 255) {
                    throw new IllegalArgumentException("联系人姓名长度不能超过255字节，当前长度: " + nameBytes.length + " 字节");
                }
            }
            this.contactName = contactName;
        }

        /**
         * 获取标志的无符号值
         *
         * @return 无符号标志 (0-255)
         */
        public int getFlagUnsigned() {
            return flag & 0xFF;
        }

        /**
         * 检查是否为呼入
         *
         * @return true表示呼入
         */
        public boolean isIncoming() {
            return flag == ContactFlag.INCOMING;
        }

        /**
         * 检查是否为呼出
         *
         * @return true表示呼出
         */
        public boolean isOutgoing() {
            return flag == ContactFlag.OUTGOING;
        }

        /**
         * 检查是否为呼入/呼出
         *
         * @return true表示呼入/呼出
         */
        public boolean isBidirectional() {
            return flag == ContactFlag.BIDIRECTIONAL;
        }

        /**
         * 获取标志的描述
         *
         * @return 标志描述
         */
        public String getFlagDescription() {
            switch (flag) {
                case ContactFlag.INCOMING:
                    return "呼入";
                case ContactFlag.OUTGOING:
                    return "呼出";
                case ContactFlag.BIDIRECTIONAL:
                    return "呼入/呼出";
                default:
                    return "未知标志: " + (flag & 0xFF);
            }
        }

        /**
         * 获取电话号码的字节长度（UTF-8编码）
         *
         * @return 字节长度
         */
        public int getPhoneNumberByteLength() {
            if (phoneNumber == null || phoneNumber.isEmpty()) {
                return 0;
            }
            return phoneNumber.getBytes(Charset.forName("UTF-8")).length;
        }

        /**
         * 获取联系人姓名的字节长度（GBK编码）
         *
         * @return 字节长度
         */
        public int getContactNameByteLength() {
            if (contactName == null || contactName.isEmpty()) {
                return 0;
            }
            return contactName.getBytes(Charset.forName("GBK")).length;
        }

        @Override
        public String toString() {
            return "ContactItem{" +
                    "flag=" + getFlagUnsigned() +
                    ", flagDesc='" + getFlagDescription() + '\'' +
                    ", phoneNumber='" + phoneNumber + '\'' +
                    ", contactName='" + contactName + '\'' +
                    ", phoneByteLength=" + getPhoneNumberByteLength() +
                    ", nameByteLength=" + getContactNameByteLength() +
                    '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ContactItem that = (ContactItem) o;
            return flag == that.flag &&
                    Objects.equals(phoneNumber, that.phoneNumber) &&
                    Objects.equals(contactName, that.contactName);
        }

        @Override
        public int hashCode() {
            return Objects.hash(flag, phoneNumber, contactName);
        }
    }

    /**
     * 联系人标志常量
     */
    public static class ContactFlag {
        /**
         * 呼入
         */
        public static final byte INCOMING = 1;
        /**
         * 呼出
         */
        public static final byte OUTGOING = 2;
        /**
         * 呼入/呼出
         */
        public static final byte BIDIRECTIONAL = 3;
    }
}