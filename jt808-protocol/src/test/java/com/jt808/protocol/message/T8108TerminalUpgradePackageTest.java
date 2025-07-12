package com.jt808.protocol.message;

import com.jt808.protocol.factory.JT808MessageFactory;
import io.vertx.core.buffer.Buffer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("T8108下发终端升级包消息测试")
class T8108TerminalUpgradePackageTest {

    private T8108TerminalUpgradePackage message;
    private JT808MessageFactory factory;

    @BeforeEach
    void setUp() {
        message = new T8108TerminalUpgradePackage();
        factory = JT808MessageFactory.getInstance();
    }

    @Test
    @DisplayName("测试消息ID")
    void testMessageId() {
        assertEquals(0x8108, message.getMessageId());
    }

    @Test
    @DisplayName("测试默认构造函数")
    void testDefaultConstructor() {
        assertNotNull(message.getManufacturerId());
        assertEquals(5, message.getManufacturerId().length);
        assertEquals(0, message.getVersionLength());
        assertNull(message.getVersion());
        assertEquals(0, message.getUpgradeDataLength());
        assertNull(message.getUpgradeData());
    }

    @Test
    @DisplayName("测试带参数构造函数")
    void testParameterizedConstructor() {
        byte[] manufacturerId = {0x01, 0x02, 0x03, 0x04, 0x05};
        String version = "V1.0.0";
        byte[] upgradeData = {0x10, 0x20, 0x30, 0x40};

        T8108TerminalUpgradePackage msg = new T8108TerminalUpgradePackage(
                T8108TerminalUpgradePackage.UPGRADE_TYPE_TERMINAL,
                manufacturerId,
                version,
                upgradeData
        );

        assertEquals(T8108TerminalUpgradePackage.UPGRADE_TYPE_TERMINAL, msg.getUpgradeType());
        assertArrayEquals(manufacturerId, msg.getManufacturerId());
        assertEquals(version, msg.getVersion());
        assertEquals(version.getBytes().length, msg.getVersionLength());
        assertArrayEquals(upgradeData, msg.getUpgradeData());
        assertEquals(upgradeData.length, msg.getUpgradeDataLength());
    }

    @Test
    @DisplayName("测试升级类型常量")
    void testUpgradeTypeConstants() {
        assertEquals(0x00, T8108TerminalUpgradePackage.UPGRADE_TYPE_TERMINAL);
        assertEquals(0x12, T8108TerminalUpgradePackage.UPGRADE_TYPE_IC_CARD_READER);
        assertEquals(0x52, T8108TerminalUpgradePackage.UPGRADE_TYPE_BEIDOU_MODULE);
    }

    @Test
    @DisplayName("测试升级类型描述")
    void testUpgradeTypeDescription() {
        message.setUpgradeType(T8108TerminalUpgradePackage.UPGRADE_TYPE_TERMINAL);
        assertEquals("终端", message.getUpgradeTypeDescription());

        message.setUpgradeType(T8108TerminalUpgradePackage.UPGRADE_TYPE_IC_CARD_READER);
        assertEquals("道路运输证IC卡读卡器", message.getUpgradeTypeDescription());

        message.setUpgradeType(T8108TerminalUpgradePackage.UPGRADE_TYPE_BEIDOU_MODULE);
        assertEquals("北斗卫星定位模块", message.getUpgradeTypeDescription());

        message.setUpgradeType((byte) 0xFF);
        assertEquals("未知类型(255)", message.getUpgradeTypeDescription());
    }

