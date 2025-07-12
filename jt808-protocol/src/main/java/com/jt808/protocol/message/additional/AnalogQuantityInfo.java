package com.jt808.protocol.message.additional;

/**
 * 模拟量 (ID: 0x2B)
 * DWORD，bit0-15，AD0；bit16-31，AD1
 */
public class AnalogQuantityInfo extends AdditionalInfo {

    /**
     * 模拟量值
     */
    private long analogValue;

    public AnalogQuantityInfo() {
        super(0x2B, 4);
    }

    public AnalogQuantityInfo(long analogValue) {
        super(0x2B, 4);
        this.analogValue = analogValue;
    }

    public AnalogQuantityInfo(int ad0, int ad1) {
        super(0x2B, 4);
        this.analogValue = ((long) ad1 << 16) | (ad0 & 0xFFFF);
    }

    @Override
    public String getTypeName() {
        return "模拟量";
    }

    @Override
    public String getDescription() {
        return String.format("模拟量: AD0=%d, AD1=%d", getAD0(), getAD1());
    }

    @Override
    public void parseData(byte[] data) {
        this.analogValue = parseDWORD(data);
    }

    @Override
    public byte[] encodeData() {
        return encodeDWORD(analogValue);
    }

    /**
     * 获取AD0值（bit0-15）
     *
     * @return AD0值
     */
    public int getAD0() {
        return (int) (analogValue & 0xFFFF);
    }

    /**
     * 设置AD0值
     *
     * @param ad0 AD0值
     */
    public void setAD0(int ad0) {
        this.analogValue = (analogValue & 0xFFFF0000L) | (ad0 & 0xFFFF);
    }

    /**
     * 获取AD1值（bit16-31）
     *
     * @return AD1值
     */
    public int getAD1() {
        return (int) ((analogValue >> 16) & 0xFFFF);
    }

    /**
     * 设置AD1值
     *
     * @param ad1 AD1值
     */
    public void setAD1(int ad1) {
        this.analogValue = (analogValue & 0x0000FFFFL) | ((long) (ad1 & 0xFFFF) << 16);
    }

    /**
     * 获取模拟量原始值
     *
     * @return 模拟量原始值
     */
    public long getAnalogValue() {
        return analogValue;
    }

    /**
     * 设置模拟量原始值
     *
     * @param analogValue 模拟量原始值
     */
    public void setAnalogValue(long analogValue) {
        this.analogValue = analogValue;
    }

    @Override
    public String toString() {
        return String.format("AnalogQuantityInfo{id=0x%02X, AD0=%d, AD1=%d}", id, getAD0(), getAD1());
    }
}