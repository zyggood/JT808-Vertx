package com.jt808.protocol.message.additional;

/**
 * 无线通信网络信号强度 (ID: 0x30)
 * BYTE
 */
public class SignalStrengthInfo extends AdditionalInfo {

    /**
     * 信号强度
     */
    private int signalStrength;

    public SignalStrengthInfo() {
        super(0x30, 1);
    }

    public SignalStrengthInfo(int signalStrength) {
        super(0x30, 1);
        this.signalStrength = signalStrength;
    }

    @Override
    public String getTypeName() {
        return "无线通信网络信号强度";
    }

    @Override
    public String getDescription() {
        return String.format("信号强度: %d", signalStrength);
    }

    @Override
    public void parseData(byte[] data) {
        this.signalStrength = data[0] & 0xFF;
    }

    @Override
    public byte[] encodeData() {
        return new byte[]{(byte) signalStrength};
    }

    /**
     * 获取信号强度
     *
     * @return 信号强度
     */
    public int getSignalStrength() {
        return signalStrength;
    }

    /**
     * 设置信号强度
     *
     * @param signalStrength 信号强度
     */
    public void setSignalStrength(int signalStrength) {
        this.signalStrength = signalStrength;
    }

    @Override
    public String toString() {
        return String.format("SignalStrengthInfo{id=0x%02X, signalStrength=%d}", id, signalStrength);
    }
}