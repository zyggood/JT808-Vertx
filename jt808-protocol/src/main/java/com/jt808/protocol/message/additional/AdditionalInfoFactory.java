package com.jt808.protocol.message.additional;

/**
 * 附加信息工厂类
 * 负责根据ID创建对应的附加信息实例
 */
public class AdditionalInfoFactory {

    /**
     * 根据ID和数据创建附加信息实例
     *
     * @param id   附加信息ID
     * @param data 数据
     * @return 附加信息实例
     */
    public static AdditionalInfo createAdditionalInfo(int id, byte[] data) {
        AdditionalInfo info = createAdditionalInfo(id);
        if (info != null && data != null) {
            info.parseData(data);
        }
        return info;
    }

    /**
     * 根据ID创建附加信息实例
     *
     * @param id 附加信息ID
     * @return 附加信息实例
     */
    public static AdditionalInfo createAdditionalInfo(int id) {
        return switch (id) {
            case 0x01 -> new MileageInfo();
            case 0x02 -> new FuelInfo();
            case 0x03 -> new RecordSpeedInfo();
            case 0x04 -> new ManualAlarmEventInfo();
            case 0x11 -> new OverspeedAlarmInfo();
            case 0x12 -> new AreaRouteAlarmInfo();
            case 0x13 -> new RouteTimeAlarmInfo();
            case 0x25 -> new ExtendedVehicleSignalInfo();
            case 0x2A -> new IOStatusInfo();
            case 0x2B -> new AnalogQuantityInfo();
            case 0x30 -> new SignalStrengthInfo();
            case 0x31 -> new SatelliteCountInfo();
            default -> new CustomInfo(id);
        };
    }

    /**
     * 获取附加信息类型名称
     *
     * @param id 附加信息ID
     * @return 类型名称
     */
    public static String getTypeName(int id) {
        AdditionalInfo info = createAdditionalInfo(id);
        return info != null ? info.getTypeName() : "未知类型";
    }

    /**
     * 检查是否为已知的附加信息类型
     *
     * @param id 附加信息ID
     * @return 是否为已知类型
     */
    public static boolean isKnownType(int id) {
        return switch (id) {
            case 0x01, 0x02, 0x03, 0x04, 0x11, 0x12, 0x13, 0x25, 0x2A, 0x2B, 0x30, 0x31 -> true;
            default -> false;
        };
    }
}