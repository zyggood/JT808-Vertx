package com.jt808.protocol.message;

import io.vertx.core.buffer.Buffer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.charset.Charset;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("T8300文本信息下发消息测试")
class T8300TextInfoDistributionTest {

    private T8300TextInfoDistribution message;

    @BeforeEach
    void setUp() {
        message = new T8300TextInfoDistribution();
    }

    @Test
    @DisplayName("测试消息ID")
    void testMessageId() {
        assertEquals(0x8300, message.getMessageId());
    }

    @Test
    @DisplayName("测试默认构造函数")
    void testDefaultConstructor() {
        T8300TextInfoDistribution msg = new T8300TextInfoDistribution();
        assertNotNull(msg);
        assertEquals(0x8300, msg.getMessageId());
        assertEquals(0, msg.getTextFlag());
        assertNull(msg.getTextInfo());
    }

    @Test
    @DisplayName("测试参数构造函数")
    void testParameterizedConstructor() {
        byte flag = T8300TextInfoDistribution.TextFlag.EMERGENCY | T8300TextInfoDistribution.TextFlag.TERMINAL_DISPLAY;
        String text = "紧急通知：请立即停车检查";

        T8300TextInfoDistribution msg = new T8300TextInfoDistribution(flag, text);
        assertEquals(flag, msg.getTextFlag());
        assertEquals(text, msg.getTextInfo());
        assertTrue(msg.isEmergency());
        assertTrue(msg.isTerminalDisplay());
    }

    @Test
    @DisplayName("测试消息体编码 - 基本功能")
    void testEncodeBodyBasic() {
        message.setTextFlag((byte) 0x05); // 紧急 + 终端显示
        message.setTextInfo("测试文本");

        Buffer encoded = message.encodeBody();
        assertNotNull(encoded);
        assertTrue(encoded.length() > 1);

        // 验证标志位
        assertEquals((byte) 0x05, encoded.getByte(0));

        // 验证文本内容
        byte[] textBytes = "测试文本".getBytes(Charset.forName("GBK"));
        byte[] actualTextBytes = encoded.getBytes(1, encoded.length());
        assertArrayEquals(textBytes, actualTextBytes);
    }

    @Test
    @DisplayName("测试消息体编码 - 空文本")
    void testEncodeBodyEmptyText() {
        message.setTextFlag((byte) 0x01);
        message.setTextInfo("");

        Buffer encoded = message.encodeBody();
        assertEquals(1, encoded.length()); // 只有标志位
        assertEquals((byte) 0x01, encoded.getByte(0));
    }

    @Test
    @DisplayName("测试消息体编码 - null文本")
    void testEncodeBodyNullText() {
        message.setTextFlag((byte) 0x01);
        message.setTextInfo(null);

        Buffer encoded = message.encodeBody();
        assertEquals(1, encoded.length()); // 只有标志位
        assertEquals((byte) 0x01, encoded.getByte(0));
    }

    @Test
    @DisplayName("测试消息体编码 - 文本长度限制")
    void testEncodeBodyTextLengthLimit() {
        message.setTextFlag((byte) 0x01);

        // 创建超过1024字节的文本
        StringBuilder longText = new StringBuilder();
        for (int i = 0; i < 1025; i++) {
            longText.append("a");
        }

        assertThrows(IllegalArgumentException.class, () -> {
            message.setTextInfo(longText.toString());
        });
    }

    @Test
    @DisplayName("测试消息体编码 - 中文文本长度限制")
    void testEncodeBodyChineseTextLengthLimit() {
        message.setTextFlag((byte) 0x01);

        // 创建超过1024字节的中文文本（每个中文字符在GBK中占2字节）
        StringBuilder longText = new StringBuilder();
        for (int i = 0; i < 513; i++) { // 513 * 2 = 1026 > 1024
            longText.append("中");
        }

        assertThrows(IllegalArgumentException.class, () -> {
            message.setTextInfo(longText.toString());
        });
    }

    @Test
    @DisplayName("测试消息体解码 - 基本功能")
    void testDecodeBodyBasic() {
        Buffer buffer = Buffer.buffer();
        buffer.appendByte((byte) 0x0D); // 紧急(0x01) + 终端显示(0x04) + 终端TTS(0x08) = 0x0D
        buffer.appendBytes("紧急通知".getBytes(Charset.forName("GBK")));

        message.decodeBody(buffer);

        assertEquals((byte) 0x0D, message.getTextFlag());
        assertEquals("紧急通知", message.getTextInfo());
        assertTrue(message.isEmergency());
        assertTrue(message.isTerminalDisplay()); // 0x0D包含位2，所以应该是true
        assertTrue(message.isTerminalTTS());
        assertFalse(message.isAdvertisementDisplay());
    }

