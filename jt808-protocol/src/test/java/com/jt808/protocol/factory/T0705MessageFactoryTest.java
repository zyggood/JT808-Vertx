package com.jt808.protocol.factory;

import com.jt808.common.exception.ProtocolException;
import com.jt808.protocol.message.JT808Header;
import com.jt808.protocol.message.JT808Message;
import com.jt808.protocol.message.T0705CanBusDataUpload;
import io.vertx.core.buffer.Buffer;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * T0705MessageFactoryTest 集成测试
 */
public class T0705MessageFactoryTest {

    private static final Logger logger = LoggerFactory.getLogger(T0705MessageFactoryTest.class);

    @Test
    public void testCreateT0705Message() {
        JT808MessageFactory factory = JT808MessageFactory.getInstance();
        
        // 测试工厂是否支持T0705消息
        assertTrue(factory.isSupported(0x0705));
        
        // 创建T0705消息实例
        JT808Message message = factory.createMessage(0x0705);
        assertNotNull(message);
        assertInstanceOf(T0705CanBusDataUpload.class, message);
        assertEquals(0x0705, message.getMessageId());
        
        logger.info("成功创建T0705消息实例: {}", message.getClass().getSimpleName());
    }

    @Test
    public void testT0705MessageEncodeDecode() {
        JT808MessageFactory factory = JT808MessageFactory.getInstance();
        
        // 创建原始消息
        T0705CanBusDataUpload originalMessage = new T0705CanBusDataUpload();
        
        // 设置消息头
        JT808Header header = new JT808Header();
        header.setMessageId(0x0705);
        header.setPhoneNumber("123456789012");
        header.setSerialNumber(1);
        originalMessage.setHeader(header);
        
        // 设置接收时间
        originalMessage.setReceiveTime(LocalTime.of(15, 30, 45, 123_000_000));
        
        // 添加CAN数据项
        T0705CanBusDataUpload.CanBusDataItem item = new T0705CanBusDataUpload.CanBusDataItem();
        item.setCanChannel(1); // CAN2
        item.setFrameType(1);  // 扩展帧
        item.setDataCollectionMethod(0); // 原始数据
        item.setCanBusId(0x18FF1234); // CAN总线ID
        item.setCanData(new byte[]{0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08});
        originalMessage.addCanDataItem(item);
        
        // 编码消息
        Buffer encodedMessage = factory.encodeMessage(originalMessage);
        assertNotNull(encodedMessage);
        assertTrue(encodedMessage.length() > 0);
        
        logger.info("编码后的完整消息长度: {} 字节", encodedMessage.length());
        
        // 打印编码后的消息数据（用于调试）
        StringBuilder hexString = new StringBuilder();
        for (int i = 0; i < encodedMessage.length(); i++) {
            hexString.append(String.format("%02X ", encodedMessage.getByte(i) & 0xFF));
        }
        logger.info("编码后的消息数据: {}", hexString.toString().trim());
        
        // 解码消息
        JT808Message decodedMessage;
        try {
            decodedMessage = factory.parseMessage(encodedMessage);
        } catch (ProtocolException e) {
            fail("解码消息失败: " + e.getMessage());
            return;
        }
        assertNotNull(decodedMessage);
        assertInstanceOf(T0705CanBusDataUpload.class, decodedMessage);
        
        T0705CanBusDataUpload decodedT0705 = (T0705CanBusDataUpload) decodedMessage;
        
        // 验证解码结果
        assertEquals(1, decodedT0705.getItemCount());
        assertEquals(LocalTime.of(15, 30, 45, 123_000_000), decodedT0705.getReceiveTime());
        assertEquals(1, decodedT0705.getCanDataItems().size());
        
        T0705CanBusDataUpload.CanBusDataItem decodedItem = decodedT0705.getCanDataItems().get(0);
        assertEquals(1, decodedItem.getCanChannel());
        assertEquals(1, decodedItem.getFrameType());
        assertEquals(0, decodedItem.getDataCollectionMethod());
        assertEquals(0x18FF1234, decodedItem.getCanBusId());
        assertArrayEquals(new byte[]{0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08}, decodedItem.getCanData());
        
        logger.info("成功解码T0705消息: {}", decodedT0705.toString());
    }

    @Test
    public void testT0705MessageWithMultipleCanItems() {
        JT808MessageFactory factory = JT808MessageFactory.getInstance();
        
        // 创建包含多个CAN数据项的消息
        T0705CanBusDataUpload message = new T0705CanBusDataUpload();
        
        // 设置消息头
        JT808Header header = new JT808Header();
        header.setMessageId(0x0705);
        header.setPhoneNumber("123456789012");
        header.setSerialNumber(2);
        message.setHeader(header);
        
        // 设置接收时间
        message.setReceiveTime(LocalTime.of(9, 15, 30, 500_000_000));
        
        // 添加多个CAN数据项
        for (int i = 0; i < 3; i++) {
            T0705CanBusDataUpload.CanBusDataItem item = new T0705CanBusDataUpload.CanBusDataItem();
            item.setCanChannel(i % 2); // 交替使用CAN1和CAN2
            item.setFrameType(i % 2);   // 交替使用标准帧和扩展帧
            item.setDataCollectionMethod(i % 2); // 交替使用原始数据和平均值
            item.setCanBusId(0x100 + i); // 不同的CAN总线ID
            
            byte[] data = new byte[8];
            for (int j = 0; j < 8; j++) {
                data[j] = (byte) (i * 8 + j);
            }
            item.setCanData(data);
            
            message.addCanDataItem(item);
        }
        
        // 编码和解码
        Buffer encoded = factory.encodeMessage(message);
        JT808Message decoded;
        try {
            decoded = factory.parseMessage(encoded);
        } catch (ProtocolException e) {
            fail("解码消息失败: " + e.getMessage());
            return;
        }
        
        assertInstanceOf(T0705CanBusDataUpload.class, decoded);
        T0705CanBusDataUpload decodedMessage = (T0705CanBusDataUpload) decoded;
        
        assertEquals(3, decodedMessage.getItemCount());
        assertEquals(LocalTime.of(9, 15, 30, 500_000_000), decodedMessage.getReceiveTime());
        assertEquals(3, decodedMessage.getCanDataItems().size());
        
        // 验证每个CAN数据项
        for (int i = 0; i < 3; i++) {
            T0705CanBusDataUpload.CanBusDataItem item = decodedMessage.getCanDataItems().get(i);
            assertEquals(i % 2, item.getCanChannel());
            assertEquals(i % 2, item.getFrameType());
            assertEquals(i % 2, item.getDataCollectionMethod());
            assertEquals(0x100 + i, item.getCanBusId());
            
            byte[] expectedData = new byte[8];
            for (int j = 0; j < 8; j++) {
                expectedData[j] = (byte) (i * 8 + j);
            }
            assertArrayEquals(expectedData, item.getCanData());
        }
        
        logger.info("成功处理包含{}个CAN数据项的T0705消息", decodedMessage.getItemCount());
    }

    @Test
    public void testT0705MessageRegistration() {
        JT808MessageFactory factory = JT808MessageFactory.getInstance();
        
        // 验证T0705消息已正确注册
        assertTrue(factory.isSupported(0x0705));
        
        // 验证支持的消息ID列表包含0x0705
        assertTrue(factory.getSupportedMessageIds().contains(0x0705));
        
        logger.info("T0705消息注册验证通过");
    }
}