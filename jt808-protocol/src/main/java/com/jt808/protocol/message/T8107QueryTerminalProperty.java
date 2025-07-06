package com.jt808.protocol.message;

import io.vertx.core.buffer.Buffer;

/**
 * 查询终端属性消息 (0x8107)
 * 平台查询终端属性的消息，消息体为空
 */
public class T8107QueryTerminalProperty extends JT808Message {

    public T8107QueryTerminalProperty() {
    }

    public T8107QueryTerminalProperty(JT808Header header) {
        super(header);
    }

    @Override
    public int getMessageId() {
        return 0x8107;
    }

    @Override
    public Buffer encodeBody() {
        // 消息体为空
        return Buffer.buffer();
    }

    @Override
    public void decodeBody(Buffer body) {
        // 消息体为空，无需解码
    }

    @Override
    public String toString() {
        return "T8107QueryTerminalProperty{" +
                "header=" + getHeader() +
                '}';
    }
}
