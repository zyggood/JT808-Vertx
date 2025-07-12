package com.jt808.protocol.message;

import com.jt808.protocol.factory.JT808MessageFactory;
import io.vertx.core.buffer.Buffer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("T0108终端升级结果通知消息测试")
class T0108TerminalUpgradeResultNotificationTest {

    private T0108TerminalUpgradeResultNotification message;
    private JT808MessageFactory factory;

    @BeforeEach
    void setUp() {
        message = new T0108TerminalUpgradeResultNotification();
        factory = JT808MessageFactory.getInstance();
    }

    @Test
    @DisplayName("测试消息ID")
    void testMessageId() {
        assertEquals(0x0108, message.getMessageId());
    }

    @Test
    @DisplayName("测试默认构造函数")
    void testDefaultConstructor() {
        assertEquals(0, message.getUpgradeType());
        assertEquals(0, message.getUpgradeResult());
    }

    @Test
    @DisplayName("测试带参数构造函数")
    void testParameterizedConstructor() {
        T0108TerminalUpgradeResultNotification msg = new T0108TerminalUpgradeResultNotification(
                T0108TerminalUpgradeResultNotification.UPGRADE_TYPE_IC_CARD_READER,
                T0108TerminalUpgradeResultNotification.UPGRADE_RESULT_SUCCESS
        );

        assertEquals(T0108TerminalUpgradeResultNotification.UPGRADE_TYPE_IC_CARD_READER, msg.getUpgradeType());
        assertEquals(T0108TerminalUpgradeResultNotification.UPGRADE_RESULT_SUCCESS, msg.getUpgradeResult());
    }

    @Test
    @DisplayName("测试升级类型常量")
    void testUpgradeTypeConstants() {
        assertEquals(0x00, T0108TerminalUpgradeResultNotification.UPGRADE_TYPE_TERMINAL);
        assertEquals(0x12, T0108TerminalUpgradeResultNotification.UPGRADE_TYPE_IC_CARD_READER);
        assertEquals(0x52, T0108TerminalUpgradeResultNotification.UPGRADE_TYPE_BEIDOU_MODULE);
    }

    @Test
    @DisplayName("测试升级结果常量")
    void testUpgradeResultConstants() {
        assertEquals(0x00, T0108TerminalUpgradeResultNotification.UPGRADE_RESULT_SUCCESS);
        assertEquals(0x01, T0108TerminalUpgradeResultNotification.UPGRADE_RESULT_FAILURE);
        assertEquals(0x02, T0108TerminalUpgradeResultNotification.UPGRADE_RESULT_CANCEL);
    }

    @Test
    @DisplayName("测试升级类型描述")
    void testUpgradeTypeDescription() {
        message.setUpgradeType(T0108TerminalUpgradeResultNotification.UPGRADE_TYPE_TERMINAL);
        assertEquals("终端", message.getUpgradeTypeDescription());

        message.setUpgradeType(T0108TerminalUpgradeResultNotification.UPGRADE_TYPE_IC_CARD_READER);
        assertEquals("道路运输证IC卡读卡器", message.getUpgradeTypeDescription());

        message.setUpgradeType(T0108TerminalUpgradeResultNotification.UPGRADE_TYPE_BEIDOU_MODULE);
        assertEquals("北斗卫星定位模块", message.getUpgradeTypeDescription());

        message.setUpgradeType((byte) 0xFF);
        assertEquals("未知类型(255)", message.getUpgradeTypeDescription());
    }

    @Test
    @DisplayName("测试升级结果描述")
    void testUpgradeResultDescription() {
        message.setUpgradeResult(T0108TerminalUpgradeResultNotification.UPGRADE_RESULT_SUCCESS);
        assertEquals("成功", message.getUpgradeResultDescription());

        message.setUpgradeResult(T0108TerminalUpgradeResultNotification.UPGRADE_RESULT_FAILURE);
        assertEquals("失败", message.getUpgradeResultDescription());

        message.setUpgradeResult(T0108TerminalUpgradeResultNotification.UPGRADE_RESULT_CANCEL);
        assertEquals("取消", message.getUpgradeResultDescription());

        message.setUpgradeResult((byte) 0xFF);
        assertEquals("未知结果(255)", message.getUpgradeResultDescription());
    }

