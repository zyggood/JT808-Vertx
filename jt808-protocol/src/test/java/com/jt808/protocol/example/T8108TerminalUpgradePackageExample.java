package com.jt808.protocol.example;

import com.jt808.protocol.factory.JT808MessageFactory;
import com.jt808.protocol.message.JT808Message;
import com.jt808.protocol.message.T8108TerminalUpgradePackage;
import io.vertx.core.buffer.Buffer;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.*;

/**
 * T8108下发终端升级包消息使用示例
 * <p>
 * 该示例演示了如何创建、编码、解码和使用T8108下发终端升级包消息。
 * T8108是平台向终端发送升级包数据的指令，包含升级类型、制造商ID、版本号和升级数据包。
 */
public class T8108TerminalUpgradePackageExample {

    private static final Logger logger = LoggerFactory.getLogger(T8108TerminalUpgradePackageExample.class);

    /**
     * 主方法，用于独立运行示例
     */
    public static void main(String[] args) {
        T8108TerminalUpgradePackageExample example = new T8108TerminalUpgradePackageExample();
        example.demonstrateT8108Usage();
    }

    @Test
    public void demonstrateT8108Usage() {
        logger.info("=== T8108下发终端升级包消息功能演示 ===");

        // 1. 使用工厂创建消息
        logger.info("\n1. 使用工厂创建T8108消息");
        JT808MessageFactory factory = JT808MessageFactory.getInstance();
        JT808Message factoryMessage = factory.createMessage(0x8108);
        assertInstanceOf(T8108TerminalUpgradePackage.class, factoryMessage);
        logger.info("工厂创建成功: {}", factoryMessage.getClass().getSimpleName());

        // 2. 创建终端升级包消息
        logger.info("\n2. 创建终端升级包消息");
        demonstrateTerminalUpgrade();

        // 3. 创建IC卡读卡器升级包消息
        logger.info("\n3. 创建IC卡读卡器升级包消息");
        demonstrateIcCardReaderUpgrade();

        // 4. 创建北斗模块升级包消息
        logger.info("\n4. 创建北斗模块升级包消息");
        demonstrateBeidouModuleUpgrade();

        // 5. 演示消息编解码
        logger.info("\n5. 演示消息编解码");
        demonstrateEncodeDecodeProcess();

        // 6. 演示大数据包处理
        logger.info("\n6. 演示大数据包处理");
        demonstrateLargeDataPackage();

        logger.info("\n=== T8108消息示例演示完成 ===");
    }

    /**
     * 演示终端升级包创建
     */
    private void demonstrateTerminalUpgrade() {
        // 制造商ID (5字节)
        byte[] manufacturerId = {0x01, 0x02, 0x03, 0x04, 0x05};

        // 版本号
        String version = "V2.1.0";

        // 模拟升级数据包 (实际应用中这会是固件文件的内容)
        byte[] upgradeData = {
                0x7E, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07,
                0x08, 0x09, 0x0A, 0x0B, 0x0C, 0x0D, 0x0E, 0x0F
        };

        // 使用静态工厂方法创建
        T8108TerminalUpgradePackage upgradeMessage = T8108TerminalUpgradePackage.createTerminalUpgrade(
                manufacturerId, version, upgradeData);

        logger.info("终端升级包消息创建成功:");
        logger.info("  升级类型: {}", upgradeMessage.getUpgradeTypeDescription());
        logger.info("  制造商ID: {}", bytesToHex(upgradeMessage.getManufacturerId()));
        logger.info("  版本号: {}", upgradeMessage.getVersion());
        logger.info("  版本号长度: {} 字节", upgradeMessage.getVersionLength());
        logger.info("  升级数据长度: {} 字节", upgradeMessage.getUpgradeDataLength());
        logger.info("  升级数据预览: {}...", bytesToHex(upgradeMessage.getUpgradeData(), 8));

        // 验证消息
        assertEquals(0x8108, upgradeMessage.getMessageId());
        assertEquals(T8108TerminalUpgradePackage.UPGRADE_TYPE_TERMINAL, upgradeMessage.getUpgradeType());
        assertArrayEquals(manufacturerId, upgradeMessage.getManufacturerId());
        assertEquals(version, upgradeMessage.getVersion());
        assertEquals(upgradeData.length, upgradeMessage.getUpgradeDataLength());
        assertArrayEquals(upgradeData, upgradeMessage.getUpgradeData());
    }

    /**
     * 演示IC卡读卡器升级包创建
     */
    private void demonstrateIcCardReaderUpgrade() {
        byte[] manufacturerId = {(byte) 0xAA, (byte) 0xBB, (byte) 0xCC, (byte) 0xDD, (byte) 0xEE};
        String version = "IC_V1.5.2";
        byte[] upgradeData = generateMockFirmwareData(256); // 256字节的模拟固件数据

        T8108TerminalUpgradePackage upgradeMessage = T8108TerminalUpgradePackage.createIcCardReaderUpgrade(
                manufacturerId, version, upgradeData);

        logger.info("IC卡读卡器升级包消息创建成功:");
        logger.info("  升级类型: {}", upgradeMessage.getUpgradeTypeDescription());
        logger.info("  制造商ID: {}", bytesToHex(upgradeMessage.getManufacturerId()));
        logger.info("  版本号: {}", upgradeMessage.getVersion());
        logger.info("  升级数据长度: {} 字节", upgradeMessage.getUpgradeDataLength());

        assertEquals(T8108TerminalUpgradePackage.UPGRADE_TYPE_IC_CARD_READER, upgradeMessage.getUpgradeType());
        assertEquals("道路运输证IC卡读卡器", upgradeMessage.getUpgradeTypeDescription());
    }

