package com.jt808.protocol.message;

import io.vertx.core.buffer.Buffer;

/**
 * 终端注册应答消息 (0x8100)
 * 平台对终端注册请求的应答
 */
public class T8100TerminalRegisterResponse extends JT808Message {

    // 结果常量定义
    public static final byte RESULT_SUCCESS = 0x00;                    // 成功
    public static final byte RESULT_VEHICLE_REGISTERED = 0x01;         // 车辆已被注册
    public static final byte RESULT_VEHICLE_NOT_IN_DATABASE = 0x02;    // 数据库中无该车辆
    public static final byte RESULT_TERMINAL_REGISTERED = 0x03;         // 终端已被注册
    public static final byte RESULT_TERMINAL_NOT_IN_DATABASE = 0x04;    // 数据库中无该终端
    /**
     * 应答流水号 (对应的终端注册消息的流水号)
     */
    private int responseSerialNumber;
    /**
     * 结果
     */
    private byte result;
    /**
     * 鉴权码 (只有注册成功时才有此字段)
     */
    private String authCode;

    public T8100TerminalRegisterResponse() {
        super();
    }

    public T8100TerminalRegisterResponse(JT808Header header) {
        super(header);
    }

    /**
     * 构造终端注册应答
     *
     * @param responseSerialNumber 应答流水号
     * @param result               结果
     * @param authCode             鉴权码（成功时）
     */
    public T8100TerminalRegisterResponse(int responseSerialNumber, byte result, String authCode) {
        this.responseSerialNumber = responseSerialNumber;
        this.result = result;
        this.authCode = authCode;
    }

    /**
     * 创建成功的注册应答
     */
    public static T8100TerminalRegisterResponse createSuccessResponse(int serialNumber, String authCode) {
        return new T8100TerminalRegisterResponse(serialNumber, RESULT_SUCCESS, authCode);
    }

    /**
     * 创建失败的注册应答
     */
    public static T8100TerminalRegisterResponse createFailureResponse(int serialNumber, byte result) {
        return new T8100TerminalRegisterResponse(serialNumber, result, null);
    }

    @Override
    public int getMessageId() {
        return 0x8100;
    }

    @Override
    public Buffer encodeBody() {
        Buffer buffer = Buffer.buffer();

        // 应答流水号 (2字节)
        buffer.appendUnsignedShort(responseSerialNumber);

        // 结果 (1字节)
        buffer.appendByte(result);

        // 鉴权码 (只有成功时才有)
        if (result == RESULT_SUCCESS && authCode != null && !authCode.isEmpty()) {
            buffer.appendBytes(authCode.getBytes());
        }

        return buffer;
    }

    @Override
    public void decodeBody(Buffer body) {
        int index = 0;

        // 应答流水号 (2字节)
        responseSerialNumber = body.getUnsignedShort(index);
        index += 2;

        // 结果 (1字节)
        result = body.getByte(index);
        index += 1;

        // 鉴权码 (剩余字节，只有成功时才有)
        if (result == RESULT_SUCCESS && index < body.length()) {
            byte[] authBytes = body.getBytes(index, body.length());
            authCode = new String(authBytes);
        }
    }

    /**
     * 获取结果描述
     */
    public String getResultDescription() {
        switch (result) {
            case RESULT_SUCCESS:
                return "成功";
            case RESULT_VEHICLE_REGISTERED:
                return "车辆已被注册";
            case RESULT_VEHICLE_NOT_IN_DATABASE:
                return "数据库中无该车辆";
            case RESULT_TERMINAL_REGISTERED:
                return "终端已被注册";
            case RESULT_TERMINAL_NOT_IN_DATABASE:
                return "数据库中无该终端";
            default:
                return "未知结果(" + (result & 0xFF) + ")";
        }
    }

    /**
     * 判断是否注册成功
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

    public byte getResult() {
        return result;
    }

    public void setResult(byte result) {
        this.result = result;
    }

    public String getAuthCode() {
        return authCode;
    }

    public void setAuthCode(String authCode) {
        this.authCode = authCode;
    }

    @Override
    public String toString() {
        return "T8100TerminalRegisterResponse{" +
                "responseSerialNumber=" + responseSerialNumber +
                ", result=" + getResultDescription() +
                ", authCode='" + authCode + '\'' +
                ", header=" + getHeader() +
                '}';
    }
}