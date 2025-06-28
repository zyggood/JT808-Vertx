package com.jt808.protocol.message;

import io.vertx.core.buffer.Buffer;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 位置信息汇报消息 (0x0200)
 * 终端定时或按需向平台汇报位置信息
 */
public class T0200LocationReport extends JT808Message {

    /** 报警标志位 */
    private int alarmFlag;
    
    /** 状态位 */
    private int statusFlag;
    
    /** 纬度 (以度为单位的纬度值乘以10^6，精确到百万分之一度) */
    private int latitude;
    
    /** 经度 (以度为单位的经度值乘以10^6，精确到百万分之一度) */
    private int longitude;
    
    /** 高程 (海拔高度，单位为米) */
    private int altitude;
    
    /** 速度 (1/10km/h) */
    private int speed;
    
    /** 方向 (0-359，正北为0，顺时针) */
    private int direction;
    
    /** 时间 (BCD[6] YY-MM-DD-hh-mm-ss GMT+8) */
    private LocalDateTime dateTime;
    
    /** 附加信息 */
    private Buffer additionalInfo;
    
    public T0200LocationReport() {
        super();
    }
    
    public T0200LocationReport(JT808Header header) {
        super(header);
    }
    
    @Override
    public int getMessageId() {
        return 0x0200;
    }
    
    @Override
    public Buffer encodeBody() {
        Buffer buffer = Buffer.buffer();
        
        // 报警标志位 (4字节)
        buffer.appendUnsignedInt(alarmFlag);
        
        // 状态位 (4字节)
        buffer.appendUnsignedInt(statusFlag);
        
        // 纬度 (4字节)
        buffer.appendUnsignedInt(latitude);
        
        // 经度 (4字节)
        buffer.appendUnsignedInt(longitude);
        
        // 高程 (2字节)
        buffer.appendUnsignedShort(altitude);
        
        // 速度 (2字节)
        buffer.appendUnsignedShort(speed);
        
        // 方向 (2字节)
        buffer.appendUnsignedShort(direction);
        
        // 时间 (6字节BCD码)
        if (dateTime != null) {
            String timeStr = dateTime.format(DateTimeFormatter.ofPattern("yyMMddHHmmss"));
            for (int i = 0; i < timeStr.length(); i += 2) {
                String byteStr = timeStr.substring(i, i + 2);
                // BCD码编码：每个字节的高4位和低4位分别表示一位十进制数
                int high = Character.getNumericValue(byteStr.charAt(0));
                int low = Character.getNumericValue(byteStr.charAt(1));
                int bcdByte = (high << 4) | low;
                buffer.appendByte((byte) bcdByte);
            }
        } else {
            // 如果时间为空，填充6个0字节
            buffer.appendBytes(new byte[6]);
        }
        
        // 附加信息
        if (additionalInfo != null && additionalInfo.length() > 0) {
            buffer.appendBuffer(additionalInfo);
        }
        
        return buffer;
    }
    
