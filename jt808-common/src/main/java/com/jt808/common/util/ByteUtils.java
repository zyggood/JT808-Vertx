package com.jt808.common.util;

import io.vertx.core.buffer.Buffer;

/**
 * 字节操作工具类
 */
public final class ByteUtils {
    
    private ByteUtils() {
        // 工具类，禁止实例化
    }
    
    /**
     * 计算校验码（异或校验）
     * @param data 数据
     * @param start 起始位置
     * @param length 长度
     * @return 校验码
     */
    public static byte calculateChecksum(byte[] data, int start, int length) {
        byte checksum = 0;
        for (int i = start; i < start + length; i++) {
            checksum ^= data[i];
        }
        return checksum;
    }
    
    /**
     * 计算校验码（异或校验）
     * @param buffer 缓冲区
     * @param start 起始位置
     * @param length 长度
     * @return 校验码
     */
    public static byte calculateChecksum(Buffer buffer, int start, int length) {
        byte checksum = 0;
        for (int i = start; i < start + length; i++) {
            checksum ^= buffer.getByte(i);
        }
        return checksum;
    }
    
    /**
     * 转义处理
     * @param data 原始数据
     * @return 转义后的数据
     */
    public static Buffer escape(Buffer data) {
        Buffer result = Buffer.buffer();
        for (int i = 0; i < data.length(); i++) {
            byte b = data.getByte(i);
            if (b == 0x7E) {
                result.appendByte((byte) 0x7D);
                result.appendByte((byte) 0x02);
            } else if (b == 0x7D) {
                result.appendByte((byte) 0x7D);
                result.appendByte((byte) 0x01);
            } else {
                result.appendByte(b);
            }
        }
        return result;
    }
    
    /**
     * 反转义处理
     * @param data 转义后的数据
     * @return 原始数据
     */
    public static Buffer unescape(Buffer data) {
        Buffer result = Buffer.buffer();
        for (int i = 0; i < data.length(); i++) {
            byte b = data.getByte(i);
            if (b == 0x7D && i + 1 < data.length()) {
                byte next = data.getByte(i + 1);
                if (next == 0x02) {
                    result.appendByte((byte) 0x7E);
                    i++; // 跳过下一个字节
                } else if (next == 0x01) {
                    result.appendByte((byte) 0x7D);
                    i++; // 跳过下一个字节
                } else {
                    result.appendByte(b);
                }
            } else {
                result.appendByte(b);
            }
        }
        return result;
    }
    
    /**
     * 字节数组转十六进制字符串
     * @param bytes 字节数组
     * @return 十六进制字符串
     */
    public static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X", b & 0xFF));
        }
        return sb.toString();
    }
    
    /**
     * 十六进制字符串转字节数组
     * @param hex 十六进制字符串
     * @return 字节数组
     * TODO 非法数据抛出异常
     */
    public static byte[] hexToBytes(String hex) {
        int length = hex.length();
        byte[] bytes = new byte[length / 2];
        for (int i = 0; i < length; i += 2) {
            bytes[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                    + Character.digit(hex.charAt(i + 1), 16));
        }
        return bytes;
    }
    
    /**
     * BCD编码
     * @param value 数值
     * @param length 字节长度
     * @return BCD编码后的字节数组
     */
    public static byte[] toBCD(long value, int length) {
        byte[] bcd = new byte[length];
        String str = String.format("%0" + (length * 2) + "d", value);
        for (int i = 0; i < length; i++) {
            int high = Character.getNumericValue(str.charAt(i * 2));
            int low = Character.getNumericValue(str.charAt(i * 2 + 1));
            bcd[i] = (byte) ((high << 4) | low);
        }
        return bcd;
    }
    
    /**
     * BCD解码
     * @param bcd BCD编码的字节数组
     * @return 数值
     */
    public static long fromBCD(byte[] bcd) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bcd) {
            int high = (b >> 4) & 0x0F;
            int low = b & 0x0F;
            sb.append(high).append(low);
        }
        return Long.parseLong(sb.toString());
    }
}