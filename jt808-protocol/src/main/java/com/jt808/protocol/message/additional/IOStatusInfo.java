package com.jt808.protocol.message.additional;

/**
 * IO状态位 (ID: 0x2A)
 * WORD，定义见JT/T 808-2011表29
 */
public class IOStatusInfo extends AdditionalInfo {

    /**
     * IO状态位
     */
    private int ioStatus;

    public IOStatusInfo() {
        super(0x2A, 2);
    }

    public IOStatusInfo(int ioStatus) {
        super(0x2A, 2);
        this.ioStatus = ioStatus;
    }

    @Override
    public String getTypeName() {
        return "IO状态位";
    }

    @Override
    public String getDescription() {
        StringBuilder sb = new StringBuilder("IO状态位: ");
        if (ioStatus == 0) {
            sb.append("无");
        } else {
            boolean first = true;
            for (int i = 0; i < 16; i++) {
                if ((ioStatus & (1 << i)) != 0) {
                    if (!first) sb.append(", ");
                    sb.append("IO").append(i);
                    first = false;
                }
            }
        }
        return sb.toString();
    }

    @Override
    public void parseData(byte[] data) {
        this.ioStatus = parseWORD(data);
    }

    @Override
    public byte[] encodeData() {
        return encodeWORD(ioStatus);
    }

    /**
     * 检查指定IO位是否设置
     *
     * @param bit IO位（0-15）
     * @return 是否设置
     */
    public boolean isIOSet(int bit) {
        if (bit < 0 || bit > 15) {
            return false;
        }
        return (ioStatus & (1 << bit)) != 0;
    }

    /**
     * 设置指定IO位
     *
     * @param bit   IO位（0-15）
     * @param value 是否设置
     */
    public void setIO(int bit, boolean value) {
        if (bit < 0 || bit > 15) {
            return;
        }
        if (value) {
            ioStatus |= (1 << bit);
        } else {
            ioStatus &= ~(1 << bit);
        }
    }

    /**
     * 获取IO状态位
     *
     * @return IO状态位
     */
    public int getIoStatus() {
        return ioStatus;
    }

    /**
     * 设置IO状态位
     *
     * @param ioStatus IO状态位
     */
    public void setIoStatus(int ioStatus) {
        this.ioStatus = ioStatus;
    }

    @Override
    public String toString() {
        return String.format("IOStatusInfo{id=0x%02X, ioStatus=0x%04X}", id, ioStatus);
    }
}

