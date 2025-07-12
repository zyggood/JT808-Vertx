package com.jt808.protocol.message.additional;

/**
 * 路段行驶时间报警附加信息 (ID: 0x13)
 * 路段ID + 路段行驶时间 + 结果
 */
public class RouteTimeAlarmInfo extends AdditionalInfo {

    private long routeId;
    private int driveTime;
    private int result;

    public RouteTimeAlarmInfo() {
        super(0x13, 7);
    }

    public RouteTimeAlarmInfo(long routeId, int driveTime, int result) {
        super(0x13, 7);
        this.routeId = routeId;
        this.driveTime = driveTime;
        this.result = result;
    }

    @Override
    public String getTypeName() {
        return "路段行驶时间报警附加信息";
    }

    @Override
    public String getDescription() {
        String resultDesc = switch (result) {
            case 0 -> "不足";
            case 1 -> "过长";
            default -> "未知结果(" + result + ")";
        };
        return String.format("路段行驶时间报警: 路段ID=%d, 行驶时间=%d秒, 结果=%s", routeId, driveTime, resultDesc);
    }

    @Override
    public void parseData(byte[] data) {
        if (data.length >= 7) {
            this.routeId = parseDWORD(data, 0);
            this.driveTime = parseWORD(data, 4);
            this.result = data[6] & 0xFF;
        }
    }

    @Override
    public byte[] encodeData() {
        byte[] data = new byte[7];
        byte[] routeIdBytes = encodeDWORD(routeId);
        System.arraycopy(routeIdBytes, 0, data, 0, 4);
        byte[] driveTimeBytes = encodeWORD(driveTime);
        System.arraycopy(driveTimeBytes, 0, data, 4, 2);
        data[6] = (byte) result;
        return data;
    }

    // Getters and Setters
    public long getRouteId() {
        return routeId;
    }

    public void setRouteId(long routeId) {
        this.routeId = routeId;
    }

    public int getDriveTime() {
        return driveTime;
    }

    public void setDriveTime(int driveTime) {
        this.driveTime = driveTime;
    }

    public int getResult() {
        return result;
    }

    public void setResult(int result) {
        this.result = result;
    }

    @Override
    public String toString() {
        return String.format("RouteTimeAlarmInfo{id=0x%02X, routeId=%d, driveTime=%d, result=%d}",
                id, routeId, driveTime, result);
    }
}