    @Override
    public void decodeBody(Buffer body) {
        int index = 0;
        
        // 报警标志位 (4字节)
        alarmFlag = (int) body.getUnsignedInt(index);
        index += 4;
        
        // 状态位 (4字节)
        statusFlag = (int) body.getUnsignedInt(index);
        index += 4;
        
        // 纬度 (4字节)
        latitude = (int) body.getUnsignedInt(index);
        index += 4;
        
        // 经度 (4字节)
        longitude = (int) body.getUnsignedInt(index);
        index += 4;
        
        // 高程 (2字节)
        altitude = body.getUnsignedShort(index);
        index += 2;
        
        // 速度 (2字节)
        speed = body.getUnsignedShort(index);
        index += 2;
        
        // 方向 (2字节)
        direction = body.getUnsignedShort(index);
        index += 2;
        
        // 时间 (6字节BCD码)
        if (index + 6 <= body.length()) {
            StringBuilder timeStr = new StringBuilder();
            for (int i = 0; i < 6; i++) {
                int bcdByte = body.getUnsignedByte(index + i);
                // BCD码转换：每个字节的高4位和低4位分别表示一位十进制数
                int high = (bcdByte >> 4) & 0x0F;
                int low = bcdByte & 0x0F;
                timeStr.append(String.format("%d%d", high, low));
            }
            try {
                dateTime = LocalDateTime.parse("20" + timeStr.toString(), 
                    DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
            } catch (Exception e) {
                // 时间解析失败，使用当前时间
                System.err.println("时间解析失败，原始BCD时间字符串: " + timeStr.toString() + ", 错误: " + e.getMessage());
                dateTime = LocalDateTime.now();
            }
            index += 6;
        }
        
        // 附加信息 (剩余字节)
        if (index < body.length()) {
            additionalInfo = body.getBuffer(index, body.length());
        }
    }
    
    /**
     * 获取纬度（度）
     */
    public double getLatitudeDegrees() {
        return latitude / 1000000.0;
    }
    
    /**
     * 设置纬度（度）
     */
    public void setLatitudeDegrees(double degrees) {
        this.latitude = (int) (degrees * 1000000);
    }
    
    /**
     * 获取经度（度）
     */
    public double getLongitudeDegrees() {
        return longitude / 1000000.0;
    }
    
    /**
     * 设置经度（度）
     */
    public void setLongitudeDegrees(double degrees) {
        this.longitude = (int) (degrees * 1000000);
    }
    
    /**
     * 获取速度（km/h）
     */
    public double getSpeedKmh() {
        return speed / 10.0;
    }
    
    /**
     * 设置速度（km/h）
     */
    public void setSpeedKmh(double kmh) {
        this.speed = (int) (kmh * 10);
    }
    
    // Getters and Setters
    public int getAlarmFlag() {
        return alarmFlag;
    }
    
    public void setAlarmFlag(int alarmFlag) {
        this.alarmFlag = alarmFlag;
    }
    
    public int getStatusFlag() {
        return statusFlag;
    }
    
    public void setStatusFlag(int statusFlag) {
        this.statusFlag = statusFlag;
    }
    
    public int getLatitude() {
        return latitude;
    }
    
    public void setLatitude(int latitude) {
        this.latitude = latitude;
    }
    
    public int getLongitude() {
        return longitude;
    }
    
    public void setLongitude(int longitude) {
        this.longitude = longitude;
    }
    
    public int getAltitude() {
        return altitude;
    }
    
    public void setAltitude(int altitude) {
        this.altitude = altitude;
    }
    
    public int getSpeed() {
        return speed;
    }
    
    public void setSpeed(int speed) {
        this.speed = speed;
    }
    
    public int getDirection() {
        return direction;
    }
    
    public void setDirection(int direction) {
        this.direction = direction;
    }
    
    public LocalDateTime getDateTime() {
        return dateTime;
    }
    
    public void setDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
    }
    
    public Buffer getAdditionalInfo() {
        return additionalInfo;
    }
    
    public void setAdditionalInfo(Buffer additionalInfo) {
        this.additionalInfo = additionalInfo;
    }
    
    /**
     * 检查是否有紧急报警
     */
    public boolean hasEmergencyAlarm() {
        return (alarmFlag & 0x00000001) != 0;
    }
    
    /**
     * 检查是否有超速报警
     */
    public boolean hasSpeedingAlarm() {
        return (alarmFlag & 0x00000002) != 0;
    }
    
    /**
     * 检查是否有疲劳驾驶报警
     */
    public boolean hasFatigueAlarm() {
        return (alarmFlag & 0x00000004) != 0;
    }
    
    /**
     * 检查是否有危险预警
     */
    public boolean hasDangerWarning() {
        return (alarmFlag & 0x00000008) != 0;
    }
    
    /**
     * 检查是否有GNSS模块发生故障
     */
    public boolean hasGNSSFault() {
        return (alarmFlag & 0x00000010) != 0;
    }
    
    /**
     * 检查是否有GNSS天线未接或被剪断
     */
    public boolean hasGNSSAntennaFault() {
        return (alarmFlag & 0x00000020) != 0;
    }
    
    /**
     * 检查是否有GNSS天线短路
     */
    public boolean hasGNSSAntennaShort() {
        return (alarmFlag & 0x00000040) != 0;
    }
    
    /**
     * 检查是否有终端主电源欠压
     */
    public boolean hasMainPowerUndervoltage() {
        return (alarmFlag & 0x00000080) != 0;
    }
    
    /**
     * 检查是否有终端主电源掉电
     */
    public boolean hasMainPowerFailure() {
        return (alarmFlag & 0x00000100) != 0;
    }
    
    /**
     * 检查是否有终端LCD或显示器故障
     */
    public boolean hasLCDFault() {
        return (alarmFlag & 0x00000200) != 0;
    }
    
    /**
     * 检查是否有TTS模块故障
     */
    public boolean hasTTSFault() {
        return (alarmFlag & 0x00000400) != 0;
    }
    
    /**
     * 检查是否有摄像头故障
     */
    public boolean hasCameraFault() {
        return (alarmFlag & 0x00000800) != 0;
    }
    
    /**
     * 检查是否当天累计驾驶超时
     */
    public boolean hasDailyDrivingTimeout() {
        return (alarmFlag & 0x00040000) != 0;
    }
    
    /**
     * 检查是否超时停车
     */
    public boolean hasOvertimeParking() {
        return (alarmFlag & 0x00080000) != 0;
    }
    
    /**
     * 检查是否进出区域报警
     */
    public boolean hasAreaInOutAlarm() {
        return (alarmFlag & 0x00100000) != 0;
    }
    
