package com.jt808.protocol.common;

/**
 * JT808协议报警类型枚举
 * 提供类型安全的报警标志位定义
 * <p>
 * 位 定义 处理说明
 * 0 1：紧急报警，触动报警开关后触发 收到应答后清零
 * 1 1：超速报警 标志维持至报警条件解除
 * 2 1：疲劳驾驶 标志维持至报警条件解除
 * 3 1：危险预警 收到应答后清零
 * 4 1：GNSS 模块发生故障 标志维持至报警条件解除
 * 5 1：GNSS 天线未接或被剪断 标志维持至报警条件解除
 * 6 1：GNSS 天线短路 标志维持至报警条件解除
 * 7 1：终端主电源欠压 标志维持至报警条件解除
 * 8 1：终端主电源掉电 标志维持至报警条件解除
 * 23
 * 位 定义 处理说明
 * 9 1：终端 LCD 或显示器故障 标志维持至报警条件解除
 * 10 1：TTS 模块故障 标志维持至报警条件解除
 * 11 1：摄像头故障 标志维持至报警条件解除
 * 12 1：道路运输证 IC 卡模块故障 标志维持至报警条件解除
 * 13 1：超速预警 标志维持至报警条件解除
 * 14 1：疲劳驾驶预警 标志维持至报警条件解除
 * 15-17 保留
 * 18 1：当天累计驾驶超时 标志维持至报警条件解除
 * 19 1：超时停车 标志维持至报警条件解除
 * 20 1：进出区域 收到应答后清零
 * 21 1：进出路线 收到应答后清零
 * 22 1：路段行驶时间不足/过长 收到应答后清零
 * 23 1：路线偏离报警 标志维持至报警条件解除
 * 24 1：车辆 VSS 故障 标志维持至报警条件解除
 * 25 1：车辆油量异常 标志维持至报警条件解除
 * 26 1：车辆被盗(通过车辆防盗器) 标志维持至报警条件解除
 * 27 1：车辆非法点火 收到应答后清零
 * 28 1：车辆非法位移 收到应答后清零
 * 29 1：碰撞预警 标志维持至报警条件解除
 * 30 1：侧翻预警 标志维持至报警条件解除
 * 31 1：非法开门报警（终端未设置区域时，不判断非法开门） 收到应答后清零
 */
public enum AlarmType {

    // 基础报警类型 (0x00000001 - 0x00000800)
    EMERGENCY(0x00000001, "紧急报警", "终端触发紧急报警按钮"),
    SPEEDING(0x00000002, "超速报警", "车辆超过限定速度"),
    FATIGUE(0x00000004, "疲劳驾驶", "驾驶员疲劳驾驶"),
    DANGER_WARNING(0x00000008, "危险预警", "危险预警报警"),
    GNSS_FAULT(0x00000010, "GNSS模块故障", "GNSS模块发生故障"),
    GNSS_ANTENNA_FAULT(0x00000020, "GNSS天线故障", "GNSS天线未接或被剪断"),
    GNSS_ANTENNA_SHORT(0x00000040, "GNSS天线短路", "GNSS天线短路"),
    MAIN_POWER_UNDERVOLTAGE(0x00000080, "主电源欠压", "终端主电源欠压"),
    MAIN_POWER_FAILURE(0x00000100, "主电源掉电", "终端主电源掉电"),
    LCD_FAULT(0x00000200, "LCD故障", "终端LCD或显示器故障"),
    TTS_FAULT(0x00000400, "TTS模块故障", "TTS模块故障"),
    CAMERA_FAULT(0x00000800, "摄像头故障", "摄像头故障"),

    // 驾驶行为报警 (0x00040000 - 0x00800000)
    DAILY_DRIVING_TIMEOUT(0x00040000, "当天累计驾驶超时", "当天累计驾驶超时"),
    OVERTIME_PARKING(0x00080000, "超时停车", "超时停车"),
    AREA_IN_OUT_ALARM(0x00100000, "进出区域报警", "进出区域报警"),
    ROUTE_IN_OUT_ALARM(0x00200000, "进出路线报警", "进出路线报警"),
    ROAD_SECTION_TIME_ALARM(0x00400000, "路段行驶时间报警", "路段行驶时间不足/过长"),
    ROUTE_DEVIATION_ALARM(0x00800000, "路线偏离报警", "路线偏离报警"),

    // 车辆状态报警 (0x01000000 - 0x40000000)
    VSS_FAULT(0x01000000, "VSS故障", "车辆VSS故障"),
    FUEL_ABNORMAL(0x02000000, "油量异常", "车辆油量异常"),
    VEHICLE_THEFT(0x04000000, "车辆被盗", "车辆被盗(通过车辆防盗器)"),
    ILLEGAL_IGNITION(0x08000000, "非法点火", "车辆非法点火"),
    ILLEGAL_DISPLACEMENT(0x10000000, "非法位移", "车辆非法位移"),
    COLLISION_ROLLOVER(0x20000000, "碰撞侧翻报警", "碰撞侧翻报警"),
    ILLEGAL_DOOR_OPEN(0x40000000, "非法开门报警", "非法开门报警(终端未设防时，车门开启，且车辆未启动)");

    private final int flag;
    private final String name;
    private final String description;

    AlarmType(int flag, String name, String description) {
        this.flag = flag;
        this.name = name;
        this.description = description;
    }

    /**
     * 根据标志位获取报警类型
     */
    public static AlarmType fromFlag(int flag) {
        for (AlarmType type : values()) {
            if (type.flag == flag) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown alarm flag: 0x" + Integer.toHexString(flag));
    }

    /**
     * 获取所有激活的报警类型
     */
    public static java.util.List<AlarmType> getActiveAlarms(int alarmFlag) {
        java.util.List<AlarmType> activeAlarms = new java.util.ArrayList<>();
        for (AlarmType type : values()) {
            if (type.isSet(alarmFlag)) {
                activeAlarms.add(type);
            }
        }
        return activeAlarms;
    }

    /**
     * 获取所有激活的报警描述
     */
    public static java.util.List<String> getActiveAlarmNames(int alarmFlag) {
        return getActiveAlarms(alarmFlag).stream()
                .map(AlarmType::getName)
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * 组合多个报警类型为标志位
     */
    public static int combineFlags(AlarmType... types) {
        int combined = 0;
        for (AlarmType type : types) {
            combined |= type.flag;
        }
        return combined;
    }

    /**
     * 获取报警标志位
     */
    public int getFlag() {
        return flag;
    }

    /**
     * 获取报警名称
     */
    public String getName() {
        return name;
    }

    /**
     * 获取报警描述
     */
    public String getDescription() {
        return description;
    }

    /**
     * 检查指定的报警标志位是否包含此报警类型
     */
    public boolean isSet(int alarmFlag) {
        return (alarmFlag & this.flag) != 0;
    }

    @Override
    public String toString() {
        return String.format("%s(0x%08X): %s", name, flag, description);
    }
}