package com.jt808.protocol.example;

import com.jt808.protocol.message.T8301EventSetting;
import io.vertx.core.buffer.Buffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * T8301事件设置消息功能演示
 */
public class T8301EventSettingExample {
    private static final Logger logger = LoggerFactory.getLogger(T8301EventSettingExample.class);

    public static void main(String[] args) {
        T8301EventSettingExample example = new T8301EventSettingExample();
        example.demonstrateT8301Usage();
    }

    public void demonstrateT8301Usage() {
        logger.info("=== T8301事件设置消息功能演示 ===");

        demonstrateBasicUsage();
        demonstrateFactoryMethods();
        demonstrateEncodeDecode();
        demonstrateSettingTypes();
        demonstrateRealWorldScenarios();
        demonstrateExceptionHandling();

        logger.info("=== T8301事件设置消息演示完成 ===");
    }

    private void demonstrateBasicUsage() {
        logger.info("\n--- 1. 基本使用方法 ---");

        // 创建事件设置消息
        T8301EventSetting message = new T8301EventSetting();
        message.setSettingType(T8301EventSetting.SettingType.UPDATE);

        // 添加事件项
        message.addEventItem((byte) 1, "紧急事件处理");
        message.addEventItem((byte) 2, "车辆故障报警");
        message.addEventItem((byte) 3, "定期维护提醒");

        logger.info("消息ID: 0x" + Integer.toHexString(message.getMessageId()).toUpperCase());
        logger.info("设置类型: " + message.getSettingTypeDescription());
        logger.info("事件总数: " + message.getEventCount());

        // 显示事件项详情
        for (T8301EventSetting.EventItem item : message.getEventItems()) {
            logger.info("事件ID: " + (item.getEventId() & 0xFF) + ", 内容: '" +
                    item.getContentString() + "', 长度: " + item.getContentLength() + "字节");
        }
    }

    private void demonstrateFactoryMethods() {
        logger.info("\n--- 2. 工厂方法使用 ---");

        // 删除所有事件
        T8301EventSetting deleteAll = T8301EventSetting.createDeleteAll();
        logger.info("删除所有事件: " + deleteAll.getSettingTypeDescription());

        // 更新事件
        T8301EventSetting update = T8301EventSetting.createUpdate();
        update.addEventItem((byte) 10, "更新的事件内容");
        logger.info("更新事件: " + update.getSettingTypeDescription() + ", 事件数量: " + update.getEventCount());

        // 追加事件
        T8301EventSetting append = T8301EventSetting.createAppend();
        append.addEventItem((byte) 20, "新增的事件内容");
        logger.info("追加事件: " + append.getSettingTypeDescription() + ", 事件数量: " + append.getEventCount());

        // 修改事件
        T8301EventSetting modify = T8301EventSetting.createModify();
        modify.addEventItem((byte) 30, "修改后的事件内容");
        logger.info("修改事件: " + modify.getSettingTypeDescription() + ", 事件数量: " + modify.getEventCount());

        // 删除特定事件
        T8301EventSetting deleteSpecific = T8301EventSetting.createDeleteSpecific();
        deleteSpecific.addEventId((byte) 1);
        deleteSpecific.addEventId((byte) 3);
        deleteSpecific.addEventId((byte) 5);
        logger.info("删除特定事件: " + deleteSpecific.getSettingTypeDescription() +
                ", 要删除的事件数量: " + deleteSpecific.getEventCount());
    }

    private void demonstrateEncodeDecode() {
        logger.info("\n--- 3. 编解码演示 ---");

        // 创建原始消息
        T8301EventSetting original = T8301EventSetting.createUpdate();
        original.addEventItem((byte) 1, "紧急制动事件");
        original.addEventItem((byte) 2, "发动机故障");
        original.addEventItem((byte) 3, "GPS信号丢失");

        logger.info("原始消息: " + original);

        // 编码消息体
        Buffer encoded = original.encodeBody();
        logger.info("编码后数据长度: " + encoded.length() + " 字节");
        logger.info("编码后数据: " + bytesToHex(encoded.getBytes()));

        // 解码消息体
        T8301EventSetting decoded = new T8301EventSetting();
        decoded.decodeBody(encoded);

        logger.info("解码后消息: " + decoded);
        logger.info("编解码一致性: " + original.equals(decoded));

        // 验证每个事件项
        for (T8301EventSetting.EventItem originalItem : original.getEventItems()) {
            T8301EventSetting.EventItem decodedItem = decoded.getEventItem(originalItem.getEventId());
            logger.info("事件ID " + (originalItem.getEventId() & 0xFF) + ": 原始='" +
                    originalItem.getContentString() + "', 解码='" +
                    (decodedItem != null ? decodedItem.getContentString() : "null") +
                    "', 一致性=" + originalItem.equals(decodedItem));
        }
    }

