package com.jt808.protocol.message.additional;

/**
 * 扩展车辆信号状态位信息 (ID: 0x25)
 * DWORD，各位定义见JT/T808标准
 */
public class ExtendedVehicleSignalInfo extends AdditionalInfo {

    /**
     * 车辆信号状态位
     */
    private long signalBits;

    public ExtendedVehicleSignalInfo() {
        super(0x25, 4);
    }

    public ExtendedVehicleSignalInfo(long signalBits) {
        super(0x25, 4);
        this.signalBits = signalBits;
    }

    @Override
    public String getTypeName() {
        return "扩展车辆信号状态位";
    }

    @Override
    public String getDescription() {
        StringBuilder sb = new StringBuilder("车辆信号: ");
        boolean hasSignal = false;

        if (isLowBeam()) {
            sb.append("近光灯 ");
            hasSignal = true;
        }
        if (isHighBeam()) {
            sb.append("远光灯 ");
            hasSignal = true;
        }
        if (isRightTurnSignal()) {
            sb.append("右转向灯 ");
            hasSignal = true;
        }
        if (isLeftTurnSignal()) {
            sb.append("左转向灯 ");
            hasSignal = true;
        }
        if (isBrake()) {
            sb.append("制动 ");
            hasSignal = true;
        }
        if (isReverse()) {
            sb.append("倒车 ");
            hasSignal = true;
        }
        if (isFogLight()) {
            sb.append("雾灯 ");
            hasSignal = true;
        }
        if (isPositionLight()) {
            sb.append("示廓灯 ");
            hasSignal = true;
        }
        if (isHorn()) {
            sb.append("喇叭 ");
            hasSignal = true;
        }
        if (isAirConditioner()) {
            sb.append("空调 ");
            hasSignal = true;
        }
        if (isNeutral()) {
            sb.append("空挡 ");
            hasSignal = true;
        }
        if (isRetarder()) {
            sb.append("缓速器 ");
            hasSignal = true;
        }
        if (isAbs()) {
            sb.append("ABS ");
            hasSignal = true;
        }
        if (isHeater()) {
            sb.append("加热器 ");
            hasSignal = true;
        }
        if (isClutch()) {
            sb.append("离合器 ");
            hasSignal = true;
        }

        if (!hasSignal) {
            sb.append("无");
        }

        return sb.toString().trim();
    }

    @Override
    public void parseData(byte[] data) {
        this.signalBits = parseDWORD(data);
    }

    @Override
    public byte[] encodeData() {
        return encodeDWORD(signalBits);
    }

    // 各种信号状态的getter和setter方法

    public boolean isLowBeam() {
        return (signalBits & 0x00000001) != 0;
    }

    public void setLowBeam(boolean lowBeam) {
        setBit(0x00000001, lowBeam);
    }

    public boolean isHighBeam() {
        return (signalBits & 0x00000002) != 0;
    }

    public void setHighBeam(boolean highBeam) {
        setBit(0x00000002, highBeam);
    }

    public boolean isRightTurnSignal() {
        return (signalBits & 0x00000004) != 0;
    }

    public void setRightTurnSignal(boolean rightTurnSignal) {
        setBit(0x00000004, rightTurnSignal);
    }

    public boolean isLeftTurnSignal() {
        return (signalBits & 0x00000008) != 0;
    }

    public void setLeftTurnSignal(boolean leftTurnSignal) {
        setBit(0x00000008, leftTurnSignal);
    }

    public boolean isBrake() {
        return (signalBits & 0x00000010) != 0;
    }

    public void setBrake(boolean brake) {
        setBit(0x00000010, brake);
    }

    public boolean isReverse() {
        return (signalBits & 0x00000020) != 0;
    }

    public void setReverse(boolean reverse) {
        setBit(0x00000020, reverse);
    }

    public boolean isFogLight() {
        return (signalBits & 0x00000040) != 0;
    }

    public void setFogLight(boolean fogLight) {
        setBit(0x00000040, fogLight);
    }

    public boolean isPositionLight() {
        return (signalBits & 0x00000080) != 0;
    }

    public void setPositionLight(boolean positionLight) {
        setBit(0x00000080, positionLight);
    }

    public boolean isHorn() {
        return (signalBits & 0x00000100) != 0;
    }

    public void setHorn(boolean horn) {
        setBit(0x00000100, horn);
    }

    public boolean isAirConditioner() {
        return (signalBits & 0x00000200) != 0;
    }

    public void setAirConditioner(boolean airConditioner) {
        setBit(0x00000200, airConditioner);
    }

    public boolean isNeutral() {
        return (signalBits & 0x00000400) != 0;
    }

    public void setNeutral(boolean neutral) {
        setBit(0x00000400, neutral);
    }

    public boolean isRetarder() {
        return (signalBits & 0x00000800) != 0;
    }

    public void setRetarder(boolean retarder) {
        setBit(0x00000800, retarder);
    }

    public boolean isAbs() {
        return (signalBits & 0x00001000) != 0;
    }

    public void setAbs(boolean abs) {
        setBit(0x00001000, abs);
    }

    public boolean isHeater() {
        return (signalBits & 0x00002000) != 0;
    }

    public void setHeater(boolean heater) {
        setBit(0x00002000, heater);
    }

    public boolean isClutch() {
        return (signalBits & 0x00004000) != 0;
    }

    public void setClutch(boolean clutch) {
        setBit(0x00004000, clutch);
    }

    /**
     * 获取原始信号位
     *
     * @return 信号位
     */
    public long getSignalBits() {
        return signalBits;
    }

    /**
     * 设置原始信号位
     *
     * @param signalBits 信号位
     */
    public void setSignalBits(long signalBits) {
        this.signalBits = signalBits;
    }

    /**
     * 设置指定位的状态
     *
     * @param mask  位掩码
     * @param value 状态值
     */
    private void setBit(long mask, boolean value) {
        if (value) {
            signalBits |= mask;
        } else {
            signalBits &= ~mask;
        }
    }

    @Override
    public String toString() {
        return String.format("ExtendedVehicleSignalInfo{id=0x%02X, signalBits=0x%08X, description='%s'}",
                id, signalBits, getDescription());
    }
}