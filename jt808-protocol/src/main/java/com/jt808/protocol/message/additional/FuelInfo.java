package com.jt808.protocol.message.additional;

/**
 * 油量信息 (ID: 0x02)
 * WORD，1/10L，对应车上油量表读数
 */
public class FuelInfo extends AdditionalInfo {

    /**
     * 油量值（单位：0.1L）
     */
    private int fuelRaw;

    public FuelInfo() {
        super(0x02, 2);
    }

    public FuelInfo(double fuelL) {
        super(0x02, 2);
        this.fuelRaw = (int) Math.round(fuelL * 10);
    }

    @Override
    public String getTypeName() {
        return "油量信息";
    }

    @Override
    public String getDescription() {
        return String.format("油量: %.1f L", getFuelL());
    }

    @Override
    public void parseData(byte[] data) {
        this.fuelRaw = parseWORD(data);
    }

    @Override
    public byte[] encodeData() {
        return encodeWORD(fuelRaw);
    }

    /**
     * 获取油量（单位：L）
     *
     * @return 油量值
     */
    public double getFuelL() {
        return fuelRaw / 10.0;
    }

    /**
     * 设置油量（单位：L）
     *
     * @param fuelL 油量值
     */
    public void setFuelL(double fuelL) {
        this.fuelRaw = (int) Math.round(fuelL * 10);
    }

    /**
     * 获取原始油量值（单位：0.1L）
     *
     * @return 原始油量值
     */
    public int getFuelRaw() {
        return fuelRaw;
    }

    /**
     * 设置原始油量值（单位：0.1L）
     *
     * @param fuelRaw 原始油量值
     */
    public void setFuelRaw(int fuelRaw) {
        this.fuelRaw = fuelRaw;
    }

    @Override
    public String toString() {
        return String.format("FuelInfo{id=0x%02X, fuel=%.1f L}", id, getFuelL());
    }
}