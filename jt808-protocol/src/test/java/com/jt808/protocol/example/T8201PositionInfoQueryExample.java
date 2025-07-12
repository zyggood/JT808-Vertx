package com.jt808.protocol.example;

import com.jt808.protocol.message.T8201PositionInfoQuery;
import io.vertx.core.buffer.Buffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * T8201位置信息查询消息示例
 * <p>
 * 该示例演示了如何使用T8201PositionInfoQuery类来创建、编码和解码
 * 位置信息查询消息。
 *
 * @author JT808 Protocol Team
 * @version 1.0
 */
public class T8201PositionInfoQueryExample {

    private static final Logger logger = LoggerFactory.getLogger(T8201PositionInfoQueryExample.class);

    public static void main(String[] args) {
        logger.info("=== T8201位置信息查询消息示例 ===");

        // 示例1：使用构造函数创建消息
        demonstrateConstructorUsage();

        // 示例2：使用静态工厂方法创建消息
        demonstrateStaticFactoryMethod();

        // 示例3：消息编解码示例
        demonstrateEncodeDecodeProcess();

        // 示例4：消息属性和方法示例
        demonstrateMessageProperties();

        // 示例5：异常处理示例
        demonstrateExceptionHandling();
    }

    /**
     * 演示构造函数的使用
     */
    private static void demonstrateConstructorUsage() {
        logger.info("\n--- 构造函数使用示例 ---");

        // 使用默认构造函数
        T8201PositionInfoQuery message = new T8201PositionInfoQuery();
        logger.info("创建的消息: " + message);
        logger.info("消息ID: 0x" + Integer.toHexString(message.getMessageId()).toUpperCase());
        logger.info("消息描述: " + message.getMessageDescription());
    }

    /**
     * 演示静态工厂方法的使用
     */
    private static void demonstrateStaticFactoryMethod() {
        logger.info("\n--- 静态工厂方法示例 ---");

        // 使用静态工厂方法创建消息
        T8201PositionInfoQuery message = T8201PositionInfoQuery.create();
        logger.info("工厂创建的消息: " + message);

        // 验证消息属性
        logger.info("消息ID验证: " + (message.getMessageId() == 0x8201 ? "通过" : "失败"));
        logger.info("消息描述验证: " + ("位置信息查询".equals(message.getMessageDescription()) ? "通过" : "失败"));
    }

    /**
     * 演示消息编解码过程
     */
    private static void demonstrateEncodeDecodeProcess() {
        logger.info("\n--- 编解码过程示例 ---");

        // 创建原始消息
        T8201PositionInfoQuery originalMessage = T8201PositionInfoQuery.create();
        logger.info("原始消息: " + originalMessage);

        // 编码消息
        Buffer encodedBuffer = originalMessage.encodeBody();
        logger.info("编码后的数据长度: " + encodedBuffer.length() + " 字节");
        logger.info("编码后的字节数据: " + (encodedBuffer.length() == 0 ? "空" : bytesToHexString(encodedBuffer.getBytes())));

        // 解码消息
        T8201PositionInfoQuery decodedMessage = new T8201PositionInfoQuery();
        try {
            decodedMessage.decodeBody(encodedBuffer);
            logger.info("解码后的消息: " + decodedMessage);

            // 验证编解码一致性
            boolean isConsistent = originalMessage.getMessageId() == decodedMessage.getMessageId() &&
                    originalMessage.getMessageDescription().equals(decodedMessage.getMessageDescription());
            logger.info("编解码一致性检查: " + (isConsistent ? "通过" : "失败"));
        } catch (Exception e) {
            logger.info("解码失败: " + e.getMessage());
        }
    }

    /**
     * 演示消息属性和方法
     */
    private static void demonstrateMessageProperties() {
        logger.info("\n--- 消息属性和方法示例 ---");

        T8201PositionInfoQuery message1 = new T8201PositionInfoQuery();
        T8201PositionInfoQuery message2 = T8201PositionInfoQuery.create();

        // 测试equals方法
        logger.info("两个消息是否相等: " + message1.equals(message2));

        // 测试hashCode方法
        logger.info("消息1的hashCode: " + message1.hashCode());
        logger.info("消息2的hashCode: " + message2.hashCode());
        logger.info("hashCode是否相等: " + (message1.hashCode() == message2.hashCode()));

        // 测试toString方法
        logger.info("消息的字符串表示: " + message1.toString());

        // 测试消息的不可变性
        int originalMessageId = message1.getMessageId();
        String originalDescription = message1.getMessageDescription();

        // 执行编码操作
        message1.encodeBody();

        // 验证消息状态未改变
        boolean unchanged = (originalMessageId == message1.getMessageId()) &&
                originalDescription.equals(message1.getMessageDescription());
        logger.info("消息不可变性验证: " + (unchanged ? "通过" : "失败"));
    }

    /**
     * 演示异常处理
     */
    private static void demonstrateExceptionHandling() {
        logger.info("\n--- 异常处理示例 ---");

        T8201PositionInfoQuery message = new T8201PositionInfoQuery();

        // 测试正常的空消息体解码
        try {
            message.decodeBody(Buffer.buffer());
            logger.info("空消息体解码: 成功");
        } catch (Exception e) {
            logger.info("空消息体解码失败: " + e.getMessage());
        }

        // 测试null消息体解码
        try {
            message.decodeBody(null);
            logger.info("null消息体解码: 成功");
        } catch (Exception e) {
            logger.info("null消息体解码失败: " + e.getMessage());
        }

        // 测试非空消息体解码（应该抛出异常）
        try {
            Buffer nonEmptyBuffer = Buffer.buffer().appendByte((byte) 0x01);
            message.decodeBody(nonEmptyBuffer);
            logger.info("非空消息体解码: 意外成功（应该失败）");
        } catch (IllegalArgumentException e) {
            logger.info("非空消息体解码: 正确抛出异常 - " + e.getMessage());
        } catch (Exception e) {
            logger.info("非空消息体解码: 抛出了意外异常 - " + e.getMessage());
        }

        // 测试多字节非空消息体解码
        try {
            Buffer multiByteBuffer = Buffer.buffer().appendBytes(new byte[]{0x01, 0x02, 0x03});
            message.decodeBody(multiByteBuffer);
            logger.info("多字节消息体解码: 意外成功（应该失败）");
        } catch (IllegalArgumentException e) {
            logger.info("多字节消息体解码: 正确抛出异常 - " + e.getMessage());
        } catch (Exception e) {
            logger.info("多字节消息体解码: 抛出了意外异常 - " + e.getMessage());
        }
    }

    /**
     * 将字节数组转换为十六进制字符串
     *
     * @param bytes 字节数组
     * @return 十六进制字符串
     */
    private static String bytesToHexString(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X ", b & 0xFF));
        }
        return sb.toString().trim();
    }
}