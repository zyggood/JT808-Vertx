package com.jt808.protocol.message.additional;

/**
 * 超速报警附加信息 (ID: 0x11)
 * 位置类型 + 区域或路段ID
 */
public class OverspeedAlarmInfo extends AdditionalInfo {

    /**
     * 位置类型
     * 0：无特定位置
     * 1：圆形区域
     * 2：矩形区域
     * 3：多边形区域
     * 4：路段
     */
    private int locationType;

    /**
     * 区域或路段ID
     */
    private long areaId;

    public OverspeedAlarmInfo() {
        super(0x11, 5);
    }

    public OverspeedAlarmInfo(int locationType, long areaId) {
        super(0x11, 5);
        this.locationType = locationType;
        this.areaId = areaId;
    }

    @Override
    public String getTypeName() {
        return "超速报警附加信息";
    }

    @Override
    public String getDescription() {
        String locationDesc = switch (locationType) {
            case 0 -> "无特定位置";
            case 1 -> "圆形区域";
            case 2 -> "矩形区域";
            case 3 -> "多边形区域";
            case 4 -> "路段";
            default -> "未知位置类型(" + locationType + ")";
        };
        return String.format("超速报警: %s, ID=%d", locationDesc, areaId);
    }

    @Override
    public void parseData(byte[] data) {
        if (data.length >= 5) {
            this.locationType = data[0] & 0xFF;
            this.areaId = parseDWORD(data, 1);
        }
    }

    @Override
    public byte[] encodeData() {
        byte[] data = new byte[5];
        data[0] = (byte) locationType;
        byte[] areaIdBytes = encodeDWORD(areaId);
        System.arraycopy(areaIdBytes, 0, data, 1, 4);
        return data;
    }

    /**
     * 获取位置类型
     *
     * @return 位置类型
     */
    public int getLocationType() {
        return locationType;
    }

    /**
     * 设置位置类型
     *
     * @param locationType 位置类型
     */
    public void setLocationType(int locationType) {
        this.locationType = locationType;
    }

    /**
     * 获取区域或路段ID
     *
     * @return 区域或路段ID
     */
    public long getAreaId() {
        return areaId;
    }

    /**
     * 设置区域或路段ID
     *
     * @param areaId 区域或路段ID
     */
    public void setAreaId(long areaId) {
        this.areaId = areaId;
    }

    @Override
    public String toString() {
        return String.format("OverspeedAlarmInfo{id=0x%02X, locationType=%d, areaId=%d}",
                id, locationType, areaId);
    }
}