    @Test
    @DisplayName("测试消息体解码 - 只有标志位")
    void testDecodeBodyFlagOnly() {
        Buffer buffer = Buffer.buffer();
        buffer.appendByte((byte) 0x01);

        message.decodeBody(buffer);

        assertEquals((byte) 0x01, message.getTextFlag());
        assertEquals("", message.getTextInfo());
    }

    @Test
    @DisplayName("测试消息体解码 - 空缓冲区异常")
    void testDecodeBodyNullException() {
        assertThrows(IllegalArgumentException.class, () -> {
            message.decodeBody(null);
        });
    }

    @Test
    @DisplayName("测试消息体解码 - 长度不足异常")
    void testDecodeBodyInsufficientLength() {
        Buffer buffer = Buffer.buffer();

        assertThrows(IllegalArgumentException.class, () -> {
            message.decodeBody(buffer);
        });
    }

    @Test
    @DisplayName("测试编解码一致性")
    void testEncodeDecodeConsistency() {
        // 测试各种标志位组合
        byte[] flags = {
                T8300TextInfoDistribution.TextFlag.EMERGENCY,
                (byte) (T8300TextInfoDistribution.TextFlag.TERMINAL_DISPLAY | T8300TextInfoDistribution.TextFlag.TERMINAL_TTS),
                (byte) (T8300TextInfoDistribution.TextFlag.ADVERTISEMENT_DISPLAY | T8300TextInfoDistribution.TextFlag.CAN_FAULT_INFO),
                (byte) 0x1F // 所有位都设置
        };

        String[] texts = {
                "简单文本",
                "包含中文的文本信息",
                "Emergency Alert!",
                "混合文本 Mixed Text 123"
        };

        for (byte flag : flags) {
            for (String text : texts) {
                T8300TextInfoDistribution original = new T8300TextInfoDistribution(flag, text);

                Buffer encoded = original.encodeBody();

                T8300TextInfoDistribution decoded = new T8300TextInfoDistribution();
                decoded.decodeBody(encoded);

                assertEquals(original.getTextFlag(), decoded.getTextFlag());
                assertEquals(original.getTextInfo(), decoded.getTextInfo());
            }
        }
    }

    @Test
    @DisplayName("测试标志位检查方法")
    void testFlagCheckMethods() {
        // 测试紧急标志
        message.setTextFlag(T8300TextInfoDistribution.TextFlag.EMERGENCY);
        assertTrue(message.isEmergency());
        assertFalse(message.isTerminalDisplay());
        assertFalse(message.isTerminalTTS());
        assertFalse(message.isAdvertisementDisplay());
        assertFalse(message.isCANFaultInfo());

        // 测试终端显示标志
        message.setTextFlag(T8300TextInfoDistribution.TextFlag.TERMINAL_DISPLAY);
        assertFalse(message.isEmergency());
        assertTrue(message.isTerminalDisplay());
        assertFalse(message.isTerminalTTS());
        assertFalse(message.isAdvertisementDisplay());
        assertFalse(message.isCANFaultInfo());

        // 测试组合标志
        byte combinedFlag = (byte) (T8300TextInfoDistribution.TextFlag.EMERGENCY |
                T8300TextInfoDistribution.TextFlag.TERMINAL_TTS |
                T8300TextInfoDistribution.TextFlag.CAN_FAULT_INFO);
        message.setTextFlag(combinedFlag);
        assertTrue(message.isEmergency());
        assertFalse(message.isTerminalDisplay());
        assertTrue(message.isTerminalTTS());
        assertFalse(message.isAdvertisementDisplay());
        assertTrue(message.isCANFaultInfo());
    }

