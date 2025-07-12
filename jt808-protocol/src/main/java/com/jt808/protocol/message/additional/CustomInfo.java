package com.jt808.protocol.message.additional;

/**
 * 自定义信息 (未知ID)
 * 用于处理未知类型的附加信息
 */
public class CustomInfo extends AdditionalInfo {

    /**
     * 原始数据
     */
    private byte[] rawData;

    public CustomInfo(int id) {
        super(id, 0);
    }

    public CustomInfo(int id, byte[] data) {
        super(id, data != null ? data.length : 0);
        this.rawData = data != null ? data.clone() : new byte[0];
    }

    @Override
    public String getTypeName() {
        return "自定义信息";
    }

    @Override
    public String getDescription() {
        if (rawData == null || rawData.length == 0) {
            return String.format("自定义信息(ID: 0x%02X): 无数据", id);
        }

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("自定义信息(ID: 0x%02X): ", id));
        for (int i = 0; i < Math.min(rawData.length, 8); i++) {
            sb.append(String.format("%02X ", rawData[i] & 0xFF));
        }
        if (rawData.length > 8) {
            sb.append("...");
        }
        return sb.toString().trim();
    }

    @Override
    public void parseData(byte[] data) {
        this.rawData = data != null ? data.clone() : new byte[0];
    }

    @Override
    public byte[] encodeData() {
        return rawData != null ? rawData.clone() : new byte[0];
    }

    /**
     * 获取原始数据
     *
     * @return 原始数据
     */
    public byte[] getRawData() {
        return rawData != null ? rawData.clone() : new byte[0];
    }

    /**
     * 设置原始数据
     *
     * @param rawData 原始数据
     */
    public void setRawData(byte[] rawData) {
        this.rawData = rawData != null ? rawData.clone() : new byte[0];
    }

    /**
     * 获取数据的16进制字符串表示
     *
     * @return 16进制字符串
     */
    public String getHexString() {
        if (rawData == null || rawData.length == 0) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        for (byte b : rawData) {
            sb.append(String.format("%02X", b & 0xFF));
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        return String.format("CustomInfo{id=0x%02X, length=%d, data=%s}",
                id, rawData != null ? rawData.length : 0, getHexString());
    }
}