    /**
     * 演示北斗模块升级包创建
     */
    private void demonstrateBeidouModuleUpgrade() {
        byte[] manufacturerId = {0x11, 0x22, 0x33, 0x44, 0x55};
        String version = "BD_V3.0.1";
        byte[] upgradeData = generateMockFirmwareData(512); // 512字节的模拟固件数据

        T8108TerminalUpgradePackage upgradeMessage = T8108TerminalUpgradePackage.createBeidouModuleUpgrade(
                manufacturerId, version, upgradeData);

        logger.info("北斗模块升级包消息创建成功:");
        logger.info("  升级类型: {}", upgradeMessage.getUpgradeTypeDescription());
        logger.info("  制造商ID: {}", bytesToHex(upgradeMessage.getManufacturerId()));
        logger.info("  版本号: {}", upgradeMessage.getVersion());
        logger.info("  升级数据长度: {} 字节", upgradeMessage.getUpgradeDataLength());

        assertEquals(T8108TerminalUpgradePackage.UPGRADE_TYPE_BEIDOU_MODULE, upgradeMessage.getUpgradeType());
        assertEquals("北斗卫星定位模块", upgradeMessage.getUpgradeTypeDescription());
    }

    /**
     * 演示消息编解码过程
     */
    private void demonstrateEncodeDecodeProcess() {
        // 创建原始消息
        byte[] manufacturerId = {0x12, 0x34, 0x56, 0x78, (byte) 0x9A};
        String version = "TEST_V1.0";
        byte[] upgradeData = {0x01, 0x02, 0x03, 0x04, 0x05};

        T8108TerminalUpgradePackage originalMessage = new T8108TerminalUpgradePackage(
                T8108TerminalUpgradePackage.UPGRADE_TYPE_TERMINAL,
                manufacturerId,
                version,
                upgradeData
        );

        logger.info("原始消息: {}", originalMessage);

        // 编码消息体
        Buffer encodedBody = originalMessage.encodeBody();
        logger.info("编码后的消息体长度: {} 字节", encodedBody.length());
        logger.info("编码后的消息体内容: {}", bytesToHex(encodedBody.getBytes()));

        // 解码消息体
        T8108TerminalUpgradePackage decodedMessage = new T8108TerminalUpgradePackage();
        decodedMessage.decodeBody(encodedBody);

        logger.info("解码后的消息: {}", decodedMessage);

        // 验证编解码一致性
        assertEquals(originalMessage.getUpgradeType(), decodedMessage.getUpgradeType());
        assertArrayEquals(originalMessage.getManufacturerId(), decodedMessage.getManufacturerId());
        assertEquals(originalMessage.getVersion(), decodedMessage.getVersion());
        assertEquals(originalMessage.getUpgradeDataLength(), decodedMessage.getUpgradeDataLength());
        assertArrayEquals(originalMessage.getUpgradeData(), decodedMessage.getUpgradeData());

        logger.info("编解码一致性验证通过!");
    }

    /**
     * 演示大数据包处理
     */
    private void demonstrateLargeDataPackage() {
        byte[] manufacturerId = {0x01, 0x02, 0x03, 0x04, 0x05};
        String version = "LARGE_V1.0";

        // 创建一个较大的升级数据包 (1KB)
        byte[] largeUpgradeData = generateMockFirmwareData(1024);

        T8108TerminalUpgradePackage largeMessage = T8108TerminalUpgradePackage.createTerminalUpgrade(
                manufacturerId, version, largeUpgradeData);

        logger.info("大数据包消息创建成功:");
        logger.info("  升级类型: {}", largeMessage.getUpgradeTypeDescription());
        logger.info("  版本号: {}", largeMessage.getVersion());
        logger.info("  升级数据长度: {} 字节 ({}KB)",
                largeMessage.getUpgradeDataLength(),
                largeMessage.getUpgradeDataLength() / 1024.0);

        // 编码和解码测试
        Buffer encodedLarge = largeMessage.encodeBody();
        logger.info("编码后总长度: {} 字节", encodedLarge.length());

        T8108TerminalUpgradePackage decodedLarge = new T8108TerminalUpgradePackage();
        decodedLarge.decodeBody(encodedLarge);

        // 验证大数据包的一致性
        assertEquals(largeMessage.getUpgradeDataLength(), decodedLarge.getUpgradeDataLength());
        assertArrayEquals(largeMessage.getUpgradeData(), decodedLarge.getUpgradeData());

        logger.info("大数据包编解码验证通过!");
    }

    /**
     * 生成模拟固件数据
     */
    private byte[] generateMockFirmwareData(int size) {
        byte[] data = new byte[size];
        for (int i = 0; i < size; i++) {
            data[i] = (byte) (i % 256);
        }
        return data;
    }

    /**
     * 字节数组转十六进制字符串
     */
    private String bytesToHex(byte[] bytes) {
        return bytesToHex(bytes, bytes.length);
    }

    /**
     * 字节数组转十六进制字符串 (限制长度)
     */
    private String bytesToHex(byte[] bytes, int maxLength) {
        if (bytes == null) return "null";

        StringBuilder sb = new StringBuilder();
        int length = Math.min(bytes.length, maxLength);

        for (int i = 0; i < length; i++) {
            if (i > 0) sb.append(" ");
            sb.append(String.format("%02X", bytes[i] & 0xFF));
        }

        if (bytes.length > maxLength) {
            sb.append("...");
        }

        return sb.toString();
    }
}