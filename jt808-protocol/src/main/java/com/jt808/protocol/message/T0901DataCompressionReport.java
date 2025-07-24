package com.jt808.protocol.message;

import com.jt808.protocol.constants.MessageTypes;
import io.vertx.core.buffer.Buffer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Objects;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * T0901数据压缩上报
 * 消息ID: 0x0901
 */
public class T0901DataCompressionReport extends JT808Message {
    
    /**
     * 获取消息ID
     * @return 消息ID 0x0901
     */
    @Override
    public int getMessageId() {
        return 0x0901;
    }
    
    /**
     * 压缩消息长度
     */
    private long compressedMessageLength;
    
    /**
     * 压缩消息体
     */
    private byte[] compressedMessageBody;
    
    /**
     * 默认构造函数
     */
    public T0901DataCompressionReport() {
        super();
        this.compressedMessageLength = 0L;
        this.compressedMessageBody = new byte[0];
    }
    
    /**
     * 构造函数
     * @param compressedMessageBody 压缩消息体
     */
    public T0901DataCompressionReport(byte[] compressedMessageBody) {
        super();
        this.compressedMessageBody = compressedMessageBody != null ? compressedMessageBody.clone() : new byte[0];
        this.compressedMessageLength = this.compressedMessageBody.length;
    }
    
    /**
     * 构造函数（自动压缩原始数据）
     * @param originalData 原始数据
     * @param autoCompress 是否自动压缩
     */
    public T0901DataCompressionReport(byte[] originalData, boolean autoCompress) {
        super();
        if (autoCompress && originalData != null) {
            try {
                this.compressedMessageBody = compressData(originalData);
                this.compressedMessageLength = this.compressedMessageBody.length;
            } catch (IOException e) {
                throw new RuntimeException("数据压缩失败", e);
            }
        } else {
            this.compressedMessageBody = originalData != null ? originalData.clone() : new byte[0];
            this.compressedMessageLength = this.compressedMessageBody.length;
        }
    }
    
    /**
     * 编码消息体
     * @return 编码后的消息体
     */
    @Override
    public Buffer encodeBody() {
        Buffer buffer = Buffer.buffer();
        // 压缩消息长度 (DWORD, 4字节)
        buffer.appendInt((int) compressedMessageLength);
        // 压缩消息体
        if (compressedMessageBody != null && compressedMessageBody.length > 0) {
            buffer.appendBytes(compressedMessageBody);
        }
        return buffer;
    }
    
    /**
     * 解码消息体
     * @param body 消息体数据
     */
    @Override
    public void decodeBody(Buffer body) {
        if (body == null || body.length() < 4) {
            throw new IllegalArgumentException("消息体数据不能为空且长度至少为4字节");
        }
        
        // 读取压缩消息长度 (DWORD, 4字节)
        this.compressedMessageLength = body.getInt(0) & 0xFFFFFFFFL;
        
        // 读取压缩消息体
        if (body.length() > 4) {
            int bodyLength = body.length() - 4;
            this.compressedMessageBody = new byte[bodyLength];
            body.getBytes(4, body.length(), this.compressedMessageBody);
            
            // 验证压缩消息长度是否与实际长度一致
            if (this.compressedMessageLength != bodyLength) {
                throw new IllegalArgumentException(
                    "压缩消息长度不匹配: 声明长度=" + this.compressedMessageLength + ", 实际长度=" + bodyLength);
            }
        } else {
            this.compressedMessageBody = new byte[0];
        }
    }
    
    /**
     * 静态解码方法
     * @param buffer 消息体数据
     * @return 解码后的消息对象
     */
    public static T0901DataCompressionReport decode(Buffer buffer) {
        T0901DataCompressionReport message = new T0901DataCompressionReport();
        message.decodeBody(buffer);
        return message;
    }
    
    /**
     * 编码消息体（兼容旧接口）
     * @return 编码后的消息体
     */
    public Buffer encode() {
        return encodeBody();
    }
    
    /**
     * 压缩数据
     * @param data 原始数据
     * @return 压缩后的数据
     * @throws IOException 压缩异常
     */
    public static byte[] compressData(byte[] data) throws IOException {
        if (data == null || data.length == 0) {
            return new byte[0];
        }
        
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             GZIPOutputStream gzipOut = new GZIPOutputStream(baos)) {
            gzipOut.write(data);
            gzipOut.finish();
            return baos.toByteArray();
        }
    }
    
    /**
     * 解压缩数据
     * @param compressedData 压缩数据
     * @return 解压缩后的数据
     * @throws IOException 解压缩异常
     */
    public static byte[] decompressData(byte[] compressedData) throws IOException {
        if (compressedData == null || compressedData.length == 0) {
            return new byte[0];
        }
        
        try (ByteArrayInputStream bais = new ByteArrayInputStream(compressedData);
             GZIPInputStream gzipIn = new GZIPInputStream(bais);
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            
            byte[] buffer = new byte[1024];
            int len;
            while ((len = gzipIn.read(buffer)) != -1) {
                baos.write(buffer, 0, len);
            }
            return baos.toByteArray();
        }
    }
    
    /**
     * 解压缩当前消息体
     * @return 解压缩后的数据
     * @throws IOException 解压缩异常
     */
    public byte[] decompressMessageBody() throws IOException {
        return decompressData(this.compressedMessageBody);
    }
    
    /**
     * 计算压缩比
     * @param originalLength 原始数据长度
     * @return 压缩比（百分比）
     */
    public double getCompressionRatio(int originalLength) {
        if (originalLength == 0) {
            return 0.0;
        }
        return (1.0 - (double) compressedMessageLength / originalLength) * 100.0;
    }
    
    // Getters and Setters
    public long getCompressedMessageLength() {
        return compressedMessageLength;
    }
    
    public void setCompressedMessageLength(long compressedMessageLength) {
        if (compressedMessageLength < 0) {
            throw new IllegalArgumentException("压缩消息长度不能为负数");
        }
        this.compressedMessageLength = compressedMessageLength;
    }
    
    public byte[] getCompressedMessageBody() {
        return compressedMessageBody != null ? compressedMessageBody.clone() : new byte[0];
    }
    
    public void setCompressedMessageBody(byte[] compressedMessageBody) {
        this.compressedMessageBody = compressedMessageBody != null ? compressedMessageBody.clone() : new byte[0];
        this.compressedMessageLength = this.compressedMessageBody.length;
    }
    
    /**
     * 设置原始数据并自动压缩
     * @param originalData 原始数据
     * @throws IOException 压缩异常
     */
    public void setOriginalDataAndCompress(byte[] originalData) throws IOException {
        this.compressedMessageBody = compressData(originalData);
        this.compressedMessageLength = this.compressedMessageBody.length;
    }
    
    @Override
    public String toString() {
        return "T0901DataCompressionReport{" +
                "compressedMessageLength=" + compressedMessageLength +
                ", compressedMessageBodyLength=" + (compressedMessageBody != null ? compressedMessageBody.length : 0) +
                '}';
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        T0901DataCompressionReport that = (T0901DataCompressionReport) o;
        return compressedMessageLength == that.compressedMessageLength &&
                java.util.Arrays.equals(compressedMessageBody, that.compressedMessageBody);
    }
    
    @Override
    public int hashCode() {
        int result = Objects.hash(compressedMessageLength);
        result = 31 * result + java.util.Arrays.hashCode(compressedMessageBody);
        return result;
    }
}