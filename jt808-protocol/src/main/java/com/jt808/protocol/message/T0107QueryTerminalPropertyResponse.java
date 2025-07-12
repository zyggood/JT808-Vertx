package com.jt808.protocol.message;

import com.jt808.common.util.ByteUtils;
import io.vertx.core.buffer.Buffer;

/**
 * T0107查询终端属性应答
 * 消息ID: 0x0107
 * <p>
 * 终端对平台查询终端属性指令(0x8107)的应答消息
 * 消息体包含终端的各种属性信息
 *
 * @author JT808 Protocol Team
 */
public class T0107QueryTerminalPropertyResponse extends JT808Message {

    /**
     * 消息ID常量
     */
    public static final int MESSAGE_ID = 0x0107;

    // 终端类型
    private int terminalType;

    // 制造商ID（5字节）
    private String manufacturerId;

    // 终端型号（20字节）
    private String terminalModel;

    // 终端ID（7字节）
    private String terminalId;

    // 终端SIM卡ICCID（10字节BCD码）
    private String iccid;

    // 终端硬件版本号长度
    private int hardwareVersionLength;

    // 终端硬件版本号
    private String hardwareVersion;

    // 终端固件版本号长度
    private int firmwareVersionLength;

    // 终端固件版本号
    private String firmwareVersion;

    // GNSS模块属性
    private int gnssAttribute;

    // 通信模块属性
    private int communicationAttribute;

    /**
     * 默认构造函数
     */
    public T0107QueryTerminalPropertyResponse() {
        this.manufacturerId = "";
        this.terminalModel = "";
        this.terminalId = "";
        this.iccid = "";
        this.hardwareVersion = "";
        this.firmwareVersion = "";
    }

    /**
     * 完整构造函数
     */
    public T0107QueryTerminalPropertyResponse(int terminalType, String manufacturerId,
                                              String terminalModel, String terminalId, String iccid,
                                              String hardwareVersion, String firmwareVersion,
                                              int gnssAttribute, int communicationAttribute) {
        this.terminalType = terminalType;
        this.manufacturerId = manufacturerId != null ? manufacturerId : "";
        this.terminalModel = terminalModel != null ? terminalModel : "";
        this.terminalId = terminalId != null ? terminalId : "";
        this.iccid = iccid != null ? iccid : "";
        this.hardwareVersion = hardwareVersion != null ? hardwareVersion : "";
        this.firmwareVersion = firmwareVersion != null ? firmwareVersion : "";
        this.gnssAttribute = gnssAttribute;
        this.communicationAttribute = communicationAttribute;
        this.hardwareVersionLength = this.hardwareVersion.getBytes().length;
        this.firmwareVersionLength = this.firmwareVersion.getBytes().length;
    }

    @Override
    public int getMessageId() {
        return MESSAGE_ID;
    }

    @Override
    public Buffer encodeBody() {
        Buffer buffer = Buffer.buffer();

        // 终端类型（2字节）
        buffer.appendUnsignedShort(terminalType);

        // 制造商ID（5字节，不足补0x00）
        byte[] manufacturerBytes = new byte[5];
        byte[] srcBytes = manufacturerId.getBytes();
        System.arraycopy(srcBytes, 0, manufacturerBytes, 0, Math.min(srcBytes.length, 5));
        buffer.appendBytes(manufacturerBytes);

        // 终端型号（20字节，不足补0x00）
        byte[] modelBytes = new byte[20];
        srcBytes = terminalModel.getBytes();
        System.arraycopy(srcBytes, 0, modelBytes, 0, Math.min(srcBytes.length, 20));
        buffer.appendBytes(modelBytes);

        // 终端ID（7字节，不足补0x00）
        byte[] idBytes = new byte[7];
        srcBytes = terminalId.getBytes();
        System.arraycopy(srcBytes, 0, idBytes, 0, Math.min(srcBytes.length, 7));
        buffer.appendBytes(idBytes);

        // 终端SIM卡ICCID（10字节BCD码）
        byte[] iccidBytes = ByteUtils.encodeBcd(iccid, 10);
        buffer.appendBytes(iccidBytes);

        // 终端硬件版本号长度（1字节）
        buffer.appendUnsignedByte((short) hardwareVersionLength);

        // 终端硬件版本号
        if (hardwareVersionLength > 0) {
            buffer.appendBytes(hardwareVersion.getBytes());
        }

        // 终端固件版本号长度（1字节）
        buffer.appendUnsignedByte((short) firmwareVersionLength);

        // 终端固件版本号
        if (firmwareVersionLength > 0) {
            buffer.appendBytes(firmwareVersion.getBytes());
        }

        // GNSS模块属性（1字节）
        buffer.appendUnsignedByte((short) gnssAttribute);

        // 通信模块属性（1字节）
        buffer.appendUnsignedByte((short) communicationAttribute);

        return buffer;
    }