    @Test
    @DisplayName("测试标志位描述")
    void testTextFlagDescription() {
        // 测试单个标志
        message.setTextFlag(T8300TextInfoDistribution.TextFlag.EMERGENCY);
        String desc = message.getTextFlagDescription();
        assertTrue(desc.contains("紧急"));
        assertTrue(desc.contains("中心导航信息"));

        // 测试组合标志
        byte combinedFlag = (byte) (T8300TextInfoDistribution.TextFlag.EMERGENCY |
                T8300TextInfoDistribution.TextFlag.TERMINAL_DISPLAY |
                T8300TextInfoDistribution.TextFlag.CAN_FAULT_INFO);
        message.setTextFlag(combinedFlag);
        desc = message.getTextFlagDescription();
        assertTrue(desc.contains("紧急"));
        assertTrue(desc.contains("终端显示器显示"));
        assertTrue(desc.contains("CAN故障码信息"));
        assertFalse(desc.contains("中心导航信息"));
    }

    @Test
    @DisplayName("测试工厂方法 - 紧急文本")
    void testCreateEmergencyText() {
        String text = "紧急停车通知";
        T8300TextInfoDistribution msg = T8300TextInfoDistribution.createEmergencyText(text, true, true);

        assertTrue(msg.isEmergency());
        assertTrue(msg.isTerminalDisplay());
        assertTrue(msg.isTerminalTTS());
        assertFalse(msg.isAdvertisementDisplay());
        assertFalse(msg.isCANFaultInfo());
        assertEquals(text, msg.getTextInfo());
    }

    @Test
    @DisplayName("测试工厂方法 - 普通文本")
    void testCreateNormalText() {
        String text = "路况信息";
        T8300TextInfoDistribution msg = T8300TextInfoDistribution.createNormalText(text, true, false, true);

        assertFalse(msg.isEmergency());
        assertTrue(msg.isTerminalDisplay());
        assertFalse(msg.isTerminalTTS());
        assertTrue(msg.isAdvertisementDisplay());
        assertFalse(msg.isCANFaultInfo());
        assertEquals(text, msg.getTextInfo());
    }

    @Test
    @DisplayName("测试工厂方法 - CAN故障码信息")
    void testCreateCANFaultInfo() {
        String text = "发动机故障码: P0001";
        T8300TextInfoDistribution msg = T8300TextInfoDistribution.createCANFaultInfo(text, true);

        assertFalse(msg.isEmergency());
        assertTrue(msg.isTerminalDisplay());
        assertFalse(msg.isTerminalTTS());
        assertFalse(msg.isAdvertisementDisplay());
        assertTrue(msg.isCANFaultInfo());
        assertEquals(text, msg.getTextInfo());
    }

    @Test
    @DisplayName("测试toString方法")
    void testToString() {
        message.setTextFlag((byte) 0x05);
        message.setTextInfo("测试文本");

        String str = message.toString();
        assertTrue(str.contains("T8300TextInfoDistribution"));
        assertTrue(str.contains("0x05"));
        assertTrue(str.contains("测试文本"));
    }

    @Test
    @DisplayName("测试equals和hashCode")
    void testEqualsAndHashCode() {
        T8300TextInfoDistribution msg1 = new T8300TextInfoDistribution((byte) 0x01, "文本1");
        T8300TextInfoDistribution msg2 = new T8300TextInfoDistribution((byte) 0x01, "文本1");
        T8300TextInfoDistribution msg3 = new T8300TextInfoDistribution((byte) 0x02, "文本1");
        T8300TextInfoDistribution msg4 = new T8300TextInfoDistribution((byte) 0x01, "文本2");

        assertEquals(msg1, msg2);
        assertEquals(msg1.hashCode(), msg2.hashCode());

        assertNotEquals(msg1, msg3);
        assertNotEquals(msg1, msg4);
        assertNotEquals(msg1, null);
        assertNotEquals(msg1, "string");
    }

    @Test
    @DisplayName("测试边界值")
    void testBoundaryValues() {
        // 测试标志位边界值
        message.setTextFlag((byte) 0x00);
        assertEquals((byte) 0x00, message.getTextFlag());

        message.setTextFlag((byte) 0xFF);
        assertEquals((byte) 0xFF, message.getTextFlag());

        // 测试最大长度文本（1024字节）
        StringBuilder maxText = new StringBuilder();
        for (int i = 0; i < 1024; i++) {
            maxText.append("a");
        }

        assertDoesNotThrow(() -> {
            message.setTextInfo(maxText.toString());
        });

        Buffer encoded = message.encodeBody();
        assertEquals(1025, encoded.length()); // 1字节标志位 + 1024字节文本
    }
}