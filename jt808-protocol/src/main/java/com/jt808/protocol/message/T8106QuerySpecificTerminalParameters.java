package com.jt808.protocol.message;

import io.vertx.core.buffer.Buffer;
import java.util.ArrayList;
import java.util.List;

/**
 * 查询指定终端参数消息 (0x8106)
 * 平台查询指定终端参数的消息
 */
public class T8106QuerySpecificTerminalParameters extends JT808Message {
    
    /**
     * 参数ID列表
     */
    private List<Integer> parameterIds;
    
    public T8106QuerySpecificTerminalParameters() {
        super();
        this.parameterIds = new ArrayList<>();
    }
    
    public T8106QuerySpecificTerminalParameters(JT808Header header) {
        super(header);
        this.parameterIds = new ArrayList<>();
    }
    
    public T8106QuerySpecificTerminalParameters(List<Integer> parameterIds) {
        super();
        this.parameterIds = parameterIds != null ? new ArrayList<>(parameterIds) : new ArrayList<>();
    }
    
    @Override
    public int getMessageId() {
        return 0x8106;
    }
    
    @Override
    public Buffer encodeBody() {
        Buffer buffer = Buffer.buffer();
        
        // 参数总数 (1字节)
        buffer.appendByte((byte) parameterIds.size());
        
        // 参数ID列表 (每个参数ID 4字节)
        for (Integer parameterId : parameterIds) {
            buffer.appendInt(parameterId);
        }
        
        return buffer;
    }
    
    @Override
    public void decodeBody(Buffer body) {
        if (body == null || body.length() < 1) {
            return;
        }
        
        int offset = 0;
        
        // 参数总数 (1字节)
        int parameterCount = body.getUnsignedByte(offset);
        offset += 1;
        
        // 检查消息体长度是否足够
        if (body.length() < 1 + parameterCount * 4) {
            throw new IllegalArgumentException("消息体长度不足，期望长度: " + (1 + parameterCount * 4) + ", 实际长度: " + body.length());
        }
        
        // 参数ID列表 (每个参数ID 4字节)
        parameterIds = new ArrayList<>(parameterCount);
        for (int i = 0; i < parameterCount; i++) {
            int parameterId = body.getInt(offset);
            parameterIds.add(parameterId);
            offset += 4;
        }
    }
    
    /**
     * 获取参数ID列表
     * @return 参数ID列表
     */
    public List<Integer> getParameterIds() {
        return new ArrayList<>(parameterIds);
    }
    
    /**
     * 设置参数ID列表
     * @param parameterIds 参数ID列表
     */
    public void setParameterIds(List<Integer> parameterIds) {
        this.parameterIds = parameterIds != null ? new ArrayList<>(parameterIds) : new ArrayList<>();
    }
    
    /**
     * 添加参数ID
     * @param parameterId 参数ID
     */
    public void addParameterId(int parameterId) {
        this.parameterIds.add(parameterId);
    }
    
    /**
     * 获取参数总数
     * @return 参数总数
     */
    public int getParameterCount() {
        return parameterIds.size();
    }
    
    @Override
    public String toString() {
        return "T8106QuerySpecificTerminalParameters{" +
                "parameterCount=" + parameterIds.size() +
                ", parameterIds=" + parameterIds +
                ", header=" + getHeader() +
                '}';
    }
}