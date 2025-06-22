package com.jt808.protocol.message;

import io.vertx.core.buffer.Buffer;

/**
 * 终端心跳消息 (0x0002)
 * 终端定期向平台发送心跳包，保持连接活跃
 */
public class T0002TerminalHeartbeat extends JT808Message {
    
    public T0002TerminalHeartbeat() {
        super();
    }
    
    public T0002TerminalHeartbeat(JT808Header header) {
        super(header);
    }
    
    @Override
    public int getMessageId() {
        return 0x0002;
    }
    
    @Override
    public Buffer encodeBody() {
        // 心跳消息没有消息体
        return Buffer.buffer();
    }
    
    @Override
    public void decodeBody(Buffer body) {
        // 心跳消息没有消息体，无需解码
    }
    
    @Override
    public String toString() {
        return "T0002TerminalHeartbeat{" +
                "header=" + getHeader() +
                '}';
    }
}