    @Test
    @DisplayName("测试静态工厂方法")
    void testStaticFactoryMethods() {
        byte[] manufacturerId = {0x01, 0x02, 0x03, 0x04, 0x05};
        String version = "V1.0.0";
        byte[] upgradeData = {0x10, 0x20, 0x30};

        // 测试终端升级
        T8108TerminalUpgradePackage terminalUpgrade = T8108TerminalUpgradePackage.createTerminalUpgrade(
                manufacturerId, version, upgradeData);
        assertEquals(T8108TerminalUpgradePackage.UPGRADE_TYPE_TERMINAL, terminalUpgrade.getUpgradeType());

        // 测试IC卡读卡器升级
        T8108TerminalUpgradePackage icCardUpgrade = T8108TerminalUpgradePackage.createIcCardReaderUpgrade(
                manufacturerId, version, upgradeData);
        assertEquals(T8108TerminalUpgradePackage.UPGRADE_TYPE_IC_CARD_READER, icCardUpgrade.getUpgradeType());

        // 测试北斗模块升级
        T8108TerminalUpgradePackage beidouUpgrade = T8108TerminalUpgradePackage.createBeidouModuleUpgrade(
                manufacturerId, version, upgradeData);
        assertEquals(T8108TerminalUpgradePackage.UPGRADE_TYPE_BEIDOU_MODULE, beidouUpgrade.getUpgradeType());
    }

    @Test
    @DisplayName("测试消息编码")
    void testEncodeBody() {
        // 设置测试数据
        message.setUpgradeType(T8108TerminalUpgradePackage.UPGRADE_TYPE_TERMINAL);
        message.setManufacturerId(new byte[]{0x01, 0x02, 0x03, 0x04, 0x05});
        message.setVersion("V1.0");
        message.setUpgradeData(new byte[]{0x10, 0x20});

        Buffer encoded = message.encodeBody();

        // 验证编码结果
        assertNotNull(encoded);

        int index = 0;
        // 升级类型 (1字节)
        assertEquals(T8108TerminalUpgradePackage.UPGRADE_TYPE_TERMINAL, encoded.getByte(index));
        index += 1;

        // 制造商ID (5字节)
        byte[] manufacturerId = encoded.getBytes(index, index + 5);
        assertArrayEquals(new byte[]{0x01, 0x02, 0x03, 0x04, 0x05}, manufacturerId);
        index += 5;

        // 版本号长度 (1字节)
        assertEquals(4, encoded.getByte(index)); // "V1.0".length = 4
        index += 1;

        // 版本号 (4字节)
        String version = new String(encoded.getBytes(index, index + 4));
        assertEquals("V1.0", version);
        index += 4;

        // 升级数据包长度 (4字节)
        assertEquals(2, encoded.getUnsignedInt(index));
        index += 4;

        // 升级数据包 (2字节)
        byte[] upgradeData = encoded.getBytes(index, index + 2);
        assertArrayEquals(new byte[]{0x10, 0x20}, upgradeData);
    }

    @Test
    @DisplayName("测试消息解码")
    void testDecodeBody() {
        // 构造测试数据
        Buffer buffer = Buffer.buffer();
        buffer.appendByte(T8108TerminalUpgradePackage.UPGRADE_TYPE_IC_CARD_READER); // 升级类型
        buffer.appendBytes(new byte[]{0x11, 0x22, 0x33, 0x44, 0x55}); // 制造商ID
        buffer.appendByte((byte) 6); // 版本号长度
        buffer.appendBytes("V2.0.1".getBytes()); // 版本号
        buffer.appendUnsignedInt(3); // 升级数据包长度
        buffer.appendBytes(new byte[]{(byte) 0xAA, (byte) 0xBB, (byte) 0xCC}); // 升级数据包

        // 解码
        message.decodeBody(buffer);

        // 验证解码结果
        assertEquals(T8108TerminalUpgradePackage.UPGRADE_TYPE_IC_CARD_READER, message.getUpgradeType());
        assertArrayEquals(new byte[]{0x11, 0x22, 0x33, 0x44, 0x55}, message.getManufacturerId());
        assertEquals(6, message.getVersionLength());
        assertEquals("V2.0.1", message.getVersion());
        assertEquals(3, message.getUpgradeDataLength());
        assertArrayEquals(new byte[]{(byte) 0xAA, (byte) 0xBB, (byte) 0xCC}, message.getUpgradeData());
    }

