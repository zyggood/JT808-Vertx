package com.jt808.protocol.example;

import com.jt808.protocol.message.JT808Header;
import com.jt808.protocol.message.T0301EventReport;
import io.vertx.core.buffer.Buffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * T0301事件报告消息功能演示
 */
public class T0301EventReportExample {

    private static final Logger logger = LoggerFactory.getLogger(T0301EventReportExample.class);

    public static void main(String[] args) {
        T0301EventReportExample example = new T0301EventReportExample();
        example.demonstrateT0301Usage();
    }

    public void demonstrateT0301Usage() {
        logger.info("=== T0301事件报告消息功能演示 ===");

        demonstrateBasicUsage();
        demonstrateConstructors();
        demonstrateEncodeDecode();
        demonstrateEventIdHandling();
        demonstrateRealWorldScenarios();

        logger.info("=== T0301事件报告消息演示完成 ===");
    }

    /**
     * 基本使用演示
     */
    private void demonstrateBasicUsage() {
        logger.info("\n--- 基本使用演示 ---");

        // 创建事件报告消息
        T0301EventReport eventReport = new T0301EventReport();
        eventReport.setEventId((byte) 0x01);

        logger.info("消息ID: 0x{}", String.format("%04X", eventReport.getMessageId()));
        logger.info("事件ID: {} (0x{})", eventReport.getEventIdUnsigned(),
                String.format("%02X", eventReport.getEventIdUnsigned()));
        logger.info("消息对象: {}", eventReport);
    }

    /**
     * 构造函数演示
     */
    private void demonstrateConstructors() {
        logger.info("\n--- 构造函数演示 ---");

        // 默认构造函数
        T0301EventReport msg1 = new T0301EventReport();
        logger.info("默认构造: 事件ID = {}", msg1.getEventIdUnsigned());

        // 带事件ID的构造函数
        T0301EventReport msg2 = new T0301EventReport((byte) 0x05);
        logger.info("带事件ID构造: 事件ID = {}", msg2.getEventIdUnsigned());

        // 带消息头的构造函数
        JT808Header header = new JT808Header();
        header.setPhoneNumber("123456789012");
        header.setSerialNumber(1001);
        T0301EventReport msg3 = new T0301EventReport(header);
        logger.info("带消息头构造: 手机号 = {}, 流水号 = {}",
                header.getPhoneNumber(), header.getSerialNumber());

        // 带消息头和事件ID的构造函数
        T0301EventReport msg4 = new T0301EventReport(header, (byte) 0x10);
        logger.info("完整构造: 手机号 = {}, 流水号 = {}, 事件ID = {}",
                header.getPhoneNumber(), header.getSerialNumber(), msg4.getEventIdUnsigned());

        // 静态工厂方法
        T0301EventReport msg5 = T0301EventReport.create((byte) 0x20);
        logger.info("静态工厂方法: 事件ID = {}", msg5.getEventIdUnsigned());
    }

    /**
     * 编解码演示
     */
    private void demonstrateEncodeDecode() {
        logger.info("\n--- 编解码演示 ---");

        // 创建原始消息
        T0301EventReport original = T0301EventReport.create((byte) 0x88);
        logger.info("原始消息: 事件ID = {} (0x{})",
                original.getEventIdUnsigned(),
                String.format("%02X", original.getEventIdUnsigned()));

        // 编码消息体
        Buffer encoded = original.encodeBody();
        logger.info("编码后长度: {} 字节", encoded.length());
        logger.info("编码后数据: {}", bytesToHex(encoded.getBytes()));

        // 解码消息体
        T0301EventReport decoded = new T0301EventReport();
        decoded.decodeBody(encoded);
        logger.info("解码后消息: 事件ID = {} (0x{})",
                decoded.getEventIdUnsigned(),
                String.format("%02X", decoded.getEventIdUnsigned()));

        // 验证一致性
        boolean isConsistent = original.equals(decoded);
        logger.info("编解码一致性: {}", isConsistent ? "通过" : "失败");
    }