    @Test
    @DisplayName("测试终端升级静态工厂方法")
    void testTerminalUpgradeStaticFactoryMethods() {
        // 测试终端升级成功
        T0108TerminalUpgradeResultNotification successMsg = T0108TerminalUpgradeResultNotification.createTerminalUpgradeSuccess();
        assertEquals(T0108TerminalUpgradeResultNotification.UPGRADE_TYPE_TERMINAL, successMsg.getUpgradeType());
        assertEquals(T0108TerminalUpgradeResultNotification.UPGRADE_RESULT_SUCCESS, successMsg.getUpgradeResult());

        // 测试终端升级失败
        T0108TerminalUpgradeResultNotification failureMsg = T0108TerminalUpgradeResultNotification.createTerminalUpgradeFailure();
        assertEquals(T0108TerminalUpgradeResultNotification.UPGRADE_TYPE_TERMINAL, failureMsg.getUpgradeType());
        assertEquals(T0108TerminalUpgradeResultNotification.UPGRADE_RESULT_FAILURE, failureMsg.getUpgradeResult());

        // 测试终端升级取消
        T0108TerminalUpgradeResultNotification cancelMsg = T0108TerminalUpgradeResultNotification.createTerminalUpgradeCancel();
        assertEquals(T0108TerminalUpgradeResultNotification.UPGRADE_TYPE_TERMINAL, cancelMsg.getUpgradeType());
        assertEquals(T0108TerminalUpgradeResultNotification.UPGRADE_RESULT_CANCEL, cancelMsg.getUpgradeResult());
    }

    @Test
    @DisplayName("测试IC卡读卡器升级静态工厂方法")
    void testIcCardReaderUpgradeStaticFactoryMethods() {
        // 测试IC卡读卡器升级成功
        T0108TerminalUpgradeResultNotification successMsg = T0108TerminalUpgradeResultNotification.createIcCardReaderUpgradeSuccess();
        assertEquals(T0108TerminalUpgradeResultNotification.UPGRADE_TYPE_IC_CARD_READER, successMsg.getUpgradeType());
        assertEquals(T0108TerminalUpgradeResultNotification.UPGRADE_RESULT_SUCCESS, successMsg.getUpgradeResult());

        // 测试IC卡读卡器升级失败
        T0108TerminalUpgradeResultNotification failureMsg = T0108TerminalUpgradeResultNotification.createIcCardReaderUpgradeFailure();
        assertEquals(T0108TerminalUpgradeResultNotification.UPGRADE_TYPE_IC_CARD_READER, failureMsg.getUpgradeType());
        assertEquals(T0108TerminalUpgradeResultNotification.UPGRADE_RESULT_FAILURE, failureMsg.getUpgradeResult());

        // 测试IC卡读卡器升级取消
        T0108TerminalUpgradeResultNotification cancelMsg = T0108TerminalUpgradeResultNotification.createIcCardReaderUpgradeCancel();
        assertEquals(T0108TerminalUpgradeResultNotification.UPGRADE_TYPE_IC_CARD_READER, cancelMsg.getUpgradeType());
        assertEquals(T0108TerminalUpgradeResultNotification.UPGRADE_RESULT_CANCEL, cancelMsg.getUpgradeResult());
    }

    @Test
    @DisplayName("测试北斗模块升级静态工厂方法")
    void testBeidouModuleUpgradeStaticFactoryMethods() {
        // 测试北斗模块升级成功
        T0108TerminalUpgradeResultNotification successMsg = T0108TerminalUpgradeResultNotification.createBeidouModuleUpgradeSuccess();
        assertEquals(T0108TerminalUpgradeResultNotification.UPGRADE_TYPE_BEIDOU_MODULE, successMsg.getUpgradeType());
        assertEquals(T0108TerminalUpgradeResultNotification.UPGRADE_RESULT_SUCCESS, successMsg.getUpgradeResult());

        // 测试北斗模块升级失败
        T0108TerminalUpgradeResultNotification failureMsg = T0108TerminalUpgradeResultNotification.createBeidouModuleUpgradeFailure();
        assertEquals(T0108TerminalUpgradeResultNotification.UPGRADE_TYPE_BEIDOU_MODULE, failureMsg.getUpgradeType());
        assertEquals(T0108TerminalUpgradeResultNotification.UPGRADE_RESULT_FAILURE, failureMsg.getUpgradeResult());

        // 测试北斗模块升级取消
        T0108TerminalUpgradeResultNotification cancelMsg = T0108TerminalUpgradeResultNotification.createBeidouModuleUpgradeCancel();
        assertEquals(T0108TerminalUpgradeResultNotification.UPGRADE_TYPE_BEIDOU_MODULE, cancelMsg.getUpgradeType());
        assertEquals(T0108TerminalUpgradeResultNotification.UPGRADE_RESULT_CANCEL, cancelMsg.getUpgradeResult());
    }

