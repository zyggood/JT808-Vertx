package com.jt808.protocol.util;

import com.jt808.common.util.ByteUtils;
import io.vertx.core.buffer.Buffer;

/**
 * JT808校验码工具类
 * 提供校验码计算和验证的专用方法
 */
public final class ChecksumUtils {

    private ChecksumUtils() {
        // 工具类，禁止实例化
    }

    /**
     * 计算JT808消息的校验码
     * 校验码为消息头和消息体所有字节的异或值
     *
     * @param data 消息数据（不包含标识位和校验码）
     * @return 校验码
     */
    public static byte calculateChecksum(byte[] data) {
        if (data == null || data.length == 0) {
            return 0;
        }
        return ByteUtils.calculateChecksum(data, 0, data.length);
    }

    /**
     * 计算JT808消息的校验码
     *
     * @param data   消息数据
     * @param start  起始位置
     * @param length 数据长度
     * @return 校验码
     */
    public static byte calculateChecksum(byte[] data, int start, int length) {
        if (data == null || start < 0 || length <= 0 || start + length > data.length) {
            return 0;
        }
        return ByteUtils.calculateChecksum(data, start, length);
    }

    /**
     * 计算Buffer的校验码
     *
     * @param buffer 数据缓冲区
     * @return 校验码
     */
    public static byte calculateChecksum(Buffer buffer) {
        if (buffer == null || buffer.length() == 0) {
            return 0;
        }
        return ByteUtils.calculateChecksum(buffer, 0, buffer.length());
    }

    /**
     * 计算Buffer的校验码
     *
     * @param buffer 数据缓冲区
     * @param start  起始位置
     * @param length 数据长度
     * @return 校验码
     */
    public static byte calculateChecksum(Buffer buffer, int start, int length) {
        if (buffer == null || start < 0 || length <= 0 || start + length > buffer.length()) {
            return 0;
        }
        return ByteUtils.calculateChecksum(buffer, start, length);
    }

    /**
     * 验证校验码是否正确
     *
     * @param data             消息数据（不包含标识位和校验码）
     * @param expectedChecksum 期望的校验码
     * @return 校验码是否正确
     */
    public static boolean verifyChecksum(byte[] data, byte expectedChecksum) {
        byte actualChecksum = calculateChecksum(data);
        return actualChecksum == expectedChecksum;
    }

    /**
     * 验证校验码是否正确
     *
     * @param data             消息数据
     * @param start            起始位置
     * @param length           数据长度
     * @param expectedChecksum 期望的校验码
     * @return 校验码是否正确
     */
    public static boolean verifyChecksum(byte[] data, int start, int length, byte expectedChecksum) {
        byte actualChecksum = calculateChecksum(data, start, length);
        return actualChecksum == expectedChecksum;
    }

    /**
     * 验证Buffer的校验码是否正确
     *
     * @param buffer           数据缓冲区
     * @param expectedChecksum 期望的校验码
     * @return 校验码是否正确
     */
    public static boolean verifyChecksum(Buffer buffer, byte expectedChecksum) {
        byte actualChecksum = calculateChecksum(buffer);
        return actualChecksum == expectedChecksum;
    }

    /**
     * 验证Buffer的校验码是否正确
     *
     * @param buffer           数据缓冲区
     * @param start            起始位置
     * @param length           数据长度
     * @param expectedChecksum 期望的校验码
     * @return 校验码是否正确
     */
    public static boolean verifyChecksum(Buffer buffer, int start, int length, byte expectedChecksum) {
        byte actualChecksum = calculateChecksum(buffer, start, length);
        return actualChecksum == expectedChecksum;
    }

    /**
     * 从完整的JT808消息中提取并验证校验码
     * 消息格式：7E + 消息头 + 消息体 + 校验码 + 7E
     *
     * @param completeMessage 完整的JT808消息（包含标识位）
     * @return 校验码验证结果
     */
    public static ChecksumResult verifyCompleteMessage(Buffer completeMessage) {
        if (completeMessage == null || completeMessage.length() < 5) {
            return new ChecksumResult(false, (byte) 0, (byte) 0, "消息长度不足");
        }

        // 检查标识位
        if (completeMessage.getByte(0) != 0x7E ||
                completeMessage.getByte(completeMessage.length() - 1) != 0x7E) {
            return new ChecksumResult(false, (byte) 0, (byte) 0, "消息标识位错误");
        }

        // 提取校验码（倒数第二个字节）
        byte expectedChecksum = completeMessage.getByte(completeMessage.length() - 2);

        // 计算实际校验码（消息头+消息体）
        int dataLength = completeMessage.length() - 3; // 去掉首尾标识位和校验码
        byte actualChecksum = calculateChecksum(completeMessage, 1, dataLength);

        boolean isValid = actualChecksum == expectedChecksum;
        String message = isValid ? "校验码验证成功" : "校验码验证失败";

        return new ChecksumResult(isValid, actualChecksum, expectedChecksum, message);
    }

    /**
     * 校验码验证结果
     */
    public static class ChecksumResult {
        private final boolean valid;
        private final byte actualChecksum;
        private final byte expectedChecksum;
        private final String message;

        public ChecksumResult(boolean valid, byte actualChecksum, byte expectedChecksum, String message) {
            this.valid = valid;
            this.actualChecksum = actualChecksum;
            this.expectedChecksum = expectedChecksum;
            this.message = message;
        }

        /**
         * 校验码是否有效
         */
        public boolean isValid() {
            return valid;
        }

        /**
         * 实际计算的校验码
         */
        public byte getActualChecksum() {
            return actualChecksum;
        }

        /**
         * 期望的校验码
         */
        public byte getExpectedChecksum() {
            return expectedChecksum;
        }

        /**
         * 验证结果消息
         */
        public String getMessage() {
            return message;
        }

        @Override
        public String toString() {
            return String.format("ChecksumResult{valid=%s, actual=0x%02X, expected=0x%02X, message='%s'}",
                    valid, actualChecksum & 0xFF, expectedChecksum & 0xFF, message);
        }
    }
}