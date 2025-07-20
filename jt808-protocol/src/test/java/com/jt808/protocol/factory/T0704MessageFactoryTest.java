package com.jt808.protocol.factory;

import com.jt808.protocol.message.JT808Header;
import com.jt808.protocol.message.JT808Message;
import com.jt808.protocol.message.T0200LocationReport;
import com.jt808.protocol.message.T0704LocationDataBatchUpload;
import io.vertx.core.buffer.Buffer;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * T0704消息工厂集成测试
 */
class T0704MessageFactoryTest {

    private static final Logger logger = LoggerFactory.getLogger(T0704MessageFactoryTest.class);

    private final JT808MessageFactory factory = JT808MessageFactory.getInstance();

    @Test
    void testCreateT0704Message() {
        // 测试工厂是否支持T0704消息
        assertTrue(factory.isSupported(0x0704));
        assertTrue(factory.getSupportedMessageIds().contains(0x0704));

        // 测试创建T0704消息实例
        JT808Message message = factory.createMessage(0x0704);
        assertNotNull(message);
        assertInstanceOf(T0704LocationDataBatchUpload.class, message);
        assertEquals(0x0704, message.getMessageId());

        logger.info("成功创建T0704消息实例: {}", message.getClass().getSimpleName());
    }

    @Test
    void testT0704MessageEncodeDecode() {
        // 创建T0704消息
        T0704LocationDataBatchUpload originalMessage = new T0704LocationDataBatchUpload();
        originalMessage.setLocationType((byte) 0);
        
        // 设置消息头
        JT808Header header = new JT808Header();
        header.setMessageId(0x0704);
        header.setPhoneNumber("123456789012");
        header.setSerialNumber(1);
        originalMessage.setHeader(header);

        // 添加位置汇报数据
        T0200LocationReport report = new T0200LocationReport();
        report.setAlarmFlag(0);
        report.setStatusFlag(0x02);
        report.setLatitude(31123456);
        report.setLongitude(121123456);
        report.setAltitude(100);
        report.setSpeed(60);
        report.setDirection(90);
        report.setDateTime(LocalDateTime.of(2024, 1, 15, 10, 30, 0));
        
        originalMessage.addLocationReport(report);

        // 编码消息
        Buffer encoded = factory.encodeMessage(originalMessage);
        assertNotNull(encoded);
        assertTrue(encoded.length() > 0);

        logger.info("编码后的完整消息长度: {} 字节", encoded.length());
        logger.info("编码后的消息数据: {}", bytesToHex(encoded.getBytes()));

        // 解码消息
        try {
            JT808Message decodedMessage = factory.parseMessage(encoded);
            assertNotNull(decodedMessage);
            assertInstanceOf(T0704LocationDataBatchUpload.class, decodedMessage);

            T0704LocationDataBatchUpload decoded = (T0704LocationDataBatchUpload) decodedMessage;
            assertEquals(originalMessage.getItemCount(), decoded.getItemCount());
            assertEquals(originalMessage.getLocationType(), decoded.getLocationType());
            assertEquals(1, decoded.getLocationDataItems().size());

            // 验证位置汇报数据
            T0200LocationReport decodedReport = decoded.getLocationDataItems().get(0).getLocationReport();
            assertNotNull(decodedReport);
            assertEquals(report.getLatitude(), decodedReport.getLatitude());
            assertEquals(report.getLongitude(), decodedReport.getLongitude());
            assertEquals(report.getDateTime(), decodedReport.getDateTime());

            logger.info("成功解码T0704消息: {}", decoded.toString());
        } catch (Exception e) {
            fail("解码T0704消息失败: " + e.getMessage(), e);
        }
    }

    @Test
    void testT0704MessageWithMultipleLocationReports() {
        // 创建包含多个位置汇报的T0704消息
        T0704LocationDataBatchUpload message = new T0704LocationDataBatchUpload();
        message.setLocationType((byte) 1); // 盲区补报
        
        // 设置消息头
        JT808Header header2 = new JT808Header();
        header2.setMessageId(0x0704);
        header2.setPhoneNumber("123456789012");
        header2.setSerialNumber(2);
        message.setHeader(header2);

        // 添加多个位置汇报
        for (int i = 0; i < 5; i++) {
            T0200LocationReport report = new T0200LocationReport();
            report.setAlarmFlag(0);
            report.setStatusFlag(0x02);
            report.setLatitude(31123456 + i * 1000);
            report.setLongitude(121123456 + i * 1000);
            report.setAltitude(100 + i * 10);
            report.setSpeed(60 + i * 5);
            report.setDirection(90 + i * 10);
            report.setDateTime(LocalDateTime.of(2024, 1, 15, 10, 30, i * 10));
            message.addLocationReport(report);
        }

        // 编码和解码
        Buffer encoded = factory.encodeMessage(message);
        assertNotNull(encoded);

        try {
            JT808Message decoded = factory.parseMessage(encoded);
            assertInstanceOf(T0704LocationDataBatchUpload.class, decoded);

            T0704LocationDataBatchUpload decodedMessage = (T0704LocationDataBatchUpload) decoded;
            assertEquals(5, decodedMessage.getItemCount());
            assertEquals((byte) 1, decodedMessage.getLocationType());
            assertEquals(5, decodedMessage.getLocationDataItems().size());

            logger.info("成功处理包含{}个位置汇报的T0704消息", decodedMessage.getItemCount());
        } catch (Exception e) {
            fail("处理多位置汇报T0704消息失败: " + e.getMessage(), e);
        }
    }

    @Test
    void testT0704MessageRegistration() {
        // 验证T0704消息已正确注册到工厂
        assertTrue(factory.getSupportedMessageIds().contains(0x0704));
        
        // 验证可以通过registerMessage方法重新注册
        factory.registerMessage(0x0704, T0704LocationDataBatchUpload::new);
        assertTrue(factory.isSupported(0x0704));
        
        JT808Message message = factory.createMessage(0x0704);
        assertInstanceOf(T0704LocationDataBatchUpload.class, message);
        
        logger.info("T0704消息注册验证通过");
    }

    /**
     * 将字节数组转换为十六进制字符串
     */
    private String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02X ", b));
        }
        return result.toString().trim();
    }
}