package com.jt808.protocol.message.additional;

/**
 * 里程信息 (ID: 0x01)
 * DWORD，1/10km，对应车上里程表读数
 */
public class MileageInfo extends AdditionalInfo {

    /**
     * 里程值（单位：0.1km）
     */
    private long mileageRaw;

    public MileageInfo() {
        super(0x01, 4);
    }

    public MileageInfo(double mileageKm) {
        super(0x01, 4);
        this.mileageRaw = Math.round(mileageKm * 10);
    }

    @Override
    public String getTypeName() {
        return "里程信息";
    }

    @Override
    public String getDescription() {
        return String.format("里程: %.1f km", getMileageKm());
    }

    @Override
    public void parseData(byte[] data) {
        this.mileageRaw = parseDWORD(data);
    }

    @Override
    public byte[] encodeData() {
        return encodeDWORD(mileageRaw);
    }

    /**
     * 获取里程（单位：km）
     *
     * @return 里程值
     */
    public double getMileageKm() {
        return mileageRaw / 10.0;
    }

    /**
     * 设置里程（单位：km）
     *
     * @param mileageKm 里程值
     */
    public void setMileageKm(double mileageKm) {
        this.mileageRaw = Math.round(mileageKm * 10);
    }

    /**
     * 获取原始里程值（单位：0.1km）
     *
     * @return 原始里程值
     */
    public long getMileageRaw() {
        return mileageRaw;
    }

    /**
     * 设置原始里程值（单位：0.1km）
     *
     * @param mileageRaw 原始里程值
     */
    public void setMileageRaw(long mileageRaw) {
        this.mileageRaw = mileageRaw;
    }

    @Override
    public String toString() {
        return String.format("MileageInfo{id=0x%02X, mileage=%.1f km}", id, getMileageKm());
    }
}