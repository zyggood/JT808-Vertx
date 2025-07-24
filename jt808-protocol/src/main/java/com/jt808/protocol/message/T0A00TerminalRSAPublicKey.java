package com.jt808.protocol.message;

import com.jt808.protocol.constants.MessageTypes;
import io.vertx.core.buffer.Buffer;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Objects;

/**
 * 终端RSA公钥消息
 * 消息ID: 0x0A00
 * 
 * 该消息用于终端向平台上报RSA公钥，包含RSA公钥的e和n参数。
 * RSA公钥格式为{e,n}，其中：
 * - e: 公钥指数，4字节DWORD
 * - n: 公钥模数，128字节数组
 * 
 * @author JT808-Vertx
 * @since 1.0.0
 */
public class T0A00TerminalRSAPublicKey extends JT808Message {
    
    /**
     * RSA公钥指数e（4字节）
     */
    private long publicExponent;
    
    /**
     * RSA公钥模数n（128字节）
     */
    private byte[] modulus;
    
    /**
     * 默认构造函数
     */
    public T0A00TerminalRSAPublicKey() {
        super();
        this.publicExponent = 0L;
        this.modulus = new byte[128];
    }
    
    /**
     * 构造函数
     * 
     * @param publicExponent RSA公钥指数e
     * @param modulus RSA公钥模数n（128字节）
     */
    public T0A00TerminalRSAPublicKey(long publicExponent, byte[] modulus) {
        super();
        this.publicExponent = publicExponent;
        setModulus(modulus);
    }
    
    /**
     * 构造函数（使用BigInteger）
     * 
     * @param publicExponent RSA公钥指数e
     * @param modulus RSA公钥模数n
     */
    public T0A00TerminalRSAPublicKey(BigInteger publicExponent, BigInteger modulus) {
        super();
        this.publicExponent = publicExponent.longValue();
        setModulusFromBigInteger(modulus);
    }
    
    /**
     * 获取消息ID
     * @return 消息ID 0x0A00
     */
    @Override
    public int getMessageId() {
        return MessageTypes.Terminal.TERMINAL_RSA_PUBLIC_KEY;
    }
    
    @Override
    public Buffer encodeBody() {
        Buffer buffer = Buffer.buffer();
        
        // 写入公钥指数e（4字节DWORD，大端序）
        buffer.appendUnsignedInt(publicExponent);
        
        // 写入公钥模数n（128字节）
        buffer.appendBytes(modulus);
        
        return buffer;
    }
    
    @Override
    public void decodeBody(Buffer buffer) {
        if (buffer.length() < 132) { // 4 + 128 = 132
            throw new IllegalArgumentException("消息体长度不足，期望132字节，实际" + buffer.length() + "字节");
        }
        
        int offset = 0;
        
        // 读取公钥指数e（4字节DWORD，大端序）
        this.publicExponent = buffer.getUnsignedInt(offset);
        offset += 4;
        
        // 读取公钥模数n（128字节）
        this.modulus = new byte[128];
        buffer.getBytes(offset, offset + 128, this.modulus);
    }
    
    /**
     * 获取RSA公钥指数e
     * 
     * @return 公钥指数e
     */
    public long getPublicExponent() {
        return publicExponent;
    }
    
    /**
     * 设置RSA公钥指数e
     * 
     * @param publicExponent 公钥指数e
     */
    public void setPublicExponent(long publicExponent) {
        this.publicExponent = publicExponent;
    }
    
    /**
     * 获取RSA公钥指数e（BigInteger格式）
     * 
     * @return 公钥指数e
     */
    public BigInteger getPublicExponentAsBigInteger() {
        return BigInteger.valueOf(publicExponent);
    }
    
    /**
     * 获取RSA公钥模数n
     * 
     * @return 公钥模数n的副本
     */
    public byte[] getModulus() {
        return Arrays.copyOf(modulus, modulus.length);
    }
    
    /**
     * 设置RSA公钥模数n
     * 
     * @param modulus 公钥模数n（128字节）
     */
    public void setModulus(byte[] modulus) {
        if (modulus != null && modulus.length == 128) {
            this.modulus = Arrays.copyOf(modulus, 128);
        } else {
            this.modulus = new byte[128];
            if (modulus != null) {
                System.arraycopy(modulus, 0, this.modulus, 0, Math.min(modulus.length, 128));
            }
        }
    }
    
    /**
     * 获取RSA公钥模数n（BigInteger格式）
     * 
     * @return 公钥模数n
     */
    public BigInteger getModulusAsBigInteger() {
        return new BigInteger(1, modulus);
    }
    
    /**
     * 设置RSA公钥模数n（BigInteger格式）
     * 
     * @param modulus 公钥模数n
     */
    public void setModulusFromBigInteger(BigInteger modulus) {
        byte[] modulusBytes = modulus.toByteArray();
        this.modulus = new byte[128];
        
        if (modulusBytes.length >= 128) {
            System.arraycopy(modulusBytes, modulusBytes.length - 128, this.modulus, 0, 128);
        } else {
            System.arraycopy(modulusBytes, 0, this.modulus, 128 - modulusBytes.length, modulusBytes.length);
        }
    }
    
    /**
     * 验证RSA公钥参数是否有效
     * 
     * @return 如果公钥参数有效返回true，否则返回false
     */
    public boolean isValidRSAKey() {
        // 检查公钥指数e是否为常用值（通常为65537或3）
        if (publicExponent != 65537L && publicExponent != 3L && publicExponent != 17L) {
            return false;
        }
        
        // 检查模数n是否为空或全零
        if (modulus == null || modulus.length != 128) {
            return false;
        }
        
        boolean allZero = true;
        for (byte b : modulus) {
            if (b != 0) {
                allZero = false;
                break;
            }
        }
        
        return !allZero;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        T0A00TerminalRSAPublicKey that = (T0A00TerminalRSAPublicKey) o;
        return publicExponent == that.publicExponent &&
               Arrays.equals(modulus, that.modulus);
    }
    
    @Override
    public int hashCode() {
        int result = Objects.hash(publicExponent);
        result = 31 * result + Arrays.hashCode(modulus);
        return result;
    }
    
    @Override
    public String toString() {
        return "T0A00TerminalRSAPublicKey{" +
                "messageId=0x" + Integer.toHexString(getMessageId()).toUpperCase() +
                ", publicExponent=" + publicExponent +
                ", modulus=" + bytesToHex(modulus) +
                ", isValid=" + isValidRSAKey() +
                "}";
    }
    
    /**
     * 将字节数组转换为十六进制字符串
     * 
     * @param bytes 字节数组
     * @return 十六进制字符串
     */
    private String bytesToHex(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return "";
        }
        
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < Math.min(bytes.length, 16); i++) { // 只显示前16字节
            sb.append(String.format("%02X", bytes[i]));
            if (i < Math.min(bytes.length, 16) - 1) {
                sb.append(" ");
            }
        }
        
        if (bytes.length > 16) {
            sb.append("...(共").append(bytes.length).append("字节)");
        }
        
        return sb.toString();
    }
}