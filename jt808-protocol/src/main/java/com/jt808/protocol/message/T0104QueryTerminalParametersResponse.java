package com.jt808.protocol.message;

import io.vertx.core.buffer.Buffer;

import java.util.ArrayList;
import java.util.List;

/**
 * 查询终端参数应答消息 (0x0104)
 * 终端对平台查询终端参数的应答
 */
public class T0104QueryTerminalParametersResponse extends JT808Message {

    /**
     * 应答流水号
     */
    private int responseSerialNumber;

    /**
     * 参数项列表
     */
    private List<ParameterItem> parameterItems;

    public T0104QueryTerminalParametersResponse() {
        super();
        this.parameterItems = new ArrayList<>();
    }

    public T0104QueryTerminalParametersResponse(JT808Header header) {
        super(header);
        this.parameterItems = new ArrayList<>();
    }

    public T0104QueryTerminalParametersResponse(int responseSerialNumber) {
        super();
        this.responseSerialNumber = responseSerialNumber;
        this.parameterItems = new ArrayList<>();
    }


    @Override
    public int getMessageId() {
        return 0x0104;
    }

    @Override
    public Buffer encodeBody() {
        Buffer buffer = Buffer.buffer();

        // 应答流水号 (2字节)
        buffer.appendUnsignedShort(responseSerialNumber);

        // 应答参数个数 (1字节)
        buffer.appendByte((byte) parameterItems.size());

        // 参数项列表
        for (ParameterItem item : parameterItems) {
            // 参数ID (4字节)
            buffer.appendUnsignedInt(item.getParameterId());

            // 参数值字节数组
            byte[] valueBytes = item.getValueBytes();

            // 参数长度 (1字节)
            buffer.appendByte((byte) valueBytes.length);

            // 参数值
            buffer.appendBytes(valueBytes);
        }

        return buffer;
    }

    @Override
    public void decodeBody(Buffer body) {
        if (body == null || body.length() < 3) {
            return;
        }

        int index = 0;
        parameterItems.clear();

        // 应答流水号 (2字节)
        responseSerialNumber = body.getUnsignedShort(index);
        index += 2;

        // 应答参数个数 (1字节)
        int parameterCount = body.getUnsignedByte(index);
        index += 1;

        // 解析参数项列表
        for (int i = 0; i < parameterCount; i++) {
            if (index + 5 > body.length()) {
                break; // 防止数组越界
            }

            // 参数ID (4字节)
            long parameterId = body.getUnsignedInt(index);
            index += 4;

            // 参数长度 (1字节)
            int parameterLength = body.getUnsignedByte(index);
            index += 1;

            if (index + parameterLength > body.length()) {
                break; // 防止数组越界
            }

            // 参数值
            byte[] valueBytes = body.getBytes(index, index + parameterLength);
            index += parameterLength;

            // 创建参数项
            ParameterItem item = new ParameterItem(parameterId, valueBytes);
            parameterItems.add(item);
        }
    }

    /**
     * 添加DWORD类型参数
     */
    public void addDwordParameter(long parameterId, long value) {
        parameterItems.add(ParameterItem.createDwordParameter(parameterId, value));
    }

    /**
     * 添加WORD类型参数
     */
    public void addWordParameter(long parameterId, int value) {
        parameterItems.add(ParameterItem.createWordParameter(parameterId, value));
    }

    /**
     * 添加BYTE类型参数
     */
    public void addByteParameter(long parameterId, byte value) {
        parameterItems.add(ParameterItem.createByteParameter(parameterId, value));
    }

    /**
     * 添加STRING类型参数
     */
    public void addStringParameter(long parameterId, String value) {
        parameterItems.add(ParameterItem.createStringParameter(parameterId, value));
    }

    /**
     * 添加字节数组类型参数
     */
    public void addBytesParameter(long parameterId, byte[] value) {
        parameterItems.add(new ParameterItem(parameterId, value));
    }

    /**
     * 添加参数项
     */
    public void addParameterItem(ParameterItem item) {
        if (item != null) {
            parameterItems.add(item);
        }
    }

    /**
     * 获取DWORD类型参数值
     */
    public Long getDwordParameter(long parameterId) {
        ParameterItem item = getParameterItem(parameterId);
        return item != null ? item.getDwordValue() : null;
    }

    /**
     * 获取WORD类型参数值
     */
    public Integer getWordParameter(long parameterId) {
        ParameterItem item = getParameterItem(parameterId);
        return item != null ? item.getWordValue() : null;
    }

    /**
     * 获取BYTE类型参数值
     */
    public Byte getByteParameter(long parameterId) {
        ParameterItem item = getParameterItem(parameterId);
        return item != null ? item.getByteValue() : null;
    }

    /**
     * 获取STRING类型参数值
     */
    public String getStringParameter(long parameterId) {
        ParameterItem item = getParameterItem(parameterId);
        return item != null ? item.getStringValue() : null;
    }

    /**
     * 获取参数项
     */
    public ParameterItem getParameterItem(long parameterId) {
        return parameterItems.stream()
                .filter(item -> item.getParameterId() == parameterId)
                .findFirst()
                .orElse(null);
    }

    /**
     * 移除参数
     */
    public boolean removeParameter(long parameterId) {
        return parameterItems.removeIf(item -> item.getParameterId() == parameterId);
    }

    /**
     * 清空所有参数
     */
    public void clearParameters() {
        parameterItems.clear();
    }

    // Getters and Setters
    public int getResponseSerialNumber() {
        return responseSerialNumber;
    }

    public void setResponseSerialNumber(int responseSerialNumber) {
        this.responseSerialNumber = responseSerialNumber;
    }

    public List<ParameterItem> getParameterItems() {
        return new ArrayList<>(parameterItems);
    }

    public void setParameterItems(List<ParameterItem> parameterItems) {
        this.parameterItems = parameterItems != null ? new ArrayList<>(parameterItems) : new ArrayList<>();
    }

    /**
     * 获取应答参数个数
     */
    public int getParameterCount() {
        return parameterItems.size();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("T0104QueryTerminalParametersResponse{");
        sb.append("responseSerialNumber=").append(responseSerialNumber);
        sb.append(", parameterCount=").append(parameterItems.size());
        sb.append(", parameters=[");
        for (int i = 0; i < parameterItems.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append(parameterItems.get(i));
        }
        sb.append("], header=").append(getHeader());
        sb.append('}');
        return sb.toString();
    }
}
