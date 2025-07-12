package com.jt808.protocol.message;

import io.vertx.core.buffer.Buffer;

/**
 * 终端通用应答消息 (0x0001)
 * 终端对平台消息的通用应答
 */
public class T0001TerminalCommonResponse extends JT808Message {

    // 结果常量定义
    public static final byte RESULT_SUCCESS = 0x00;           // 成功/确认
    public static final byte RESULT_FAILURE = 0x01;           // 失败
    public static final byte RESULT_MESSAGE_ERROR = 0x02;     // 消息有误
    public static final byte RESULT_NOT_SUPPORTED = 0x03;     // 不支持
    /**
     * 应答流水号 (对应的平台消息的流水号)
     */
    private int responseSerialNumber;
    /**
     * 应答ID (对应的平台消息的ID)
     */
    private int responseMessageId;
    /**
     * 结果
     */
    private byte result;

    public T0001TerminalCommonResponse() {
        super();
    }

    public T0001TerminalCommonResponse(JT808Header header) {
        super(header);
    }

    /**
     * 构造终端通用应答
     *
     * @param responseSerialNumber 应答流水号
     * @param responseMessageId    应答消息ID
     * @param result               结果
     */
    public T0001TerminalCommonResponse(int responseSerialNumber, int responseMessageId, byte result) {
        this.responseSerialNumber = responseSerialNumber;
        this.responseMessageId = responseMessageId;
        this.result = result;
    }

    /**
     * 创建成功应答
     */
    public static T0001TerminalCommonResponse createSuccessResponse(int serialNumber, int messageId) {
        return new T0001TerminalCommonResponse(serialNumber, messageId, RESULT_SUCCESS);
    }

    /**
     * 创建失败应答
     */
    public static T0001TerminalCommonResponse createFailureResponse(int serialNumber, int messageId, byte result) {
        return new T0001TerminalCommonResponse(serialNumber, messageId, result);
    }

    @Override
    public int getMessageId() {
        return 0x0001;
    }

    @Override
    public Buffer encodeBody() {
        Buffer buffer = Buffer.buffer();

        // 应答流水号 (2字节)
        buffer.appendUnsignedShort(responseSerialNumber);

        // 应答ID (2字节)
        buffer.appendUnsignedShort(responseMessageId);

        // 结果 (1字节)
        buffer.appendByte(result);

        return buffer;
    }

    @Override
    public void decodeBody(Buffer body) {
        // 检查消息体长度
        if (body.length() < 5) {
            throw new IllegalArgumentException("终端通用应答消息体长度不足，至少需要5字节，实际长度: " + body.length());
        }

        int index = 0;

        // 应答流水号 (2字节)
        responseSerialNumber = body.getUnsignedShort(index);
        index += 2;

        // 应答ID (2字节)
        responseMessageId = body.getUnsignedShort(index);
        index += 2;

        // 结果 (1字节)
        result = body.getByte(index);
    }

    /**
     * 获取结果描述
     */
    public String getResultDescription() {
        switch (result) {
            case RESULT_SUCCESS:
                return "成功/确认";
            case RESULT_FAILURE:
                return "失败";
            case RESULT_MESSAGE_ERROR:
                return "消息有误";
            case RESULT_NOT_SUPPORTED:
                return "不支持";
            default:
                return "未知结果(" + (result & 0xFF) + ")";
        }
    }

    /**
     * 判断是否成功
     */
    public boolean isSuccess() {
        return result == RESULT_SUCCESS;
    }

    // Getters and Setters
    public int getResponseSerialNumber() {
        return responseSerialNumber;
    }

    public void setResponseSerialNumber(int responseSerialNumber) {
        this.responseSerialNumber = responseSerialNumber;
    }

    public int getResponseMessageId() {
        return responseMessageId;
    }

    public void setResponseMessageId(int responseMessageId) {
        this.responseMessageId = responseMessageId;
    }

    public byte getResult() {
        return result;
    }

    public void setResult(byte result) {
        this.result = result;
    }

    @Override
    public String toString() {
        return "T0001TerminalCommonResponse{" +
                "responseSerialNumber=" + responseSerialNumber +
                ", responseMessageId=0x" + Integer.toHexString(responseMessageId).toUpperCase() +
                ", result=" + getResultDescription() +
                ", header=" + getHeader() +
                '}';
    }
}