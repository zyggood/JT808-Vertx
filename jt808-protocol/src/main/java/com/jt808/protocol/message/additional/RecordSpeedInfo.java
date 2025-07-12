package com.jt808.protocol.message.additional;

/**
 * 行驶记录速度信息 (ID: 0x03)
 * WORD，1/10km/h，行驶记录功能获取的速度
 */
public class RecordSpeedInfo extends AdditionalInfo {

    /**
     * 速度值（单位：0.1km/h）
     */
    private int speedRaw;

    public RecordSpeedInfo() {
        super(0x03, 2);
    }

    public RecordSpeedInfo(double speedKmh) {
        super(0x03, 2);
        this.speedRaw = (int) Math.round(speedKmh * 10);
    }

    @Override
    public String getTypeName() {
        return "行驶记录速度";
    }

    @Override
    public String getDescription() {
        return String.format("行驶记录速度: %.1f km/h", getSpeedKmh());
    }

    @Override
    public void parseData(byte[] data) {
        this.speedRaw = parseWORD(data);
    }

    @Override
    public byte[] encodeData() {
        return encodeWORD(speedRaw);
    }

    /**
     * 获取速度（单位：km/h）
     *
     * @return 速度值
     */
    public double getSpeedKmh() {
        return speedRaw / 10.0;
    }

    /**
     * 设置速度（单位：km/h）
     *
     * @param speedKmh 速度值
     */
    public void setSpeedKmh(double speedKmh) {
        this.speedRaw = (int) Math.round(speedKmh * 10);
    }

    /**
     * 获取原始速度值（单位：0.1km/h）
     *
     * @return 原始速度值
     */
    public int getSpeedRaw() {
        return speedRaw;
    }

    /**
     * 设置原始速度值（单位：0.1km/h）
     *
     * @param speedRaw 原始速度值
     */
    public void setSpeedRaw(int speedRaw) {
        this.speedRaw = speedRaw;
    }

    @Override
    public String toString() {
        return String.format("RecordSpeedInfo{id=0x%02X, speed=%.1f km/h}", id, getSpeedKmh());
    }
}