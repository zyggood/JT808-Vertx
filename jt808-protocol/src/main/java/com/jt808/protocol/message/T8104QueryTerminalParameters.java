package com.jt808.protocol.message;

import io.vertx.core.buffer.Buffer;

/**
 * 查询终端参数消息 (0x8104)
 * 平台查询终端参数的消息，消息体为空
 */
public class T8104QueryTerminalParameters extends JT808Message {

    public T8104QueryTerminalParameters() {
        super();
    }

    public T8104QueryTerminalParameters(JT808Header header) {
        super(header);
    }

    @Override
    public int getMessageId() {
        return 0x8104;
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
        return "T8104QueryTerminalParameters{" +
                "header=" + getHeader() +
                '}';
    }
}