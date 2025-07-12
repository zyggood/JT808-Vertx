package com.jt808.protocol.message.additional;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.vertx.core.buffer.Buffer;

/**
 * 附加信息基类
 * 所有附加信息类型都应继承此类
 */
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = MileageInfo.class, name = "mileage"),
        @JsonSubTypes.Type(value = FuelInfo.class, name = "fuel"),
        @JsonSubTypes.Type(value = RecordSpeedInfo.class, name = "recordSpeed"),
        @JsonSubTypes.Type(value = ManualAlarmEventInfo.class, name = "manualAlarmEvent"),
        @JsonSubTypes.Type(value = OverspeedAlarmInfo.class, name = "overspeedAlarm"),
        @JsonSubTypes.Type(value = AreaRouteAlarmInfo.class, name = "areaRouteAlarm"),
        @JsonSubTypes.Type(value = RouteTimeAlarmInfo.class, name = "routeTimeAlarm"),
        @JsonSubTypes.Type(value = ExtendedVehicleSignalInfo.class, name = "extendedVehicleSignal"),
        @JsonSubTypes.Type(value = IOStatusInfo.class, name = "ioStatus"),
        @JsonSubTypes.Type(value = AnalogQuantityInfo.class, name = "analogQuantity"),
        @JsonSubTypes.Type(value = SignalStrengthInfo.class, name = "signalStrength"),
        @JsonSubTypes.Type(value = SatelliteCountInfo.class, name = "satelliteCount"),
        @JsonSubTypes.Type(value = CustomInfo.class, name = "custom")
})
public abstract class AdditionalInfo {

    /**
     * 附加信息ID
     */
    protected final int id;

    /**
     * 原始数据长度
     */
    protected final int length;

    /**
     * 构造函数
     *
     * @param id     附加信息ID
     * @param length 数据长度
     */
    protected AdditionalInfo(int id, int length) {
        this.id = id;
        this.length = length;
    }

    /**
     * 获取附加信息ID
     *
     * @return 附加信息ID
     */
    public int getId() {
        return id;
    }

    /**
     * 获取数据长度
     *
     * @return 数据长度
     */
    public int getLength() {
        return length;
    }

    /**
     * 获取附加信息类型名称
     *
     * @return 类型名称
     */
    public abstract String getTypeName();

    /**
     * 获取附加信息描述
     *
     * @return 描述信息
     */
    public abstract String getDescription();

    /**
     * 解析附加信息数据
     *
     * @param data 原始数据
     */
    public abstract void parseData(byte[] data);

    /**
     * 编码附加信息为字节数组
     *
     * @return 编码后的字节数组
     */
    public abstract byte[] encodeData();

    /**
     * 编码完整的附加信息项（包含ID和长度）
     *
     * @return 编码后的Buffer
     */
    public Buffer encode() {
        byte[] data = encodeData();
        Buffer buffer = Buffer.buffer();
        buffer.appendByte((byte) id);
        buffer.appendByte((byte) data.length);
        buffer.appendBytes(data);
        return buffer;
    }

    /**
     * 解析WORD类型数据（2字节，大端序）
     *
     * @param data   数据
     * @param offset 偏移量
     * @return 解析结果
     */
    protected int parseWORD(byte[] data, int offset) {
        if (data.length < offset + 2) return 0;
        return ((data[offset] & 0xFF) << 8) | (data[offset + 1] & 0xFF);
    }

    /**
     * 解析WORD类型数据（2字节，大端序）
     *
     * @param data 数据
     * @return 解析结果
     */
    protected int parseWORD(byte[] data) {
        return parseWORD(data, 0);
    }

    /**
     * 解析DWORD类型数据（4字节，大端序）
     *
     * @param data   数据
     * @param offset 偏移量
     * @return 解析结果
     */
    protected long parseDWORD(byte[] data, int offset) {
        if (data.length < offset + 4) return 0;
        return ((long) (data[offset] & 0xFF) << 24) |
                ((long) (data[offset + 1] & 0xFF) << 16) |
                ((long) (data[offset + 2] & 0xFF) << 8) |
                (long) (data[offset + 3] & 0xFF);
    }

    /**
     * 解析DWORD类型数据（4字节，大端序）
     *
     * @param data 数据
     * @return 解析结果
     */
    protected long parseDWORD(byte[] data) {
        return parseDWORD(data, 0);
    }

    /**
     * 编码WORD类型数据（2字节，大端序）
     *
     * @param value 值
     * @return 编码后的字节数组
     */
    protected byte[] encodeWORD(int value) {
        return new byte[]{
                (byte) ((value >> 8) & 0xFF),
                (byte) (value & 0xFF)
        };
    }

    /**
     * 编码DWORD类型数据（4字节，大端序）
     *
     * @param value 值
     * @return 编码后的字节数组
     */
    protected byte[] encodeDWORD(long value) {
        return new byte[]{
                (byte) ((value >> 24) & 0xFF),
                (byte) ((value >> 16) & 0xFF),
                (byte) ((value >> 8) & 0xFF),
                (byte) (value & 0xFF)
        };
    }

    @Override
    public String toString() {
        return String.format("%s{id=0x%02X, length=%d}",
                getClass().getSimpleName(), id, length);
    }
}