    private void demonstrateSettingTypes() {
        logger.info("\n--- 4. 设置类型处理 ---");

        byte[] settingTypes = {
                T8301EventSetting.SettingType.DELETE_ALL,
                T8301EventSetting.SettingType.UPDATE,
                T8301EventSetting.SettingType.APPEND,
                T8301EventSetting.SettingType.MODIFY,
                T8301EventSetting.SettingType.DELETE_SPECIFIC
        };

        for (byte settingType : settingTypes) {
            T8301EventSetting message = new T8301EventSetting(settingType);

            logger.info("设置类型 " + settingType + ": " + message.getSettingTypeDescription());
            logger.info("  - 是否删除所有: " + message.isDeleteAll());
            logger.info("  - 是否更新: " + message.isUpdate());
            logger.info("  - 是否追加: " + message.isAppend());
            logger.info("  - 是否修改: " + message.isModify());
            logger.info("  - 是否删除特定: " + message.isDeleteSpecific());

            // 测试编码（删除所有事件的特殊情况）
            if (settingType == T8301EventSetting.SettingType.DELETE_ALL) {
                Buffer encoded = message.encodeBody();
                logger.info("  - 删除所有事件编码长度: " + encoded.length() + " 字节");
            } else if (settingType == T8301EventSetting.SettingType.DELETE_SPECIFIC) {
                message.addEventId((byte) 1);
                message.addEventId((byte) 2);
                Buffer encoded = message.encodeBody();
                logger.info("  - 删除特定事件编码长度: " + encoded.length() + " 字节 (包含" +
                        message.getEventCount() + "个事件ID)");
            } else {
                message.addEventItem((byte) 1, "测试事件");
                Buffer encoded = message.encodeBody();
                logger.info("  - 带事件内容编码长度: " + encoded.length() + " 字节");
            }
        }
    }

    private void demonstrateRealWorldScenarios() {
        logger.info("\n--- 5. 实际应用场景 ---");

        // 场景1: 初始化车辆事件配置
        logger.info("场景1: 初始化车辆事件配置");
        T8301EventSetting initConfig = T8301EventSetting.createUpdate();
        initConfig.addEventItem((byte) 1, "紧急制动触发时，立即上报位置并启动录音");
        initConfig.addEventItem((byte) 2, "发动机故障时，记录故障码并限制车速");
        initConfig.addEventItem((byte) 3, "超速行驶时，语音提醒并记录违规信息");
        initConfig.addEventItem((byte) 4, "疲劳驾驶检测时，强制休息提醒");
        initConfig.addEventItem((byte) 5, "车门异常打开时，立即报警");

        logger.info("初始化配置包含 " + initConfig.getEventCount() + " 个事件");
        Buffer initEncoded = initConfig.encodeBody();
        logger.info("配置数据大小: " + initEncoded.length() + " 字节");

        // 场景2: 追加新的安全事件
        logger.info("\n场景2: 追加新的安全事件");
        T8301EventSetting addSafety = T8301EventSetting.createAppend();
        addSafety.addEventItem((byte) 10, "安全带未系提醒");
        addSafety.addEventItem((byte) 11, "手机使用检测报警");
        addSafety.addEventItem((byte) 12, "车道偏离预警");

        logger.info("追加 " + addSafety.getEventCount() + " 个安全事件");

        // 场景3: 修改现有事件内容
        logger.info("\n场景3: 修改现有事件内容");
        T8301EventSetting modifyEvent = T8301EventSetting.createModify();
        modifyEvent.addEventItem((byte) 2, "发动机故障时，记录详细故障码、限制车速至60km/h并寻找最近维修点");

        logger.info("修改事件ID 2 的处理流程");

        // 场景4: 删除过时的事件
        logger.info("\n场景4: 删除过时的事件");
        T8301EventSetting removeOld = T8301EventSetting.createDeleteSpecific();
        removeOld.addEventId((byte) 5); // 删除车门事件
        removeOld.addEventId((byte) 10); // 删除安全带事件

        logger.info("删除 " + removeOld.getEventCount() + " 个过时事件");

        // 场景5: 重置所有事件配置
        logger.info("\n场景5: 重置所有事件配置");
        T8301EventSetting resetAll = T8301EventSetting.createDeleteAll();
        logger.info("清空所有现有事件配置");
    }

    private void demonstrateExceptionHandling() {
        logger.info("\n--- 6. 异常处理演示 ---");

        try {
            // 测试超长内容
            T8301EventSetting message = T8301EventSetting.createUpdate();
            StringBuilder longContent = new StringBuilder();
            for (int i = 0; i < 300; i++) {
                longContent.append("测试");
            }
            message.addEventItem((byte) 1, longContent.toString());
        } catch (Exception e) {
            logger.info("捕获超长内容异常: " + e.getMessage());
        }

        try {
            // 测试空缓冲区解码
            T8301EventSetting message = new T8301EventSetting();
            message.decodeBody(Buffer.buffer());
        } catch (Exception e) {
            logger.info("捕获空缓冲区解码异常: " + e.getMessage());
        }

        try {
            // 测试null缓冲区解码
            T8301EventSetting message = new T8301EventSetting();
            message.decodeBody(null);
        } catch (Exception e) {
            logger.info("捕获null缓冲区解码异常: " + e.getMessage());
        }

        logger.info("异常处理演示完成");
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02X ", b));
        }
        return result.toString().trim();
    }
}