    @Test
    @DisplayName("测试消息编码")
    void testEncodeBody() {
        // 设置测试数据
        message.setUpgradeType(T0108TerminalUpgradeResultNotification.UPGRADE_TYPE_IC_CARD_READER);
        message.setUpgradeResult(T0108TerminalUpgradeResultNotification.UPGRADE_RESULT_FAILURE);

        Buffer encoded = message.encodeBody();

        // 验证编码结果
        assertNotNull(encoded);
        assertEquals(2, encoded.length()); // 总共2字节

        // 升级类型 (1字节)
        assertEquals(T0108TerminalUpgradeResultNotification.UPGRADE_TYPE_IC_CARD_READER, encoded.getByte(0));

        // 升级结果 (1字节)
        assertEquals(T0108TerminalUpgradeResultNotification.UPGRADE_RESULT_FAILURE, encoded.getByte(1));
    }

    @Test
    @DisplayName("测试消息解码")
    void testDecodeBody() {
        // 构造测试数据
        Buffer buffer = Buffer.buffer();
        buffer.appendByte(T0108TerminalUpgradeResultNotification.UPGRADE_TYPE_BEIDOU_MODULE); // 升级类型
        buffer.appendByte(T0108TerminalUpgradeResultNotification.UPGRADE_RESULT_SUCCESS); // 升级结果

        // 解码
        message.decodeBody(buffer);

        // 验证解码结果
        assertEquals(T0108TerminalUpgradeResultNotification.UPGRADE_TYPE_BEIDOU_MODULE, message.getUpgradeType());
        assertEquals(T0108TerminalUpgradeResultNotification.UPGRADE_RESULT_SUCCESS, message.getUpgradeResult());
    }

    @Test
    @DisplayName("测试编解码往返一致性")
    void testEncodeDecodeRoundTrip() {
        // 设置原始数据
        T0108TerminalUpgradeResultNotification original = new T0108TerminalUpgradeResultNotification();
        original.setUpgradeType(T0108TerminalUpgradeResultNotification.UPGRADE_TYPE_TERMINAL);
        original.setUpgradeResult(T0108TerminalUpgradeResultNotification.UPGRADE_RESULT_CANCEL);

        // 编码
        Buffer encoded = original.encodeBody();

        // 解码
        T0108TerminalUpgradeResultNotification decoded = new T0108TerminalUpgradeResultNotification();
        decoded.decodeBody(encoded);

        // 验证一致性
        assertEquals(original.getUpgradeType(), decoded.getUpgradeType());
        assertEquals(original.getUpgradeResult(), decoded.getUpgradeResult());
    }

    @Test
    @DisplayName("测试工厂创建")
    void testFactoryCreation() {
        JT808Message factoryMessage = factory.createMessage(0x0108);
        assertInstanceOf(T0108TerminalUpgradeResultNotification.class, factoryMessage);
        assertEquals(0x0108, factoryMessage.getMessageId());
    }

    @Test
    @DisplayName("测试toString方法")
    void testToString() {
        message.setUpgradeType(T0108TerminalUpgradeResultNotification.UPGRADE_TYPE_TERMINAL);
        message.setUpgradeResult(T0108TerminalUpgradeResultNotification.UPGRADE_RESULT_SUCCESS);

        String result = message.toString();
        assertNotNull(result);
        assertTrue(result.contains("T0108TerminalUpgradeResultNotification"));
        assertTrue(result.contains("终端"));
        assertTrue(result.contains("成功"));
    }

    @Test
    @DisplayName("测试所有升级类型和结果组合")
    void testAllUpgradeTypesAndResults() {
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

        // 测试所有组合
        for (byte upgradeType : upgradeTypes) {
            for (byte upgradeResult : upgradeResults) {
                T0108TerminalUpgradeResultNotification testMsg = new T0108TerminalUpgradeResultNotification(upgradeType, upgradeResult);

                // 编解码测试
                Buffer encoded = testMsg.encodeBody();
                T0108TerminalUpgradeResultNotification decoded = new T0108TerminalUpgradeResultNotification();
                decoded.decodeBody(encoded);

                assertEquals(upgradeType, decoded.getUpgradeType());
                assertEquals(upgradeResult, decoded.getUpgradeResult());

                // 描述方法测试
                assertNotNull(decoded.getUpgradeTypeDescription());
                assertNotNull(decoded.getUpgradeResultDescription());
            }
        }
    }
}