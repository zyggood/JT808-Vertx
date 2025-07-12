package com.jt808.protocol.message;

/**
 * JT808消息头
 */
public class JT808Header {

    /**
     * 消息ID
     */
    private int messageId;

    /**
     * 消息体属性
     */
    private int messageProperty;

    /**
     * 协议版本号
     */
    private byte protocolVersion;

    /**
     * 终端手机号
     */
    private String phoneNumber;

    /**
     * 消息流水号
     */
    private int serialNumber;

    /**
     * 消息包封装项（分包时使用）
     */
    private PackageInfo packageInfo;

    public JT808Header() {
    }

    public JT808Header(int messageId, String phoneNumber, int serialNumber) {
        this.messageId = messageId;
        this.phoneNumber = phoneNumber;
        this.serialNumber = serialNumber;
    }

    /**
     * 获取消息体长度
     *
     * @return 消息体长度
     */
    public int getBodyLength() {
        return messageProperty & 0x03FF;
    }

    /**
     * 设置消息体长度
     *
     * @param length 消息体长度
     */
    public void setBodyLength(int length) {
        messageProperty = (messageProperty & 0xFC00) | (length & 0x03FF);
    }

    /**
     * 获取数据加密方式
     *
     * @return 加密方式
     */
    public int getEncryptType() {
        return (messageProperty & 0x1C00) >> 10;
    }

    /**
     * 设置数据加密方式
     *
     * @param encryptType 加密方式
     */
    public void setEncryptType(int encryptType) {
        messageProperty = (messageProperty & 0xE3FF) | ((encryptType & 0x07) << 10);
    }

    /**
     * 是否分包
     *
     * @return true表示分包
     */
    public boolean isSubpackage() {
        return (messageProperty & 0x2000) != 0;
    }

    /**
     * 设置分包标志
     *
     * @param subpackage 是否分包
     */
    public void setSubpackage(boolean subpackage) {
        if (subpackage) {
            messageProperty |= 0x2000;
        } else {
            messageProperty &= 0xDFFF;
        }
    }

    /**
     * 获取保留位
     *
     * @return 保留位
     */
    public int getReserved() {
        return (messageProperty & 0xC000) >> 14;
    }

    /**
     * 设置保留位
     *
     * @param reserved 保留位
     */
    public void setReserved(int reserved) {
        messageProperty = (messageProperty & 0x3FFF) | ((reserved & 0x03) << 14);
    }

    // Getters and Setters
    public int getMessageId() {
        return messageId;
    }

    public void setMessageId(int messageId) {
        this.messageId = messageId;
    }

    public int getMessageProperty() {
        return messageProperty;
    }

    public void setMessageProperty(int messageProperty) {
        this.messageProperty = messageProperty;
    }

    public byte getProtocolVersion() {
        return protocolVersion;
    }

    public void setProtocolVersion(byte protocolVersion) {
        this.protocolVersion = protocolVersion;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public int getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(int serialNumber) {
        this.serialNumber = serialNumber;
    }

    public PackageInfo getPackageInfo() {
        return packageInfo;
    }

    public void setPackageInfo(PackageInfo packageInfo) {
        this.packageInfo = packageInfo;
    }

    @Override
    public String toString() {
        return "JT808Header{" +
                "messageId=0x" + Integer.toHexString(messageId).toUpperCase() +
                ", messageProperty=0x" + Integer.toHexString(messageProperty).toUpperCase() +
                ", protocolVersion=0x" + Integer.toHexString(protocolVersion & 0xFF).toUpperCase() +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", serialNumber=" + serialNumber +
                ", packageInfo=" + packageInfo +
                '}';
    }

    /**
     * 分包信息
     */
    public static class PackageInfo {
        /**
         * 消息包总数
         */
        private int totalPackages;

        /**
         * 包序号
         */
        private int packageSequence;

        public PackageInfo() {
        }

        public PackageInfo(int totalPackages, int packageSequence) {
            this.totalPackages = totalPackages;
            this.packageSequence = packageSequence;
        }

        public int getTotalPackages() {
            return totalPackages;
        }

        public void setTotalPackages(int totalPackages) {
            this.totalPackages = totalPackages;
        }

        public int getPackageSequence() {
            return packageSequence;
        }

        public void setPackageSequence(int packageSequence) {
            this.packageSequence = packageSequence;
        }

        @Override
        public String toString() {
            return "PackageInfo{" +
                    "totalPackages=" + totalPackages +
                    ", packageSequence=" + packageSequence +
                    '}';
        }
    }
}