    @Override
    public void decodeBody(Buffer body) {
        if (body == null || body.length() < 37) {
            throw new IllegalArgumentException("消息体长度不足");
        }

        int offset = 0;

        // 终端类型（2字节）
        terminalType = body.getUnsignedShort(offset);
        offset += 2;

        // 制造商ID（5字节）
        byte[] manufacturerBytes = new byte[5];
        body.getBytes(offset, offset + 5, manufacturerBytes);
        manufacturerId = new String(manufacturerBytes).trim().replace("\0", "");
        offset += 5;

        // 终端型号（20字节）
        byte[] modelBytes = new byte[20];
        body.getBytes(offset, offset + 20, modelBytes);
        terminalModel = new String(modelBytes).trim().replace("\0", "");
        offset += 20;

        // 终端ID（7字节）
        byte[] idBytes = new byte[7];
        body.getBytes(offset, offset + 7, idBytes);
        terminalId = new String(idBytes).trim().replace("\0", "");
        offset += 7;

        // 终端SIM卡ICCID（10字节BCD码）
        byte[] iccidBytes = new byte[10];
        body.getBytes(offset, offset + 10, iccidBytes);
        iccid = ByteUtils.decodeBcd(iccidBytes);
        offset += 10;

        // 终端硬件版本号长度（1字节）
        hardwareVersionLength = body.getUnsignedByte(offset);
        offset += 1;

        // 终端硬件版本号
        if (hardwareVersionLength > 0 && offset + hardwareVersionLength <= body.length()) {
            byte[] hwBytes = new byte[hardwareVersionLength];
            body.getBytes(offset, offset + hardwareVersionLength, hwBytes);
            hardwareVersion = new String(hwBytes);
            offset += hardwareVersionLength;
        } else {
            hardwareVersion = "";
        }

        // 终端固件版本号长度（1字节）
        if (offset < body.length()) {
            firmwareVersionLength = body.getUnsignedByte(offset);
            offset += 1;

            // 终端固件版本号
            if (firmwareVersionLength > 0 && offset + firmwareVersionLength <= body.length()) {
                byte[] fwBytes = new byte[firmwareVersionLength];
                body.getBytes(offset, offset + firmwareVersionLength, fwBytes);
                firmwareVersion = new String(fwBytes);
                offset += firmwareVersionLength;
            } else {
                firmwareVersion = "";
            }
        }

        // GNSS模块属性（1字节）
        if (offset < body.length()) {
            gnssAttribute = body.getUnsignedByte(offset);
            offset += 1;
        }

        // 通信模块属性（1字节）
        if (offset < body.length()) {
            communicationAttribute = body.getUnsignedByte(offset);
        }
    }


    // Getters and Setters
    public int getTerminalType() {
        return terminalType;
    }

    public void setTerminalType(int terminalType) {
        this.terminalType = terminalType;
    }

    public String getManufacturerId() {
        return manufacturerId;
    }

    public void setManufacturerId(String manufacturerId) {
        this.manufacturerId = manufacturerId != null ? manufacturerId : "";
    }

    public String getTerminalModel() {
        return terminalModel;
    }

    public void setTerminalModel(String terminalModel) {
        this.terminalModel = terminalModel != null ? terminalModel : "";
    }

    public String getTerminalId() {
        return terminalId;
    }

    public void setTerminalId(String terminalId) {
        this.terminalId = terminalId != null ? terminalId : "";
    }

    public String getIccid() {
        return iccid;
    }

    public void setIccid(String iccid) {
        this.iccid = iccid != null ? iccid : "";
    }

    public int getHardwareVersionLength() {
        return hardwareVersionLength;
    }

    public String getHardwareVersion() {
        return hardwareVersion;
    }

    public void setHardwareVersion(String hardwareVersion) {
        this.hardwareVersion = hardwareVersion != null ? hardwareVersion : "";
        this.hardwareVersionLength = this.hardwareVersion.getBytes().length;
    }

    public int getFirmwareVersionLength() {
        return firmwareVersionLength;
    }

    public String getFirmwareVersion() {
        return firmwareVersion;
    }

    public void setFirmwareVersion(String firmwareVersion) {
        this.firmwareVersion = firmwareVersion != null ? firmwareVersion : "";
        this.firmwareVersionLength = this.firmwareVersion.getBytes().length;
    }

    public int getGnssAttribute() {
        return gnssAttribute;
    }

    public void setGnssAttribute(int gnssAttribute) {
        this.gnssAttribute = gnssAttribute;
    }

    public int getCommunicationAttribute() {
        return communicationAttribute;
    }

    public void setCommunicationAttribute(int communicationAttribute) {
        this.communicationAttribute = communicationAttribute;
    }

    @Override
    public String toString() {
        return "T0107QueryTerminalPropertyResponse{" +
                "header=" + getHeader() +
                ", terminalType=" + terminalType +
                ", manufacturerId='" + manufacturerId + '\'' +
                ", terminalModel='" + terminalModel + '\'' +
                ", terminalId='" + terminalId + '\'' +
                ", iccid='" + iccid + '\'' +
                ", hardwareVersion='" + hardwareVersion + '\'' +
                ", firmwareVersion='" + firmwareVersion + '\'' +
                ", gnssAttribute=" + gnssAttribute +
                ", communicationAttribute=" + communicationAttribute +
                '}';
    }
}