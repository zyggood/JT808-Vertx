package com.jt808.protocol.message;

import io.vertx.core.buffer.Buffer;
import java.util.*;

/**
 * 终端参数项
 * 用于T8103设置终端参数和T0104查询终端参数应答消息
 */
public class ParameterItem {
    /** 参数ID */
    private long parameterId;
    
    /** 参数值字节数组 */
    private byte[] valueBytes;
    
    public ParameterItem(long parameterId, byte[] valueBytes) {
        this.parameterId = parameterId;
        this.valueBytes = valueBytes != null ? valueBytes.clone() : new byte[0];
    }
    
    /**
     * 创建DWORD类型参数项
     */
    public static ParameterItem createDwordParameter(long parameterId, long value) {
        Buffer buffer = Buffer.buffer();
        buffer.appendUnsignedInt(value);
        return new ParameterItem(parameterId, buffer.getBytes());
    }
    
    /**
     * 创建WORD类型参数项
     */
    public static ParameterItem createWordParameter(long parameterId, int value) {
        Buffer buffer = Buffer.buffer();
        buffer.appendUnsignedShort(value);
        return new ParameterItem(parameterId, buffer.getBytes());
    }
    
    /**
     * 创建BYTE类型参数项
     */
    public static ParameterItem createByteParameter(long parameterId, byte value) {
        return new ParameterItem(parameterId, new byte[]{value});
    }
    
    /**
     * 创建STRING类型参数项
     */
    public static ParameterItem createStringParameter(long parameterId, String value) {
        byte[] bytes = value != null ? value.getBytes() : new byte[0];
        return new ParameterItem(parameterId, bytes);
    }
    
    /**
     * 获取DWORD值
     */
    public Long getDwordValue() {
        if (valueBytes.length >= 4) {
            Buffer buffer = Buffer.buffer(valueBytes);
            return buffer.getUnsignedInt(0);
        }
        return null;
    }
    
    /**
     * 获取WORD值
     */
    public Integer getWordValue() {
        if (valueBytes.length == 2) {
            Buffer buffer = Buffer.buffer(valueBytes);
            return buffer.getUnsignedShort(0);
        }
        return null;
    }
    
    /**
     * 获取BYTE值
     */
    public Byte getByteValue() {
        if (valueBytes.length >= 1) {
            return valueBytes[0];
        }
        return null;
    }
    
    /**
     * 获取STRING值
     */
    public String getStringValue() {
        return new String(valueBytes);
    }
    
    /**
     * 获取参数类型描述
     */
    public String getParameterDescription() {
        return ParameterDefinitions.getParameterDescription(parameterId);
    }
    
    // Getters and Setters
    public long getParameterId() {
        return parameterId;
    }
    
    public void setParameterId(long parameterId) {
        this.parameterId = parameterId;
    }
    
    public byte[] getValueBytes() {
        return valueBytes.clone();
    }
    
    public void setValueBytes(byte[] valueBytes) {
        this.valueBytes = valueBytes != null ? valueBytes.clone() : new byte[0];
    }
    
    /**
     * 获取参数长度
     */
    public int getParameterLength() {
        return valueBytes.length;
    }
    
    @Override
    public String toString() {
        String description = getParameterDescription();
        String valueStr;
        
        // 根据参数类型显示值
        if (valueBytes.length == 4) {
            Long dwordValue = getDwordValue();
            valueStr = dwordValue != null ? String.valueOf(dwordValue) : "null";
        } else if (valueBytes.length == 2) {
            Integer wordValue = getWordValue();
            valueStr = wordValue != null ? String.valueOf(wordValue) : "null";
        } else if (valueBytes.length == 1) {
            Byte byteValue = getByteValue();
            valueStr = byteValue != null ? String.valueOf(byteValue & 0xFF) : "null";
        } else {
            valueStr = getStringValue();
        }
        
        return String.format("ParameterItem{id=0x%04X, length=%d, value=%s, description=%s}", 
                parameterId, valueBytes.length, valueStr, description);
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ParameterItem that = (ParameterItem) o;
        return parameterId == that.parameterId && Arrays.equals(valueBytes, that.valueBytes);
    }
    
    @Override
    public int hashCode() {
        int result = Objects.hash(parameterId);
        result = 31 * result + Arrays.hashCode(valueBytes);
        return result;
    }
}