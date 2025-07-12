package com.jt808.protocol.message.additional;

/**
 * 进出区域/路线报警附加信息 (ID: 0x12)
 * 位置类型 + 区域或路段ID + 方向
 */
public class AreaRouteAlarmInfo extends AdditionalInfo {

    private int locationType;
    private long areaId;
    private int direction;

    public AreaRouteAlarmInfo() {
        super(0x12, 6);
    }

    public AreaRouteAlarmInfo(int locationType, long areaId, int direction) {
        super(0x12, 6);
        this.locationType = locationType;
        this.areaId = areaId;
        this.direction = direction;
    }

    @Override
    public String getTypeName() {
        return "进出区域/路线报警附加信息";
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
        String directionDesc = switch (direction) {
            case 0 -> "进入";
            case 1 -> "离开";
            default -> "未知方向(" + direction + ")";
        };
        return String.format("进出区域/路线报警: %s, ID=%d, %s", locationDesc, areaId, directionDesc);
    }

    @Override
    public void parseData(byte[] data) {
        if (data.length >= 6) {
            this.locationType = data[0] & 0xFF;
            this.areaId = parseDWORD(data, 1);
            this.direction = data[5] & 0xFF;
        }
    }

    @Override
    public byte[] encodeData() {
        byte[] data = new byte[6];
        data[0] = (byte) locationType;
        byte[] areaIdBytes = encodeDWORD(areaId);
        System.arraycopy(areaIdBytes, 0, data, 1, 4);
        data[5] = (byte) direction;
        return data;
    }

    // Getters and Setters
    public int getLocationType() {
        return locationType;
    }

    public void setLocationType(int locationType) {
        this.locationType = locationType;
    }

    public long getAreaId() {
        return areaId;
    }

    public void setAreaId(long areaId) {
        this.areaId = areaId;
    }

    public int getDirection() {
        return direction;
    }

    public void setDirection(int direction) {
        this.direction = direction;
    }

    @Override
    public String toString() {
        return String.format("AreaRouteAlarmInfo{id=0x%02X, locationType=%d, areaId=%d, direction=%d}",
                id, locationType, areaId, direction);
    }
}