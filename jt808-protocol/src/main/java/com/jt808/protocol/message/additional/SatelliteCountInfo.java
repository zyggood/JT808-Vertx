package com.jt808.protocol.message.additional;

/**
 * GNSS定位卫星数 (ID: 0x31)
 * BYTE
 */
public class SatelliteCountInfo extends AdditionalInfo {

    /**
     * 卫星数
     */
    private int satelliteCount;

    public SatelliteCountInfo() {
        super(0x31, 1);
    }

    public SatelliteCountInfo(int satelliteCount) {
        super(0x31, 1);
        this.satelliteCount = satelliteCount;
    }

    @Override
    public String getTypeName() {
        return "GNSS定位卫星数";
    }

    @Override
    public String getDescription() {
        return String.format("GNSS定位卫星数: %d", satelliteCount);
    }

    @Override
    public void parseData(byte[] data) {
        this.satelliteCount = data[0] & 0xFF;
    }

    @Override
    public byte[] encodeData() {
        return new byte[]{(byte) satelliteCount};
    }

    /**
     * 获取卫星数
     *
     * @return 卫星数
     */
    public int getSatelliteCount() {
        return satelliteCount;
    }

    /**
     * 设置卫星数
     *
     * @param satelliteCount 卫星数
     */
    public void setSatelliteCount(int satelliteCount) {
        this.satelliteCount = satelliteCount;
    }

    @Override
    public String toString() {
        return String.format("SatelliteCountInfo{id=0x%02X, satelliteCount=%d}", id, satelliteCount);
    }
}