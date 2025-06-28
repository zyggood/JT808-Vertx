package com.jt808.protocol.message;

import io.vertx.core.buffer.Buffer;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    
    /**
     * 解析后的附加信息
     */
    private Map<Integer, Object> parsedAdditionalInfo;
    
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
            // 解析附加信息项
            byte[] additionalBytes = additionalInfo.getBytes();
            parsedAdditionalInfo = parseAdditionalInfo(additionalBytes);
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
    
    public Map<Integer, Object> getParsedAdditionalInfo() {
        return parsedAdditionalInfo;
    }
    
    public void setParsedAdditionalInfo(Map<Integer, Object> parsedAdditionalInfo) {
        this.parsedAdditionalInfo = parsedAdditionalInfo;
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
     * 检查是否有超速预警
     */
    public boolean hasSpeedingWarning() {
        return (alarmFlag & 0x00002000) != 0;
    }
    
    /**
     * 检查是否有疲劳驾驶预警
     */
    public boolean hasFatigueWarning() {
        return (alarmFlag & 0x00004000) != 0;
    }
    
    /**
     * 检查是否有道路运输证IC卡模块故障
     */
    public boolean hasICCardFault() {
        return (alarmFlag & 0x00001000) != 0;
    }
    
    /**
     * 检查是否碰撞预警
     */
    public boolean hasCollisionWarning() {
        return (alarmFlag & 0x20000000) != 0;
    }
    
    /**
     * 检查是否侧翻预警
     */
    public boolean hasRolloverWarning() {
        return (alarmFlag & 0x40000000) != 0;
    }
    
    /**
     * 检查是否非法开门报警(终端未设防时，车门开启，且车辆未启动)
     */
    public boolean hasIllegalDoorOpenAlarm() {
        return (alarmFlag & 0x80000000) != 0;
    }
    
    // ==================== 状态位解析方法 ====================
    
    /**
     * 检查ACC开关状态
     * @return true: ACC开, false: ACC关
     */
    public boolean isACCOn() {
        return (statusFlag & 0x00000001) != 0;
    }
    
    /**
     * 检查定位状态
     * @return true: 已定位, false: 未定位
     */
    public boolean isPositioned() {
        return (statusFlag & 0x00000002) != 0;
    }
    
    /**
     * 检查南北纬
     * @return true: 南纬, false: 北纬
     */
    public boolean isSouthLatitude() {
        return (statusFlag & 0x00000004) != 0;
    }
    
    /**
     * 检查东西经
     * @return true: 西经, false: 东经
     */
    public boolean isWestLongitude() {
        return (statusFlag & 0x00000008) != 0;
    }
    
    /**
     * 检查运营状态
     * @return true: 停运状态, false: 运营状态
     */
    public boolean isOutOfService() {
        return (statusFlag & 0x00000010) != 0;
    }
    
    /**
     * 检查经纬度加密状态
     * @return true: 已加密, false: 未加密
     */
    public boolean isCoordinateEncrypted() {
        return (statusFlag & 0x00000020) != 0;
    }
    
    /**
     * 获取载重状态
     * @return 0: 空车, 1: 半载, 2: 保留, 3: 满载
     */
    public int getLoadStatus() {
        return (statusFlag >> 8) & 0x03;
    }
    
    /**
     * 检查车辆油路状态
     * @return true: 断开, false: 正常
     */
    public boolean isOilCircuitDisconnected() {
        return (statusFlag & 0x00000400) != 0;
    }
    
    /**
     * 检查车辆电路状态
     * @return true: 断开, false: 正常
     */
    public boolean isElectricCircuitDisconnected() {
        return (statusFlag & 0x00000800) != 0;
    }
    
    /**
     * 检查车门锁定状态
     * @return true: 加锁, false: 解锁
     */
    public boolean isDoorLocked() {
        return (statusFlag & 0x00001000) != 0;
    }
    
    /**
     * 检查门1状态（前门）
     * @return true: 门1开, false: 门1关
     */
    public boolean isDoor1Open() {
        return (statusFlag & 0x00002000) != 0;
    }
    
    /**
     * 检查门2状态（中门）
     * @return true: 门2开, false: 门2关
     */
    public boolean isDoor2Open() {
        return (statusFlag & 0x00004000) != 0;
    }
    
    /**
     * 检查门3状态（后门）
     * @return true: 门3开, false: 门3关
     */
    public boolean isDoor3Open() {
        return (statusFlag & 0x00008000) != 0;
    }
    
    /**
     * 检查门4状态（驾驶席门）
     * @return true: 门4开, false: 门4关
     */
    public boolean isDoor4Open() {
        return (statusFlag & 0x00010000) != 0;
    }
    
    /**
     * 检查门5状态（自定义）
     * @return true: 门5开, false: 门5关
     */
    public boolean isDoor5Open() {
        return (statusFlag & 0x00020000) != 0;
    }
    
    /**
     * 检查GPS定位状态
     * @return true: 使用GPS卫星进行定位, false: 未使用GPS卫星进行定位
     */
    public boolean isGPSPositioning() {
        return (statusFlag & 0x00040000) != 0;
    }
    
    /**
     * 检查北斗定位状态
     * @return true: 使用北斗卫星进行定位, false: 未使用北斗卫星进行定位
     */
    public boolean isBeidouPositioning() {
        return (statusFlag & 0x00080000) != 0;
    }
    
    /**
     * 检查GLONASS定位状态
     * @return true: 使用GLONASS卫星进行定位, false: 未使用GLONASS卫星进行定位
     */
    public boolean isGLONASSPositioning() {
        return (statusFlag & 0x00100000) != 0;
    }
    
    /**
     * 检查Galileo定位状态
     * @return true: 使用Galileo卫星进行定位, false: 未使用Galileo卫星进行定位
     */
    public boolean isGalileoPositioning() {
        return (statusFlag & 0x00200000) != 0;
    }
    
    // ==================== 附加信息项解析方法 ====================
    
    /**
     * 解析附加信息项
     * @param additionalData 附加信息字节数组
     * @return 解析结果的Map，key为附加信息ID，value为解析后的值
     */
    public Map<Integer, Object> parseAdditionalInfo(byte[] additionalData) {
        Map<Integer, Object> result = new HashMap<>();
        if (additionalData == null || additionalData.length == 0) {
            return result;
        }
        
        int offset = 0;
        while (offset < additionalData.length) {
            if (offset + 2 > additionalData.length) {
                break; // 数据不足，无法读取ID和长度
            }
            
            int id = additionalData[offset] & 0xFF;
            int length = additionalData[offset + 1] & 0xFF;
            offset += 2;
            
            if (offset + length > additionalData.length) {
                break; // 数据不足，无法读取完整的数据
            }
            
            byte[] data = new byte[length];
            System.arraycopy(additionalData, offset, data, 0, length);
            offset += length;
            
            Object value = parseAdditionalInfoItem(id, data);
            result.put(id, value);
        }
        
        return result;
    }
    
    /**
     * 解析单个附加信息项
     * @param id 附加信息ID
     * @param data 数据内容
     * @return 解析后的值
     */
    private Object parseAdditionalInfoItem(int id, byte[] data) {
        return switch (id) {
            case 0x01 -> // 里程，DWORD，1/10km，对应车上里程表读数
                    parseDWORD(data) / 10.0;
            case 0x02 -> // 油量，WORD，1/10L，对应车上油量表读数
                    parseWORD(data) / 10.0;
            case 0x03 -> // 行驶记录功能获取的速度，WORD，1/10km/h
                    parseWORD(data) / 10.0;
            case 0x04 -> // 需要人工确认报警事件的ID，WORD，从1开始计数
                    parseWORD(data);
            case 0x11 -> // 超速报警附加信息
                    parseOverspeedAlarmInfo(data);
            case 0x12 -> // 进出区域/路线报警附加信息
                    parseAreaRouteAlarmInfo(data);
            case 0x13 -> // 路段行驶时间不足/过长报警附加信息
                    parseRouteTimeAlarmInfo(data);
            case 0x25 -> // 扩展车辆信号状态位
                    parseExtendedVehicleSignalStatus(data);
            case 0x2A -> // IO状态位
                    parseIOStatus(data);
            case 0x2B -> // 模拟量，bit0-15，AD0；bit16-31，AD1
                    parseAnalogValue(data);
            case 0x30 -> // 无线通信网络信号强度
                    data[0] & 0xFF;
            case 0x31 -> // GNSS定位卫星数
                    data[0] & 0xFF;
            default ->
                // 自定义信息或未知类型，返回原始字节数组
                    data;
        };
    }
    
    /**
     * 解析WORD类型数据（2字节）
     */
    private int parseWORD(byte[] data) {
        if (data.length < 2) return 0;
        return ((data[0] & 0xFF) << 8) | (data[1] & 0xFF);
    }
    
    /**
     * 解析DWORD类型数据（4字节）
     */
    private long parseDWORD(byte[] data) {
        if (data.length < 4) return 0;
        return ((long)(data[0] & 0xFF) << 24) | 
               ((long)(data[1] & 0xFF) << 16) | 
               ((long)(data[2] & 0xFF) << 8) | 
               (long)(data[3] & 0xFF);
    }
    
    /**
     * 解析超速报警附加信息
     */
    private Map<String, Object> parseOverspeedAlarmInfo(byte[] data) {
        Map<String, Object> info = new HashMap<>();
        if (data.length >= 5) {
            info.put("locationType", data[0] & 0xFF); // 位置类型
            info.put("areaId", parseDWORD(Arrays.copyOfRange(data, 1, 5))); // 区域或路段ID
        }
        return info;
    }
    
    /**
     * 解析进出区域/路线报警附加信息
     */
    private Map<String, Object> parseAreaRouteAlarmInfo(byte[] data) {
        Map<String, Object> info = new HashMap<>();
        if (data.length >= 6) {
            info.put("locationType", data[0] & 0xFF); // 位置类型
            info.put("areaId", parseDWORD(Arrays.copyOfRange(data, 1, 5))); // 区域或路段ID
            info.put("direction", data[5] & 0xFF); // 方向
        }
        return info;
    }
    
    /**
     * 解析路段行驶时间不足/过长报警附加信息
     */
    private Map<String, Object> parseRouteTimeAlarmInfo(byte[] data) {
        Map<String, Object> info = new HashMap<>();
        if (data.length >= 7) {
            info.put("routeId", parseDWORD(Arrays.copyOfRange(data, 0, 4))); // 路段ID
            info.put("driveTime", parseWORD(Arrays.copyOfRange(data, 4, 6))); // 路段行驶时间
            info.put("result", data[6] & 0xFF); // 结果
        }
        return info;
    }
    
    /**
     * 解析扩展车辆信号状态位
     */
    private Map<String, Boolean> parseExtendedVehicleSignalStatus(byte[] data) {
        Map<String, Boolean> status = new HashMap<>();
        if (data.length >= 4) {
            long statusBits = parseDWORD(data);
            status.put("lowBeam", (statusBits & 0x00000001) != 0); // 近光灯信号
            status.put("highBeam", (statusBits & 0x00000002) != 0); // 远光灯信号
            status.put("rightTurnSignal", (statusBits & 0x00000004) != 0); // 右转向灯信号
            status.put("leftTurnSignal", (statusBits & 0x00000008) != 0); // 左转向灯信号
            status.put("brake", (statusBits & 0x00000010) != 0); // 制动信号
            status.put("reverse", (statusBits & 0x00000020) != 0); // 倒车信号
            status.put("fogLight", (statusBits & 0x00000040) != 0); // 雾灯信号
            status.put("positionLight", (statusBits & 0x00000080) != 0); // 示廓灯
            status.put("horn", (statusBits & 0x00000100) != 0); // 喇叭信号
            status.put("airConditioner", (statusBits & 0x00000200) != 0); // 空调状态
            status.put("neutral", (statusBits & 0x00000400) != 0); // 空挡信号
            status.put("retarder", (statusBits & 0x00000800) != 0); // 缓速器工作
            status.put("abs", (statusBits & 0x00001000) != 0); // ABS工作
            status.put("heater", (statusBits & 0x00002000) != 0); // 加热器工作
            status.put("clutch", (statusBits & 0x00004000) != 0); // 离合器状态
        }
        return status;
    }
    
    /**
     * 解析IO状态位
     */
    private Map<String, Boolean> parseIOStatus(byte[] data) {
        Map<String, Boolean> status = new HashMap<>();
        if (data.length >= 2) {
            int statusBits = parseWORD(data);
            // 深度休眠状态
            status.put("deepSleep", (statusBits & 0x0001) != 0);
            // 休眠状态
            status.put("sleep", (statusBits & 0x0002) != 0);
        }
        return status;
    }
    
    /**
     * 解析模拟量
     */
    private Map<String, Integer> parseAnalogValue(byte[] data) {
        Map<String, Integer> values = new HashMap<>();
        if (data.length >= 4) {
            long analogBits = parseDWORD(data);
            values.put("AD0", (int)(analogBits & 0xFFFF)); // bit0-15，AD0
            values.put("AD1", (int)((analogBits >> 16) & 0xFFFF)); // bit16-31，AD1
        }
        return values;
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
        if (hasICCardFault()) alarms.add("道路运输证IC卡模块故障");
        if (hasSpeedingWarning()) alarms.add("超速预警");
        if (hasFatigueWarning()) alarms.add("疲劳驾驶预警");
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
        if (hasCollisionWarning()) alarms.add("碰撞预警");
        if (hasRolloverWarning()) alarms.add("侧翻预警");
        if (hasIllegalDoorOpenAlarm()) alarms.add("非法开门报警");
        return alarms;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("T0200LocationReport {\n");
        
        // 基本信息
        sb.append("  报警标志位: 0x").append(Integer.toHexString(alarmFlag).toUpperCase()).append("\n");
        sb.append("  状态标志位: 0x").append(Integer.toHexString(statusFlag).toUpperCase()).append("\n");
        sb.append("  纬度: ").append(latitude).append(" (").append(getLatitudeDegrees()).append("°)").append("\n");
        sb.append("  经度: ").append(longitude).append(" (").append(getLongitudeDegrees()).append("°)").append("\n");
        sb.append("  海拔高度: ").append(altitude).append("m\n");
        sb.append("  速度: ").append(speed).append(" (").append(getSpeedKmh()).append("km/h)\n");
        sb.append("  方向: ").append(direction).append("°\n");
        sb.append("  时间: ").append(dateTime).append("\n");
        
        // 状态位详细信息
        sb.append("  ACC开关: ").append(isACCOn() ? "开" : "关").append("\n");
        sb.append("  定位状态: ").append(isPositioned() ? "已定位" : "未定位").append("\n");
        sb.append("  纬度类型: ").append(isSouthLatitude() ? "南纬" : "北纬").append("\n");
        sb.append("  经度类型: ").append(isWestLongitude() ? "西经" : "东经").append("\n");
        sb.append("  运营状态: ").append(isOutOfService() ? "停运" : "运营").append("\n");
        sb.append("  坐标加密: ").append(isCoordinateEncrypted() ? "已加密" : "未加密").append("\n");
        
        // 载重状态
        String loadStatusDesc;
        switch (getLoadStatus()) {
            case 0: loadStatusDesc = "空车"; break;
            case 1: loadStatusDesc = "半载"; break;
            case 2: loadStatusDesc = "保留"; break;
            case 3: loadStatusDesc = "满载"; break;
            default: loadStatusDesc = "未知"; break;
        }
        sb.append("  载重状态: ").append(loadStatusDesc).append("\n");
        
        // 车辆状态
        sb.append("  油路状态: ").append(isOilCircuitDisconnected() ? "断开" : "正常").append("\n");
        sb.append("  电路状态: ").append(isElectricCircuitDisconnected() ? "断开" : "正常").append("\n");
        sb.append("  车门锁定: ").append(isDoorLocked() ? "加锁" : "解锁").append("\n");
        
        // 车门状态
        sb.append("  门1状态: ").append(isDoor1Open() ? "开" : "关").append("\n");
        sb.append("  门2状态: ").append(isDoor2Open() ? "开" : "关").append("\n");
        sb.append("  门3状态: ").append(isDoor3Open() ? "开" : "关").append("\n");
        sb.append("  门4状态: ").append(isDoor4Open() ? "开" : "关").append("\n");
        sb.append("  门5状态: ").append(isDoor5Open() ? "开" : "关").append("\n");
        
        // 定位系统状态
        sb.append("  GPS定位: ").append(isGPSPositioning() ? "使用" : "未使用").append("\n");
        sb.append("  北斗定位: ").append(isBeidouPositioning() ? "使用" : "未使用").append("\n");
        sb.append("  GLONASS定位: ").append(isGLONASSPositioning() ? "使用" : "未使用").append("\n");
        sb.append("  Galileo定位: ").append(isGalileoPositioning() ? "使用" : "未使用").append("\n");
        
        // 报警信息
        List<String> activeAlarms = getActiveAlarmDescriptions();
        if (activeAlarms.isEmpty()) {
            sb.append("  激活报警: 无\n");
        } else {
            sb.append("  激活报警: \n");
            for (String alarm : activeAlarms) {
                sb.append("    - ").append(alarm).append("\n");
            }
        }
        
        // 附加信息
        sb.append("  附加信息: ").append(additionalInfo != null ? additionalInfo.length() + " bytes" : "无").append("\n");
        
        // 解析后的附加信息
        if (parsedAdditionalInfo != null && !parsedAdditionalInfo.isEmpty()) {
            sb.append("  解析后的附加信息: \n");
            for (Map.Entry<Integer, Object> entry : parsedAdditionalInfo.entrySet()) {
                sb.append("    ID 0x").append(String.format("%02X", entry.getKey()))
                  .append(": ").append(entry.getValue().toString()).append("\n");
            }
        }
        
        sb.append("}");
        return sb.toString();
    }
}