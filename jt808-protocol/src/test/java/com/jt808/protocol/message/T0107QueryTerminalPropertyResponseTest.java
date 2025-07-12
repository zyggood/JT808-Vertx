package com.jt808.protocol.message;

import com.jt808.protocol.codec.JT808Decoder;
import com.jt808.protocol.codec.JT808Encoder;
import com.jt808.protocol.factory.JT808MessageFactory;
import io.vertx.core.buffer.Buffer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * T0107查询终端属性应答消息测试类
 */
public class T0107QueryTerminalPropertyResponseTest {

    @Test
    public void testMessageId() {
        T0107QueryTerminalPropertyResponse message = new T0107QueryTerminalPropertyResponse();
        assertEquals(0x0107, message.getMessageId());
        assertEquals(T0107QueryTerminalPropertyResponse.MESSAGE_ID, message.getMessageId());
    }

    @Test
    public void testDefaultConstructor() {
        T0107QueryTerminalPropertyResponse message = new T0107QueryTerminalPropertyResponse();

        assertEquals(0, message.getTerminalType());
        assertEquals("", message.getManufacturerId());
        assertEquals("", message.getTerminalModel());
        assertEquals("", message.getTerminalId());
        assertEquals("", message.getIccid());
        assertEquals("", message.getHardwareVersion());
        assertEquals("", message.getFirmwareVersion());
        assertEquals(0, message.getGnssAttribute());
        assertEquals(0, message.getCommunicationAttribute());
    }

    @Test
    public void testFullConstructor() {
        T0107QueryTerminalPropertyResponse message = new T0107QueryTerminalPropertyResponse(
                1, "ABCDE", "Model123", "ID12345", "1234567890123456789",
                "HW1.0", "FW2.0", 0x01, 0x02
        );

        assertEquals(1, message.getTerminalType());
        assertEquals("ABCDE", message.getManufacturerId());
        assertEquals("Model123", message.getTerminalModel());
        assertEquals("ID12345", message.getTerminalId());
        assertEquals("1234567890123456789", message.getIccid());
        assertEquals("HW1.0", message.getHardwareVersion());
        assertEquals("FW2.0", message.getFirmwareVersion());
        assertEquals(0x01, message.getGnssAttribute());
        assertEquals(0x02, message.getCommunicationAttribute());
        assertEquals(5, message.getHardwareVersionLength());
        assertEquals(5, message.getFirmwareVersionLength());
    }

    @Test
    public void testFactoryCreation() {
        JT808MessageFactory factory = JT808MessageFactory.getInstance();
        JT808Message message = factory.createMessage(0x0107);

        assertNotNull(message);
        assertTrue(message instanceof T0107QueryTerminalPropertyResponse);
        assertEquals(0x0107, message.getMessageId());
    }

    @Test
    public void testBasicEncodeDecode() {
        T0107QueryTerminalPropertyResponse original = new T0107QueryTerminalPropertyResponse(
                1, "MANU1", "MODEL001", "ID001", "12345678901234567890",
                "V1.0", "V2.0", 0x01, 0x02
        );

        // 编码
        Buffer encoded = original.encodeBody();
        assertNotNull(encoded);
        assertTrue(encoded.length() >= 37); // 最小长度

        // 解码
        T0107QueryTerminalPropertyResponse decoded = new T0107QueryTerminalPropertyResponse();
        decoded.decodeBody(encoded);

        // 验证
        assertEquals(original.getTerminalType(), decoded.getTerminalType());
        assertEquals(original.getManufacturerId(), decoded.getManufacturerId());
        assertEquals(original.getTerminalModel(), decoded.getTerminalModel());
        assertEquals(original.getTerminalId(), decoded.getTerminalId());
        assertEquals(original.getIccid(), decoded.getIccid());
        assertEquals(original.getHardwareVersion(), decoded.getHardwareVersion());
        assertEquals(original.getFirmwareVersion(), decoded.getFirmwareVersion());
        assertEquals(original.getGnssAttribute(), decoded.getGnssAttribute());
        assertEquals(original.getCommunicationAttribute(), decoded.getCommunicationAttribute());
    }

    @Test
    public void testEncodeDecodeConsistency() {
        T0107QueryTerminalPropertyResponse original = new T0107QueryTerminalPropertyResponse(
                0x1234, "TEST", "TESTMODEL", "TESTID", "98765432109876543210",
                "Hardware1.0", "Firmware2.0", 0xFF, 0xAA
        );

        Buffer encoded = original.encodeBody();
        T0107QueryTerminalPropertyResponse decoded = new T0107QueryTerminalPropertyResponse();
        decoded.decodeBody(encoded);

        // 再次编码验证一致性
        Buffer reEncoded = decoded.encodeBody();
        assertArrayEquals(encoded.getBytes(), reEncoded.getBytes());
    }

    @Test
    public void testToString() {
        T0107QueryTerminalPropertyResponse message = new T0107QueryTerminalPropertyResponse(
                1, "MANU", "MODEL", "ID", "12345", "HW1.0", "FW1.0", 1, 2
        );

        String str = message.toString();
        assertNotNull(str);
        assertTrue(str.contains("T0107QueryTerminalPropertyResponse"));
        assertTrue(str.contains("terminalType=1"));
        assertTrue(str.contains("manufacturerId='MANU'"));
    }

