package com.jt808.protocol.message;

import io.vertx.core.buffer.Buffer;

/**
 * 终端注册消息 (0x0100)
 * 终端向平台发起注册请求
 */
public class T0100TerminalRegister extends JT808Message {
    
    /** 省域ID */
    private int provinceId;
    
    /** 市县域ID */
    private int cityId;
    
    /** 制造商ID (5字节) */
    private String manufacturerId;
    
    /** 终端型号 (20字节) */
    private String terminalModel;
    
    /** 终端ID (7字节) */
    private String terminalId;
    
    /** 车牌颜色 */
    private byte plateColor;
    
    /** 车辆标识 */
    private String plateNumber;
    
    public T0100TerminalRegister() {
        super();
    }
    
    public T0100TerminalRegister(JT808Header header) {
        super(header);
    }
    
    @Override
    public int getMessageId() {
        return 0x0100;
    }
    
    @Override
    public Buffer encodeBody() {
        Buffer buffer = Buffer.buffer();
        
        // 省域ID (2字节)
        buffer.appendUnsignedShort(provinceId);
        
        // 市县域ID (2字节)
        buffer.appendUnsignedShort(cityId);
        
        // 制造商ID (5字节，不足补0)
        byte[] manufacturerBytes = new byte[5];
        if (manufacturerId != null) {
            byte[] srcBytes = manufacturerId.getBytes();
            System.arraycopy(srcBytes, 0, manufacturerBytes, 0, Math.min(srcBytes.length, 5));
        }
        buffer.appendBytes(manufacturerBytes);
        
        // 终端型号 (20字节，不足补0)
        byte[] modelBytes = new byte[20];
        if (terminalModel != null) {
            byte[] srcBytes = terminalModel.getBytes();
            System.arraycopy(srcBytes, 0, modelBytes, 0, Math.min(srcBytes.length, 20));
        }
        buffer.appendBytes(modelBytes);
        
        // 终端ID (7字节，不足补0)
        byte[] terminalIdBytes = new byte[7];
        if (terminalId != null) {
            byte[] srcBytes = terminalId.getBytes();
            System.arraycopy(srcBytes, 0, terminalIdBytes, 0, Math.min(srcBytes.length, 7));
        }
        buffer.appendBytes(terminalIdBytes);
        
        // 车牌颜色 (1字节)
        buffer.appendByte(plateColor);
        
        // 车辆标识 (变长)
        if (plateNumber != null) {
            buffer.appendBytes(plateNumber.getBytes());
        }
        
        return buffer;
    }
    
    @Override
    public void decodeBody(Buffer body) {
        int index = 0;
        
        // 省域ID (2字节)
        provinceId = body.getUnsignedShort(index);
        index += 2;
        
        // 市县域ID (2字节)
        cityId = body.getUnsignedShort(index);
        index += 2;
        
        // 制造商ID (5字节)
        byte[] manufacturerBytes = body.getBytes(index, index + 5);
        manufacturerId = new String(manufacturerBytes).trim().replace("\0", "");
        index += 5;
        
        // 终端型号 (20字节)
        byte[] modelBytes = body.getBytes(index, index + 20);
        terminalModel = new String(modelBytes).trim().replace("\0", "");
        index += 20;
        
        // 终端ID (7字节)
        byte[] terminalIdBytes = body.getBytes(index, index + 7);
        terminalId = new String(terminalIdBytes).trim().replace("\0", "");
        index += 7;
        
        // 车牌颜色 (1字节)
        plateColor = body.getByte(index);
        index += 1;
        
        // 车辆标识 (剩余字节)
        if (index < body.length()) {
            byte[] plateBytes = body.getBytes(index, body.length());
            plateNumber = new String(plateBytes);
        } else {
            plateNumber = ""; // 确保空车牌号返回空字符串而不是null
        }
    }
    
    // Getters and Setters
    public int getProvinceId() {
        return provinceId;
    }
    
    public void setProvinceId(int provinceId) {
        this.provinceId = provinceId;
    }
    
    public int getCityId() {
        return cityId;
    }
    
    public void setCityId(int cityId) {
        this.cityId = cityId;
    }
    
    public String getManufacturerId() {
        return manufacturerId;
    }
    
    public void setManufacturerId(String manufacturerId) {
        this.manufacturerId = manufacturerId;
    }
    
    public String getTerminalModel() {
        return terminalModel;
    }
    
    public void setTerminalModel(String terminalModel) {
        this.terminalModel = terminalModel;
    }
    
    public String getTerminalId() {
        return terminalId;
    }
    
    public void setTerminalId(String terminalId) {
        this.terminalId = terminalId;
    }
    
    public byte getPlateColor() {
        return plateColor;
    }
    
    public void setPlateColor(byte plateColor) {
        this.plateColor = plateColor;
    }
    
    public String getPlateNumber() {
        return plateNumber;
    }
    
    public void setPlateNumber(String plateNumber) {
        this.plateNumber = plateNumber;
    }
    
    /**
     * 获取车牌颜色描述
     */
    public String getPlateColorDescription() {
        switch (plateColor) {
            case 1:
                return "蓝色";
            case 2:
                return "黄色";
            case 3:
                return "黑色";
            case 4:
                return "白色";
            case 9:
                return "其他";
            default:
                return "未知(" + (plateColor & 0xFF) + ")";
        }
    }
    
    @Override
    public String toString() {
        return "T0100TerminalRegister{" +
                "provinceId=" + provinceId +
                ", cityId=" + cityId +
                ", manufacturerId='" + manufacturerId + '\'' +
                ", terminalModel='" + terminalModel + '\'' +
                ", terminalId='" + terminalId + '\'' +
                ", plateColor=" + plateColor +
                ", plateNumber='" + plateNumber + '\'' +
                ", header=" + getHeader() +
                '}';
    }
}