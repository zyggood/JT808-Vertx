package com.jt808.protocol.util;

import io.vertx.core.buffer.Buffer;
import com.jt808.common.util.ByteUtils;

/**
 * JT808转义处理工具类
 * 提供消息转义和反转义的专用方法
 */
public final class EscapeUtils {
    
    /** JT808协议标识位 */
    public static final byte PROTOCOL_FLAG = 0x7E;
    
    /** 转义标识符 */
    public static final byte ESCAPE_FLAG = 0x7D;
    
    /** 0x7E的转义值 */
    public static final byte ESCAPE_7E = 0x02;
    
    /** 0x7D的转义值 */
    public static final byte ESCAPE_7D = 0x01;
    
    private EscapeUtils() {
        // 工具类，禁止实例化
    }
    
    /**
     * 对数据进行转义处理
     * 转义规则：
     * - 0x7E -> 0x7D 0x02
     * - 0x7D -> 0x7D 0x01
     * 
     * @param data 原始数据
     * @return 转义后的数据
     */
    public static Buffer escape(Buffer data) {
        if (data == null) {
            return Buffer.buffer();
        }
        return ByteUtils.escape(data);
    }
    
    /**
     * 对字节数组进行转义处理
     * 
     * @param data 原始数据
     * @return 转义后的数据
     */
    public static Buffer escape(byte[] data) {
        if (data == null || data.length == 0) {
            return Buffer.buffer();
        }
        return escape(Buffer.buffer(data));
    }
    
    /**
     * 对数据进行反转义处理
     * 反转义规则：
     * - 0x7D 0x02 -> 0x7E
     * - 0x7D 0x01 -> 0x7D
     * 
     * @param data 转义后的数据
     * @return 原始数据
     */
    public static Buffer unescape(Buffer data) {
        if (data == null) {
            return Buffer.buffer();
        }
        return ByteUtils.unescape(data);
    }
    
    /**
     * 对字节数组进行反转义处理
     * 
     * @param data 转义后的数据
     * @return 原始数据
     */
    public static Buffer unescape(byte[] data) {
        if (data == null || data.length == 0) {
            return Buffer.buffer();
        }
        return unescape(Buffer.buffer(data));
    }
    
    /**
     * 检查数据是否需要转义
     * 
     * @param data 数据
     * @return 是否需要转义
     */
    public static boolean needsEscape(Buffer data) {
        if (data == null || data.length() == 0) {
            return false;
        }
        
        for (int i = 0; i < data.length(); i++) {
            byte b = data.getByte(i);
            if (b == PROTOCOL_FLAG || b == ESCAPE_FLAG) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * 检查字节数组是否需要转义
     * 
     * @param data 数据
     * @return 是否需要转义
     */
    public static boolean needsEscape(byte[] data) {
        if (data == null || data.length == 0) {
            return false;
        }
        
        for (byte b : data) {
            if (b == PROTOCOL_FLAG || b == ESCAPE_FLAG) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * 统计数据中需要转义的字节数量
     * 
     * @param data 数据
     * @return 需要转义的字节数量
     */
    public static int countEscapeBytes(Buffer data) {
        if (data == null || data.length() == 0) {
            return 0;
        }
        
        int count = 0;
        for (int i = 0; i < data.length(); i++) {
            byte b = data.getByte(i);
            if (b == PROTOCOL_FLAG || b == ESCAPE_FLAG) {
                count++;
            }
        }
        return count;
    }
    
    /**
     * 统计字节数组中需要转义的字节数量
     * 
     * @param data 数据
     * @return 需要转义的字节数量
     */
    public static int countEscapeBytes(byte[] data) {
        if (data == null || data.length == 0) {
            return 0;
        }
        
        int count = 0;
        for (byte b : data) {
            if (b == PROTOCOL_FLAG || b == ESCAPE_FLAG) {
                count++;
            }
        }
        return count;
    }
    
    /**
     * 计算转义后的数据长度
     * 
     * @param originalLength 原始数据长度
     * @param escapeCount 需要转义的字节数量
     * @return 转义后的数据长度
     */
    public static int calculateEscapedLength(int originalLength, int escapeCount) {
        return originalLength + escapeCount;
    }
    
    /**
     * 验证转义数据的完整性
     * 检查转义数据中是否存在不完整的转义序列
     * 
     * @param escapedData 转义后的数据
     * @return 验证结果
     */
    public static EscapeValidationResult validateEscapedData(Buffer escapedData) {
        if (escapedData == null) {
            return new EscapeValidationResult(true, "数据为空");
        }
        
        for (int i = 0; i < escapedData.length(); i++) {
            byte b = escapedData.getByte(i);
            
            // 检查是否有未转义的标识位
            if (b == PROTOCOL_FLAG) {
                return new EscapeValidationResult(false, 
                    String.format("位置 %d 存在未转义的标识位 0x7E", i));
            }
            
            // 检查转义序列的完整性
            if (b == ESCAPE_FLAG) {
                if (i + 1 >= escapedData.length()) {
                    return new EscapeValidationResult(false, 
                        String.format("位置 %d 的转义序列不完整", i));
                }
                
                byte next = escapedData.getByte(i + 1);
                if (next != ESCAPE_7E && next != ESCAPE_7D) {
                    return new EscapeValidationResult(false, 
                        String.format("位置 %d 的转义序列无效：0x7D 0x%02X", i, next & 0xFF));
                }
                
                i++; // 跳过下一个字节
            }
        }
        
        return new EscapeValidationResult(true, "转义数据验证通过");
    }
    
    /**
     * 验证转义数据的完整性
     * 
     * @param escapedData 转义后的数据
     * @return 验证结果
     */
    public static EscapeValidationResult validateEscapedData(byte[] escapedData) {
        if (escapedData == null || escapedData.length == 0) {
            return new EscapeValidationResult(true, "数据为空");
        }
        return validateEscapedData(Buffer.buffer(escapedData));
    }
    
    /**
     * 转义验证结果
     */
    public static class EscapeValidationResult {
        private final boolean valid;
        private final String message;
        
        public EscapeValidationResult(boolean valid, String message) {
            this.valid = valid;
            this.message = message;
        }
        
        /**
         * 转义数据是否有效
         */
        public boolean isValid() {
            return valid;
        }
        
        /**
         * 验证结果消息
         */
        public String getMessage() {
            return message;
        }
        
        @Override
        public String toString() {
            return String.format("EscapeValidationResult{valid=%s, message='%s'}", valid, message);
        }
    }
}