    @Test
    @DisplayName("测试编解码往返一致性")
    void testEncodeDecodeRoundTrip() {
        // 设置原始数据
        T8108TerminalUpgradePackage original = new T8108TerminalUpgradePackage();
        original.setUpgradeType(T8108TerminalUpgradePackage.UPGRADE_TYPE_BEIDOU_MODULE);
        original.setManufacturerId(new byte[]{0x01, 0x02, 0x03, 0x04, 0x05});
        original.setVersion("V3.1.4");
        original.setUpgradeData(new byte[]{0x10, 0x20, 0x30, 0x40, 0x50});

        // 编码
        Buffer encoded = original.encodeBody();

        // 解码
        T8108TerminalUpgradePackage decoded = new T8108TerminalUpgradePackage();
        decoded.decodeBody(encoded);

        // 验证一致性
        assertEquals(original.getUpgradeType(), decoded.getUpgradeType());
        assertArrayEquals(original.getManufacturerId(), decoded.getManufacturerId());
        assertEquals(original.getVersionLength(), decoded.getVersionLength());
        assertEquals(original.getVersion(), decoded.getVersion());
        assertEquals(original.getUpgradeDataLength(), decoded.getUpgradeDataLength());
        assertArrayEquals(original.getUpgradeData(), decoded.getUpgradeData());
    }

    @Test
    @DisplayName("测试空数据处理")
    void testEmptyDataHandling() {
        message.setUpgradeType(T8108TerminalUpgradePackage.UPGRADE_TYPE_TERMINAL);
        message.setManufacturerId(new byte[]{0x01, 0x02, 0x03, 0x04, 0x05});
        message.setVersion(""); // 空版本号
        message.setUpgradeData(new byte[0]); // 空升级数据

        Buffer encoded = message.encodeBody();
        assertNotNull(encoded);

        T8108TerminalUpgradePackage decoded = new T8108TerminalUpgradePackage();
        decoded.decodeBody(encoded);

        assertEquals(message.getUpgradeType(), decoded.getUpgradeType());
        assertArrayEquals(message.getManufacturerId(), decoded.getManufacturerId());
        assertEquals(0, decoded.getVersionLength());
        assertEquals(0, decoded.getUpgradeDataLength());
    }

    @Test
    @DisplayName("测试工厂创建")
    void testFactoryCreation() {
        JT808Message factoryMessage = factory.createMessage(0x8108);
        assertInstanceOf(T8108TerminalUpgradePackage.class, factoryMessage);
        assertEquals(0x8108, factoryMessage.getMessageId());
    }

    @Test
    @DisplayName("测试toString方法")
    void testToString() {
        message.setUpgradeType(T8108TerminalUpgradePackage.UPGRADE_TYPE_TERMINAL);
        message.setManufacturerId(new byte[]{0x01, 0x02, 0x03, 0x04, 0x05});
        message.setVersion("V1.0");
        message.setUpgradeData(new byte[]{0x10, 0x20});

        String result = message.toString();
        assertNotNull(result);
        assertTrue(result.contains("T8108TerminalUpgradePackage"));
        assertTrue(result.contains("终端"));
        assertTrue(result.contains("V1.0"));
    }

    @Test
    @DisplayName("测试制造商ID设置边界条件")
    void testManufacturerIdBoundaryConditions() {
        // 测试null
        message.setManufacturerId(null);
        assertNotNull(message.getManufacturerId());

        // 测试长度不正确的数组
        message.setManufacturerId(new byte[]{0x01, 0x02, 0x03}); // 长度为3
        // 应该不会改变原有的制造商ID

        // 测试正确长度的数组
        byte[] correctId = {0x11, 0x22, 0x33, 0x44, 0x55};
        message.setManufacturerId(correctId);
        assertArrayEquals(correctId, message.getManufacturerId());
    }
}