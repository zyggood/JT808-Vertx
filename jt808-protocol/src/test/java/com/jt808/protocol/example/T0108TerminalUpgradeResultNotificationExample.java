package com.jt808.protocol.example;

import com.jt808.protocol.message.T0108TerminalUpgradeResultNotification;
import io.vertx.core.buffer.Buffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * T0108终端升级结果通知消息示例
 * <p>
 * 该示例演示了如何使用T0108TerminalUpgradeResultNotification类来创建、编码和解码
 * 终端升级结果通知消息。
 *
 * @author JT808 Protocol Team
 * @version 1.0
 */
public class T0108TerminalUpgradeResultNotificationExample {

    private static final Logger logger = LoggerFactory.getLogger(T0108TerminalUpgradeResultNotificationExample.class);

    public static void main(String[] args) {
        logger.info("=== T0108终端升级结果通知消息示例 ===");

        // 示例1：使用构造函数创建消息
        demonstrateConstructorUsage();

        // 示例2：使用静态工厂方法创建消息
        demonstrateStaticFactoryMethods();

        // 示例3：消息编解码示例
        demonstrateEncodeDecodeProcess();

        // 示例4：所有升级类型和结果的组合示例
        demonstrateAllCombinations();
    }

    /**
     * 演示构造函数的使用
     */
    private static void demonstrateConstructorUsage() {
        logger.info("\n--- 构造函数使用示例 ---");

        // 默认构造函数
        T0108TerminalUpgradeResultNotification defaultMsg = new T0108TerminalUpgradeResultNotification();
        logger.info("默认消息: {}", defaultMsg);

        // 带参数构造函数
        T0108TerminalUpgradeResultNotification paramMsg = new T0108TerminalUpgradeResultNotification(
                T0108TerminalUpgradeResultNotification.UPGRADE_TYPE_IC_CARD_READER,
                T0108TerminalUpgradeResultNotification.UPGRADE_RESULT_SUCCESS
        );
        logger.info("参数化消息: {}", paramMsg);
    }

    /**
     * 演示静态工厂方法的使用
     */
    private static void demonstrateStaticFactoryMethods() {
        logger.info("\n--- 静态工厂方法示例 ---");

        // 终端升级结果
        logger.info("终端升级成功: {}", T0108TerminalUpgradeResultNotification.createTerminalUpgradeSuccess());
        logger.info("终端升级失败: {}", T0108TerminalUpgradeResultNotification.createTerminalUpgradeFailure());
        logger.info("终端升级取消: {}", T0108TerminalUpgradeResultNotification.createTerminalUpgradeCancel());

        // IC卡读卡器升级结果
        logger.info("IC卡读卡器升级成功: {}", T0108TerminalUpgradeResultNotification.createIcCardReaderUpgradeSuccess());
        logger.info("IC卡读卡器升级失败: {}", T0108TerminalUpgradeResultNotification.createIcCardReaderUpgradeFailure());
        logger.info("IC卡读卡器升级取消: {}", T0108TerminalUpgradeResultNotification.createIcCardReaderUpgradeCancel());

        // 北斗模块升级结果
        logger.info("北斗模块升级成功: {}", T0108TerminalUpgradeResultNotification.createBeidouModuleUpgradeSuccess());
        logger.info("北斗模块升级失败: {}", T0108TerminalUpgradeResultNotification.createBeidouModuleUpgradeFailure());
        logger.info("北斗模块升级取消: {}", T0108TerminalUpgradeResultNotification.createBeidouModuleUpgradeCancel());
    }

    /**
     * 演示消息编解码过程
     */
    private static void demonstrateEncodeDecodeProcess() {
        logger.info("\n--- 编解码过程示例 ---");

        // 创建原始消息
        T0108TerminalUpgradeResultNotification originalMessage =
                T0108TerminalUpgradeResultNotification.createBeidouModuleUpgradeFailure();

        logger.info("原始消息: {}", originalMessage);
        logger.info("消息ID: 0x{}", Integer.toHexString(originalMessage.getMessageId()).toUpperCase());

        // 编码消息
        Buffer encodedBuffer = originalMessage.encodeBody();
        logger.info("编码后的字节数据: {}", bytesToHexString(encodedBuffer.getBytes()));
        logger.info("编码后的数据长度: {} 字节", encodedBuffer.length());

        // 解码消息
        T0108TerminalUpgradeResultNotification decodedMessage = new T0108TerminalUpgradeResultNotification();
        decodedMessage.decodeBody(encodedBuffer);

        logger.info("解码后的消息: {}", decodedMessage);

        // 验证编解码一致性
        boolean isConsistent = originalMessage.getUpgradeType() == decodedMessage.getUpgradeType() &&
                originalMessage.getUpgradeResult() == decodedMessage.getUpgradeResult();
        logger.info("编解码一致性检查: {}", (isConsistent ? "通过" : "失败"));
    }

    /**
     * 演示所有升级类型和结果的组合
     */
    private static void demonstrateAllCombinations() {
        logger.info("\n--- 所有升级类型和结果组合示例 ---");

        byte[] upgradeTypes = {
                T0108TerminalUpgradeResultNotification.UPGRADE_TYPE_TERMINAL,
                T0108TerminalUpgradeResultNotification.UPGRADE_TYPE_IC_CARD_READER,
                T0108TerminalUpgradeResultNotification.UPGRADE_TYPE_BEIDOU_MODULE
        };

        byte[] upgradeResults = {
                T0108TerminalUpgradeResultNotification.UPGRADE_RESULT_SUCCESS,
                T0108TerminalUpgradeResultNotification.UPGRADE_RESULT_FAILURE,
                T0108TerminalUpgradeResultNotification.UPGRADE_RESULT_CANCEL
        };

        logger.info("升级类型和结果的所有组合:");
        for (byte upgradeType : upgradeTypes) {
            for (byte upgradeResult : upgradeResults) {
                T0108TerminalUpgradeResultNotification message =
                        new T0108TerminalUpgradeResultNotification(upgradeType, upgradeResult);

                Buffer encoded = message.encodeBody();
                logger.info("类型: {}, 结果: {}, 编码: {}",
                        message.getUpgradeTypeDescription(),
                        message.getUpgradeResultDescription(),
                        bytesToHexString(encoded.getBytes())
                );
            }
        }
    }

    /**
     * 将字节数组转换为十六进制字符串
     *
     * @param bytes 字节数组
     * @return 十六进制字符串
     */
    private static String bytesToHexString(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X ", b & 0xFF));
        }
        return sb.toString().trim();
    }
}