    /**
     * 事件ID处理演示
     */
    private void demonstrateEventIdHandling() {
        logger.info("\n--- 事件ID处理演示 ---");

        T0301EventReport eventReport = new T0301EventReport();

        // 测试不同的事件ID值
        byte[] testEventIds = {0x00, 0x01, 0x7F, (byte) 0x80, (byte) 0xFF};
        String[] descriptions = {"最小值", "最小正值", "最大正值", "最小负值", "最大值"};

        for (int i = 0; i < testEventIds.length; i++) {
            byte eventId = testEventIds[i];
            eventReport.setEventId(eventId);

            logger.info("{}: 有符号值 = {}, 无符号值 = {} (0x{})",
                    descriptions[i],
                    eventId,
                    eventReport.getEventIdUnsigned(),
                    String.format("%02X", eventReport.getEventIdUnsigned()));
        }
    }

    /**
     * 真实场景演示
     */
    private void demonstrateRealWorldScenarios() {
        logger.info("\n--- 真实场景演示 ---");

        // 场景1：紧急事件报告
        demonstrateEmergencyEvent();

        // 场景2：故障事件报告
        demonstrateFaultEvent();

        // 场景3：自定义事件报告
        demonstrateCustomEvent();
    }

    /**
     * 紧急事件报告场景
     */
    private void demonstrateEmergencyEvent() {
        logger.info("\n-- 场景1: 紧急事件报告 --");

        // 创建紧急事件报告（假设事件ID 0x01 表示紧急报警）
        T0301EventReport emergencyReport = T0301EventReport.create((byte) 0x01);

        // 设置消息头信息
        JT808Header header = new JT808Header();
        header.setPhoneNumber("138888888888");
        header.setSerialNumber(2001);
        emergencyReport.setHeader(header);

        logger.info("紧急事件报告创建完成:");
        logger.info("  终端手机号: {}", header.getPhoneNumber());
        logger.info("  消息流水号: {}", header.getSerialNumber());
        logger.info("  事件ID: {} (紧急报警)", emergencyReport.getEventIdUnsigned());

        // 编码并显示
        Buffer encoded = emergencyReport.encodeBody();
        logger.info("  编码数据: {}", bytesToHex(encoded.getBytes()));
    }

    /**
     * 故障事件报告场景
     */
    private void demonstrateFaultEvent() {
        logger.info("\n-- 场景2: 故障事件报告 --");

        // 创建故障事件报告（假设事件ID 0x05 表示GNSS模块故障）
        T0301EventReport faultReport = T0301EventReport.create((byte) 0x05);

        // 设置消息头信息
        JT808Header header = new JT808Header();
        header.setPhoneNumber("139999999999");
        header.setSerialNumber(2002);
        faultReport.setHeader(header);

        logger.info("故障事件报告创建完成:");
        logger.info("  终端手机号: {}", header.getPhoneNumber());
        logger.info("  消息流水号: {}", header.getSerialNumber());
        logger.info("  事件ID: {} (GNSS模块故障)", faultReport.getEventIdUnsigned());

        // 编码并显示
        Buffer encoded = faultReport.encodeBody();
        logger.info("  编码数据: {}", bytesToHex(encoded.getBytes()));
    }

    /**
     * 自定义事件报告场景
     */
    private void demonstrateCustomEvent() {
        logger.info("\n-- 场景3: 自定义事件报告 --");

        // 创建自定义事件报告（假设事件ID 0x80 表示自定义事件）
        T0301EventReport customReport = T0301EventReport.create((byte) 0x80);

        // 设置消息头信息
        JT808Header header = new JT808Header();
        header.setPhoneNumber("137777777777");
        header.setSerialNumber(2003);
        customReport.setHeader(header);

        logger.info("自定义事件报告创建完成:");
        logger.info("  终端手机号: {}", header.getPhoneNumber());
        logger.info("  消息流水号: {}", header.getSerialNumber());
        logger.info("  事件ID: {} (自定义事件)", customReport.getEventIdUnsigned());

        // 编码并显示
        Buffer encoded = customReport.encodeBody();
        logger.info("  编码数据: {}", bytesToHex(encoded.getBytes()));

        // 演示完整的编解码过程
        T0301EventReport decoded = new T0301EventReport();
        decoded.decodeBody(encoded);
        logger.info("  解码验证: 事件ID = {} ({})",
                decoded.getEventIdUnsigned(),
                decoded.equals(customReport) ? "成功" : "失败");
    }

    /**
     * 字节数组转十六进制字符串
     */
    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X ", b));
        }
        return sb.toString().trim();
    }
}