    @Test
    public void testSetters() {
        T0107QueryTerminalPropertyResponse message = new T0107QueryTerminalPropertyResponse();

        message.setTerminalType(100);
        message.setManufacturerId("NEWMANU");
        message.setTerminalModel("NEWMODEL");
        message.setTerminalId("NEWID");
        message.setIccid("11111111111111111111");
        message.setHardwareVersion("NEWHW");
        message.setFirmwareVersion("NEWFW");
        message.setGnssAttribute(0x10);
        message.setCommunicationAttribute(0x20);

        assertEquals(100, message.getTerminalType());
        assertEquals("NEWMANU", message.getManufacturerId());
        assertEquals("NEWMODEL", message.getTerminalModel());
        assertEquals("NEWID", message.getTerminalId());
        assertEquals("11111111111111111111", message.getIccid());
        assertEquals("NEWHW", message.getHardwareVersion());
        assertEquals("NEWFW", message.getFirmwareVersion());
        assertEquals(0x10, message.getGnssAttribute());
        assertEquals(0x20, message.getCommunicationAttribute());
        assertEquals(5, message.getHardwareVersionLength());
        assertEquals(5, message.getFirmwareVersionLength());
    }

    @Test
    public void testNullSafety() {
        T0107QueryTerminalPropertyResponse message = new T0107QueryTerminalPropertyResponse(
                1, null, null, null, null, null, null, 1, 2
        );

        assertEquals("", message.getManufacturerId());
        assertEquals("", message.getTerminalModel());
        assertEquals("", message.getTerminalId());
        assertEquals("", message.getIccid());
        assertEquals("", message.getHardwareVersion());
        assertEquals("", message.getFirmwareVersion());

        // 测试setter的null安全性
        message.setManufacturerId(null);
        message.setTerminalModel(null);
        message.setTerminalId(null);
        message.setIccid(null);
        message.setHardwareVersion(null);
        message.setFirmwareVersion(null);

        assertEquals("", message.getManufacturerId());
        assertEquals("", message.getTerminalModel());
        assertEquals("", message.getTerminalId());
        assertEquals("", message.getIccid());
        assertEquals("", message.getHardwareVersion());
        assertEquals("", message.getFirmwareVersion());
    }

    @Test
    public void testMessageFactorySupport() {
        JT808MessageFactory factory = JT808MessageFactory.getInstance();
        assertTrue(factory.isSupported(0x0107));
        assertTrue(factory.getSupportedMessageIds().contains(0x0107));
    }

    @Test
    public void testCompleteMessageFlow() {
        try {
            // 创建消息
            T0107QueryTerminalPropertyResponse message = new T0107QueryTerminalPropertyResponse(
                    0x0001, "MANU1", "MODEL123456789012345", "ID12345", "12345678901234567890",
                    "Hardware1.0", "Firmware2.0", 0x01, 0x02
            );

            // 设置消息头
            JT808Header header = new JT808Header();
            header.setMessageId(0x0107);
            header.setPhoneNumber("13800138000");
            header.setSerialNumber(1);
            header.setMessageProperty(message.encodeBody().length()); // 设置消息体长度
            message.setHeader(header);

            // 编码完整消息
            JT808Encoder encoder = new JT808Encoder();
            Buffer encodedMessage = encoder.encode(message);
            assertNotNull(encodedMessage);
            assertTrue(encodedMessage.length() > 0);

            // 解码完整消息
            JT808Decoder decoder = new JT808Decoder();
            JT808Message decodedMessage = decoder.decode(encodedMessage);

            assertNotNull(decodedMessage);
            assertTrue(decodedMessage instanceof T0107QueryTerminalPropertyResponse);
            assertEquals(0x0107, decodedMessage.getMessageId());

            T0107QueryTerminalPropertyResponse decoded = (T0107QueryTerminalPropertyResponse) decodedMessage;
            assertEquals(message.getTerminalType(), decoded.getTerminalType());
            assertEquals(message.getManufacturerId(), decoded.getManufacturerId());
            assertEquals(message.getTerminalModel(), decoded.getTerminalModel());

        } catch (Exception e) {
            fail("完整消息流程测试失败: " + e.getMessage());
        }
    }

    @Test
    public void testMinimalMessage() {
        T0107QueryTerminalPropertyResponse message = new T0107QueryTerminalPropertyResponse();

        Buffer encoded = message.encodeBody();
        assertNotNull(encoded);

        T0107QueryTerminalPropertyResponse decoded = new T0107QueryTerminalPropertyResponse();
        decoded.decodeBody(encoded);

        assertEquals(message.getTerminalType(), decoded.getTerminalType());
        assertEquals(message.getManufacturerId(), decoded.getManufacturerId());
        assertEquals(message.getTerminalModel(), decoded.getTerminalModel());
    }

    @Test
    public void testMessageTypeConstant() {
        assertEquals(0x0107, T0107QueryTerminalPropertyResponse.MESSAGE_ID);
    }

    @Test
    public void testLongVersionStrings() {
        T0107QueryTerminalPropertyResponse message = new T0107QueryTerminalPropertyResponse();

        // 测试长版本字符串
        String longHardwareVersion = "Very Long Hardware Version String That Exceeds Normal Length";
        String longFirmwareVersion = "Very Long Firmware Version String That Exceeds Normal Length";

        message.setHardwareVersion(longHardwareVersion);
        message.setFirmwareVersion(longFirmwareVersion);

        assertEquals(longHardwareVersion, message.getHardwareVersion());
        assertEquals(longFirmwareVersion, message.getFirmwareVersion());
        assertEquals(longHardwareVersion.getBytes().length, message.getHardwareVersionLength());
        assertEquals(longFirmwareVersion.getBytes().length, message.getFirmwareVersionLength());

        // 测试编解码
        Buffer encoded = message.encodeBody();
        T0107QueryTerminalPropertyResponse decoded = new T0107QueryTerminalPropertyResponse();
        decoded.decodeBody(encoded);

        assertEquals(message.getHardwareVersion(), decoded.getHardwareVersion());
        assertEquals(message.getFirmwareVersion(), decoded.getFirmwareVersion());
    }
}