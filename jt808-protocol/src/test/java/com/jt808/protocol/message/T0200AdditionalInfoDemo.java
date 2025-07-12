package com.jt808.protocol.message;

import com.jt808.protocol.message.additional.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * T0200位置信息汇报新附加信息架构演示
 */
public class T0200AdditionalInfoDemo {

    private static final Logger logger = LoggerFactory.getLogger(T0200AdditionalInfoDemo.class);

    public static void main(String[] args) {
        logger.info("=== T0200位置信息汇报新附加信息架构演示 ===");

        // 创建位置信息汇报对象
        T0200LocationReport report = new T0200LocationReport();

        // 设置基本位置信息
        report.setLatitude((int) (39.9042 * 1000000));  // 北京纬度，转换为微度
        report.setLongitude((int) (116.4074 * 1000000)); // 北京经度，转换为微度
        report.setSpeed(60);  // 速度，单位：0.1km/h
        report.setDirection(180);
        report.setAltitude(50);

        logger.info("\n1. 添加各种附加信息:");

        // 添加里程信息
        MileageInfo mileageInfo = new MileageInfo(12345.6);
        report.addAdditionalInfo(mileageInfo);
        logger.info("   添加里程信息: {} km", mileageInfo.getMileageKm());

        // 添加油量信息
        FuelInfo fuelInfo = new FuelInfo(45.8);
        report.addAdditionalInfo(fuelInfo);
        logger.info("   添加油量信息: {} L", fuelInfo.getFuelL());

        // 添加行驶记录速度
        RecordSpeedInfo speedInfo = new RecordSpeedInfo(62.3);
        report.addAdditionalInfo(speedInfo);
        logger.info("   添加行驶记录速度: {} km/h", speedInfo.getSpeedKmh());

        // 添加人工确认报警事件
        ManualAlarmEventInfo alarmInfo = new ManualAlarmEventInfo(12345);
        report.addAdditionalInfo(alarmInfo);
        logger.info("   添加人工确认报警事件ID: {}", alarmInfo.getEventId());

        // 添加超速报警信息
        OverspeedAlarmInfo overspeedInfo = new OverspeedAlarmInfo((byte) 1, 9001);
        report.addAdditionalInfo(overspeedInfo);
        logger.info("   添加超速报警信息: 位置类型={}, 区域ID={}",
                overspeedInfo.getLocationType(), overspeedInfo.getAreaId());

        // 添加模拟量信息
        AnalogQuantityInfo analogInfo = new AnalogQuantityInfo(1024, 2048);
        report.addAdditionalInfo(analogInfo);
        logger.info("   添加模拟量信息: AD0={}, AD1={}",
                analogInfo.getAD0(), analogInfo.getAD1());

        // 添加信号强度信息
        SignalStrengthInfo signalInfo = new SignalStrengthInfo((byte) -75);
        report.addAdditionalInfo(signalInfo);
        logger.info("   添加信号强度: {} dBm", signalInfo.getSignalStrength());

        // 添加卫星数信息
        SatelliteCountInfo satelliteInfo = new SatelliteCountInfo((byte) 12);
        report.addAdditionalInfo(satelliteInfo);
        logger.info("   添加卫星数: {}", satelliteInfo.getSatelliteCount());

        logger.info("\n2. 使用便利方法获取附加信息:");
        logger.info("   里程: {} km", report.getMileage());
        logger.info("   油量: {} L", report.getFuelLevel());
        logger.info("   行驶记录速度: {} km/h", report.getRecordSpeed());
        logger.info("   人工确认报警事件ID: {}", report.getManualAlarmEventId());
        logger.info("   信号强度: {} dBm", report.getSignalStrength());
        logger.info("   卫星数: {}", report.getSatelliteCount());

        logger.info("\n3. 检查附加信息存在性:");
        logger.info("   是否有里程信息: {}", report.hasAdditionalInfo(MileageInfo.class));
        logger.info("   是否有油量信息: {}", report.hasAdditionalInfo(FuelInfo.class));
        logger.info("   是否有IO状态信息: {}", report.hasAdditionalInfo(IOStatusInfo.class));

        logger.info("\n4. 通过ID和类型获取附加信息:");
        AdditionalInfo mileageById = report.getAdditionalInfoById(0x01);
        if (mileageById instanceof MileageInfo) {
            logger.info("   通过ID 0x01获取里程: {} km", ((MileageInfo) mileageById).getMileageKm());
        }

        FuelInfo fuelByType = report.getAdditionalInfoByType(FuelInfo.class);
        if (fuelByType != null) {
            logger.info("   通过类型获取油量: {} L", fuelByType.getFuelL());
        }

        logger.info("\n5. 附加信息列表:");
        logger.info("   总共有 {} 个附加信息项", report.getAdditionalInfoList().size());
        for (AdditionalInfo info : report.getAdditionalInfoList()) {
            logger.info("   - ID: 0x{}, 类型: {}, 描述: {}",
                    String.format("%02X", info.getId()),
                    info.getTypeName(),
                    info.getDescription());
        }

        logger.info("\n6. 移除附加信息演示:");
        logger.info("   移除前附加信息数量: {}", report.getAdditionalInfoList().size());

        // 移除里程信息
        boolean removed = report.removeAdditionalInfo(MileageInfo.class);
        logger.info("   移除里程信息: {}", removed ? "成功" : "失败");
        logger.info("   移除后附加信息数量: {}", report.getAdditionalInfoList().size());
        logger.info("   里程信息是否还存在: {}", report.hasAdditionalInfo(MileageInfo.class));

        logger.info("\n7. 完整的toString输出:");
        logger.info("\n{}", report.toString());

        logger.info("\n=== 演示完成 ===");
    }
}