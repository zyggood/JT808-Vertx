package com.jt808.protocol.codec;

import com.jt808.common.JT808Constants;
import com.jt808.common.util.ByteUtils;
import com.jt808.protocol.message.JT808Header;
import com.jt808.protocol.message.JT808Message;
import io.vertx.core.buffer.Buffer;

/**
 * JT808消息编码器
 */
public class JT808Encoder {

    /**
     * 编码JT808消息
     *
     * @param message JT808消息
     * @return 编码后的数据
     */
    public Buffer encode(JT808Message message) {
        Buffer buffer = Buffer.buffer();

        // 编码消息体
        Buffer body = message.encodeBody();
        if (body == null) {
            body = Buffer.buffer();
        }

        // 更新消息头中的消息体长度
        JT808Header header = message.getHeader();
        header.setBodyLength(body.length());

        // 编码消息头
        Buffer headerBuffer = encodeHeader(header);

        // 组装消息（不包括标识位和校验码）
        Buffer messageBuffer = Buffer.buffer();
        messageBuffer.appendBuffer(headerBuffer);
        messageBuffer.appendBuffer(body);

        // 计算校验码
        byte checksum = ByteUtils.calculateChecksum(messageBuffer, 0, messageBuffer.length());

        // 添加校验码
        messageBuffer.appendByte(checksum);

        // 转义处理
        Buffer escapedBuffer = ByteUtils.escape(messageBuffer);

        // 添加标识位
        buffer.appendByte(JT808Constants.PROTOCOL_FLAG);
        buffer.appendBuffer(escapedBuffer);
        buffer.appendByte(JT808Constants.PROTOCOL_FLAG);

        return buffer;
    }

    /**
     * 编码消息头
     *
     * @param header 消息头
     * @return 编码后的消息头
     */
    private Buffer encodeHeader(JT808Header header) {
        Buffer buffer = Buffer.buffer();

        // 消息ID（2字节）
        buffer.appendUnsignedShort(header.getMessageId());

        // 消息体属性（2字节）
        buffer.appendUnsignedShort(header.getMessageProperty());

        // 协议版本号（1字节，2019版本才有）
        if (header.getProtocolVersion() != 0) {
            buffer.appendByte(header.getProtocolVersion());
        }

        // 终端手机号（6字节BCD码）
        String phoneNumber = header.getPhoneNumber();
        if (phoneNumber != null && phoneNumber.length() <= 12) {
            // 左侧补齐到12位（用0填充）
            phoneNumber = String.format("%012d", Long.parseLong(phoneNumber));
            byte[] phoneBcd = ByteUtils.toBCD(Long.parseLong(phoneNumber), 6);
            buffer.appendBytes(phoneBcd);
        } else {
            // 默认填充0
            buffer.appendBytes(new byte[6]);
        }

        // 消息流水号（2字节）
        buffer.appendUnsignedShort(header.getSerialNumber());

        // 消息包封装项（分包时才有，4字节）
        if (header.isSubpackage() && header.getPackageInfo() != null) {
            JT808Header.PackageInfo packageInfo = header.getPackageInfo();
            buffer.appendUnsignedShort(packageInfo.getTotalPackages());
            buffer.appendUnsignedShort(packageInfo.getPackageSequence());
        }

        return buffer;
    }
}