    /**
     * 检查是否进出路线报警
     */
    public boolean hasRouteInOutAlarm() {
        return (alarmFlag & 0x00200000) != 0;
    }
    
    /**
     * 检查是否路段行驶时间不足/过长
     */
    public boolean hasRoadSectionTimeAlarm() {
        return (alarmFlag & 0x00400000) != 0;
    }
    
    /**
     * 检查是否路线偏离报警
     */
    public boolean hasRouteDeviationAlarm() {
        return (alarmFlag & 0x00800000) != 0;
    }
    
    /**
     * 检查是否车辆VSS故障
     */
    public boolean hasVSSFault() {
        return (alarmFlag & 0x01000000) != 0;
    }
    
    /**
     * 检查是否车辆油量异常
     */
    public boolean hasFuelAbnormal() {
        return (alarmFlag & 0x02000000) != 0;
    }
    
    /**
     * 检查是否车辆被盗(通过车辆防盗器)
     */
    public boolean hasVehicleTheft() {
        return (alarmFlag & 0x04000000) != 0;
    }
    
    /**
     * 检查是否车辆非法点火
     */
    public boolean hasIllegalIgnition() {
        return (alarmFlag & 0x08000000) != 0;
    }
    
    /**
     * 检查是否车辆非法位移
     */
    public boolean hasIllegalDisplacement() {
        return (alarmFlag & 0x10000000) != 0;
    }
    
    /**
     * 检查是否碰撞侧翻报警
     */
    public boolean hasCollisionRolloverAlarm() {
        return (alarmFlag & 0x20000000) != 0;
    }
    
    /**
     * 检查是否非法开门报警(终端未设防时，车门开启，且车辆未启动)
     */
    public boolean hasIllegalDoorOpenAlarm() {
        return (alarmFlag & 0x40000000) != 0;
    }
    
    /**
     * 获取所有激活的报警类型描述
     * @return 报警类型描述列表
     */
    public java.util.List<String> getActiveAlarmDescriptions() {
        java.util.List<String> alarms = new java.util.ArrayList<>();
        if (hasEmergencyAlarm()) alarms.add("紧急报警");
        if (hasSpeedingAlarm()) alarms.add("超速报警");
        if (hasFatigueAlarm()) alarms.add("疲劳驾驶");
        if (hasDangerWarning()) alarms.add("危险预警");
        if (hasGNSSFault()) alarms.add("GNSS模块故障");
        if (hasGNSSAntennaFault()) alarms.add("GNSS天线未接或被剪断");
        if (hasGNSSAntennaShort()) alarms.add("GNSS天线短路");
        if (hasMainPowerUndervoltage()) alarms.add("终端主电源欠压");
        if (hasMainPowerFailure()) alarms.add("终端主电源掉电");
        if (hasLCDFault()) alarms.add("终端LCD或显示器故障");
        if (hasTTSFault()) alarms.add("TTS模块故障");
        if (hasCameraFault()) alarms.add("摄像头故障");
        if (hasDailyDrivingTimeout()) alarms.add("当天累计驾驶超时");
        if (hasOvertimeParking()) alarms.add("超时停车");
        if (hasAreaInOutAlarm()) alarms.add("进出区域报警");
        if (hasRouteInOutAlarm()) alarms.add("进出路线报警");
        if (hasRoadSectionTimeAlarm()) alarms.add("路段行驶时间不足/过长");
        if (hasRouteDeviationAlarm()) alarms.add("路线偏离报警");
        if (hasVSSFault()) alarms.add("车辆VSS故障");
        if (hasFuelAbnormal()) alarms.add("车辆油量异常");
        if (hasVehicleTheft()) alarms.add("车辆被盗");
        if (hasIllegalIgnition()) alarms.add("车辆非法点火");
        if (hasIllegalDisplacement()) alarms.add("车辆非法位移");
        if (hasCollisionRolloverAlarm()) alarms.add("碰撞侧翻报警");
        if (hasIllegalDoorOpenAlarm()) alarms.add("非法开门报警");
        return alarms;
    }
    
    @Override
    public String toString() {
        List<String> activeAlarms = getActiveAlarmDescriptions();
        String alarmDesc = activeAlarms.isEmpty() ? "无报警" : String.join(", ", activeAlarms);
        
        return "T0200LocationReport{" +
                "alarmFlag=0x" + Integer.toHexString(alarmFlag).toUpperCase() +
                " (" + alarmDesc + ")" +
                ", statusFlag=0x" + Integer.toHexString(statusFlag).toUpperCase() +
                ", latitude=" + getLatitudeDegrees() + "°" +
                ", longitude=" + getLongitudeDegrees() + "°" +
                ", altitude=" + altitude + "m" +
                ", speed=" + getSpeedKmh() + "km/h" +
                ", direction=" + direction + "°" +
                ", dateTime=" + dateTime +
                ", header=" + getHeader() +
                '}';
    }
}