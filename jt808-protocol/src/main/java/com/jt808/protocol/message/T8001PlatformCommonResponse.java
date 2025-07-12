package com.jt808.protocol.message;

import io.vertx.core.buffer.Buffer;

/**
 * 平台通用应答消息 (0x8001)
 * 平台对终端消息的通用应答
 */
public class T8001PlatformCommonResponse extends JT808Message {

    // 结果常量定义
    public static final byte RESULT_SUCCESS = 0x00;           // 成功/确认
    public static final byte RESULT_FAILURE = 0x01;           // 失败
    public static final byte RESULT_MESSAGE_ERROR = 0x02;     // 消息有误
    public static final byte RESULT_NOT_SUPPORTED = 0x03;     // 不支持
    /**
     * 应答流水号 (对应的终端消息的流水号)
     */
    private int responseSerialNumber;
    /**
     * 应答ID (对应的终端消息的ID)
     */
    private int responseMessageId;
    /**
     * 结果
     */
    private byte result;

    public T8001PlatformCommonResponse() {
        super();
    }

    public T8001PlatformCommonResponse(JT808Header header) {
        super(header);
    }

    /**
     * 构造平台通用应答
     *
     * @param responseSerialNumber 应答流水号
     * @param responseMessageId    应答消息ID
     * @param result               结果
     */
    public T8001PlatformCommonResponse(int responseSerialNumber, int responseMessageId, byte result) {
        this.responseSerialNumber = responseSerialNumber;
        this.responseMessageId = responseMessageId;
        this.result = result;
    }

    /**
     * 创建成功应答
     */
    public static T8001PlatformCommonResponse createSuccessResponse(int serialNumber, int messageId) {
        T8001PlatformCommonResponse response = new T8001PlatformCommonResponse();
        response.setResponseSerialNumber(serialNumber);
        response.setResponseMessageId(messageId);
        response.setResult(RESULT_SUCCESS);
        return response;
    }

    /**
     * 创建失败应答
     */
    public static T8001PlatformCommonResponse createFailureResponse(int serialNumber, int messageId, byte result) {
        T8001PlatformCommonResponse response = new T8001PlatformCommonResponse();
        response.setResponseSerialNumber(serialNumber);
        response.setResponseMessageId(messageId);
        response.setResult(result);
        return response;
    }

    @Override
    public int getMessageId() {
        return 0x8001;
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
        return "T8001PlatformCommonResponse{" +
                "responseSerialNumber=" + responseSerialNumber +
                ", responseMessageId=0x" + Integer.toHexString(responseMessageId).toUpperCase() +
                ", result=" + getResultDescription() +
                ", header=" + getHeader() +
                '}';
    }
}